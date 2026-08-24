package com.riebhopallibrary.app;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progress;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        progress = findViewById(R.id.progress);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);

        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        settings.setMediaPlaybackRequiresUserGesture(false);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if (progress != null) {

                    progress.setProgress(newProgress);

                    if (newProgress >= 100) {
                        progress.setVisibility(ProgressBar.GONE);
                    } else {
                        progress.setVisibility(ProgressBar.VISIBLE);
                    }
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                Uri uri = request.getUrl();

                String scheme = uri.getScheme() == null
                        ? ""
                        : uri.getScheme();

                if ("http".equalsIgnoreCase(scheme)
                        || "https".equalsIgnoreCase(scheme)) {

                    return false;
                }

                try {

                    Intent intent =
                            new Intent(Intent.ACTION_VIEW, uri);

                    startActivity(intent);

                } catch (Exception e) {

                    Toast.makeText(
                            MainActivity.this,
                            "Unable to open link",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                return true;
            }
        });

        webView.setDownloadListener(
                (url, userAgent, contentDisposition,
                 mimeType, contentLength) -> {

                    try {

                        DownloadManager.Request request =
                                new DownloadManager.Request(
                                        Uri.parse(url)
                                );

                        request.setMimeType(mimeType);

                        request.addRequestHeader(
                                "User-Agent",
                                userAgent
                        );

                        String cookies =
                                CookieManager
                                        .getInstance()
                                        .getCookie(url);

                        if (cookies != null) {

                            request.addRequestHeader(
                                    "Cookie",
                                    cookies
                            );
                        }

                        request.setTitle(
                                "RIE Bhopal Library"
                        );

                        request.setDescription(
                                "Downloading file..."
                        );

                        request.setNotificationVisibility(
                                DownloadManager.Request
                                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        );

                        request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                "RIE_Bhopal_Library_"
                                        + System.currentTimeMillis()
                        );

                        DownloadManager manager =
                                (DownloadManager)
                                        getSystemService(
                                                DOWNLOAD_SERVICE
                                        );

                        manager.enqueue(request);

                        Toast.makeText(
                                MainActivity.this,
                                "Download started",
                                Toast.LENGTH_SHORT
                        ).show();

                    } catch (Exception e) {

                        try {

                            Intent intent =
                                    new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(url)
                                    );

                            startActivity(intent);

                        } catch (Exception ignored) {
                        }
                    }
                }
        );

        /*
         * IMPORTANT:
         * Load the local HTML dashboard.
         */

        if (savedInstanceState == null) {

            webView.loadUrl(
                    "file:///android_asset/index.html"
            );

        } else {

            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(
            Bundle outState) {

        if (webView != null) {

            webView.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }
}
