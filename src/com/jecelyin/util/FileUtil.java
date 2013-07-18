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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jecelyin.android.v2.text.SpannableStringBuilder;

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

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
    public static final int EOF = -1;

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

        if ((ret = size / ONE_GB) > 1.0)
        {
            displaySize = " G";
        } else if ((ret = size / ONE_MB) > 1.0)
        {
            displaySize = " M";
        } else if ((ret = size / ONE_KB) > 1.0)
        {
            displaySize = " KB";
        } else
        {
            ret = size;
            displaySize = " B";
        }

        DecimalFormat df = new DecimalFormat("0.00");

        return df.format(ret) + displaySize;
    }

    /**
     * By default File#delete fails for non-empty directories, it works like
     * "rm". We need something a little more brutual - this does the equivalent
     * of "rm -r"
     * 
     * @param path
     *            Root File Path
     * @return true iff the file and all sub files/directories have been removed
     * @throws FileNotFoundException
     */
    public static boolean remove(File path)
    {
        if (path == null || !path.exists())
            return false;
        boolean ret = true;
        if (path.isDirectory())
        {
            for (File f : path.listFiles())
            {
                ret = ret && remove(f);
            }
        }
        return ret && path.delete();
    }

    public static SpannableStringBuilder readFile(String filename) throws IOException
    {
        return readFile(filename, "UTF-8", LineBreak.NORMAL);
    }

    /**
     * 读取整个文件, android默认编码为utf-8,如果文件编码是gbk或其它编码,要是没有指定正确的编码,就会统一当成ut-8编码处理
     * 
     * @param filename
     *            文件名
     * @param encoding
     *            指定文件编码,否则使用系统默认的编码
     * @return
     * @throws IOException 
     */
    public static String readFileAsString(String filename, String encoding) throws IOException
    {
        return readFileAsString(new File(filename), encoding);
    }

    public static String readFileAsString(File filename, String encoding) throws IOException
    {
        FileInputStream fis = new FileInputStream(filename);
        return readFileAsString(fis, encoding);
    }

    public static String readFileAsString(InputStream input, String encoding) throws IOException
    {
        StringBuilder output = new StringBuilder();

        InputStreamReader isr = new InputStreamReader(input, Charset.forName(encoding));
        int n = 0;
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];

        while (EOF != (n = isr.read(buffer)))
            output.append(new String(buffer, 0, n));

        if (input != null)
            input.close();

        return output.toString();
    }
    
    public static SpannableStringBuilder readFile(String filename, String encoding, int lineBreak) throws IOException
    {
        return readFile(new File(filename), encoding, lineBreak);
    }

    public static SpannableStringBuilder readFile(File filename, String encoding, int lineBreak) throws IOException
    {
        FileInputStream fis = new FileInputStream(filename);
        return readFile(fis, encoding, lineBreak);
    }

    public static SpannableStringBuilder readFile(InputStream input, String encoding, int lineBreak) throws IOException
    {
        SpannableStringBuilder output = new SpannableStringBuilder();

        InputStreamReader isr = new InputStreamReader(input, Charset.forName(encoding));
        int n = 0;
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];

        while (EOF != (n = isr.read(buffer)))
        {
            if(lineBreak != LineBreak.NORMAL)
            {
                output.append(convertLineBreak(buffer, n, lineBreak));
            }else{
                output.append(new String(buffer, 0, n));
            }
        }

        if (input != null)
            input.close();

        return output;
    }
    
    private static String convertLineBreak(char[] buffer, int bufferLength, int lineBreak)
    {
        char[] buffer2 = new char[bufferLength*2];
        int k=0;
        for(int i=0; i<bufferLength; i++)
        {
            if(buffer[i] == '\r')
            {
                if(i+1<bufferLength)
                {
                    if(lineBreak == LineBreak.WIN)
                    {
                        if(buffer[i+1] == '\n'){
                            buffer2[k] = buffer[i];
                            buffer2[++k] = buffer[++i];
                        }else{
                            buffer2[k] = buffer[i];
                            buffer2[++k] = '\n'; //补齐\r\n
                        }
                    }else{
                        if(lineBreak == LineBreak.MAC)
                            buffer2[k] = '\r'; //mac保留这个\r, unix如果下个字符为\n则移除掉它
                        if(buffer[i+1] == '\n')
                            i++;
                        else if(lineBreak == LineBreak.UNIX)
                            buffer2[k] = '\n'; //replace \r to \n
                    }
                    
                }
            }else if(buffer[i] == '\n'){
                if(LineBreak.MAC==lineBreak)
                {
                    buffer2[k] = '\r';
                }else if(LineBreak.WIN==lineBreak){
                    buffer2[k] = '\r';
                    buffer2[++k] = '\n';
                }
                
            }else{
                buffer2[k] = buffer[i];
            }
            k++;
        }
        return new String(buffer2, 0, k);
    }

    public static void writeFile(String path, String text) throws IOException
    {
        writeFile(path, text, "UTF-8", LineBreak.NORMAL, true);
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
    public static boolean writeFile(String path, String text, String encoding, int lineBreak, boolean isRoot) throws IOException
    {
        File file = new File(path);
        String tempFile = JecEditor.TEMP_PATH + "/root_file_buffer.tmp";
        String fileString = path;
        boolean root = false;
        if (!file.canWrite() && isRoot && RootTools.isAccessGiven())
        {
            // 需要Root权限处理
            fileString = tempFile;
            root = true;
        }
        BufferedWriter bw = null;
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileString), encoding));
        
        if(lineBreak == LineBreak.NORMAL)
        {
            bw.write(text);
        }else if(lineBreak == LineBreak.WIN){
            bw.write(text.replaceAll("\r\n|\n|\r", "\r\n"));
        }else if(lineBreak == LineBreak.MAC){
            bw.write(text.replaceAll("\r\n|\n", "\r"));
        }else if(lineBreak == LineBreak.UNIX){
            bw.write(text.replaceAll("\r\n|\r", "\n"));
        }
        
        bw.close();
        if (root)
        {

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
        if (lastIndex == -1)
            return null;
        return path.substring(lastIndex + 1).trim().toLowerCase();
    }

    public static ArrayList<File> getFileList(String path, boolean runAtRoot)
    {
        ArrayList<File> fileList = new ArrayList<File>();
        ArrayList<File> folderList = new ArrayList<File>();
        if (runAtRoot == false)
        {
            File base = new File(path);
            File[] files = base.listFiles();
            if (files == null)
                return null;
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    folderList.add(file);
                } else
                {
                    fileList.add(file);
                }
            }
        } else
        {
            /** 带 root */
            try
            {
                // -1 One column output
                // -F Append indicator (one of */=@|) to entries * 表示普通的可执行文件； /
                // 表示目录； @ 表示符号链接；| 表示FIFOs；= 表示套接字 (sockets) ；什么也没有则表示普通文件。
                List<String> resultList = RootTools.sendShell("busybox ls -1 "
                        + path, 1000);
                File file;
                for (String line : resultList)
                {
                    if ("".equals(line.trim()) || "0".equals(line.trim()))
                        continue;
                    file = new File(path, line);
                    if (line.endsWith("/") || file.isDirectory())
                    {
                        folderList.add(file);
                    } else
                    {
                        fileList.add(file);
                    }
                }

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Comparator<File> mComparator = new Comparator<File>()
        {
            public int compare(File fl1, File fl2)
            {
                return fl1.getName().compareToIgnoreCase(fl2.getName());
            }
        };
        // 排序
        Collections.sort(fileList, mComparator);
        Collections.sort(folderList, mComparator);

        ArrayList<File> list = new ArrayList<File>();
        for (File f : folderList)
            list.add(f);
        for (File f : fileList)
            list.add(f);

        fileList = null;
        folderList = null;

        return list;
    }

}
