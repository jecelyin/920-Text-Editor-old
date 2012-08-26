package com.jecelyin.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil
{
    public static String getDate()
    {
        return format(new Date());
    }

    public static String getDate(long ts)
    {
        return format(new Date(ts));
    }
    
    public static String getDateByFormat(String format)
    {
        try
        {
            return getDateByFormat(Integer.valueOf(format));
        }catch (Exception e)
        {
            //yyyy年MM月dd日_HH时mm分ss秒
            Calendar cal = Calendar.getInstance();
            format = format.replaceAll("yyyy", String.valueOf(cal.get(Calendar.YEAR)));
            format = format.replaceAll("MM", String.valueOf(cal.get(Calendar.MONTH)));
            format = format.replaceAll("dd", String.valueOf(cal.get(Calendar.DATE)));
            format = format.replaceAll("HH", String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
            format = format.replaceAll("mm", String.valueOf(cal.get(Calendar.MINUTE)));
            format = format.replaceAll("ss", String.valueOf(cal.get(Calendar.SECOND)));
            return format;
        }
    }

    public static String getDateByFormat(int format)
    {
        Date now = new Date();
        DateFormat df;

        switch(format)
        {
            case 0:
                df = DateFormat.getDateTimeInstance();
                return df.format(now);
            case 1:
                // 默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                df = DateFormat.getDateInstance();
                return df.format(now);
            case 2:
                df = new SimpleDateFormat("yyyyMMddHHmmss");
                return df.format(now);
            case 3:
                df = DateFormat.getTimeInstance();
                return df.format(now);
            case 4:
                df = DateFormat.getInstance();
                return df.format(now);
            case 5:
                df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
                return df.format(now);
            case 6:
                df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
                return df.format(now);
            case 7:
                df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                return df.format(now);
            case 8:
                df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
                return df.format(now);
            default:
                return now.toString();
        }
    }

    private static String format(Date d)
    {
        return DateFormat.getDateTimeInstance().format(d);
    }

}
