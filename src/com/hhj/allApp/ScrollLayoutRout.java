package src.com.hhj.allApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hhj.allApp.AppAdapter.AppItem;
import com.hhj.allApp.MyView.BigStone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Scroller;

public class ScrollLayoutRout extends ViewGroup {

    private static final String TAG = "HHJ";
    // 用于滑动的类
    private Scroller mScroller;
    // 用来跟踪触摸速度的类
    private VelocityTracker mVelocityTracker;
    // 当前的屏幕视图
    private int mCurScreen;
    // 默认的显示视图
    private int mDefaultScreen = 0;
    // 无事件的状态
    private static final int TOUCH_STATE_REST = 0;
    // 处于拖动的状态
    private static final int TOUCH_STATE_SCROLLING = 1;
    // 滑动的速度
    private static final int SNAP_VELOCITY = 600;

    private int mTouchState = TOUCH_STATE_REST;
    private int mTouchSlop;
    private float mLastMotionX;
    // 用来处理立体效果的类
    private Camera mCamera;
    private Matrix mMatrix;
    // 旋转的角度
    private float angle = 100;
    
    private int mNum;
    
    Paint mPaint = new Paint();
    
    private Context mContext;

    public ScrollLayoutRout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    // 在构造器中初始化
    public ScrollLayoutRout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mScroller = new Scroller(context);

