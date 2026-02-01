# Quick Reference: 13 TTS Test Cases

## Test Cases (In Order)

| # | Test Name | Button Label | Key Pattern | Ivona Ready |
|---|-----------|--------------|-------------|------------|
| 1 | TEST_2ARG | Service: Test 2-arg TTS | `TextToSpeech(this, listener)` | ⚠️ |
| 2 | TEST_3ARG_IVONA | Service: Test 3-arg TTS (Ivona) | `TextToSpeech(this, listener, "ivona.tts")` | ✅ |
| 3 | TEST_APP_CONTEXT_2ARG | Service: Test 2-arg (ApplicationContext) | `TextToSpeech(appContext, listener)` | ⚠️ |
| 4 | TEST_APP_CONTEXT_3ARG | Service: Test 3-arg Ivona (ApplicationContext) | `TextToSpeech(appContext, listener, "ivona.tts")` | ✅ |
| 5 | TEST_QUEUE_ADD | Service: Test QUEUE_ADD Mode | Multiple speak() with QUEUE_ADD | ⚠️ |
| 6 | TEST_BUNDLE_PARAMS | Service: Test Bundle Parameters | speak() with volume bundle | ⚠️ |
| 7 | TEST_ENGINE_VERIFICATION | Service: Test Engine Verification | Pre-flight engine check | ✅ |
| 8 | TEST_FOREGROUND_SERVICE_LISTENER | Service: Foreground Service + Listener (CRITICAL) | **Promotion + Listener BEFORE speak()** | ✅✅✅ |
| 9 | TEST_LANGUAGE_AVAILABILITY | Service: Language Availability Check | isLanguageAvailable() checks | ✅ |
| 10 | TEST_AUDIO_ATTRIBUTES_USAGE | Service: Audio Attributes USAGE Types | setAudioAttributes() with 5 USAGE types | ✅ |
| 11 | TEST_SPEECH_RATE_PITCH | Service: Speech Rate & Pitch | setSpeechRate() + setPitch() | ✅ |
| 12 | TEST_RECOVERY_PATTERN | Service: TTS Recovery Pattern | Stop → Delay → Speak | ✅ |
| 13 | TEST_MULTIPLE_USAGE_TYPES | Service: Multiple USAGE Types | Fallback USAGE strategy | ✅ |

## Critical Findings

### The Pattern That Works ✅
```kotlin
// TEST_FOREGROUND_SERVICE_LISTENER
1. tts.stop()                          // Step 1
2. Thread.sleep(50)                    // Step 2: Wait for stop to complete
3. promoteToForegroundService()        // Step 3: Service to foreground
4. Thread.sleep(100)                   // Step 4: Wait for system recognition
5. tts.setOnUtteranceProgressListener() // Step 5: Listener BEFORE speak()
6. requestAudioFocus()                 // Step 6: With setWillPauseWhenDucked(false)
7. tts.speak()                         // Step 7: Finally call speak()
```

### Audio Attributes Pattern
```kotlin
// SpeakThat uses USAGE_ASSISTANCE_NAVIGATION_GUIDANCE (default)
setAudioAttributes(
    AudioAttributes.Builder()
        .setUsage(USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)  // ← Default
        .setContentType(CONTENT_TYPE_SPEECH)              // ← Always
        .build()
)
```

### speak() Signature
```kotlin
// ALWAYS this pattern:
tts.speak(
    text,
    TextToSpeech.QUEUE_FLUSH,           // ← Always flush (not QUEUE_ADD)
    volumeParams,                       // ← Bundle with routing
    "notification_utterance"            // ← Utterance ID for callbacks
)
```

## Test Flow

1. Click button → Notification sent to service
2. Service receives notification in onNotificationPosted()
3. Service reads test type from shared preferences
4. Service executes appropriate test method
5. TTS initialized and speaks
6. Listener callbacks fire (onStart → onDone/onError)
7. Toast shows result
8. Logcat shows detailed flow

## Running All Tests

```bash
# Build
./gradlew :TTSTestApp:assembleDebug -x lint

# Install
adb install TTSTestApp/build/outputs/apk/debug/TTSTestApp-debug.apk

# Grant permissions in Settings:
# - Notification Listener access
# - Modify audio settings

# View logs
adb logcat | grep TestNotificationSvc

# Click each test button in order:
# Button 1: Service: Test 2-arg TTS
# Button 2: Service: Test 3-arg TTS (Ivona)
# ... etc
```

## Success Indicators

✅ Toast appears showing test name
✅ Toast shows "SUCCESS" or completion message
✅ Logcat shows step-by-step execution
✅ Ivona engine speaks the test message
✅ Listener callbacks fire (onStart, onDone)
✅ No error messages in logcat

## Known Issues / Warnings

⚠️ Some tests may fail if Ivona not installed (use system default instead)
⚠️ Requires Notification Listener permission granted
⚠️ Requires Modify Audio Settings permission
⚠️ Some patterns work better on Android 12+ (foreground service requirements)

## File Locations

- **Source**: `/TTSTestApp/src/main/java/com/example/ttstest/`
  - TestNotificationService.kt (all test methods)
  - MainActivity.kt (all buttons)
  
- **APK**: `/TTSTestApp/build/outputs/apk/debug/TTSTestApp-debug.apk` (5.6M)

- **Documentation**:
  - ALL_SPEAKTHAT_PATTERNS_IMPLEMENTED.md (this suite)
  - CRITICAL_TEST_IMPLEMENTATION.md (foreground+listener details)
  - SPEAKTHAT_ANALYSIS_DISCOVERY.md (SpeakThat source analysis)

---

**Status**: ✅ COMPLETE - All 13 patterns implemented, built, and ready for testing
