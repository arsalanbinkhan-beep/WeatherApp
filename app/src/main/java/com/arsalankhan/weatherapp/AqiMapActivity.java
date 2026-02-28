package com.arsalankhan.weatherapp;

import android.content.Intent;
import android.net.Uri;
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
        webSettings.setAllowFileAccess(false);
        webSettings.setAllowContentAccess(false);

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
                        "Failed to load AQI map. Opening in browser...",
                        Toast.LENGTH_SHORT).show();
                openInBrowser();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Check if the URL is external (not from our allowed domain)
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    // Let WebView load it
                    return false;
                }
                return true;
            }
        });
    }

    private void loadAqiMap() {
        // Google's AQI map - more reliable
        String googleAqiMapUrl = "https://www.google.com/maps/search/air+quality/";

        // Alternative: IQAir's global AQI map (very reliable)
        String iqAirUrl = "https://www.iqair.com/air-quality-map";

        // Alternative: PurpleAir map (community-based sensors)
        String purpleAirUrl = "https://map.purpleair.com/";

        // Load IQAir map (recommended as it works best in WebView)
        webView.loadUrl(iqAirUrl);
    }

    private void openInBrowser() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.iqair.com/air-quality-map"));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open browser", Toast.LENGTH_SHORT).show();
        }
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