# Add project specific ProGuard rules here.

# Keep Capacitor's bridge & plugin classes — required for JS <-> native calls
# to keep working once code is minified in a release build.
-keep class com.getcapacitor.** { *; }
-keep public class * extends com.getcapacitor.Plugin
-keepclassmembers class * {
    @com.getcapacitor.annotation.PluginMethod public *;
}

# Keep WebView JavaScript interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep the app's own MainActivity
-keep class com.spyfall.game.MainActivity { *; }
