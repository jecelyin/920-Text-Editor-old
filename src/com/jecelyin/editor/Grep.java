package com.jecelyin.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jecelyin.util.FileBrowser;

public class Grep extends Activity
{
    static {
        System.loadLibrary("grep");
    }
    
    public native static void find(String[] cmd);

    private static ArrayList<GrepResult> mGrepResults = new ArrayList<GrepResult>();

    private JecEditor mJecEditor;
    private boolean isRoot = false;

    private EditText keywordEditText;
    private EditText pathEditText;
    private CheckBox recurseCheckBox;
    private CheckBox ignorecaseCheckBox;
    private CheckBox regexCheckBox;
    private CheckBox wholewordCheckBox;
    private ListView filelistsListView;

    private ProgressDialog mProgressDialog;

    private SearchHandler mSearchHandler;

    private ResultAdapter adapter;
    
    public static void clearResult()
    {
        mGrepResults.clear();
    }

    public static void addMatchResult(String file, int linenum, String line, long offset)
    {
        //Log.d("Grep", file+":"+linenum+":"+line);
        mGrepResults.add(new GrepResult(file, linenum, line, offset));
    }
    
    public static ArrayList<GrepResult> getResults()
    {
        return mGrepResults;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder_search);
        Intent it = getIntent();
        final String path = it.getStringExtra("path");
        String keyword = it.getStringExtra("keyword");
        isRoot = it.getBooleanExtra("isRoot", false);

        keywordEditText = (EditText) findViewById(R.id.keyword);
        keywordEditText.setText(keyword);
        pathEditText = (EditText) findViewById(R.id.path);
        recurseCheckBox = (CheckBox) findViewById(R.id.recurse);
        ignorecaseCheckBox = (CheckBox) findViewById(R.id.ignore_case);
        regexCheckBox = (CheckBox) findViewById(R.id.use_regex);
        wholewordCheckBox = (CheckBox) findViewById(R.id.match_whole_word);
        filelistsListView = (ListView) findViewById(R.id.filelists);
        
        pathEditText.setText("".equals(path) ? android.os.Environment.getExternalStorageDirectory().getPath() : path);

        Button browse = (Button) findViewById(R.id.browse);
        browse.setOnClickListener(new OnClickListener() {

            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.putExtra("path", path);
                intent.putExtra("mode", FileBrowser.MODE_BROWSE);
                intent.putExtra("isRoot", isRoot);
                intent.setClass(Grep.this, FileBrowser.class);
                startActivityForResult(intent, FileBrowser.MODE_BROWSE);
            }
        });
        ImageButton search = (ImageButton) findViewById(R.id.search);
        search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                search();
            }
        });
        adapter = new ResultAdapter(this.getApplicationContext(), R.layout.search_list, mGrepResults);
        filelistsListView.setAdapter(adapter);
        filelistsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                GrepResult res = adapter.getItem(position);
                Intent intent = new Intent();
                intent.putExtra("file", res.file);
                intent.putExtra("offset", res.offset);
                intent.putExtra("line", res.lineNum);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * startActivityForResult回调函数
     * 
     * @param requestCode
     *            这里的requestCode就是前面启动新Activity时的带过去的requestCode
     * @param resultCode
     *            resultCode则关联上了setResult中的resultCode
     * @param data
     *            返回的Intent参数
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(RESULT_OK != resultCode)
        {
            return;
        }

        if(requestCode == FileBrowser.MODE_BROWSE)
        {
            pathEditText.setText(data.getStringExtra("path"));
        }
    }

    private void search()
    {
        String pathStr = pathEditText.getText().toString().trim();
        if(pathStr == null || "".equals(pathStr))
        {
            msgbox(R.string.path_not_correct);
            return;
        }
        File path = new File(pathStr);
        if(!path.exists())
        {
            msgbox(R.string.path_not_correct);
            return;
        }
        String keyword = keywordEditText.getText().toString();
        if("".equals(keyword))
        {
            msgbox(R.string.keyword_notbe_empty);
            return;
        }
        final ArrayList<String> cmd = new ArrayList<String>();
        if(recurseCheckBox.isChecked())
            cmd.add("-r");
        if(ignorecaseCheckBox.isChecked())
            cmd.add("-i");
        if(wholewordCheckBox.isChecked())
            cmd.add("-w");
        if(regexCheckBox.isChecked())
            cmd.add("-E");
        
        cmd.add(keyword);
        cmd.add(path.getAbsolutePath());

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.spinner_message);
        mProgressDialog.setMessage(getText(R.string.searching));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog)
            {
                dismissProgress();
            }
        });
        mProgressDialog.show();

        mSearchHandler = new SearchHandler(Grep.this);
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run()
            {
                Message msg = mSearchHandler.obtainMessage();
                clearResult();
                String[] args = cmd.toArray(new String[cmd.size()]);
                find(args);
                msg.sendToTarget();
            }
        });
        thread.start();
    }

    private void msgbox(int id)
    {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }

    public void dismissProgress()
    {
        if(mProgressDialog != null)
            mProgressDialog.dismiss();
    }
    
    public void showResult()
    {
        adapter.notifyDataSetChanged();
    }

    static class SearchHandler extends Handler
    {
        private Grep mGrep;

        public SearchHandler(Grep grep)
        {
            super();
            mGrep = grep;
        }

        @Override
        public void handleMessage(Message msg)
        {
            mGrep.showResult();
            //Grep.clearResult();
            mGrep.dismissProgress();
            Toast.makeText(mGrep, mGrep.getString(R.string.find_result).replaceAll("%s", String.valueOf(getResults().size())), Toast.LENGTH_LONG).show();
        }
    }

}

class GrepResult
{
    public String file;
    public int lineNum;
    public String line;
    public long offset;

    public GrepResult(String file, int linenum, String line, long offset)
    {
        this.file = file;
        this.lineNum = linenum;
        this.line = line;
        this.offset = offset;
    }
}

class ResultAdapter extends ArrayAdapter<GrepResult>
{

    private LayoutInflater mInflater;
    private String lineStr;

    public ResultAdapter(Context context, int textViewResourceId, List<GrepResult> objects)
    {
        super(context, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
        lineStr = mInflater.getContext().getString(R.string.line_num);
    }
    
    public View getView(int position, View view, ViewGroup parent)
    {
        TextView file, line, code;

        if (view == null) {
            view = mInflater.inflate(R.layout.search_list, null);
        }

        file = (TextView) view.findViewById(R.id.file);
        line = (TextView) view.findViewById(R.id.line);
        code = (TextView) view.findViewById(R.id.code);
        GrepResult res = getItem(position);
        file.setText(res.file);
        line.setText(lineStr.replaceAll("%s", String.valueOf(res.lineNum)));
        code.setText(res.line);

        return view;
    }
}
