package jecelyin.android.compat;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;

public class TextViewBase extends View implements TextViewInterface, EditTextInterface
{

    public TextViewBase(Context context)
    {
        this(context, null);
    }

    public TextViewBase(Context context, AttributeSet attrs)
    {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public TextViewBase(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    //对外接口 start
    public void setTextSize(float size) {}
    
    public CharSequence getText(){ return null; }
    
    public void setText(CharSequence text) {};
    public Editable getEditableText(){return null;}
    
    public int getSelectionStart(){return 0;}
    public int getSelectionEnd(){return 0;}
    
    public int length() {return 0;}
    
    public void setSelection(int start, int stop){};
    //end
    
    protected static boolean mHideSoftKeyboard=false;
    public static void setHideKeyboard(boolean bool)
    {
        mHideSoftKeyboard = bool;
    }

    @Override
    public void init()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean gotoLine(int line)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getPath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPath(String path)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCurrentFileExt(String ext)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getCurrentFileExt()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTitle(String title)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getTitle()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reHighlight()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canUndo()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canRedo()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void unDo()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void reDo()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetUndoStatus()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUndoRedoButtonStatus()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean gotoBackEditLocation()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean gotoForwardEditLocation()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCanBackEditLocation()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCanForwardEditLocation()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addJecOnTextChangedListener(JecOnTextChangedListener l)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateLineNumberWidth()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLineNumberFontSize(float size)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLineNumberColor(int color)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showIME(boolean show)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getEncoding()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setEncoding(String encoding)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLineBreak(int linebreak)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getLineBreak()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateTextFinger()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void show()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hide()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getString()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isTextChanged()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
