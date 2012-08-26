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

import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class EncodingList
{
    private JecEditor mJecEditor;
    public static final String[] list = {
        "UTF-8",
        "GBK",
        "ARMSCII-8",
        "BIG5",
        "BIG5-HKSCS",
        "CP866",
        "CP932",
        "EUC-JP",
        "EUC-JP-MS",
        "EUC-KR",
        "EUC-TW",
        "GB18030",
        "GB2312",
        "GBK",
        "Georgian",
        "HZ",
        "IBM850",
        "IBM852",
        "IBM855",
        "IBM857",
        "IBM862",
        "IBM864",
        "ISO-2022-JP",
        "ISO-2022-KR",
        "ISO-8859-1",
        "ISO-8859-10",
        "ISO-8859-13",
        "ISO-8859-14",
        "ISO-8859-15",
        "ISO-8859-16",
        "ISO-8859-2",
        "ISO-8859-3",
        "ISO-8859-4",
        "ISO-8859-5",
        "ISO-8859-6",
        "ISO-8859-7",
        "ISO-8859-8",
        "ISO-8859-8-I",
        "ISO-8859-9",
        "ISO-IR-111",
        "JOHAB",
        "KOI8-R",
        "KOI8R",
        "KOI8U",
        "SHIFT_JIS",
        "TCVN",
        "TIS-620",
        "UCS-2",
        "UCS-4",
        "UHC",
        "UTF-7",
        "UTF-8",
        "UTF-16",
        "UTF-16BE",
        "UTF-16LE",
        "UTF-32",
        "VISCII",
        "WINDOWS-1250",
        "WINDOWS-1251",
        "WINDOWS-1252",
        "WINDOWS-1253",
        "WINDOWS-1254",
        "WINDOWS-1255",
        "WINDOWS-1256",
        "WINDOWS-1257",
        "WINDOWS-1258",
    };
    
    public EncodingList(JecEditor mJecEditor)
    {
        this.mJecEditor = mJecEditor;
        ListAdapter adapter = new DialogListAdapter(mJecEditor, R.layout.dialog_list_row, Arrays.asList(list));
        new AlertDialog.Builder(mJecEditor).setTitle(R.string.encoding).setAdapter(adapter, mClickEvent).show();
    }
    
    private OnClickListener mClickEvent = new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            final String encoding = list[which];
            mJecEditor.setEncoding(encoding);
        }
        
    };

    private class DialogListAdapter extends ArrayAdapter<String>
    {

        public DialogListAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
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
            String text = getItem(position);

            holder.text.setText(text);

            return view;
        }
    }
    
}
