package com.yangp.ypwaveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
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
        CIRCLE(1), SQUARE(2), HEART(3), STAR(4);
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
    private float waveVector = -0.25f;
    private int waveOffset = 25;
    private int speed = 25;
    private HandlerThread thread = new HandlerThread("YPWaveView_" + hashCode());
    private Handler animHandler, uiHandler;

    /*畫筆*/
    private Paint mBorderPaint = new Paint(); //邊線的Paint
    private Paint mViewPaint = new Paint(); //水位的Paint
    private Paint mWavePaint1 = new Paint(); //後波著色
    private Paint mWavePaint2 = new Paint(); //前波著色

    private Path mPathContent;
    private Path mPathBorder;

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
    private static final boolean DEFAULT_HIDE_TEXT = false;
    private static final int DEFAULT_SPIKE_COUNT = 5;
    private static final int DEFAULT_PADDING = 0;

    /*參數值*/
    private int mPadding = DEFAULT_PADDING; //內縮
    private int mProgress = DEFAULT_PROGRESS; //水位
    private int mMax = DEFAULT_MAX; //水位最大值
    private int mFrontWaveColor = DEFAULT_FRONT_WAVE_COLOR; //前面水波顏色
    private int mBehindWaveColor = DEFAULT_BEHIND_WAVE_COLOR; //後面水波顏色
    private int mBorderColor = DEFAULT_BORDER_COLOR; //邊線顏色
    private float mBorderWidth = DEFAULT_BORDER_WIDTH; //邊線寬度
    private int mTextColor = DEFAULT_TEXT_COLOR; //字體顏色
    private boolean isAnimation = DEFAULT_ENABLE_ANIMATION;
    private boolean isHideText = DEFAULT_HIDE_TEXT;
    private int mStrong = DEFAULT_STRONG; //波峰
    private int mSpikes = DEFAULT_SPIKE_COUNT;
    private Shape mShape = Shape.CIRCLE;
    private OnWaveStuffListener mListener;
    private Point screenSize = new Point(0, 0);

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
        isHideText = attributes.getBoolean(R.styleable.YPWaveView_textHidden, DEFAULT_HIDE_TEXT);

        /*設定抗鋸齒 & 設定為"線"*/
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(mBorderColor);

        /*建立著色器*/
        mWavePaint1 = new Paint();
        mWavePaint1.setStrokeWidth(2f);
        mWavePaint1.setAntiAlias(true);
        mWavePaint1.setColor(mBehindWaveColor);
        mWavePaint2 = new Paint();
        mWavePaint2.setStrokeWidth(2f);
        mWavePaint2.setAntiAlias(true);
        mWavePaint2.setColor(mFrontWaveColor);


        /*開啟動畫執行緒*/
        thread.start();
        animHandler = new Handler(thread.getLooper());
        uiHandler = new UIHandler(new WeakReference<View>(this));

        screenSize = new Point(getWidth(), getHeight());
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定水位
     * 0-MAX
     */
    public void setProgress(int progress) {
        if (progress <= mMax) {
            if (mListener != null) {
                mListener.onStuffing(progress, mMax);
            }
            mProgress = progress;
            createShader();
            Message message = Message.obtain(uiHandler);
            message.sendToTarget();
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void startAnimation() {
        isAnimation = true;
        if (getWidth() > 0 && getHeight() > 0) {
            animHandler.removeCallbacksAndMessages(null);
            animHandler.post(new Runnable() {
                @Override
                public void run() {
                    shiftX1 += waveVector; //位移量
                    long time = System.currentTimeMillis();
                    createShader();
                    System.out.println("time=>" + (System.currentTimeMillis() - time));
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

    public OnWaveStuffListener getListener() {
        return mListener;
    }

    public void setListener(OnWaveStuffListener mListener) {
        this.mListener = mListener;
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

    public int getMax() {
        return mMax;
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
        mWavePaint2.setColor(mFrontWaveColor);
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定後波顏色
     */
    public void setBehindWaveColor(int color) {
        mBehindWaveColor = color;
        mWavePaint1.setColor(mBehindWaveColor);
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
        resetShapes();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定內縮
     */
    public void setPadding(int mPadding) {
        this.mPadding = mPadding;
        resetShapes();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定動畫速度
     * Fast -> Slow
     * 0...∞
     */
    public void setAnimationSpeed(int speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("The speed must be greater than 0.");
        }
        this.speed = speed;
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定前後水波每次刷新偏移多少
     * 0-100
     */
    public void setWaveVector(float offset) {
        if (offset < 0 || offset > 100) {
            throw new IllegalArgumentException("The vector of wave must be between 0 and 100.");
        }
        this.waveVector = (offset - 50f) / 50f;
        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定字體是否隱藏
     *
     * @param hidden 隱藏
     */
    public void setHideText(boolean hidden) {
        this.isHideText = hidden;
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    /**
     * 設定星星的角數
     * 3...∞
     *
     * @param count 角數
     */
    public void setStarSpikes(int count) {
        if (count < 3) {
            throw new IllegalArgumentException("The number of spikes must be greater than 3.");
        }
        this.mSpikes = count;
        if (Math.min(screenSize.x, screenSize.y) != 0) {
            /*===星星路徑===*/
            resetShapes();
        }
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
        resetShapes();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenSize = new Point(w, h);
        resetShapes();
        if (isAnimation) {
            startAnimation();
        }
    }

    private void resetShapes() {
        int radius = Math.min(screenSize.x, screenSize.y);
        int cx = (screenSize.x - radius) / 2;
        int cy = (screenSize.y - radius) / 2;
        switch (mShape) {
            case STAR:
                /*===星星路徑===*/
                mPathBorder = drawStart(radius / 2 + cx, radius / 2 + cy + (int) mBorderWidth, mSpikes, radius / 2 - (int) mBorderWidth, radius / 4);
                mPathContent = drawStart(radius / 2 + cx, radius / 2 + cy + (int) mBorderWidth, mSpikes, radius / 2 - (int) mBorderWidth - mPadding, radius / 4 - mPadding);
                break;
            case HEART:
                /*===愛心路徑===*/
                mPathBorder = drawHeart(cx, cy, radius);
                mPathContent = drawHeart(cx + (mPadding / 2), cy + (mPadding / 2), radius - mPadding);
                break;
            case CIRCLE:
                /*===圓形路徑===*/
                mPathBorder = drawCircle(cx, cy, radius);
                mPathContent = drawCircle(cx + (mPadding / 2), cy + (mPadding / 2), radius - mPadding);
                break;
            case SQUARE:
                /*===方形路徑===*/
                mPathBorder = drawSquare(cx, cy, radius);
                mPathContent = drawSquare(cx + (mPadding / 2), cy + (mPadding / 2), radius - mPadding);
                break;
        }


        createShader();
        Message message = Message.obtain(uiHandler);
        message.sendToTarget();
    }

    private Path drawSquare(int cx, int cy, int radius) {
        Path path = new Path();
        path.moveTo(cx, cy + (mBorderWidth / 2));
        path.lineTo(cx, radius + cy - mBorderWidth);
        path.lineTo(radius + cx, radius + cy - mBorderWidth);
        path.lineTo(radius + cx, cy + mBorderWidth);
        path.lineTo(cx, cy + mBorderWidth);
        path.close();
        return path;
    } 

    private Path drawCircle(int cx, int cy, int radius) {
        Path path = new Path();
        path.addCircle((radius / 2) + cx, (radius / 2) + cy, (radius / 2) - mBorderWidth, Path.Direction.CCW);
        path.close();
        return path;
    }
    
    private Path drawHeart(int cx, int cy, int radius) {
        Path path = new Path();
        /*起此點*/
        path.moveTo(radius / 2 + cx, radius / 5 + cy);
        /*左上升線*/
        path.cubicTo(5 * radius / 14 + cx, cy, cx, radius / 15 + cy, radius / 28 + cx, 2 * radius / 5 + cy);
        /*左下降線*/
        path.cubicTo(radius / 14 + cx, 2 * radius / 3 + cy, 3 * radius / 7 + cx, 5 * radius / 6 + cy, radius / 2 + cx, 9 * radius / 10 + cy);
        /*右下降線*/
        path.cubicTo(4 * radius / 7 + cx, 5 * radius / 6 + cy, 13 * radius / 14 + cx, 2 * radius / 3 + cy, 27 * radius / 28 + cx, 2 * radius / 5 + cy);
        /*右上升線*/
        path.cubicTo(radius + cx, radius / 15 + cy, 9 * radius / 14 + cx, cy, radius / 2 + cx, radius / 5 + cy);
        path.close();
        return path;
    }

    /**
     * 畫星星
     *
     * @param cx          X
     * @param cy          Y
     * @param spikes      星星的角數
     * @param outerRadius 外圈半徑
     * @param innerRadius 內圈半徑
     * @return 路徑
     */
    private Path drawStart(int cx, int cy, int spikes, int outerRadius, int innerRadius) {
        Path path = new Path();
        double rot = Math.PI / 2d * 3d;
        double step = Math.PI / spikes;

        path.moveTo(cx, cy - outerRadius);
        for (int i = 0; i < spikes; i++) {
            path.lineTo(cx + (float) Math.cos(rot) * outerRadius, cy + (float) Math.sin(rot) * outerRadius);
            rot += step;

            path.lineTo(cx + (float) Math.cos(rot) * innerRadius, cy + (float) Math.sin(rot) * innerRadius);
            rot += step;
        }
        path.lineTo(cx, cy - outerRadius);
        path.close();
        return path;
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
        if (screenSize.x <= 0 || screenSize.y <= 0) {
            return;
        }
        int viewSize = Math.min(screenSize.x, screenSize.y);
        double w = (2.0f * Math.PI) / viewSize;

        /*建立畫布*/
        Bitmap bitmap = Bitmap.createBitmap(viewSize, viewSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float level = ((((float) (mMax - mProgress)) / (float) mMax) * viewSize) + ((viewSize / 2) - (viewSize / 2)); //水位的高度
        int x2 = viewSize + 1;//寬度
        int y2 = viewSize + 1;//高度
        float zzz = (((float) viewSize * ((waveOffset - 50) / 100f)) / ((float) viewSize / 6.25f));
        float shiftX2 = shiftX1 + zzz; //前後波相差
        int waveLevel = mStrong * (viewSize / 20) / 100;  // viewSize / 20

        for (int x1 = 0; x1 < x2; x1++) {
            /*建立後波 (先後再前覆蓋)*/
            float y1 = (float) (waveLevel * Math.sin(w * x1 + shiftX1) + level);
            canvas.drawLine((float) x1, y1, (float) x1, y2, mWavePaint1);
            /*建立前波*/
            y1 = (float) (waveLevel * Math.sin(w * x1 + shiftX2) + level);
            canvas.drawLine((float) x1, y1, (float) x1, y2, mWavePaint2);
        }

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
        canvas.drawPath(mPathContent, mViewPaint);
        /*畫邊線*/
        if (mBorderWidth > 0) {
            canvas.drawPath(mPathBorder, mBorderPaint);
        }


        if (!isHideText) {
            /*建立百分比文字*/
            float percent = (mProgress * 100) / (float) mMax;
            String text = String.format(Locale.TAIWAN, "%.1f", percent) + "%";
            TextPaint textPaint = new TextPaint();
            textPaint.setColor(mTextColor);
            if (mShape == Shape.STAR) {
                textPaint.setTextSize((Math.min(screenSize.x, screenSize.y) / 2f) / 3);
            } else {
                textPaint.setTextSize((Math.min(screenSize.x, screenSize.y) / 2f) / 2);
            }

            textPaint.setAntiAlias(true);
            float textHeight = textPaint.descent() + textPaint.ascent();
            canvas.drawText(text, (screenSize.x - textPaint.measureText(text)) / 2.0f, (screenSize.y - textHeight) / 2.0f, textPaint);

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
