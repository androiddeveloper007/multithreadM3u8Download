package com.m3u8test;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.m3u8test.adapter.VideoListAdapter;
import com.m3u8test.bean.SpBean;
import com.m3u8test.listener.OnM3U8DownloadListener;
import com.m3u8test.m3u8.M3U8Downloader;
import com.m3u8test.m3u8.M3U8DownloaderConfig;
import com.m3u8test.m3u8.M3U8Task;
import com.m3u8test.utils.JsonTool;
import com.m3u8test.utils.PasteTool;
import com.m3u8test.utils.SPHelper;
import com.m3u8test.utils.StorageUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private List<M3U8Task> taskList = new ArrayList<>();
    private VideoListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = taskList.get(position).getUrl();
                if (M3U8Downloader.getInstance().checkM3U8IsExist(url)){
                    Toast.makeText(getApplicationContext(),"本地文件已下载，正在播放中！！！", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(MainActivity.this,FullScreenActivity.class);
//                    intent.putExtra("M3U8_URL",M3U8Downloader.getInstance().getM3U8Path(url));
//                    startActivity(intent);
                }else {
                    M3U8Downloader.getInstance().download(url);
                }
            }
        });
//        findViewById(R.id.mShowTimeTv).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MUtils.clearDir(new File(dirPath));
//                adapter.notifyDataSetChanged();
//            }
//        });
        initData();
    }

    private void initData(){
        String dirPath = StorageUtils.getCacheDirectory(this).getPath();
        M3U8DownloaderConfig
                .build(getApplicationContext())
                .setSaveDir(dirPath)
                .setThreadCount(5)
                .setDebugMode(true);
        M3U8Downloader.getInstance().setOnM3U8DownloadListener(onM3U8DownloadListener);
        //读取sp中集合
        SPHelper.init(
                this);
        String jsonArrayStr = SPHelper.getString(Const.JsonKey,"");
        List<SpBean> beanList = null;
        try {
            beanList = JsonTool.Json2SpBean(jsonArrayStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(beanList!=null&& beanList.size()>0){
            for(SpBean bean : beanList) {
                taskList.add(new M3U8Task(bean.getUrl()));
            }
        }
        adapter = new VideoListAdapter(this, R.layout.list_item, taskList);
        listView.setAdapter(adapter);
    }

    private OnM3U8DownloadListener onM3U8DownloadListener = new OnM3U8DownloadListener() {

        @Override
        public void onDownloadItem(M3U8Task task, long itemFileSize, int totalTs, int curTs) {
            super.onDownloadItem(task, itemFileSize, totalTs, curTs);
        }

        @Override
        public void onDownloadSuccess(M3U8Task task) {
            super.onDownloadSuccess(task);
            adapter.notifyChanged(taskList, task);
        }

        @Override
        public void onDownloadPending(M3U8Task task) {
            super.onDownloadPending(task);
            notifyChanged(task);
        }

        @Override
        public void onDownloadPause(M3U8Task task) {
            super.onDownloadPause(task);
            notifyChanged(task);
        }

        @Override
        public void onDownloadProgress(final M3U8Task task) {
            super.onDownloadProgress(task);
            notifyChanged(task);
        }

        @Override
        public void onDownloadPrepare(final M3U8Task task) {
            super.onDownloadPrepare(task);
            notifyChanged(task);
        }

        @Override
        public void onDownloadError(final M3U8Task task, Throwable errorMsg) {
            super.onDownloadError(task, errorMsg);
            notifyChanged(task);
        }
    };

    private void notifyChanged(final M3U8Task task){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyChanged(taskList, task);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String str = PasteTool.getPasteStr(this);
        if(!TextUtils.isEmpty(str)) {
            M3U8Task task = new M3U8Task(str);
            if(adapter!=null && !taskList.contains(task)){
                taskList.add(task);
                adapter.notifyDataSetChanged();
                setList2Sp(taskList);
            }
        }
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
}
