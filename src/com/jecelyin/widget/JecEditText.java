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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import com.jecelyin.colorschemes.ColorScheme;
import com.jecelyin.editor.JecEditor;
import com.jecelyin.editor.R;
import com.jecelyin.editor.UndoParcel;
import com.jecelyin.editor.UndoParcel.TextChange;
import com.jecelyin.highlight.Highlight;
import com.jecelyin.util.FileUtil;
import com.jecelyin.util.TextUtil;
import com.jecelyin.util.TimeUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.MetaKeyKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.text.method.Touch;
import android.text.style.ParagraphStyle;
import android.text.style.TabStopSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

public class JecEditText extends EditText
{
    // private Rect mRect;
    private Paint mWhiteSpacePaint;
    private Paint mLineNumberPaint;
    private boolean mShowWhiteSpace = false;
    private boolean mShowLineNum = true;
    private Path mLineBreakPath = new Path();
    private Path mTabPath = new Path();
    private Path[] mWhiteSpacePaths = new Path[]{ mTabPath, mLineBreakPath };
    private TextPaint mTextPaint;
    private TextPaint mWorkPaint;
    /** 缩放比例 */

    private int paddingLeft = 0;
    private int lastPaddingLeft = 0;
    private int realLineNum = 0;
    private boolean hasNewline = true;
    private static float TAB_INCREMENT = 20F;
    private static Rect sTempRect = new Rect();
    private FastScroller mFastScroller;
    private Layout mLayout;
    private Editable mText = null;
    private UndoParcel mUndoParcel = new UndoParcel(); // 撤销与重做缓存
    private UndoParcel mRedoParcel = new UndoParcel(); // 撤销与重做缓存
    private boolean mUndoRedo = false; // 是否撤销过
    private boolean mAutoIndent = false;
    private HashMap<Integer, String> mLineStr = new HashMap<Integer, String>();
    private int mLineNumber = 0; // 总行数
    private int mLineNumberWidth = 0; // 行数栏宽度
    private int mLineNumberLength = 0; // 行数字数
    private ArrayList<Integer> mLastEditBuffer = new ArrayList<Integer>();
    private final static int LAST_EDIT_DISTANCE_LIMIT = 20; // 最后编辑位置距离限制，不做同行判断
    private int mLastEditIndex = -1; // 最后编辑位置功能的游标

    private final static String TAG = "JecEditText";
    private VelocityTracker mVelocityTracker;
    private FlingRunnable mFlingRunnable;

    private String current_encoding = "UTF-8"; // 当前文件的编码,用于正确回写文件
    private String current_path = ""; // 当前打开的文件路径
    private String current_ext = ""; // 当前扩展名
    private String current_title = ""; // 当前tab标题
    private int current_linebreak = 0; // 换行字符
    private int src_text_length; // 原始文本内容长度
    private long src_text_crc32; // crc校验
    private CRC32 mCRC32;
    private boolean mNoWrapMode = false;
    private int mLineNumX = 0; // 行数位置
    private String mDateFormat = "0";

    private Highlight mHighlight;
    /**
     * Touch mode
     */
    public static boolean TOUCH_ZOOM_ENABLED = true;
    private static final int TOUCH_DRAG_START_MODE = 2;
    private static final int TOUCH_DONE_MODE = 7;
    private int mTouchMode = TOUCH_DONE_MODE;
    /** 记录按下第二个点距第一个点的距离 */
    private float oldDist;
    /** 最小字体 */
    private static final float MIN_TEXT_SIZE = 10f;
    /** 最大字体 */
    private static final float MAX_TEXT_SIZE = 32.0f;
    /** 缩放比例 */
    private float scale = 0.5f;
    /** 设置字体大小 */
    private float mTextSize;
    // whether support multi-touch
    private boolean mSupportMultiTouch;
    //是否显示输入法
    private static boolean mHideSoftKeyboard;
    //首字母大写
    private static boolean mAutoCapitalize = false;
    //禁止拼写检查
    private static boolean mDisableSpellCheck = false;
    //是否使用系统菜单
    private static boolean USE_SYSTEM_MENU = true;

