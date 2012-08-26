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

package com.jecelyin.widget;

//import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class JecImageButton extends ImageButton
{
//    private HashMap<String, String> mStrHashMap;
   // public int bg; //背景
    public int hit; //点击次数
    public String symbol;
    public String idName;
    
    public JecImageButton(Context context)
    {
        super(context);
//        mStrHashMap = new HashMap<String, String>();
    }
    
    public JecImageButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
//        mStrHashMap = new HashMap<String, String>();
    }
    
    public JecImageButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
//        mStrHashMap = new HashMap<String, String>();
    }
    
/*    public void putString(String key, String value)
    {
        mStrHashMap.put(key, value);
    }
    
    public String getString(String key)
    {
        return mStrHashMap.get(key);
    }*/

}
