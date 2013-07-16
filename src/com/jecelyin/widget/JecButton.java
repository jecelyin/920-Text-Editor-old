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

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;

import java.util.HashMap;

public class JecButton extends Button
{
    private HashMap<String, String> mStrHashMap;
    
    public JecButton(Context context) {
        super(context);
        mStrHashMap = new HashMap<String, String>();
        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        //lp.setMargins(0, 0, 0, 0);
        lp.weight = 1.0F;
        setLayoutParams(lp);
        //setPadding(0, 0, 0, 0);
    }
    
    public void putString(String key, String value)
    {
        mStrHashMap.put(key, value);
    }
    
    public String getString(String key)
    {
        return mStrHashMap.get(key);
    }
    
/*    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        TextPaint paint = getPaint();
        CharSequence text = getText();
        float width = paint.measureText(text, 0, text.length());
        //width = Math.max(Math.max(width, 50f), getMeasuredWidth());
        float density = getResources().getDisplayMetrics().density;
        width = Math.max(width*density, getSuggestedMinimumWidth());
        setMeasuredDimension((int)width, getMeasuredHeight());
    }*/
 

}