    // we need this constructor for LayoutInflater
    public JecEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // init();
    }

    private static class JecSaveState extends BaseSavedState
    {
        UndoParcel mRedoParcelState;
        UndoParcel mUndoParcelState;

        JecSaveState(Parcelable superState)
        {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags)
        {
            super.writeToParcel(out, flags);
            out.writeParcelable(mUndoParcelState, 0);
            out.writeParcelable(mRedoParcelState, 0);
        }

        private JecSaveState(Parcel in)
        {
            super(in);
            mUndoParcelState = in.readParcelable(UndoParcel.class.getClassLoader());
            mRedoParcelState = in.readParcelable(UndoParcel.class.getClassLoader());
        }
    }

    /**
     * 保存文本各个操作状态
     */
    @Override
    public Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        JecSaveState mJecSaveState = new JecSaveState(superState);
        mJecSaveState.mUndoParcelState = mUndoParcel;
        mJecSaveState.mRedoParcelState = mRedoParcel;
        return mJecSaveState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        // Log.v("EditText", String.valueOf(state instanceof JecSaveState));
        if(!(state instanceof JecSaveState))
        {
            super.onRestoreInstanceState(state);
            return;
        }
        JecSaveState mJecSaveState = (JecSaveState) state;
        super.onRestoreInstanceState(mJecSaveState.getSuperState());
        mUndoParcel = mJecSaveState.mUndoParcelState;
        mRedoParcel = mJecSaveState.mRedoParcelState;
        setUndoRedoButtonStatus();
    }

    public void init()
    {
        mCRC32 = new CRC32();
        mHighlight = new Highlight();
        mWorkPaint = new TextPaint();
        mTextPaint = getPaint(); // new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mLineNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mWhiteSpacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 横屏的时候关闭完成按钮和编辑状态不使用系统的全屏编辑框
        // IME_FLAG_NO_EXTRACT_UI: Flag of imeOptions: used to specify that the
        // IME does not need to show its extracted text
        // UI. For input methods that may be fullscreen, often when in landscape
        // mode, this allows them to be smaller and let
        // part of the application be shown behind. Though there will likely be
        // limited access to the application available
        // from the user, it can make the experience of a (mostly) fullscreen
        // IME less jarring. Note that when this flag is
        // specified the IME may not be set up to be able to display text, so it
        // should only be used in situations where this
        // is not needed.
        // IME_ACTION_DONE: Bits of IME_MASK_ACTION: the action key performs a
        // "done" operation, typically meaning the IME will be closed.
        setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // 设置填充
        paddingLeft = getPaddingLeft();
        mFastScroller = new FastScroller(getContext(), this);
        addTextChangedListener(mUndoWatcher);
        clearFocus();

        mTextSize = mTextPaint.getTextSize();

        mLineNumberPaint.setTextSize(mTextSize - 2);
        mLineNumberPaint.setTypeface(Typeface.MONOSPACE);
        mLineNumberPaint.setStrokeWidth(1);
        mLineNumberPaint.setColor(Color.parseColor(ColorScheme.color_font));

        mWhiteSpacePaint.setStrokeWidth(0.75F);
        // mWhiteSpacePaint.setTextSize(textSize);
        // mWhiteSpacePaint.setTypeface(mTextPaint.getTypeface());
        mWhiteSpacePaint.setStyle(Paint.Style.STROKE);
        mWhiteSpacePaint.setColor(Color.GRAY);

        float textHeight;

        // 绘制换行符
        mLineBreakPath.reset();
        float width = mTextPaint.measureText("L");
        // descent为根据当前字体及其大小的基线到下面的距离(正数),ascent则相反
        float mDescent = mTextPaint.descent();
        float mAscent = mTextPaint.ascent();
        textHeight = mDescent - mAscent;
        /**
         * lineTo在没有moveTo的情况下,默认坐标是0,0；但是要注意,这个坐标是在 "正" 默认坐标是在正字左下角
         */
        // 移到底部中央
        mLineBreakPath.moveTo(width * 0.6F, 0);
        // 竖线
        mLineBreakPath.lineTo(width * 0.6F, -textHeight * 0.7F);
        // 左箭头
        mLineBreakPath.moveTo(width * 0.6F, 0);
        mLineBreakPath.lineTo(width * 0.25F, -textHeight * 0.3F);
        // 右箭头
        mLineBreakPath.moveTo(width * 0.6F, 0);
        mLineBreakPath.lineTo(width * 0.95F, -textHeight * 0.3F);

        // 绘制制表符
        mTabPath.reset();
        width = mTextPaint.measureText("\t\t"); // 制表符4个空格
        textHeight = mTextPaint.descent() - mTextPaint.ascent();
        // 绘制 >> 符号
        mTabPath.moveTo(0, -textHeight * 0.5F);
        // 绘制箭头下面那部分
        mTabPath.lineTo(width * 0.1F, -textHeight * 0.35F);
        // 绘制箭头上面部分
        mTabPath.lineTo(0, -textHeight * 0.2F);
        // two >
        mTabPath.moveTo(width * 0.15F, -textHeight * 0.5F);
        // 绘制箭头下面那部分
        mTabPath.lineTo(width * 0.25F, -textHeight * 0.35F);
        // 绘制箭头上面部分
        mTabPath.lineTo(width * 0.15F, -textHeight * 0.2F);
        // 判断设备是否支持多点触摸
        PackageManager pm = getContext().getPackageManager();
        mSupportMultiTouch = pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
    }

    private OnTextChangedListener mOnTextChangedListener = null;

    public interface OnTextChangedListener
    {
        void onTextChanged(JecEditText mEditText);
    }

    public void setOnTextChangedListener(OnTextChangedListener l)
    {
        mOnTextChangedListener = l;
    }

    private TextWatcher mUndoWatcher = new TextWatcher() {
        TextChange lastChange;

        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            // Log.v(TAG, "isLoading:" + JecEditor.isLoading);
            if(JecEditor.isLoading)
                return;
            mHighlight.redraw();
            // 撤销，重做
            if(lastChange != null)
            {
                if(count < UndoParcel.MAX_SIZE)
                {
                    lastChange.newtext = s.subSequence(start, start + count);
                    if(start == lastChange.start && (lastChange.oldtext.length() > 0 || lastChange.newtext.length() > 0)
                            && !equalsCharSequence(lastChange.newtext, lastChange.oldtext))
                    {
                        mUndoParcel.push(lastChange);
                        mRedoParcel.removeAll();
                    }
                    //注意此操作会引起符号栏
                    setUndoRedoButtonStatus();
                }else
                {
                    mUndoParcel.removeAll();
                    mRedoParcel.removeAll();
                }
                lastChange = null;
            }
            // 记住最后修改位置
            int bufSize = mLastEditBuffer.size();
            int lastLoc = 0;
            if(bufSize != 0)
            {
                lastLoc = mLastEditBuffer.get(bufSize - 1);
            }
            // 不在附近位置才记住它，不做是否同一行判断，性能问题
            if(Math.abs(start - lastLoc) > LAST_EDIT_DISTANCE_LIMIT)
            {
                mLastEditBuffer.add(start);
                mLastEditIndex = mLastEditBuffer.size() - 1;
                if(mOnTextChangedListener != null)
                    mOnTextChangedListener.onTextChanged(JecEditText.this);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            // Log.v(TAG, "isLoading:" + JecEditor.isLoading);
            if(JecEditor.isLoading)
                return;
            if(mUndoRedo)
            {
                mUndoRedo = false;
            }else
            {
                if(count < UndoParcel.MAX_SIZE)
                {
                    lastChange = new TextChange();
                    lastChange.start = start;
                    lastChange.oldtext = s.subSequence(start, start + count);
                }else
                {
                    mUndoParcel.removeAll();
                    mRedoParcel.removeAll();
                    lastChange = null;
                }
            }
        }

        public void afterTextChanged(Editable s)
        {
        }
    };

    private boolean equalsCharSequence(CharSequence s1, CharSequence s2)
    {
        if(s1 == null || s2 == null)
        {
            return false;
        }
        if(s1.length() != s2.length())
        {
            return false;
        }
        return s1.toString().equals(s2.toString());
    }

    private void setUndoRedoButtonStatus()
    {
        if(mOnTextChangedListener != null)
            mOnTextChangedListener.onTextChanged(this);
    }

    public boolean canUndo()
    {
        return mUndoParcel.canUndo();
    }

    public boolean canRedo()
    {
        return mRedoParcel.canUndo();
    }

    public void show()
    {
        setVisibility(View.VISIBLE);
        if(mOnTextChangedListener != null)
            mOnTextChangedListener.onTextChanged(this);
    }

    public void hide()
    {
        setVisibility(View.GONE);
    }

    /**
     * 撤销
     */
    public void unDo()
    {
        TextChange textchange = mUndoParcel.pop();
        if(textchange != null)
        {
            Editable text = getText();
            mUndoRedo = true;
            text.replace(textchange.start, textchange.start + textchange.newtext.length(), textchange.oldtext);
            Selection.setSelection(text, textchange.start + textchange.oldtext.length());
            mRedoParcel.push(textchange);
            setUndoRedoButtonStatus();
        }
    }

    /**
     * 重做
     */
    public void reDo()
    {
        TextChange textchange = mRedoParcel.pop();
        if(textchange != null)
        {
            Editable text = getText();
            mUndoRedo = true;
            text.replace(textchange.start, textchange.start + textchange.oldtext.length(), textchange.newtext);
            Selection.setSelection(text, textchange.start + textchange.newtext.length());
            mUndoParcel.push(textchange);
            setUndoRedoButtonStatus();
        }
    }

    /**
     * 重置撤销，重做状态
     */
    public void resetUndoStatus()
    {
        mRedoParcel.clean();
        mUndoParcel.clean();
        setUndoRedoButtonStatus();
        mLastEditBuffer.clear();
    }

    private void setLineNumberWidth(int lastline)
    {
        mLineNumberWidth = (int) mLineNumberPaint.measureText(lastline + "|");

        mLineNumber = lastline;
        mLineNumberLength = Integer.toString(lastline).length();
        setShowLineNum(mShowLineNum);
    }

    public void setShowLineNum(boolean b)
    {
        mShowLineNum = b;

        int left;

        if(!mShowLineNum)
        {
            left = paddingLeft;
        }else
        {
            left = paddingLeft + mLineNumberWidth;
        }
        setPaddingLeft(left);
    }

    public void setShowWhitespace(boolean b)
    {
        mShowWhiteSpace = b;
    }

    public void setText2(CharSequence text)
    {
        try
        {
            super.setText(text);
        }catch (OutOfMemoryError e)
        {
            Toast.makeText(getContext(), R.string.out_of_memory, Toast.LENGTH_SHORT).show();
            // Log.d(TAG, e.getMessage());
        }
    }

    public String getString()
    {
        String text = "";
        try
        {
            text = getText().toString();
        }catch (OutOfMemoryError e)
        {
            Toast.makeText(getContext(), R.string.out_of_memory, Toast.LENGTH_SHORT).show();
        }
        return text;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        mLayout = getLayout();
        mText = (Editable) getText();

        super.onDraw(canvas);

        drawView(canvas);

        if(mFastScroller != null)
        {
            mFastScroller.draw(canvas);
        }

    }

    public boolean onTouchEvent(MotionEvent event)
    {
        if(mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        // 是否按住滚动条拖动
        if(mFastScroller != null)
        {
            boolean intercepted;
            intercepted = mFastScroller.onTouchEvent(event);
            // Log.v(TAG, "intercepted2:"+intercepted);
            if(intercepted)
            {
                return true;
            }
            intercepted = mFastScroller.onInterceptTouchEvent(event);
            // Log.v(TAG, "intercepted1:"+intercepted);
            if(intercepted)
            {
                return true;
            }
        }

        // 处理文本快速顺畅地滚动
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(TOUCH_ZOOM_ENABLED)
                {
                    mTouchMode = TOUCH_DRAG_START_MODE;
                    oldDist = calc_spacing(event);
                }
                
                if(mFlingRunnable != null)
                {
                    mFlingRunnable.endFling();
                    cancelLongPress();
                }
                break;
            case MotionEvent.ACTION_UP:
                // cancelLongPress();
                mTouchMode = TOUCH_DONE_MODE;

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
                        mHighlight.stop();
                        mFlingRunnable.start(this, -initialVelocity);
                    }catch (Exception e)
                    {
                    }
                }else
                {
                    mHighlight.redraw();
                    //moveCursorToVisibleOffset();
                }

                if(mVelocityTracker != null)
                {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if(TOUCH_ZOOM_ENABLED && mTouchMode == TOUCH_DRAG_START_MODE && mSupportMultiTouch && event.getPointerCount() >= 2)
                {
                    cancelLongPress();
                    //mTouchMode = TOUCH_DRAG_MODE;
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

                }
                break;
        }
        boolean res;
        try
        {
            res = super.onTouchEvent(event);
        }catch (Exception e)
        {
            res = true;
        }
        return res;
    }

    /**
     * 求出2个触点间的 距离
     * 
     * @param event
     * @return
     */
    private float calc_spacing(MotionEvent event)
    {
        if(event.getPointerCount() < 2)
            return 0;
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

        if(mTextSize > MAX_TEXT_SIZE)
        {
            mTextSize = MAX_TEXT_SIZE;
        }
        setTextSize(mTextSize);
        mLineNumberPaint.setTextSize(mTextSize - 2);
    }

    /**
     * 缩小
     */
    protected void zoomIn()
    {
        mTextSize -= scale;
        if(mTextSize < MIN_TEXT_SIZE)
        {
            mTextSize = MIN_TEXT_SIZE;
        }
        setTextSize(mTextSize);
        mLineNumberPaint.setTextSize(mTextSize - 2);
    }

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

        private JecEditText mWidget = null;

        FlingRunnable(Context context)
        {
            mScroller = new Scroller(context);
        }

        void start(JecEditText parent, int initialVelocity)
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
                    // Log.d(TAG, "delta:"+delta);
                    if(Math.abs(delta) <= 3)
                    {
                        mWidget.mHighlight.redraw();
                    }
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {

        if(mFastScroller != null)
        {
            mFastScroller.onSizeChanged(w, h, oldw, oldh);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        super.onScrollChanged(l, t, oldl, oldt);

        if(mFastScroller != null && mLayout != null)
        {
            int h = getVisibleHeight();
            int h2 = mLayout.getHeight();
            mFastScroller.onScroll(this, t, h, h2);
        }

    }

    public int getVisibleHeight()
    {
        int b = getBottom();
        int t = getTop();
        int pb = getExtendedPaddingBottom();
        int pt = getExtendedPaddingTop();
        return b - t - pb - pt;
    }

    /**
     * Draw this Layout on the specified canvas, with the highlight path drawn
     * between the background and the text.
     * 
     * @param c
     *            the canvas
     * @param highlight
     *            the path of the highlight or cursor; can be null
     * @param highlightPaint
     *            the paint for the highlight
     * @param cursorOffsetVertical
     *            the amount to temporarily translate the canvas while rendering
     *            the highlight
     */
    public void drawView(Canvas c)
    {
        int dtop, dbottom;

        synchronized (sTempRect)
        {
            if(!c.getClipBounds(sTempRect))
            {
                return;
            }

            dtop = sTempRect.top;
            dbottom = sTempRect.bottom;
        }
        if(mLayout == null)
            return;

        int textLength = mText.length();

        int top = 0;
        int lineCount = mLayout.getLineCount();
        int bottom = mLayout.getLineTop(lineCount);

        if(dtop > top)
        {
            top = dtop;
        }
        if(dbottom < bottom)
        {
            bottom = dbottom;
        }

        int first = mLayout.getLineForVertical(top);
        int last = mLayout.getLineForVertical(bottom);

        int previousLineBottom = mLayout.getLineTop(first);
        int previousLineEnd = mLayout.getLineStart(first);

        TextPaint paint = mTextPaint;

        ParagraphStyle[] spans = NO_PARA_SPANS;

        // Log.d("Highlight", first+"-"+last+"="+dtop+":"+dbottom);
        // 这里不要使用getScrollY，因为修改时，光标会变，滚动条不会变，但是高亮需要变
        int previousLineEnd2 = mLayout.getLineStart(first >= 3 ? first - 3 : 0);
        mHighlight.render(mText, previousLineEnd2, mLayout.getLineStart(last + 3 > lineCount ? lineCount : last + 3));

        if(!mShowLineNum && !mShowWhiteSpace)
        {
            return;
        }

        // 显示行数
        int lastline = lineCount < 1 ? 1 : lineCount;
        if(lastline != mLineNumber)
        {
            setLineNumberWidth(lastline);
        }
        // 设置显示行号的位置
        if(mNoWrapMode)
        {
            mLineNumX = mLineNumberWidth + getScrollX();
        }else
        {
            mLineNumX = mLineNumberWidth;
        }

        int right = getWidth();
        int left = getPaddingLeft();
        // 真实行数
        if(previousLineEnd > 1)
        {
            if(previousLineEnd >= mText.length())
                return;
            realLineNum = TextUtil.countMatches(mText, '\n', 0, previousLineEnd);
            // Log.v("edittext",
            // "curVisibleLineEnd:"+curVisibleLineEnd+" realLineNum:"+realLineNum);
            // 如果当前行是新行，则需要+1
            if(mText.charAt(previousLineEnd) != '\n')
            {
                realLineNum++;
            }
        }else
        {
            realLineNum = 1;
        }
        // Log.v("tag", "f:"+first+" l:"+last);
        hasNewline = true;

        // 为了空白时也默认有一行
        if(last == 0)
        {
            c.drawLine(mLineNumX, top, mLineNumX, mTextPaint.getTextSize(), mLineNumberPaint);
            if(hasNewline)
            {
                String lineString = mLineStr.get(realLineNum);
                if(lineString == null)
                {
                    lineString = "      " + realLineNum;
                    mLineStr.put(realLineNum, lineString);
                }
                c.drawText(lineString, lineString.length() - mLineNumberLength, lineString.length(), mLineNumX - mLineNumberWidth, mTextPaint.getTextSize(), mLineNumberPaint);
            }
            return;
        }

        // Next draw the lines, one at a time.
        // the baseline is the top of the following line minus the current
        // line's descent.
        for (int i = first; i <= last; i++)
        {
            int start = previousLineEnd;

            previousLineEnd = mLayout.getLineStart(i + 1);
            int end = getLineVisibleEnd(i, start, previousLineEnd);

            int ltop = previousLineBottom;
            int lbottom = mLayout.getLineTop(i + 1);
            previousLineBottom = lbottom;
            int lbaseline = lbottom;// - mLayout.getLineDescent(i);

            int dir = mLayout.getParagraphDirection(i);

            // Adjust the point at which to start rendering depending on the
            // alignment of the paragraph.
            int x;
            if(dir == DIR_LEFT_TO_RIGHT)
            {
                x = left;
            }else
            {
                x = right;
            }

            // jecelyin: 默认左到右，肯定不会有右到左出现
            // Directions directions = getLineDirections(i);
            Directions directions = DIRS_ALL_LEFT_TO_RIGHT;
            // android.text.Layout.Directions directions =
            // mLayout.getLineDirections(i);
            boolean hasTab = mLayout.getLineContainsTab(i);
            drawText(c, start, end, dir, directions, x, ltop, lbaseline, lbottom, paint, mWorkPaint, hasTab, spans, textLength, i + 1 == last);

        }
    }

    private void drawText(Canvas canvas, int start, int end, int dir, Directions directions, final float x, int top, int y, int bottom, TextPaint paint, TextPaint workPaint,
            boolean hasTabs, Object[] parspans, int textLength, boolean islastline)
    {
        // linenum
        if(mShowLineNum)
        {
            // 竖线
            // drawLine (float startX, float startY, float stopX, float stopY,
            // Paint paint)
            canvas.drawLine(mLineNumX, top, mLineNumX, islastline ? bottom + (bottom - top) : bottom, mLineNumberPaint);
            if(hasNewline)
            {
                String lineString = mLineStr.get(realLineNum);
                if(lineString == null)
                {
                    lineString = "      " + realLineNum;
                    mLineStr.put(realLineNum, lineString);
                }
                canvas.drawText(lineString, lineString.length() - mLineNumberLength, lineString.length(), mLineNumX - mLineNumberWidth + 1, y - 2, mLineNumberPaint);
                realLineNum++;
                hasNewline = false;
            }
        }

        float h = 0;
        int here = 0;
        for (int i = 0; i < directions.mDirections.length; i++)
        // for (int i = 0; i < 1; i++)
        {
            int there = here + directions.mDirections[i];
            if(there > end - start)
                there = end - start;

            int segstart = here;
            for (int j = hasTabs ? here : there; j <= there; j++)
            {
                if(start + j > end)
                    break;
                char at = start + j == end ? 0 : mText.charAt(start + j);
                if(j == there || at == '\t')
                {

                    h += Styled.drawText(null, mText, start + segstart, start + j, dir, (i & 1) != 0, x + h, top, y, bottom, paint, workPaint, (start + j == end) || hasTabs);

                    if(j != there && at == '\t' && mShowWhiteSpace)
                    {
                        if(x + h > mLineNumX)
                        {
                            canvas.translate(x + h, y);
                            canvas.drawPath(mWhiteSpacePaths[0], mWhiteSpacePaint);
                            canvas.translate(-x - h, -y);
                        }
                        h = dir * nextTabPos(mText, start, end, h * dir, parspans);
                    }else if(j == there)
                    {
                        if(end < textLength && mText.charAt(end) == '\n')
                        {
                            if(mShowWhiteSpace && x + h > mLineNumX)
                            {
                                canvas.translate(x + h, y);
                                canvas.drawPath(mWhiteSpacePaths[1], mWhiteSpacePaint);
                                canvas.translate(-x - h, -y);
                            }

                            hasNewline = true;
                            break;
                        }
                    }

                    segstart = j + 1;
                }
            }// end for
            here = there;
        }

    }

    /**
     * Returns the position of the next tab stop after h on the line.
     * 
     * @param text
     *            the text
     * @param start
     *            start of the line
     * @param end
     *            limit of the line
     * @param h
     *            the current horizontal offset
     * @param tabs
     *            the tabs, can be null. If it is null, any tabs in effect on
     *            the line will be used. If there are no tabs, a default offset
     *            will be used to compute the tab stop.
     * @return the offset of the next tab stop.
     */
    /* package */static float nextTabPos(CharSequence text, int start, int end, float h, Object[] tabs)
    {
        float nh = Float.MAX_VALUE;
        boolean alltabs = false;

        if(text instanceof Spanned)
        {
            if(tabs == null)
            {
                tabs = ((Spanned) text).getSpans(start, end, TabStopSpan.class);
                alltabs = true;
            }

            for (int i = 0; i < tabs.length; i++)
            {
                if(!alltabs)
                {
                    if(!(tabs[i] instanceof TabStopSpan))
                        continue;
                }

                int where = ((TabStopSpan) tabs[i]).getTabStop();

                if(where < nh && where > h)
                    nh = where;
            }

            if(nh != Float.MAX_VALUE)
                return nh;
        }

        return ((int) ((h + TAB_INCREMENT) / TAB_INCREMENT)) * TAB_INCREMENT;
    }

    /**
     * Stores information about bidirectional (left-to-right or right-to-left)
     * text within the layout of a line. TODO: This work is not complete or
     * correct and will be fleshed out in a later revision.
     */
    public static class Directions
    {
        private short[] mDirections;

        // The values in mDirections are the offsets from the first character
        // in the line to the next flip in direction. Runs at even indices
        // are left-to-right, the others are right-to-left. So, for example,
        // a line that starts with a right-to-left run has 0 at mDirections[0],
        // since the 'first' (ltr) run is zero length.
        //
        // The code currently assumes that each run is adjacent to the previous
        // one, progressing in the base line direction. This isn't sufficient
        // to handle nested runs, for example numeric text in an rtl context
        // in an ltr paragraph.
        /* package */Directions(short[] dirs)
        {
            mDirections = dirs;
        }
    }

    private static final ParagraphStyle[] NO_PARA_SPANS = new ParagraphStyle[]{};

    /* package */static final Directions DIRS_ALL_LEFT_TO_RIGHT = new Directions(new short[]{ 32767 });
    /* package */static final Directions DIRS_ALL_RIGHT_TO_LEFT = new Directions(new short[]{ 0, 32767 });
    public static final int DIR_LEFT_TO_RIGHT = 1;
    public static final int DIR_RIGHT_TO_LEFT = -1;

    /**
     * Return the text offset after the last visible character (so whitespace is
     * not counted) on the specified line.
     */
    public int getLineVisibleEnd(int line)
    {
        return getLineVisibleEnd(line, mLayout.getLineStart(line), mLayout.getLineStart(line + 1));
    }

    private int getLineVisibleEnd(int line, int start, int end)
    {

        CharSequence text = getText();
        char ch;
        if(line == getLineCount() - 1)
        {
            return end;
        }
        // fix IndexOutOfBoundsException SpannableStringBuilder.charAt()

        if(end < 1)
            return 0;

        for (; end > start; end--)
        {
            try
            {
                ch = text.charAt(end - 1);
            }catch (Exception e)
            {
                return end;
            }

            if(ch == '\n')
            {
                return end - 1;
            }

            if(ch != ' ' && ch != '\t')
            {
                break;
            }

        }

        return end;
    }

    public boolean gotoLine(int line)
    {
        if(line < 1)
            return false;
        int count = 0;
        int strlen = mText.length();
        for (int index = 0; index < strlen; index++)
        {
            if(mText.charAt(index) == '\n')
            {
                count++;
                if(count == line)
                {
                    Selection.setSelection((Spannable) mText, index, index);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean gotoBackEditLocation()
    {
        if(mLastEditIndex < 1)
            return false;
        mLastEditIndex--;
        int offset = mLastEditBuffer.get(mLastEditIndex);
        setSelection(offset, offset);
        return true;
    }

    public boolean gotoForwardEditLocation()
    {
        if(mLastEditIndex >= mLastEditBuffer.size())
            return false;
        mLastEditIndex++;
        int offset = mLastEditBuffer.get(mLastEditIndex);
        setSelection(offset, offset);
        return true;
    }

    public boolean isCanBackEditLocation()
    {
        if(mLastEditIndex < 1)
            return false;
        return mLastEditIndex < mLastEditBuffer.size();
    }

    public boolean isCanForwardEditLocation()
    {
        if(mLastEditIndex >= mLastEditBuffer.size() - 1)
            return false;
        // return mLastEditIndex < mLastEditBuffer.size();
        return true;
    }

    public void setAutoIndent(boolean open)
    {
        mAutoIndent = open;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean result = super.onKeyDown(keyCode, event);
        // 自动缩进
        if(mAutoIndent && keyCode == KeyEvent.KEYCODE_ENTER)
        {
            Editable mEditable = (Editable) mText;
            if(mEditable == null)
                return result;

            int start = getSelectionStart();
            int end = getSelectionEnd();
            if(start == end)
            {
                int prev = start - 2;
                while (prev >= 0 && mEditable.charAt(prev) != '\n')
                {
                    prev--;
                }
                prev++;
                int pos = prev;
                while (mEditable.charAt(pos) == ' ' || mEditable.charAt(pos) == '\t' || mEditable.charAt(pos) == '\u3000')
                {
                    pos++;
                }
                int len = pos - prev;
                if(len > 0)
                {
                    try
                    {
                        char[] dest = new char[len];
                        mEditable.getChars(prev, pos, dest, 0);
                        mEditable.replace(start, end, new String(dest));
                        setSelection(start + len);
                    }catch (Exception e)
                    {

                    }

                }
            }
        }
        return result;
    }

    public void setEncoding(String encoding)
    {
        current_encoding = encoding;
    }

    public void setPath(String path)
    {
        if("".equals(path))
            return;
        current_path = path;

        File f = new File(current_path);
        long fsize = f.length() / 1024;
        if(fsize > Highlight.getLimitFileSize())
        {
            Toast.makeText(getContext(), getResources().getString(R.string.highlight_stop_msg), Toast.LENGTH_LONG).show();
            return;
        }
        setCurrentFileExt(FileUtil.getExt(path));
    }
    
    public void setTitle(String title)
    {
        current_title = title;
    }

    public void setCurrentFileExt(String ext)
    {
        current_ext = ext;

        mHighlight.redraw();
        mHighlight.setSyntaxType(current_ext);
    }

    public String getCurrentFileExt()
    {
        return current_ext;
    }

    public String getEncoding()
    {
        return current_encoding;
    }

    public String getPath()
    {
        return current_path;
    }
    
    public String getTitle()
    {
        return current_title;
    }

    public void setTextFinger()
    {
        src_text_length = getText().length();
        byte bytes[] = getString().getBytes();
        mCRC32.reset();
        mCRC32.update(bytes,0,bytes.length);
        src_text_crc32 = mCRC32.getValue();
    }

    public boolean isTextChanged()
    {
        CharSequence text = getText();
        int hash = text.length();
        //长度不相等，肯定是有更改了
        if(src_text_length != hash)
        {
            return true;
        }
        //进行CRC检验
        mCRC32.reset();
        byte bytes[] = getString().getBytes();
        mCRC32.update(bytes,0,bytes.length);
        return src_text_crc32 != mCRC32.getValue();
    }

    public void setHorizontallyScrolling(boolean whether)
    {
        mNoWrapMode = whether;
        super.setHorizontallyScrolling(whether);
    }

    public void setPaddingLeft(int padding)
    {
        if(lastPaddingLeft == padding)
            return;
        if(padding < paddingLeft)
            padding = paddingLeft;
        lastPaddingLeft = padding;
        setPadding(padding, 0, getPaddingRight(), getPaddingBottom());
    }

    public void setLineBreak(int linebreak)
    {
        current_linebreak = linebreak;
    }

    public int getLineBreak()
    {
        return current_linebreak;
    }

    
    
    public void showIME(boolean show)
    {
        JecEditText.setHideKeyboard(!show);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if(getResources().getConfiguration().hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES)
        {
            show = false;
        }
        if(show)
        { // 显示键盘，即输入法
            int type = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            if ( mAutoCapitalize ){
                type |= InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
                //setSingleLine(false);
            }
            if( mDisableSpellCheck )
            {
                type |= ~InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
            }
            setInputType(type);
            if(imm != null)
            {
                imm.showSoftInput(this, 0);
            }
        }else
        { // 隐藏键盘
            setRawInputType(0);
            if(imm != null)
            {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }
    
    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_A:
            if (canSelectAll()) {
                return onTextContextMenuItem(ID_SELECT_ALL);
            }

            break;

        case KeyEvent.KEYCODE_X:
            if (canCut()) {
                return onTextContextMenuItem(ID_CUT);
            }

            break;

        case KeyEvent.KEYCODE_C:
            if (canCopy()) {
                return onTextContextMenuItem(ID_COPY);
            }

            break;

        case KeyEvent.KEYCODE_V:
            if (canPaste()) {
                return onTextContextMenuItem(ID_PASTE);
            }

            break;
        }

        return super.onKeyShortcut(keyCode, event);
    }
    
    private boolean canSelectAll() {
        if (mText instanceof Spannable && mText.length() != 0 &&
                getMovementMethod() != null && getMovementMethod().canSelectArbitrarily()) {
            return true;
        }

        return false;
    }

    private boolean canSelectText() {
        if (mText instanceof Spannable && mText.length() != 0 &&
                getMovementMethod() != null && getMovementMethod().canSelectArbitrarily()) {
            return true;
        }

        return false;
    }

    private boolean canCut() {
        if (getTransformationMethod() instanceof PasswordTransformationMethod) {
            return false;
        }

        if (mText.length() > 0 && getSelectionStart() >= 0) {
            if (mText instanceof Editable && getKeyListener() != null) {
                return true;
            }
        }

        return false;
    }

    private boolean canCopy() {
        if (getTransformationMethod() instanceof PasswordTransformationMethod) {
            return false;
        }

        if (mText.length() > 0 && getSelectionStart() >= 0) {
            return true;
        }

        return false;
    }

    private boolean canPaste() {
        if (mText instanceof Editable && getKeyListener() != null &&
            getSelectionStart() >= 0 && getSelectionEnd() >= 0) {
            ClipboardManager clip = (ClipboardManager)getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            if (clip.hasText()) {
                return true;
            }
        }

        return false;
    }
    
    public static void setUseSystemMenu(boolean b)
    {
        USE_SYSTEM_MENU = b;
    }
    
    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        if(USE_SYSTEM_MENU)
            super.onCreateContextMenu(menu);

        if (!isFocused()) {
            if (isFocusable() && getKeyListener() != null && !USE_SYSTEM_MENU) {
                if (canCopy()) {
                    MenuHandler handler = new MenuHandler();
                    int name = R.string.copyAll;

                    menu.add(0, ID_COPY, 0, name).
                        setOnMenuItemClickListener(handler).
                        setAlphabeticShortcut('c');
                    menu.setHeaderTitle(R.string.editTextMenuTitle);
                }
            }

            return;
        }

        MenuHandler handler = new MenuHandler();

        boolean selection = getSelectionStart() != getSelectionEnd();
        if(!USE_SYSTEM_MENU)
        {
            if (canSelectAll()) {
                menu.add(0, ID_SELECT_ALL, 0,
                        R.string.selectAll).
                    setOnMenuItemClickListener(handler).
                    setAlphabeticShortcut('a');
            }

            if (canSelectText()) {
                if (MetaKeyKeyListener.getMetaState(mText, META_SELECTING) != 0) {
                    menu.add(0, ID_STOP_SELECTING_TEXT, 0,
                            R.string.stopSelectingText).
                        setOnMenuItemClickListener(handler);
                } else {
                    menu.add(0, ID_START_SELECTING_TEXT, 0,
                            R.string.selectText).
                        setOnMenuItemClickListener(handler);
                }
            }
    
            if (canCut()) {
                int name;
                if (selection) {
                    name = R.string.cut;
                } else {
                    name = R.string.cutAll;
                }
    
                menu.add(0, ID_CUT, 0, name).
                    setOnMenuItemClickListener(handler).
                    setAlphabeticShortcut('x');
            }
    
            if (canCopy()) {
                int name;
                if (selection) {
                    name = R.string.copy;
                } else {
                    name = R.string.copyAll;
                }
    
                menu.add(0, ID_COPY, 0, name).
                    setOnMenuItemClickListener(handler).
                    setAlphabeticShortcut('c');
            }
    
            if (canPaste()) {
                menu.add(0, ID_PASTE, 0, R.string.paste).
                        setOnMenuItemClickListener(handler).
                        setAlphabeticShortcut('v');
            }
    
            if (mText instanceof Spanned) {
                int selStart = getSelectionStart();
                int selEnd = getSelectionEnd();
    
                int min = Math.min(selStart, selEnd);
                int max = Math.max(selStart, selEnd);
    
                URLSpan[] urls = ((Spanned) mText).getSpans(min, max,
                                                            URLSpan.class);
                if (urls.length == 1) {
                    menu.add(0, ID_COPY_URL, 0,
                             R.string.copyUrl).
                                setOnMenuItemClickListener(handler);
                }
            }
        }
        
        // 重复行或选中的文本
        int menu_line = selection ? R.string.duplicate_selected_text : R.string.duplicate_line;
        menu.add(0, R.id.duplicate_line, 0, menu_line).setOnMenuItemClickListener(handler);
        // 转为小写
        menu.add(0, R.id.to_lower, 0, R.string.to_lower).setOnMenuItemClickListener(handler);
        // 转为大写
        menu.add(0, R.id.to_upper, 0, R.string.to_upper).setOnMenuItemClickListener(handler);
        // 跳转到指定行
        menu.add(0, R.id.go_to_begin, 0, R.string.go_to_begin).setOnMenuItemClickListener(handler);
        // 跳转到指定行
        menu.add(0, R.id.go_to_end, 0, R.string.go_to_end).setOnMenuItemClickListener(handler);
        // 跳转到指定行
        menu.add(0, R.id.goto_line, 0, R.string.goto_line).setOnMenuItemClickListener(handler);
        
        // 插入时间
        String date = TimeUtil.getDateByFormat(mDateFormat);
        menu.add(0, R.id.insert_datetime, 0, getResources().getString(R.string.insert_datetime)+date).setOnMenuItemClickListener(handler);

        if(mHideSoftKeyboard)
        {
            // 显示输入法
            menu.add(0, R.id.show_ime, 0, R.string.show_ime).setOnMenuItemClickListener(handler);
        } else {
            // 隐藏输入法
            menu.add(0, R.id.hide_ime, 0, R.string.hide_ime).setOnMenuItemClickListener(handler);
        }
        //文档统计
        menu.add(0, R.id.doc_stat, 0, R.string.doc_stat).setOnMenuItemClickListener(handler);
        
        menu.setHeaderTitle(R.string.editTextMenuTitle);
        
    }
    
    /**
     * Called when a context menu option for the text view is selected.  Currently
     * this will be one of: {@link android.R.id#selectAll},
     * {@link android.R.id#startSelectingText}, {@link android.R.id#stopSelectingText},
     * {@link android.R.id#cut}, {@link android.R.id#copy},
     * {@link android.R.id#paste}, {@link android.R.id#copyUrl},
     * or {@link android.R.id#switchInputMethod}.
     */
    public boolean onTextContextMenuItem(int id) {
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();

        if (!isFocused()) {
            selStart = 0;
            selEnd = mText != null ? mText.length() : 0;
        }

        int min = Math.min(selStart, selEnd);
        int max = Math.max(selStart, selEnd);

        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }

        ClipboardManager clip = (ClipboardManager)getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);

        switch (id) {
            case ID_SELECT_ALL:
                Selection.setSelection((Spannable) mText, 0,
                        mText.length());
                return true;

            /*case ID_START_SELECTING_TEXT:
                MetaKeyKeyListener.startSelecting(this, (Spannable) mText, selStart);
                return true;

            case ID_STOP_SELECTING_TEXT:
                MetaKeyKeyListener.stopSelecting(this, (Spannable) mText);
                Selection.setSelection((Spannable) mText, selEnd);
                return true;*/

            case ID_CUT:
                //MetaKeyKeyListener.stopSelecting(this, (Spannable) mText);
                if(!super.onTextContextMenuItem(id))
                {
                    if (min == max) {
                        min = 0;
                        max = mText.length();
                    }
    
                    clip.setText(mText.subSequence(min, max));
                    ((Editable) mText).delete(min, max);
                }
                return true;

            case ID_COPY:
                //MetaKeyKeyListener.stopSelecting(this, (Spannable) mText);
                if(!super.onTextContextMenuItem(id))
                {
                    if (min == max) {
                        min = 0;
                        max = mText.length();
                    }
    
                    clip.setText(mText.subSequence(min, max));
                    }
                return true;

            case ID_PASTE:
                //MetaKeyKeyListener.stopSelecting(this, (Spannable) mText);
                if(!super.onTextContextMenuItem(id))
                {
                    CharSequence paste = clip.getText();
    
                    if (paste != null) {
                        Selection.setSelection((Spannable) mText, max);
                        ((Editable) mText).replace(min, max, paste);
                    }
                }
                return true;

            case ID_COPY_URL:
                //MetaKeyKeyListener.stopSelecting(this, (Spannable) mText);
                if(!super.onTextContextMenuItem(id))
                {
                    URLSpan[] urls = ((Spanned) mText).getSpans(min, max,
                                                           URLSpan.class);
                    if (urls.length == 1) {
                        clip.setText(urls[0].getURL());
                    }
                }
                return true;
            
            case R.id.show_ime:
                showIME(true);
                break;
            case R.id.hide_ime:
                showIME(false);
                break;
            case R.id.to_lower:
            case R.id.to_upper:
                int start = getSelectionStart();
                int end = getSelectionEnd();
                if(start == end)
                    break;
                try
                {
                    Editable mText2 = getText();
                    char[] dest = new char[end - start];
                    mText2.getChars(start, end, dest, 0);
                    if(id == R.id.to_lower)
                    {
                        mText2.replace(start, end, (new String(dest)).toLowerCase());
                    }else
                    {
                        mText2.replace(start, end, (new String(dest)).toUpperCase());
                    }
                }catch (Exception e)
                {
                    //printException(e);
                }
                break;
            case R.id.goto_line:
                final EditText lineEditText = new EditText(getContext());
                lineEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.goto_line).setView(lineEditText).setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            CharSequence lineCharSequence = lineEditText.getText();
                            int line = Integer.valueOf(lineCharSequence.toString());
                            if(!gotoLine(line))
                            {
                                Toast.makeText(getContext(), R.string.can_not_gotoline, Toast.LENGTH_LONG).show();
                            }else
                            {
                                dialog.dismiss();
                            }
                        }catch (Exception e)
                        {
                            //printException(e);
                        }
                    }
                });
                builder.show();
            case R.id.go_to_begin:
                setSelection(0, 0);
                break;
            case R.id.go_to_end:
                int len = getText().length();
                setSelection(len, len);
                break;
            case R.id.insert_datetime:
                String text = TimeUtil.getDateByFormat(mDateFormat);
                getText().replace(min, max, text, 0, text.length());
                break;
            case R.id.duplicate_line:
                CharSequence text2;
                int offset;
                if(selStart == selEnd)
                {//重复行
                    int s=selStart,e=selEnd;
                    for(;--s>=0;)
                    {
                        if(mText.charAt(s) == '\n')
                        {
                            break;
                        }
                    }
                    int textlen = mText.length();
                    for(;e++<textlen;)
                    {
                        if(mText.charAt(e) == '\n')
                        {
                            break;
                        }
                    }
                    if(s<0)s=0;
                    if(e>=textlen)e=textlen-1;
                    text2=mText.subSequence(s, e);
                    offset=e;
                } else {
                    //重复选中的文本
                    text2=mText.subSequence(min, max);
                    offset=max;
                }
                getText().replace(offset, offset, text2, 0, text2.length());
                break;
            
            case R.id.doc_stat:
                Context context = getContext();
                StringBuilder sb = new StringBuilder();
                //match word
                Matcher m = Pattern.compile( "\\w+" ).matcher( mText );
                int i=0;
                while ( m.find() ){
                    i++;
                }
                
                sb.append(context.getString(R.string.filename))
                .append("\t\t").append(getPath()).append("\n\n")
                //总长度
                .append(context.getString(R.string.total_chars))
                .append("\t\t").append(mText.length()).append("\n")
                //总单词数
                .append(context.getString(R.string.total_words))
                .append("\t\t").append(i).append("\n")
                //总行数
                .append(context.getString(R.string.total_lines))
                .append("\t\t").append(TextUtil.countMatches(mText, '\n', 0, mText.length()-1)+1);
                

                
                new AlertDialog.Builder(context)
                .setTitle(R.string.doc_stat)
                .setMessage(sb.toString()).setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        }).show();
                sb = null;
            }
        //调用TextView的选择文本功能
        return super.onTextContextMenuItem(id);
    }
    
    private static final int META_SELECTING = 1 << 16;
    private static final int ID_SELECT_ALL = android.R.id.selectAll;
    private static final int ID_START_SELECTING_TEXT = android.R.id.startSelectingText;
    private static final int ID_STOP_SELECTING_TEXT = android.R.id.stopSelectingText;
    private static final int ID_CUT = android.R.id.cut;
    private static final int ID_COPY = android.R.id.copy;
    private static final int ID_PASTE = android.R.id.paste;
    private static final int ID_COPY_URL = android.R.id.copyUrl;

    private class MenuHandler implements MenuItem.OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            return onTextContextMenuItem(item.getItemId());
        }
    }
    
    public static void setHideKeyboard(boolean bool)
    {
        mHideSoftKeyboard = bool;
    }
    
    public void setAutoCapitalize( boolean cap )
    {
        mAutoCapitalize = cap;
        if(mAutoCapitalize)
            setInputType(getInputType() | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }
    
    public void setDateFormat(String format)
    {
        mDateFormat = format;
    }
    
    public static void setDisableSpellCheck(boolean b)
    {
        mDisableSpellCheck = b;
    }

}
