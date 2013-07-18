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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jecelyin.android.compat.JecOnTextChangedListener;
import jecelyin.android.compat.TextViewBase;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jecelyin.colorschemes.ColorScheme;
import com.jecelyin.highlight.Highlight;
import com.jecelyin.util.ColorPicker;
import com.jecelyin.util.FileBrowser;
import com.jecelyin.util.FileUtil;
import com.jecelyin.util.JecLog;
import com.jecelyin.widget.JecMenu;
import com.jecelyin.widget.JecMenu.OnMenuItemSelectedListener;
import com.jecelyin.widget.SymbolGrid;
import com.jecelyin.widget.SymbolGrid.OnSymbolClickListener;
import com.jecelyin.widget.TabHost;
import com.jecelyin.widget.TabHost.OnTabChangeListener;
import com.jecelyin.widget.TabHost.OnTabCloseListener;
import com.jecelyin.widget.TabWidget;

/*import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;*/

public class JecEditor extends BaseActivity
{
    public final static int FILE_BROWSER_OPEN_CODE = 0; // 打开
    public final static int FILE_BROWSER_SAVEAS_CODE = 1; // 另存为
    public final static int SEARCH_CODE = 3;
    public final static String PREF_HISTORY = "history"; // 保存打开文件记录
    private final static String PREF_LAST_FILE = "last_files"; // 最后打开的文件
    private final static String SYNTAX_SIGN = "25";
    public static String version = "~";
    public static String TEMP_PATH = "";
    private TextViewBase mEditText;
    // SL4A
    private static final String EXTRA_SCRIPT_PATH = "com.googlecode.android_scripting.extra.SCRIPT_PATH";
    private static final String EXTRA_SCRIPT_CONTENT = "com.googlecode.android_scripting.extra.SCRIPT_CONTENT";
    private static final String ACTION_EDIT_SCRIPT = "com.googlecode.android_scripting.action.EDIT_SCRIPT";
    // end

    public static boolean isLoading = false; // 是否正在加载文件
    private static boolean fullScreen = false; // 是否已经全屏状态
    private static boolean hideToolbar = false; // 是否已经隐藏工具栏
    public static boolean isFinish = false; //是否在退出APP状态

    // button
    private ImageButton undoBtn;
    private ImageButton redoBtn;
    private ImageButton previewBtn;
    private LinearLayout findLayout;
    private LinearLayout replaceLayout;
    private Button replaceShowButton;
    private EditText findEditText;
    private EditText replaceEditText;
    private AsyncSearch mAsyncSearch;
    private SymbolGrid mSymbolGrid;
    private TabHost mTabHost;
    private CheckBox mSearchRegex;
    private CheckBox mSearchIgnoreCase;
    
    private final static String jecSign = 
            "308201a730820110a00302010202044e572379300d06092a864886f70" +
    		"d01010505003018311630140603550403130d6a6563656c79696e2070" +
    		"656e67301e170d3131303832363034333932315a170d3431303831383" +
    		"034333932315a3018311630140603550403130d6a6563656c79696e20" +
    		"70656e6730819f300d06092a864886f70d010101050003818d0030818" +
    		"9028181008a2ca8de57d513e3139bf7e390defe7411356b7234e3f281" +
    		"18ee40b219e642d89d2690d55f3d847c086dd69be3e94cd23bfb29b48" +
    		"9cf80e64d7a8de9041ff8e1937167eb89b6f36d65dfd6291a150b55b9" +
    		"7946c7c27dfa0689bba098b3ae3153917abcfde2d2ec8a38a95edda5c" +
    		"11b0b0967bff62d7e3e9839c52eeec5a8b3470203010001300d06092a" +
    		"864886f70d010105050003818100564b0a890a91d778ac02ac8af9526" +
    		"46c6992efdb78513374972f1b3facb6d764d64848b48bec407da23096" +
    		"c97a044613d24cb2bde6edcc18a72f7a51ec40ab4bf3f4d3d53410b9c" +
    		"25837c0ebae127e51081bb896d83e5bac532f301dfae01d613786b725" +
    		"2bc0c4c610b2bafd46eee97706ff905722f144cb20ea6c098e22c67d";

    // 打开文件浏览器后的回调操作
    private Runnable fileBrowserCallbackRunnable = new Runnable() {

        @Override
        public void run()
        {

        }
    };
    private HorizontalScrollView toolbar;
    private Drawable undo_can_drawable;
    private Drawable undo_no_drawable;
    private Drawable redo_can_drawable;
    private Drawable redo_no_drawable;
    private ImageButton last_edit_back;
    private ImageButton last_edit_forward;
    private Drawable last_edit_back_d;
    private Drawable last_edit_back_s;
    private Drawable last_edit_forward_d;
    private Drawable last_edit_forward_s;
    private JecMenu mMenu;
    private ArrayList<String> mLastFiles = new ArrayList<String>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        isFinish = false;
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        JecLog.d("Create");
        //优先加载
        try
        {
            initEnv();
        } catch (IOException e)
        {
            JecLog.e(e.getMessage(), e);
        }
        
        EditorSettings.addOnSharedPreferenceChanged(mPreferenceChangeListener);
        
