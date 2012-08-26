/**
 *   920 Text Editor is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   920 Text Editor is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with 920 Text Editor.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jecelyin.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jecelyin.util.ColorPicker;
import com.jecelyin.util.ColorPicker.OnColorChangedListener;

public class ColorPickerView extends View {
    private Paint mPaint;
    private OnColorChangedListener mListener;
    private float center_radius;
    //中心圆半径比例
    private final static float CENTER_RADIUS_SCALE = 0.4f;
    private int[] mColors;
    private Paint mCenterPaint;
    private boolean mTrackingCenter;
    private boolean mHighlightCenter;
    private int mColor1;
    private int mColor2;
    private Paint mLeftPaint;
    private int mColor3;
    private Paint mRightPaint;
    private float mRadius;
    private int mColor4 = 0xFFFFFFFF;;

    public void setOnColorChangedListener(OnColorChangedListener ccl)
    {
        mListener = ccl;
    }
    
    public ColorPickerView(Context context, AttributeSet attr)
    {
        super(context, attr);
        init();
    }
    
    public ColorPickerView(Context c) {
        super(c);
    }
    
    private void init()
    {
        mColors = new int[] {
            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
            0xFFFFFF00, 0xFFFF0000
        };
        mColor1 = 0xFFFFFFFF;
        mColor2 = 0xFF000000;
        Shader s = new SweepGradient(0, 0, mColors, null);
        
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(s);
        mPaint.setStyle(Paint.Style.STROKE);
        
        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setStrokeWidth(5);
        
        mLeftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mLeftPaint.setStrokeWidth(10);
        
        mRightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mRightPaint.setStrokeWidth(10);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // view size
        int width = getWidth();
        int height = getHeight();
        // left and right picker width
        float lr_width = width * 0.20F;
        float space = width * 0.03F;
        //半径
        float outer_radius = Math.min(width, height)*0.48f;
        float touch_feedback_ring = center_radius + 2*mCenterPaint.getStrokeWidth();
        mRadius = (outer_radius + touch_feedback_ring) / 2;
        
        //两边渐变
        Shader s = new LinearGradient(0, 0, 0, height, mColor1, mColor2, Shader.TileMode.CLAMP);
        mLeftPaint.setShader(s);
        canvas.drawRect(space, 0, lr_width, height, mLeftPaint);
        Shader sr = new LinearGradient(0, 0, 0, height, mColor3, mColor4, Shader.TileMode.CLAMP);
        mRightPaint.setShader(sr);
        canvas.drawRect(width-lr_width, 0, width-space, height, mRightPaint);
        //mLeftPaint.setShader(null);
        //end
        
        //转移坐标到中间
        canvas.translate(width/2, height/2);
        
        mPaint.setStrokeWidth(outer_radius - touch_feedback_ring);
        
        //色盘
        canvas.drawCircle(0, 0, mRadius, mPaint);

        //中间圆心
        canvas.drawCircle(0, 0, center_radius, mCenterPaint);
        
        if (mTrackingCenter) {
            int c = mCenterPaint.getColor();
            mCenterPaint.setStyle(Paint.Style.STROKE);
            
            if (mHighlightCenter) {
                mCenterPaint.setAlpha(0xFF);
            } else {
                mCenterPaint.setAlpha(0x80);
            }
            
            // The skinny ring around the center to indicate that it is being pressed
            canvas.drawCircle(0, 0,
                    center_radius + mCenterPaint.getStrokeWidth(),
                              mCenterPaint);
            
            mCenterPaint.setStyle(Paint.Style.FILL);
            mCenterPaint.setColor(c);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //窗口尺寸
        int max_width = MeasureSpec.getSize(widthMeasureSpec);
        int max_height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(max_width, max_height);
        //中间圆心的圆周率
        this.center_radius = CENTER_RADIUS_SCALE * (size*0.5F)/2;
        
        setMeasuredDimension(size, (int)(size*0.6f));
    }
    
    public void setColor(int color)
    {
        mCenterPaint.setColor(color);
        //左边色盘坐当前色到暗色
        mColor1 = color;
        mColor2 = 0xff000000;
        mColor3 = color;
        mColor4 = 0xFFFFFFFF;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int width = getWidth();
        int height = getHeight();
        float x = event.getX() - width/2; // - width * 0.20F
        float y = event.getY() - height/2;
        
        boolean inCenter = PointF.length(x, y) <= center_radius;
        //Log.v("color", "x:"+x+" ex:"+event.getX()+" y:"+y+" center_radius:"+center_radius+" len:"+PointF.length(x, y));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;
                if (inCenter) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter;
                        invalidate();
                    }
                } else {
                    float unit;
                    int newcolor;
                    //左边色盘
                    if(event.getX() <= width*0.2F)
                    {
                        unit = event.getY() / height;
                        newcolor = interpColor(new int[] {mColor1, mColor2}, unit, true);
                        mColor3 = newcolor;
                    } else if (event.getX() >= width-width*0.2F) { //右边色盘
                        unit = event.getY() / height;
                        newcolor = interpColor(new int[] {mColor3, mColor4}, unit, true);
                    } else {
                        //控制色盘范围
                        float angle = (float)java.lang.Math.atan2(y, x);
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        unit = angle/(2*(float) Math.PI);
                        if (unit < 0) {
                            unit += 1;
                        }
                        newcolor = interpColor(mColors, unit, false);
                        setColor(newcolor);
                        
                    }
                    if (mListener != null) {
                        mListener.onColorChanged("",ColorPicker.getColor(newcolor));
                    }
                    mCenterPaint.setColor(newcolor);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingCenter) {
                    if (inCenter) {
                        if (mListener != null) {
                            //mListener.onColorPicked(this, mCenterPaint.getColor());
                        }
                    }
                    mTrackingCenter = false;    // so we draw w/o halo
                    invalidate();
                }
                break;
        }
        return true;
    }
    
    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }
    
    private int interpColor(int colors[], float unit, boolean isSlider) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }
        float p;
        int c0,c1;
        if(isSlider)
        {
            p = unit;
            c0 = colors[0];
            c1 = colors[colors.length - 1];
        } else {
            p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;
            c0 = colors[i];
            c1 = colors[i+1];
        }

        // now p is just the fractional part [0...1) and i is the index
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);
        
        return Color.argb(a, r, g, b);
    }
    
}