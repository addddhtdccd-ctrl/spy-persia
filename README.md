# SPYFALL — Android app (Capacitor wrapper)

Your game's HTML/CSS/JS is untouched — it lives in `www/index.html` exactly
as you uploaded it. This package wraps it into a native, offline Android
app using [Capacitor](https://capacitorjs.com).

## Why a couple of terminal commands are still needed

Capacitor's own CLI (`npx cap add android`) is what generates the actual
Android Studio project — Gradle wrapper, the native `capacitor-android`
library module, etc. Those files total tens of thousands of lines and are
pulled fresh from npm for whatever Capacitor version you install, so they
can't be shipped as a static ZIP — they have to be generated on a machine
with internet access. Everything *around* that (the game files, app icon,
splash screen, manifest, portrait lock, release/signing config) is already
built for you below — you just run 4 commands and drop 3 files in.

Total hands-on time: about 10 minutes.

## What's in this ZIP

```
SpyfallAndroidApp/
├── www/index.html            ← your game, byte-for-byte unchanged
├── package.json               ← Capacitor 8.4.2 deps
├── capacitor.config.json      ← offline/perf/portrait-friendly config
└── android-assets/            ← drop-in files for after `cap add android`
    ├── AndroidManifest.xml
    ├── app-build.gradle.snippet
    ├── proguard-rules.pro
    ├── java/com/spyfall/game/MainActivity.java
    └── res/
        ├── values/{styles.xml, colors.xml, strings.xml}
        ├── drawable/{splash_background.xml, ic_launcher_foreground.xml}
        └── mipmap-anydpi-v26/{ic_launcher.xml, ic_launcher_round.xml}
```

## Prerequisites

- [Node.js](https://nodejs.org) 20+ (Capacitor 8 wants Node 22+ ideally)
- [Android Studio](https://developer.android.com/studio) (Otter/2025.2.1+)
- Java 17 (bundled with recent Android Studio)

## Step 1 — install dependencies

Unzip this package, `cd` into `SpyfallAndroidApp`, then:

```bash
npm install
```

## Step 2 — generate the Android project

```bash
npx cap init "SPYFALL" "com.spyfall.game" --web-dir=www
npx cap add android
```

This creates the full `android/` folder — Gradle wrapper, `gradlew`,
`capacitor-android` module, the works.

## Step 3 — drop in the game-specific native files

Copy the contents of `android-assets/` into the freshly generated project,
overwriting where noted:

| From (`android-assets/…`) | To (`android/…`) |
|---|---|
| `AndroidManifest.xml` | `app/src/main/AndroidManifest.xml` (overwrite) |
| `java/com/spyfall/game/MainActivity.java` | `app/src/main/java/com/spyfall/game/MainActivity.java` (overwrite) |
| `res/values/styles.xml` | `app/src/main/res/values/styles.xml` (overwrite) |
| `res/values/colors.xml` | `app/src/main/res/values/colors.xml` (overwrite, or merge if you added others) |
| `res/values/strings.xml` | `app/src/main/res/values/strings.xml` (overwrite) |
| `res/drawable/*.xml` | `app/src/main/res/drawable/` (add both files) |
| `res/mipmap-anydpi-v26/*.xml` | `app/src/main/res/mipmap-anydpi-v26/` (overwrite both) |
| `proguard-rules.pro` | `app/proguard-rules.pro` (overwrite) |

Also **delete** the old placeholder launcher PNGs so the vector adaptive
icon is the only one used:
```bash
rm -rf android/app/src/main/res/mipmap-hdpi android/app/src/main/res/mipmap-mdpi \
       android/app/src/main/res/mipmap-xhdpi android/app/src/main/res/mipmap-xxhdpi \
       android/app/src/main/res/mipmap-xxxhdpi
```
(Safe because `minSdkVersion` is 26 — adaptive icons alone cover every
supported device, no legacy PNG fallback needed.)

Open `android/app/build.gradle` and merge in the settings from
`android-assets/app-build.gradle.snippet` (min SDK 26, signing config,
release minify/proguard, packaging options) — keep the `apply from:` line
and the `dependencies { }` block Capacitor generated.

## Step 4 — sync

```bash
npx cap sync android
```

Run this again any time you edit `www/index.html`.

## Step 5 — open in Android Studio & build

```bash
npx cap open android
```

Let Gradle sync finish (this is when Capacitor's Android libraries actually
download — needs internet once).

### Debug build (for testing on a device/emulator)
Run ▶ from Android Studio, or:
```bash
cd android && ./gradlew assembleDebug
```

### Release build (signed, for the Play Store or sideloading)

1. Generate a keystore once (keep it safe — you need it for every future
   update):
   ```bash
   keytool -genkeypair -v -keystore spyfall-release.keystore \
     -alias spyfall -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Set the four env vars the gradle snippet reads (or hardcode them locally
   — just don't commit real passwords):
   ```bash
   export SPYFALL_KEYSTORE_PATH=/absolute/path/to/spyfall-release.keystore
   export SPYFALL_KEYSTORE_PASSWORD=your_store_password
   export SPYFALL_KEY_ALIAS=spyfall
   export SPYFALL_KEY_PASSWORD=your_key_password
   ```
3. Build:
   ```bash
   # APK (sideloading / direct install)
   ./gradlew assembleRelease
   # -> android/app/build/outputs/apk/release/app-release.apk

   # AAB (Play Store upload format)
   ./gradlew bundleRelease
   # -> android/app/build/outputs/bundle/release/app-release.aab
   ```

## What's covered, and how

| Requirement | How it's done |
|---|---|
| Design/gameplay unchanged | `www/index.html` copied verbatim, zero edits |
| Fully offline | Game is a single self-contained HTML file with no network calls; `capacitor.config.json` disables remote debugging/logging for release |
| Android 8.0+ | `minSdkVersion 26` in the gradle snippet |
| JS + DOM Storage enabled | Capacitor's `Bridge` enables both by default; `MainActivity.java` sets them explicitly too |
| Hardware acceleration | `android:hardwareAccelerated="true"` on `<application>` and the activity, plus `LAYER_TYPE_HARDWARE` on the WebView |
| Full screen | `AppTheme` theme + edge-to-edge (`shortEdges` cutout mode); no visible status bar chrome around the game |
| Portrait lock | `android:screenOrientation="portrait"` on the activity |
| Splash screen | `AppTheme.NoActionBarLaunch` shows `splash_background.xml` (game's near-black bg + crimson eye mark) instantly; `MainActivity` swaps to `AppTheme` right after `super.onCreate()` |
| Adaptive icon | Vector foreground (crimson stencil eye, matches the game's SPYFALL branding) over a near-black background layer, `mipmap-anydpi-v26/ic_launcher.xml` |
| Touch performance | `overScrollMode` disabled, hardware layer on WebView, `captureInput` in Capacitor config |
| Release mode | `minifyEnabled true`, `shrinkResources true`, ProGuard rules that keep the Capacitor bridge + your `MainActivity` |
| APK + AAB ready | Standard `assembleRelease` / `bundleRelease` Gradle tasks, no extra config needed |

## Notes

- No `INTERNET` permission is requested — the manifest omits it since the
  game makes no network calls. If you later add a plugin that needs it,
  add `<uses-permission android:name="android.permission.INTERNET" />`.
- The app ID is `com.spyfall.game` — change it in `capacitor.config.json`,
  the gradle snippet, and the manifest/Java package path together if you
  want something else (must match your Play Console listing).
