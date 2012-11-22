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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.jecelyin.editor.JecEditor;
import com.stericson.RootTools.RootTools;


public class FileUtil
{
    /***
     * The number of bytes in a kilobyte.
     */
    public static final double ONE_KB = 1024.0;

    /***
     * The number of bytes in a megabyte.
     */
    public static final double ONE_MB = ONE_KB * ONE_KB;

    /***
     * The number of bytes in a gigabyte.
     */
    public static final double ONE_GB = ONE_KB * ONE_MB;

    /***
     * Returns a human-readable version of the file size, where the input
     * represents a specific number of bytes.
     * 
     * @param size
     *            the number of bytes
     * @return a human-readable display value (includes units)
     */
    public static String byteCountToDisplaySize(long size)
    {
        return byteCountToDisplaySize((double) size);
    }

    public static String byteCountToDisplaySize(double size)
    {
        String displaySize;
        double ret;

        if((ret = size / ONE_GB) > 1.0)
        {
            displaySize = " G";
        }else if((ret = size / ONE_MB) > 1.0)
        {
            displaySize = " M";
        }else if((ret = size / ONE_KB) > 1.0)
        {
            displaySize = " KB";
        }else
        {
            ret = size;
            displaySize = " B";
        }

        DecimalFormat df = new DecimalFormat("0.00");

        return df.format(ret) + displaySize;
    }

    public static String ReadFile(String filename)
    {
        return ReadFile(filename, "UTF-8");
    }

    public static String Read(String filename, String encoding)
    {
        return Read(new File(filename), encoding);
    }

    public static String Read(File file, String encoding)
    {

        try
        {
            BufferedReader in = new BufferedReader(new FileReader(file));

            // Create an array of characters the size of the file
            char[] allChars = new char[(int) file.length()];

            // Read the characters into the allChars array
            in.read(allChars, 0, (int) file.length());
            in.close();

            // Convert to a string
            String allCharsString = new String(allChars);
            return allCharsString;
        }catch (IOException ex)
        {
            throw new RuntimeException(file + ": trouble reading", ex);
        }

    }
    
    /**
     * 读取整个文件, android默认编码为utf-8,如果文件编码是gbk或其它编码,要是没有指定正确的编码,就会统一当成ut-8编码处理
     * 
     * @param filename
     *            文件名
     * @param encoding
     *            指定文件编码,否则使用系统默认的编码
     * @return
     */
    public static String ReadFile(String filename, String encoding)
    {
        return ReadFile(new File(filename), encoding);
    }
    
    public static String ReadFile(File filename, String encoding)
    {
        try
        {
            FileInputStream fis = new FileInputStream(filename);
            return ReadFile(fis, encoding);
        }catch (FileNotFoundException e)
        {
            return "";
        }
    }
    
    public static String ReadFile(InputStream fis, String encoding)
    {
        BufferedReader br;
        StringBuilder b = new StringBuilder();
        String line;
        String sp = System.getProperty("line.separator");

        try
        {
            br = new BufferedReader(new InputStreamReader(fis, encoding));
            try
            {
                while ((line = br.readLine()) != null)
                {
                    b.append(line).append(sp);
                }
                br.close();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return b.toString();
    }

    public static void writeFile(String path, String text) throws IOException
    {
        writeFile(path, text, "UTF-8", true);
    }

    /**
     * 写入文件, 需要指定编码
     * 
     * @param path
     * @param text
     * @param encoding
     * @return
     * @throws IOException 
     */
    public static boolean writeFile(String path, String text, String encoding, boolean isRoot) throws IOException
    {
        File file = new File(path);
        String tempFile = JecEditor.TEMP_PATH + "/root_file_buffer.tmp";
        String fileString = path;
        boolean root = false;
        if(!file.canWrite() && isRoot && RootTools.isAccessGiven())
        {
            //需要Root权限处理
            fileString = tempFile;
            root = true;
        }
        BufferedWriter bw = null;
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileString),encoding));
        bw.write(text);
        bw.close();
        if(root)
        {
            //RootTools.remount(path, "RW");
            //RootTools.sendShell("busybox cat " + fileString + " > " + LinuxShell.getCmdPath(path), 1000);
            RootTools.copyFile(fileString, LinuxShell.getCmdPath(path), true, true);
            if (RootTools.lastExitCode != 0)
                return false;
            new File(tempFile).delete();
        }
        return true;
    }

    public static String getExt(String path)
    {
        int lastIndex = path.lastIndexOf(".");
        if(lastIndex == -1)
            return null;
        return path.substring(lastIndex + 1).trim().toLowerCase();
    }

    public static ArrayList<File> getFileList(String path, boolean runAtRoot)
    {
        ArrayList<File> fileList = new ArrayList<File>();
        ArrayList<File> folderList = new ArrayList<File>();
        if(runAtRoot == false)
        {
            File base = new File(path);
            File[] files = base.listFiles();
            if(files == null)
                return null;
            for(File file: files)
            {
                if(file.isDirectory())
                {
                    folderList.add(file);
                } else {
                    fileList.add(file);
                }
            }
        }else{
            /** 带 root */
            try
            {
                //-1    One column output
                //-F    Append indicator (one of */=@|) to entries  * 表示普通的可执行文件； / 表示目录； @ 表示符号链接；| 表示FIFOs；= 表示套接字 (sockets) ；什么也没有则表示普通文件。
                List<String> resultList = RootTools.sendShell("busybox ls -1 " + path, 1000);
                File file;
                for(String line: resultList)
                {
                    if("".equals(line.trim()) || "0".equals(line.trim()))
                        continue;
                    file = new File(path, line);
                    if(line.endsWith("/") || file.isDirectory())
                    {
                        folderList.add(file);
                    } else {
                        fileList.add(file);
                    }
                }
                
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Comparator<File> mComparator = new Comparator<File>() {
            public int compare(File fl1, File fl2)
            {
                return fl1.getName().compareToIgnoreCase(fl2.getName());
            }
        };
        //排序
        Collections.sort(fileList, mComparator);
        Collections.sort(folderList, mComparator);
        
        ArrayList<File> list = new ArrayList<File>();
        for(File f:folderList)
            list.add(f);
        for(File f:fileList)
            list.add(f);
        
        fileList = null;
        folderList = null;
        
        return list;
    }

}
