/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.jecelyin.widget;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

public class ForegroundColorSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {

    private int mColor;

    public ForegroundColorSpan(int color) {
        mColor = color;
    }

    public ForegroundColorSpan(Parcel src) {
        mColor = src.readInt();
    }
    
    public void setColor(int color)
    {
        mColor = color;
    }
    
    //public static final int FOREGROUND_COLOR_SPAN = 2;
    public int getSpanTypeId() {
        return 2;
    }
    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mColor);
    }

    public int getForegroundColor() {
        return mColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(mColor);
    }
}
