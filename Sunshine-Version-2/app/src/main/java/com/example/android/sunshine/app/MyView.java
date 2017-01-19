package com.example.android.sunshine.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 * Created by victor on 21/12/16.
 */

public class MyView extends View {
    private Paint mTextPaint;
    private float mTextX;
    private float mTextY;
    private String direcao;

    public MyView(Context context) {
        super(context);
        init(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setDirecao(String direcao) {
        this.direcao = direcao;
    }

    private void init(Context context) {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.STROKE);
        //        mTextPaint.setStrokeWidth(32);
        mTextPaint.setTextSize(80);
        mTextPaint.setColor(Color.WHITE);
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        }
        mTextX = 0;
        mTextY = 0;

        if(isInEditMode()){
            direcao = "Sample";
        }
    }

    private static final int CENTER_X = 300;
    private static final int CENTER_Y = 300;
    private static final int CENTER_RADIUS = 150;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight = hSpecSize;

        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int myWidth = wSpecSize;

        //        setMeasuredDimension(myWidth, myHeight);
        setMeasuredDimension(CENTER_X, CENTER_Y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect rectangle = new Rect();
        final String txt = direcao;
        if (txt != null) {
            mTextPaint.getTextBounds(txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );
            //        int alturaTexto = canvas.
            canvas.drawText(txt, 0, canvas.getHeight() / 2, mTextPaint);
        }
    }
}
