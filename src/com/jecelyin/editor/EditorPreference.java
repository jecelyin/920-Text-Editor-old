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


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;
import com.jecelyin.colorschemes.ColorScheme;
import com.jecelyin.editor.preferences.CustomMenu;
import com.jecelyin.util.ColorPicker;
import com.jecelyin.util.TimeUtil;

import java.net.URLEncoder;

public class EditorPreference extends PreferenceActivity
{
    private int category;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        JecApp.addActivity(this);
        category = getIntent().getIntExtra("category", R.xml.options);
        addPreferencesFromResource(category);
        
        switch(category)
        {
            case R.xml.highlight:
                initHighlight();
                break;
            case R.xml.help:
                initHelp();
                break;
            case R.xml.options:
                init();
                break;
            case R.xml.editors:
                initEditors();
                break;
            case R.xml.date_format:
                initDateFormat();
                break;
            case R.xml.other:
                initOther();
                break;
            case R.xml.project:
                initProject();
                break;
        }
    }
    
    protected void onDestroy()
    {
        super.onDestroy();
        JecApp.removeActivity(this);
    }
    
    private void initOther()
    {
        ListPreference screen_ori = (ListPreference) findPreference("screen_orientation");
        String[] ori = new String[]{getString(R.string.screen_orientation_auto), getString(R.string.screen_orientation_landscape), getString(R.string.screen_orientation_portrait)};
        screen_ori.setEntries(ori);
        screen_ori.setEntryValues(ori);
        screen_ori.setDefaultValue(getString(R.string.screen_orientation_auto));
    }

    private void initHelp()
    {

        findPreference("about").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Intent intent = new Intent(EditorPreference.this, About.class);
                startActivity(intent);
                return true;
            }
        });
        findPreference("help").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Help.showHelp(EditorPreference.this);
                return true;
            }
        });
        findPreference("feedback").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Uri uri;
                try
                {
                    uri = Uri.parse("http://www.jecelyin.com/920report.php?ver=" + URLEncoder.encode(JecEditor.version+"/"+android.os.Build.MODEL+"/"+android.os.Build.VERSION.RELEASE, "utf-8"));
                }catch (Exception e)
                {
                    uri = Uri.parse("http://www.jecelyin.com/920report.php?var=badver");
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // Intent intent = new Intent(Options.this, Donate.class);
                startActivity(intent);
                return true;
            }
        });

        /*findPreference("project").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.getlocalization.com/920TextEditor/"));
                startActivity(intent);
                return true;
            }
        });*/
        
    }

    private void initHighlight()
    {
        setHighlightEvent("hlc_font", EditorSettings.HIGHLIGHT_FONT);
        setHighlightEvent("hlc_backgroup", EditorSettings.HIGHLIGHT_BACKGROUP);
        setHighlightEvent("hlc_string", EditorSettings.HIGHLIGHT_STRING);
        setHighlightEvent("hlc_keyword", EditorSettings.HIGHLIGHT_KEYWORD);
        setHighlightEvent("hlc_comment", EditorSettings.HIGHLIGHT_COMMENT);
        setHighlightEvent("hlc_tag", EditorSettings.HIGHLIGHT_TAG);
        setHighlightEvent("hlc_attr_name", EditorSettings.HIGHLIGHT_ATTR_NAME);
        setHighlightEvent("hlc_function", EditorSettings.HIGHLIGHT_FUNCTION);

        PreferenceCategory cate = (PreferenceCategory) findPreference("custom_highlight_color");
        cate.setEnabled(EditorSettings.CUSTORM_HIGHLIGHT_COLOR);
        CheckBoxPreference uchc = (CheckBoxPreference) findPreference("use_custom_hl_color");
        uchc.setOnPreferenceChangeListener(mOnHighlightChange);
        mOnHighlightChange.onPreferenceChange(null, EditorSettings.CUSTORM_HIGHLIGHT_COLOR);

        ListPreference csPref = (ListPreference) findPreference("hl_colorscheme");
        String[] csNames = ColorScheme.getSchemeNames();
        if(csNames == null)
            csNames = new String[]{ "Default" };
        csPref.setEntries(csNames);
        csPref.setEntryValues(csNames);

    }

    private void setHighlightEvent(final String key, final String val)
    {
        Preference pref = (Preference) findPreference(key);
        pref.setSummary(val);

        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                ColorPicker cp = new ColorPicker(
                        EditorPreference.this
                        , new ColorListener()
                        , preference.getKey()
                        , preference.getTitle().toString()
                        , Color.parseColor(val));
                cp.show();
                return true;
            }
        });
    }

    private class ColorListener implements ColorPicker.OnColorChangedListener
    {
        @Override
        public void onColorChanged(String key, String color)
        {
            Preference pref = (Preference) findPreference(key);
            pref.setSummary(color);
            pref.getEditor().putString(key, color).commit();
        }

    }

    private void initView()
    {
        ListPreference fontPf = (ListPreference) findPreference("font");
        String[] fonts = new String[]{ "Normal", "Monospace", "Sans Serif", "Serif" };
        fontPf.setEntries(fonts);
        fontPf.setEntryValues(fonts);
        fontPf.setDefaultValue(EditorSettings.FONT);

        ListPreference fontSizePf = (ListPreference) findPreference("font_size");
        String[] font_size = new String[]{ "10", "12", "13", "14", "16", "18", "20", "22", "24", "26", "28", "32" };
        fontSizePf.setEntries(font_size);
        fontSizePf.setEntryValues(font_size);
        fontSizePf.setDefaultValue(String.valueOf(EditorSettings.FONT_SIZE));
        
        ListPreference cursorWidthLP = (ListPreference) findPreference("cursor_width");
        String[] cursor_width = new String[]{ "1", "2", "3", "4" };
        cursorWidthLP.setEntries(cursor_width);
        cursorWidthLP.setEntryValues(cursor_width);
        cursorWidthLP.setDefaultValue(String.valueOf(EditorSettings.CURSOR_WIDTH));

    }
    
    private void initEditors()
    {
        initView();
        setOptionsPreference("date_format", R.xml.date_format);
    }
    
    private void initProject()
    {
        ListPreference encoding = (ListPreference) findPreference("encoding");
        String[] lists = EncodingList.list;
        lists[0] = getString(R.string.auto_detection);
        encoding.setEntries(lists);
        encoding.setEntryValues(lists);
        encoding.setDefaultValue(EditorSettings.DEFAULT_ENCODING);
    }

    private void initDateFormat()
    {
        ListPreference sys_format = (ListPreference) findPreference("sys_date_format");

        String[] fmName = new String[]{
             TimeUtil.getDateByFormat(0)
             ,TimeUtil.getDateByFormat(1)
             ,TimeUtil.getDateByFormat(2)
             ,TimeUtil.getDateByFormat(3)
             ,TimeUtil.getDateByFormat(4)
             ,TimeUtil.getDateByFormat(5)
             ,TimeUtil.getDateByFormat(6)
             ,TimeUtil.getDateByFormat(7)
             ,TimeUtil.getDateByFormat(8)
             ,TimeUtil.getDateByFormat(9)
        };
        String[] fmVal = new String[]{
            "0","1","2","3","4","5","6","7","8","9"
        };
        sys_format.setEntries(fmName);
        sys_format.setEntryValues(fmVal);
        
        if("".equals(sys_format.getValue()))
            sys_format.setValue(fmVal[0]);
        
        EditTextPreference custom_format = (EditTextPreference) findPreference("custom_date_format");
        custom_format.getEditText().setSingleLine();
    }

    private void setOptionsPreference(final String key, final int id)
    {
        Preference pref = (Preference) findPreference(key);
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Intent intent = new Intent(EditorPreference.this, EditorPreference.class);
                intent.putExtra("category", id);
                startActivity(intent);
                return true;
            }
        });
    }

    private void init()
    {
        setOptionsPreference("opt_editors", R.xml.editors);
        setOptionsPreference("opt_project", R.xml.project);
        setOptionsPreference("opt_highlight", R.xml.highlight);
        setOptionsPreference("opt_other", R.xml.other);
        setOptionsPreference("opt_help", R.xml.help);

        /*findPreference("donate").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Uri uri = Uri.parse("http://www.jecelyin.com/donate.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });*/

        findPreference("clear_history").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                SharedPreferences sp = getSharedPreferences(JecEditor.PREF_HISTORY, MODE_PRIVATE);
                sp.edit().clear().commit();
                Toast.makeText(getApplicationContext(), R.string.clear_history_ok, Toast.LENGTH_LONG).show();
                return true;
            }
        });
        
        findPreference("opt_checkupdate").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Intent intent;
                try
                {
                    intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.jecelyin.com/920upgrade.php?code="
                             + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode));
                    startActivity(intent);
                } catch (NameNotFoundException e)
                {
                    e.printStackTrace();
                }

                return true;
            }
        });
/*
        findPreference("opt_custom_toolbar").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Intent intent;
                try
                {
                    intent = new Intent(EditorPreference.this, CustomMenu.class);
                    startActivity(intent);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

                return true;
            }
        });*/
    }

    public static Typeface getFont(String font)
    {
        if("Monospace".equals(font))
            return Typeface.MONOSPACE;
        else if("Sans Serif".equals(font))
            return Typeface.SANS_SERIF;
        else if("Serif".equals(font))
            return Typeface.SERIF;
        return Typeface.DEFAULT;
    }

    private OnPreferenceChangeListener mOnHighlightChange = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference pref, Object val)
        {
            PreferenceCategory cate = (PreferenceCategory) findPreference("custom_highlight_color");
            ListPreference colorscheme = (ListPreference) findPreference("hl_colorscheme");
            boolean isTrue = val.toString().equals("true");
            cate.setEnabled(isTrue ? true : false);
            colorscheme.setEnabled(isTrue ? false : true);
            return true;
        }
    };

}
