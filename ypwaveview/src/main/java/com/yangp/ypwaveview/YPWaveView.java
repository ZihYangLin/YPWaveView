package com.yangp.ypwaveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Created by yangp on 2018/4/23.
 * waveView
 */

public class YPWaveView extends View {
    /*類型常數*/
    public enum Shape {
        CIRCLE(1), SQUARE(2), HEART(3);
        int value;

        Shape(int value) {
            this.value = value;
        }

        static Shape fromValue(int value) {
            for (Shape shape : values()) {
                if (shape.value == value) return shape;
            }
            return CIRCLE;
        }
    }


    /*位移Animator*/
    private float shiftX1 = 0;
    private float shiftOffset = -0.25f;
    private int waveOffset = 25;
    private int speed = 25;
    private HandlerThread thread = new HandlerThread("YPWaveView_" + hashCode());
    private Handler animHandler, uiHandler;

    /*畫筆*/
    private Paint mBorderPaint = new Paint(); //邊線的Paint
    private Paint mViewPaint = new Paint(); //水位的Paint
    //    private Paint mPointPaint = new Paint();
    private Path pathHeart; //愛心路徑

    /*初始常數*/
    private static final int DEFAULT_PROGRESS = 405;
    private static final int DEFAULT_MAX = 1000;
    private static final int DEFAULT_STRONG = 50;
    public static final int DEFAULT_BEHIND_WAVE_COLOR = Color.parseColor("#443030d5");
    public static final int DEFAULT_FRONT_WAVE_COLOR = Color.parseColor("#FF3030d5");
    public static final int DEFAULT_BORDER_COLOR = Color.parseColor("#000000");
    private static final float DEFAULT_BORDER_WIDTH = 5f;
    public static final int DEFAULT_TEXT_COLOR = Color.parseColor("#000000");
    private static final boolean DEFAULT_ENABLE_ANIMATION = false;

    /*參數值*/
    private int mProgress = DEFAULT_PROGRESS; //水位
    private int mMax = DEFAULT_MAX; //水位最大值
    private int mFrontWaveColor = DEFAULT_FRONT_WAVE_COLOR; //前面水波顏色
    private int mBehindWaveColor = DEFAULT_BEHIND_WAVE_COLOR; //後面水波顏色
    private int mBorderColor = DEFAULT_BORDER_COLOR; //邊線顏色
    private float mBorderWidth = DEFAULT_BORDER_WIDTH; //邊線寬度
    private int mTextColor = DEFAULT_TEXT_COLOR; //字體顏色
    private boolean isAnimation = DEFAULT_ENABLE_ANIMATION;
    private int mStrong = DEFAULT_STRONG; //波峰
    private Shape mShape = Shape.CIRCLE;
    private int value = 0; //寬或高的最小值


    public YPWaveView(Context context) {
        this(context, null);
    }

    public YPWaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YPWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        /*取得xml參數*/
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.YPWaveView, defStyleAttr, 0);

        /*設定xml參數*/
        mFrontWaveColor = attributes.getColor(R.styleable.YPWaveView_frontColor, DEFAULT_FRONT_WAVE_COLOR);
        mBehindWaveColor = attributes.getColor(R.styleable.YPWaveView_behideColor, DEFAULT_BEHIND_WAVE_COLOR);
        mBorderColor = attributes.getColor(R.styleable.YPWaveView_borderColor, DEFAULT_BORDER_COLOR);
        mTextColor = attributes.getColor(R.styleable.YPWaveView_textColor, DEFAULT_TEXT_COLOR);
        mProgress = attributes.getInt(R.styleable.YPWaveView_progress, DEFAULT_PROGRESS);
        mMax = attributes.getInt(R.styleable.YPWaveView_max, DEFAULT_MAX);
        mBorderWidth = attributes.getDimension(R.styleable.YPWaveView_borderWidthSize, DEFAULT_BORDER_WIDTH);
        mStrong = attributes.getInt(R.styleable.YPWaveView_strong, DEFAULT_STRONG);
        mShape = Shape.fromValue(attributes.getInt(R.styleable.YPWaveView_shapeType, 1));
        isAnimation = attributes.getBoolean(R.styleable.YPWaveView_animatorEnable, DEFAULT_ENABLE_ANIMATION);

        /*設定抗鋸齒 & 設定為"線"*/
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(mBorderColor);

