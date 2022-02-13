package com.lib.camera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

/**
 * @author pickerx
 * @date 2022/2/4 10:12 上午
 */
public class AutoFitSurfaceView extends SurfaceView {
    public AutoFitSurfaceView(Context context) {
        super(context);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private int maxWidth;
    private int maxHeight;
    private float aspectRatio = 0f;

    /**
     * Sets the aspect ratio for this view. The size of the view will be
     * measured based on the ratio calculated from the parameters.
     *
     * @param width  Camera resolution horizontal size
     * @param height Camera resolution vertical size
     */
    public void setAspectRatio(int width, int height) throws IllegalAccessException {
        if (width < 0 || height < 0) {
            throw new IllegalAccessException("Size cannot be negative");
        }
        aspectRatio = (float) width / (float) height;
        getHolder().setFixedSize(width, height);
        requestLayout();
    }


    public void setMaxSize(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth < 0 ? 0 : maxWidth;
        this.maxHeight = maxHeight < 0 ? 0 : maxHeight;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (aspectRatio == 0f) {
            setMeasuredDimension(width, height);
        } else {
            // Performs center-crop transformation of the camera frames
            int newWidth;
            int newHeight;
            float actualRatio = 0f;
            if (width > height)
                actualRatio = aspectRatio;
            else
                actualRatio = 1f / aspectRatio;

            if (width < height) {
                if (maxHeight != 0 && height > maxHeight) {
                    newHeight = maxHeight;
                    newWidth = Math.round(maxHeight * actualRatio);
                } else {
                    newHeight = height;
                    newWidth = Math.round(height * actualRatio);
                }
            } else {
                if (maxWidth != 0 && width > maxWidth) {
                    newWidth = maxWidth;
                    newHeight = Math.round(maxWidth / actualRatio);
                } else {
                    newWidth = width;
                    newHeight = Math.round(width / actualRatio);
                }

            }

            Log.d("AutoFitSurfaceView",
                    "Measured dimensions set: " + newWidth + "x" + newHeight);
            setMeasuredDimension(newWidth, newHeight);
        }
    }
}
