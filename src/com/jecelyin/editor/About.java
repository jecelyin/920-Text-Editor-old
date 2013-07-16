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

import android.os.Bundle;
import android.widget.TextView;

public class About extends BaseActivity
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        JecApp.addActivity(this);
        setContentView(R.layout.about);

        TextView appNameField = (TextView) findViewById(R.id.field_app_name);
        appNameField.setText(R.string.app_name);

        TextView idField = (TextView) findViewById(R.id.field_version);
        idField.setText("v"+JecEditor.version);
    }
}