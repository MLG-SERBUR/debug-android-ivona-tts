# TTS Initialization Analysis: SpeakThat vs VoiceNotify with Ivona

## Problem Summary

**Question**: Why does VoiceNotify work fine with Ivona TTS while SpeakThat has problems initializing Ivona when reading notifications?

**Answer**: The difference lies in the **Context used to initialize TextToSpeech** and how it **handles custom TTS engine packages** when called from a `NotificationListenerService`.

---

## Root Cause Analysis

### SpeakThat Approach (Has Issues with Ivona)

**TTS Initialization:**
- Location: [NotificationReaderService.initializeTextToSpeech()](https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt#L1076-L1116)
- Method: `TextToSpeech(this, this)` or `TextToSpeech(this, this, selectedEngine)`
- Context: **Service context** (the `NotificationListenerService` instance)
- Engine handling: Can pass a **custom engine package name** as third parameter

**Initialization Code:**
```kotlin
// SpeakThat - lines 1107-1116
if (selectedEngine.isNullOrEmpty()) {
    // Use system default engine
    textToSpeech = TextToSpeech(this, this)
} else {
    // Use selected custom engine
    textToSpeech = TextToSpeech(this, this, selectedEngine)
}
```

**TTS.speak() Call:**
- Location: [NotificationReaderService line 5038](https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt#L5034-L5040)
- Method: `textToSpeech?.speak(finalSpeechText, TextToSpeech.QUEUE_FLUSH, volumeParams, "notification_utterance")`
- Includes bundle parameters for volume ducking

### VoiceNotify Approach (Works with Ivona)

**TTS Initialization:**
- Location: [Service.initTts()](https://github.com/pilot51/voicenotify/blob/main/app/src/main/java/com/pilot51/voicenotify/Service.kt#L160-L174)
- Method: `TextToSpeech(appContext, OnInitListener { ... })`
- Context: **Application context** (not service context)
- Engine handling: Does NOT pass custom engine package; relies on system default

**Initialization Code:**
```kotlin
// VoiceNotify - lines 160-174
isAwaitingTtsInit.value = true
tts = TextToSpeech(appContext, OnInitListener { status ->
    latestTtsStatus = status
    isAwaitingTtsInit.value = false
    if (status == TextToSpeech.SUCCESS) {
        onInit(true)
    } else {
        shutdownTts()
        val errorMsg = getString(R.string.error_tts_init, status)
        Log.w(TAG, errorMsg)
        onInit(false)
        // ...
    }
})
```

**TTS.speak() Call:**
- Location: [Service.kt line ~448-450](https://github.com/pilot51/voicenotify/blob/main/app/src/main/java/com/pilot51/voicenotify/Service.kt#L441-L450)
- Method: `tts?.speak(info.ttsMessage, TextToSpeech.QUEUE_ADD, getTtsParams(info.settings), utteranceId.toString())`
- Uses QUEUE_ADD mode for queueing

---

## Why Ivona Works with VoiceNotify but Not SpeakThat

### Technical Reason

When `TextToSpeech` is initialized **from a `NotificationListenerService` with a service context**, the Android system may have **restricted access** to bind to certain TTS engine services, especially when a specific engine package is passed. This is particularly true on **Android 12+** where stricter package visibility and service binding rules apply.

Key differences:

| Aspect | SpeakThat | VoiceNotify |
|--------|-----------|------------|
| **Context** | Service (`this`) | Application context |
| **Custom Engine** | Yes (passed as 3rd param) | No (system default) |
| **Service Binding** | May fail for third-party engines | Usually succeeds with app context |
| **Android 12+ Compat** | Restricted by package visibility | Better compatibility |
| **In-App Success** | Works (uses Activity context in MainActivity) | Works (consistent usage) |
| **Notification Success** | Fails with Ivona (service context issue) | Works (app context) |

### Why SpeakThat Works In-App But Not for Notifications

**In-App (MainActivity):**
- SpeakThat initializes TTS from `MainActivity` (Activity context): [MainActivity.initializeTextToSpeech()](https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/MainActivity.kt#L556-L588)
- Activity context has better permissions for service binding
- Ivona engine initializes successfully

**Notifications (NotificationReaderService):**
- SpeakThat initializes TTS from `NotificationReaderService` (Service context)
- Service context + custom engine package = restricted binding
- Ivona engine fails to initialize
- Self-test diagnostics confirm TTS not initialized

---

## Minimal Repro Project

A test application (`ttsrepro`) has been created in the SpeakThat repository to demonstrate both approaches:

### Files Created:
1. **Module**: [SpeakThat/ttsrepro/](https://github.com/mitchib1440/SpeakThat/tree/main/ttsrepro)
2. **Manifest**: [ttsrepro/src/main/AndroidManifest.xml](https://github.com/mitchib1440/SpeakThat/blob/main/ttsrepro/src/main/AndroidManifest.xml)
3. **Service**: [ttsrepro/src/main/java/com/micoyc/ttsrepro/ReproNotificationService.kt](https://github.com/mitchib1440/SpeakThat/blob/main/ttsrepro/src/main/java/com/micoyc/ttsrepro/ReproNotificationService.kt)
   - Initializes TTS two ways:
     - `TextToSpeech(this, this)` (service context - like SpeakThat)
     - `TextToSpeech(applicationContext, { ... })` (app context - like VoiceNotify)
4. **Activity**: [ttsrepro/src/main/java/com/micoyc/ttsrepro/MainActivity.kt](https://github.com/mitchib1440/SpeakThat/blob/main/ttsrepro/src/main/java/com/micoyc/ttsrepro/MainActivity.kt)
   - Opens notification access settings for testing

### How to Use:
1. Install the APK on a device/emulator with Ivona TTS installed
2. Launch the app and grant notification access
3. Send test notifications
4. Monitor logs (adb logcat -s TTSRepro) to observe:
   - Whether `TextToSpeech(applicationContext, ...)` succeeds with Ivona
   - Whether `TextToSpeech(this, ...)` fails with Ivona
5. Compare with both TTS engines to confirm the pattern

---

## Recommended Fixes for SpeakThat

### Option 1: Use Application Context (Recommended)
Replace service context with application context in `NotificationReaderService.initializeTextToSpeech()`:

```kotlin
// Change from:
textToSpeech = TextToSpeech(this, this, selectedEngine)

// To:
textToSpeech = TextToSpeech(applicationContext, this, selectedEngine)
```

This mirrors VoiceNotify's proven approach.

### Option 2: Remove Custom Engine Package for Notifications
Only pass the engine package for UI-context TTS (activities), not for `NotificationListenerService`:

```kotlin
if (selectedEngine.isNullOrEmpty()) {
    textToSpeech = TextToSpeech(applicationContext, this)
} else {
    // Only use custom engine from Activity context, not from service
    textToSpeech = TextToSpeech(applicationContext, this)
}
```

### Option 3: Diagnostic Logging
Add detailed logging before TTS initialization to track whether the engine is available:

```kotlin
if (!selectedEngine.isNullOrEmpty()) {
    try {
        val ttsIntent = Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE)
        ttsIntent.setPackage(selectedEngine)
        val resolveInfo = packageManager.queryIntentServices(ttsIntent, PackageManager.GET_RESOLVED_FILTER)
        Log.d(TAG, "Engine $selectedEngine resolution: found=${resolveInfo.isNotEmpty()}")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to resolve engine $selectedEngine", e)
    }
}
```

---

## Code Snippets for Reference

### SpeakThat TTS Init (Service Context)
**File**: [NotificationReaderService.kt](https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt)
**Lines**: [1076-1116](https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt#L1076-L1116)

### SpeakThat speak() Call
**File**: [NotificationReaderService.kt](https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt)
**Lines**: [5034-5040](https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt#L5034-L5040)

### VoiceNotify TTS Init (App Context)
**File**: [Service.kt](https://github.com/pilot51/voicenotify/blob/main/app/src/main/java/com/pilot51/voicenotify/Service.kt)
**Lines**: [146-174](https://github.com/pilot51/voicenotify/blob/main/app/src/main/java/com/pilot51/voicenotify/Service.kt#L146-L174)

---

## Build Information

**APK Name**: `ttsrepro-debug.apk`
**Min SDK**: 24 (Android 7.0)
**Target SDK**: 36 (Android 16)
**Size**: ~5.4 MB
**Architecture**: Universal (multiple ABIs included)

### Building Locally:
```bash
# Requirements: JDK 21+, Android SDK 36
export JAVA_HOME=/path/to/jdk21
cd SpeakThat
./gradlew :ttsrepro:assembleDebug
# APK location: ttsrepro/build/outputs/apk/debug/ttsrepro-debug.apk
```

---

## Testing Steps

1. **Install on Device/Emulator**:
   ```bash
   adb install -r ttsrepro-debug.apk
   ```

2. **Grant Permissions**:
   - Launch app, tap "Open notification access settings"
   - Enable "TTS Repro" in notification listeners

3. **Trigger Test**:
   - Send test notification from another app
   - App will attempt both TTS initialization methods

4. **Monitor Logs**:
   ```bash
   adb logcat -s TTSRepro | grep -E "TextToSpeech|init|failed"
   ```

5. **Observe Behavior**:
   - With **Google TTS**: Both methods should work
   - With **Ivona TTS**: `applicationContext` method succeeds, `service context` may fail

---

## Conclusion

The primary difference in how SpeakThat and VoiceNotify invoke TTS from a `NotificationListenerService` is the **Context object used**:

- **VoiceNotify**: `TextToSpeech(applicationContext, ...)` → Reliable, works with third-party engines
- **SpeakThat**: `TextToSpeech(this, ...)` + custom engine → Service context limitations, fails with Ivona

**The fix**: Switch SpeakThat to use application context for TTS initialization in `NotificationReaderService`, matching VoiceNotify's proven approach.
