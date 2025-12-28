package com.arsalankhan.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
        EdgeToEdge.enable(this);
        setContentView(getLayoutId());

        setupEdgeToEdge();
        setupBottomNavigation();
    }

    private void setupEdgeToEdge() {
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        // If main view doesn't exist, just continue without edge-to-edge
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        if (bottomNav == null) {
            // Try alternative ID names (common mistakes)
            bottomNav = findViewById(R.id.bottomNavigation);
            if (bottomNav == null) {
                // Check other possible IDs
                int[] possibleIds = {
                        R.id.bottomNavigation,
                        android.R.id.content
                };

                for (int id : possibleIds) {
                    View view = findViewById(id);
                    if (view instanceof BottomNavigationView) {
                        bottomNav = (BottomNavigationView) view;
                        break;
                    }
                }
            }
        }

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(getBottomNavMenuId());

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home && !(this instanceof MainActivity)) {
                    navigateTo(MainActivity.class);
                    return true;
                } else if (id == R.id.nav_search && !(this instanceof SearchActivity)) {
                    navigateTo(SearchActivity.class);
                    return true;
                } else if (id == R.id.nav_impact && !(this instanceof ImpactActivity)) {
                    navigateTo(ImpactActivity.class);
                    return true;
                } else if (id == R.id.nav_more && !(this instanceof MoreActivity)) {
                    navigateTo(MoreActivity.class);
                    return true;
                }

                return false;
            });
        }
    }

    private void navigateTo(Class<?> destination) {
        startActivity(new Intent(this, destination));
        overridePendingTransition(0, 0);
        finish();
    }
}