package com.m3u8test;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.m3u8test.adapter.ThirdListAdapter;
import com.m3u8test.bean.M3U8;
import com.m3u8test.bean.SpBean;
import com.m3u8test.listener.OnTaskDownloadListener;
import com.m3u8test.m3u8.M3U8DownloadTask;
import com.m3u8test.m3u8.M3U8DownloaderConfig;
import com.m3u8test.m3u8.M3U8Task;
import com.m3u8test.m3u8.M3U8TaskState;
import com.m3u8test.utils.JsonTool;
import com.m3u8test.utils.M3U8Log;
import com.m3u8test.utils.PasteTool;
import com.m3u8test.utils.SPHelper;
import com.m3u8test.utils.StorageUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class ThirdActivity extends AppCompatActivity {
    private M3U8DownloadTask m3U8DownLoadTask;
    List<M3U8Task> taskList = new ArrayList<>();
    List<M3U8DownloadTask> downloadTaskList = new ArrayList<>();
    List<OnTaskDownloadListener> listenerList = new ArrayList<>();
    private ListView listView;
    private ThirdListAdapter adapter;
    boolean isAfterOnCreate = false;
//    private M3U8Task currentM3U8Task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third);
        requestAppPermissions();
        initView();
        initData();
        isAfterOnCreate = true;
    }

    private void requestAppPermissions() {
        Dexter.withActivity(this)
                .withPermissions(Const.PERMISSIONS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

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
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = taskList.get(position).getUrl();
                if (downloadTaskList.get(position).getM3u8File(url).exists()){
                    Intent intent = new Intent(ThirdActivity.this, PlayActivity.class);
                    intent.putExtra("M3U8_URL",downloadTaskList.get(position).getM3u8File(url).getPath());
                    startActivity(intent);
                } else {
                    downloadTaskList.get(position).download(taskList.get(position).getUrl(), listenerList.get(position));
                }
            }
        });
    }

    private void initData(){
        SPHelper.init(this);
//        String dirPath = StorageUtils.getCacheDirectory(this).getPath();
        String dirPath = StorageUtils.getOwnCacheDirectory(this,"123").getPath();
        M3U8DownloaderConfig
                .build(getApplicationContext())
                .setSaveDir(dirPath)
                .setThreadCount(1)
                .setDebugMode(true);
        //读取sp中集合
        String jsonArrayStr = SPHelper.getString(Const.JsonKey,"");
        List<SpBean> beanList = null;
        try {
            beanList = JsonTool.Json2SpBean(jsonArrayStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int i = 0;
        if(beanList!=null&& beanList.size()>0){
            for(SpBean bean : beanList) {
                m3U8DownLoadTask = new M3U8DownloadTask();
                downloadTaskList.add(m3U8DownLoadTask);
                final M3U8Task currentM3U8Task = new M3U8Task(bean.getUrl());
                taskList.add(currentM3U8Task);
                final int j = i;
                OnTaskDownloadListener listener = new OnTaskDownloadListener() {
                    private long lastLength;
                    private float downloadProgress;
                    @Override
                    public void onStartDownload(int totalTs, int curTs) {
                        M3U8Log.e("onStartDownload: "+j +" "+totalTs+"|"+curTs);
                        currentM3U8Task.setState(M3U8TaskState.DOWNLOADING);
                        downloadProgress = 1.0f * curTs / totalTs;
                        notifyChanged(currentM3U8Task);
                    }

                    @Override
                    public void onDownloading(long totalFileSize, long itemFileSize, int totalTs, int curTs) {
                        M3U8Log.e("onDownloading: "+j +" "+totalFileSize+"|"+itemFileSize+"|"+totalTs+"|"+curTs);
                        downloadProgress = 1.0f * curTs / totalTs;
                        currentM3U8Task.setProgressStr(curTs+"/"+totalTs);
                        notifyChanged(currentM3U8Task);
                    }

                    @Override
                    public void onSuccess(M3U8 m3U8) {
                        m3U8DownLoadTask.stop();
                        currentM3U8Task.setM3U8(m3U8);
                        currentM3U8Task.setState( M3U8TaskState.SUCCESS);
                        M3U8Log.e("m3u8 Downloader onSuccess: "+j +" "+ m3U8);
                        notifyChanged(currentM3U8Task);
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
                        M3U8Log.e("onDownloadPrepare: "+j +" "+ currentM3U8Task.getUrl());
                    }

                    @Override
                    public void onError(Throwable errorMsg) {
                        if (errorMsg.getMessage() != null && errorMsg.getMessage().contains("ENOSPC")){
                            currentM3U8Task.setState(M3U8TaskState.ENOSPC);
                        }else {
                            currentM3U8Task.setState(M3U8TaskState.ERROR);
                        }
                        M3U8Log.e("onError: " +j +" "+ errorMsg.getMessage());
                        notifyChanged(currentM3U8Task);
                    }
                };
                m3U8DownLoadTask.download(bean.getUrl(), listener);
                listenerList.add(listener);
                i++;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(adapter==null) {
                adapter = new ThirdListAdapter(this, R.layout.list_item, taskList);
                listView.setAdapter(adapter);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String str = PasteTool.getPasteStr(this);
        if(!TextUtils.isEmpty(str) && isAfterOnCreate) {
            final M3U8Task currentM3U8Task = new M3U8Task(str);
            if(!taskList.contains(currentM3U8Task)) {
                m3U8DownLoadTask = new M3U8DownloadTask();
                downloadTaskList.add(m3U8DownLoadTask);
                taskList.add(currentM3U8Task);
                OnTaskDownloadListener listener = new OnTaskDownloadListener() {
                    private long lastLength;
                    private float downloadProgress;
                    @Override
                    public void onStartDownload(int totalTs, int curTs) {
                        M3U8Log.e("onStartDownload: "+totalTs+"|"+curTs);
                        currentM3U8Task.setState(M3U8TaskState.DOWNLOADING);
                        downloadProgress = 1.0f * curTs / totalTs;
                        notifyChanged(currentM3U8Task);
                    }

                    @Override
                    public void onDownloading(long totalFileSize, long itemFileSize, int totalTs, int curTs) {
                        M3U8Log.e("onDownloading: "+totalFileSize+"|"+itemFileSize+"|"+totalTs+"|"+curTs);
                        downloadProgress = 1.0f * curTs / totalTs;
                        currentM3U8Task.setProgressStr(curTs+"/"+totalTs);
                        notifyChanged(currentM3U8Task);
                    }

                    @Override
                    public void onSuccess(M3U8 m3U8) {
                        m3U8DownLoadTask.stop();
                        currentM3U8Task.setM3U8(m3U8);
                        currentM3U8Task.setState( M3U8TaskState.SUCCESS);
                        M3U8Log.e("m3u8 Downloader onSuccess: "+ m3U8);
                        notifyChanged(currentM3U8Task);
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
                        notifyChanged(currentM3U8Task);
                    }
                };
                m3U8DownLoadTask.download(str, listener);
                listenerList.add(listener);
                if(adapter==null) {
                    adapter = new ThirdListAdapter(this, R.layout.list_item, taskList);
                    listView.setAdapter(adapter);
                } else{
                    adapter.notifyDataSetChanged();
                }
                setList2Sp(taskList);
            }
        }
    }

    void setTextOnUiThread(final TextView tv, final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(str);
            }
        });
    }

    private void notifyChanged(final M3U8Task task){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyChanged(taskList, task);
            }
        });
    }

    private void setList2Sp(List<M3U8Task> tasks) {
        if(tasks!=null&&taskList.size()>0){
            List<SpBean> list = new ArrayList<>();
            for(M3U8Task bean : tasks) {
                list.add(new SpBean(bean.getUrl(), ""));
            }
            try {
                SPHelper.putString(Const.JsonKey, JsonTool.SpBean2Json(list));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addListener(final M3U8Task task) {
        OnTaskDownloadListener listener = new OnTaskDownloadListener() {
            private long lastLength;
            private float downloadProgress;
            @Override
            public void onStartDownload(int totalTs, int curTs) {
                M3U8Log.e("onStartDownload: "+totalTs+"|"+curTs);
                task.setState(M3U8TaskState.DOWNLOADING);
                downloadProgress = 1.0f * curTs / totalTs;
                notifyChanged(task);
            }

            @Override
            public void onDownloading(long totalFileSize, long itemFileSize, int totalTs, int curTs) {
                M3U8Log.e("onDownloading: "+totalFileSize+"|"+itemFileSize+"|"+totalTs+"|"+curTs);
                downloadProgress = 1.0f * curTs / totalTs;
                task.setProgressStr(curTs+"/"+totalTs);
                notifyChanged(task);
            }

            @Override
            public void onSuccess(M3U8 m3U8) {
                m3U8DownLoadTask.stop();
                task.setM3U8(m3U8);
                task.setState( M3U8TaskState.SUCCESS);
                M3U8Log.e("m3u8 Downloader onSuccess: "+ m3U8);
                notifyChanged(task);
            }

            @Override
            public void onProgress(long curLength) {
                if (curLength - lastLength > 0) {
                    task.setProgress(downloadProgress);
                    task.setSpeed(curLength - lastLength);
                    lastLength = curLength;
                }
            }

            @Override
            public void onStart() {
                task.setState(M3U8TaskState.PREPARE);
                M3U8Log.e("onDownloadPrepare: "+ task.getUrl());
            }

            @Override
            public void onError(Throwable errorMsg) {
                if (errorMsg.getMessage() != null && errorMsg.getMessage().contains("ENOSPC")){
                    task.setState(M3U8TaskState.ENOSPC);
                }else {
                    task.setState(M3U8TaskState.ERROR);
                }
                M3U8Log.e("onError: " + errorMsg.getMessage());
                notifyChanged(task);
            }
        };
        listenerList.add(listener);
    }
}
