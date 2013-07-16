package com.jecelyin.editor;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;


import com.jecelyin.util.JecLog;

import android.app.Activity;
import android.app.Application;

public class JecApp extends Application
{
    private static UncaughtExceptionHandler mDefaultHandler;
    private static volatile boolean mCrashing = false;
    private final static ArrayList<WeakReference<Activity>> activitys = new ArrayList<WeakReference<Activity>>();

    @Override
    public void onCreate()
    {
        super.onCreate();
        JecLog.init(this);
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 捕捉未知异常
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler());
        EditorSettings.initialize(this);
    }
    
    public static void addActivity(Activity context)
    {
        JecLog.d("addActivity="+context);
        activitys.add(new WeakReference<Activity>(context));
    }
    
    public static void removeActivity(Activity context)
    {
        JecLog.d("removeActivity="+context);
        activitys.remove(new WeakReference<Activity>(context));
    }
    
    private static void closeAllActivitys()
    {
        Iterator<WeakReference<Activity>> iter = activitys.iterator();
        while (iter.hasNext())
        {
          Activity activity = (Activity)((WeakReference<Activity>)iter.next()).get();
          if ((activity != null) && (!activity.isFinishing()))
              activity.finish();
        }
    }

    private static class UncaughtHandler implements
            Thread.UncaughtExceptionHandler
    {
        @Override
        public void uncaughtException(Thread thread, Throwable ex)
        {
            if (mCrashing)
                return;
            mCrashing = true;
            closeAllActivitys();
            JecLog.e("UnknowException for " + ex.getClass().getName(), ex);
            //mDefaultHandler.uncaughtException(thread, ex);

            //Process.killProcess(Process.myPid());
            //System.exit(10);
        }
    }
}
