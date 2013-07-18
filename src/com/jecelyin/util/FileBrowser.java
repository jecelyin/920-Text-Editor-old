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

package com.jecelyin.util;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.*;
import com.jecelyin.editor.EditorSettings;
import com.jecelyin.editor.EncodingList;
import com.jecelyin.editor.JecApp;
import com.jecelyin.editor.JecEditor;
import com.jecelyin.editor.R;
import com.jecelyin.widget.JecButton;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import java.text.SimpleDateFormat;

/**
 * 文件选择器
 * 
 * @author jecelyin
 * 
 */
public class FileBrowser extends ListActivity
{
    private ArrayList<File> files; //当前文件列表
    private FileListAdapter fileListAdapter;
    private LinearLayout pathButtons;
    private String default_filename = "";
    private int request_mode = 0; //0打开， 1保存模式，2选择路径
    private String current_path = ""; //当前路径
    private boolean isRoot = false;
    private EditText editTextFilename;
    private Intent mIntent;
    private Button saveButton;
    private Spinner linebreakSpinner;
    private Spinner encoding_list;
    private static int OPEN_WITH_CODE = 0;
    private int lastPos;
    
    public static final int MODE_BROWSE = 2;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        JecApp.addActivity(this);
        setContentView(R.layout.file_bowsers);
        getListView().setFastScrollEnabled(true);
        registerForContextMenu(getListView());
        pathButtons = (LinearLayout)findViewById(R.id.pathButtons);
        editTextFilename = (EditText)findViewById(R.id.editTextFilename);
        saveButton = (Button)findViewById(R.id.btnSave);
        saveButton.setOnClickListener(onSaveBtnClickListener);
        
        linebreakSpinner = (Spinner)findViewById(R.id.linebreak_list);
        LinearLayout linebreakLinearLayout = (LinearLayout)findViewById(R.id.linebreakLinearLayout);
        LinearLayout encodingLinearLayout = (LinearLayout)findViewById(R.id.encodingLinearLayout);
        
