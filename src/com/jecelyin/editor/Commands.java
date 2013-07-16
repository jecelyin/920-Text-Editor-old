package com.jecelyin.editor;

import android.util.SparseArray;

public class Commands
{

    public static class Command
    {
        public int name;
        public int icon;
        public String hotkey;
        
        public Command(int name, int icon, String hotkey)
        {
            this.name = name;
            this.icon = icon;
            this.hotkey = hotkey;
        }
    }
    
    public static final int DIVIDER_NAME = R.string.settings_divider_text;
    public static final Command Divider = new Command(DIVIDER_NAME, R.drawable.settings_divider, "");
    
    public static final Command[] COMMAND_LIST = new Command[]{
        new Command(R.string.open, R.drawable.open_file_sel2, "CTRL+O")
        ,new Command(R.string.save, R.drawable.save_sel2, "CTRL+S")
        ,new Command(R.string.undo, R.drawable.undo_no2, "CTRL+Z")
        ,new Command(R.string.redo, R.drawable.redo_no2, "CTRL+Y")
        ,new Command(R.string.symbol_bar, R.drawable.symbol_s2, "CTRL+B")
        ,new Command(R.string.back, R.drawable.back_edit_location_d2, "")
        ,new Command(R.string.forward, R.drawable.forward_edit_location_d2, "")
        ,new Command(R.string.find_in_files, R.drawable.folder_search_sel2, "")
        ,new Command(R.string.preview, R.drawable.preview_sel2, "CTRL+P")
        ,new Command(R.string.color, R.drawable.tool_color_sel2, "")
    };
    
    public static final SparseArray<Command> COMMAND_MAP = new SparseArray<Command>(COMMAND_LIST.length);
    static {
        for(Command c:COMMAND_LIST)
            COMMAND_MAP.put(c.name, c);
    }
    
    public static final int DEFAULT_TOOLBAR_COMMANDS[] = new int[]{
        R.string.open
        ,R.string.save
        ,R.string.undo
        ,R.string.redo
        ,R.string.symbol_bar
        ,R.string.back
        ,R.string.forward
        ,R.string.find_in_files
        ,R.string.preview
        ,R.string.color
    };

}
