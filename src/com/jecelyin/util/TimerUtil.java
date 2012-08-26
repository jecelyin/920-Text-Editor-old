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

import android.util.Log;

public class TimerUtil
{
    private static long startTime, endTime;
    
    public static void start()
    {
        startTime = System.currentTimeMillis();
    }
    
    public static long getTime()
    {
        return System.currentTimeMillis();
    }
    
    public static void stop(String tag)
    {
        endTime = System.currentTimeMillis();
        double t = (endTime - startTime)/1000.0;
        Log.d(tag, String.valueOf(t) + " seconds");
    }

}