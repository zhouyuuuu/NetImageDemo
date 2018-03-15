package com.example.administrator.netimageapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.administrator.netimageapplication.util.BitmapUtil;

/**
 * Edited by Administrator on 2018/3/15.
 */

public class PercentProgressBar extends View {
    private static final int SIZE_DEFAULT = (int) BitmapUtil.dip2px(40);
    private static final int STROKE_WIDTH_DEFAULT = (int) BitmapUtil.dip2px(10);
    private Paint mPaint;
    private int percent = 0;
    private RectF rectF = null;

    public void setPercent(int percent) {
        this.percent = percent;
        invalidate();
    }

    public PercentProgressBar(Context context) {
        this(context,null);
    }

    public PercentProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PercentProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getMeasureLength(widthMeasureSpec),getMeasureLength(heightMeasureSpec));
    }

    private int getMeasureLength(int measureSpec){
        switch (MeasureSpec.getMode(measureSpec)){
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.AT_MOST:
                return SIZE_DEFAULT;
            case MeasureSpec.UNSPECIFIED:
                return 0;
            default:
                return 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setARGB(0xaa,0x55,0x55,0x55);
        int width = getWidth();
        int height = getHeight();
        int radio = width>height?(height-STROKE_WIDTH_DEFAULT)/2:(width-STROKE_WIDTH_DEFAULT)/2;
        canvas.drawCircle(width/2,height/2,radio,mPaint);
        mPaint.setARGB(0xee,0xff,0xff,0xff);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawArc(width / 2 - radio, height / 2 - radio, width / 2 + radio, height / 2 + radio,-90,(int)(percent*1.0f/100*360),false,mPaint);
        }else {
            if (rectF == null)
                rectF = new RectF(width / 2 - radio, height / 2 - radio, width / 2 + radio, height / 2 + radio);
            canvas.drawArc(rectF,-90,(int)(percent*1.0f/100*360),false,mPaint);
        }
    }
}
