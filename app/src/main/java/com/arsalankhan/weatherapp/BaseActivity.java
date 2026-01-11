package com.arsalankhan.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int getLayoutId();
    protected abstract int getBottomNavMenuId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply edge-to-edge before setting content
        EdgeToEdge.enable(this);

        setContentView(getLayoutId());

        // Apply status bar color
        applyStatusBarColor();

        setupEdgeToEdge();
        setupBottomNavigation();
    }

    private void applyStatusBarColor() {
        try {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupEdgeToEdge() {
        View mainView = findViewById(android.R.id.content);
        if (mainView == null) {
            mainView = getWindow().getDecorView().findViewById(android.R.id.content);
        }

        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void setupBottomNavigation() {
        try {
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

            if (bottomNav != null) {
                // Set the selected item based on current activity
                bottomNav.setSelectedItemId(getBottomNavMenuId());

                bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();

                    Log.d("BaseActivity", "Bottom nav item clicked: " + id);

                    // Check if we're already on the target activity
                    boolean shouldNavigate = false;
                    Class<?> destination = null;

                    if (id == R.id.nav_home) {
                        shouldNavigate = !(this instanceof MainActivity);
                        destination = MainActivity.class;
                    } else if (id == R.id.nav_search) {
                        shouldNavigate = !(this instanceof SearchActivity);
                        destination = SearchActivity.class;
                    } else if (id == R.id.nav_impact) {
                        shouldNavigate = !(this instanceof ImpactActivity);
                        destination = ImpactActivity.class;
                    } else if (id == R.id.nav_more) {
                        shouldNavigate = !(this instanceof MoreActivity);
                        destination = MoreActivity.class;
                    }

                    if (shouldNavigate && destination != null) {
                        navigateTo(destination);
                        return true;
                    }

                    // If already on the destination, still return true to show selection
                    return true;
                });
            } else {
                Log.e("BaseActivity", "BottomNavigationView not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BaseActivity", "Error in setupBottomNavigation: " + e.getMessage());
        }
    }

    void navigateTo(Class<?> destination) {
        try {
            Log.d("BaseActivity", "Navigating to: " + destination.getSimpleName());
            Intent intent = new Intent(this, destination);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BaseActivity", "Navigation error: " + e.getMessage());
        }
    }
}