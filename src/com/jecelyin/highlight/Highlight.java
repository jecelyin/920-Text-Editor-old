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

package com.jecelyin.highlight;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import com.jecelyin.colorschemes.ColorScheme;
import com.jecelyin.editor.EditorSettings;
import com.jecelyin.editor.JecEditor;
import com.jecelyin.util.FileUtil;
import com.jecelyin.widget.ForegroundColorSpan;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Highlight
{
    static {
        System.loadLibrary("highlight");
    }
    
    public static final int GROUP_TAG_ID        = 1;
    public static final int GROUP_COMMENT_ID    = 2;
    public static final int GROUP_STRING_ID     = 3;
    public static final int GROUP_KEYWORD_ID    = 4;
    public static final int GROUP_FUNCTION_ID   = 5;
    public static final int GROUP_ATTR_NAME_ID  = 6;

    
    private final static String TAG = "Highlight";
    private static HashMap<String, String[]> langTab;
    private static ArrayList<String[]> nameTab;
    private static int color_tag;
    private static int color_string;
    private static int color_keyword;
    private static int color_function;
    private static int color_comment;
    private static int color_attr_name;
    private String mExt = "";
    private int mEndOffset = -1;
    private int mStartOffset = -1;
    private boolean mStop = true;
    private static int mLimitFileSize = 0; //单位：KB
    private static ArrayList<ForegroundColorSpan> mSpans = new ArrayList<ForegroundColorSpan>();
    
    public static void init()
    {
        loadLang();
        loadColorScheme();
    }
    
    public void setSyntaxType(String file_extension)
    {
        this.mExt = file_extension;
    }
    
    public void stop()
    {
        this.mStop = true;
    }

    public void redraw()
    {
        this.mStop = false;
        this.mEndOffset = -1;
        this.mStartOffset = -1;
    }
    
    /**
     * 
     * @param j 
     * @param mLayout 
     * @return 返回[[高亮类型,开始offset, 结束offset],,]
     */
    public boolean render(Spannable mText, int startOffset, int endOffset)
    {
        if(!EditorSettings.ENABLE_HIGHLIGHT || langTab == null)
            return false;

        if(this.mStop || "".equals(this.mExt))
            return false;
        
        if(this.mStartOffset <= startOffset && this.mEndOffset >= endOffset)
            return false;
        String[] lang = langTab.get(this.mExt);
        if(lang == null)
        {
            return false;
        }
        //lock it 不然会因为添加了span后导致offset改变，不断地进行高亮
        this.mStop = true;
        //TimerUtil.start();
        //Log.d(TAG, startOffset+"="+endOffset);
        
        this.mStartOffset = startOffset;
        this.mEndOffset = endOffset;
        String text = mText.subSequence(0, endOffset).toString();
        int[] ret = jni_parse(text, JecEditor.TEMP_PATH + File.separator + lang[1]);
        //TimerUtil.stop("hg parse");
        if(ret == null)
        {
            this.mStop = false;
            return false;
        }
        int len = ret.length;
        if(len < 1 || len % 3.0F != 0)
        {
            this.mStop = false;
            return false;
        }
        
        //TimerUtil.start();
        //不能清除全陪，因为滚动条需要一个span来按住拖动
        //mText.clearSpans();
        
        int color;
        int start;
        int end;
        int index=0;
        int bufLen = mSpans.size();
        ForegroundColorSpan fcs;
        for(ForegroundColorSpan fcs2:mSpans)
        {
            mText.removeSpan(fcs2);
        }
        for(int i=0; i<len; i++)
        {
            
            switch(ret[i])
            {
                case GROUP_TAG_ID:
                    color = color_tag;
                    break;
                case GROUP_STRING_ID:
                    color = color_string;
                    break;
                case GROUP_KEYWORD_ID:
                    color = color_keyword;
                    break;
                case GROUP_FUNCTION_ID:
                    color = color_function;
                    break;
                case GROUP_COMMENT_ID:
                    color = color_comment;
                    break;
                case GROUP_ATTR_NAME_ID:
                    color = color_attr_name;
                    break;
                default:
                    Log.d(TAG, "获取颜色group id失败");
                    mStop = false;
                    return false;
            }
            
            start = ret[++i];
            end   = ret[++i];
            
            if(end < startOffset)
                continue;
            
            if(index >= bufLen)
            {
                fcs = new ForegroundColorSpan(color);
                mSpans.add(fcs);
            } else {
                fcs = mSpans.get(index);
                fcs.setColor(color);
            }
            
            ++index;
            try {
                mText.setSpan(fcs, start, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch(Exception e) {
                
            }
            
        }
        ret = null;
        //TimerUtil.stop("hg 1");
        this.mStop = false;
        return true;
    }
    
    /**
     * [{语法名称,其中一个扩展名},,]
     */
    public static ArrayList<String[]> getLangList()
    {
        return nameTab;
    }
    
    public static boolean loadLang()
    {
        String langfile = JecEditor.TEMP_PATH + "/lang.conf";
        File file = new File(langfile);
        if(!file.isFile())
        {
            return false;
        }
        file = null;
        langTab = new HashMap<String, String[]>();
        nameTab = new ArrayList<String[]>();

        try
        {
            String mData = FileUtil.readFileAsString(langfile, "utf-8");
            String[] lines = mData.split("\n");
            String[] cols;
            for(String line:lines)
            {
                line = line.trim();
                if(line.startsWith("#"))
                    continue;
                cols = line.split(":");
                String name = cols[0].trim();
                String synfile = cols[1].trim();
                String extsString = cols[2].trim();
                String[] exts = extsString.split("\\s+");
                nameTab.add(new String[] {name, exts[0]});
                for(String ext:exts)
                {
                    langTab.put(ext, new String[]{name, synfile});
                }
            }

            Collections.sort(nameTab, new Comparator<String[]>() {

                @Override
                public int compare(String[] object1, String[] object2)
                {
                    return object1[0].compareToIgnoreCase(object2[2]);
                }
            });
        }catch (Exception e)
        {
            return false;
        }
        
        return true;
    }
    
    public static void loadColorScheme()
    {
        //色彩模块
        color_tag            = Color.parseColor(ColorScheme.color_tag);
        color_string         = Color.parseColor(ColorScheme.color_string);
        color_keyword        = Color.parseColor(ColorScheme.color_keyword);
        color_function       = Color.parseColor(ColorScheme.color_function);
        color_comment        = Color.parseColor(ColorScheme.color_comment);
        color_attr_name      = Color.parseColor(ColorScheme.color_attr_name);
    }
    
    public static String getNameByExt(String ext)
    {
        if(langTab == null)
        {
            loadLang();
            return "";
        }
        String[] info = langTab.get(ext);
        if(info == null)
        {
            return "";
        }
        return info[0];
    }

    private native static int[] jni_parse(String text, String syntaxFile);

}
