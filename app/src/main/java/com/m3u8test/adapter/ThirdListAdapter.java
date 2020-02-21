package com.m3u8test.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.m3u8test.R;
import com.m3u8test.m3u8.M3U8Downloader;
import com.m3u8test.m3u8.M3U8Task;
import com.m3u8test.m3u8.M3U8TaskState;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ThirdListAdapter extends ArrayAdapter<M3U8Task> {

    public ThirdListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<M3U8Task> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        M3U8Task mediaBean = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
        TextView urlName = view.findViewById(R.id.url_tv);
        urlName.setText(mediaBean.getUrl());
        TextView stateTv = view.findViewById(R.id.state_tv);
        setStateText(stateTv,mediaBean);
        TextView progressTv = view.findViewById(R.id.progress_tv);
        setProgressText(progressTv, mediaBean);
        return view;
    }

    private void setProgressText(TextView progressTv, M3U8Task task) {
        switch (task.getState()) {
            case M3U8TaskState.DOWNLOADING:
                progressTv.setText("进度：" + String.format("%.1f ",task.getProgress() * 100)+ "%       速度：" + task.getFormatSpeed());
                break;
            case M3U8TaskState.SUCCESS:
                progressTv.setText(task.getFormatTotalSize());
                break;
            case M3U8TaskState.PAUSE:
                progressTv.setText("进度：" + String.format("%.1f ",task.getProgress() * 100)+ "%" + task.getFormatTotalSize());
                break;
        }
    }

    private void setStateText(TextView stateTv, M3U8Task task){
        if (M3U8Downloader.getInstance().checkM3U8IsExist(task.getUrl())){
            stateTv.setText("已下载");
            return;
        }
        switch (task.getState()){
            case M3U8TaskState.PENDING:
                stateTv.setText("等待中");
                break;
            case M3U8TaskState.DOWNLOADING:
                stateTv.setText("正在下载");
                break;
            case M3U8TaskState.ERROR:
                stateTv.setText("下载异常，点击重试");
                break;
            case M3U8TaskState.ENOSPC:
                stateTv.setText("存储空间不足");
                break;
            case M3U8TaskState.PREPARE:
                stateTv.setText("准备中");
                break;
            case M3U8TaskState.SUCCESS:
                stateTv.setText("下载完成");
                break;
            case M3U8TaskState.PAUSE:
                stateTv.setText("暂停中");
                break;
            default:stateTv.setText("未下载");
                break;
        }
    }

    public void notifyChanged(List<M3U8Task> taskList, M3U8Task m3U8Task){
        for (int i = 0; i < getCount(); i++){
//            Log.e("ZZP","url:"+i+m3U8Task.getUrl());
            if (getItem(i).equals(m3U8Task)){
                Log.e("ZZP","第"+i+"个刷新列表数据");
                taskList.set(i,m3U8Task);
                notifyDataSetChanged();
            }
        }
    }
}
