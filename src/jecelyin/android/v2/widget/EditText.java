/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jecelyin.android.v2.widget;

import java.util.zip.CRC32;

import com.jecelyin.editor.R;
import com.jecelyin.util.JecLog;
import com.jecelyin.widget.TouchZoom;

import jecelyin.android.compat.EditTextInterface;
import jecelyin.android.compat.EditableFactory;
import jecelyin.android.v2.text.Layout;
import jecelyin.android.v2.text.Selection;
import jecelyin.android.v2.text.TextUtils;
import jecelyin.android.v2.text.method.ArrowKeyMovementMethod;
import jecelyin.android.v2.text.method.MovementMethod;
import jecelyin.android.v2.text.method.Touch;
import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;


/*
 * This is supposed to be a *very* thin veneer over TextView.
 * Do not make any changes here that do anything that a TextView
 * with a key listener and a movement method wouldn't do!
 */

/**
 * EditText is a thin veneer over TextView that configures itself
 * to be editable.
 *
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-formstuff.html">Form Stuff
 * tutorial</a>.</p>
 * <p>
 * <b>XML attributes</b>
 * <p>
 * See {@link android.R.styleable#EditText EditText Attributes},
 * {@link android.R.styleable#TextView TextView Attributes},
 * {@link android.R.styleable#View View Attributes}
 */
public class EditText extends TextView implements EditTextInterface {
    public EditText(Context context) {
        this(context, null);
    }

