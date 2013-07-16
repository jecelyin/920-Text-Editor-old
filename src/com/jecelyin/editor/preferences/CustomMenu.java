package com.jecelyin.editor.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jecelyin.editor.BaseActivity;
import com.jecelyin.editor.Commands;
import com.jecelyin.editor.Commands.Command;
import com.jecelyin.editor.EditorSettings;
import com.jecelyin.editor.R;
import com.jecelyin.widget.TouchInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomMenu extends BaseActivity
{

    private TouchInterceptor list;

    private List<Command> items = new ArrayList<Commands.Command>();

    private FunctionAdapter adapter;

    private SharedPreferences sp;

    private int type;

    public static final String TYPE_TOOLBAR = "toolbar_icons";

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.tool_settings);
        list = (TouchInterceptor) findViewById(R.id.toolbar_listView);

        Intent it = getIntent();
        type = it.getIntExtra("type", R.string.custom_toolbar);
        setTitle(type);
        sp = EditorSettings.getInstance(TYPE_TOOLBAR);
        Collection<?> map = sp.getAll().values();
        ArrayList<Integer> assigned = new ArrayList<Integer>();
        //已经设置好的项目
        if (map.size() > 0)
        {
            int id;
            for (Object v : map)
            {
                id = Integer.valueOf(v.toString());
                if (Commands.COMMAND_MAP.indexOfKey(id) >= 0)
                {
                    items.add(Commands.COMMAND_MAP.get(id));
                    assigned.add(id);
                }
            }
        }
        //分割线
        items.add(Commands.Divider);
        //没有使用的项目
        for (Command c : Commands.COMMAND_LIST)
        {
            if (!assigned.contains(c.name))
                items.add(c);
        }
        adapter = new FunctionAdapter(this);
        list.setAdapter(adapter);
        list.setDropListener(mDropListener);
    }

    protected void onStop()
    {
        super.onStop();
        
        Editor edit = sp.edit();
        edit.clear();
        
        Command item;
        StringBuilder sb = new StringBuilder();
        for (int i = 0;; i++) {
            View vv = list.getChildAt(i);
            if (vv == null || vv.getId()==R.layout.tool_settings_divider) {
                break;
            }
            item = items.get(i);
            sb.append(',').append(item.name);
        }
        edit.putString(String.valueOf(type), sb.toString());
        edit.commit();
        
    }

    // 交换listview的数据
    private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener()
    {
        public void drop(int from, int to)
        {
            Command item = items.get(from);
            items.remove(item);// .remove(from);
            items.add(to, item);
            adapter.notifyDataSetChanged();
        }
    };

    private class FunctionAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        Context mContext;

        public FunctionAdapter(Context c)
        {
            mInflater = LayoutInflater.from(c);
            mContext = c;
        }

        public int getCount()
        {
            return items.size();
        }

        public Command getItem(int arg0)
        {
            return items.get(arg0);
        }

        public long getItemId(int arg0)
        {
            return arg0;
        }

        public View getView(int arg0, View contentView, ViewGroup arg2)
        {
            ImageView img;
            TextView name, hotkey;

            Command cmd = getItem(arg0);

            if (cmd.name == Commands.DIVIDER_NAME)
            {
                contentView = mInflater.inflate(R.layout.tool_settings_divider, null);
                contentView.setId(R.layout.tool_settings_divider);
                return contentView;
            }

            if (contentView == null || contentView.getId()!=R.layout.tool_settings_list)
            {
                contentView = mInflater.inflate(R.layout.tool_settings_list, null);
                contentView.setId(R.layout.tool_settings_list);
            }

            img = (ImageView) contentView.findViewById(R.id.func_icon);
            img.setImageResource(cmd.icon);
            //simg.setBackgroundResource(cmd.icon);
            name = (TextView) contentView.findViewById(R.id.func_name);
            name.setText(cmd.name);
            hotkey = (TextView) contentView.findViewById(R.id.func_hotkey);
            hotkey.setText(cmd.hotkey);
            hotkey.setKeyListener(new KeyListener()
            {

                @Override
                public int getInputType()
                {
                    return 0;
                }

                @Override
                public boolean onKeyDown(View view, Editable text, int keyCode,
                        KeyEvent event)
                {
                    StringBuilder keys = new StringBuilder();
                    if (event.isSymPressed())
                        keys.append("SYM + ");
                    if (event.isShiftPressed())
                        keys.append("Shift + ");
                    if (event.isAltPressed())
                        keys.append("Alt + ");
                    keys.append(String.valueOf((char) event.getUnicodeChar()));
                    ((TextView) view).setText(keys.toString());
                    return false;
                }

                @Override
                public boolean onKeyUp(View view, Editable text, int keyCode,
                        KeyEvent event)
                {
                    return false;
                }

                @Override
                public boolean onKeyOther(View view, Editable text,
                        KeyEvent event)
                {
                    return false;
                }

                @Override
                public void clearMetaKeyState(View view, Editable content,
                        int states)
                {

                }

            });
            // handle =
            // (ImageView)contentView.findViewById(R.id.func_drag_handle);
            // handle.setOnTouchListener(CustomMenu.this.list.get);
            return contentView;
        }

    }

}
