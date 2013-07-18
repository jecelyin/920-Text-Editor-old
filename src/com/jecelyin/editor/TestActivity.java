package com.jecelyin.editor;

import java.io.IOException;

import com.jecelyin.util.FileUtil;

import jecelyin.android.v2.widget.EditText;

import android.os.Bundle;
import android.app.Activity;

public class TestActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
        String data="";
        try
        {
            data = FileUtil.readFileAsString("/storage/sdcard0/jecelyin/moodlelib.php", "utf-8");
            //data = FileUtil.readFileAsString("/sdcard/moodlelib.php", "utf-8");
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        EditText mEditText = (EditText)findViewById(R.id.text_content);
        mEditText.setText(data);
    }

}
