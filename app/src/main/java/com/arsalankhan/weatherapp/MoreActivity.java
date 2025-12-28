package com.arsalankhan.weatherapp;

public class MoreActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_more;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_more;
    }
}