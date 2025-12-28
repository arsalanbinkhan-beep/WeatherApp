package com.arsalankhan.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class MainActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageButton setting = findViewById(R.id.setting);
        if (setting != null) {
            setting.setOnClickListener(v -> {
                startActivity(new Intent(this, SettingActivity.class));
            });
        }
    }
}