package me.ajax.clockview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by aj on 2018/4/2
 */

public class ClockView extends View {

    Paint mPaint = new Paint();
    ValueAnimator animatorDot;
    ValueAnimator animatorCircle;
    ValueAnimator animatorLine;
    ValueAnimator animatorSecond;
    ValueAnimator animatorMinute;
    int animationSecondRepeatCount = 0;

    boolean isShowCircle = false;
    boolean isShowSecond = false;
    boolean isStart = false;


    public ClockView(Context context) {
        super(context);
        init();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {

        //画笔
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(dp2Dx(2));
        mPaint.setStyle(Paint.Style.STROKE);

        initAnimator();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStart) return;
                isShowCircle = false;
                isShowSecond = false;
                isStart = true;
                startAnimator(animatorDot);
            }
        });

        post(new Runnable() {
            @Override
            public void run() {
                performClick();
            }
        });
    }

    public void initAnimator() {

        animatorDot = ValueAnimator.ofFloat(-dp2Dx(200), 0);
        animatorDot.setDuration(800);
        animatorDot.setInterpolator(new BounceInterpolator());
        animatorDot.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startAnimator(animatorCircle);
                isShowCircle = true;
            }
        });
        animatorDot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidateView();
            }
        });

        animatorCircle = ValueAnimator.ofFloat(0, dp2Dx(110));
        animatorCircle.setDuration(400);
        animatorCircle.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startAnimator(animatorLine);
            }
        });
        animatorCircle.setInterpolator(new LinearInterpolator());
        animatorCircle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidateView();
            }
        });

        animatorLine = ValueAnimator.ofFloat(-dp2Dx(150), 0);
        animatorLine.setDuration(350);
        animatorLine.setInterpolator(new LinearInterpolator());
        animatorLine.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startAnimator(animatorSecond);
                startAnimator(animatorMinute);
                isShowSecond = true;
                animationSecondRepeatCount = 0;
            }
        });
        animatorLine.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidateView();
            }
        });

        animatorSecond = ValueAnimator.ofFloat(1F, 0.5F, 1F);
        animatorSecond.setDuration(500);
        animatorSecond.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                animationSecondRepeatCount++;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (animationSecondRepeatCount < 4) {
                    animatorSecond.cancel();
                    animatorSecond.setStartDelay(500);
                    animatorSecond.start();
                } else {
                    isStart = false;
                }
            }
        });
        animatorSecond.setInterpolator(new DecelerateInterpolator());
        animatorSecond.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidateView();
            }
        });


        animatorMinute = ValueAnimator.ofFloat(-90, -15);
        animatorMinute.setDuration(1500);
        animatorMinute.setInterpolator(new BounceInterpolator());
        animatorMinute.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidateView();
            }
        });
    }

    Path path = new Path();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int mWidth = getWidth();
        int mHeight = getHeight();

        canvas.save();
        canvas.translate(mWidth / 2, mHeight / 2);

        mPaint.setStyle(Paint.Style.STROKE);

        //点
        drawDot(canvas);
        //圆
        drawCircle(canvas);
        //线
        drawLine(canvas);
        //分、秒
        drawMinuteAndSecond(canvas);

        canvas.restore();
    }


    void drawDot(Canvas canvas) {

        if (!animatorDot.isRunning()) return;
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, (float) animatorDot.getAnimatedValue(), dp2Dx(3), mPaint);
    }

    void drawCircle(Canvas canvas) {

        if (!isShowCircle) return;
        canvas.drawCircle(0, 0, (float) animatorCircle.getAnimatedValue(), mPaint);
    }

    void drawLine(Canvas canvas) {

        if (!animatorLine.isRunning()) return;
        float value = (float) animatorLine.getAnimatedValue();
        canvas.drawLine(0, value - dp2Dx(100), 0, value, mPaint);
    }

    void drawMinuteAndSecond(Canvas canvas) {

        if (!isShowSecond) return;

        //时钟
        canvas.save();
        if (animationSecondRepeatCount > 0) {
            canvas.rotate(90 * (animationSecondRepeatCount - 2) + 90 * animatorSecond.getAnimatedFraction());
        } else {
            canvas.rotate(-90);
        }

        path.reset();

        //计算路径
        float value = (float) animatorSecond.getAnimatedValue();
        for (int i = 0; i < 19; i++) {
            float y = (float) Math.sin(-Math.toRadians(30 * i));
            path.lineTo(i * dp2Dx(5) * value, y * dp2Dx(15) * (1 - value));
        }
        canvas.drawPath(path, mPaint);
        canvas.restore();

        //分钟
        canvas.save();
        canvas.rotate((float) animatorMinute.getAnimatedValue());
        canvas.drawLine(0, 0, dp2Dx(60) * animatorMinute.getAnimatedFraction(), 0, mPaint);
        canvas.save();

    }

    int dp2Dx(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }

    void l(Object o) {
        Log.e("######", o.toString());
    }

    void startAnimator(ValueAnimator animator) {
        if (animator != null) {
            animator.cancel();
            animator.start();
        }
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimAndRemoveCallbacks();
    }

    private void stopAnimAndRemoveCallbacks() {

        if (animatorDot != null) animatorDot.end();
        if (animatorCircle != null) animatorCircle.end();
        if (animatorLine != null) animatorLine.end();
        if (animatorSecond != null) animatorSecond.end();
        if (animatorMinute != null) animatorMinute.end();

        Handler handler = this.getHandler();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
