package com.slidingcaptcha;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Random;

/**
 * Created by ruoxuan.fu on 2018/2/27.
 * <p>
 * Code is far away from bug with WOW protecting.
 */

public class SlidingCaptchaView extends android.support.v7.widget.AppCompatImageView {


    public static final int WITH_SEEK = 0;
    public static final int WITH_FINGER = 1;

    @IntDef({WITH_SEEK, WITH_FINGER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SlidingMode {
    }

    /**
     * 滑块高度
     */
    private int mCaptchaHeight;

    /**
     * 滑块宽度
     */
    private int mCaptchaWidth;

    /**
     * 默认最大验证偏差
     */
    private float mMatchDeviation;

    /**
     * 控件宽度
     */
    private int mWidth;

    /**
     * 控件高度
     */
    private int mHeight;

    /**
     * 失败动画
     */
    private ValueAnimator mFailAnim;

    /**
     * 成功动画
     */
    private ValueAnimator mSuccessAnim;

    /**
     * 动画监听
     */
    private OnCaptchaMatchListener mOnCaptchaMatchListener;

    /**
     * 是否绘制滑块
     */
    private boolean isDrawSlider;

    /**
     * 是否绘制成功动画
     */
    private boolean isShowSuccessAnim;

    /**
     * 是否处于验证模式，在验证成功后 为false，其余情况为true
     */
    private boolean isMatchMode;

    /**
     * 成功动画平移轨迹
     */
    private float mSuccessAnimOffset;

    /**
     * 成功动画画笔
     */
    private Paint mSuccessPaint;

    /**
     * 成功动画的Path
     */
    private Path mSuccessPath;

    /**
     * 产生随机数
     */
    private Random mRandom;

    /**
     * 验证码阴影区域的X坐标
     */
    private int mCaptchaX;

    /**
     * 验证码阴影区域的Y坐标
     */
    private int mCaptchaY;

    /**
     * 验证码区域Path
     */
    private Path mCaptchaPath;

    /**
     * 滑块的bitmap
     */
    private Bitmap mSliderBitmap;

    /**
     * 滑块阴影的bitmap
     */
    private Bitmap mSliderShadowBitmap;

    /**
     * 滑块被拖动的距离
     */
    private int mDragOffset;

    /**
     * 滑块的画笔
     */
    private Paint mSliderPaint;

    /**
     * 滑块阴影的画笔
     */
    private Paint mMaskShadowPaint;

    /**
     * 待校验部分的画笔
     */
    private Paint mPaint;

    /**
     * 超出部分的半径
     */
    private int mOutSideR;

    /**
     * 左边是否超出
     * 180是超出
     */
    private int mLeftOutSide;

    /**
     * 右边是否超出
     * 180是超出
     */
    private int mRightOutSide;

    /**
     * 滑动方式
     * 0，用seek
     * 1，用手指
     */
    private int mSliderMode;

    /**
     * 手指当前位置的X坐标
     */
    private float mSlidingX;

    /**
     * 手指当前位置的Y坐标
     */
    private float mSlidingY;

    public SlidingCaptchaView(Context context) {
        this(context, null);
    }

    public SlidingCaptchaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingCaptchaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        init(context, attrs, defStyleAttr);
    }

    /**
     * 设置验证方式
     */
    public void setSliderMode(@SlidingMode int slidingMode) {
        this.mSliderMode = slidingMode;
    }

    /**
     * 设置动画监听
     */
    public void setOnCaptchaMatchListener(OnCaptchaMatchListener onCaptchaMatchListener) {
        mOnCaptchaMatchListener = onCaptchaMatchListener;
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        //默认尺寸
        int defaultSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                getResources().getDisplayMetrics());
        mMatchDeviation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3,
                getResources().getDisplayMetrics());
        mCaptchaHeight = defaultSize;
        mCaptchaWidth = defaultSize;
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlidingCaptchaView,
                defStyleAttr, 0);
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int a = typedArray.getIndex(i);
            if (a == R.styleable.SlidingCaptchaView_captchaWidth) {
                mCaptchaWidth = (int) typedArray.getDimension(typedArray.getIndex(i), defStyleAttr);

            } else if (a == R.styleable.SlidingCaptchaView_captchaHeight) {
                mCaptchaHeight = (int) typedArray.getDimension(typedArray.getIndex(i), defStyleAttr);

            } else if (a == R.styleable.SlidingCaptchaView_matchDeviation) {
                mMatchDeviation = typedArray.getDimension(typedArray.getIndex(i), mMatchDeviation);

            } else if (a == R.styleable.SlidingCaptchaView_slidingMode) {
                mSliderMode = typedArray.getInt(typedArray.getIndex(i), WITH_SEEK);
            }
        }
        typedArray.recycle();

        //产生随机数
        mRandom = new Random(System.nanoTime());

        //验证区域Path
        mCaptchaPath = new Path();

        //实例化滑块画笔
        mSliderPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        //实例化滑块阴影的画笔
        mMaskShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mMaskShadowPaint.setColor(Color.BLACK);
        // 设置画笔遮罩滤镜
        mMaskShadowPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));

        //实例化待校验部分的画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setColor(0xB3000000);
        // 设置画笔遮罩滤镜
        // 增加阴影效果，需要关闭硬件加速
        // setLayerType(LAYER_TYPE_SOFTWARE, null);
        // BlurMaskFilter(float radius, BlurMaskFilter.Blur style)
        // radius值越大我们的阴影越扩散
        // style表示的是模糊的类型
        // · SOLID 图像的Alpha边界外产生一层与Paint颜色一致的阴影效果而不影响图像本身
        // · NORMAL 会将整个图像模糊掉。
        // · OUTER 会在Alpha边界外产生一层阴影且会将原本的图像变透明
        // · INNER 会在图像内部产生模糊，有种淡淡的浮雕感
        mPaint.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.SOLID));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        //设置动画区域
        createMatchAnim();
        post(new Runnable() {
            @Override
            public void run() {
                createCaptcha();
            }
        });
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        post(new Runnable() {
            @Override
            public void run() {
                createCaptcha();
            }
        });
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        post(new Runnable() {
            @Override
            public void run() {
                createCaptcha();
            }
        });
    }

    /**
     * 设置验证正确和失败动画
     */
    private void createMatchAnim() {
        //失败动画
        mFailAnim = ValueAnimator.ofFloat(0, 1);
        //持续时间
        mFailAnim.setDuration(100);
        //重复模式，反向重复
        mFailAnim.setRepeatMode(ValueAnimator.REVERSE);
        //重复次数
        mFailAnim.setRepeatCount(4);
        //设置失败动画监听
        mFailAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mOnCaptchaMatchListener != null) {
                    mOnCaptchaMatchListener.onMatchFail();
                }
            }
        });
        mFailAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //当值大于0.5时，绘制滑块
                isDrawSlider = !((float) animation.getAnimatedValue() < 0.5);
                invalidate();
            }
        });

        //设置验证成功的动画平行四边形宽度
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mWidth / 4, getResources().getDisplayMetrics());
        //成功动画，从右到左，起始位置是控件右上角，结束位置是平行四边形右下角在课件左下角左侧
        mSuccessAnim = ValueAnimator.ofFloat(mWidth, -width / 2 * 3);
        mSuccessAnim.setDuration(500);
        mSuccessAnim.setRepeatMode(ValueAnimator.REVERSE);
        mSuccessAnim.setRepeatCount(1);
        //FastOutLinearInInterpolator MaterialDesign基于贝塞尔曲线的时间插补器 效果：依次 慢慢快
        mSuccessAnim.setInterpolator(new FastOutLinearInInterpolator());
        mSuccessAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mOnCaptchaMatchListener != null) {
                    mOnCaptchaMatchListener.onMatchSuccess();
                    isShowSuccessAnim = false;
                    isMatchMode = false;
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isShowSuccessAnim = true;
            }
        });
        mSuccessAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSuccessAnimOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        mSuccessPaint = new Paint();
        //Shader称为着色器，通过给Paint设置Shader，对图像进行渲染或颜色修改
        mSuccessPaint.setShader(new LinearGradient(0, 0, width / 2 * 3, mHeight,
                new int[]{0x00ffffff, 0x88ffffff}, new float[]{0, 0.5f}, Shader.TileMode.MIRROR));
        //设置画笔遮罩滤镜
        mSuccessPaint.setMaskFilter(new BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL));
        //一个平行四边形的Path
        mSuccessPath = new Path();
        mSuccessPath.moveTo(0, 0);
        mSuccessPath.rLineTo(width, 0);
        mSuccessPath.rLineTo(width / 2, mHeight);
        mSuccessPath.rLineTo(-width, 0);
        mSuccessPath.close();
    }

    /**
     * 创建验证码
     */
    public void createCaptcha() {
        if (getDrawable() != null) {
            //重置标记
            resetFlags();
            //生成验证码的path
            createCaptchaPath();
            //生成滑块
            createSlider();
            //刷新
            invalidate();
        }
    }

    //重置一些flags
    private void resetFlags() {
        //开启验证模式
        isMatchMode = true;
    }

    //生成验证码Path
    private void createCaptchaPath() {
        //设置验证码滑块凹凸距离顶点的值
        int gap = mCaptchaWidth / 3;
        mOutSideR = Math.abs(mCaptchaWidth / 2 - gap);
        //随机生成验证码阴影区域距离控件左上角的xy坐标
        //产生[gap~(mWidth - mCaptchaWidth - gap)]区间的值
        //避免验证码的凸出部分超出控件大小
        mCaptchaX = mRandom.nextInt(mWidth - mCaptchaWidth - 2 * gap) + gap;
        mCaptchaY = mRandom.nextInt(mHeight - mCaptchaHeight - 2 * gap) + gap;

        mSlidingX = mRandom.nextInt(mWidth - mCaptchaWidth - mCaptchaX - mOutSideR) - mCaptchaX + mOutSideR;
        mSlidingY = mRandom.nextInt(mHeight - mCaptchaHeight - mCaptchaY - mOutSideR) - mCaptchaY + mOutSideR;

        mCaptchaPath.reset();
        mCaptchaPath.lineTo(0, 0);
        //从左上角开始绘制验证码阴影区域，用椭圆绘制凹凸部分
        mCaptchaPath.moveTo(mCaptchaX, mCaptchaY);
        //设置椭圆半径
        mCaptchaPath.lineTo(mCaptchaX + gap, mCaptchaY);//上
        RectF oval = new RectF(mCaptchaX + gap, mCaptchaY - (mOutSideR), mCaptchaX + gap + (mOutSideR * 2), mCaptchaY + (mOutSideR));
        mCaptchaPath.arcTo(oval, 180, mRandom.nextBoolean() ? 180 : -180);

        mCaptchaPath.lineTo(mCaptchaX + mCaptchaWidth, mCaptchaY);//右
        oval = new RectF(mCaptchaX + mCaptchaWidth - (mOutSideR), mCaptchaY + gap,
                mCaptchaX + mCaptchaWidth + (mOutSideR), mCaptchaY + gap + (mOutSideR * 2));
        mCaptchaPath.arcTo(oval, 270, mRightOutSide = mRandom.nextBoolean() ? 180 : -180);

        mCaptchaPath.lineTo(mCaptchaX + mCaptchaWidth, mCaptchaY + mCaptchaHeight);//下
        oval = new RectF(mCaptchaX + gap, mCaptchaY + mCaptchaHeight - (mOutSideR),
                mCaptchaX + gap + (mOutSideR * 2), mCaptchaY + mCaptchaHeight + (mOutSideR));
        mCaptchaPath.arcTo(oval, 0, mRandom.nextBoolean() ? 180 : -180);

        mCaptchaPath.lineTo(mCaptchaX, mCaptchaY + mCaptchaHeight);//左
        oval = new RectF(mCaptchaX - (mOutSideR), mCaptchaY + gap, mCaptchaX + (mOutSideR), mCaptchaY + gap + (mOutSideR * 2));
        mCaptchaPath.arcTo(oval, 90, mLeftOutSide = mRandom.nextBoolean() ? 180 : -180);

        mCaptchaPath.close();

    }

    /**
     * 绘制滑块
     */
    private void createSlider() {
        //创建滑块
        //传入背景和滑块的Path
        mSliderBitmap = getSliderBitmap(((BitmapDrawable) getDrawable()).getBitmap(), mCaptchaPath);
        //生成滑块阴影位置
        mSliderShadowBitmap = mSliderBitmap.extractAlpha();
        //拖动的位移重置
        mDragOffset = 0;
        //isDrawSlider  绘制失败闪烁动画用
        isDrawSlider = true;
    }

    /**
     * 创建滑块
     */
    private Bitmap getSliderBitmap(Bitmap bitmap, Path captchaPath) {
        //以控件宽高create一块bitmap
        Bitmap tempBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        //把创建的bitmap作为画板
        Canvas canvas = new Canvas(tempBitmap);
        // 抗锯齿
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        //绘制用于遮罩的圆形
        canvas.drawPath(captchaPath, mSliderPaint);
        //设置滑块遮盖方式
        PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        //设置遮罩模式(图像混合模式)
        mSliderPaint.setXfermode(porterDuffXfermode);
        //用Matrix对Bitmap进行缩放
        canvas.drawBitmap(bitmap, getImageMatrix(), mSliderPaint);
        mSliderPaint.setXfermode(null);
        return tempBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isMatchMode) {
            if (mCaptchaPath != null) {
                canvas.drawPath(mCaptchaPath, mPaint);
            }
            // 绘制滑块
            // isDrawMask 绘制失败闪烁动画用
            if (null != mSliderBitmap && null != mSliderShadowBitmap && isDrawSlider) {
                if (mLeftOutSide == 180) {
                    // 先绘制滑块的阴影
                    // 再绘制滑块
                    if (mSliderMode == 0) {
                        canvas.drawBitmap(mSliderShadowBitmap, (-mCaptchaX + mDragOffset + mOutSideR), 0, mMaskShadowPaint);
                        canvas.drawBitmap(mSliderBitmap, (-mCaptchaX + mDragOffset + mOutSideR), 0, null);
                    } else {
                        canvas.drawBitmap(mSliderShadowBitmap, (mSlidingX), mSlidingY, mMaskShadowPaint);
                        canvas.drawBitmap(mSliderBitmap, (mSlidingX), mSlidingY, null);
                    }
                } else {
                    // 先绘制滑块的阴影
                    // 再绘制滑块
                    // 滑块的X坐标要设置为：(-mCaptchaX + mDragOffset)是因为，滑块的Bitmap是由图像混合模式得到的，
                    // (mCaptchaX,mCaptchaY)坐标是图像混合后的图案的左上角的坐标，也是待验证阴影的左上角的坐标，
                    // (-mCaptchaX + mDragOffset)则是滑块相对于待验证阴影的左上角X坐标的相对位置，
                    // 最后验证是否成功则是(-mCaptchaX + mDragOffset)的绝对值是否<=3
                    if (mSliderMode == 0) {
                        canvas.drawBitmap(mSliderShadowBitmap, (-mCaptchaX + mDragOffset), 0, mMaskShadowPaint);
                        canvas.drawBitmap(mSliderBitmap, (-mCaptchaX + mDragOffset), 0, null);
                    } else {
                        canvas.drawBitmap(mSliderShadowBitmap, (mSlidingX), mSlidingY, mMaskShadowPaint);
                        canvas.drawBitmap(mSliderBitmap, (mSlidingX), mSlidingY, null);
                    }
                }
            }
            // 验证成功，白光扫过的动画
            if (isShowSuccessAnim) {
                //移动当前平行四边形画布
                canvas.translate(mSuccessAnimOffset, 0);
                canvas.drawPath(mSuccessPath, mSuccessPaint);
            }
        }
    }

    /**
     * 校验
     */
    public void matchCaptcha() {
        if (null != mOnCaptchaMatchListener && isMatchMode) {
            //这里验证逻辑，是通过比较，拖拽的距离 和 验证码起点x坐标。 默认3dp以内算是验证成功。
            if (mLeftOutSide == 180) {
                if (mSliderMode == 0) {
                    if (Math.abs(mDragOffset + mOutSideR - mCaptchaX) < mMatchDeviation) {
                        //成功的动画
                        mSuccessAnim.start();
                    } else {
                        mFailAnim.start();
                    }
                } else {
                    if ((Math.abs(mSlidingX) < mMatchDeviation)
                            && (Math.abs(mSlidingY) < mMatchDeviation)) {
                        //成功的动画
                        mSuccessAnim.start();
                    } else {
                        mFailAnim.start();
                    }
                }
            } else {
                if (mSliderMode == 0) {
                    if (Math.abs(mDragOffset - mCaptchaX) < mMatchDeviation) {
                        //成功的动画
                        mSuccessAnim.start();
                    } else {
                        mFailAnim.start();
                    }
                } else {
                    if ((Math.abs(mSlidingX) < mMatchDeviation)
                            && (Math.abs(mSlidingY) < mMatchDeviation)) {
                        //成功的动画
                        mSuccessAnim.start();
                    } else {
                        mFailAnim.start();
                    }
                }
            }
        }
    }

    /**
     * 重置验证码滑动距离(用于验证失败)
     */
    public void resetCaptcha() {
        if (mSliderMode == 0) {
            mDragOffset = 0;
        } else {
            mSlidingX = mRandom.nextInt(mWidth - mCaptchaWidth - mCaptchaX - mOutSideR) - mCaptchaX + mOutSideR;
            mSlidingY = mRandom.nextInt(mHeight - mCaptchaHeight - mCaptchaY - mOutSideR) - mCaptchaY + mOutSideR;
        }
        invalidate();
    }

    /**
     * 最大可滑动值
     */
    public int getMaxSwipeValue() {
        //返回控件宽度
        if ((mLeftOutSide == 180 && mRightOutSide == -180) || (mLeftOutSide == -180 && mRightOutSide == 180)) {
            return mWidth - mCaptchaWidth - mOutSideR;
        } else if (mLeftOutSide == 180 && mRightOutSide == 180) {
            return mWidth - mCaptchaWidth - mOutSideR * 2;
        } else {
            return mWidth - mCaptchaWidth;
        }
    }

    /**
     * 设置当前滑动值
     */
    public void setCurrentSwipeValue(int value) {
        mDragOffset = value;
        invalidate();
    }

    /**
     * 手指滑动
     * 设置当前滑块位置
     */
    public void setSliderSwipeValue(float x, float y) {
        mSlidingX = x - mCaptchaX - (mCaptchaWidth / 2);
        mSlidingY = y - mCaptchaY - (mCaptchaHeight / 2);
        invalidate();
    }

    public interface OnCaptchaMatchListener {
        void onMatchSuccess();

        void onMatchFail();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && mSliderMode == 1) {
            if (event.getX() < (mCaptchaX + mSlidingX)
                    || event.getX() > (mCaptchaX + mSlidingX + mCaptchaWidth)
                    || event.getY() < (mCaptchaY + mSlidingY)
                    || event.getY() > (mCaptchaY + mSlidingY + mCaptchaHeight)) {
                return false;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setSliderSwipeValue(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                setSliderSwipeValue(event.getX(), event.getY());
                matchCaptcha();
                break;
            case MotionEvent.ACTION_MOVE:
                setSliderSwipeValue(event.getX(), event.getY());
                break;
        }
        return true;
    }
}
