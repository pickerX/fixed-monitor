package com.fixed.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.fixed.monitor.model.video.VideoListAct;
import com.fixed.monitor.model.video.VideoPlayerAct;
import com.fixed.monitor.util.DataUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.test_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//              VideoPlayerAct.openAct(MainActivity.this, DataUtil.SAMPLE_URL);
                startActivity(new Intent(MainActivity.this, VideoListAct.class));
            }
        });

    }
}