package com.jecelyin.util;

import java.util.HashMap;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.jecelyin.editor.JecEditor;

public class JecLog
{
    private static Application mApp;

    public static void init(Application editor)
    {
        mApp = editor;
    }
    
    public static void e(String msg)
    {
        e(msg, null);
    }
    
    public static void e(final String msg, final Throwable th)
    {
        Log.e("JecEditor_ERROR", msg, th);
        new Thread(){
            public void run()
            {
                String trace = createCrashDataString(th);
                Intent dialogIntent = new Intent(mApp, CrashReportDialog.class);
                dialogIntent.putExtra("msg", msg);
                dialogIntent.putExtra("trace", trace);
                dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mApp.startActivity(dialogIntent);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        }.start();
        //android.os.Process.killProcess(android.os.Process.myPid());
        //System.exit(10);
    }
    
    public static void d(String msg)
    {
        d(msg, null);
    }
    
    public static void d(String msg, Throwable th)
    {
        Log.d("JecEditor_DEBUG", msg, th);
    }
    
    public static void msg(String msg)
    {
        msg(msg, null);
    }
    
    public static void msg(String msg, Throwable th)
    {
        if(th != null)
            th.printStackTrace();
        Toast.makeText(mApp, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Collects crash data.
     * 
     * @param th
     *            Throwable that caused the crash.
     * @return CrashReportData representing the current state of the application
     *         at the instant of the Exception.
     */
    public static HashMap<String, String> createCrashData(Throwable th)
    {
        final HashMap<String, String> crashReportData = new HashMap<String, String>();

        crashReportData.put("APP_VERSION_NAME", JecEditor.version);

        // Device model
        crashReportData.put("PHONE_MODEL", android.os.Build.BRAND+" "+android.os.Build.MODEL);

        // Android version
        crashReportData.put("ANDROID_VERSION", android.os.Build.VERSION.RELEASE);

        WindowManager wm = (WindowManager) mApp.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        crashReportData.put("SCREEN", display.getWidth()+"x"+display.getHeight());
        
        // Device Memory
        ActivityManager activityManager = (ActivityManager) mApp.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        crashReportData.put("FREE_MEM", Long.toString(memoryInfo.availMem/1024/1024)+"M");
        
        crashReportData.put("STACK_TRACE", getStackTrace(th));

        return crashReportData;
    }

    /**
     * Generates the string which is posted in the single custom data field in
     * the GoogleDocs Form.
     * 
     * @return A string with a 'key = value' pair on each line.
     */
    public static String createCrashDataString(Throwable th)
    {
        HashMap<String, String> crashReportData = createCrashData(th);
        final StringBuilder customInfo = new StringBuilder();
        for (final String currentKey : crashReportData.keySet())
        {
            String currentVal = crashReportData.get(currentKey);
            customInfo.append(currentKey);
            customInfo.append(": ");

            customInfo.append(currentVal);
            customInfo.append("\n");
        }
        return customInfo.toString();
    }

    private static String getStackTrace(Throwable th)
    {
        if(th == null)
            return "";
        
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : th.getStackTrace()) {
            if(element.toString().indexOf("jecelyin") > -1)
                sb.append("\tat ").append(element).append("\n");
        }

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = th.getCause();
        while (cause != null)
        {
            sb.append("Caused by: ").append(cause.toString()).append("\n");
            for (StackTraceElement element : cause.getStackTrace()) {
                if(element.toString().indexOf("jecelyin") > -1)
                    sb.append("\tat ").append(element).append("\n");
            }
            cause = cause.getCause();
        }

        return sb.toString();
    }

}
