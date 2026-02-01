# TTS Invocation Method Comparison with Code Snippets

## Complete Code Snippet Comparison

### 1. CONTEXT TYPE - The Root Cause

#### SpeakThat ❌ (FAILS with Ivona from NotificationListenerService)

```kotlin
// File: app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt
// Lines: 1076-1116
// Context: NotificationListenerService

private fun initializeTextToSpeech() {
    try {
        Log.d(TAG, "Starting TTS initialization...")
        
        // Get selected TTS engine from preferences
        val voiceSettingsPrefs = getSharedPreferences("VoiceSettings", Context.MODE_PRIVATE)
        val selectedEngine = voiceSettingsPrefs.getString("tts_engine_package", "")
        
        if (selectedEngine.isNullOrEmpty()) {
            // Use system default engine
            // ❌ PROBLEM: Still using service context (this)
            textToSpeech = TextToSpeech(this, this)
            Log.d(TAG, "Using system default TTS engine")
        } else {
            // Use selected custom engine
            // ❌ CRITICAL PROBLEM: Service context + custom engine package
            // This fails on Android 12+ with Ivona TTS
            textToSpeech = TextToSpeech(this, this, selectedEngine)
            Log.d(TAG, "Using custom TTS engine: $selectedEngine")
        }
        
        Log.d(TAG, "TTS initialization started")
    } catch (e: Exception) {
        Log.e(TAG, "Error in initializeTextToSpeech", e)
        attemptTtsRecovery("Initialization exception: ${e.message}")
    }
}
```

**Why this fails:**
- Service context has restricted access to system services on Android 12+
- Package visibility rules prevent binding to third-party TTS engines
- Passing custom engine package (`ivona.tts`) requires higher permission scope

---

#### VoiceNotify ✅ (WORKS with Ivona)

```kotlin
// File: app/src/main/java/com/pilot51/voicenotify/Service.kt
// Lines: 146-174
// Context: NotificationListenerService (inherits from it)

private suspend fun initTts(onInit: (initSuccess: Boolean) -> Unit) {
    val isAwaiting = isAwaitingTtsInit.value
    if (tts != null || isAwaiting) {
        if (isAwaiting) try {
            withTimeout(2.seconds) {
                isAwaitingTtsInit.first { !it }
            }
        } catch (_: TimeoutCancellationException) {
            // timeout
        }
        onInit(tts != null)
        return
    }

    isAwaitingTtsInit.value = true
    // ✅ CORRECT: Using application context, not service context
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
}
```

**Why this works:**
- Application context has broader service binding permissions
- No custom engine package passed (simpler, more reliable)
- Compatible with Android 12+ package visibility rules

---

### 2. QUEUE MODE

#### SpeakThat - QUEUE_FLUSH

```kotlin
// File: app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt
// Lines: 5034-5040

private fun executeSpeech(speechText: String, appName: String, originalText: String, ...) {
    // ...
    
    val speakResult = textToSpeech?.speak(
        finalSpeechText, 
        TextToSpeech.QUEUE_FLUSH,  // ← Interrupts current speech
        volumeParams, 
        "notification_utterance"
    )
    
    Log.d(TAG, "TTS.speak() returned: $speakResult")
    
    // ...
}
```

**Effect**: Each new notification interrupts the current one immediately

---

#### VoiceNotify - QUEUE_ADD

```kotlin
// File: app/src/main/java/com/pilot51/voicenotify/Service.kt
// Lines: 441-450

private suspend fun speak(info: NotificationInfo) {
    if (!isRunning.value) {
        Log.w(TAG, "Speak failed due to service destroyed")
        info.addIgnoreReasons(IgnoreReason.SERVICE_STOPPED)
        NotifyList.updateInfo(info)
        return
    }
    
    // ... queue management ...
    
    initTts { initSuccess ->
        ioScope.launch {
            val isSpeakFailed = !initSuccess || tts?.speak(
                info.ttsMessage,
                TextToSpeech.QUEUE_ADD,  // ← Queues notification
                getTtsParams(info.settings),
                utteranceId.toString()
            ) != TextToSpeech.SUCCESS
            
            if (isSpeakFailed) {
                Log.e(TAG, "Error adding notification to TTS queue.")
                info.isInterrupted = false
                if (restartingTts) {
                    info.addIgnoreReasons(IgnoreReason.TTS_RESTARTED)
                } else {
                    info.addIgnoreReasons(IgnoreReason.TTS_FAILED)
                    restartTts()
                }
            }
        }
    }
}
```

