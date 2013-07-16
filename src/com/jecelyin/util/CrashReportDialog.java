package com.jecelyin.util;

import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jecelyin.editor.R;

public class CrashReportDialog extends Activity implements DialogInterface.OnClickListener, OnDismissListener
{
    private EditText mComment;
    private EditText mailEditText;
    private String mTrace;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent it = getIntent();
        mTrace = it.getStringExtra("msg")+"\n"+it.getStringExtra("trace");
        show();
    }
    

    public void show()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.crash_report);
        dialogBuilder.setView(buildCustomView());
        dialogBuilder.setPositiveButton(android.R.string.ok, CrashReportDialog.this);
        dialogBuilder.setNegativeButton(android.R.string.cancel, CrashReportDialog.this);
        
        AlertDialog dialog = dialogBuilder.create();
        
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(this);
        dialog.show();
    }
    
    private View buildCustomView()
    {
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10, 10, 10, 10);
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        final ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        final LinearLayout scrollable = new LinearLayout(this);
        scrollable.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(scrollable);

        // Add an optional prompt for user comments
        final TextView label = new TextView(this);
        label.setText(R.string.comment);

        label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
        scrollable.addView(label, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        mComment = new EditText(this);
        mComment.setLines(2);
        scrollable.addView(mComment);

        // Add an optional user email field
        final TextView label1 = new TextView(this);
        label1.setText(R.string.label_email);
        label1.setPadding(label1.getPaddingLeft(), 10, label1.getPaddingRight(), label1.getPaddingBottom());
        scrollable.addView(label1);

        mailEditText = new EditText(this);
        mailEditText.setSingleLine();
        mailEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        scrollable.addView(mailEditText);

        final TextView text = new TextView(this);
        text.setText(mTrace);

        scrollable.addView(text);

        return root;
    }

    
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if (which != DialogInterface.BUTTON_POSITIVE) // no ok
        {
            close();
            return;
        }
        String mail = URLEncoder.encode(mailEditText.getText().toString().trim());
        String content = URLEncoder.encode(mComment.getText().toString().trim()+"\n## "+mTrace);
        Intent i = new Intent( Intent.ACTION_VIEW );
        i.setData( Uri.parse( "http://www.jecelyin.com/920report.php?ver=crash&email="+mail+"&content="+content ) );
        startActivity( i );
        close();
    }


    @Override
    public void onDismiss(DialogInterface dialog)
    {
        close();
    }
    
    private void close()
    {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}