    public EditText(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.editTextStyle);
    }

    public EditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setEditableFactory(EditableFactory.getInstance());
    }

    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    @Override
    public Editable getText() {
        return (Editable) super.getText();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int, int)}.
     */
    public void setSelection(int start, int stop) {
        Selection.setSelection(getText(), start, stop);
    }

    /**
     * Convenience for {@link Selection#setSelection(Spannable, int)}.
     */
    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }

    /**
     * Convenience for {@link Selection#selectAll}.
     */
    public void selectAll() {
        Selection.selectAll(getText());
    }

    /**
     * Convenience for {@link Selection#extendSelection}.
     */
    public void extendSelection(int index) {
        Selection.extendSelection(getText(), index);
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
            throw new IllegalArgumentException("EditText cannot use the ellipsize mode "
                    + "TextUtils.TruncateAt.MARQUEE");
        }
        super.setEllipsize(ellipsis);
    }
    
    //jec+: fast touch scroll
    /**
     * Responsible for fling behavior. Use {@link #start(int)} to initiate a
     * fling. Each frame of the fling is handled in {@link #run()}. A
     * FlingRunnable will keep re-posting itself until the fling is done.
     * 
     */
    private static class FlingRunnable implements Runnable
    {

        static final int TOUCH_MODE_REST = -1;
        static final int TOUCH_MODE_FLING = 3;

        int mTouchMode = TOUCH_MODE_REST;

        /**
         * Tracks the decay of a fling scroll
         */
        private final Scroller mScroller;

        /**
         * Y value reported by mScroller on the previous fling
         */
        private int mLastFlingY;

        private EditText mWidget = null;

        FlingRunnable(Context context)
        {
            mScroller = new Scroller(context);
        }

        void start(EditText parent, int initialVelocity)
        {
            mWidget = parent;
            int initialX = parent.getScrollX(); // initialVelocity < 0 ?
                                                // Integer.MAX_VALUE : 0;
            int initialY = parent.getScrollY(); // initialVelocity < 0 ?
                                                // Integer.MAX_VALUE : 0;
            mLastFlingY = initialY;
            mScroller.fling(initialX, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            mTouchMode = TOUCH_MODE_FLING;

            mWidget.post(this);

        }

        private void endFling()
        {
            mTouchMode = TOUCH_MODE_REST;

            if(mWidget != null)
            {
                try
                {
                    mWidget.removeCallbacks(this);
                    mWidget.moveCursorToVisibleOffset();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

                mWidget = null;
            }

        }

        public void run()
        {
            switch(mTouchMode)
            {
                default:
                    return;

                case TOUCH_MODE_FLING:
                {

                    final Scroller scroller = mScroller;
                    boolean more = scroller.computeScrollOffset();

                    int x = scroller.getCurrX();
                    int y = scroller.getCurrY();

                    Layout layout = mWidget.getLayout();
                    if(layout == null)
                        break;

                    int padding;
                    try
                    {
                        padding = mWidget.getTotalPaddingTop() + mWidget.getTotalPaddingBottom();
                    }catch (Exception e)
                    {
                        padding = 0;
                    }

                    y = Math.min(y, layout.getHeight() - (mWidget.getHeight() - padding));
                    y = Math.max(y, 0);

                    Touch.scrollTo(mWidget, layout, x, y);
                    int delta = mLastFlingY - y;

                    if(more && delta != 0)
                    {
                        mWidget.invalidate();
                        mLastFlingY = y;
                        mWidget.post(this);
                    }else
                    {
                        endFling();

                    }
                    break;
                }
            }

        }
    }
    
    private VelocityTracker mVelocityTracker;
    private FlingRunnable mFlingRunnable;
    
    public boolean onTouchEvent(MotionEvent event)
    {
        //滚动条优先
        if (mFastScroller != null) {
            if (mFastScroller.onInterceptTouchEvent(event))
                return true;

            if (mFastScroller.onTouchEvent(event))
                return true;
        }
        //放大第二
        if(mTouchZoom.onTouchEvent(event))
            return true;
        //快速滚动第三
        if(mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);

        // 处理文本快速顺畅地滚动
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                if(mFlingRunnable != null)
                {
                    mFlingRunnable.endFling();
                    cancelLongPress();
                }
                break;
            case MotionEvent.ACTION_UP:
                // cancelLongPress();

                int mMinimumVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
                int mMaximumVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final int initialVelocity = (int) mVelocityTracker.getYVelocity();

                if(Math.abs(initialVelocity) > mMinimumVelocity)
                {
                    try
                    {
                        if(mFlingRunnable == null)
                        {
                            mFlingRunnable = new FlingRunnable(getContext());
                        }
                        mFlingRunnable.start(this, -initialVelocity);
                    }catch (Exception e)
                    {
                    }
                }else
                {
                    //moveCursorToVisibleOffset();
                }

                if(mVelocityTracker != null)
                {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        
        return super.onTouchEvent(event);
    }

    @Override
    public void init()
    {
        super.init();
        mCRC32 = new CRC32();
        mTouchZoom = new TouchZoom(this);
    }

    @Override
    public void show()
    {
        setVisibility(View.VISIBLE);
        updateUndoRedoButtonStatus();
    }

    @Override
    public void hide()
    {
        setVisibility(View.GONE);
    }

    @Override
    public String getString()
    {
        String text = "";
        try
        {
            text = getText().toString();
        }catch (OutOfMemoryError e)
        {
            JecLog.msg(getContext().getString(R.string.out_of_memory));
        }
        return text;
    }

    private int mOldTextlength = 0;
    private long mOldTextCrc32 = 0;
    @Override
    public void updateTextFinger()
    {
        mOldTextlength = getText().length();
        byte bytes[] = getString().getBytes();
        mCRC32.reset();
        mCRC32.update(bytes,0,bytes.length);
        mOldTextCrc32 = mCRC32.getValue();
    }

    @Override
    public boolean isTextChanged()
    {
        CharSequence text = getText();
        int hash = text.length();
        //长度不相等，肯定是有更改了
        if(mOldTextlength != hash)
        {
            return true;
        }
        //进行CRC检验
        mCRC32.reset();
        byte bytes[] = getString().getBytes();
        mCRC32.update(bytes,0,bytes.length);
        return mOldTextCrc32 != mCRC32.getValue();
    }
    
    private CRC32 mCRC32;
    private TouchZoom mTouchZoom;
}