**Effect**: Notifications are queued and played sequentially (more reliable for batch notifications)

---

### 3. BUNDLE PARAMETERS

#### SpeakThat - Volume Parameters Bundle

```kotlin
// File: app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt
// In executeSpeech() method

val voicePrefs = getSharedPreferences("VoiceSettings", MODE_PRIVATE)
val ttsVolume = voicePrefs.getFloat("tts_volume", 1.0f)
val ttsUsageIndex = voicePrefs.getInt("audio_usage", 4)
val speakerphoneEnabled = voicePrefs.getBoolean("speakerphone_enabled", false)

val volumeParams = VoiceSettingsActivity.createVolumeBundle(
    ttsVolume, 
    ttsUsage, 
    speakerphoneEnabled
)

val speakResult = textToSpeech?.speak(
    finalSpeechText,
    TextToSpeech.QUEUE_FLUSH,
    volumeParams,  // ← Custom volume parameters
    "notification_utterance"
)
```

---

#### VoiceNotify - Stream Parameters Bundle

```kotlin
// File: app/src/main/java/com/pilot51/voicenotify/Service.kt
// Lines: 719-722

private fun getTtsParams(settings: Settings) = Bundle().apply {
    putInt(
        TextToSpeech.Engine.KEY_PARAM_STREAM,
        settings.ttsStream!!  // ← Explicit stream parameter
    )
}

// Usage in speak():
val isSpeakFailed = !initSuccess || tts?.speak(
    info.ttsMessage,
    TextToSpeech.QUEUE_ADD,
    getTtsParams(info.settings),  // ← Pass stream params
    utteranceId.toString()
) != TextToSpeech.SUCCESS
```

**Difference**: 
- SpeakThat passes volume control parameters
- VoiceNotify passes audio stream parameters
- Both use Bundle approach, but for different purposes

---

### 4. ENGINE INITIALIZATION COMPARISON

#### SpeakThat - Conditional Engine Handling

```kotlin
// File: NotificationReaderService.kt lines 1107-1116

if (selectedEngine.isNullOrEmpty()) {
    // Use system default engine
    textToSpeech = TextToSpeech(this, this)
    Log.d(TAG, "Using system default TTS engine")
} else {
    // Use selected custom engine
    // ❌ PROBLEM: Service context + custom engine
    textToSpeech = TextToSpeech(this, this, selectedEngine)
    Log.d(TAG, "Using custom TTS engine: $selectedEngine")
}
```

**Problem**: Tries to pass custom engine from service context

---

#### VoiceNotify - Always Use App Context

```kotlin
// File: Service.kt lines 160-174

isAwaitingTtsInit.value = true
// ✅ SOLUTION: Always use appContext, never pass custom engine
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
    }
})
```

**Solution**: Uses application context consistently, no custom engine package

---

### 5. IN-APP vs NOTIFICATION TTS

#### SpeakThat In-App (Works with Ivona)

```kotlin
// File: app/src/main/java/com/micoyc/speakthat/MainActivity.kt
// Lines: 556-588

private fun initializeTextToSpeech() {
    // MainActivity context - ✅ Activity context has better permissions
    textToSpeech = TextToSpeech(this) { status ->
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS initialized successfully in MainActivity")
            onTtsInitialized()
        } else {
            Log.e(TAG, "TTS initialization failed: $status")
            showError("Failed to initialize TTS")
        }
    }
}
```

**Why it works**: Activity context has broader permissions than service context

---

#### SpeakThat Notification (Fails with Ivona)

```kotlin
// File: app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt
// Lines: 1076-1116

// NotificationReaderService context - ❌ Service context is restricted
if (selectedEngine.isNullOrEmpty()) {
    textToSpeech = TextToSpeech(this, this)
} else {
    textToSpeech = TextToSpeech(this, this, selectedEngine)  // ❌ FAILS with Ivona
}
```

**Why it fails**: Service context is restricted on Android 12+

---

## Test App Implementation

### New Tests in TTSTestApp

#### Test Case: Application Context with Ivona (Critical)

