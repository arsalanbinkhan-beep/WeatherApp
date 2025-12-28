package com.arsalankhan.weatherapp;

public class SearchActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_search;
    }
}