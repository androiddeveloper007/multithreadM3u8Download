package com.m3u8test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.m3u8test.server.EncryptM3U8Server;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import androidx.annotation.Nullable;

public class PlayActivity extends Activity {

    private StandardGSYVideoPlayer videoPlayer;
    private EncryptM3U8Server m3u8Server = new EncryptM3U8Server();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        videoPlayer = (StandardGSYVideoPlayer)findViewById(R.id.videoView);
        videoPlayer.setNeedLockFull(false);
        String url = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            url = bundle.getString("M3U8_URL");
        }
        m3u8Server.execute();
        videoPlayer.getCurrentPlayer().setUp(m3u8Server.createLocalHttpUrl(url),false,"");
        videoPlayer.startWindowFullscreen(this,false,false);

        videoPlayer.setBackFromFullScreenListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        videoPlayer.getCurrentPlayer().startPlayLogic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        m3u8Server.decrypt();
    }

    @Override
    protected void onPause() {
        super.onPause();
        m3u8Server.encrypt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m3u8Server.finish();
        //释放所有
        videoPlayer.getCurrentPlayer().release();
        GSYVideoPlayer.releaseAllVideos();
        videoPlayer.setStandardVideoAllCallBack(null);
    }
}
