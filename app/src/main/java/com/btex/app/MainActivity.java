package com.btex.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // Bridge: Btex.html कडून येणारे कोणतेही message या ठिकाणी पकडले जातात (सध्या काहीही active नाही)
        webView.addJavascriptInterface(new AppInventorBridge(), "AppInventor");

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/Btex.html");
    }

    private class AppInventorBridge {
        @JavascriptInterface
        public void setWebViewString(String value) {
            // Ad-related bridge messages removed — no longer sent from Btex.html
        }
    }
}
