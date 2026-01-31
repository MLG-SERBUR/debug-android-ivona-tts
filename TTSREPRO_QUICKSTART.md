# TTS Repro APK - Quick Start Guide

## What This App Does

This is a minimal test application that demonstrates **two different ways of initializing TextToSpeech from a NotificationListenerService**:

1. **Service Context Method** (like SpeakThat): `TextToSpeech(this, this)`
2. **Application Context Method** (like VoiceNotify): `TextToSpeech(applicationContext, onInitListener)`

When you trigger a notification, the app will attempt to initialize TTS using both methods and log the results.

## Installation

### Prerequisites
- Android device or emulator running API 24+ (Android 7.0+)
- Ivona TTS engine installed (for testing the problematic case)
- ADB installed on your computer

### Steps

1. **Download the APK**:
   ```
   ttsrepro-debug.apk (5.4 MB)
   ```

2. **Install**:
   ```bash
   adb install -r ttsrepro-debug.apk
   ```

3. **Verify Installation**:
   ```bash
   adb shell pm list packages | grep ttsrepro
   # Should output: package:com.micoyc.ttsrepro
   ```

## Running the Test

### 1. Launch the App
```bash
adb shell am start -n com.micoyc.ttsrepro/.MainActivity
```

### 2. Grant Notification Access
- Click the button "Open notification access settings"
- Find "TTS Repro" in the notification listeners list
- Toggle it ON to grant permission

### 3. Start Monitoring Logs
```bash
adb logcat -s TTSRepro -v time &
```

### 4. Trigger a Test Notification
- Use another app to send a notification (e.g., a test notification from a system app)
- Or send one via adb:
  ```bash
  adb shell am broadcast -a android.intent.action.SEND --es extra_text "Test notification"
  ```

### 5. Watch the Logs
You'll see output like:
```
01-31 16:35:42.123  TTSRepro: Requested TextToSpeech(this, this)
01-31 16:35:42.145  TTSRepro: Requested TextToSpeech(applicationContext, ...)
01-31 16:35:43.001  TTSRepro: Service-context TTS init: status=0 (SUCCESS)
01-31 16:35:43.005  TTSRepro: App-context TTS init: status=0 (SUCCESS)
```

Or with failures (when using Ivona from service context):
```
01-31 16:35:42.123  TTSRepro: Requested TextToSpeech(this, this)
01-31 16:35:42.145  TTSRepro: Requested TextToSpeech(applicationContext, ...)
01-31 16:35:43.200  TTSRepro: Service-context TTS init failed: status=-1
01-31 16:35:43.250  TTSRepro: App-context TTS init succeeded: status=0
```

## Expected Results

### With Google TTS (Default)
- ✅ Both methods succeed
- Both contexts can bind to the Google TTS engine

### With Ivona TTS
- ❌ Service context method fails (status -1 or ERROR)
- ✅ Application context method succeeds
- Demonstrates the difference between the two approaches

### With Other TTS Engines
- Depends on how the engine handles service vs. application context binding
- Ivona is known to have strict service binding requirements

## Interpreting Log Output

### TTS Status Codes
- `0` = `TextToSpeech.SUCCESS` - Engine initialized successfully
- `-1` = Generic failure - Engine service not found or binding failed
- Other negative numbers = Various TTS engine errors (see Android TTS documentation)

### Key Log Markers
- `"Requested TextToSpeech(this, this)"` - Service context attempt started
- `"Requested TextToSpeech(applicationContext, ...)"` - App context attempt started
- `"Service-context TTS init"` - Result from OnInitListener (service method)
- `"App-context TTS init"` - Result from lambda callback (app method)

## Uninstallation

```bash
adb uninstall com.micoyc.ttsrepro
```

## Source Code

- **Service**: Handles notification interception and TTS initialization
  - Location in repo: `ttsrepro/src/main/java/com/micoyc/ttsrepro/ReproNotificationService.kt`
  - Initializes TTS two ways and logs results

- **Activity**: Launcher UI
  - Location in repo: `ttsrepro/src/main/java/com/micoyc/ttsrepro/MainActivity.kt`
  - Opens notification access settings

- **Manifest**: Declares service and permissions
  - Location in repo: `ttsrepro/src/main/AndroidManifest.xml`

## Troubleshooting

### App doesn't show up in notification listeners
- Restart the device after granting permission
- Check `Settings → Apps & notifications → Notifications → Special access → Notification access`

### No logs appear
- Ensure you're monitoring with: `adb logcat -s TTSRepro`
- Check that notification access is actually granted
- Try sending a notification from a different app (e.g., Gmail, Slack)

### "No notifications received"
- The test service only logs when a notification is posted
- Make sure the app has notification access enabled
- Try triggering a notification from a system app like Messages or Calendar

## Related Documentation

See `TTS_COMPARISON_ANALYSIS.md` for:
- Deep technical analysis of SpeakThat vs. VoiceNotify
- Code snippets and line numbers
- Recommended fixes for the Ivona compatibility issue
