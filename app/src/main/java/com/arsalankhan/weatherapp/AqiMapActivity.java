package com.arsalankhan.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AqiMapActivity extends BaseActivity {

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_aqi_map;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_more; // Keep same as MoreActivity for navigation
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
        setupWebView();
        loadAqiMap();
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AqiMapActivity.this,
                        "Failed to load AQI map. Please check your connection.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Handle external links if needed
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }

    private void loadAqiMap() {
        // Option 1: OpenWeatherMap Weather Maps (Basic maps are free)
        String openWeatherMapUrl = "https://openweathermap.org/weathermap?basemap=map&cities=true";

        // Option 2: OpenAQ Explorer (Free air quality data visualization)
        String openAqUrl = "https://explorer.openaq.org/#/map";

        // Option 3: World Air Quality Index Project
        String waqiUrl = "https://waqi.info/#/c/10/10/2z";

        // Load OpenAQ Explorer (recommended for AQI visualization)
        webView.loadUrl(openAqUrl);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}