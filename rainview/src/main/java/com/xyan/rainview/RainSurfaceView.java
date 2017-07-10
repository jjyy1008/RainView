package com.xyan.rainview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by chenxueqing on 2017/4/18.
 */

public class RainSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private DrawThread drawThread;

    private int viewWidth;
    private int viewHeight;

    private Paint mPaint;
    private Path mPath;

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

    public RainSurfaceView(Context context) {
        this(context, null);
    }

    public RainSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RainSurfaceView);
            RAIN_COUNT = ta.getInt(R.styleable.RainSurfaceView_rsv_dot_count, RAIN_COUNT);
            if (RAIN_COUNT < 0) {
                RAIN_COUNT = 1;
            }
            MAX_SPEED = ta.getInt(R.styleable.RainSurfaceView_rsv_max_speed, MAX_SPEED);
            MIN_SPEED = ta.getInt(R.styleable.RainSurfaceView_rsv_min_speed, MIN_SPEED);
            MAX_LENGTH = ta.getDimensionPixelSize(R.styleable.RainSurfaceView_rsv_max_length, (int) MAX_LENGTH);
            MIN_LENGTH = ta.getDimensionPixelSize(R.styleable.RainSurfaceView_rsv_min_length, (int) MIN_LENGTH);
            WATER_R = ta.getDimensionPixelSize(R.styleable.RainView_rv_water_radius, (int) WATER_R);
            float maxAlpha = ta.getFloat(R.styleable.RainSurfaceView_rsv_max_alpha, 0.12f);
            if (maxAlpha <= 0) {
                maxAlpha = 0.01f;
            } else if (maxAlpha > 1) {
                maxAlpha = 1;
            }
            MAX_ALPHA = (int) (maxAlpha * 255);
            hasAlphaGrad = ta.getBoolean(R.styleable.RainSurfaceView_rsv_alpha_gradient, true);
            ta.recycle();
        }

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setZOrderOnTop(true);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);

        mPath = new Path();
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
     * @param MAX_SPEED default is 45
     */
    public void setMaxSpeed(int MAX_SPEED) {
        this.MAX_SPEED = MAX_SPEED;
    }

    /**
     * @param MIN_SPEED default is 28
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

    public void setRainColor(int color) {
        if (mPaint != null) {
            mPaint.setColor(color);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFormat(PixelFormat.TRANSPARENT);
        drawThread = new DrawThread(holder);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        viewWidth = width;
        viewHeight = height;
        initRainDots();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (drawThread != null) {
            drawThread.stopThread();
        }
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

    private class DrawThread extends Thread {

        private final SurfaceHolder holder;
        private boolean isRunning = true;



        DrawThread(SurfaceHolder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            while (isRunning) {
                synchronized (holder) {
                    if (holder != null) {
                        Canvas canvas = holder.lockCanvas();
                        if (canvas == null) {
                            break;
                        }
                        draw(canvas);
                        try {
                            Thread.sleep(DIFF_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            holder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }
        }

        private void draw(Canvas canvas) {
            if (viewWidth <= 0 || viewHeight <= 0) {
                return;
            }
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
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

        private void stopThread() {
            isRunning = false;
        }
    }
}