        mTabHost = (TabHost) findViewById(R.id.tabs);
        mTabHost.initTabHost(this);
        mTabHost.addTab("");
        mEditText = mTabHost.getCurrentEditText();
        findLayout = (LinearLayout) findViewById(R.id.findlinearLayout);
        replaceLayout = (LinearLayout) findViewById(R.id.replace_linearLayout);
        replaceShowButton = (Button) findViewById(R.id.show_replace_button);
        mSearchRegex = (CheckBox) findViewById(R.id.search_regex);
        mSearchIgnoreCase = (CheckBox) findViewById(R.id.search_ignore_case);
        findEditText = (EditText) findViewById(R.id.find_editText);
        replaceEditText = (EditText) findViewById(R.id.replace_editText);
        previewBtn = (ImageButton) findViewById(R.id.preview);
        toolbar = (HorizontalScrollView) findViewById(R.id.toolbar);
        last_edit_back = (ImageButton) findViewById(R.id.last_edit_back);
        last_edit_forward = (ImageButton) findViewById(R.id.last_edit_forward);
        undo_can_drawable = getResources().getDrawable(R.drawable.undo_sel2);
        undo_no_drawable = getResources().getDrawable(R.drawable.undo_no2);
        redo_can_drawable = getResources().getDrawable(R.drawable.redo_sel2);
        redo_no_drawable = getResources().getDrawable(R.drawable.redo_no2);
        // last edit button
        last_edit_back_d = getResources().getDrawable(R.drawable.back_edit_location_d2);
        last_edit_back_s = getResources().getDrawable(R.drawable.back_edit_location_s2);
        last_edit_forward_d = getResources().getDrawable(R.drawable.forward_edit_location_d2);
        last_edit_forward_s = getResources().getDrawable(R.drawable.forward_edit_location_s2);
        // 设置横屏时不全屏编辑
        findEditText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        replaceEditText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        // 一些android 3.0设备没有菜单按钮， 要特殊处理
        /**
         *  Android 4.0, 4.0.1, 4.0.2    14  ICE_CREAM_SANDWICH
         *  Android 3.2     13  HONEYCOMB_MR2   
         *  Android 3.1.x   12  HONEYCOMB_MR1
         *  Android 3.0.x   11
         */
        mMenu = new JecMenu(JecEditor.this);
        mMenu.setOnMenuItemSelectedListener(mOnMenuItemSelectedListener);
        //尽量在平板电脑上才显示菜单按钮
        boolean showMenu = android.os.Build.VERSION.SDK_INT > 10;
        ImageButton menuButton = (ImageButton) findViewById(R.id.menu);
        if(showMenu)
        {
            menuButton.setVisibility(View.VISIBLE);
            menuButton.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v)
                {
                    closeOptionsMenu();
                    openOptionsMenu();
                    mMenu.show();
                }
            });
        }
        // end

        // 确保顺序没错
        mAsyncSearch = new AsyncSearch();

        // 最后编辑按钮事件
        mTabHost.setOnTextChangedListener(new JecOnTextChangedListener() {
            
            @Override
            public void onTextChanged(TextViewBase editText)
            {
                onEditLocationChanged(editText);
                if(editText.canUndo())
                {
                    undoBtn.setImageDrawable(undo_can_drawable);
                }else
                {
                    undoBtn.setImageDrawable(undo_no_drawable);
                }
                if(editText.canRedo())
                {
                    redoBtn.setImageDrawable(redo_can_drawable);
                }else
                {
                    redoBtn.setImageDrawable(redo_no_drawable);
                }
            }
        });

        //标签切换事件
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            
            @Override
            public void onTabChanged(int tabId)
            {
                mEditText = mTabHost.getCurrentEditText();
                String name = Highlight.getNameByExt(mEditText.getCurrentFileExt());
                switchPreviewButton(name);
            }
        });
        
        mTabHost.setOnTabCloseListener(new OnTabCloseListener() {
            
            @Override
            public void onTabClose(final int action, final int startIndex, final int curIndex)
            {
                saveConfirm(new Runnable() {
                    @Override
                    public void run()
                    {
                        int lastId = mTabHost.closeTab(curIndex);
                        //mEditText = mTabHost.getCurrentEditText();
                        if(isFinish && lastId == -1)
                        {
                            JecEditor.this.finish();
                            return;
                        }
                        if(action != TabWidget.MENU_ACTION_CLOSE_ONE)
                            mTabHost.iterCloseTab(action, startIndex, curIndex);
                    }
                });
            }
        });
        
        last_edit_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                if(mEditText.isCanBackEditLocation())
                {
                    mEditText.gotoBackEditLocation();
                    onEditLocationChanged(mEditText);
                }/*else
                {
                    Toast.makeText(JecEditor.this, R.string.not_need_back, Toast.LENGTH_LONG).show();
                }*/
            }
        });
        last_edit_forward.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                if(mEditText.isCanForwardEditLocation())
                {
                    mEditText.gotoForwardEditLocation();
                    onEditLocationChanged(mEditText);
                }/*else
                {
                    Toast.makeText(JecEditor.this, R.string.not_need_forward, Toast.LENGTH_LONG).show();
                }*/
            }
        });

        // 添加工具栏按钮
        mSymbolGrid = (SymbolGrid) findViewById(R.id.symbolGrid1);// new
                                                                  // SymbolGrid(this);
        mSymbolGrid.setClickListener(new OnSymbolClickListener() {

            @Override
            public void OnClick(String symbol)
            {
                insert_text(symbol);
            }
        });

        // 设置符号图标点击事件
        ImageButton symbolButton = (ImageButton) findViewById(R.id.symbol);
        symbolButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                mSymbolGrid.setVisibility(View.VISIBLE);
            }
        });
        // bind event
        bindEvent();
        
        //显示新版本更新日志
        String prefVer=EditorSettings.getVersion();
        if(!version.equals(prefVer))
        {
            Help.showChangesLog(this);
            EditorSettings.setString("version", version);
        }
        onNewIntent(getIntent());
        
    }
    
    @Override
    protected void onNewIntent(Intent mIntent)
    {
        try {
            doNewIntent(mIntent);
        }catch(Exception e) {
            JecLog.e(e.getMessage(), e);
        }
    }
    
    protected void doNewIntent(Intent mIntent)
    {
        //super.onNewIntent(intent);
        if(isLoading != false)
            return;
        // 处理来自其它程序通过Intent来打开文件
        //Intent mIntent = getIntent();
        if (mIntent != null 
                && (Intent.ACTION_VIEW.equals(mIntent.getAction()) 
                || Intent.ACTION_EDIT.equals(mIntent.getAction())
        )) {
            if (mIntent.getScheme().equals("content"))
            {
                try {
                    InputStream attachment = getContentResolver().openInputStream(mIntent.getData());
                    BufferedReader br = new BufferedReader( new InputStreamReader(attachment) , 8192*2 );
                    StringBuilder sb = new StringBuilder();
                    String text;
                    while((text=br.readLine()) != null)
                    {
                        sb.append(text).append("\n");
                    }
                    attachment.close();
                    br.close();
                    mTabHost.addTab("");
                    mEditText.setText(sb.toString());
                    sb.setLength(0);
                } catch (Exception e) {
                    //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    JecLog.msg(e.getMessage(), e);
                }
            }else if(mIntent.getScheme().equals("file")) {
                Uri mUri = mIntent.getData();
                String open_path = mUri != null ? mUri.getPath() : "";
                if(!"".equals(open_path) && open_path != null)
                {
                    readFileToEditText(open_path);
                }
            }
            
        }else if (mIntent != null && Intent.ACTION_SEND.equals(mIntent.getAction()) && mIntent.getExtras() != null) {
                Bundle extras = mIntent.getExtras();
                CharSequence text = extras.getCharSequence(Intent.EXTRA_TEXT);
                
                if (text != null) {
                    mTabHost.addTab("");
                    mEditText.setText(text.toString());
                } else {
                    Object stream = extras.get(Intent.EXTRA_STREAM);
                    if(stream != null && stream instanceof Uri)
                    {
                        readFileToEditText(((Uri)stream).getPath());
                    }
                }

        } else if (mIntent != null && ACTION_EDIT_SCRIPT.equals(mIntent.getAction()) && mIntent.getExtras() != null) {
            Bundle extras = mIntent.getExtras();
            String path = extras.getString(EXTRA_SCRIPT_PATH);
            CharSequence contents = extras.getCharSequence(EXTRA_SCRIPT_CONTENT);
            if (contents != null) {
                mTabHost.addTab("");
                mEditText.setText(contents);
            } else {
                if (path != null) {
                    readFileToEditText(path);
                }
            }
        }else
        {
            // 打开上次打开的文件
            if(EditorSettings.OPEN_LAST_FILES)
            {
                //在onLoaded处理最后位置
                
                SharedPreferences sp = getSharedPreferences(PREF_LAST_FILE, MODE_PRIVATE);
                Map<String, ?> map = sp.getAll();
                if(map.size() > 0)
                {
                    for (Entry<String, ?> entry : map.entrySet())
                    {
                        Object val = entry.getValue();
                        if (val instanceof String) {
                            //readFileToEditText((String)val);
                            mLastFiles.add((String)val);
                        }
                    }
                    loadLastOpenFiles();
                }
            }
        }
        
    }
    
    /**
     * 递归方式打开最后打开的文件列表
     */
    private void loadLastOpenFiles()
    {
        if(mLastFiles.size() < 1)
            return;
        String file = mLastFiles.remove(0);
        readFileToEditText(file);
    }

    /**
     * 初始化一些重要的环境
     * @throws IOException 
     */
    private void initEnv() throws IOException
    {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        {
            TEMP_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/.920TextEditor";
        }else
        {
            TEMP_PATH = getFilesDir().getAbsolutePath() + "/.920TextEditor";
        }

        File temp = new File(TEMP_PATH);
        if(!temp.isDirectory() && !temp.mkdir())
        {
            alert(R.string.can_not_create_temp_path);
            // return;
        }
        // 解压语法文件
        String synfilestr = TEMP_PATH + "/version";
        File synsignfile = new File(synfilestr);
        if(!synsignfile.isFile())
        {
            if(!unpackSyntax())
            {
                alert(R.string.can_not_create_synfile);
                // return;
            }else
            {
                FileUtil.writeFile(synfilestr, SYNTAX_SIGN);
            }
        }else
        {
            if(!SYNTAX_SIGN.equals(FileUtil.readFileAsString(synfilestr, "utf-8")))
            {
                if(!unpackSyntax())
                {
                    alert(R.string.can_not_create_synfile);
                    // return;
                }else
                {
                    FileUtil.writeFile(synfilestr, SYNTAX_SIGN);
                }
            }
        }

        ColorScheme.init();
        Highlight.init();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    protected void onEditLocationChanged(TextViewBase editText)
    {
        if(editText.isCanBackEditLocation())
        {
            last_edit_back.setImageDrawable(last_edit_back_s);
        }else
        {
            last_edit_back.setImageDrawable(last_edit_back_d);
        }
        if(editText.isCanForwardEditLocation())
        {
            last_edit_forward.setImageDrawable(last_edit_forward_s);
        }else
        {
            last_edit_forward.setImageDrawable(last_edit_forward_d);
        }
    }


    class ColorListener implements ColorPicker.OnColorChangedListener
    {
        @Override
        public void onColorChanged(String key, String color)
        {
            insert_text(color);
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        JecLog.d("Resume");

        // 按HOME键后，再点击程序图标恢复程序，不会执行onRestoreInstanceState，所以这里要处理一下
        isLoading = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {
        //isLoading = true;
        super.onSaveInstanceState(savedInstanceState);
        // 自动保存当前文档
        if(EditorSettings.AUTO_SAVE && mEditText.isTextChanged())
        {
            save();
            //Toast.makeText(this, R.string.has_autosave, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        try
        {
            //isLoading = false;
            super.onRestoreInstanceState(savedInstanceState);
        }catch (Exception e)
        {
            JecLog.msg(e.getMessage(), e);
        }
    }
    
    protected void onStop() {
        if(!isFinish)
            saveHistory();
        // 自动保存当前文档
        if(EditorSettings.AUTO_SAVE && mEditText.isTextChanged())
        {
            save();
            //Toast.makeText(this, R.string.has_autosave, Toast.LENGTH_LONG).show();
        }
        super.onStop();
    }
    
    public void onFinish()
    {
        isFinish = true;
        saveHistory();
        mTabHost.setAutoNewTab(false);
        int count = mTabHost.getTabCount();
        if(count < 1)
        {
            finish();
            return;
        }
        mTabHost.setCurrentTab(count-1);
        mTabHost.iterCloseTab(TabWidget.MENU_ACTION_CLOSE_ALL, 0, count);
    }
    
    /**
     * finish后，彻底退出程序
     */
    public void onDestroy()
    {
        super.onDestroy();

        /*
         * Notify the system to finalize and collect all objects of the
         * application on exit so that the process running the application can
         * be killed by the system without causing issues. NOTE: If this is set
         * to true then the process will not be killed until all of its threads
         * have closed.
         */
        System.runFinalizersOnExit(true);

        /*
         * Force the system to close the application down completely instead of
         * retaining it in the background. The process that runs the application
         * will be killed. The application will be completely created as a new
         * application in a new process if the user starts the application
         * again.
         */
        System.exit(0);
    }
    
    public TextViewBase getEditText()
    {
        return mTabHost.getCurrentEditText();
    }
    
    public TabHost getTabHost()
    {
        return mTabHost;
    }

    private OnSharedPreferenceChangeListener mPreferenceChangeListener = new OnSharedPreferenceChangeListener()
    {
        
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if("screen_orientation".equals(key))
            {
                String screen_orientation = EditorSettings.SCREEN_ORIENTATION;
                if("portrait".equals(screen_orientation))
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else if("landscape".equals(screen_orientation))
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            ColorScheme.init();
            Highlight.loadColorScheme();
            init_highlight();
        }
    };
    
    private void init_highlight()
    {
        if(mEditText == null)
            return;
        getEditText().reHighlight();
        if(!EditorSettings.ENABLE_HIGHLIGHT)
        {
            getEditText().getEditableText().clearSpans();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        int ctrlKeyCode = 8 | 0x1000;
        int keycode = event.getKeyCode();
        // CTRL + KEYDOWN
        int meta = (int)event.getMetaState();
        boolean ctrl = (meta & ctrlKeyCode) != 0 ;
        if(ctrl)
        {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keycode == KeyEvent.KEYCODE_S )
            {
                save();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                if(isLoading)
                {
                    break;
                }else if(mSymbolGrid.isShown())
                {
                    mSymbolGrid.setVisibility(View.GONE);
                }else if(findLayout.getVisibility() == View.VISIBLE)
                {
                    findLayout.setVisibility(View.GONE);
                    replaceLayout.setVisibility(View.GONE);
                }else if(EditorSettings.BACK_BUTTON_EXIT)
                {
                    onFinish();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(!hideToolbar)
                {
                    toolbar.setVisibility(View.GONE);
                    hideToolbar = true;
                    Toast.makeText(this, R.string.volume_up_toolbar_msg, Toast.LENGTH_LONG).show();
                }else if(!fullScreen)
                {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
                    fullScreen = true;
                    Toast.makeText(this, R.string.volume_up_fullscreen_msg, Toast.LENGTH_LONG).show();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(hideToolbar)
                {
                    toolbar.setVisibility(View.VISIBLE);
                    hideToolbar = false;
                }else if(fullScreen)
                {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    fullScreen = false;
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_SEARCH: // 查找按钮
                if(findLayout.getVisibility() == View.GONE)
                {
                    findLayout.setVisibility(View.VISIBLE);
                    replaceShowButton.setVisibility(View.VISIBLE);
                }
                find("next");
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void bindEvent()
    {
        ImageButton btnOpen = (ImageButton) findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(onBtnOpenClicked);
        ImageButton btnSave = (ImageButton) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(onBtnSaveClicked);
        bindUndoButtonClickEvent();
        bindRedoButtonClickEvent();

        replaceShowButton.setOnClickListener(replaceShowClickListener);
        // 搜索相关
        ImageButton findNext = (ImageButton) findViewById(R.id.find_next_imageButton);
        ImageButton findBack = (ImageButton) findViewById(R.id.find_back_imageButton);
        findNext.setOnClickListener(findButtonClickListener);
        findBack.setOnClickListener(findButtonClickListener);
        // replace
        Button replaceButton = (Button) findViewById(R.id.replace_button);
        Button replaceAllButton = (Button) findViewById(R.id.replace_all_button);
        replaceButton.setOnClickListener(replaceClickListener);
        replaceAllButton.setOnClickListener(replaceClickListener);

        previewBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                if("".equals(mEditText.getPath()) || mEditText.isTextChanged())
                {
                    Toast.makeText(JecEditor.this, R.string.preview_msg, Toast.LENGTH_LONG).show();
                    return;
                }
                try
                {
                    Uri uri = Uri.fromFile(new File(mEditText.getPath()));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "text/html");
                    startActivity(intent);
                }catch (Exception e)
                {
                    JecLog.e(e.getMessage(), e);
                }
                
            }
        });
        ImageButton colorButton = (ImageButton) findViewById(R.id.color);
        colorButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ColorPicker cp = new ColorPicker(JecEditor.this, new ColorListener(), "edittext", JecEditor.this.getString(R.string.insert_color), Color.GREEN);
                cp.show();
            }
        });
        ImageButton foldersearchButton = (ImageButton) findViewById(R.id.search_file);
        foldersearchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                if(mEditText != null) {
                    intent.putExtra("keyword", mEditText.getText().subSequence(mEditText.getSelectionStart(), mEditText.getSelectionEnd()).toString());
                    intent.putExtra("path", mEditText.getPath());
                }else {
                    intent.putExtra("keyword", "");
                    intent.putExtra("path", "");
                }
                intent.putExtra("isRoot", EditorSettings.TRY_ROOT);
                intent.setClass(JecEditor.this, Grep.class);
                startActivityForResult(intent, SEARCH_CODE);
            }
        });
    }

    /**
     * 查找
     * 
     * @param direction
     *            next or back
     */
    public void find(String direction)
    {
        String keyword = findEditText.getText().toString();
        if("".equals(keyword))
            return;
        if(mAsyncSearch == null)
            mAsyncSearch = new AsyncSearch(); //不应该有这情况，测试一下
        mAsyncSearch.regex = mSearchRegex.isChecked();
        mAsyncSearch.ignorecase = mSearchIgnoreCase.isChecked();
        if("back".equals(direction))
        {
            mAsyncSearch.search(keyword, false, JecEditor.this);
        }else
        {
            mAsyncSearch.search(keyword, true, JecEditor.this);
        }
    }

    private OnClickListener findButtonClickListener = new OnClickListener() {

        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case R.id.find_next_imageButton:
                    find("next");
                    break;
                case R.id.find_back_imageButton:
                    find("back");
                    break;
            }
        }
    };

    private OnClickListener replaceClickListener = new OnClickListener() {

        @Override
        public void onClick(View v)
        {
            String searchText = findEditText.getText().toString();
            String replaceText = replaceEditText.getText().toString();
            if("".equals(searchText))
            {
                return;
            }
            mAsyncSearch.regex = mSearchRegex.isChecked();
            mAsyncSearch.ignorecase = mSearchIgnoreCase.isChecked();
            switch(v.getId())
            {
                case R.id.replace_button:
                    mAsyncSearch.replace(replaceText);
                    break;
                case R.id.replace_all_button:
                    mAsyncSearch.replaceAll(searchText, replaceText, JecEditor.this);
                    break;
            }
        }
    };

    private OnClickListener replaceShowClickListener = new OnClickListener() {

        @Override
        public void onClick(View v)
        {
            replaceLayout.setVisibility(View.VISIBLE);
            v.setVisibility(View.GONE);
            replaceEditText.requestFocus();
        }
    };

    private void bindUndoButtonClickEvent()
    {
        undoBtn = (ImageButton) findViewById(R.id.undo);
        // undoBtn.
        undoBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View paramView)
            {
                mEditText.unDo();
            }
        });
    }

    private void bindRedoButtonClickEvent()
    {
        redoBtn = (ImageButton) findViewById(R.id.redo);
        redoBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View paramView)
            {
                mEditText.reDo();
            }
        });

    }


    public void scrollToTop()
    {
        mEditText.scrollTo(0, 0);
    }

    /**
     * 警告并退出程序
     */
    public void alert(int msg)
    {
        new AlertDialog.Builder(this).setMessage(msg).setPositiveButton(R.string.yes, // 保存
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        JecEditor.this.finish();
                    }
                }).show();
    }

    /**
     * 是否要进行保存
     * 
     * @return 返回true则需要保存
     */
    public void saveConfirm(final Runnable mRunnable)
    {
        if(!mEditText.isTextChanged())
        {// 内容没有改变
            mRunnable.run();
            return;
        }

        String filename = "".equals(mEditText.getPath()) ? mEditText.getTitle() : mEditText.getPath();
        String msg = String.format(getString(R.string.save_changes_to), filename);
        new AlertDialog.Builder(this).setTitle(R.string.save_changes).setMessage(msg).setPositiveButton(R.string.yes, // 保存
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if("".equals(mEditText.getPath()))
                        {
                            openFileBrowser(FILE_BROWSER_SAVEAS_CODE, getString(R.string.new_filename), mRunnable);
                            return;
                        }
                        save();
                        dialog.dismiss();
                        mRunnable.run();
                    }
                }).setNeutralButton(R.string.no, // 放弃
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        mRunnable.run();
                    }
                }).setNegativeButton(R.string.cancel, // 取消
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        JecEditor.isFinish = false;
                    }
                }).show();

    }
    
    private void save()
    {
        save(mEditText.getEncoding(), mEditText.getLineBreak());
    }

    private void save(String encoding, int linebreak)
    {
        if("".equals(mEditText.getPath()) || isLoading)
            return;
        
        String content = mEditText.getString();
        if("".equals(encoding))
            encoding = "utf-8";
        boolean ok = true;

        String failMsg = "";
        
        try
        {
            ok = FileUtil.writeFile(mEditText.getPath(), content, encoding, linebreak, EditorSettings.TRY_ROOT);
        }catch (Exception e)
        {
            failMsg = e.getMessage();
            ok = false;
        }
        
        if(ok)
        {
            mEditText.updateTextFinger();
            mTabHost.setTabStatus(false);
            Toast.makeText(JecEditor.this, R.string.save_succ, Toast.LENGTH_LONG).show();
        }else
        {
            Toast.makeText(JecEditor.this, JecEditor.this.getString(R.string.save_failed)+failMsg, Toast.LENGTH_LONG).show();
        }
    }

    private OnClickListener onBtnSaveClicked = new OnClickListener() {

        @Override
        public void onClick(View v)
        {
            if("".equals(mEditText.getPath()))
            {
                openFileBrowser(FILE_BROWSER_SAVEAS_CODE, "Untitled.txt");
                return;
            }
            save();
        }
    };

    private OnClickListener onBtnOpenClicked = new OnClickListener() {

        @Override
        public void onClick(View v)
        {
            openFileBrowser(FILE_BROWSER_OPEN_CODE, "");
        }
    };
    

    private void openFileBrowser(int mode, String filename)
    {
        openFileBrowser(mode, filename, new Runnable() {
            @Override
            public void run()
            {
            }
        });
    }

    /**
     * 打开文件浏览器
     * 
     * @param mode
     *            0打开， 1保存模式
     * @param filename
     * @param mRunnable
     */
    private void openFileBrowser(int mode, String filename, Runnable mRunnable)
    {
        fileBrowserCallbackRunnable = mRunnable;
        Intent intent = new Intent();
        intent.putExtra("filename", filename);
        intent.putExtra("mode", mode);
        intent.putExtra("isRoot", EditorSettings.TRY_ROOT);
        intent.setClass(JecEditor.this, FileBrowser.class);
        startActivityForResult(intent, mode);
    }

    /**
     * startActivityForResult回调函数
     * 
     * @param requestCode
     *            这里的requestCode就是前面启动新Activity时的带过去的requestCode
     * @param resultCode
     *            resultCode则关联上了setResult中的resultCode
     * @param data
     *            返回的Intent参数
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(RESULT_OK != resultCode)
        {
            return;
        }
        
        final String path;
        final int lineBreak;
        final int encoding;
        final String charset;
        
        switch(requestCode)
        {
            case FILE_BROWSER_OPEN_CODE: // 打开
                path = data.getStringExtra("file");
                lineBreak = data.getIntExtra("linebreak", 0);
                encoding = data.getIntExtra("encoding", 0);

                if(encoding < 1)
                {
                    charset = "";
                } else {
                    charset = EncodingList.list[encoding];
                }
                readFileToEditText(path, charset, lineBreak, 0, 0);
                break;
            case FILE_BROWSER_SAVEAS_CODE:
                isLoading = false;
                path = data.getStringExtra("file");
                lineBreak = data.getIntExtra("linebreak", 0);
                encoding = data.getIntExtra("encoding", 0);
                
                if(encoding < 1)
                {
                    charset = "";
                } else {
                    charset = EncodingList.list[encoding];
                }
                
                final File file = new File(path);
                if(file.exists())
                {
                    new AlertDialog.Builder(this).setMessage(getText(R.string.overwrite_confirm)).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            mEditText.setPath(path);
                            setTitle(file.getName());
                            save(charset, lineBreak);
                        }
                    }).setNegativeButton(android.R.string.no, null).show();
                }else
                {
                    mEditText.setPath(path);
                    setTitle(file.getName());
                    save(charset, lineBreak);
                }

                break;
            case SEARCH_CODE:
                path = data.getStringExtra("file");
                int offset = (int)data.getLongExtra("offset", 0);
                //readFileToEditText(path, encoding, linebreak, selstart, selend);
                readFileToEditText(path, "", 0, offset, offset); //
                break;
        }
        fileBrowserCallbackRunnable.run();
    }

    public void onLoaded(int selstart, int selend)
    {
        
        String msg = getString(R.string.encoding) + ": " + mEditText.getEncoding();
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        mEditText.resetUndoStatus();
        String filename = new File(mEditText.getPath()).getName();
        setTitle(filename);
        String name = Highlight.getNameByExt(mEditText.getCurrentFileExt());
        switchPreviewButton(name);
        //记住最后位置
        SharedPreferences sp = getSharedPreferences(PREF_HISTORY, MODE_PRIVATE);
        if(selstart==0 && selend==0)
        {
            String[] selinfo = sp.getString(mEditText.getPath(), "").split(",");
            if(selinfo.length >= 3)
            {
                selstart = Integer.valueOf(selinfo[0]);
                selend = Integer.valueOf(selinfo[1]);
                if(selstart >= mEditText.length() || selend >= mEditText.length() || selstart < 0 || selend < 0){
                    selstart = 0;
                    selend = 0;
                }
                mEditText.setSelection(selstart, selend);
            }
        }
        //注意顺序
        saveHistory();
        loadLastOpenFiles();
    }

    public void setTitle(String title)
    {
        super.setTitle(title);
        mTabHost.setTitle(title);
    }


/*    public void removeHighlight()
    {
        Editable text = text_content.getText();
        text.clearSpans();
        // 重新设置文本，不然会产生无法滚动和光标不闪烁或光标不可见的问题
        text_content.setText(text);
        text_content.invalidate();
    }*/

    /**
     * 解压语法配置文件
     * 
     * @return
     */
    public boolean unpackSyntax()
    {
        try
        {
            InputStream is = getAssets().open("syntax.zip");
            ZipInputStream zin = new ZipInputStream(is);
            ZipEntry ze = null;
            String name;
            File file;
            while ((ze = zin.getNextEntry()) != null)
            {
                name = ze.getName();
                // Log.v("Decompress", "Unzipping " + name);

                if(ze.isDirectory())
                {
                    file = new File(TEMP_PATH + File.separator + name);
                    if(!file.exists())
                    {
                        if(!file.mkdir())
                        {
                            return false;
                        }
                    }
                }else
                {
                    FileOutputStream fout = new FileOutputStream(TEMP_PATH + File.separator + name);
                    byte[] buf = new byte[1024 * 4];
                    int len;
                    while ((len = zin.read(buf)) > 0)
                    {
                        fout.write(buf, 0, len);
                    }
                    buf = null;
                    zin.closeEntry();
                    fout.close();
                }

            }
            zin.close();
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void switchPreviewButton(String type)
    {
        if(type.toUpperCase().startsWith("HTML"))
        {
            previewBtn.setVisibility(View.VISIBLE);
        }else
        {
            previewBtn.setVisibility(View.GONE);
        }
    }

    public void setEncoding(String encoding)
    {
        try
        {
            byte[] bytes = mEditText.getString().getBytes(mEditText.getEncoding());
            mEditText.setText(new String(bytes, encoding));
            mEditText.setEncoding(encoding);
            //doHighlight(mEditText.getCurrentFileExt());
        }catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }catch(OutOfMemoryError ome) {
            Toast.makeText(this, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
        }
    }
    
    public void readFileToEditText(String path)
    {
        SharedPreferences sp = getSharedPreferences(PREF_HISTORY, MODE_PRIVATE);
        String[] selinfo = sp.getString(path, "").split(",");
        int linebreak=0;
        String encoding = "";
        int selstart=0;
        int selend=0;
        if(selinfo.length >= 5)
        {
            linebreak = Integer.valueOf(selinfo[3]);
            encoding = selinfo[4];
            selstart = Integer.valueOf(selinfo[0]);
            selend = Integer.valueOf(selinfo[1]);
        }
        //readFileToEditText(path, encoding, linebreak, selstart, selend);
        //修正另存为其它编码后，在历史列表打开会乱码的问题
        readFileToEditText(path, "", linebreak, selstart, selend);
    }

    public void readFileToEditText(String path, String encoding, int lineBreak, int selstart, int selend)
    {
        if("".equals(path))
            return;
        
        try
        {
            Signature[] sign = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            String sig = sign[0].toCharsString();
            if(!jecSign.equals(sig))
            {
                Toast.makeText(this, R.string.invalid_sign, Toast.LENGTH_LONG).show();
            }
        }catch (NameNotFoundException e)
        {
            JecLog.e(e.getMessage(), e);
        }
        // text_content.setText("");
        // text_content.resetUndoStatus();
        //current_path_tmp = path;
        //current_ext_tmp = FileUtil.getExt(path);
        mTabHost.addTab(path);
        new AsyncReadFile(JecEditor.this, path, encoding, lineBreak, selstart, selend);
        // String content = FileUtil.Read(path, encoding);
        // text_content.setText(content);
        // saveHistory();
    }

    public void insert_text(String text)
    {
        if(mEditText == null)
            return;
        int start = mEditText.getSelectionStart();
        int end = mEditText.getSelectionEnd();

        if(start < 0 || end < 0 || !mEditText.isFocused())
            return;
        mEditText.getEditableText().replace(Math.min(start, end), Math.max(start, end), text, 0, text.length());

    }

    /**
     * EditText菜单
     */
    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == mEditText.getId())
        {
            MenuHandler handler = new MenuHandler();
            // 跳转到指定行
            menu.add(0, R.id.go_to_begin, 0, R.string.go_to_begin).setOnMenuItemClickListener(handler);
            // 跳转到指定行
            menu.add(0, R.id.go_to_end, 0, R.string.go_to_end).setOnMenuItemClickListener(handler);
            // 跳转到指定行
            menu.add(0, R.id.goto_line, 0, R.string.goto_line).setOnMenuItemClickListener(handler);
            // 转为小写
            menu.add(0, R.id.to_lower, 0, R.string.to_lower).setOnMenuItemClickListener(handler);
            // 转为大写
            menu.add(0, R.id.to_upper, 0, R.string.to_upper).setOnMenuItemClickListener(handler);
            // 插入时间
            menu.add(0, R.id.insert_datetime, 0, getString(R.string.insert_datetime)+TimeUtil.getDate()).setOnMenuItemClickListener(handler);
            if(mHideSoftKeyboard)
            {
                // 显示输入法
                menu.add(0, R.id.show_ime, 0, R.string.show_ime).setOnMenuItemClickListener(handler);
            } else {
                // 隐藏输入法
                menu.add(0, R.id.hide_ime, 0, R.string.hide_ime).setOnMenuItemClickListener(handler);
            }
            

        }
    }*/

    /*private class MenuHandler implements MenuItem.OnMenuItemClickListener
    {
        public boolean onMenuItemClick(MenuItem item)
        {
            int itemId = item.getItemId();
            switch(itemId)
            {
                case R.id.show_ime:
                    showIME(true);
                    break;
                case R.id.hide_ime:
                    showIME(false);
                    break;
                case R.id.to_lower:
                case R.id.to_upper:
                    int start = mEditText.getSelectionStart();
                    int end = mEditText.getSelectionEnd();
                    if(start == end)
                        break;
                    try
                    {
                        Editable mText = mEditText.getText();
                        char[] dest = new char[end - start];
                        mText.getChars(start, end, dest, 0);
                        if(itemId == R.id.to_lower)
                        {
                            mText.replace(start, end, (new String(dest)).toLowerCase());
                        }else
                        {
                            mText.replace(start, end, (new String(dest)).toUpperCase());
                        }
                    }catch (Exception e)
                    {
                        printException(e);
                    }
                    break;
                case R.id.goto_line:
                    final EditText lineEditText = new EditText(JecEditor.this);
                    lineEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    AlertDialog.Builder builder = new AlertDialog.Builder(JecEditor.this);
                    builder.setTitle(R.string.goto_line).setView(lineEditText).setNegativeButton(android.R.string.cancel, null);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                CharSequence lineCharSequence = lineEditText.getText();
                                int line = Integer.valueOf(lineCharSequence.toString());
                                if(!mEditText.gotoLine(line))
                                {
                                    Toast.makeText(JecEditor.this, R.string.can_not_gotoline, Toast.LENGTH_LONG).show();
                                }else
                                {
                                    dialog.dismiss();
                                }
                            }catch (Exception e)
                            {
                                printException(e);
                            }
                        }
                    });
                    builder.show();
                case R.id.go_to_begin:
                    mEditText.setSelection(0, 0);
                    break;
                case R.id.go_to_end:
                    int len = mEditText.getText().length();
                    mEditText.setSelection(len, len);
                    break;
                case R.id.insert_datetime:
                    insert_text(TimeUtil.getDate());
                    break;
            }

            return true; // true表示完成当前item的click处理，不再传递到父类处理
        }
    }*/
    
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //Toast.makeText(this, "onCreateOptionsMenu", Toast.LENGTH_LONG).show();
        //必须有菜单被创建，不然点menu按钮，只弹出一次菜单
        getMenuInflater().inflate(R.menu.main, menu);

        mMenu.show();
        return true;
    }
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        //Toast.makeText(this, "onMenuOpened", Toast.LENGTH_LONG).show();
        mMenu.show();
        return false;// 返回为true 则显示系统menu
    }

    private OnMenuItemSelectedListener mOnMenuItemSelectedListener = new OnMenuItemSelectedListener() {
        
        @Override
        public boolean onMenuItemSelected(int id, View v)
        {
            switch(id)
            {
                case R.id.menu_reopen:
                    new HistoryList(JecEditor.this);
                    break;
                case R.id.menu_highlight:
                    new LangList(JecEditor.this);
                    break;
                case R.id.menu_encoding:
                    new EncodingList(JecEditor.this);
                    break;
                case R.id.menu_saveas:
                    openFileBrowser(1, mEditText.getTitle());
                    break;
                case R.id.menu_search_replace:
                    findLayout.setVisibility(View.VISIBLE);
                    replaceShowButton.setVisibility(View.VISIBLE);
                    break;
                case R.id.menu_pipe:
                    final String [] items;
                    // Python, Perl, JRuby, Lua, BeanShell, JavaScript, Tcl, and shell are currently supported
                    String ext = mEditText.getCurrentFileExt();
                    if("py".equals(ext) || "pl".equals(ext) || "lua".equals(ext) || "sh".equals(ext) || "js".equals(ext) || "tcl".equals(ext))
                    {
                        items=new String []{
                            getString(R.string.view)
                            ,getString(R.string.share)
                            ,getString(R.string.run_in_sl4a_terminal)
                            ,getString(R.string.run_in_sl4a_background)
                         };
                    } else {
                        items=new String []{
                            getString(R.string.view)
                            ,getString(R.string.share)
                         };
                    }
                    
                    AlertDialog.Builder builder=new AlertDialog.Builder(JecEditor.this);
                    builder.setTitle(R.string.open_mode);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent   = new Intent();
                            if(getString(R.string.view).equals(items[which]))
                            {//view
                                String file = mEditText.getPath();
                                if("".equals(file))
                                {
                                    Toast.makeText(JecEditor.this, R.string.preview_msg, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Uri uri = Uri.parse("file://"+file);
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, "*/*");
                            }else if(getString(R.string.share).equals(items[which])) {
                                //send text
                                intent.setAction(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                int selstart = mEditText.getSelectionStart();
                                int selend = mEditText.getSelectionEnd();
                                String text;
                                if(selend != selstart)
                                {
                                    //has selection text
                                    text=mEditText.getText().subSequence(selstart, selend).toString();
                                } else {
                                    text=mEditText.getString();
                                }
                                intent.putExtra(Intent.EXTRA_TEXT, text);
                            } else if(getString(R.string.run_in_sl4a_background).equals(items[which]) || getString(R.string.run_in_sl4a_terminal).equals(items[which])) {
                                String file = mEditText.getPath();
                                if("".equals(file))
                                {
                                    Toast.makeText(JecEditor.this, R.string.run_before_save_file, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                ComponentName SL4A_SERVICE_LAUNCHER_COMPONENT_NAME = new ComponentName("com.googlecode.android_scripting", "com.googlecode.android_scripting.activity.ScriptingLayerServiceLauncher");
                                intent.setComponent(SL4A_SERVICE_LAUNCHER_COMPONENT_NAME);
                                if(getString(R.string.run_in_sl4a_terminal).equals(items[which])) {
                                    intent.setAction("com.googlecode.android_scripting.action.LAUNCH_FOREGROUND_SCRIPT");
                                    intent.putExtra("com.googlecode.android_scripting.extra.SCRIPT_PATH", file);
                                } else {
                                    intent.setAction("com.googlecode.android_scripting.action.LAUNCH_BACKGROUND_SCRIPT");
                                    intent.putExtra("com.googlecode.android_scripting.extra.SCRIPT_PATH", file);
                                }
                            }
                            try
                            {
                                startActivity(intent);
                            }catch (Exception e)
                            {
                                //Toast.makeText(JecEditor.this, "Exception: "+e.getMessage(), Toast.LENGTH_LONG).show();
                                JecLog.e(e.getMessage(), e);
                            }
                        }
                    });

                    builder.show();

                    break;
                case R.id.menu_preferences:
                    Intent intent = new Intent(JecEditor.this, EditorPreference.class);
                    startActivity(intent);
                    break;
                case R.id.menu_exit:
                    JecEditor.this.onFinish();
                    break;
            }
            return true;
        }
    };

    private void saveHistory()
    {
        SharedPreferences sp;
        Editor editor;
        if(mEditText.getPath() != null && !"".equals(mEditText.getPath()))
        {
            int selstart = mEditText.getSelectionStart();
            int selend = mEditText.getSelectionEnd();

            sp = getSharedPreferences(PREF_HISTORY, MODE_PRIVATE);
            editor = sp.edit();
            editor.putString(mEditText.getPath(), String.format("%d,%d,%d,%d,%s", selstart, selend, System.currentTimeMillis(), mEditText.getLineBreak(), mEditText.getEncoding()));
            editor.commit();
        }
        
        sp = getSharedPreferences(PREF_LAST_FILE, MODE_PRIVATE);
        editor = sp.edit();
        editor.clear();
        //mPref.edit().putString("last_file", mEditText.getPath()).commit();
        ArrayList<String> paths = mTabHost.getAllPath();
        //按反序保存，打开后才和之前的顺序一样
        int size = paths.size();
        //for(String path : paths)
        String path;
        for(int i=0; i<size; i++)
        {
            path = paths.get(i);
            editor.putString(path, path);
        }
        editor.commit();
    }

}
