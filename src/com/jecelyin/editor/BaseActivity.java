package com.jecelyin.editor;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        JecApp.addActivity(this);
    }
    
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
