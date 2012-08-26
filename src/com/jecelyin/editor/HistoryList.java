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

package com.jecelyin.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryList
{
    private JecEditor mJecEditor;
    private ArrayList<FileInfo> historyFileList;
    
    public HistoryList(JecEditor mJecEditor)
    {
        this.mJecEditor = mJecEditor;
        historyFileList = getFileList();
        ListAdapter adapter = new HistoryListAdapter(mJecEditor, R.layout.dialog_list_row, historyFileList);
        new AlertDialog.Builder(mJecEditor).setTitle(R.string.history).setAdapter(adapter, mClickEvent).show();
    }
    
    private OnClickListener mClickEvent = new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            final FileInfo fi = historyFileList.get(which);
            final String path = fi.path;
            File mFile = new File(path);
            if(!mFile.exists())
            {
                Toast.makeText(mJecEditor.getApplicationContext(), mJecEditor.getString(R.string.file_not_exists), Toast.LENGTH_LONG);
                return;
            }
            mJecEditor.saveConfirm(new Runnable() {
                
                @Override
                public void run()
                {
                    mJecEditor.readFileToEditText(path);
                    //mJecEditor.getEditText().setSelection(fi.sel_start, fi.sel_end);
                }
            });
        }
        
    };
    
    private class FileInfo
    {
        String path = "";
        long access_time = 0;
        int sel_start = 0;
        int sel_end = 0;
    }
    
    private ArrayList<FileInfo> getFileList()
    {
        SharedPreferences sp = mJecEditor.getSharedPreferences(JecEditor.PREF_HISTORY, Context.MODE_PRIVATE);
        ArrayList<FileInfo> fl = new ArrayList<FileInfo>();
        Map<String, ?> map = sp.getAll();
        for (Entry<String, ?> entry : map.entrySet())
        {
            Object val = entry.getValue();
            if (val instanceof String) {
                String[] vals = ((String)val).split(",");
                if (vals.length >= 3) {
                    try {
                        FileInfo fi = new FileInfo();
                        fi.path = entry.getKey();
                        fi.sel_start = Integer.parseInt(vals[0]);
                        fi.sel_end = Integer.parseInt(vals[1]);
                        fi.access_time = Long.parseLong(vals[2]);
                        fl.add(fi);
                    } catch (Exception e) {
                    }
                }
            }
        }//end for
        if (fl.size() == 0) {
            return fl;
        }

        Collections.sort(fl, new Comparator<FileInfo>() {
            public int compare(FileInfo object1, FileInfo object2) {
                if (object2.access_time < object1.access_time) {
                    return -1;
                } else if (object2.access_time > object1.access_time) {
                    return 1;
                }
                return 0;
            }
        });

        int historymax = fl.size();
        if (historymax > 20) {
            historymax = 20;
        }
        ArrayList<FileInfo> items = new ArrayList<FileInfo>();
        int max = fl.size();
        for (int i = 0; i < max; i++) {
            if (i >= historymax) {
                // 限制最近打开历史记录条数
                sp.edit().remove(fl.get(i).path);
            } else {
                items.add(fl.get(i));
            }
        }
        sp.edit().commit();
        return items;
    }
    
    private class HistoryListAdapter extends ArrayAdapter<FileInfo>
    {

        public HistoryListAdapter(Context context, int textViewResourceId, List<FileInfo> objects) {
            super(context, textViewResourceId, objects);
        }
        
        private class ViewHolder
        {
            public TextView path;
        }
        
        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent)
        {
            final View view;
            ViewHolder holder;
            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder)view.getTag();
            } else {
                view = View.inflate(mJecEditor, R.layout.dialog_list_row, (ViewGroup)null);
                
                holder = new ViewHolder();
                holder.path = (TextView)view.findViewById(R.id.textView1);
                //holder.path.setHeight(48);
                view.setTag(holder);
            }
            FileInfo fi = getItem(position);

            holder.path.setText(fi.path);

            return view;
        }
    }
    
}
