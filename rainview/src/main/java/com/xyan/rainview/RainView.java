package com.xyan.rainview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Created by chenxueqing on 2017/4/18.
 */

public class RainView extends View {

    private Paint mPaint;
    private Path mPath;

    private int viewWidth;
    private int viewHeight;

    private int RAIN_COUNT = 70;
    private final Random random = new Random();
    private RainDot[] rainDots;
    private int MAX_SPEED = 36;
    private int MIN_SPEED = 20;
    private float MAX_LENGTH = 40;
    private float MIN_LENGTH = 20;
    private float WATER_R = 3f;
    private int MAX_ALPHA = 30; // Max 255

    private final int DIFF_TIME = 16;

    private boolean hasAlphaGrad = true;
    private Runnable runnable;

    public RainView(Context context) {
        this(context, null);
    }

    public RainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RainView);
            RAIN_COUNT = ta.getInt(R.styleable.RainView_rv_dot_count, RAIN_COUNT);
            if (RAIN_COUNT < 0) {
                RAIN_COUNT = 1;
            }
            MAX_SPEED = ta.getInt(R.styleable.RainView_rv_max_speed, MAX_SPEED);
            MIN_SPEED = ta.getInt(R.styleable.RainView_rv_min_speed, MIN_SPEED);
            MAX_LENGTH = ta.getDimensionPixelSize(R.styleable.RainView_rv_max_length, (int) MAX_LENGTH);
            MIN_LENGTH = ta.getDimensionPixelSize(R.styleable.RainView_rv_min_length, (int) MIN_LENGTH);
            WATER_R = ta.getDimensionPixelSize(R.styleable.RainView_rv_water_radius, (int) WATER_R);
            float maxAlpha = ta.getFloat(R.styleable.RainView_rv_max_alpha, 0.12f);
            if (maxAlpha <= 0) {
                maxAlpha = 0.01f;
            } else if (maxAlpha > 1) {
                maxAlpha = 1;
            }
            MAX_ALPHA = (int) (maxAlpha * 255);
            hasAlphaGrad = ta.getBoolean(R.styleable.RainView_rv_alpha_gradient, true);
            ta.recycle();
        }

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);

        mPath = new Path();

        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
                postDelayed(runnable, DIFF_TIME);
            }
        };
        postDelayed(runnable, DIFF_TIME);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        removeCallbacks(runnable);
        if (visibility == VISIBLE) {
            post(runnable);
        }

    }

    public void enableAlphaGradient(boolean enable) {
        hasAlphaGrad = enable;
    }

    /**
     * @param alpha float: 0 ~ 1
     */
    public void setMaxAlpha(float alpha) {
        if (alpha <= 0) {
            alpha = 0.01f;
        } else if (alpha > 1) {
            alpha = 1;
        }
        MAX_ALPHA = (int) (255 * alpha);
    }

    /**
     * @param MAX_SPEED
     */
    public void setMaxSpeed(int MAX_SPEED) {
        this.MAX_SPEED = MAX_SPEED;
    }

    /**
     * @param MIN_SPEED
     */
    public void setMinSpeed(int MIN_SPEED) {
        this.MIN_SPEED = MIN_SPEED;
    }

    /**
     * @param MAX_LENGTH water max length
     */
    public void setMaxLength(float MAX_LENGTH) {
        this.MAX_LENGTH = MAX_LENGTH;
    }

    /**
     * @param MIN_LENGTH water min length
     */
    public void setMinLength(float MIN_LENGTH) {
        this.MIN_LENGTH = MIN_LENGTH;
    }

    public void setWaterRadius(float WATER_R) {
        this.WATER_R = WATER_R;
    }

    public void setRainCount(int RAIN_COUNT) {
        if (RAIN_COUNT < 1) {
            RAIN_COUNT = 1;
        }
        this.RAIN_COUNT = RAIN_COUNT;
        initRainDots();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        viewWidth = getWidth();
        viewHeight = getHeight();
        initRainDots();
    }

    private void initRainDots() {
        if (rainDots == null || rainDots.length != RAIN_COUNT) {
            rainDots = new RainDot[RAIN_COUNT];
        }
        for (int i = 0; i < RAIN_COUNT; i++) {
            if (rainDots[i] == null) {
                rainDots[i] = new RainDot(random.nextInt(viewWidth), random.nextInt(viewHeight),
                        random.nextInt(MAX_SPEED), random.nextFloat());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }
        for (int i = 0; i < RAIN_COUNT; i++) {

            if (rainDots[i].point.x > viewWidth || rainDots[i].point.y > viewHeight) {
                rainDots[i].point.x = random.nextInt(viewWidth);
                rainDots[i].point.y = 0;
            }

            rainDots[i].point.y += rainDots[i].speed + MIN_SPEED;
            //draw dot
            mPath.reset();
            float scaleYDiff = MAX_LENGTH * rainDots[i].speed / MAX_SPEED;
            float R = WATER_R;
            float L = MIN_LENGTH + scaleYDiff;
            mPath.addCircle(R, L, R, Path.Direction.CW);
            mPath.moveTo(R, 0);
            double x = R + Math.sqrt(Math.pow(R, 2) - (Math.pow(R, 4) / Math.pow(L, 2)));
            double y = L - Math.pow(R, 2) / (double) L;
            mPath.lineTo((float) x, (float) y);
            mPath.lineTo((float) (2 * R - x), (float) y);
            mPath.close();
            mPath.offset(rainDots[i].point.x, rainDots[i].point.y);
            if (hasAlphaGrad) {
                mPaint.setAlpha((int) (rainDots[i].alpha * MAX_ALPHA));
            } else {
                mPaint.setAlpha(MAX_ALPHA);
            }
            canvas.drawPath(mPath, mPaint);
        }
    }
}
