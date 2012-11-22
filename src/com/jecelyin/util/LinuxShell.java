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

package com.jecelyin.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.stericson.RootTools.RootTools;

import android.util.Log;

public class LinuxShell
{
    public static String getCmdPath(String path)
    {
        return path.replace(" ", "\\ ").replace("'", "\\'");
    }
    
    /**
     * 返回执行完成的结果
     * @param cmd 命令内容
     * @return
     */
    public static BufferedReader execute(String cmd)
    {
        BufferedReader reader = null; //errReader = null;
        try
        {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            //os.writeBytes("mount -oremount,rw /dev/block/mtdblock3 /system\n");
            //os.writeBytes("busybox cp /data/data/com.koushikdutta.superuser/su /system/bin/su\n");
            os.writeBytes(cmd+"\n");
            os.writeBytes("exit\n");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String err = (new BufferedReader(new InputStreamReader(process.getErrorStream()))).readLine();
            os.flush();

            if(process.waitFor() != 0 || (!"".equals(err) && null != err))
            {
                Log.e("920TERoot", err);
                return null;
            }
            return reader;
        }catch (IOException e)
        {
            e.printStackTrace();
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static boolean canRoot()
    {
        return RootTools.isRootAvailable() || RootTools.isBusyboxAvailable();
    }

}


