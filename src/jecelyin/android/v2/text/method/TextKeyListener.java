/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jecelyin.android.v2.text.method;

import com.jecelyin.editor.EditorSettings;
import com.jecelyin.util.JecLog;

import jecelyin.android.v2.text.Selection;
import android.text.Editable;
import android.text.SpanWatcher;
import android.view.KeyEvent;
import android.view.View;

/**
 * This is the key listener for typing normal text.  It delegates to
 * other key listeners appropriate to the current keyboard and language.
 */
public class TextKeyListener extends android.text.method.TextKeyListener implements SpanWatcher {
    private static TextKeyListener[] sInstance = new TextKeyListener[Capitalize.values().length * 2];

    /**
     * Creates a new TextKeyListener with the specified capitalization
     * and correction properties.
     *
     * @param cap when, if ever, to automatically capitalize.
     * @param autotext whether to automatically do spelling corrections.
     */
    public TextKeyListener(Capitalize cap, boolean autotext) {
        super(cap, autotext);
    }

    /**
     * Returns a new or existing instance with the specified capitalization
     * and correction properties.
     *
     * @param cap when, if ever, to automatically capitalize.
     * @param autotext whether to automatically do spelling corrections.
     */
    public static TextKeyListener getInstance(boolean autotext, Capitalize cap) {
        int off = cap.ordinal() * 2 + (autotext ? 1 : 0);

        if (sInstance[off] == null) {
            sInstance[off] = new TextKeyListener(cap, autotext);
        }

        return sInstance[off];
    }

    /**
     * Returns a new or existing instance with no automatic capitalization
     * or correction.
     */
    public static TextKeyListener getInstance() {
        return getInstance(false, Capitalize.NONE);
    }

    //jec+
    public boolean doIndent(Editable content)
    {
        int start = Selection.getSelectionStart(content);
        int end = Selection.getSelectionEnd(content);

        int prev = start - 2;
        while (prev >= 0 && content.charAt(prev) != '\n')
            prev--;
        
        prev++;
        int pos = prev;
        while (content.charAt(pos) == ' ' || content.charAt(pos) == '\t' || content.charAt(pos) == '\u3000')
            pos++;
        
        int len = pos - prev;
        if(len > 0)
        {
            try
            {
                char[] dest = new char[len];
                content.getChars(prev, pos, dest, 0);
                String newstr = (new String(dest)).replaceAll("\t", EditorSettings.INDENT_STRING);
                content.replace(start, end, newstr);
                Selection.setSelection(content, start + len);
            }catch (Exception e)
            {
                JecLog.e(e.getMessage(), e);
            }

        }
        return true;
    }
    //end
    
    @Override
    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {

        boolean result = super.onKeyDown(view, content, keyCode, event);
        
        if(keyCode == KeyEvent.KEYCODE_TAB && EditorSettings.USE_SPACE_FOR_TAB)
        {
            content.insert(Selection.getSelectionStart(content), EditorSettings.INDENT_STRING);
        } else if (keyCode == KeyEvent.KEYCODE_ENTER ){
            doIndent(content);
        }

        return result;
        //end
    }

    @Override
    public boolean onKeyOther(View view, Editable content, KeyEvent event) {
        return super.onKeyOther(view, content, event);
    }
}
