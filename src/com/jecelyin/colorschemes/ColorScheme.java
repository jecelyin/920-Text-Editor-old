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

package com.jecelyin.colorschemes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.jecelyin.editor.JecEditor;
import android.content.SharedPreferences;

public class ColorScheme
{
    private final static String FONT_COLOR = "#000000";
    public static String color_font = FONT_COLOR; //文字颜色
    
    private final static String BACKGROUP_COLOR = "#ffffff";
    public static String color_backgroup = BACKGROUP_COLOR; //背景颜色
    
    private final static String STRING_COLOR = "#008800";
    public static String color_string = STRING_COLOR; //string content
    
    private final static String KEYWORD_COLOR = "#000088";
    public static String color_keyword = KEYWORD_COLOR; //keyword
    
    private final static String COMMENT_COLOR = "#3F7F5F";
    public static String color_comment = COMMENT_COLOR; //comment
    
    private final static String TAG_COLOR = "#800080";
    public static String color_tag = TAG_COLOR; //a markup tag name
    
    private final static String ATTR_NAME_COLOR = "#FF0000";
    public static String color_attr_name = ATTR_NAME_COLOR; //attribute name
    
    private final static String FUNCTION_COLOR = "#000080";
    public static String color_function = FUNCTION_COLOR; //html or xml tag color
    
    private static ArrayList<SchemeTable> schemeTables = new ArrayList<SchemeTable>();
    private static String[] schemeNames;
    
    public static void set(SharedPreferences mSharedPreferences)
    {
        loadAllScheme();
        String cs = mSharedPreferences.getString("hl_colorscheme", "");
        if(!cs.equals(""))
        {
            for(SchemeTable st:schemeTables)
            {
                if(cs.equals(st.colors_name))
                {
                    color_font = st.font;
                    color_backgroup = st.backgroup;
                    color_string = st.string;
                    color_keyword = st.keyword;
                    color_comment = st.comment;
                    color_tag = st.tag;
                    color_attr_name = st.attr_name;
                    color_function = st.function;
                    return;
                }
            }
        }
        if(mSharedPreferences.getBoolean("use_custom_hl_color", false))
        {
            color_font = mSharedPreferences.getString("hlc_font", color_font);
            color_backgroup = mSharedPreferences.getString("hlc_backgroup", color_backgroup);
            color_string = mSharedPreferences.getString("hlc_string", color_string);
            color_keyword = mSharedPreferences.getString("hlc_keyword", color_keyword);
            color_comment = mSharedPreferences.getString("hlc_comment", color_comment);
            color_tag = mSharedPreferences.getString("hlc_tag", color_tag);
            color_attr_name = mSharedPreferences.getString("hlc_attr_name", color_attr_name);
            color_function = mSharedPreferences.getString("hlc_function", color_function);
        } else {
            color_font = FONT_COLOR;
            color_backgroup = BACKGROUP_COLOR;
            color_string = STRING_COLOR;
            color_keyword = KEYWORD_COLOR;
            color_comment = COMMENT_COLOR;
            color_tag = TAG_COLOR;
            color_attr_name = ATTR_NAME_COLOR;
            color_function = FUNCTION_COLOR;
        }
    }
    
    public static void loadAllScheme()
    {
        if(schemeTables.size() > 0)
            return;
        File files = new File(JecEditor.TEMP_PATH+"/colors");
        if(!files.isDirectory())
            return;
        File list[] = files.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename)
            {
                if(filename.endsWith(".conf"))
                    return true;
                return false;
            }
        });
        String line, key, val;
        for(File f:list)
        {
            try
            {
                FileInputStream fis = new FileInputStream(f);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                SchemeTable st = new SchemeTable();
                while((line=br.readLine()) != null)
                {
                    String[] sp = line.split("=");
                    if(sp.length != 2)
                        continue;
                    key = sp[0].trim();
                    val = sp[1].trim();
                    
                    if("colors_name".equals(key)){
                        st.colors_name = val;
                    } else if("backgroup".equals(key)) {
                        st.backgroup = val;
                    } else if("string".equals(key)) {
                        st.string = val;
                    } else if("font".equals(key)) {
                        st.font = val;
                    } else if("comment".equals(key)) {
                        st.comment = val;
                    } else if("keyword".equals(key)) {
                        st.keyword = val;
                    } else if("tag".equals(key)) {
                        st.tag = val;
                    } else if("attr_name".equals(key)) {
                        st.attr_name = val;
                    } else if("function".equals(key)) {
                        st.function = val;
                    }
                }
                schemeTables.add(st);
            }catch (Exception e)
            {
            }
        }
        if(schemeTables.size() > 0)
        {
            schemeNames = new String[schemeTables.size()];
            int i=0;
            for(SchemeTable st:schemeTables)
            {
                schemeNames[i] = st.colors_name;
                i++;
            }
        }
    }
    
    public static String[] getSchemeNames()
    {
        //try again
        if(schemeNames == null)
            loadAllScheme();
        return schemeNames;
    }

}

class SchemeTable
{
    public String colors_name;
    public String backgroup;
    public String string;
    public String font;
    public String comment;
    public String keyword;
    public String tag;
    public String attr_name;
    public String function;
}