package com.jecelyin.editor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.jecelyin.util.FileUtil;

public class Help
{

    public static void showChangesLog(Context mContext)
    {
        popupWindow(mContext, R.string.changelog, "CHANGES");
    }
    
    public static void showHelp(Context mContext)
    {
        popupWindow(mContext, R.string.help, "HELP");
    }

    private static void popupWindow(final Context mContext, int title, String file)
    {
        String text;
        try
        {
            text = FileUtil.readFileAsString(mContext.getAssets().open(file), "utf-8");
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setIcon(0).setTitle(title).setMessage(text).setPositiveButton(android.R.string.ok, null);

            builder.setNegativeButton(R.string.donate, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                    mContext.startActivity(Donate.getWebIntent());
                }
            });
            
            builder.show();
        }catch (Exception e)
        {
            return;
        }
    }
}