        encoding_list = (Spinner)findViewById(R.id.encoding_list);
        String[] lists = EncodingList.list;
        lists[0] = getString(R.string.auto_detection);
        encoding_list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, lists));
        int position = 0;
        if(!"".equals(EditorSettings.DEFAULT_ENCODING))
        for(int i=0; i<lists.length; i++)
            if(EditorSettings.DEFAULT_ENCODING.equals(lists[i]))
            {
                position = i;
                break;
            }
        encoding_list.setSelection(position);
        JecLog.d("pos="+position);
        File file = android.os.Environment.getExternalStorageDirectory();
        current_path = EditorSettings.HIGHLIGHT_LAST_PATH.length()==0 ? file.getPath() : EditorSettings.HIGHLIGHT_LAST_PATH;
        //获取传来的数据
        mIntent = getIntent();
        default_filename = mIntent.getStringExtra("filename");
        request_mode = mIntent.getIntExtra("mode", 0);
        isRoot = mIntent.getBooleanExtra("isRoot", false);
        editTextFilename.setText(default_filename);
        
        LinearLayout filenameLinearLayout = (LinearLayout)findViewById(R.id.filenameLinearLayout);
        if(request_mode == JecEditor.FILE_BROWSER_OPEN_CODE)
        {
            filenameLinearLayout.setVisibility(View.GONE);
            linebreakLinearLayout.setVisibility(View.VISIBLE);
            encodingLinearLayout.setVisibility(View.VISIBLE);
        } else if(request_mode == MODE_BROWSE) {
            LinearLayout okLinearLayout = (LinearLayout)findViewById(R.id.okLinearLayout);
            okLinearLayout.setVisibility(View.VISIBLE);
            filenameLinearLayout.setVisibility(View.GONE);
            linebreakLinearLayout.setVisibility(View.GONE);
            encodingLinearLayout.setVisibility(View.GONE);
            Button btnOK = (Button)findViewById(R.id.btnOK);
            btnOK.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v)
                {
                    mIntent.putExtra("path", current_path);
                    setResult(RESULT_OK, mIntent);
                    finish();
                }
            });
        }
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        showFileList(new File(current_path));
    }
    
    protected void onDestroy()
    {
        super.onDestroy();
        JecApp.removeActivity(this);
    }

    private OnClickListener onSaveBtnClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v)
        {
            String filename = editTextFilename.getText().toString().trim();
            if("".equals(filename))
            {
                Toast.makeText(getApplicationContext(), R.string.filename_is_empty, Toast.LENGTH_LONG).show();
            }else{
                mIntent.putExtra("file", current_path+File.separator+filename);
                mIntent.putExtra("linebreak", linebreakSpinner.getSelectedItemPosition());
                mIntent.putExtra("encoding", encoding_list.getSelectedItemPosition());
                setResult(RESULT_OK, mIntent);
                finish();
            }
            
        }
    };
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        //打开方式...
        menu.add(0, R.string.open_with, 0, R.string.open_with);
        //重命名
        menu.add(0, R.string.rename, 0, R.string.rename);
        //删除
        menu.add(0, R.string.delete, 0, R.string.delete);
        //创建目录
        menu.add(0, R.string.new_folder, 0, R.string.new_folder);
    }
    
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }
        int position = info.position;
        final File f;
        try {
            f = files.get(position);
            if(f == null)
                return false;
        } catch (Exception e) {
            return false;
        }
        lastPos = position;
        int itemId = item.getItemId();
        switch(itemId)
        {
            case R.string.open_with:
                Uri uri = Uri.fromFile(f);
                try
                {
                    if(f.isDirectory())
                    {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setDataAndType(uri, "file/*");
                        //startActivityForResult(intent, OPEN_WITH_CODE);
                        startActivity(intent);
                    } else {
                        Intent it   = new Intent(Intent.ACTION_VIEW);
                        it.setDataAndType(uri, "*/*");
                        startActivity(it);
                    }
                }catch (Exception e)
                {
                    
                }
                
                
                return false;
            case R.string.rename:
                final EditText lineEditText = new EditText(this);
                lineEditText.setText(f.getName());
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.rename).setView(lineEditText)
                        .setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dialog.dismiss();
                            String newname = lineEditText.getText().toString().trim();
                            String newfile = f.getParent() + File.separator + newname;
                            if("".equals(newname) || !f.renameTo(new File(newfile)))
                            {
                                Toast.makeText(FileBrowser.this, R.string.rename_failed, Toast.LENGTH_LONG).show();
                            } else {
                                refresh();
                            }
                        }catch(Exception e) {
                            
                        }
                     }
                });
                builder.show();
                return true;
            case R.string.delete:
                new AlertDialog.Builder(this).setTitle(R.string.delete).setMessage(R.string.confirm_delete)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        FileUtil.remove(f);
                        refresh();
                    }
                    
                })
                .show();
                return true;
            case R.string.new_folder:
                final EditText filenameEditText = new EditText(this);
                filenameEditText.setText(f.getName());
                AlertDialog.Builder fbuilder = new AlertDialog.Builder(this);
                fbuilder.setTitle(R.string.new_folder).setView(filenameEditText)
                        .setNegativeButton(android.R.string.cancel, null);
                fbuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dialog.dismiss();
                            String newname = filenameEditText.getText().toString().trim();
                            String newfile = (f.isFile() ? f.getParent() : f.getPath()) + File.separator + newname;
                            if("".equals(newname) || !(new File(newfile)).mkdirs())
                            {
                                Toast.makeText(FileBrowser.this, R.string.create_folder_fail, Toast.LENGTH_LONG).show();
                            } else {
                                refresh();
                            }
                        }catch(Exception e) {
                            
                        }
                     }
                });
                fbuilder.show();
                return true;
        }
        return super.onContextItemSelected(item);
    }
    
    @Override
    public void startActivityForResult (Intent intent, int requestCode)   
    {
        if(requestCode == OPEN_WITH_CODE)
        {
            refresh();
            return;
        }
        super.startActivityForResult(intent, requestCode);
    }
    
    public void refresh()
    {
        showFileList(new File(current_path));
    }
    
    @Override
    public void finish()
    {
        //记住最后打开的路径
        EditorSettings.setString("last_path", current_path);
        super.finish();
    }

    /**
     * 显示文件列表
     * 
     * @param path
     */
    private void showFileList(File path)
    {
        String curPath;
        try
        {
            curPath = path.getCanonicalPath();
        }catch (IOException e)
        {
            curPath = path.getPath().replaceAll("/./", "/");
        }
        //设置标题
        setTitle(curPath);
        
        current_path = curPath;
        //处理顶部路径按钮
        String[] paths=curPath.split(File.separator);
        JecButton mButton;
        StringBuilder mStringBuilder = new StringBuilder();
        pathButtons.removeAllViews();
        //始终显示根按钮
        if(paths.length < 1)
            paths = new String[]{File.separator};
        for(String p: paths)
        {
            mButton = new JecButton(this);
            if(p == null || "".equals(p))
            {
                p = File.separator;
            }
            mButton.setText(p);
            mStringBuilder.append(File.separator).append(p);
            mButton.putString("path", mStringBuilder.toString());
            mButton.setOnClickListener(new PathOnClickListener());
            pathButtons.addView(mButton);
        }
        
        //不能读取的才开启ROOT权限访问
        files = FileUtil.getFileList(curPath, isRoot && !path.canRead() && RootTools.isAccessGiven()); //path.listFiles();
        if(files == null)
        {
            Toast.makeText(FileBrowser.this, R.string.can_not_list_file, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            fileListAdapter = new FileListAdapter(this, R.layout.file_list, files);
            setListAdapter(fileListAdapter);
            if(lastPos > 0)
            {
                if(files.size() > lastPos)
                {
                    setSelection(lastPos);
                }else if(--lastPos < files.size()){
                    setSelection(lastPos);
                }
            }
        } catch(OutOfMemoryError ome) {
            Toast.makeText(FileBrowser.this, R.string.out_of_memory, Toast.LENGTH_LONG).show();
            return;
        }
        
    }
    
/*    private Comparator<FileLists> comparator = new Comparator<FileLists>() {
        public int compare(FileLists fl1, FileLists fl2)
        {
            return fl1.file.getName().compareToIgnoreCase(fl2.file.getName());
        }
    };*/

    protected void onListItemClick(ListView listView, View view, int position, long id)
    {
        File file = fileListAdapter.getItem(position);
        if(file.isDirectory())
        {
            showFileList(file);
        }else if(request_mode == MODE_BROWSE) {//same btnOK button
            mIntent.putExtra("path", file.getPath());
            setResult(RESULT_OK, mIntent);
            finish();
        }else {
            mIntent.putExtra("file", file.getPath());
            mIntent.putExtra("linebreak", linebreakSpinner.getSelectedItemPosition());
            mIntent.putExtra("encoding", encoding_list.getSelectedItemPosition());
            setResult(RESULT_OK, mIntent);
            finish();
        }
    }
    
    private class PathOnClickListener implements View.OnClickListener
    {
        public void onClick(View paramView)
        {
            JecButton mButton = (JecButton)paramView;
            FileBrowser.this.showFileList(new File(mButton.getString("path")));
        }
    }

}