```kotlin
// File: TTSTestApp/src/main/java/com/example/ttstest/TestNotificationService.kt

private fun testAppContextTts3ArgIvona() {
    Log.d(TAG, "=== SERVICE TEST: 3-arg TTS with Ivona (ApplicationContext) ===")
    Log.d(TAG, "Context type: ApplicationContext (not service)")
    
    val ivonaPackage = "ivona.tts"
    Log.d(TAG, "Creating TTS with: TextToSpeech(applicationContext, listener, \"$ivonaPackage\")")
    
    showToast("Service: Testing 3-arg TTS with Ivona (ApplicationContext)...")
    
    tts?.shutdown()
    // ✅ This is the fix: Use applicationContext instead of 'this'
    tts = TextToSpeech(applicationContext, { status ->
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "SUCCESS: 3-arg TTS (Ivona) with applicationContext initialized")
            val engine = tts?.defaultEngine
            Log.d(TAG, "Reported engine: $engine")
            showToast("SUCCESS: 3-arg Ivona with appContext from Service")
            speakFromService("app context 3-arg Ivona test successful")
        } else {
            Log.e(TAG, "FAILED: 3-arg TTS (Ivona) with appContext, status: $status")
            showToast("FAILED: 3-arg Ivona with appContext, status: $status")
        }
    }, ivonaPackage)
}
```

**Expected**: If this passes, the fix works for SpeakThat!

---

#### Test Case: Bundle Parameters

```kotlin
private fun testBundleParams() {
    Log.d(TAG, "=== SERVICE TEST: Bundle parameters in speak() ===")
    Log.d(TAG, "VoiceNotify passes bundle with stream parameters")
    
    showToast("Service: Testing bundle parameters...")
    
    tts?.shutdown()
    tts = TextToSpeech(this) { status ->
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "SUCCESS: TTS initialized for bundle params test")
            
            val utteranceId = "bundle_test_${System.currentTimeMillis()}"
            val params = android.os.Bundle().apply {
                // ✅ VoiceNotify approach: Pass KEY_PARAM_STREAM
                putInt(
                    android.speech.tts.TextToSpeech.Engine.KEY_PARAM_STREAM,
                    android.media.AudioManager.STREAM_MUSIC
                )
            }
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) { Log.d(TAG, "Bundle test started") }
                override fun onDone(id: String?) { Log.d(TAG, "Bundle test finished") }
                override fun onError(id: String?) { Log.e(TAG, "Bundle test error") }
            })
            
            val result = tts?.speak(
                "Testing bundle parameters",
                TextToSpeech.QUEUE_FLUSH,
                params,  // ✅ Pass the bundle with stream parameters
                utteranceId
            )
            Log.d(TAG, "speak() with bundle returned: $result")
            showToast("SUCCESS: Bundle params test")
        } else {
            Log.e(TAG, "FAILED: TTS init for bundle params test, status: $status")
            showToast("FAILED: Bundle params test, status: $status")
        }
    }
}
```

---

## Summary Table

| Aspect | SpeakThat | VoiceNotify | Test Coverage |
|--------|-----------|------------|----------------|
| **Context** | ❌ `this` | ✅ `appContext` | ✅ Test 1, 2 |
| **Custom Engine** | Passed from service | Never passed | ✅ Test 2 |
| **Queue Mode** | QUEUE_FLUSH | QUEUE_ADD | ✅ Test 3 |
| **Bundle Params** | Volume params | Stream params | ✅ Test 4 |
| **Engine Verify** | No | Implied | ✅ Test 5 |
| **Ivona Support** | ❌ In service | ✅ Works | ✅ Test 2 result |

---

## Direct GitHub Links with Line Numbers

### SpeakThat Problem Areas

1. **TTS Initialization (Service Context)**
   - https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt#L1107-L1116

2. **speak() Call (QUEUE_FLUSH)**
   - https://github.com/mitchib1440/SpeakThat/blob/main/app/src/main/java/com/micoyc/speakthat/NotificationReaderService.kt#L5034-L5040

### VoiceNotify Working Approaches

1. **TTS Initialization (App Context)**
   - https://github.com/pilot51/voicenotify/blob/main/app/src/main/java/com/pilot51/voicenotify/Service.kt#L160-L174

2. **speak() Call (QUEUE_ADD)**
   - https://github.com/pilot51/voicenotify/blob/main/app/src/main/java/com/pilot51/voicenotify/Service.kt#L448-L450

3. **Bundle Parameters (Stream)**
   - https://github.com/pilot51/voicenotify/blob/main/app/src/main/java/com/pilot51/voicenotify/Service.kt#L719-L722
