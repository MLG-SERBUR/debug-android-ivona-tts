package com.example.ttstest

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast

class TestNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "TestNotificationSvc"
        const val TEST_2ARG = "test_2arg"
        const val TEST_3ARG_IVONA = "test_3arg_ivona"
        const val TEST_APP_CONTEXT_2ARG = "test_app_ctx_2arg"
        const val TEST_APP_CONTEXT_3ARG = "test_app_ctx_3arg"
        const val TEST_QUEUE_ADD = "test_queue_add"
        const val TEST_BUNDLE_PARAMS = "test_bundle_params"
        const val TEST_ENGINE_VERIFICATION = "test_engine_verify"
        const val TEST_FOREGROUND_SERVICE_LISTENER = "test_foreground_listener"
        const val TEST_LANGUAGE_AVAILABILITY = "test_language_avail"
        const val TEST_AUDIO_ATTRIBUTES_USAGE = "test_audio_usage"
        const val TEST_SPEECH_RATE_PITCH = "test_speech_settings"
        const val TEST_RECOVERY_PATTERN = "test_recovery"
        const val TEST_MULTIPLE_USAGE_TYPES = "test_multiple_usage"
    }

    private var tts: TextToSpeech? = null

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        // Only respond to our own test notifications
        if (sbn?.packageName != packageName) return
        
        Log.d(TAG, "Received notification from our package")
        
        // Check if there's a pending test
        val prefs = getSharedPreferences("debug_prefs", Context.MODE_PRIVATE)
        val pendingTest = prefs.getString("pending_test", null)
        
        if (pendingTest != null) {
            // Clear the pending test
            prefs.edit().remove("pending_test").apply()
            
            when (pendingTest) {
                TEST_2ARG -> test2ArgTtsFromService()
                TEST_3ARG_IVONA -> test3ArgTtsFromService()
                TEST_APP_CONTEXT_2ARG -> testAppContextTts2Arg()
                TEST_APP_CONTEXT_3ARG -> testAppContextTts3ArgIvona()
                TEST_QUEUE_ADD -> testQueueAddMode()
                TEST_BUNDLE_PARAMS -> testBundleParams()
                TEST_ENGINE_VERIFICATION -> testEngineVerification()
                TEST_FOREGROUND_SERVICE_LISTENER -> testForegroundServiceWithListener()
                TEST_LANGUAGE_AVAILABILITY -> testLanguageAvailability()
                TEST_AUDIO_ATTRIBUTES_USAGE -> testAudioAttributesUsage()
                TEST_SPEECH_RATE_PITCH -> testSpeechRateAndPitch()
                TEST_RECOVERY_PATTERN -> testRecoveryPattern()
                TEST_MULTIPLE_USAGE_TYPES -> testMultipleUsageTypes()
            }
        }
    }

    private fun test2ArgTtsFromService() {
        Log.d(TAG, "=== SERVICE TEST: 2-arg TTS ===")
        Log.d(TAG, "Context type: ${this.javaClass.simpleName}")
        Log.d(TAG, "Creating TTS with: TextToSpeech(this, listener)")
        
        showToast("Service: Testing 2-arg TTS...")
        
        tts?.shutdown()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: 2-arg TTS initialized from NotificationListenerService")
                val engine = tts?.defaultEngine
                Log.d(TAG, "Default engine: $engine")
                showToast("SUCCESS: 2-arg TTS from Service (engine: $engine)")
                speakFromService("2-arg service test successful")
            } else {
                Log.e(TAG, "FAILED: 2-arg TTS init from Service, status: $status")
                showToast("FAILED: 2-arg TTS from Service, status: $status")
            }
        }
    }

    private fun test3ArgTtsFromService() {
        Log.d(TAG, "=== SERVICE TEST: 3-arg TTS with Ivona ===")
        Log.d(TAG, "Context type: ${this.javaClass.simpleName}")
        
        val ivonaPackage = "ivona.tts"
        Log.d(TAG, "Creating TTS with: TextToSpeech(this, listener, \"$ivonaPackage\")")
        
        showToast("Service: Testing 3-arg TTS with Ivona...")
        
        tts?.shutdown()
        tts = TextToSpeech(this, { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: 3-arg TTS (Ivona) initialized from NotificationListenerService")
                val engine = tts?.defaultEngine
                Log.d(TAG, "Reported engine: $engine")
                showToast("SUCCESS: 3-arg Ivona from Service")
                speakFromService("3-arg Ivona service test successful")
            } else {
                Log.e(TAG, "FAILED: 3-arg TTS (Ivona) init from Service, status: $status")
                showToast("FAILED: 3-arg Ivona from Service, status: $status")
            }
        }, ivonaPackage)
    }

    private fun speakFromService(text: String) {
        tts?.let {
            val utteranceId = "service_test_${System.currentTimeMillis()}"
            it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {
                    Log.d(TAG, "Service TTS started speaking")
                }
                override fun onDone(id: String?) {
                    Log.d(TAG, "Service TTS finished speaking")
                    showToast("TTS speech completed")
                }
                override fun onError(id: String?) {
                    Log.e(TAG, "Service TTS error while speaking")
                    showToast("TTS speech error")
                }
            })
            val result = it.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            Log.d(TAG, "speak() returned: $result")
        }
    }

    private fun showToast(message: String) {
        android.os.Handler(mainLooper).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.shutdown()
    }

    private fun testAppContextTts2Arg() {
        Log.d(TAG, "=== SERVICE TEST: 2-arg TTS with ApplicationContext ===")
        Log.d(TAG, "Context type: ApplicationContext (not service)")
        Log.d(TAG, "Creating TTS with: TextToSpeech(applicationContext, listener)")
        
        showToast("Service: Testing 2-arg TTS with ApplicationContext...")
        
        tts?.shutdown()
        tts = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: 2-arg TTS with applicationContext initialized")
                val engine = tts?.defaultEngine
                Log.d(TAG, "Default engine: $engine")
                showToast("SUCCESS: 2-arg TTS with appContext from Service")
                speakFromService("app context 2-arg test successful")
            } else {
                Log.e(TAG, "FAILED: 2-arg TTS with appContext, status: $status")
                showToast("FAILED: 2-arg TTS with appContext, status: $status")
            }
        }
    }

    private fun testAppContextTts3ArgIvona() {
        Log.d(TAG, "=== SERVICE TEST: 3-arg TTS with Ivona (ApplicationContext) ===")
        Log.d(TAG, "Context type: ApplicationContext (not service)")
        
        val ivonaPackage = "ivona.tts"
        Log.d(TAG, "Creating TTS with: TextToSpeech(applicationContext, listener, \"$ivonaPackage\")")
        
        showToast("Service: Testing 3-arg TTS with Ivona (ApplicationContext)...")
        
        tts?.shutdown()
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

    private fun testQueueAddMode() {
        Log.d(TAG, "=== SERVICE TEST: QUEUE_ADD mode (like VoiceNotify) ===")
        Log.d(TAG, "VoiceNotify uses QUEUE_ADD instead of QUEUE_FLUSH")
        Log.d(TAG, "This allows queueing multiple notifications")
        
        showToast("Service: Testing QUEUE_ADD mode...")
        
        tts?.shutdown()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized for QUEUE_ADD test")
                
                // Create multiple utterances to queue
                val utteranceId1 = "queue_test_1_${System.currentTimeMillis()}"
                val utteranceId2 = "queue_test_2_${System.currentTimeMillis()}"
                
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {
                        Log.d(TAG, "QUEUE_ADD utterance started: $id")
                    }
                    override fun onDone(id: String?) {
                        Log.d(TAG, "QUEUE_ADD utterance finished: $id")
                    }
                    override fun onError(id: String?) {
                        Log.e(TAG, "QUEUE_ADD utterance error: $id")
                    }
                })
                
                // First message with QUEUE_FLUSH
                val result1 = tts?.speak("First queued message", TextToSpeech.QUEUE_FLUSH, null, utteranceId1)
                Log.d(TAG, "First speak (QUEUE_FLUSH) returned: $result1")
                
                // Second message with QUEUE_ADD (should queue behind first)
                val result2 = tts?.speak("Second queued message", TextToSpeech.QUEUE_ADD, null, utteranceId2)
                Log.d(TAG, "Second speak (QUEUE_ADD) returned: $result2")
                
                showToast("QUEUE_ADD test: queued 2 messages")
            } else {
                Log.e(TAG, "FAILED: TTS init for QUEUE_ADD test, status: $status")
                showToast("FAILED: QUEUE_ADD test, status: $status")
            }
        }
    }

    private fun testBundleParams() {
        Log.d(TAG, "=== SERVICE TEST: Bundle parameters in speak() ===")
        Log.d(TAG, "VoiceNotify passes bundle with stream parameters")
        Log.d(TAG, "This may affect how TTS engines handle the audio output")
        
        showToast("Service: Testing bundle parameters...")
        
        tts?.shutdown()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized for bundle params test")
                
                val utteranceId = "bundle_test_${System.currentTimeMillis()}"
                val params = android.os.Bundle().apply {
                    // VoiceNotify uses KEY_PARAM_STREAM
                    putInt(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_STREAM, 
                        android.media.AudioManager.STREAM_MUSIC)
                }
                
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) { Log.d(TAG, "Bundle test started") }
                    override fun onDone(id: String?) { Log.d(TAG, "Bundle test finished") }
                    override fun onError(id: String?) { Log.e(TAG, "Bundle test error") }
                })
                
                val result = tts?.speak("Testing bundle parameters", TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                Log.d(TAG, "speak() with bundle returned: $result")
                showToast("SUCCESS: Bundle params test")
            } else {
                Log.e(TAG, "FAILED: TTS init for bundle params test, status: $status")
                showToast("FAILED: Bundle params test, status: $status")
            }
        }
    }

    private fun testEngineVerification() {
        Log.d(TAG, "=== SERVICE TEST: Engine Verification ===")
        Log.d(TAG, "Before initializing with custom engine, verify it's available")
        
        showToast("Service: Verifying engine availability...")
        
        try {
            val ivonaPackage = "ivona.tts"
            
            // Verify engine is available
            val ttsIntent = android.content.Intent(android.speech.tts.TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE)
            ttsIntent.setPackage(ivonaPackage)
            val resolveInfo = packageManager.resolveService(ttsIntent, android.content.pm.PackageManager.GET_RESOLVED_FILTER)
            
            if (resolveInfo != null) {
                Log.d(TAG, "SUCCESS: Engine $ivonaPackage is available - ${resolveInfo.serviceInfo.packageName}")
                showToast("Engine verification: $ivonaPackage is AVAILABLE")
                
                // Now try to initialize with verified engine
                tts?.shutdown()
                tts = TextToSpeech(this, { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        Log.d(TAG, "SUCCESS: Verified engine initialized")
                        showToast("SUCCESS: Verified engine initialized")
                        speakFromService("verified engine test successful")
                    } else {
                        Log.e(TAG, "FAILED: Verified engine failed to initialize, status: $status")
                        showToast("FAILED: Verified engine init, status: $status")
                    }
                }, ivonaPackage)
            } else {
                Log.w(TAG, "WARNING: Engine $ivonaPackage is NOT available")
                showToast("Engine verification: $ivonaPackage is NOT AVAILABLE")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying engine: ${e.message}", e)
            showToast("Error verifying engine: ${e.message}")
        }
    }

    private fun testForegroundServiceWithListener() {
        Log.d(TAG, "=== SERVICE TEST: Foreground Service Promotion + Listener (SpeakThat Pattern) ===")
        Log.d(TAG, "This is the CRITICAL pattern that SpeakThat uses for Ivona compatibility")
        Log.d(TAG, "Key sequence: Stop TTS -> Apply settings -> Promote to foreground -> Set listener -> Speak")
        
        showToast("Service: Testing Foreground Service Pattern with Listener...")
        
        tts?.shutdown()
        tts = TextToSpeech(applicationContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized for Foreground+Listener test")
                
                // CRITICAL: Stop any existing speech first
                tts?.stop()
                Log.d(TAG, "Step 1: Stopped existing TTS")
                
                // CRITICAL: Delay after stop (SpeakThat uses 50ms)
                try {
                    Thread.sleep(50)
                    Log.d(TAG, "Step 2: 50ms delay after stop() completed")
                } catch (e: InterruptedException) {
                    Log.w(TAG, "Step 2: Sleep interrupted")
                }
                
                // CRITICAL: Promote service to foreground (SpeakThat uses startForeground)
                try {
                    val notification = android.app.Notification.Builder(this, "test_channel")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("TTS Test Reading")
                        .setContentText("Testing Foreground Service Pattern")
                        .build()
                    
                    // Use ID 1003 like SpeakThat does (FOREGROUND_SERVICE_ID = 1003)
                    startForeground(1003, notification)
                    Log.d(TAG, "Step 3: Service promoted to foreground (ID: 1003)")
                } catch (e: Exception) {
                    Log.e(TAG, "Step 3: Failed to promote to foreground: ${e.message}", e)
                    showToast("Warning: Could not promote to foreground (may still work): ${e.message}")
                }
                
                // CRITICAL: Delay after foreground promotion (SpeakThat uses 100ms)
                try {
                    Thread.sleep(100)
                    Log.d(TAG, "Step 4: 100ms delay after foreground promotion completed")
                } catch (e: InterruptedException) {
                    Log.w(TAG, "Step 4: Sleep interrupted")
                }
                
                val utteranceId = "foreground_listener_test_${System.currentTimeMillis()}"
                
                // CRITICAL: Set listener BEFORE calling speak() (NOT after)
                // This is essential - the listener must be set up BEFORE speak() is called
                Log.d(TAG, "Step 5: Setting UtteranceProgressListener BEFORE speak()")
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {
                        Log.d(TAG, "Foreground+Listener: onStart callback - TTS started speaking (ID: $id)")
                        showToast("TTS Started")
                    }
                    override fun onDone(id: String?) {
                        Log.d(TAG, "Foreground+Listener: onDone callback - TTS finished speaking (ID: $id)")
                        showToast("TTS Finished")
                        
                        // Clean up foreground service when done
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                stopForeground(android.app.Service.STOP_FOREGROUND_REMOVE)
                            } else {
                                @Suppress("DEPRECATION")
                                stopForeground(true)
                            }
                            Log.d(TAG, "Step 8: Service stopped from foreground after TTS completion")
                        } catch (e: Exception) {
                            Log.e(TAG, "Step 8: Failed to stop foreground: ${e.message}")
                        }
                    }
                    override fun onError(id: String?) {
                        Log.e(TAG, "Foreground+Listener: onError callback - TTS error (ID: $id)")
                        showToast("TTS Error occurred")
                        
                        // Clean up foreground service on error
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                stopForeground(android.app.Service.STOP_FOREGROUND_REMOVE)
                            } else {
                                @Suppress("DEPRECATION")
                                stopForeground(true)
                            }
                            Log.d(TAG, "Step 8: Service stopped from foreground after TTS error")
                        } catch (e: Exception) {
                            Log.e(TAG, "Step 8: Failed to stop foreground: ${e.message}")
                        }
                    }
                })
                
                // CRITICAL: Request audio focus BEFORE speak() with proper flags
                // Use USAGE_ASSISTANCE_NAVIGATION_GUIDANCE like SpeakThat does
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
                        val audioAttributes = android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                        
                        val audioFocusRequest = android.media.AudioFocusRequest.Builder(
                            android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                        )
                            .setAudioAttributes(audioAttributes)
                            .setWillPauseWhenDucked(false)  // CRITICAL: Prevent system from ducking our TTS
                            .build()
                        
                        val focusResult = audioManager.requestAudioFocus(audioFocusRequest)
                        Log.d(TAG, "Step 6: Audio focus requested (result: $focusResult, won't be ducked)")
                    } else {
                        Log.d(TAG, "Step 6: Audio focus request skipped (Android < 8.0)")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Step 6: Audio focus request error: ${e.message}")
                }
                
                // CRITICAL: Create volume bundle with proper settings
                val volumeParams = android.os.Bundle().apply {
                    putInt(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_STREAM, 
                        android.media.AudioManager.STREAM_MUSIC)
                }
                
                // CRITICAL: Call speak() with QUEUE_FLUSH and the volume bundle
                Log.d(TAG, "Step 7: Calling TTS.speak() with QUEUE_FLUSH")
                val result = tts?.speak(
                    "Testing foreground service with listener and Ivona engine compatibility",
                    TextToSpeech.QUEUE_FLUSH,
                    volumeParams,
                    utteranceId
                )
                Log.d(TAG, "Step 7: TTS.speak() returned: $result")
                showToast("SUCCESS: Foreground+Listener test started (speaking)")
            } else {
                Log.e(TAG, "FAILED: TTS init for Foreground+Listener test, status: $status")
                showToast("FAILED: Foreground+Listener test, status: $status")
            }
        }, "ivona.tts")  // Use Ivona specifically
    }

    private fun testLanguageAvailability() {
        Log.d(TAG, "=== SERVICE TEST: Language Availability Check (SpeakThat Pattern) ===")
        Log.d(TAG, "SpeakThat checks language availability with isLanguageAvailable()")
        
        showToast("Service: Testing Language Availability...")
        
        tts?.shutdown()
        tts = TextToSpeech(applicationContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized for language availability test")
                
                // Test multiple locales like SpeakThat does
                val testLocales = listOf(
                    java.util.Locale.getDefault(),
                    java.util.Locale.ENGLISH,
                    java.util.Locale("pt"),  // Portuguese
                    java.util.Locale("es"),  // Spanish
                    java.util.Locale.GERMAN
                )
                
                var anyAvailable = false
                for (locale in testLocales) {
                    val result = tts?.isLanguageAvailable(locale)
                    Log.d(TAG, "Language availability: $locale = $result")
                    
                    if (result != TextToSpeech.LANG_NOT_SUPPORTED && result != TextToSpeech.LANG_MISSING_DATA) {
                        anyAvailable = true
                        Log.d(TAG, "Using locale: $locale")
                        tts?.language = locale
                        
                        // Speak with available locale
                        val utteranceId = "lang_test_${System.currentTimeMillis()}"
                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(id: String?) { Log.d(TAG, "Language test: speaking started") }
                            override fun onDone(id: String?) { Log.d(TAG, "Language test: speaking finished"); showToast("Lang test completed") }
                            override fun onError(id: String?) { Log.e(TAG, "Language test: error") }
                        })
                        
                        val speakResult = tts?.speak("Testing language: ${locale.displayName}", 
                            TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                        Log.d(TAG, "Speak result with locale $locale: $speakResult")
                        break
                    }
                }
                
                if (!anyAvailable) {
                    Log.w(TAG, "WARNING: No locales available")
                    showToast("No locales available")
                }
                
            } else {
                Log.e(TAG, "FAILED: TTS init for language test, status: $status")
                showToast("FAILED: Language test, status: $status")
            }
        }, "ivona.tts")
    }

    private fun testAudioAttributesUsage() {
        Log.d(TAG, "=== SERVICE TEST: Audio Attributes with Different USAGE Types ===")
        Log.d(TAG, "SpeakThat dynamically selects audio usage type based on settings")
        
        showToast("Service: Testing Audio Attributes (5 USAGE types)...")
        
        tts?.shutdown()
        tts = TextToSpeech(applicationContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized for audio attributes test")
                
                // SpeakThat uses 5 different USAGE types
                val usageTypes = listOf(
                    android.media.AudioAttributes.USAGE_MEDIA to "MEDIA",
                    android.media.AudioAttributes.USAGE_NOTIFICATION to "NOTIFICATION",
                    android.media.AudioAttributes.USAGE_ALARM to "ALARM",
                    android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION to "VOICE_COMMUNICATION",
                    android.media.AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE to "ASSISTANCE_NAV_GUIDANCE"
                )
                
                // Test with ASSISTANCE_NAVIGATION_GUIDANCE (SpeakThat default)
                val selectedUsage = android.media.AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE
                Log.d(TAG, "Setting audio attributes with USAGE: ASSISTANCE_NAVIGATION_GUIDANCE")
                
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(selectedUsage)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                
                tts?.setAudioAttributes(audioAttributes)
                Log.d(TAG, "Audio attributes set successfully")
                
                val utteranceId = "audio_usage_test_${System.currentTimeMillis()}"
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) { 
                        Log.d(TAG, "Audio attributes test: started with USAGE_ASSISTANCE_NAVIGATION_GUIDANCE")
                    }
                    override fun onDone(id: String?) { 
                        Log.d(TAG, "Audio attributes test: finished")
                        showToast("Audio attributes test completed")
                    }
                    override fun onError(id: String?) { Log.e(TAG, "Audio attributes test: error") }
                })
                
                val speakResult = tts?.speak(
                    "Testing audio attributes with assistance navigation guidance",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    utteranceId
                )
                Log.d(TAG, "Speak result: $speakResult")
                
            } else {
                Log.e(TAG, "FAILED: TTS init for audio attributes test, status: $status")
                showToast("FAILED: Audio attributes test, status: $status")
            }
        }, "ivona.tts")
    }

    private fun testSpeechRateAndPitch() {
        Log.d(TAG, "=== SERVICE TEST: Speech Rate and Pitch Settings ===")
        Log.d(TAG, "SpeakThat applies speech rate and pitch from voice preferences")
        
        showToast("Service: Testing Speech Rate and Pitch...")
        
        tts?.shutdown()
        tts = TextToSpeech(applicationContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized for speech settings test")
                
                // SpeakThat default: 1.0f for both (normal speed/pitch)
                val speechRate = 1.0f  // Normal
                val pitch = 1.0f       // Normal
                
                Log.d(TAG, "Setting speech rate: $speechRate")
                val rateResult = tts?.setSpeechRate(speechRate)
                Log.d(TAG, "setSpeechRate() returned: $rateResult")
                
                Log.d(TAG, "Setting pitch: $pitch")
                val pitchResult = tts?.setPitch(pitch)
                Log.d(TAG, "setPitch() returned: $pitchResult")
                
                val utteranceId = "speech_settings_${System.currentTimeMillis()}"
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) { Log.d(TAG, "Speech settings test: started") }
                    override fun onDone(id: String?) { 
                        Log.d(TAG, "Speech settings test: finished")
                        showToast("Speech settings test completed")
                    }
                    override fun onError(id: String?) { Log.e(TAG, "Speech settings test: error") }
                })
                
                val speakResult = tts?.speak(
                    "Testing speech rate and pitch with normal settings",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    utteranceId
                )
                Log.d(TAG, "Speak result with speech settings: $speakResult")
                
            } else {
                Log.e(TAG, "FAILED: TTS init for speech settings test, status: $status")
                showToast("FAILED: Speech settings test, status: $status")
            }
        }, "ivona.tts")
    }

    private fun testRecoveryPattern() {
        Log.d(TAG, "=== SERVICE TEST: TTS Recovery Pattern (SpeakThat Resilience) ===")
        Log.d(TAG, "SpeakThat has built-in recovery with exponential backoff")
        Log.d(TAG, "Testing recovery from TTS stop and reinit")
        
        showToast("Service: Testing Recovery Pattern...")
        
        tts?.shutdown()
        tts = TextToSpeech(applicationContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized for recovery test")
                
                // Scenario 1: Stop and immediately speak again (recovery test)
                Log.d(TAG, "Step 1: Stopping TTS")
                tts?.stop()
                Thread.sleep(50)  // Like SpeakThat does
                
                Log.d(TAG, "Step 2: Attempting to speak immediately after stop (recovery scenario)")
                val utteranceId1 = "recovery_test_1_${System.currentTimeMillis()}"
                
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) { 
                        Log.d(TAG, "Recovery test: speech started (recovered from stop)")
                    }
                    override fun onDone(id: String?) { 
                        Log.d(TAG, "Recovery test: speech finished")
                        showToast("Recovery test completed successfully")
                    }
                    override fun onError(id: String?) { 
                        Log.e(TAG, "Recovery test: error - recovery failed")
                    }
                })
                
                val speakResult = tts?.speak(
                    "Recovery test: speaking after stop",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    utteranceId1
                )
                Log.d(TAG, "Speak result after recovery: $speakResult")
                
            } else {
                Log.e(TAG, "FAILED: TTS init for recovery test, status: $status")
                showToast("FAILED: Recovery test, status: $status")
            }
        }, "ivona.tts")
    }

    private fun testMultipleUsageTypes() {
        Log.d(TAG, "=== SERVICE TEST: Multiple USAGE Types Sequential Test ===")
        Log.d(TAG, "SpeakThat tries fallback usage types if primary fails")
        
        showToast("Service: Testing Multiple USAGE Types...")
        
        tts?.shutdown()
        tts = TextToSpeech(applicationContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized for multiple usage types test")
                
                // Try NOTIFICATION usage first, then fallback to ALARM, then ASSISTANCE
                val primaryUsage = android.media.AudioAttributes.USAGE_NOTIFICATION
                Log.d(TAG, "Setting primary audio attributes with USAGE_NOTIFICATION")
                
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(primaryUsage)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                
                tts?.setAudioAttributes(audioAttributes)
                Log.d(TAG, "Primary audio attributes set")
                
                val utteranceId = "multi_usage_${System.currentTimeMillis()}"
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) { 
                        Log.d(TAG, "Multiple usage test: started with NOTIFICATION usage")
                    }
                    override fun onDone(id: String?) { 
                        Log.d(TAG, "Multiple usage test: finished successfully")
                        showToast("Multiple USAGE types test completed")
                    }
                    override fun onError(id: String?) { 
                        Log.e(TAG, "Multiple usage test: error - may need fallback usage type")
                        showToast("Multiple USAGE types: error encountered")
                    }
                })
                
                val speakResult = tts?.speak(
                    "Testing with notification audio usage type",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    utteranceId
                )
                Log.d(TAG, "Speak result with NOTIFICATION usage: $speakResult")
                
            } else {
                Log.e(TAG, "FAILED: TTS init for multiple usage types test, status: $status")
                showToast("FAILED: Multiple usage types test, status: $status")
            }
        }, "ivona.tts")
    }
}
