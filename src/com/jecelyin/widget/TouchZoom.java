package com.jecelyin.widget;

import jecelyin.android.compat.TextViewBase;

import com.jecelyin.editor.EditorSettings;

import android.util.FloatMath;
import android.view.MotionEvent;

public class TouchZoom
{
    private static final int TOUCH_DRAG_START_MODE = 1;
    private static final int TOUCH_DONE_MODE = 2;
    private int mTouchMode;
    /** 记录按下第二个点距第一个点的距离 */
    private float oldDist;
    
    /** 缩放比例 */
    private float scale = 0.5f;
    /** 设置字体大小 */
    private float mTextSize;
    private TextViewBase mTextViewBase;
    
    public TouchZoom(TextViewBase tvb)
    {
        mTextViewBase = tvb;
    }
    
    public boolean onTouchEvent(MotionEvent event)
    {
        if(!EditorSettings.ENABLE_TOUCH_ZOOM || !EditorSettings.MULTI_TOUCH)
            return false;
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount() >= 2) {
            mTouchMode = TOUCH_DRAG_START_MODE;
            oldDist = calc_spacing(event);
            return true;
        } else if (action == MotionEvent.ACTION_POINTER_UP && mTouchMode == TOUCH_DRAG_START_MODE) {
            mTouchMode = TOUCH_DONE_MODE;
            return true;
        } else if(action == MotionEvent.ACTION_MOVE && mTouchMode == TOUCH_DRAG_START_MODE && event.getPointerCount() >= 2)
        {
            mTextViewBase.cancelLongPress();
            // 正在移动的点距初始点的距离
            float newDist = calc_spacing(event);

            if(Math.abs(newDist - oldDist) > 10f)
            {
                if(newDist > oldDist)
                {
                    zoomOut();
                }else if(newDist < oldDist)
                {
                    zoomIn();
                }
                oldDist = newDist;
            }
            return true;
        }
        return false;
    }
    
    /**
     * 求出2个触点间的 距离
     * 
     * @param event
     * @return
     */
    private float calc_spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 放大
     */
    protected void zoomOut()
    {
        mTextSize += scale;

        if(mTextSize > EditorSettings.MAX_TEXT_SIZE)
            mTextSize = EditorSettings.MAX_TEXT_SIZE;
        else if(mTextSize < EditorSettings.MIN_TEXT_SIZE)
            mTextSize = EditorSettings.MIN_TEXT_SIZE;

        mTextViewBase.setTextSize(mTextSize);
    }

    /**
     * 缩小
     */
    protected void zoomIn()
    {
        mTextSize -= scale;
        
        if(mTextSize > EditorSettings.MAX_TEXT_SIZE)
            mTextSize = EditorSettings.MAX_TEXT_SIZE;
        else if(mTextSize < EditorSettings.MIN_TEXT_SIZE)
            mTextSize = EditorSettings.MIN_TEXT_SIZE;
        
        mTextViewBase.setTextSize(mTextSize);
    }
}
