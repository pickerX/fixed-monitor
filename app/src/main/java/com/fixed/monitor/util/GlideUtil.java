package com.fixed.monitor.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class GlideUtil {
    //设置加载中以及加载失败图片
    public static void loadImageDefult(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).placeholder(mImageView.getDrawable()).dontAnimate()
//                .error(R.mipmap.ic_noimage2)
                .into(mImageView);
    }

    public static void loadMp4Frame(Context mContext,String path,ImageView mImageView){
        RequestOptions options = new RequestOptions();
        options.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL)
                .frame(1000000)
                .centerCrop();
        Glide.with(mContext)
                .setDefaultRequestOptions(options)
                .load(path)
                .into(mImageView);
    }

}
