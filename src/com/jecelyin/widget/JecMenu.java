package com.jecelyin.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jecelyin.editor.JecEditor;
import com.jecelyin.editor.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class JecMenu extends Dialog implements OnItemClickListener
{
    private ViewGroup mViewGroup;
    private GridView mGrid;
    private OnMenuItemSelectedListener mOnMenuItemSelectedListener;
    private int[][] mMenuItemLists = new int[][]{
            new int[] {R.id.menu_reopen, R.string.reopen, R.drawable.menu_open},
            new int[] {R.id.menu_saveas, R.string.saveas, R.drawable.menu_saveas},
            new int[] {R.id.menu_highlight, R.string.highlight, R.drawable.menu_highlight},
            new int[] {R.id.menu_encoding, R.string.encoding, R.drawable.menu_encoding},
            new int[] {R.id.menu_search_replace, R.string.search_replace, R.drawable.menu_search},
            new int[] {R.id.menu_pipe, R.string.open_with, R.drawable.menu_openwith},
            new int[] {R.id.menu_preferences, R.string.preferences, R.drawable.menu_setting},
            new int[] {R.id.menu_exit, R.string.exit, R.drawable.menu_exit},
    };
    private ArrayList<Map<String, Object>> mMenuItems = new ArrayList<Map<String,Object>>();

    
    public JecMenu(Context context)
    {
        super(context);
        init(context);
    }
    
    public JecMenu(Context context, int theme)
    {
        super(context, theme);
        init(context);
    }
    
    private void init(Context context)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        mViewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.main_menu, null);
        mGrid = (GridView)mViewGroup.findViewById(R.id.menu_grid);
        TextView verTextView = (TextView)mViewGroup.findViewById(R.id.menu_version);
        verTextView.setText(JecEditor.version);
        
        HashMap<String, Object> map;
        for(final int[] item: mMenuItemLists)
        {
            map = new HashMap<String, Object>();
            map.put("id", item[0]);
            map.put("text", context.getString(item[1]));
            map.put("icon", item[2]);
            mMenuItems.add(map);
        }

        SimpleAdapter mAdapter = new SimpleAdapter(
                getContext()
                , mMenuItems
                , R.layout.main_menu_item
                , new String[]{ "text", "icon" }
                , new int[]{ R.id.main_menu_text, R.id.main_menu_icon }
             );
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(this);
    }
    
    protected void onCreate(Bundle mBundle)
    {
      super.onCreate(mBundle);
      setContentView(mViewGroup);
      WindowManager.LayoutParams attr = getWindow().getAttributes();
      attr.gravity = Gravity.CENTER|Gravity.BOTTOM;
      attr.verticalMargin = 0;
      //getWindow().getAttributes().alpha = 0.5F;
      attr.dimAmount = 0.0F;
      /*getWindow().getAttributes().width = (int)getContext().getResources().getDimension(2131230777);
      this.s = (int)getContext().getResources().getDimension(2131230777);
      getWindow().setBackgroundDrawableResource(2130838102);*/
      getWindow().setBackgroundDrawableResource(R.drawable.main_menu_bg);
      //按非窗口区域时，可以关闭窗口
      setCanceledOnTouchOutside(true);
      //Toast.makeText(getContext(), "Menu onCreate", Toast.LENGTH_LONG).show();
    }
    
    protected void onStart()
    {
        super.onStart();
      /*Logger.a("MttPopMenu", "onstart");
      d();
      int i1 = b();
      getWindow().getAttributes().height = this.t;
      getWindow().getAttributes().x = (this.h.x - this.s / 2);
      if (i1 != f)
        getWindow().getAttributes().y = this.h.y;
      else
        getWindow().getAttributes().y = (this.h.y - this.t);
      g = 1 + g;*/
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        int itemId = (Integer) mMenuItems.get(position).get("id");
        dismiss();
        mOnMenuItemSelectedListener.onMenuItemSelected(itemId, view);
        
    }
    
    public void setOnMenuItemSelectedListener(OnMenuItemSelectedListener l)
    {
        mOnMenuItemSelectedListener = l;
    }
    
    public interface OnMenuItemSelectedListener
    {
        public boolean onMenuItemSelected(int id, View v);
    }

}
