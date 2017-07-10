package com.xyan.rainview;

import android.graphics.Point;

/**
 * Created by chenxueqing on 2017/4/18.
 */

public class RainDot {
    public Point point;
    public int speed;
    public float alpha;

    public RainDot(int x, int y, int speed, float alpha) {
        point = new Point(x, y);
        if (speed <= 0) {
            speed = 1;
        }
        this.speed = speed;
        if (alpha < 0.2f) {
            alpha = 0.2f;
        } else if (alpha > 1) {
            alpha = 1;
        }
        this.alpha = alpha;
    }
}
