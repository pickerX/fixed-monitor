package com.fixed.monitor.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.BaseFragment;
import com.fixed.monitor.base.adapter.MCommAdapter;
import com.fixed.monitor.base.adapter.MCommVH;
import com.fixed.monitor.bean.VideoRecordBean;
import com.fixed.monitor.model.dbdao.IVideoRecordDao;
import com.fixed.monitor.model.dbdao.impl.VideoRecordDaoImpl;
import com.fixed.monitor.model.popup.PopupInputPswView;
import com.fixed.monitor.model.video.VideoListAct;
import com.fixed.monitor.model.video.VideoPlayerAct;
import com.fixed.monitor.util.GlideUtil;
import com.fixed.monitor.util.T;
import com.fixed.monitor.util.ToolUtil;
import com.fixed.monitor.view.MyDatePickerDialog;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class VideoListFragment extends BaseFragment {


    private SmartRefreshLayout refreshLayout;
    private RecyclerView rcv;
    private TextView searchDate_tv;
    private MCommAdapter<VideoRecordBean> commAdapter;

    private int pageSize = 10;
    private IVideoRecordDao dao;

    private View selectDateView;
    private DatePickerDialog datePickerDialog;

    private long startTime, endTime;

    @Override
    public int setLayoutID() {
        return R.layout.fragment_videolist;
    }

    @Override
    public void initView(View view) {
        searchDate_tv = view.findViewById(R.id.searchDate_tv);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getData(true);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getData(false);
            }
        });
        rcv = view.findViewById(R.id.rcv);
        rcv.setLayoutManager(new LinearLayoutManager(getContext()));
        commAdapter = new MCommAdapter(getContext(), new MCommVH.MCommVHInterface<VideoRecordBean>() {
            @Override
            public int setLayout() {
                return R.layout.view_videolist_item2;
            }

            @Override
            public void bindData(Context context, MCommVH mCommVH, int position, VideoRecordBean o) {
                mCommVH.setText(R.id.videoname_tv, o.videoName);
                mCommVH.setText(R.id.createtime_tv, ToolUtil.timestamp2String(o.videoCreateTime, ""));
                mCommVH.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        VideoPlayerAct.openAct(getContext(), o.videoName, o.videoCachePath);
                    }
                });
            }
        });
        commAdapter.setShowEmptyView(true);
        rcv.setAdapter(commAdapter);

        selectDateView = view.findViewById(R.id.seledate_ll);
        selectDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                MyDatePickerDialog dpd = MyDatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
//                                Log.i("jjjjjjjjaack",year+"_"+monthOfYear+"_"+dayOfMonth);
                                try {
                                    monthOfYear++;
                                    searchDate_tv.setText(year + "年" + monthOfYear + "月" + dayOfMonth + "日");
                                    String dateStr = year + "-" + monthOfYear + "-" + dayOfMonth;
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    Date date = null;
                                    date = simpleDateFormat.parse(dateStr);
                                    long starts = date.getTime();
                                    long ends = starts + 24 * 60 * 60 * 1000;
                                    startTime = starts;
                                    endTime = ends;
                                    getData(true);
                                } catch (Exception e) {
                                    T.showShort(getContext(), "选择时间失败");
                                }
                            }
                        },
                        now.get(Calendar.YEAR), // Initial year selection
                        now.get(Calendar.MONTH), // Initial month selection
                        now.get(Calendar.DAY_OF_MONTH) // Inital day selection
                );
// If you're calling this from a support Fragment
                dpd.show(getFragmentManager(), "Datepickerdialog");
                dpd.setMyDatePickerDialogInterface(new MyDatePickerDialog.MyDatePickerDialogInterface() {
                    @Override
                    public void doCancleClick() {
//                        Log.i("jjjjjjjjaack","1212");
                        searchDate_tv.setText("日期筛选");
                        startTime = 0;
                        endTime = 0;
                        getData(true);
                    }
                });
            }
        });
    }

    @Override
    public void doBusiness() {
        dao = new VideoRecordDaoImpl(getContext());
        PopupInputPswView popupInputPswView = new PopupInputPswView(getContext(), new PopupInputPswView.PopupInputPswViewInterface() {
            @Override
            public void success() {

                refreshLayout.autoRefresh();
            }
        });
        popupInputPswView.showCenter(refreshLayout);
    }

    /**
     * @param
     * @return
     * @description 查询数据
     * @author jiejack
     * @time 2022/2/4 9:50 下午
     */
    public void getData(boolean isRefresh) {
        if (isRefresh) {
            commAdapter.setData(dao.queryByDate_Page(0, pageSize, startTime, endTime));
        } else {
            int offset = commAdapter.getSize();
            commAdapter.addData(dao.queryByDate_Page(offset, pageSize, startTime, endTime));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
            }
        }, 1000);
    }


    @Override
    public void doWeakUp() {

    }
}
