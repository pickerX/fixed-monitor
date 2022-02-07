package com.fixed.monitor.model.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.adapter.MCommAdapter;
import com.fixed.monitor.base.adapter.MCommVH;
import com.fixed.monitor.bean.VideoBean;
import com.fixed.monitor.base.BaseAct;
import com.fixed.monitor.bean.VideoRecordBean;
import com.fixed.monitor.model.dbdao.IVideoRecordDao;
import com.fixed.monitor.model.dbdao.impl.VideoRecordDaoImpl;
import com.fixed.monitor.util.DataUtil;
import com.fixed.monitor.util.GlideUtil;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

public class VideoListAct extends BaseAct {

    private SmartRefreshLayout refreshLayout;
    private RecyclerView rcv;
    private MCommAdapter<VideoRecordBean> commAdapter;

    private int pageSize =10;
    private IVideoRecordDao dao;

    @Override
    public int setLayoutID() {
        return R.layout.act_videolist;
    }

    @Override
    public void initView() {
        setTitleTx("回放列表");
        refreshLayout = findViewById(R.id.refreshLayout);
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

        rcv = findViewById(R.id.rcv);
        rcv.setLayoutManager(new LinearLayoutManager(this));
        commAdapter = new MCommAdapter(this, new MCommVH.MCommVHInterface<VideoRecordBean>() {
            @Override
            public int setLayout() {
                return R.layout.view_videolist_item;
            }

            @Override
            public void bindData(Context context, MCommVH mCommVH, int position, VideoRecordBean o) {
//                GlideUtil.loadMp4Frame(context,o.videoCachePath, (ImageView) mCommVH.getView(R.id.videologo_iv));
                GlideUtil.loadImageDefult(context,o.videoCover, (ImageView) mCommVH.getView(R.id.videologo_iv));

//                MediaMetadataRetriever media = new MediaMetadataRetriever();
//                media.setDataSource(o.videoCachePath);// videoPath 本地视频的路径
//                Bitmap bitmap  = media.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//                ( (ImageView) (mCommVH.getView(R.id.videologo_iv))).setImageBitmap(bitmap);

                mCommVH.setText(R.id.videoname_tv, o.videoName);
                mCommVH.setText(R.id.videotime_tv, "时长："+o.videoDuringTime+"秒");
                mCommVH.setText(R.id.videocache_tv, o.videoCachePath);
                mCommVH.loadImageResourceByGilde(R.id.videologo_iv, o.videoCover);
                mCommVH.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        VideoPlayerAct.openAct(VideoListAct.this, o.videoName, o.videoCachePath);
                    }
                });

            }
        });
        commAdapter.setShowEmptyView(true);
        rcv.setAdapter(commAdapter);
    }

    @Override
    public void doBusiness() {
        dao = new VideoRecordDaoImpl(this);
        refreshLayout.autoRefresh();
//        commAdapter.setData(DataUtil.getVideoList());
    }



    /**
     * @param
     * @return
     * @description 查询数据
     * @author jiejack
     * @time 2022/2/4 9:50 下午
     */
    public void getData(boolean isRefresh){
        if(isRefresh){
            commAdapter.setData(dao.queryByDate_Page(0,pageSize,0,0));
        }else{
            int offset = commAdapter.getSize();
            commAdapter.addData(dao.queryByDate_Page(offset,pageSize,0,0));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
            }
        },2000);
    }

    @Override
    public void doWeakUp() {

    }
}
