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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.jecelyin.util.FileUtil;
import com.jecelyin.util.JecLog;
import com.jecelyin.util.LinuxShell;
import com.stericson.RootTools.RootTools;

import org.mozilla.charsetdetector.CharsetDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import jecelyin.android.compat.TextViewBase;
import jecelyin.android.v2.text.SpannableStringBuilder;

public class AsyncReadFile
{
    private JecEditor mJecEditor;
    public static final int RESULT_OK = 0;
    public static final int RESULT_FAIL = 1;

    private ProgressDialog mProgressDialog;
    private int mSelStart = 0;
    private int mSelEnd = 0;
    private ReadHandler mReadHandler;
    private String mEncoding = "";
    
    class AsyncResult
    {
        public String path;
        public String encoding;
        public int linebreak;
        public String errorMsg;
        public SpannableStringBuilder data;
    }

    public AsyncReadFile(final JecEditor mJecEditor, final String path, final String encoding, final int lineBreak, int selStart, int selEnd)
    {
        JecEditor.isLoading = true;
        // mJecEditor.text_content.requestFocus();
        this.mJecEditor = mJecEditor;
        mSelStart = selStart > 0 ? selStart : 0;
        mSelEnd = selEnd > 0 ? selEnd : 0;
        mReadHandler  = new ReadHandler(AsyncReadFile.this);
        showProgress();
        
        mEncoding = encoding;
        
        Thread thread = new Thread(new Runnable() {
            
            @Override
            public void run()
            {
                int what = RESULT_OK;
                AsyncResult result = new AsyncResult();
                try
                {
                    String fileString = path;
                    File file = new File(fileString);
                    fileString = file.getAbsolutePath();

                    String tempFile = JecEditor.TEMP_PATH + "/"+file.getName()+".tmp";
                    //Log.d(TAG, "write:"+file.canWrite()+" rooted:"+isRoot+" access:"+RootTools.isAccessGiven());
                    boolean root = false;
                    if(!file.canWrite() && EditorSettings.TRY_ROOT && RootTools.isAccessGiven())
                    {
                        //RootTools.sendShell("busybox cp " + LinuxShell.getCmdPath(fileString) + " " + tempFile, 1000);
                        RootTools.copyFile(LinuxShell.getCmdPath(fileString), tempFile, true, true);
                        RootTools.sendShell("busybox chmod 777 " + tempFile, 1000);
                        fileString = tempFile;
                        file = new File(fileString);
                        root = true;
                    }

                    if(file.isFile())
                    {
                        SpannableStringBuilder mData = readFile(file, lineBreak);

                        if(root)
                        {
                            LinuxShell.execute("rm -rf " + tempFile);
                        }
                        what = RESULT_OK;
                        result.data = mData;
                    } else {
                        what = RESULT_FAIL;
                        result.errorMsg = AsyncReadFile.this.mJecEditor.getString(R.string.can_not_open_file);
                    }
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    String errorMsg = e.getMessage();
                    if(errorMsg.contains("Permission denied")){
                        EditorSettings.setBoolean("get_root", true);
                        errorMsg = mJecEditor.getString(R.string.try_root);
                        try
                        {
                            RootTools.sendShell("busybox ls " + path, 1000);
                        } catch (Exception e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                    what = RESULT_FAIL;
                    result.errorMsg = errorMsg;
                }catch (Exception e)
                {
                    e.printStackTrace();
                    final String errorMsg = e.getMessage();// R.string.exception;
                    what = RESULT_FAIL;
                    result.errorMsg = errorMsg;
                }catch (OutOfMemoryError e)
                {//内存不足
                    final String errorMsg = mJecEditor.getString(R.string.out_of_memory);
                    what = RESULT_FAIL;
                    result.errorMsg = errorMsg;
                } finally {
                    result.path = path;
                    result.encoding = mEncoding;
                    result.linebreak = lineBreak;
                    Message msg = mReadHandler.obtainMessage(what, result);
                    msg.sendToTarget();
                }
            }
        });
        thread.start();
        
    }
    
    private void showProgress()
    {
        mProgressDialog = new ProgressDialog(mJecEditor);
        mProgressDialog.setTitle(R.string.spinner_message);
        mProgressDialog.setMessage(mJecEditor.getText(R.string.loading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog)
            {
                dismissProgress();
            }
        });
        mProgressDialog.show();
    }
    
    public void dismissProgress()
    {
        if(mProgressDialog != null)
            mProgressDialog.dismiss();
    }
    
    static class ReadHandler extends Handler
    {
        private AsyncReadFile mAsyncReadFile;
        
        public ReadHandler(AsyncReadFile arf)
        {
            super();
            mAsyncReadFile = arf;
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            AsyncResult rs = (AsyncResult) msg.obj;
            mAsyncReadFile.dismissProgress();
            if(msg.what == RESULT_OK) {
                mAsyncReadFile.finish(rs.data, rs.path, rs.encoding, rs.linebreak);
            } else {
                JecEditor.isLoading = false;
                JecLog.msg(rs.errorMsg);
            }
        }
    };

    private void finish(SpannableStringBuilder data, String path, String encoding, int lineBreak)
    {
        if(data == null)
        {
            JecEditor.isLoading = false;
            return;
        }
        try
        {
            TextViewBase mEditText = mJecEditor.getEditText();
            mEditText.setText(data);
            mEditText.updateTextFinger();
            int len = data.length();
            if(mSelEnd >= len || mSelStart >= len)
                mSelStart = mSelEnd = 0;
            // scroll to topprivate
            //mEditText.setSelection(0, 0);
            mEditText.setSelection(mSelStart, mSelEnd);
            mEditText.clearFocus();
            // mJecEditor.text_content.invalidate();
            mEditText.setEncoding(encoding);
            mEditText.setLineBreak(lineBreak);
            mEditText.setPath(path);
            mJecEditor.onLoaded(mSelStart, mSelEnd);
           
        }catch (OutOfMemoryError e)
        {
            Toast.makeText(mJecEditor, R.string.out_of_memory, Toast.LENGTH_LONG).show();
        }catch (Exception e)
        {
            //Toast.makeText(mJecEditor, e.getMessage(), Toast.LENGTH_LONG).show();
            JecLog.e(e.getMessage(), e);
        }
    }

    private SpannableStringBuilder readFile(File file, int lineBreak) throws Exception
    {
        //检测编码
        if(mEncoding==null || "".equals(mEncoding))
        {
            FileInputStream fis = new FileInputStream(file);
            byte[] buff = new byte[64*1024];
            int buf_len = fis.read(buff);
            fis.close();
            if(buf_len <= 0)
                return null;

            CharsetDetector cd = new CharsetDetector();
            if(cd.isOK())
            {
                cd.handleData(buff, 0, buf_len);
                cd.dataEnd();
                mEncoding = cd.getCharset();
                cd.destroy();
                if ( mEncoding != null && mEncoding.length() > 0 )
                {
                    if("GB18030".equals(mEncoding.toUpperCase()))
                        mEncoding = "GBK";
                }else{
                    mEncoding = "utf-8";
                }
            }else{
                mEncoding = "utf-8";
            }
        }
        
        return FileUtil.readFile(file, mEncoding, lineBreak);
    }
}
