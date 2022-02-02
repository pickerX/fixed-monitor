package com.fixed.monitor.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class GlideUtil {
    //设置加载中以及加载失败图片
    public static void loadImageDefult(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).placeholder(mImageView.getDrawable()).dontAnimate()
//                .error(R.mipmap.ic_noimage2)
                .into(mImageView);
    }

}
