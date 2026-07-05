package com.btex.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MainActivity extends Activity {

    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6724873553204610/6527857237";

    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // AdMob init
        MobileAds.initialize(this, initializationStatus -> { });

        // Banner ad
        AdView bannerAdView = findViewById(R.id.bannerAdView);
        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Toast.makeText(MainActivity.this,
                        "Banner failed: " + adError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdLoaded() {
                Toast.makeText(MainActivity.this, "Banner loaded", Toast.LENGTH_SHORT).show();
            }
        });
        bannerAdView.loadAd(new AdRequest.Builder().build());

        // Interstitial ad (pre-loaded, ready for challenge start/reset)
        loadInterstitialAd();

        WebView webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // Bridge: Btex.html मधून notifyKodular('SHOW_INTERSTITIAL_AD') आल्यावर हे पकडतं
        webView.addJavascriptInterface(new AppInventorBridge(), "AppInventor");

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/Btex.html");
    }

    private void loadInterstitialAd() {
        InterstitialAd.load(this, INTERSTITIAL_AD_UNIT_ID, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        interstitialAd = ad;
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                interstitialAd = null;
                                loadInterstitialAd(); // पुढच्या वेळेसाठी परत load कर
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                interstitialAd = null;
                                loadInterstitialAd();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        interstitialAd = null;
                    }
                });
    }

    private void showInterstitialAd() {
        runOnUiThread(() -> {
            if (interstitialAd != null) {
                interstitialAd.show(MainActivity.this);
            }
        });
    }

    private class AppInventorBridge {
        @JavascriptInterface
        public void setWebViewString(String value) {
            if ("SHOW_INTERSTITIAL_AD".equals(value)) {
                showInterstitialAd();
            }
        }
    }
}
