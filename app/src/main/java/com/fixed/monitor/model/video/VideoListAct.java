package com.fixed.monitor.model.video;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.adapter.MCommAdapter;
import com.fixed.monitor.base.adapter.MCommVH;
import com.fixed.monitor.bean.VideoBean;
import com.fixed.monitor.model.base.BaseAct;
import com.fixed.monitor.util.DataUtil;

import java.util.ArrayList;

public class VideoListAct extends BaseAct {

    private RecyclerView rcv;
    private MCommAdapter<VideoBean> commAdapter;

    @Override
    public int setLayoutID() {
        return R.layout.act_videolist;
    }

    @Override
    public void initView() {
        setTitleTx("回放列表");
        rcv = findViewById(R.id.rcv);
        rcv.setLayoutManager(new LinearLayoutManager(this));
        commAdapter = new MCommAdapter(this, new MCommVH.MCommVHInterface<VideoBean>() {
            @Override
            public int setLayout() {
                return R.layout.view_videolist_item;
            }

            @Override
            public void bindData(Context context, MCommVH mCommVH, int position, VideoBean o) {

//                ImageView videologo_iv = (ImageView) mCommVH.getView(R.id.videologo_iv);
//                TextView videoname_tv = (TextView) mCommVH.getView(R.id.videoname_tv);
//                TextView videotime_tv = (TextView) mCommVH.getView(R.id.videotime_tv);
//                TextView videocache_tv = (TextView) mCommVH.getView(R.id.videocache_tv);

                mCommVH.setText(R.id.videoname_tv, o.getTitle());
                mCommVH.setText(R.id.videotime_tv, "时长：30分钟");
                mCommVH.setText(R.id.videocache_tv, o.getUrl());
                mCommVH.loadImageResourceByGilde(R.id.videologo_iv, o.getThumb());
                mCommVH.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        VideoPlayerAct.openAct(VideoListAct.this, o.getTitle(), o.getUrl());
                    }
                });

            }
        });
        commAdapter.setShowEmptyView(true);
        rcv.setAdapter(commAdapter);
    }

    @Override
    public void doBusiness() {
        commAdapter.setData(DataUtil.getVideoList());
    }

    @Override
    public void doWeakUp() {

    }
}
