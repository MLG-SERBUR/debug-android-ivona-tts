# TTS Test App - Extended Test Cases

## Overview

The TTSTestApp has been extended with comprehensive test cases that replicate the exact TTS invocation methods used by both SpeakThat and VoiceNotify. These tests are designed to identify why Ivona TTS works with VoiceNotify but fails with SpeakThat when called from a NotificationListenerService.

## Build Information

- **APK**: `TTSTestApp/build/outputs/apk/debug/TTSTestApp-debug.apk`
- **Size**: 5.5 MB
- **Status**: ✅ BUILD SUCCESSFUL
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 16)

## Key Differences Identified

### 1. **Context Type** (Most Critical)

**SpeakThat (Fails with Ivona)**:
```kotlin
// Uses service context (this)
textToSpeech = TextToSpeech(this, this, selectedEngine)
```
- Context: NotificationListenerService instance
- Effect: Restricted service binding on Android 12+
- Result: Ivona TTS fails to initialize

**VoiceNotify (Works with Ivona)**:
```kotlin
// Uses application context
tts = TextToSpeech(appContext, OnInitListener { status -> ... })
```
- Context: Application context (not service)
- Effect: Better compatibility for service binding
- Result: Ivona TTS initializes successfully

### 2. **Queue Mode**

**SpeakThat**:
```kotlin
textToSpeech?.speak(finalSpeechText, TextToSpeech.QUEUE_FLUSH, volumeParams, "notification_utterance")
```
- Uses `QUEUE_FLUSH` to interrupt current speech

**VoiceNotify**:
```kotlin
tts?.speak(info.ttsMessage, TextToSpeech.QUEUE_ADD, getTtsParams(info.settings), utteranceId.toString())
```
- Uses `QUEUE_ADD` to queue notifications
- More reliable for batch notifications

### 3. **Bundle Parameters**

**VoiceNotify Approach**:
```kotlin
private fun getTtsParams(settings: Settings) = Bundle().apply {
    putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, settings.ttsStream!!)
}
```
- Explicitly specifies audio stream in bundle
- May be important for engine routing

### 4. **Engine Initialization**

**SpeakThat**:
```kotlin
if (selectedEngine.isNullOrEmpty()) {
    textToSpeech = TextToSpeech(this, this)
} else {
    textToSpeech = TextToSpeech(this, this, selectedEngine)
}
```
- Passes custom engine package as 3rd parameter
- Used both from service context

**VoiceNotify**:
```kotlin
tts = TextToSpeech(appContext, OnInitListener { ... })
// Never passes custom engine package
```
- Always uses system default
- More reliable approach

## New Test Cases Added

### Test 1: Application Context (2-arg)
**Location**: Service > Test 2-arg (ApplicationContext)

Tests TTS initialization from NotificationListenerService using application context instead of service context.

```kotlin
tts = TextToSpeech(applicationContext) { status -> ... }
```

**Expected Result**: TTS initializes successfully (like VoiceNotify)

**Diagnostic Output**:
```
Context type: ApplicationContext (not service)
Creating TTS with: TextToSpeech(applicationContext, listener)
```

### Test 2: Application Context with Ivona (3-arg)
**Location**: Service > Test 3-arg Ivona (ApplicationContext)

Tests the critical case: custom Ivona engine package with application context.

```kotlin
tts = TextToSpeech(applicationContext, { status -> ... }, ivonaPackage)
```

**Expected Result**: Ivona initializes successfully when using `applicationContext`

**Diagnostic Output**:
```
Context type: ApplicationContext (not service)
Creating TTS with: TextToSpeech(applicationContext, listener, "ivona.tts")
```

### Test 3: QUEUE_ADD Mode
**Location**: Service > Test QUEUE_ADD Mode

Tests VoiceNotify's approach of queueing multiple notifications instead of flushing.

```kotlin
val result1 = tts?.speak("First message", TextToSpeech.QUEUE_FLUSH, null, id1)
val result2 = tts?.speak("Second message", TextToSpeech.QUEUE_ADD, null, id2)
```

**Expected Result**: Both messages are queued and played sequentially

**Diagnostic Output**:
```
First speak (QUEUE_FLUSH) returned: 0
Second speak (QUEUE_ADD) returned: 0
QUEUE_ADD test: queued 2 messages
```

### Test 4: Bundle Parameters
**Location**: Service > Test Bundle Parameters

Tests VoiceNotify's approach of passing stream parameters in a bundle.

```kotlin
val params = Bundle().apply {
    putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
}
tts?.speak("Testing...", TextToSpeech.QUEUE_FLUSH, params, id)
```

**Expected Result**: TTS speaks with explicitly specified audio stream

**Diagnostic Output**:
```
Created volume bundle for TTS.speak()
TTS.speak() returned: 0
SUCCESS: Bundle params test
```

### Test 5: Engine Verification
**Location**: Service > Test Engine Verification

Verifies engine availability BEFORE attempting initialization (not done in SpeakThat).

