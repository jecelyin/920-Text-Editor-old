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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.jecelyin.editor.EditorSettings;
import com.jecelyin.editor.R;
import com.jecelyin.util.JecLog;

import java.util.ArrayList;
import java.util.List;

public class SymbolGrid extends RelativeLayout
{
    private ArrayList<String> mButtons;
    private OnSymbolClickListener mListener;
    private ImageView closeButton;
    private GridView mGridView;
    private LinearLayout mDrager;
    private int mTop, mRight, mBottom, mLeft;

    public SymbolGrid(Context context)
    {
        super(context);
    }
    
    public SymbolGrid(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        View mView = inflate(context, R.layout.symbol_grid, this);
        //init(context);
        closeButton = (ImageView) mView.findViewById(R.id.iv_close);
        mGridView = (GridView) mView.findViewById(R.id.gridview);
        mDrager = (LinearLayout) mView.findViewById(R.id.drag);
        appendToolbarButton();
    }

    public static interface OnSymbolClickListener
    {
        void OnClick(String symbol);
    }
    
    public void setClickListener(OnSymbolClickListener mOnSymbolClickListener)
    {
        mListener = mOnSymbolClickListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        //修正插入字符后，跑回原位的问题
        if(mLeft!=0 && mTop!=0 && mRight!=0 && mBottom!=0)
        {
            offsetLeftAndRight(mLeft-l);
            offsetTopAndBottom(mTop-t);
        }
        
        super.onLayout(changed, mLeft, mTop, mRight, mBottom);
    }

    private void appendToolbarButton()
    {
        mButtons = new ArrayList<String>();
        mButtons.add("{");
        mButtons.add("}");
        mButtons.add("<");
        mButtons.add(">");
        mButtons.add(";");
        mButtons.add("\"");

        mButtons.add("(");
        mButtons.add(")");
        mButtons.add("/");
        mButtons.add("\\");
        mButtons.add("'");
        mButtons.add("%");
        mButtons.add("[");
        mButtons.add("]");

        mButtons.add("|");
        mButtons.add("#");
        mButtons.add("=");
        mButtons.add("$");
        mButtons.add(":");

        mButtons.add(",");
        mButtons.add("&");
        mButtons.add("?");

        mButtons.add("\t");
        mButtons.add("\n");
        
        mButtons.add("!");
        mButtons.add("@");
        mButtons.add("^");
        mButtons.add("*");
        mButtons.add("_");
        mButtons.add("+");
        mButtons.add("-");

        closeButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v)
             {
                 SymbolGrid.this.setVisibility(View.GONE);
             }
        });

        mGridView.setAdapter(new GridAdapter(getContext(), mButtons));
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //为了显示触摸效果
                if(view instanceof TextView)
                {
                    String txt = ((TextView)view).getText().toString();
                    if("\\n".equals(txt))
                        txt = "\n";
                    else if ("\\t".equals(txt)) {
                        txt = EditorSettings.INDENT_STRING;
                    }
                    mListener.OnClick(txt);
                }
            }
        });
        
        mDrager.setOnTouchListener(new OnTouchListener() {
            private int lastX, lastY; // 记录移动的最后的位置
            
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // 获取Action
                int ea = event.getAction();

                switch(ea)
                {
                    case MotionEvent.ACTION_DOWN: // 按下
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE: // 移动
                        // 移动中动态设置位置
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        mLeft = getLeft() + dx;
                        mTop = getTop() + dy;
                        mRight = getRight() + dx;
                        mBottom = getBottom() + dy;
                        layout(mLeft, mTop, mRight, mBottom);
 
                        // 将当前的位置再次设置
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP: // 脱离
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void setVisibility(int visibility)
    {
        //不能直接GONE，不然位置会变
        if(visibility == View.GONE)
            visibility = View.INVISIBLE;
        super.setVisibility(visibility);
    }
    
    private class GridAdapter extends BaseAdapter
    {
        private List<String> mData;
        private Context mContext;
        
        public GridAdapter(Context context, List<String> data) {
            mData = data;
            mContext = context;
        }
        
        @Override
        public int getCount()
        {
            return mData.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v;
            if (convertView == null) {
                //v = mInflater.inflate(mResource, parent, false);
                String symbol = mData.get(position);

                if("\t".equals(symbol))
                    symbol = "\\t";
                else if("\n".equals(symbol))
                    symbol = "\\n";
                TextView tv = new TextView(mContext);
                tv.setTextAppearance(mContext, R.style.symbolgrid_text);
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                tv.setText(symbol);

                v = tv;
            } else {
                v = convertView;
            }

            return v;
        }
        
    }
}
