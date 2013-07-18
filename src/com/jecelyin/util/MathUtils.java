package com.jecelyin.util;

public class MathUtils
{

    public static int toInt(String value, int def)
    {
        if(value == null || value.length() == 0)
            return def;
        try
        {
            return Integer.valueOf(value);
        }catch (Exception e)
        {
            try
            {
                return Integer.valueOf(toNumber(value));
            } catch (Exception e2)
            {
                return def;
            }
        }
    }

    public static long toLong(String value, long def)
    {
        if(value == null || value.length() == 0)
            return def;
        try
        {
            return Long.valueOf(value);
        }catch (Exception e)
        {
            try
            {
                return Long.valueOf(toNumber(value));
            } catch (Exception e2)
            {
                return def;
            }
        }
    }

    public static String toNumber(String value)
    {
        if(value.charAt(0) == '-' || isNumber(value.charAt(0)))
        {
            int size = value.length();
            int i = 1;
            while ((i < size) && isNumber(value.charAt(i)))
            {
                i += 1;
            }
            return value.substring(0, i);
        }else
        {
            return "0";
        }
    }

    public static boolean isNumber(char c)
    {
        return c >= '0' && c <= '9';
    }

}
