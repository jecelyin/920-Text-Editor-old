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

package com.jecelyin.util;

import java.util.ArrayList;

import android.util.Log;

public class TC
{
    private static ArrayList<Long> mBuf = new ArrayList<Long>();
    
    public static void start()
    {
        long startTime = System.currentTimeMillis();
        mBuf.add(startTime);
    }

    public static void end(String tag)
    {
        long endTime = System.currentTimeMillis();
        long startTime = mBuf.remove(mBuf.size()-1);
        double t = (endTime - startTime)/1000.0;
        startTime = endTime;
        Log.d(tag, String.valueOf(t) + " seconds");
    }

}