//        mPointPaint.setStyle(Paint.Style.FILL);
//        mPointPaint.setColor(Color.parseColor("#8e2710"));

        /*開啟動畫執行緒*/
        thread.start();
        animHandler = new Handler(thread.getLooper());
        uiHandler = new UIHandler(new WeakReference<View>(this));


        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定水位
     * 0-MAX
     */
    public void setProgress(int progress) {
        if (progress <= mMax) {
            mProgress = progress;
            createShader();
            Message message = Message.obtain(uiHandler);
            message.sendToTarget();
        }
    }

    public void startAnimation() {
        isAnimation = true;
        if (getWidth() > 0 && getHeight() > 0) {
            animHandler.post(new Runnable() {
                @Override
                public void run() {
                    createShader();
                    Message message = Message.obtain(uiHandler);
                    message.sendToTarget();
                    if (isAnimation) {
                        animHandler.postDelayed(this, speed);
                    }
                }
            });
        }
    }

    public void stopAnimation() {
        isAnimation = false;
    }

    /**
     * 設定最大值
     */
    public void setMax(int max) {
        if (mMax != max) {
            if (max >= mProgress) {
                mMax = max;
                createShader();
                Message message = Message.obtain(uiHandler);
                message.sendToTarget();
            }
        }
    }

    /**
     * 設定邊線顏色
     */
    public void setBorderColor(int color) {
        mBorderColor = color;
        mBorderPaint.setColor(mBorderColor);
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定前波顏色
     */
    public void setFrontWaveColor(int color) {
        mFrontWaveColor = color;
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定後波顏色
     */
    public void setBehindWaveColor(int color) {
        mBehindWaveColor = color;
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定文字顏色
     */
    public void setTextColor(int color) {
        mTextColor = color;
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定邊線寬度
     */
    public void setBorderWidth(float width) {
        mBorderWidth = width;
        mBorderPaint.setStrokeWidth(mBorderWidth);
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }


    /**
     * 設定動畫速度
     * Fast -> Slow
     * 0.......∞
     */
    public void setAnimationSpeed(int speed) {
        this.speed = speed;
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定前後水波每次刷新偏移多少
     * 0-100
     */
    public void setWaveShiftOffset(float offset) {
        this.shiftOffset = (offset - 50f) / 50f;
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }


    /**
     * 設定前後水波相差位移
     * 1-100
     */
    public void setWaveOffset(int offset) {
        this.waveOffset = offset;
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定波峰
     * 0-100
     */
    public void setWaveStrong(int strong) {
        this.mStrong = strong;
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    public void setShape(Shape shape) {
        mShape = shape;
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        value = Math.min(w, h);
        int wOffset = (w - value) / 2;
        int hOffset = (h - value) / 2;
        /*===愛心路徑===*/
        pathHeart = new Path();
        /*起此點*/
        pathHeart.moveTo(value / 2 + wOffset, value / 5 + hOffset);
        /*左上升線*/
        pathHeart.cubicTo(5 * value / 14 + wOffset, hOffset, wOffset, value / 15 + hOffset, value / 28 + wOffset, 2 * value / 5 + hOffset);
        /*左下降線*/
        pathHeart.cubicTo(value / 14 + wOffset, 2 * value / 3 + hOffset, 3 * value / 7 + wOffset, 5 * value / 6 + hOffset, value / 2 + wOffset, 9 * value / 10 + hOffset);
        /*右下降線*/
        pathHeart.cubicTo(4 * value / 7 + wOffset, 5 * value / 6 + hOffset, 13 * value / 14 + wOffset, 2 * value / 3 + hOffset, 27 * value / 28 + wOffset, 2 * value / 5 + hOffset);
        /*右上升線*/
        pathHeart.cubicTo(value + wOffset, value / 15 + hOffset, 9 * value / 14 + wOffset, hOffset, value / 2 + wOffset, value / 5 + hOffset);


        createShader();
        if (isAnimation) {
            startAnimation();
        }
    }


    /**
     * 建立填充著色器
     * y = Asin(ωx+φ)+h 波型公式 (正弦型函数) y = waveLevel * Math.sin(w * x1 + shiftX) + level
     * φ (初相位x)：波型X軸偏移量     $shiftX
     * ω (角頻率)：最小正周期 T=2π/|ω|       $w
     * A (波幅)：駝峰的大小     $waveLevel
     * h (初相位y)：波型Y軸偏移量     $level
     * <p>
     * 二階貝塞爾曲線(Bézier curve)
     * B(t) = X(1-t)^2 + 2t(1-t)Y + Zt^2 , 0 <= t <= n
     */
    private void createShader() {
        double w = (2.0f * Math.PI) / value;

        /*建立畫布*/
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        /*建立著色器*/
        Paint wavePaint = new Paint();
        wavePaint.setStrokeWidth(2f);
        wavePaint.setAntiAlias(true);

        float level = ((((float) (mMax - mProgress)) / (float) mMax) * value) + ((getHeight() / 2) - (value / 2)); //水位的高度
        int x2 = getWidth() + 1;//寬度
        int y2 = getHeight() + 1;//高度
        shiftX1 += shiftOffset; //位移量
        float zzz = (((float) value * ((waveOffset - 50) / 100f)) / ((float) value / 6.25f));
        float shiftX2 = shiftX1 + zzz; //前後波相差
        int waveLevel = mStrong * (value / 20) / 100;  // value / 20
        /*建立後波 (先後再前覆蓋)*/
        wavePaint.setColor(mBehindWaveColor);
        for (int x1 = 0; x1 < x2; x1++) {
            float y1 = (float) (waveLevel * Math.sin(w * x1 + shiftX1) + level);
            canvas.drawLine((float) x1, y1, (float) x1, y2, wavePaint);
        }
     
        /*建立前波*/
        wavePaint.setColor(mFrontWaveColor);
        for (int x1 = 0; x1 < x2; x1++) {
            float y1 = (float) (waveLevel * Math.sin(w * x1 + shiftX2) + level);
            canvas.drawLine((float) x1, y1, (float) x1, y2, wavePaint);
        }

        /*建立百分比文字*/
        float percent = (mProgress * 100) / (float) mMax;
        String text = String.format(Locale.TAIWAN, "%.1f", percent) + "%";
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(mTextColor);
        textPaint.setTextSize((value / 2f) / 2);
        textPaint.setAntiAlias(true);
        float textHeight = textPaint.descent() + textPaint.ascent();
        canvas.drawText(text, (getWidth() - textPaint.measureText(text)) / 2.0f, (getHeight() - textHeight) / 2.0f, textPaint);
        mViewPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animHandler != null) {
            animHandler.removeCallbacksAndMessages(null);
        }
        if (thread != null) {
            thread.quit();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = (value / 2f) - mBorderWidth;
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        switch (mShape) {
            case CIRCLE:
                canvas.drawCircle(cx, cy, radius, mViewPaint);
                /*畫邊線*/
                if (mBorderWidth > 0) {
                    canvas.drawCircle(cx, cy, radius, mBorderPaint);
                }
                break;
            case SQUARE:
                canvas.drawRect(
                        cx - radius
                        , cy - radius
                        , cx + radius
                        , cy + radius
                        , mViewPaint);
                /*畫邊線*/
                if (mBorderWidth > 0) {
                    canvas.drawRect(
                            cx - radius
                            , cy - radius
                            , cx + radius
                            , cy + radius
                            , mBorderPaint);
                }
                break;
            case HEART:
                canvas.drawPath(pathHeart, mViewPaint);
                /*畫邊線*/
                if (mBorderWidth > 0) {
                    canvas.drawPath(pathHeart, mBorderPaint);
                }
//                int wOffset = 0;
//                int hOffset = 0;
//                wOffset = (getWidth() - value) / 2;
//                hOffset = (getHeight() - value) / 2;
//                mPointPaint.setColor(Color.parseColor("#FF00FF"));
//                canvas.drawCircle(value / 2 + wOffset, value / 5 + hOffset, 10, mPointPaint);
//                canvas.drawCircle(5 * value / 14 + wOffset, hOffset, 10, mPointPaint);
//                canvas.drawCircle(wOffset, value / 15 + hOffset, 10, mPointPaint);
//                canvas.drawCircle(value / 28 + wOffset, 2 * value / 5 + hOffset, 10, mPointPaint);
//
//                canvas.drawCircle(value / 14 + wOffset, 2 * value / 3 + hOffset, 10, mPointPaint);
//                canvas.drawCircle(3 * value / 7 + wOffset, 5 * value / 6 + hOffset, 10, mPointPaint);
//                canvas.drawCircle(value / 2 + wOffset, value + hOffset, 10, mPointPaint);
//
//                canvas.drawCircle(4 * value / 7 + wOffset, 5 * value / 6 + hOffset, 10, mPointPaint);
//                canvas.drawCircle(13 * value / 14 + wOffset, 2 * value / 3 + hOffset, 10, mPointPaint);
//                canvas.drawCircle(27 * value / 28 + wOffset, 2 * value / 5 + hOffset, 10, mPointPaint);
//
//                canvas.drawCircle(value + wOffset , value / 15 + hOffset, 10, mPointPaint);
//                canvas.drawCircle(9 * value / 14 + wOffset , hOffset, 10, mPointPaint);
//                canvas.drawCircle(value / 2 + wOffset , value / 5 + hOffset, 10, mPointPaint);

                break;
        }
    }

    private static class UIHandler extends Handler {
        private final View mView;

        UIHandler(WeakReference<View> view) {
            super(Looper.getMainLooper());
            mView = view.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mView != null) {
                mView.invalidate();
            }
        }
    }
}