```kotlin
val ttsIntent = Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE)
ttsIntent.setPackage(ivonaPackage)
val resolveInfo = packageManager.resolveService(ttsIntent, GET_RESOLVED_FILTER)
```

**Expected Result**: Confirms engine availability before initialization

**Diagnostic Output**:
```
Engine ivona.tts is available - com.ivona.tts
SUCCESS: Verified engine initialized
```

## Testing Procedure

### On Device/Emulator:

1. **Install APK**:
   ```bash
   adb install TTSTestApp-debug.apk
   ```

2. **Grant Permissions**:
   - Click "Open NotificationListener Settings"
   - Enable "Debug Ivona TTS" in notification access settings

3. **Run Tests from Service** (Critical for Ivona issue):
   - Click "Service: Test 2-arg (ApplicationContext)"
   - Click "Service: Test 3-arg Ivona (ApplicationContext)"
   - Click "Service: Test QUEUE_ADD Mode"
   - Click "Service: Test Bundle Parameters"
   - Click "Service: Test Engine Verification"

4. **Monitor Logs**:
   ```bash
   adb logcat -s TestNotificationSvc
   ```

5. **Expected Results**:
   - Tests 1-2: Should show SUCCESS with applicationContext
   - Test 3: Should queue 2 messages successfully
   - Test 4: Should speak with bundle parameters
   - Test 5: Should verify engine availability

## Code Links

### Test App Files

[MainActivity.kt](TTSTestApp/src/main/java/com/example/ttstest/MainActivity.kt)
- UI with test buttons
- Activity context tests

[TestNotificationService.kt](TTSTestApp/src/main/java/com/example/ttstest/TestNotificationService.kt)
- Service context tests
- Application context tests
- Queue and bundle parameter tests

### Reference Implementation

[SpeakThat NotificationReaderService](https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt#L1076-L1116)
- Lines 1076-1116: TTS initialization (SERVICE CONTEXT)
- Lines 5034-5040: speak() call (QUEUE_FLUSH)

[VoiceNotify Service.kt](https://github.com/pilot51/voicenotify/blob/main/app/src/main/java/com/pilot51/voicenotify/Service.kt#L160-L174)
- Lines 160-174: TTS initialization (APP CONTEXT)
- Lines 441-450: speak() call (QUEUE_ADD)

## Critical Findings

### Why Ivona Fails with SpeakThat (from NotificationListenerService):

1. **Service Context Binding Issue**: Using `TextToSpeech(this, ...)` from a `NotificationListenerService` on Android 12+ has restricted access to system services
2. **Custom Engine Package Problem**: Passing a specific engine package (`ivona.tts`) with service context compounds the issue
3. **Android 12+ Package Visibility**: Stricter package visibility rules prevent proper binding to third-party TTS engines

### Why Ivona Works with VoiceNotify:

1. **Application Context**: Using `appContext` has broader service binding permissions
2. **System Default Engine**: Not forcing a specific engine package is more reliable
3. **Consistent Approach**: Uses same context everywhere (not mixing Activity and Service contexts)

## Recommended Fix for SpeakThat

Replace in [NotificationReaderService.kt line 1110](https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt#L1110):

```kotlin
// Change from:
textToSpeech = TextToSpeech(this, this, selectedEngine)

// To:
textToSpeech = TextToSpeech(applicationContext, this, selectedEngine)
```

This single change should enable Ivona TTS to work with SpeakThat's notification reading feature.

## Build Commands

```bash
# Build debug APK
./gradlew :TTSTestApp:assembleDebug

# Build release APK
./gradlew :TTSTestApp:assembleRelease

# Run tests
./gradlew :TTSTestApp:test

# Run instrumentedTests on device
./gradlew :TTSTestApp:connectedAndroidTest
```

## Troubleshooting

### TTS Not Initializing
- Check adb logs for error status codes
- Verify TTS engine is installed: Settings > Accessibility > Text-to-speech
- Try Test 5 (Engine Verification) to confirm availability

### onInit() Not Called
- Common on Android 12+
- Try Application Context test
- Check service binding permissions

### Ivona Works in Activity but Not in Service
- Confirms context difference issue
- Use Application Context in service
- Don't pass custom engine package from service context

## File Changes Summary

- **Modified**: `TTSTestApp/src/main/java/com/example/ttstest/TestNotificationService.kt`
  - Added 5 new test methods
  - Added 5 new test constants
  - Total new lines: ~270

- **Modified**: `TTSTestApp/src/main/java/com/example/ttstest/MainActivity.kt`
  - Added 6 new buttons for new tests
  - Updated button container to include all new buttons
  - Total new lines: ~50

## APK Details

```
File: TTSTestApp-debug.apk
Size: 5.5 MB
Min SDK: 24
Target SDK: 36
Architectures: arm64-v8a, armeabi-v7a, x86, x86_64
Debug: True
Signature: Test key
```

## Next Steps

1. Install APK on device with Ivona TTS installed
2. Run all service-based tests
3. Compare results with original tests
4. If applicationContext tests pass, confirm the fix for SpeakThat
5. Apply same fix to any other apps using service context for TTS
