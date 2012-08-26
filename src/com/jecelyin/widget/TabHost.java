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

package com.jecelyin.widget;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.jecelyin.colorschemes.ColorScheme;
import com.jecelyin.editor.JecEditor;
import com.jecelyin.editor.Options;
import com.jecelyin.editor.R;
import com.jecelyin.highlight.Highlight;
import com.jecelyin.widget.JecEditText.OnTextChangedListener;
import com.jecelyin.widget.TabWidget.OnMenuClickListener;

/**
 * Container for a tabbed window view. This object holds two children: a set of
 * tab labels that the user clicks to select a specific tab, and a FrameLayout
 * object that displays the contents of that page. The individual elements are
 * typically controlled using this container object, rather than setting values
 * on the child elements themselves.
 * 
 * <p>
 * See the <a href="{@docRoot}
 * resources/tutorials/views/hello-tabwidget.html">Tab Layout tutorial</a>.
 * </p>
 */
public class TabHost extends LinearLayout
{

    private TabWidget mTabWidget;
    private LinearLayout mTabContent;
    private JecEditor mJecEditor;
    private ArrayList<JecEditText> mTabSpecs = new ArrayList<JecEditText>();

    /**
     * This field should be made private, so it is hidden from the SDK. {@hide
     * }
     */
    protected int mCurrentTab = -1;
    private JecEditText mCurrentEditText = null;
    /**
     * This field should be made private, so it is hidden from the SDK. {@hide
     * }
     */
    protected LocalActivityManager mLocalActivityManager = null;
    private OnTabChangeListener mOnTabChangeListener;
    private HorizontalScrollView mScroller;
    private OnTabCloseListener mOnTabCloseListener;
    
    private OnTextChangedListener mOnTextChangedListener = null;
    //是否自动创建tab
    public static boolean autoNewTab = true;

    public void setOnTextChangedListener(OnTextChangedListener l)
    {
        mOnTextChangedListener = l;
    }

    public TabHost(Context context)
    {
        super(context);
        // initTabHost();
    }

    public TabHost(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // initTabHost();
        
    }

