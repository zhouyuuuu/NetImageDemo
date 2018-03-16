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
    // 默认进度条大小为40dp
    private static final int SIZE_DEFAULT = (int) BitmapUtil.dip2px(40);
    // 默认进度条的条宽度为10dp
    private static final int STROKE_WIDTH_DEFAULT = (int) BitmapUtil.dip2px(10);
    // 画笔，作为成员变量避免多次创建影响性能
    private Paint mPaint;
    // 进度百分比
    private int percent = 0;
    // 矩形区域，作用同画笔
    private RectF rectF = null;

    public PercentProgressBar(Context context) {
        this(context, null);
    }

    public PercentProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
    }

    /**
     * 设置进度条的进度
     */
    public void setPercent(int percent) {
        this.percent = percent;
        // 重绘进度条
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getMeasureLength(widthMeasureSpec), getMeasureLength(heightMeasureSpec));
    }

    private int getMeasureLength(int measureSpec) {
        switch (MeasureSpec.getMode(measureSpec)) {
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
        // 画进度条灰色底
        mPaint.setARGB(0xaa, 0x55, 0x55, 0x55);
        int width = getWidth();
        int height = getHeight();
        // 取宽和高中比较短的那个的一半作为半径，要减去进度条的条宽度否则进度条过大，显示不全
        int radio = width > height ? (height - STROKE_WIDTH_DEFAULT) / 2 : (width - STROKE_WIDTH_DEFAULT) / 2;
        // 画进度条的白色进度
        canvas.drawCircle(width / 2, height / 2, radio, mPaint);
        mPaint.setARGB(0xee, 0xff, 0xff, 0xff);
        // LOLLIPOP以上版本调用该方法可以不用new一个RectF，性能比较好一点
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawArc(width / 2 - radio, height / 2 - radio, width / 2 + radio, height / 2 + radio, -90, (int) (percent * 1.0f / 100 * 360), false, mPaint);
        } else {
            if (rectF == null)
                rectF = new RectF(width / 2 - radio, height / 2 - radio, width / 2 + radio, height / 2 + radio);
            canvas.drawArc(rectF, -90, (int) (percent * 1.0f / 100 * 360), false, mPaint);
        }
    }
}
