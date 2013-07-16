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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TextUtil
{
    public static String MD5(String str, String encoding)
    {
        MessageDigest messageDigest = null;

       
            try
            {
                messageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
            messageDigest.reset();
            try
            {
                messageDigest.update(str.getBytes(encoding));
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++)
        {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }

        return md5StrBuff.toString();
    }
    
    //jecelyin: from org.apache.commons.lang.StringUtils
    // Count matches
    //-----------------------------------------------------------------------
    /**
     * <p>Counts how many times the substring appears in the larger String.</p>
     *
     * <p>A <code>null</code> or empty ("") String input returns <code>0</code>.</p>
     *
     * <pre>
     * StringUtils.countMatches(null, *)       = 0
     * StringUtils.countMatches("", *)         = 0
     * StringUtils.countMatches("abba", null)  = 0
     * StringUtils.countMatches("abba", "")    = 0
     * StringUtils.countMatches("abba", "a")   = 2
     * StringUtils.countMatches("abba", "ab")  = 1
     * StringUtils.countMatches("abba", "xxx") = 0
     * </pre>
     *
     * @param str  the String to check, may be null
     * @param sub  the substring to count, may be null
     * @return the number of occurrences, 0 if either String is <code>null</code>
     */
    public static int countMatches(CharSequence str, char sub, int start, int end) {
        if (str.length() == 0) {
            return 0;
        }
        int count = 0;
        for(int index=start; index<=end;index++)
        {
            if(str.charAt(index) == sub)
            {
                count++;
            }
        }
        return count;
    }
    
    private static Object sLock = new Object();
    private static char[] sTemp = null;
    public static char[] obtain(int len) {
        char[] buf;

        synchronized (sLock) {
            buf = sTemp;
            sTemp = null;
        }

        if (buf == null || buf.length < len)
            buf = new char[ArrayUtil.idealCharArraySize(len)];

        return buf;
    }
    
    public static void printStacks(String msg)
    {
        Exception e = new Exception("PRINT STACKS: "+msg+" ======================================================");
        e.printStackTrace();
    }

}
