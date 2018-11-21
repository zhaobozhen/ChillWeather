package com.absinthe.chillweather.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.absinthe.chillweather.R;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * function：仿小米天气 日出日落动画控件
 * creator：hw
 * time: 2018/08/20 15:21
 */
public class SunView extends View {

    Paint mPathPaint;
    private int mWidth;
    private int mHeight;
    int mainColor;
    int trackColor;
    private Path mPathPath;
    private Paint mMotionPaint;
    private Path mMotionPath;
    int controlX, controlY;
    float startX, startY;
    float endX, endY;
    private double rX;
    private double rY;
    private int[] mSunrise = new int[2];
    private int[] mSunset = new int[2];
    private Paint mSunPaint;
    private ValueAnimator valueAnimator;
    private float mProgress;
    private Paint mShadePaint;
    private Shader mPathShader;
    private float mCurrentProgress;
    private boolean isDraw = false;
    private DashPathEffect mDashPathEffect;
    private LinearGradient mBackgroundShader;
    private int sunColor;
    private Paint mSunStrokePaint;
    private float svSunSize;
    private float svTextSize;
    private float svPadding;
    private float svTrackWidth;

    public SunView(Context context) {
        super(context);
        init(null);
    }

    public SunView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SunView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SunView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        //初始化属性
        final Context context = getContext();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SunView);
        // FIXME 这个地方如果xml属性不给值则拿不到默认值
        mainColor = array.getColor(R.styleable.SunView_svMainColor, 0x67B2FD);
        trackColor = array.getColor(R.styleable.SunView_svTrackColor, 0x67B2FD);
        sunColor = array.getColor(R.styleable.SunView_svSunColor, 0x00D3FE);
        svSunSize = array.getDimension(R.styleable.SunView_svSunRadius, 10);
        svTextSize = array.getDimension(R.styleable.SunView_svTextSize, 18);
        svPadding = array.getDimension(R.styleable.SunView_svPadding, 10);
        svTrackWidth = array.getDimension(R.styleable.SunView_svTrackWidth, 3);
        array.recycle();

        // 渐变路径的画笔
        Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(mainColor);
        pathPaint.setStyle(Paint.Style.FILL);
        mPathPaint = pathPaint;
        // 渐变路径
        mPathPath = new Path();
        // 渐变遮罩的画笔
        Paint shadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadePaint.setColor(Color.parseColor("#8EFFFFFF"));
        shadePaint.setStyle(Paint.Style.FILL);
        mShadePaint = shadePaint;
        // 运动轨迹画笔
        Paint motionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        motionPaint.setColor(trackColor);
        motionPaint.setStrokeCap(Paint.Cap.ROUND);
        motionPaint.setStrokeWidth(svTrackWidth);
        motionPaint.setStyle(Paint.Style.STROKE);
        mMotionPaint = motionPaint;
        // 运动轨迹
        mMotionPath = new Path();
        // 太阳画笔
        Paint sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunPaint.setColor(sunColor);
        sunPaint.setStyle(Paint.Style.FILL);
        mSunPaint = sunPaint;
        // 太阳边框画笔
        Paint sunStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunStrokePaint.setColor(Color.WHITE);
        sunStrokePaint.setStyle(Paint.Style.FILL);
        mSunStrokePaint = sunStrokePaint;
        // 日出日落时间画笔
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(mainColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(svTextSize);
        mDashPathEffect = new DashPathEffect(new float[]{6, 12}, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        if(!isDraw){
            mWidth = getWidth();
            mHeight = getHeight();
            controlX = mWidth/2;
            controlY = 0-mHeight/2;
            startX = svPadding;
            startY = mHeight-svPadding;
            endX = mWidth-svPadding;
            endY = mHeight-svPadding;
            rX = svPadding;
            rY = mHeight-svPadding;
            // 渐变路径
            mPathShader = new LinearGradient(mWidth/2, svPadding, mWidth/2, endY,
                    mainColor, Color.WHITE, Shader.TileMode.CLAMP);
            mPathPaint.setShader(mPathShader);
            mPathPath.moveTo(startX, startY);
            mPathPath.quadTo(controlX, controlY, endX, endY);
            // 运动轨迹
            mMotionPath.moveTo(startX, startY);
            mMotionPath.quadTo(controlX, controlY, endX, endY);
            isDraw = true;
        }

        // 按遮挡关系画
        // 画渐变
        canvas.drawPath(mPathPath, mPathPaint);
        // 画已经运动过去的轨迹
        mMotionPaint.setStyle(Paint.Style.STROKE);
        mMotionPaint.setPathEffect(null);
        canvas.drawPath(mMotionPath, mMotionPaint);
        // 画一个矩形遮住未运动到的渐变和轨迹
        mShadePaint.setShader(mBackgroundShader);
        canvas.drawRect((float) rX, 0, mWidth, mHeight, mShadePaint);
        // 画一条虚线表示未运动到的轨迹
        mMotionPaint.setPathEffect(mDashPathEffect);
        canvas.drawPath(mMotionPath, mMotionPaint);

        // 画日出日落文字
        /*if (mSunrise.length != 0||mSunset.length != 0){
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("日出 "+(mSunrise[0]<10? "0"+mSunrise[0]: mSunrise[0])
                    +":"+(mSunrise[1]<10? "0"+mSunrise[1]: mSunrise[1]), startX+textOffset, startY, mTextPaint);
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("日落 "+(mSunset[0]<10? "0"+mSunset[0]: mSunset[0])
                    +":"+(mSunset[1]<10? "0"+mSunset[1]: mSunset[1]), endX-textOffset, endY, mTextPaint);
        }*/
        // 画太阳
        canvas.drawCircle((float) rX, (float)rY, svSunSize*6/5, mSunStrokePaint);
        canvas.drawCircle((float) rX, (float)rY, svSunSize, mSunPaint);
        // 画端点
        mMotionPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(startX, startY, svTrackWidth*2, mMotionPaint);
        canvas.drawCircle(endX, endY, svTrackWidth*2, mMotionPaint);

        canvas.restore();
    }

    /**
     * 设置当前进度，并更新太阳中心点的位置
     * @param t
     */
    private void setProgress(float t){
        mProgress = t;
        rX = startX * Math.pow(1 - t, 2) + 2 * controlX * t * (1 - t) + endX * Math.pow(t, 2);
        rY = startY * Math.pow(1 - t, 2) + 2 * controlY * t * (1 - t) + endY * Math.pow(t, 2);
        invalidate();
    }

    /**
     * 设置当前时间(请先设置日出日落时间)
     */
    public void setCurrentTime(int hour, int minute){
        if (mSunrise.length != 0||mSunset.length != 0){
            float p0 = mSunrise[0]*60+mSunrise[1];// 起始分钟数
            float p1 = hour*60+minute-p0;// 当前时间总分钟数
            float p2 = mSunset[0]*60+mSunset[1]-p0;// 日落到日出总分钟数
            float progress;
            if (p1 < 0) {
                progress = 0f;
            } else if (p1 > p2) {
                progress = 1f;
            } else {
                progress = p1/p2;
            }
            setProgress(progress);
            motionAnimation();
        }
    }

    /**
     * 设置日出时间
     */
    public void setSunrise(int hour, int minute){
        mSunrise[0] = hour;
        mSunrise[1] = minute;
    }

    /**
     * 设置日落时间
     */
    public void setSunset(int hour, int minute){
        mSunset[0] = hour;
        mSunset[1] = minute;
    }

    /**
     * 太阳轨迹动画
     */
    public void motionAnimation(){
        if (valueAnimator == null){
            mCurrentProgress = 0f;
            final ValueAnimator animator = ValueAnimator.ofFloat(mCurrentProgress, mProgress);
            animator.setDuration((long) (2500*(mProgress-mCurrentProgress)));
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Object val = animator.getAnimatedValue();
                    if (val instanceof Float){
                        setProgress((Float) val);
                        if ((Float)val == mProgress){
                            // 保存当前的进度，下一次更新即可以从上次时间运动到当前时间
                            mCurrentProgress = mProgress;
                        }
                    }
                }
            });
            valueAnimator = animator;
        } else {
            valueAnimator.cancel();
            valueAnimator.setFloatValues(mCurrentProgress, mProgress);
        }
        valueAnimator.start();
    }
}
