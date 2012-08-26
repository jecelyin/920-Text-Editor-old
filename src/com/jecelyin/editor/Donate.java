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

package com.jecelyin.editor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

public class Donate extends Activity
{
    //private String html_url = "file:///android_asset/donate.html";
    private String html_url = "http://www.jecelyin.com/donate.html";
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donate);
        WebView mWebView = (WebView)findViewById(R.id.donateWebView);
        mWebView.loadUrl(html_url);
    }
    
    public static Intent getWebIntent()
    {
        Uri uri = Uri.parse("http://www.jecelyin.com/donate.html");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        //startActivity(intent);
        return intent;
    }
}
