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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Searcher
{
    private String text = "";
    private Matcher mMatcher;

    public int[] find(String keyword, int start)
    {
        int offset = text.indexOf(keyword, start);
        if (offset == -1)
            return null;
        int end = offset + keyword.length();
        return new int[] { offset, end };
    }

    /**
     * 查找配对关键字，比如 "", < >等待
     * 
     * @param keyword1
     * @param keyword2
     * @param start
     * @return int[]{开始位置, 结束位置}
     */
    public int[] find(String keyword1, String keyword2, int start)
    {
        int offset = text.indexOf(keyword1, start);
        if (offset == -1)
            return null;
        int offset2 = text.indexOf(keyword2, offset + keyword1.length() + 1);
        int end;
        // 若是不能完成配对，则取文本最后内容作为配对
        if (offset2 == -1)
        {
            end = text.length();
        } else
        {
            end = offset2 + keyword2.length();
        }

        return new int[] { offset, end };
    }
    
    public void prepare(String pattern)
    {
        Pattern mPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        mMatcher = mPattern.matcher(text);
    }
    
    public Matcher getMatcher()
    {
        //mMatcher.reset(text);
        return mMatcher;
    }

    public int[] findMatch(int start, int end)
    {
        //mMatcher.reset(text);
        if(mMatcher.find(start))
        {
            if(mMatcher.end() > end)
            {
                return null;
            }
            
            return new int[] { mMatcher.start(), mMatcher.end() };
        }
        return null;
    }
    
    
    
    /**
     * 是否为可包含内容，如转义。
     * 注：只能处理"string\"ok"这种情况，不能处理字符串"string"|ok"可包含"|的情况
     * @param end
     * @param include
     * @return
     */
    public boolean isInclude(int end, String include)
    {
        if(text.substring(end-include.length(), end) == include)
            return true;
        return false;
    }
    
    public String getString(int start, int end)
    {
        return text.substring(start, end);
    }

    public void setText(String mText)
    {
        this.text = mText;
    }

    public int getTextLength()
    {
        return text.length();
    }

}
