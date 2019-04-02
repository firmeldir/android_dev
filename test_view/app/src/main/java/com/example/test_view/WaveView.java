package com.example.test_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Random;

public class WaveView extends View {

    public static final int MAX_VOLUME = 100;
    private static final int WAVE_LENGTH = 200;
    private int itemWidth;
    double[] measuredData;

    Paint linePaint;
    Path wavePath;



    private double[] originalData = getWaveData();

    public WaveView(Context context) {
        this(context, null);
        itemWidth = (int) getResources().getDimension(R.dimen.wave_view_item_width);
        linePaint = new Paint();
        wavePath = new Path();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(itemWidth);

    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        itemWidth = (int) getResources().getDimension(R.dimen.wave_view_item_width);
        linePaint = new Paint();
        wavePath = new Path();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(itemWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = originalData.length * itemWidth * 2 - itemWidth;


        switch (widthMode) {
            case View.MeasureSpec.EXACTLY:
                if (width > measuredWidth) {
                    int itemCount = (measuredWidth + itemWidth) / (itemWidth * 2);
                    measuredData = LinearInterpolation.interpolateArray(originalData, itemCount);
                } else {
                    measuredData = originalData;
                }
                width = measuredWidth;
                break;
            case View.MeasureSpec.AT_MOST:
                if (width > measuredWidth) {
                    int itemCount = (measuredWidth + itemWidth) / (itemWidth * 2);
                    measuredData = LinearInterpolation.interpolateArray(originalData, itemCount);
                    width = measuredWidth;
                } else {
                    measuredData = originalData;
                }
                break;
            case View.MeasureSpec.UNSPECIFIED:
                width = measuredWidth;
                measuredData = originalData;
                break;
        }
        int height = measuredHeight;
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        if (measuredData == null) {
            return;
        }
        wavePath.reset();
        int measuredHeight = getMeasuredHeight();
        int currentX = itemWidth;
        for (double data : measuredData) {
            float height = ((float) data / MAX_VOLUME) * measuredHeight;
            float startY = (float) measuredHeight / 2f - height / 2f;
            float endY = startY + height;
            wavePath.moveTo(currentX, startY);
            wavePath.lineTo(currentX, endY);
            currentX += itemWidth * 2;
        }
        canvas.drawPath(wavePath, linePaint);
    }

    private double[] getWaveData() {
        double[] data = new double[WAVE_LENGTH];
        Random r = new Random();
        for (int i = 0; i < data.length; i++) {
            int value = r.nextInt(MAX_VOLUME + 1);
            data[i] = value;
        }
        return data;
    }
}