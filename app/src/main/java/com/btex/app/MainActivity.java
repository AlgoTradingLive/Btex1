package com.btex.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.applovin.sdk.AppLovinSdk;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;

public class MainActivity extends Activity {

    // Approval नंतर dashboard मधून मिळालेले खरे Ad Unit ID इथे टाका
    private static final String BANNER_AD_UNIT_ID = "YOUR_BANNER_AD_UNIT_ID";
    private static final String INTERSTITIAL_AD_UNIT_ID = "YOUR_INTERSTITIAL_AD_UNIT_ID";

    private MaxInterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // AppLovin MAX init
        AppLovinSdk.getInstance(this).setMediationProvider("max");
        AppLovinSdk.getInstance(this).initializeSdk(configuration -> {
            // SDK ready — इथून पुढे banner/interstitial load करता येईल
            setupBannerAd();
            loadInterstitialAd();
        });

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

    private void setupBannerAd() {
        FrameLayout container = findViewById(R.id.bannerAdContainer);
        MaxAdView bannerAdView = new MaxAdView(BANNER_AD_UNIT_ID, this);
        bannerAdView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container.addView(bannerAdView);
        bannerAdView.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) { }
            @Override
            public void onAdDisplayed(MaxAd ad) { }
            @Override
            public void onAdHidden(MaxAd ad) { }
            @Override
            public void onAdClicked(MaxAd ad) { }
            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) { }
            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) { }
        });
        bannerAdView.loadAd();
    }

    private void loadInterstitialAd() {
        interstitialAd = new MaxInterstitialAd(INTERSTITIAL_AD_UNIT_ID, this);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) { }

            @Override
            public void onAdDisplayed(MaxAd ad) { }

            @Override
            public void onAdHidden(MaxAd ad) {
                loadInterstitialAd(); // पुढच्या वेळेसाठी परत load कर
            }

            @Override
            public void onAdClicked(MaxAd ad) { }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) { }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                loadInterstitialAd();
            }
        });
        interstitialAd.loadAd();
    }

    private void showInterstitialAd() {
        runOnUiThread(() -> {
            if (interstitialAd != null && interstitialAd.isReady()) {
                interstitialAd.showAd();
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
