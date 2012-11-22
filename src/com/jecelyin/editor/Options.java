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


import java.net.URLEncoder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import com.jecelyin.colorschemes.ColorScheme;
import com.jecelyin.util.ColorPicker;
import com.jecelyin.util.TimeUtil;

public class Options extends PreferenceActivity
{
    private int category;
    private SharedPreferences mSP;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        category = getIntent().getIntExtra("category", R.xml.options);
        addPreferencesFromResource(category);
        mSP = getPreferenceManager().getSharedPreferences();

        switch(category)
        {
            case R.xml.view:
                initView();
                break;
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
        }
    }

    private void initHelp()
    {

        findPreference("about").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Intent intent = new Intent(Options.this, About.class);
                startActivity(intent);
                return true;
            }
        });
        findPreference("help").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Help.showHelp(Options.this);
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

        findPreference("project").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.getlocalization.com/920TextEditor/"));
                startActivity(intent);
                return true;
            }
        });
    }

    private void initHighlight()
    {
        setHighlightEvent("hlc_font", ColorScheme.color_font);
        setHighlightEvent("hlc_backgroup", ColorScheme.color_backgroup);
        setHighlightEvent("hlc_string", ColorScheme.color_string);
        setHighlightEvent("hlc_keyword", ColorScheme.color_keyword);
        setHighlightEvent("hlc_comment", ColorScheme.color_comment);
        setHighlightEvent("hlc_tag", ColorScheme.color_tag);
        setHighlightEvent("hlc_attr_name", ColorScheme.color_attr_name);
        setHighlightEvent("hlc_function", ColorScheme.color_function);

        PreferenceCategory cate = (PreferenceCategory) findPreference("custom_highlight_color");
        cate.setEnabled(mSP.getBoolean("use_custom_hl_color", false));
        CheckBoxPreference uchc = (CheckBoxPreference) findPreference("use_custom_hl_color");
        uchc.setOnPreferenceChangeListener(mOnHighlightChange);

        ListPreference csPref = (ListPreference) findPreference("hl_colorscheme");
        String[] csNames = ColorScheme.getSchemeNames();
        if(csNames == null)
            csNames = new String[]{ "Default" };
        csPref.setEntries(csNames);
        csPref.setEntryValues(csNames);

    }

    private void setHighlightEvent(final String key, final String def)
    {
        Preference pref = (Preference) findPreference(key);
        pref.setSummary(mSP.getString(key, def));

        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                ColorPicker cp = new ColorPicker(Options.this, new ColorListener(), preference.getKey(), preference.getTitle().toString(), Color.parseColor(preference
                        .getSharedPreferences().getString(key, def)));
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
        fontPf.setDefaultValue("Monospace");

        ListPreference fontSizePf = (ListPreference) findPreference("font_size");
        String[] font_size = new String[]{ "10", "12", "13", "14", "16", "18", "20", "22", "24", "26", "28", "32" };
        fontSizePf.setEntries(font_size);
        fontSizePf.setEntryValues(font_size);
        fontSizePf.setDefaultValue("14");

    }
    
    private void initEditors()
    {
        setOptionsPreference("date_format", R.xml.date_format);
    }

    private void initDateFormat()
    {
        ListPreference sys_format = (ListPreference) findPreference("sys_format");

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
                Intent intent = new Intent(Options.this, Options.class);
                intent.putExtra("category", id);
                startActivity(intent);
                return true;
            }
        });
    }

    private void init()
    {
        setOptionsPreference("opt_view", R.xml.view);
        setOptionsPreference("opt_editors", R.xml.editors);
        setOptionsPreference("opt_highlight", R.xml.highlight);
        setOptionsPreference("opt_search", R.xml.search);
        setOptionsPreference("opt_other", R.xml.other);
        setOptionsPreference("opt_help", R.xml.help);

        findPreference("donate").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                startActivity(Donate.getWebIntent());
                return true;
            }
        });

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
            cate.setEnabled(val.toString().equals("true") ? true : false);
            return true;
        }
    };

}
