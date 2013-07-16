package com.jecelyin.editor;

import java.util.ArrayList;

import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.jecelyin.util.JecLog;
import com.jecelyin.util.LinuxShell;

public class EditorSettings
{
    private static Context mContext;
    private static SharedPreferences mPrefs;

    // 非设置部分 start-------
    public final static int LAST_EDIT_DISTANCE_LIMIT = 20; // 最后编辑位置距离限制，不做同行判断
    public static boolean MULTI_TOUCH = true;

    /** 缩小时最小字体 */
    public static final float MIN_TEXT_SIZE = 10f;
    /** 放大时最大字体 */
    public static final float MAX_TEXT_SIZE = 32.0f;
    // end

    public static boolean USE_DEFAULT_TOOLBAR = true;
    public static boolean USE_SPACE_FOR_TAB = false;
    public static boolean CUSTORM_HIGHLIGHT_COLOR = false;
    public static int INDENT_SIZE = 4;
    public static String INDENT_STRING = "\t";
    public static boolean OPEN_LAST_FILES = false;
    public static boolean TRY_ROOT = false;
    public static boolean ENABLE_TOUCH_ZOOM = true;
    public static boolean READONLY_MODE = false;
    public static boolean AUTO_SAVE = false;
    public static boolean BACK_BUTTON_EXIT = true;
    public static boolean USE_CUSTOM_FORMAT = false;
    public static boolean ENABLE_HIGHLIGHT = true;
    public static boolean WORD_WRAP = true;
    public static boolean SHOW_LINENUM = true;
    public static boolean SHOW_WHITESPACE = false;
    public static boolean AUTO_INDENT = false;
    public static boolean KEEP_SCREEN_ON = false;
    public static boolean SPELLCHECK = false;
    public static boolean AUTO_CAPITALIZE = false;

    public static String HIGHLIGHT_FONT = "#000000";;
    public static String HIGHLIGHT_BACKGROUP = "#ffffff";
    public static String HIGHLIGHT_STRING = "#008800";
    public static String HIGHLIGHT_KEYWORD = "#000088";
    public static String HIGHLIGHT_COMMENT = "#3F7F5F";
    public static String HIGHLIGHT_TAG = "#800080";
    public static String HIGHLIGHT_ATTR_NAME = "#FF0000";
    public static String HIGHLIGHT_FUNCTION = "#000080";
    public static String HIGHLIGHT_LAST_PATH = "";
    public static String HIGHLIGHT_COLOR_SCHEME = "";
    public static String FONT = "Monospace";
    public static String DATE_FORMAT = "0";
    public static String SYS_DATE_FORMAT = "0";
    public static String CUSTOM_DATE_FORMAT = "0";
    public static String SCREEN_ORIENTATION = "auto";
    public static int FONT_SIZE = 12;
    public static int LIMIT_HIGHLIGHT_LENGTH = 400 * 1024; // unit: kb
    public static int CURSOR_WIDTH = 2;

