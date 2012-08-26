package org.mozilla.charsetdetector;

public class CharsetDetector
{
    static {
        System.loadLibrary("CharsetDetector");
    }
    
    public static String getEncoding(String file)
    {
        String encoding = get_encoding(file);
        return encoding;
    }
    
    private native static String get_encoding(String file);
}
