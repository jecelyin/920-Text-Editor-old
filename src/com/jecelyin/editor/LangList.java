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

import java.util.ArrayList;

import com.jecelyin.highlight.Highlight;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class LangList
{
    private JecEditor mJecEditor;
    private ArrayList<String[]> list;
    
    public LangList(JecEditor mJecEditor)
    {
        this.mJecEditor = mJecEditor;
        list = Highlight.getLangList();
        ListAdapter adapter = new DialogListAdapter(mJecEditor, R.layout.dialog_list_row, list);
        new AlertDialog.Builder(mJecEditor).setTitle(R.string.highlight).setAdapter(adapter, mClickEvent).show();
    }
    
    private OnClickListener mClickEvent = new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            final String[] lang = list.get(which);
            mJecEditor.getEditText().setCurrentFileExt(lang[1]);
        }
        
    };

    private class DialogListAdapter extends ArrayAdapter<String[]>
    {

        public DialogListAdapter(Context context, int textViewResourceId, ArrayList<String[]> list) {
            super(context, textViewResourceId, list);
        }
        
        private class ViewHolder
        {
            public TextView text;
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
                holder.text = (TextView)view.findViewById(R.id.textView1);

                view.setTag(holder);
            }
            String[] lang = getItem(position);

            holder.text.setText(lang[0]);

            return view;
        }
    }
    
}
