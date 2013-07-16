package org.mozilla.charsetdetector;

public class CharsetDetector
{
    static {
        System.loadLibrary("CharsetDetector");
    }
    
    long mDet;
    public CharsetDetector()
    {
        mDet = chardet_create();
    }

    public boolean isOK()
    {
        return mDet != 0;
    }

    public void destroy()
    {
        chardet_destroy(mDet);
    }
    public int handleData( byte[] data , int offset , int len)
    {
        return chardet_handle_data( mDet , data , offset , len );
    }
    public int dataEnd()
    {
        return chardet_data_end(mDet);
    }
    public int reset()
    {
        return chardet_reset(mDet);
    }
    public String getCharset()
    {
        String ret = chardet_get_charset(mDet);
        if ( ret.length() == 0 ){
            ret = null;
        }
        return ret;
    }

    /**
     * Create an encoding detector.
     * @param pdet [out] pointer to a long variable that receives
     *             the encoding detector handle.
     * @return CHARDET_RESULT_OK if succeeded. CHARDET_RESULT_NOMEMORY otherwise.
     */
    private native static long chardet_create();

    /**
     * Destroy an encoding detector.
     * @param det [in] the encoding detector handle to be destroyed.
     */
    private native static void chardet_destroy(long det);

    /**
     * Feed data to an encoding detector.
     * @param det [in] the encoding detector handle
     * @param data [in] data
     * @param len [in] length of data in bytes.
     * @return CHARDET_RESULT_OK if succeeded.
     *         CHARSET_RESULT_NOMEMORY if running out of memory.
     *         CHARDET_RESULT_INVALID_DETECTOR if det was invalid.
     */
    private native static int chardet_handle_data(long det, byte[] data, int offset , int len);

    /**
     * Notify an end of data to an encoding detctor.
     * @param det [in] the encoding detector handle
     * @return CHARDET_RESULT_OK if succeeded.
     *         CHARDET_RESULT_INVALID_DETECTOR if det was invalid.
     */
    private native static int chardet_data_end(long det);

    /**
     * Reset an encoding detector.
     * @param det [in] the encoding detector handle
     * @return CHARDET_RESULT_OK if succeeded.
     *         CHARDET_RESULT_INVALID_DETECTOR if det was invalid.
     */
    private native static int chardet_reset(long det);

    /**
     * Get the name of encoding that was detected.
     * @param det [in] the encoding detector handle
     * @param namebuf [in/out] pointer to a buffer that receives the name of
     *                detected encoding. A valid encoding name or an empty string
     *                will be written to namebuf. If an empty strng was written,
     *                the detector could not detect any encoding.
     *                Written strings will always be NULL-terminated.
     * @param buflen [in] length of namebuf
     * @return CHARDET_RESULT_OK if succeeded.
     *         CHARDET_RESULT_NOMEMORY if namebuf was too small to store
     *         the entire encoding name.
     *         CHARDET_RESULT_INVALID_DETECTOR if det was invalid.
     */
    private native static String chardet_get_charset(long det);
}