        mCurScreen = mDefaultScreen;
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mCamera = new Camera();
        mMatrix = new Matrix();
    }

    /*
     * 
     * 为子View指定位置
     */
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        // TODO Auto-generated method stub
        Log.e(TAG, "onLayout");

        if (changed) {
            int childLeft = 0;
            final int childCount = getChildCount();

            for (int i = 0; i < childCount; i++) {
                final View childView = getChildAt(i);
                if (childView.getVisibility() != View.GONE) {
                    final int childWidth = childView.getMeasuredWidth();
                    childView.layout(childLeft, 0, childLeft + childWidth,
                            childView.getMeasuredHeight());
                    childLeft += childWidth;
                }
            }
        }
    }

    // 重写此方法用来计算高度和宽度
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        // Exactly：width代表的是精确的尺寸
        // AT_MOST：width代表的是最大可获得的空间
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout only canmCurScreen run at EXACTLY mode!");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout only can run at EXACTLY mode!");
        }

        // The children are given the same width and height as the scrollLayout
        // 得到多少页(子View)并设置他们的宽和高
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        // Log.e(TAG, "moving to screen "+mCurScreen);
        scrollTo(mCurScreen * width, 0);
    }

    /*
     * 当进行View滑动时，会导致当前的View无效，该函数的作用是对View进行重新绘制 调用drawScreen函数
     */
    protected void dispatchDraw(Canvas canvas) {
        //Log.i(TAG, "dispatchDraw");
        final long drawingTime = getDrawingTime();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            drawScreen(canvas, i, drawingTime,1);//mNum
        }
    }
    BigStone[] mStones;
    /**圆心坐标*/
    int mPointX=0, mPointY=0;
   /**定义一个圆的半径*/
    int mRadius = 180;
    
    /**定义一个陀螺的半径*/
    int[] mmRadius = {30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180};
    /**每两个点间隔的角度*/
    int mDegreeDelta;
    /**是否在拖动*/
    boolean isDrag=false;

    public void drawScreen(Canvas canvas, int screen, long drawingTime,int selsect) {
        if(selsect == 1){ //圆
            Log.i(TAG, "isDrag:"+isDrag+"    "+getScrollX());
            if(isDrag){
                GridView gridView = (GridView) getChildAt(screen);
                int width = getWidth();
                int height = getHeight();
                int screenCount = gridView.getChildCount();
                mStones = new BigStone[screenCount];;
                
                mPointX = screen*width+width/2;
                mPointY =height/2;
                
                BigStone stone;
                int angle = 0;
                mDegreeDelta = 180/screenCount;
                
                for(int index=0; index<screenCount; index++) {
                    stone = new BigStone();
                    stone.angle = angle;  
                    AppItem info = (AppItem) gridView.getChildAt(index).getTag();//将得到的icon图片赋值给图标
                    BitmapDrawable bd = (BitmapDrawable)info.mAppIcon.getDrawable();     
                    stone.text = "" +info.mAppName.getText().toString();
                    stone.bitmap  = bd.getBitmap();
                    angle += mDegreeDelta;
                    mStones[index] = stone;
                }
                
                BigStone stoneInit;
                for(int index=0; index<screenCount; index++) {
                    stoneInit = mStones[index];
                    stoneInit.x = mPointX+ (float)(mRadius * Math.cos(stoneInit.angle*Math.PI/90));//stone.angle*Math.PI/180(弧度=角度*3.14)
                    stoneInit.y = mPointY+ (float)(mRadius * Math.sin(stoneInit.angle*Math.PI/90));
                }
                
                
                for(int index=0; index<screenCount; index++) {
                    canvas.drawPoint(mStones[index].x, mStones[index].y, mPaint);
                    canvas.drawBitmap(mStones[index].bitmap, mStones[index].x-mStones[index].bitmap.getWidth()/2, mStones[index].y-mStones[index].bitmap.getHeight()/2, null);
                    canvas.drawText(mStones[index].text,mStones[index].x-mStones[index].bitmap.getWidth()/2+2, mStones[index].y+mStones[index].bitmap.getHeight()/2+8, mPaint);
                }
            }else{
                super.dispatchDraw(canvas);
            }

            

        } else if(selsect==2){
            // 得到当前子View的宽度
            final int width = getWidth();
            final int scrollWidth = screen * width;
            final int scrollX = this.getScrollX();
            if (scrollWidth > scrollX + width || scrollWidth + width < scrollX) {
                return;
            }
            final View child = getChildAt(screen);
            final int faceIndex = screen;
            final float currentDegree = getScrollX() * (angle / getMeasuredWidth());
            final float faceDegree = currentDegree - faceIndex * angle;
            if (faceDegree > 90 || faceDegree < -90) {
                return;
            }
            final float centerX = (scrollWidth < scrollX) ? scrollWidth + width
                    : scrollWidth;
            final float centerY = getHeight() / 2;
            final Camera camera = mCamera;
            final Matrix matrix = mMatrix;
            canvas.save();
            camera.save();
            camera.rotateY(-faceDegree);
            camera.getMatrix(matrix);
            camera.restore();
            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
            canvas.concat(matrix);
            drawChild(canvas, child, drawingTime);
            canvas.restore();
        }

    }
    
    /**
     * 根据目前的位置滚动到下一个视图位置.
     */
    public void snapToDestination() {
        final int screenWidth = getWidth();
        // 根据View的宽度以及滑动的值来判断是哪个View
        final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
        snapToScreen(destScreen);
    }

    public void snapToScreen(int whichScreen) {
        // get the valid layout page
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        if (getScrollX() != (whichScreen * getWidth())) {

            final int delta = whichScreen * getWidth() - getScrollX();
            mScroller.startScroll(getScrollX(), 0, delta, 0,
                    Math.abs(delta) * 2);
            mCurScreen = whichScreen;
            invalidate(); // 重新布局
        }
    }

    public void setToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        mCurScreen = whichScreen;
        scrollTo(whichScreen * getWidth(), 0);
    }

    public int getCurScreen() {
        return mCurScreen;
    }

    @Override
    public void computeScroll() {
        // TODO Auto-generated method stub
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        //Log.i(TAG, "onTouchEvent");
        
        if (mVelocityTracker == null) {
            // 使用obtain方法得到VelocityTracker的一个对象
            mVelocityTracker = VelocityTracker.obtain();
        }
        // 将当前的触摸事件传递给VelocityTracker对象
        mVelocityTracker.addMovement(event);
        // 得到触摸事件的类型
        final int action = event.getAction();
        final float x = event.getX();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            mLastMotionX = x;
            break;

        case MotionEvent.ACTION_MOVE:
            isDrag = true;
            int deltaX = (int) (mLastMotionX - x);
            mLastMotionX = x;

            scrollBy(deltaX, 0);
            break;

        case MotionEvent.ACTION_UP:
            isDrag = false;
            //Log.e(TAG, "event : up");
            // if (mTouchState == TOUCH_STATE_SCROLLING) {
            final VelocityTracker velocityTracker = mVelocityTracker;
            // 计算当前的速度
            velocityTracker.computeCurrentVelocity(1000);
            // 获得当前的速度
            int velocityX = (int) velocityTracker.getXVelocity();


            if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
                // Fling enough to move left
                snapToScreen(mCurScreen - 1);
            } else if (velocityX < -SNAP_VELOCITY
                    && mCurScreen < getChildCount() - 1) {
                // Fling enough to move right
                snapToScreen(mCurScreen + 1);
            } else {
                snapToDestination();
            }

            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            // }
            mTouchState = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
            break;
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        //Log.i(TAG, "onInterceptTouchEvent-slop:");

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();

        switch (action) {
        case MotionEvent.ACTION_MOVE:
            final int xDiff = (int) Math.abs(mLastMotionX - x);
            if (xDiff > mTouchSlop) {
                mTouchState = TOUCH_STATE_SCROLLING;

            }
            break;

        case MotionEvent.ACTION_DOWN:
            mNum = new Random().nextInt(5);
            mLastMotionX = x;
            mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                    : TOUCH_STATE_SCROLLING;
            break;

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            mTouchState = TOUCH_STATE_REST;
            break;
        }

        return mTouchState != TOUCH_STATE_REST;
    }

    class BigStone {
        //图片
        Bitmap bitmap;
        //角度
        int angle;
        //x坐标
        float x;
        //y坐标
        float y;
        //是否可见
        String text;
        boolean isVisible = true;
    }
}
