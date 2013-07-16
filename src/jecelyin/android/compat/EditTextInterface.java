package jecelyin.android.compat;

public interface EditTextInterface
{
    public void init();

    public void show();
    public void hide();

    public String getString();

    /**
     * 更新文本指纹，用于判断文本是否有改动
     */
    public void updateTextFinger();
    
    public boolean isTextChanged();
}
