package com.m3u8test;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.m3u8test.bean.M3U8;
import com.m3u8test.listener.OnTaskDownloadListener;
import com.m3u8test.m3u8.M3U8DownloadTask;
import com.m3u8test.m3u8.M3U8DownloaderConfig;
import com.m3u8test.m3u8.M3U8Task;
import com.m3u8test.m3u8.M3U8TaskState;
import com.m3u8test.utils.M3U8Log;
import com.m3u8test.utils.PasteTool;
import com.m3u8test.utils.StorageUtils;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {
    private M3U8Task currentM3U8Task;
    private TextView name;
    private TextView state;
    private TextView progress;
    private M3U8DownloadTask m3U8DownLoadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second);
        requestAppPermissions();
    }

    private void requestAppPermissions() {
        Dexter.withActivity(this)
                .withPermissions(Const.PERMISSIONS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            initView();
                        }else {
                            Toast.makeText(getApplicationContext(),"权限获取失败",Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    }
                }).check();
    }

    private void initView() {
        name = findViewById(R.id.url_tv);
        state = findViewById(R.id.state_tv);
        progress = findViewById(R.id.progress_tv);
        initData();
    }

    private void initData(){
        String dirPath = StorageUtils.getCacheDirectory(this).getPath();
        M3U8DownloaderConfig
                .build(getApplicationContext())
                .setSaveDir(dirPath)
                .setThreadCount(5)
                .setDebugMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String str = PasteTool.getPasteStr(this);
        if(!TextUtils.isEmpty(str)) {
            m3U8DownLoadTask = new M3U8DownloadTask();
            currentM3U8Task = new M3U8Task(str);
            m3U8DownLoadTask.download(str, onTaskDownloadListener);
            name.setText(str);
        }
    }

    private OnTaskDownloadListener onTaskDownloadListener = new OnTaskDownloadListener() {
        private long lastLength;
        private float downloadProgress;
        @Override
        public void onStartDownload(int totalTs, int curTs) {
            M3U8Log.e("onStartDownload: "+totalTs+"|"+curTs);
            currentM3U8Task.setState(M3U8TaskState.DOWNLOADING);
            downloadProgress = 1.0f * curTs / totalTs;
            state.setText("开始下载");
        }

        @Override
        public void onDownloading(long totalFileSize, long itemFileSize, int totalTs, int curTs) {
//            if (!m3U8DownLoadTask.isRunning())return;
            M3U8Log.e("onDownloading: "+totalFileSize+"|"+itemFileSize+"|"+totalTs+"|"+curTs);
            downloadProgress = 1.0f * curTs / totalTs;
            if(curTs==0) setTextOnUiThread(state,"下载中");
            setTextOnUiThread(progress,curTs+"/"+totalTs);
        }

        @Override
        public void onSuccess(M3U8 m3U8) {
            m3U8DownLoadTask.stop();
            currentM3U8Task.setM3U8(m3U8);
            currentM3U8Task.setState( M3U8TaskState.SUCCESS);
            M3U8Log.e("m3u8 Downloader onSuccess: "+ m3U8);
            setTextOnUiThread(state,"完成下载");
        }

        @Override
        public void onProgress(long curLength) {
            if (curLength - lastLength > 0) {
                currentM3U8Task.setProgress(downloadProgress);
                currentM3U8Task.setSpeed(curLength - lastLength);
                lastLength = curLength;
            }
        }

        @Override
        public void onStart() {
            currentM3U8Task.setState(M3U8TaskState.PREPARE);
            M3U8Log.e("onDownloadPrepare: "+ currentM3U8Task.getUrl());
        }

        @Override
        public void onError(Throwable errorMsg) {
            if (errorMsg.getMessage() != null && errorMsg.getMessage().contains("ENOSPC")){
                currentM3U8Task.setState(M3U8TaskState.ENOSPC);
            }else {
                currentM3U8Task.setState(M3U8TaskState.ERROR);
            }
            M3U8Log.e("onError: " + errorMsg.getMessage());
            setTextOnUiThread(state,"下载错误");
        }
    };

    void setTextOnUiThread(final TextView tv, final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(str);
            }
        });
    }
}