    public void initTabHost(JecEditor parent)
    {
        setFocusableInTouchMode(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        mJecEditor = parent;
        mTabContent = (LinearLayout) mJecEditor.findViewById(R.id.editorBodyLinearLayout);
        
        mCurrentTab = -1;
        mCurrentEditText = null;
        mTabWidget = (TabWidget) this.findViewById(R.id.tabWidgets);
        mTabWidget.setOnMenuClickListener(mOnMenuClickListener);
        mScroller = (HorizontalScrollView) this.findViewById(R.id.tabScroller);
        ImageButton mNewTabButton = (ImageButton) this.findViewById(R.id.add_new_tab_btn);
        mNewTabButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                addTab("");
            }
        });
    }
    
    private TextView getTabTitleView()
    {
        if(mCurrentTab < 0)
            return null;
        View mView = mTabWidget.getChildTabViewAt(mCurrentTab);
        if(mView == null)
            return null;
        return (TextView)mView.findViewById(R.id.title);
    }

    public void setTitle(String title)
    {
        TextView mTitleView = getTabTitleView();
        if(mTitleView == null)
            return;
        mTitleView.setText(title);
        mTabSpecs.get(mCurrentTab).setTitle(title);
    }

    /**
     * Add a tab.
     * 
     * @param tabSpec
     *            Specifies how to create the indicator and content.
     */
    public void addTab(String path)
    {
        if(mTabSpecs.size() > 0)
        {
            int index=0;
            for(JecEditText et : mTabSpecs)
            {
                if("".equals(et.getPath()) && et.getText().length() == 0)
                {//空白文档，没有内容时，则不打开新文件
                    setCurrentTab(index);
                    return;
                } else if(!"".equals(path) && path.equals(et.getPath())) {
                    //已经打开了
                    setCurrentTab(index);
                    return;
                }
                index++;
            }
        }
        View tabIndicator = createIndicatorView();

        mTabWidget.addView(tabIndicator);
        mTabWidget.setTabSelectionListener(new TabWidget.OnTabSelectionChanged() {
            //选中标签时的事件
            public void onTabSelectionChanged(int tabIndex)
            {
                if(tabIndex != mCurrentTab)
                {
                    setCurrentTab(tabIndex);
                    if(mOnTabChangeListener != null)
                        mOnTabChangeListener.onTabChanged(tabIndex);
                } else {
                    //关闭当前标签
                    if(mOnTabCloseListener != null)
                    {
                        mOnTabCloseListener.onTabClose(TabWidget.MENU_ACTION_CLOSE_ONE, tabIndex, tabIndex);
                    }
                }
            }
        });

        JecEditText jet = createEditText();
        //jet.setPath(path);
        setEditTextPref(jet);
        mTabSpecs.add(jet);
        mTabContent.addView(jet);
        
        setCurrentTab(mTabSpecs.size() - 1);
        setTitle(getResources().getString(R.string.new_filename));
    }
    
    public int closeTab(int tabId)
    {
        mTabWidget.removeViewAt(tabId);
        mTabContent.removeView(mTabSpecs.get(tabId));
        mTabSpecs.remove(tabId);
        
        if(tabId == 0)
        {
            if(mTabWidget.getTabCount() == 0)
            {
                mCurrentTab = -1;
                if(autoNewTab)
                    addTab("");
            } else {
                mCurrentTab = -1;
                setCurrentTab(0);
            }
            
        } else {
            setCurrentTab(--tabId);
        }
        return mCurrentTab;
    }

    private void setEditTextPref(JecEditText mEditText)
    {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(mJecEditor);
        String font = mPref.getString("font", "Monospace");
        mEditText.setTypeface(Options.getFont(font));
        String font_size = mPref.getString("font_size", "12");
        mEditText.setTextSize(Float.valueOf(font_size));
        // 自动换行设置
        mEditText.setHorizontallyScrolling(!mPref.getBoolean("wordwrap", true));
        // 显示行数
        mEditText.setShowLineNum(mPref.getBoolean("show_line_num", true));
        // 显示空白字符
        mEditText.setShowWhitespace(mPref.getBoolean("show_tab", false));

        mJecEditor.registerForContextMenu(mEditText);
        mEditText.setKeepScreenOn(mPref.getBoolean("keep_screen_on", false));
        mEditText.setAutoIndent(mPref.getBoolean("auto_indent", false));
        boolean disablespell = mPref.getBoolean("spellcheck", false);
        JecEditText.setDisableSpellCheck(disablespell);
        JecEditText.setUseSystemMenu(mPref.getBoolean("use_system_menu", true));
        if(disablespell)
        {
            mEditText.setInputType(mEditText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }

        ColorScheme.set(mPref);
        Highlight.loadColorScheme();
        mEditText.setBackgroundColor(Color.parseColor(ColorScheme.color_backgroup));
        mEditText.setTextColor(Color.parseColor(ColorScheme.color_font));
        //首字母自动大写
        mEditText.setAutoCapitalize(mPref.getBoolean("auto_capitalize", false));
        mEditText.init();
    }
    
    public void setTabStatus(boolean ischanged)
    {
        TextView mTitleView = getTabTitleView();
        if(mTitleView == null)
            return;
        CharSequence text = (CharSequence) mTitleView.getText();
        if(!ischanged)
        {
            mTitleView.setText(text.toString());
            return;
        }
        SpannableString span = new SpannableString(text);
        ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
        span.setSpan(fcs, 0, text.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTitleView.setText(span);
    }

    private JecEditText createEditText()
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View tabIndicator = inflater.inflate(R.layout.edit_text, mTabContent, false);

        JecEditText mEditText = (JecEditText) tabIndicator.findViewById(R.id.text_content);
        mEditText.setOnTextChangedListener(new JecEditText.OnTextChangedListener() {
            
            @Override
            public void onTextChanged(JecEditText mEditText)
            {
                if(mOnTextChangedListener != null)
                    mOnTextChangedListener.onTextChanged(mEditText);
                setTabStatus(mEditText.isTextChanged());
            }
        });
        return mEditText;
    }

    private View createIndicatorView()
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View tabIndicator = inflater.inflate(R.layout.tab_indicators, mTabWidget, false);
        //TextView tv = (TextView) tabIndicator.findViewById(R.id.title);

        return tabIndicator;// .findViewById(R.id.tab_indicator_layout);
    }

    public JecEditText getCurrentEditText()
    {
        return mCurrentEditText;
    }

    public void setCurrentTab(int index)
    {
        if(index < 0 || index >= mTabSpecs.size())
        {
            return;
        }

        if(index == mCurrentTab)
        {
            return;
        }

        // notify old tab content
        if(mCurrentTab != -1 && mCurrentTab < mTabSpecs.size())
        {
            mTabSpecs.get(mCurrentTab).hide();
        }

        mCurrentTab = index;
        // tab content
        mCurrentEditText = mTabSpecs.get(index);
        mCurrentEditText.show();
        if(mOnTabChangeListener != null)
            mOnTabChangeListener.onTabChanged(mCurrentTab);
        //切换到最前
        scrollToSelected();
        
        // Call the tab widget's focusCurrentTab(), instead of just
        // selecting the tab.
        //mTabWidget.focusCurrentTab(mCurrentTab);
        
        /*if(!mTabWidget.hasFocus())
        {
            // if the tab widget didn't take focus (likely because we're in
            // touch mode)
            // give the current tab content view a shot
            // 不能使用，避免输入法弹出来
            // mCurrentEditText.requestFocus();
        }*/

        // mTabContent.requestFocus(View.FOCUS_FORWARD);
        // invokeOnTabChangeListener();
    }

    /**
     * Register a callback to be invoked when the selected state of any of the
     * items in this list changes
     * 
     * @param l
     *            The callback that will run
     */
    public void setOnTabChangedListener(OnTabChangeListener l)
    {
        mOnTabChangeListener = l;
    }

    /**
     * 标签关闭前要触发的事件
     */
    public interface OnTabCloseListener
    {
        /**
         * 安全地关闭标签
         * @param action
         * @param startIndex 触发动作时的标签位置
         * @param curIndex 当前标签位置
         */
        public void onTabClose(int action, int startIndex, int curIndex);
    }
    
    public void setOnTabCloseListener(OnTabCloseListener l)
    {
        mOnTabCloseListener = l;
    }

    /**
     * Interface definition for a callback to be invoked when tab changed
     */
    public interface OnTabChangeListener
    {
        void onTabChanged(int tabId);
    }
    
    

    private void scrollToSelected()
    {
        mScroller.post(new Runnable() {

            @Override
            public void run()
            {
                if(mCurrentTab < 0)
                {
                    return;
                }
                View tab = mTabWidget.getChildTabViewAt(mCurrentTab);

                Rect tabRect = new Rect();
                tab.getGlobalVisibleRect(tabRect);
                Rect scrollerRect = new Rect();
                mScroller.getGlobalVisibleRect(scrollerRect);
                int width = tab.getWidth();
                int leftMargin = ((LinearLayout.LayoutParams)tab.getLayoutParams()).leftMargin;
                int left = width * mCurrentTab + leftMargin * mCurrentTab;
                left -= (int)(width*0.8F);
                int top = mScroller.getHeight() + mScroller.getPaddingTop();
                mScroller.setSmoothScrollingEnabled(true);
                //move current tab to center
                mScroller.smoothScrollTo(left, top);
                mTabWidget.focusCurrentTab(mCurrentTab);
                
            }
        });
    }
    
    public int getTabCount()
    {
        return mTabWidget.getTabCount();
    }
    
    public void setAutoNewTab(boolean newtab)
    {
        autoNewTab = newtab;
    }
    
    public void iterCloseTab(int action, int index, int count)
    {
        //int count = mTabWidget.getTabCount();
        int start = action==TabWidget.MENU_ACTION_CLOSE_RIGHT ? index+1 : 0;

        while(--count >= start)
        {
            if(action == TabWidget.MENU_ACTION_CLOSE_OTHER && count==index)
                continue;
            if(mOnTabCloseListener != null)
            {
                mOnTabCloseListener.onTabClose(action, index, count);
            }
            break;
        }
        /*if(autoNewTab && mTabWidget.getTabCount() < 1)
        {
            addTab("");
        }*/
    }

    private OnMenuClickListener mOnMenuClickListener = new OnMenuClickListener() {
        
        @Override
        public void onMenuClick(int action, int index)
        {
            autoNewTab = true;
            iterCloseTab(action, index, mTabWidget.getTabCount());
        }
    };
    
    public ArrayList<String> getAllPath()
    {
        ArrayList<String> ret = new ArrayList<String>();
        for(JecEditText jec :mTabSpecs)
        {
            ret.add(jec.getPath());
        }
        return ret;
    }

}
