package com.spyfall.game;

import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Show the app theme (splash background is only on AppTheme.NoActionBarLaunch,
        // set in the manifest) until the WebView is ready, then switch to the
        // normal theme so the status/nav bars match the game's palette.
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        // Keep the screen on and hardware-accelerated for smooth card flips
        // and the radar-sweep / countdown-ring animations.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        WebView webView = getBridge().getWebView();
        if (webView != null) {
            WebSettings settings = webView.getSettings();
            // JavaScript + DOM storage are already enabled by Capacitor's
            // Bridge by default; set explicitly for clarity/offline reliability.
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);

            // Touch / scrolling smoothness tuning for the reveal & timer screens.
            webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        }
    }
}
