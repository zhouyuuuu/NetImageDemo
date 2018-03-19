package com.example.administrator.netimageapplication.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.netimageapplication.R;
import com.example.administrator.netimageapplication.imagedisplayer.NetImageActivity;

/**
 * Edited by Administrator on 2018/3/19.
 */

public class LaunchActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        TextView mTvLaunch = findViewById(R.id.tv_launch);
        mTvLaunch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_launch:
                startActivity(new Intent(LaunchActivity.this, NetImageActivity.class));
        }
    }
}
