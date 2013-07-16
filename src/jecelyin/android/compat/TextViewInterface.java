package jecelyin.android.compat;

public interface TextViewInterface
{
    void init();
    
    boolean gotoLine(int line);
    
    String getPath();
    void setPath(String path);
    
    void setCurrentFileExt(String ext);
    String getCurrentFileExt();
    
    void setTitle(String title);
    String getTitle();
    
    void reHighlight();

    boolean canUndo();
    boolean canRedo();

    void unDo();
    void reDo();
    
    void resetUndoStatus();
    void updateUndoRedoButtonStatus();
    
    boolean gotoBackEditLocation();
    boolean gotoForwardEditLocation();
    
    boolean isCanBackEditLocation();
    boolean isCanForwardEditLocation();
    
    void addJecOnTextChangedListener(JecOnTextChangedListener l);
    
    void updateLineNumberWidth();
    
    void setLineNumberFontSize(float size);
    
    void setLineNumberColor(int color);
    
    void showIME(boolean show);
    
    String getEncoding();
    void setEncoding(String encoding);
    
    void setLineBreak(int linebreak);
    int getLineBreak();
}
