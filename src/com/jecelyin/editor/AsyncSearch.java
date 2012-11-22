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
 *   along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jecelyin.editor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Editable;
import android.widget.Toast;

public class AsyncSearch
{
    private String mPattern = "";
    private JecEditor mJecEditor;
    private ArrayList<int[]> mData = new ArrayList<int[]>();
    private int start = 0;
    private boolean next = true;
    private boolean replaceAll = false;
    private CharSequence replaceText = "";
    private boolean regexp = false;
    private boolean ignoreCase = true;
    
    public void search(String pattern, boolean next, JecEditor mJecEditor)
    {
        replaceAll = false;
        mJecEditor.getEditText().requestFocus();
        this.mJecEditor = mJecEditor;
        this.next = next;
        this.start = next ? mJecEditor.getEditText().getSelectionEnd() : mJecEditor.getEditText().getSelectionStart(); //光标位置
        this.mPattern = !regexp ? escapeMetaChar(pattern) : pattern;
        mData.clear();
        SearchTask mSearchTask = new SearchTask();
        mSearchTask.execute();
    }
    
    public void setRegExp(boolean open)
    {
        regexp = open;
    }
    
    public void setIgnoreCase(boolean open)
    {
        ignoreCase = open;
    }
    
    public void replace(String word)
    {
        if(mData.size() == 0)
            return;
        int[] ret = mData.get(0);
        mJecEditor.getEditText().getText().replace(ret[0], ret[1], (CharSequence)word);
    }
    
    public void replaceAll(String searchText, String replaceText, JecEditor mJecEditor)
    {
        this.replaceAll = true;
        this.replaceText = (CharSequence)replaceText;
        this.mJecEditor = mJecEditor;
        this.next = true;
        this.start = 0; //光标位置
        this.mPattern = !regexp ? escapeMetaChar(searchText) : searchText;
        mData.clear();
        SearchTask mSearchTask = new SearchTask();
        mSearchTask.execute();
    }
    
    private void onSearchFinished(ArrayList<int[]> data)
    {
        if(data.size() == 0)
        {
            String msg;
            if(replaceAll)
            {
                msg = mJecEditor.getString(R.string.replace_finish);
            } else {
                msg = mJecEditor.getString(next ? R.string.not_found_next : R.string.not_found_up).replaceAll("%s", mPattern);
            }
            Toast.makeText(mJecEditor.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        } else if(replaceAll) {
            Editable mText = mJecEditor.getEditText().getText();
            //一定要从后面开始替换,不然会有问题
            int end = data.size();
            int[] ret;
            for(int i=end-1; i>=0; i--)
            {
                ret = data.get(i);
                mText.replace(ret[0], ret[1], replaceText);
            }
        } else {
            int[] ret = data.get(0);
            //滚动当前找到的内容到中央，不然在底部看得不爽
            //int end = mJecEditor.text_content.getText().length();
            /*if(ret[1]+200 <= end)
            {
                mJecEditor.text_content.setSelection(ret[0], ret[1]+200);
            } else {
                mJecEditor.text_content.setSelection(ret[0], end);
            }*/
            mJecEditor.getEditText().setSelection(ret[0], ret[1]);
            int x = mJecEditor.getEditText().getScrollX();
            int y = mJecEditor.getEditText().getScrollY();
            mJecEditor.getEditText().scrollBy(x, y+40);
        }
    }
    
    private static String escapeMetaChar( String pattern )
    {
        final String metachar = ".^$[]*+?|()\\";

        StringBuilder newpat = new StringBuilder();

        int len = pattern.length();

        for( int i=0;i<len;i++ ){
            char c = pattern.charAt(i);
            if ( metachar.indexOf(c) >=0 ){
                newpat.append('\\');
            }
            newpat.append(c);
        }
        return newpat.toString();
    }
    
    private class SearchTask extends AsyncTask<String, Boolean, Boolean>
    {
        private ProgressDialog mProgressDialog;
        private boolean mCancelled;

        @Override
        protected void onPreExecute() {
            mCancelled=false;
            mProgressDialog = new ProgressDialog(mJecEditor);
            mProgressDialog.setTitle(R.string.spinner_message);
            mProgressDialog.setMessage(mJecEditor.getText(R.string.searching));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                public void onCancel(DialogInterface dialog)
                {
                    mCancelled=true;
                    cancel(false);
                }
            });
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            if ( isCancelled() ){
                return true;
            }
            try {
                Pattern pattern;
                if(ignoreCase)
                {
                    pattern = Pattern.compile(mPattern, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.MULTILINE);
                } else {
                    pattern = Pattern.compile(mPattern);
                }
                
                Matcher m = pattern.matcher( mJecEditor.getEditText().getString() );
                
                if(replaceAll)
                {
                    while ( m.find() )
                    {
                        if ( mCancelled ){
                            break;
                        }
                        mData.add(new int[] {m.start(), m.end()});
                    }
                }else if(next)
                {
                    if(m.find(start))
                    {
                        mData.add(new int[] {m.start(), m.end()});
                    }
                } else {
                    //查找上一个
                    if( start <= 0 )
                        return true;
                    //从头开始搜索获取所有位置
                    while ( m.find() )
                    {
                        if ( mCancelled ){
                            break;
                        }else if( m.end() >= start )
                        {
                            if(mData.size() > 0) //fixed: Caused by: java.lang.ArrayIndexOutOfBoundsException
                            {
                                int[] ret = mData.get(mData.size()-1);
                                //考虑到会边搜索边修改的情况,所以只能每次都穷搜一下
                                mData.clear();
                                mData.add(ret);
                            }
                            
                            break;
                        }
                        mData.add(new int[] {m.start(), m.end()});
                    }
                }
            }catch(Exception e) {
                Toast.makeText(mJecEditor.getApplicationContext(), mJecEditor.getString(R.string.search_error), Toast.LENGTH_LONG).show();
            }
            return true;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            AsyncSearch.this.onSearchFinished(mData);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            onPostExecute(false);
        }
        
    }
}