class FileListAdapter extends ArrayAdapter<File>
{
    private LayoutInflater mInflater;
    private Bitmap mIcon_folder;
    private Bitmap mIcon_file;
    private Bitmap mIcon_image;
    private Bitmap mIcon_audio;
    private Bitmap mIcon_video;
    private Bitmap mIcon_apk;
    private ViewHolder holder;
    //各种类型图标
    private String[] type_audio = { ".m4a", ".mp3", ".wma", ".mid", ".xmf", ".ogg", ".wav" };
    private String[] type_video = { ".3gp", ".mp4", ".avi", ".rm", ".rmvb" };
    private String[] type_image = { ".jpg", ".gif", ".png", ".bmp", ".jpeg" };
    private String[] type_app = { ".apk" };
    
    public FileListAdapter(Context context, int Resource,List<File> objects) {
        super(context,Resource,objects);
        mInflater = LayoutInflater.from(context);
        Resources res = context.getResources();
        mIcon_folder = BitmapFactory.decodeResource(res, R.drawable.folder); // 文件夹的图文件
        mIcon_file = BitmapFactory.decodeResource(res, R.drawable.file); // 文件的图文件
        mIcon_image = BitmapFactory.decodeResource(res, R.drawable.image); // 图片的图文件
        mIcon_audio = BitmapFactory.decodeResource(res, R.drawable.audio); // 音频的图文件
        mIcon_video = BitmapFactory.decodeResource(res, R.drawable.video); // 视频的图文件
        mIcon_apk = BitmapFactory.decodeResource(res, R.drawable.apk); // apk文件
        
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //Log.d("FileBrowser", (convertView==null ? "convertView==null" : "convertView!=null"));
        File file = getItem(position);
        String title = file.getName();

        if(convertView == null)
        {
            /* 使用自定义的list_items作为Layout */
            convertView = mInflater.inflate(R.layout.file_list, null);
            /* 初始化holder的text与icon */
            holder = new ViewHolder();
            holder.f_title = (TextView) convertView.findViewById(R.id.f_title);
            holder.f_text = (TextView) convertView.findViewById(R.id.f_text);
            holder.f_icon = (ImageView) convertView.findViewById(R.id.f_icon);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        String f_type = getMimeType(file.getName());
        holder.f_title.setText(title);
        if (file.isDirectory())
        {
            holder.f_icon.setImageBitmap(mIcon_folder);
            
        } else
        {
            
            if ("image".equals(f_type))
            {
                holder.f_icon.setImageBitmap(mIcon_image);
            } else if ("audio".equals(f_type))
            {
                holder.f_icon.setImageBitmap(mIcon_audio);
            } else if ("video".equals(f_type))
            {
                holder.f_icon.setImageBitmap(mIcon_video);
            } else if ("apk".equals(f_type))
            {
                holder.f_icon.setImageBitmap(mIcon_apk);
            } else
            {
                holder.f_icon.setImageBitmap(mIcon_file);
            }
        }
        //底部内容 最后修改时间及大小
        StringBuilder textStringBuilder = new StringBuilder();
        //textStringBuilder.append((new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(new Date(file.lastModified())))
        //.append("   ");
        if(!"..".equals(title))
            textStringBuilder.append(TimeUtil.getDate(file.lastModified())).append("   ");
            //textStringBuilder.append(new Date(file.lastModified()).toLocaleString()).append("  ");
        if(file.length() > 0)
            textStringBuilder.append(FileUtil.byteCountToDisplaySize(file.length()));
        holder.f_text.setText(textStringBuilder.toString());

        return convertView;
    }

    private class ViewHolder
    {
        public TextView f_title;
        public TextView f_text;
        public ImageView f_icon;
    }

    /**
     * @param file
     *            文件名
     */
    private String getMimeType(String file)
    {
        for(String ext:type_audio)
        {
            if(file.endsWith(ext))
                return "audio";
        }
        for(String ext:type_video)
        {
            if(file.endsWith(ext))
                return "video";
        }
        for(String ext:type_image)
        {
            if(file.endsWith(ext))
                return "image";
        }
        for(String ext:type_app)
        {
            if(file.endsWith(ext))
                return "app";
        }

        return "";
    }

}