    private static ArrayList<OnSharedPreferenceChangeListener> mListeners = new ArrayList<SharedPreferences.OnSharedPreferenceChangeListener>();
    public static void initialize(final Context context)
    {
        mContext = context.getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPrefs.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        onPreferenceChanged(mPrefs, null);

        // 判断设备是否支持多点触摸
        PackageManager pm = context.getPackageManager();
        MULTI_TOUCH = pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT);
    }

    public static SharedPreferences getInstance(String name)
    {
        return mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
    
    public static void addOnSharedPreferenceChanged(OnSharedPreferenceChangeListener l)
    {
        mListeners.add(l);
    }

    static OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener()
    {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            onPreferenceChanged(sharedPreferences, key); //先要保证本类是最新的，才能让其它类用
            int size = mListeners.size();
            for (int i=0; i<size; i++)
            {JecLog.d("CC "+mListeners.get(i));
                mListeners.get(i).onSharedPreferenceChanged(sharedPreferences, key);
            }
        }
    };

    private static void onPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key == null || "use_default_toolbar_settings".equals(key))
            USE_DEFAULT_TOOLBAR = mPrefs.getBoolean("use_default_toolbar_settings", USE_DEFAULT_TOOLBAR);

        if (key == null || "use_space_for_tab".equals(key))
        {
            USE_SPACE_FOR_TAB = mPrefs.getBoolean("use_space_for_tab", USE_SPACE_FOR_TAB);
            INDENT_STRING = null;
        }

        if (key == null || "use_custom_hl_color".equals(key))
            CUSTORM_HIGHLIGHT_COLOR = mPrefs.getBoolean("use_custom_hl_color", CUSTORM_HIGHLIGHT_COLOR);

        if (key == null || INDENT_STRING == null || "indent_size".equals(key))
        {
            int size = Integer.valueOf(mPrefs.getString("indent_size", "4"));
            INDENT_SIZE = size > 0 && size < 12 ? size : 4;
            INDENT_STRING = USE_SPACE_FOR_TAB ? new String(new char[INDENT_SIZE]).replace("\0", " ") : "\t";
        }

        if (key == null || "open_last_file".equals(key))
            OPEN_LAST_FILES = mPrefs.getBoolean("open_last_file", OPEN_LAST_FILES);

        if (key == null || "get_root".equals(key))
        {
            TRY_ROOT = mPrefs.getBoolean("get_root", TRY_ROOT);
            if(TRY_ROOT)
            {
                if(!LinuxShell.canRoot())
                {
                    EditorSettings.TRY_ROOT = false;
                    Toast.makeText(mContext, "ROOT failed!", Toast.LENGTH_LONG).show();
                }
            }
        }

        if (key == null || "touch_zoom".equals(key))
            ENABLE_TOUCH_ZOOM = mPrefs.getBoolean("touch_zoom", ENABLE_TOUCH_ZOOM);

        if (key == null || "readonly_mode".equals(key))
            READONLY_MODE = mPrefs.getBoolean("readonly_mode", READONLY_MODE);

        if (key == null || "autosave".equals(key))
            AUTO_SAVE = mPrefs.getBoolean("autosave", AUTO_SAVE);

        if (key == null || "back_button_exit".equals(key))
            BACK_BUTTON_EXIT = mPrefs.getBoolean("back_button_exit", BACK_BUTTON_EXIT);

        if (key == null || "enable_highlight".equals(key))
            ENABLE_HIGHLIGHT = mPrefs.getBoolean("enable_highlight", ENABLE_HIGHLIGHT);

        if (key == null || "wordwrap".equals(key))
            WORD_WRAP = mPrefs.getBoolean("wordwrap", WORD_WRAP);

        if (key == null || "show_line_num".equals(key))
            SHOW_LINENUM = mPrefs.getBoolean("show_line_num", SHOW_LINENUM);

        if (key == null || "show_tab".equals(key))
            SHOW_WHITESPACE = mPrefs.getBoolean("show_tab", SHOW_WHITESPACE);

        if (key == null || "auto_indent".equals(key))
            AUTO_INDENT = mPrefs.getBoolean("auto_indent", AUTO_INDENT);

        if (key == null || "keep_screen_on".equals(key))
            KEEP_SCREEN_ON = mPrefs.getBoolean("keep_screen_on", KEEP_SCREEN_ON);

        if (key == null || "spellcheck".equals(key))
            SPELLCHECK = mPrefs.getBoolean("spellcheck", SPELLCHECK);

        if (key == null || "auto_capitalize".equals(key))
            AUTO_CAPITALIZE = mPrefs.getBoolean("auto_capitalize", AUTO_CAPITALIZE);

        if (key == null || "hlc_font".equals(key))
            HIGHLIGHT_FONT = mPrefs.getString("hlc_font", HIGHLIGHT_FONT);

        if (key == null || "hlc_backgroup".equals(key))
            HIGHLIGHT_BACKGROUP = mPrefs.getString("hlc_backgroup", HIGHLIGHT_BACKGROUP);

        if (key == null || "hlc_string".equals(key))
            HIGHLIGHT_STRING = mPrefs.getString("hlc_string", HIGHLIGHT_STRING);

        if (key == null || "hlc_keyword".equals(key))
            HIGHLIGHT_KEYWORD = mPrefs.getString("hlc_keyword", HIGHLIGHT_KEYWORD);

        if (key == null || "hlc_comment".equals(key))
            HIGHLIGHT_COMMENT = mPrefs.getString("hlc_comment", HIGHLIGHT_COMMENT);

        if (key == null || "hlc_tag".equals(key))
            HIGHLIGHT_TAG = mPrefs.getString("hlc_tag", HIGHLIGHT_TAG);

        if (key == null || "hlc_attr_name".equals(key))
            HIGHLIGHT_ATTR_NAME = mPrefs.getString("hlc_attr_name", HIGHLIGHT_ATTR_NAME);

        if (key == null || "hlc_function".equals(key))
            HIGHLIGHT_FUNCTION = mPrefs.getString("hlc_function", HIGHLIGHT_FUNCTION);

        if (key == null || "last_path".equals(key))
            HIGHLIGHT_LAST_PATH = mPrefs.getString("last_path", HIGHLIGHT_LAST_PATH);

        if (key == null || "hl_colorscheme".equals(key))
            HIGHLIGHT_COLOR_SCHEME = mPrefs.getString("hl_colorscheme", HIGHLIGHT_COLOR_SCHEME);

        if (key == null || "font".equals(key))
            FONT = mPrefs.getString("font", FONT);

        if (key == null || "custom_format".equals(key))
            USE_CUSTOM_FORMAT = mPrefs.getBoolean("custom_format", USE_CUSTOM_FORMAT);

        if (key == null || "sys_date_format".equals(key))
            SYS_DATE_FORMAT = mPrefs.getString("sys_date_format", SYS_DATE_FORMAT);

        if (key == null || "custom_date_format".equals(key))
            CUSTOM_DATE_FORMAT = mPrefs.getString("custom_date_format", CUSTOM_DATE_FORMAT);

        DATE_FORMAT = USE_CUSTOM_FORMAT ? CUSTOM_DATE_FORMAT : SYS_DATE_FORMAT;

        if (key == null || "screen_orientation".equals(key))
            SCREEN_ORIENTATION = mPrefs.getString("screen_orientation", SCREEN_ORIENTATION);

        if (key == null || "font_size".equals(key))
            FONT_SIZE = Integer.valueOf(mPrefs.getString("font_size", String.valueOf(FONT_SIZE)));

        if (key == null || "highlight_limit".equals(key))
            LIMIT_HIGHLIGHT_LENGTH = Integer.valueOf(mPrefs.getString("highlight_limit", String.valueOf(LIMIT_HIGHLIGHT_LENGTH))); // 1M

        if (key == null || "cursor_width".equals(key))
        {
            CURSOR_WIDTH = Integer.valueOf(mPrefs.getString("cursor_width", String.valueOf(CURSOR_WIDTH)));
            if (CURSOR_WIDTH < 1)
                CURSOR_WIDTH = 2;
        }
    }

    public static String getVersion()
    {
        return mPrefs.getString("version", "-1");
    }

    public static boolean setVersion(String version)
    {
        return mPrefs.edit().putString("version", version).commit();
    }

    public static boolean setLastPath(String path)
    {
        return mPrefs.edit().putString("last_path", path).commit();
    }

    /*
     * public static boolean isUseDefaultToolbarSetting() { return
     * mPrefs.getBoolean("use_default_toolbar_settings", true); }
     * 
     * public static boolean isInsertSpaceForTabs() { return
     * mPrefs.getBoolean("use_space_for_tab", false); }
     * 
     * public static boolean isUseCustomColor() { return
     * mPrefs.getBoolean("use_custom_hl_color", false); }
     * 
     * public static String getString(String key, String defValue) { return
     * mPrefs.getString(key, defValue); }
     * 
     * public static int getIndentSize() { int size =
     * Integer.valueOf(mPrefs.getString("indent_size", "4")); return size > 0 ?
     * size : 4; }
     * 
     * public static String getIndentString() { int size = getIndentSize();
     * if(indentSize != size || indentString == null) { indentString =
     * isInsertSpaceForTabs() ? new String(new char[size]).replace("\0", " ") :
     * "\t"; } return indentString; }
     * 
     * public static String getVersion() { return mPrefs.getString("version",
     * "-1"); }
     * 
     * public static boolean setVersion(String version) { return
     * mPrefs.edit().putString("version", version).commit(); }
     * 
     * public static boolean setLastPath(String path) { return
     * mPrefs.edit().putString("last_path", path).commit(); }
     * 
     * public static boolean isOpenLastFile() { return
     * mPrefs.getBoolean("open_last_file", false); }
     * 
     * public static boolean isRoot() { return mPrefs.getBoolean("get_root",
     * false); }
     * 
     * public static boolean isTouchZoom() { return
     * mPrefs.getBoolean("touch_zoom", true); }
     * 
     * public static boolean isHideKeyboard() { return
     * mPrefs.getBoolean("hide_soft_Keyboard", false); }
     * 
     * public static boolean isAutoSave() { return mPrefs.getBoolean("autosave",
     * false); }
     * 
     * public static boolean isPressBackButtonToExit() { return
     * mPrefs.getBoolean("back_button_exit", true); }
     * 
     * public static boolean isUseCustomDateFormat() { return
     * mPrefs.getBoolean("custom_format", false); }
     * 
     * public static boolean isEnableHighlight() { return
     * mPrefs.getBoolean("enable_highlight", true); }
     * 
     * public static boolean isWordWrap() { return mPrefs.getBoolean("wordwrap",
     * true); }
     * 
     * public static boolean isShowLineNumber() { return
     * mPrefs.getBoolean("show_line_num", true); }
     * 
     * public static boolean isShowWhitespace() { return
     * mPrefs.getBoolean("show_tab", false); }
     * 
     * public static boolean isAutoIndent() { return
     * mPrefs.getBoolean("auto_indent", false); }
     * 
     * public static boolean isKeepScreenOn() { return
     * mPrefs.getBoolean("keep_screen_on", false); }
     * 
     * public static boolean isDisableSpellCheck() { return
     * mPrefs.getBoolean("spellcheck", true); }
     * 
     * public static boolean isAutoCapitalize() { return
     * mPrefs.getBoolean("auto_capitalize", false); }
     * 
     * public static boolean isUseCustomHighlight() { return
     * mPrefs.getBoolean("use_custom_hl_color", false); }
     * 
     * public static String getHighlightFont(String def) { return
     * mPrefs.getString("hlc_font", def); }
     * 
     * public static String getHighlightBackgroup(String def) { return
     * mPrefs.getString("hlc_backgroup", def); }
     * 
     * public static String getHighlightString(String def) { return
     * mPrefs.getString("hlc_string", def); }
     * 
     * public static String getHighlightKeyword(String def) { return
     * mPrefs.getString("hlc_keyword", def); }
     * 
     * public static String getHighlightComment(String def) { return
     * mPrefs.getString("hlc_comment", def); }
     * 
     * public static String getHighlightTag(String def) { return
     * mPrefs.getString("hlc_tag", def); }
     * 
     * public static String getHighlightAttrName(String def) { return
     * mPrefs.getString("hlc_attr_name", def); }
     * 
     * public static String getHighlightFunction(String def) { return
     * mPrefs.getString("hlc_function", def); }
     * 
     * public static String getLastPath(String def) { return
     * mPrefs.getString("last_path", def); }
     * 
     * public static String getColorScheme() { return
     * mPrefs.getString("hl_colorscheme", ""); }
     * 
     * public static String getFontName() { return mPrefs.getString("font",
     * "Monospace"); }
     * 
     * public static String getSystemDateFormat() { return
     * mPrefs.getString("sys_format", "0"); }
     * 
     * public static String getCustomDateFormat() { return
     * mPrefs.getString("custom_date_format", "0"); }
     * 
     * public static String getScreenOrientation() { return
     * mPrefs.getString("screen_orientation", "auto"); }
     * 
     * public static float getFontSize() { return
     * Float.valueOf(mPrefs.getString("font_size", "12")); }
     * 
     * public static int getHighlightLimit(int def) { return
     * Integer.valueOf(mPrefs.getString("highlight_limit",
     * String.valueOf(def))); }
     * 
     * public static int getCursorWidth() { int w =
     * Integer.valueOf(mPrefs.getString("cursor_width", "1")); if(w < 1) w = 1;
     * return w; }
     */
}
