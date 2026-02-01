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
        const val TEST_3ARG = "test_3arg"
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
        const val TEST_SPEAKTHAT_EXACT = "test_speakthat_exact"
        const val TEST_USAGE_ASSISTANT = "test_usage_assistant"
        const val TEST_LANGUAGE_EN_US_EXPLICIT = "test_language_en_us_explicit"
        const val TEST_LANGUAGE_AVAILABILITY_CHECK = "test_language_availability_check"
        const val TEST_SPEAKTHAT_EXECUTION_PATTERN = "test_speakthat_execution_pattern"
        const val TEST_STOP_BEFORE_SPEAK = "test_stop_before_speak"
        const val TEST_REAPPLY_SETTINGS_BEFORE_SPEAK = "test_reapply_settings_before_speak"
        const val TEST_TTS_RECOVERY_PATTERN = "test_tts_recovery_pattern"
        const val TEST_LISTENER_AFTER_SPEAK = "test_listener_after_speak"
        const val TEST_VOLUME_BUNDLE = "test_volume_bundle"
        const val TEST_LISTENER_AFTER_SPEAK_WITH_BUNDLE = "test_listener_after_speak_with_bundle"
        const val TEST_PREMATURE_RECOVERY = "test_premature_recovery"
    }

    private fun getEnginePackage(): String? {
        val prefs = getSharedPreferences("debug_prefs", Context.MODE_PRIVATE)
        val useIvona = prefs.getBoolean("use_ivona", false)
        return if (useIvona) "ivona.tts" else null
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
                TEST_2ARG -> test2ArgTts()
                TEST_3ARG -> test3ArgTtsFromService()
                TEST_APP_CONTEXT_2ARG -> testAppContextTts2Arg()
                TEST_APP_CONTEXT_3ARG -> testAppContextTts3Arg()
                TEST_QUEUE_ADD -> testQueueAddMode()
                TEST_BUNDLE_PARAMS -> testBundleParams()
                TEST_ENGINE_VERIFICATION -> testEngineVerification()
                TEST_FOREGROUND_SERVICE_LISTENER -> testForegroundServiceWithListener()
                TEST_LANGUAGE_AVAILABILITY -> testLanguageAvailability()
                TEST_AUDIO_ATTRIBUTES_USAGE -> testAudioAttributesUsage()
                TEST_SPEECH_RATE_PITCH -> testSpeechRateAndPitch()
                TEST_RECOVERY_PATTERN -> testRecoveryPattern()
                TEST_MULTIPLE_USAGE_TYPES -> testMultipleUsageTypes()
                TEST_SPEAKTHAT_EXACT -> testSpeakThatExactPattern()
                TEST_USAGE_ASSISTANT -> testUsageAssistant()
                TEST_LANGUAGE_EN_US_EXPLICIT -> testLanguageEnUsExplicit()
                TEST_LANGUAGE_AVAILABILITY_CHECK -> testLanguageAvailabilityCheck()
                TEST_SPEAKTHAT_EXECUTION_PATTERN -> testSpeakThatExecutionPattern()
                TEST_STOP_BEFORE_SPEAK -> testStopBeforeSpeak()
                TEST_REAPPLY_SETTINGS_BEFORE_SPEAK -> testReapplySettingsBeforeSpeak()
                TEST_TTS_RECOVERY_PATTERN -> testTtsRecoveryPattern()
                TEST_LISTENER_AFTER_SPEAK -> testListenerAfterSpeak()
                TEST_VOLUME_BUNDLE -> testVolumeBundle()
                TEST_LISTENER_AFTER_SPEAK_WITH_BUNDLE -> testListenerAfterSpeakWithBundle()
                TEST_PREMATURE_RECOVERY -> testPrematureRecovery()
            }
        }
    }

    // ... (existing helper methods)

    private fun test2ArgTts() {
        Log.d(TAG, "=== SERVICE TEST: 2-arg TTS (Service Context) ===")
        showToast("Service: Testing 2-arg TTS...")
        
        tts?.shutdown()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: 2-arg TTS initialized")
                showToast("SUCCESS: 2-arg TTS from Service")
                speakFromService("2-arg service test successful")
            } else {
                Log.e(TAG, "FAILED: 2-arg TTS init, status: $status")
                showToast("FAILED: 2-arg init, status: $status")
            }
        }
    }

    private fun testSpeakThatExactPattern() {
        Log.d(TAG, "=== SERVICE TEST: SpeakThat Exact Pattern ===")
        val enginePackage = getEnginePackage()
        Log.d(TAG, "Replicating: TextToSpeech(this, listener, \"${enginePackage ?: "default"}\")")
        Log.d(TAG, "AND running on MAIN THREAD (like SpeakThat onCreate)")
        Log.d(TAG, "AND setting usage to USAGE_ASSISTANT in onInit")

        showToast("Service: Testing SpeakThat Exact Pattern...")

        // SpeakThat initializes in onCreate(), which is Main Thread.
        // We must ensure this test runs on Main Thread to replicate it.
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Log.d(TAG, "Initializing TTS on Thread: ${Thread.currentThread().name}")
            
            tts?.shutdown()
            
            val listener = TextToSpeech.OnInitListener { status ->
                Log.d(TAG, "onInit callback received on Thread: ${Thread.currentThread().name}")
                if (status == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "SUCCESS: TTS init (SpeakThat pattern)")
                    
                    try {
                        // Mimic SpeakThat: Set AudioAttributes to USAGE_ASSISTANT
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            Log.d(TAG, "Setting AudioAttributes: USAGE_ASSISTANT")
                            val attrs = android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_ASSISTANT)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                            tts?.setAudioAttributes(attrs)
                            Log.d(TAG, "setAudioAttributes called successfully")
                        }
                        
                        // Mimic SpeakThat: Apply voice settings
                        tts?.setSpeechRate(1.0f)
                        tts?.setPitch(1.0f)
                        Log.d(TAG, "Voice settings applied")
                        
                        showToast("SUCCESS: SpeakThat Pattern initialized")
                        speakFromService("SpeakThat exact pattern test successful")
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "ERROR in onInit (SpeakThat pattern): ${e.message}", e)
                        showToast("ERROR in onInit: ${e.message}")
                    }
                    
                } else {
                    Log.e(TAG, "FAILED: TTS init (SpeakThat pattern), status: $status")
                    showToast("FAILED: SpeakThat Pattern init, status: $status")
                }
            }

            tts = if (enginePackage != null) {
                TextToSpeech(this, listener, enginePackage)
            } else {
                TextToSpeech(this, listener)
            }
        }
    }

    private fun testUsageAssistant() {
        Log.d(TAG, "=== SERVICE TEST: Audio Attributes USAGE_ASSISTANT ===")
        Log.d(TAG, "Targeting specifically USAGE_ASSISTANT (16) which SpeakThat uses")
        
        showToast("Service: Testing USAGE_ASSISTANT...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized")
                
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        Log.d(TAG, "Setting usage: USAGE_ASSISTANT")
                        val attrs = android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_ASSISTANT)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                        tts?.setAudioAttributes(attrs)
                        Log.d(TAG, "Attributes set")
                    }
                    
                    val result = tts?.speak("Testing usage assistant", TextToSpeech.QUEUE_FLUSH, null, "usage_assistant_test")
                    Log.d(TAG, "Speak result: $result")
                    showToast("USAGE_ASSISTANT test: Speak result $result")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting USAGE_ASSISTANT", e)
                    showToast("Error: ${e.message}")
                }
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }


    private fun testLanguageEnUsExplicit() {
        Log.d(TAG, "=== SERVICE TEST: Explicit en_US Language Setting (CRITICAL) ===")
        Log.d(TAG, "This replicates SpeakThat's exact behavior that causes LANG_MISSING_DATA")
        
        showToast("Service: Testing explicit en_US language...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized")
                
                try {
                    // Replicate SpeakThat: Set language to en_US explicitly
                    val locale = java.util.Locale("en", "US")
                    Log.d(TAG, "Attempting to set language: $locale")
                    
                    val langResult = tts?.setLanguage(locale)
                    Log.d(TAG, "setLanguage() returned: $langResult")
                    
                    when (langResult) {
                        TextToSpeech.LANG_MISSING_DATA -> {
                            Log.e(TAG, "LANG_MISSING_DATA (-2) - This is the SpeakThat issue!")
                            showToast("LANG_MISSING_DATA - This causes SpeakThat to fail!")
                        }
                        TextToSpeech.LANG_NOT_SUPPORTED -> {
                            Log.e(TAG, "LANG_NOT_SUPPORTED (-1)")
                            showToast("Language not supported")
                        }
                        TextToSpeech.LANG_AVAILABLE -> {
                            Log.d(TAG, "LANG_AVAILABLE (0)")
                            showToast("Language available (basic)")
                        }
                        TextToSpeech.LANG_COUNTRY_AVAILABLE -> {
                            Log.d(TAG, "LANG_COUNTRY_AVAILABLE (1)")
                            showToast("Language + country available")
                        }
                        TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                            Log.d(TAG, "LANG_COUNTRY_VAR_AVAILABLE (2)")
                            showToast("Full locale available")
                        }
                        else -> {
                            Log.w(TAG, "Unknown language result: $langResult")
                            showToast("Unknown result: $langResult")
                        }
                    }
                    
                    // Try to speak anyway to see what happens
                    Log.d(TAG, "Attempting to speak after language set...")
                    val speakResult = tts?.speak(
                        "Testing after explicit en US language setting", 
                        TextToSpeech.QUEUE_FLUSH, 
                        null, 
                        "lang_test"
                    )
                    Log.d(TAG, "speak() returned: $speakResult")
                    
                    if (speakResult != TextToSpeech.SUCCESS) {
                        Log.e(TAG, "speak() failed with result: $speakResult")
                        showToast("speak() failed: $speakResult")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error during language test", e)
                    showToast("Error: ${e.message}")
                }
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
                showToast("TTS init failed: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun testLanguageAvailabilityCheck() {
        Log.d(TAG, "=== SERVICE TEST: Proper Language Availability Check ===")
        Log.d(TAG, "This is the CORRECT way to handle language setting")
        
        showToast("Service: Testing proper language availability check...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized")
                
                try {
                    val locale = java.util.Locale("en", "US")
                    
                    // CORRECT: Check availability BEFORE setting
                    Log.d(TAG, "Checking if $locale is available...")
                    val availResult = tts?.isLanguageAvailable(locale)
                    Log.d(TAG, "isLanguageAvailable() returned: $availResult")
                    
                    when (availResult) {
                        TextToSpeech.LANG_MISSING_DATA -> {
                            Log.w(TAG, "Language data missing - using default instead")
                            showToast("Missing data - using default")
                            // Don't call setLanguage(), just use default
                        }
                        TextToSpeech.LANG_NOT_SUPPORTED -> {
                            Log.w(TAG, "Language not supported - using default")
                            showToast("Not supported - using default")
                            // Don't call setLanguage(), just use default
                        }
                        else -> {
                            // Available at some level - safe to set
                            Log.d(TAG, "Language available - setting it")
                            val setResult = tts?.setLanguage(locale)
                            Log.d(TAG, "setLanguage() returned: $setResult")
                            showToast("Language set successfully")
                        }
                    }
                    
                    // Now try to speak
                    Log.d(TAG, "Attempting to speak...")
                    speakFromService("Testing with proper language availability check")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error during availability check test", e)
                    showToast("Error: ${e.message}")
                }
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
                showToast("TTS init failed: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun testSpeakThatExecutionPattern() {
        Log.d(TAG, "=== SERVICE TEST: SpeakThat COMPLETE Execution Pattern (SUPER CRITICAL) ===")
        Log.d(TAG, "This is the EXACT sequence SpeakThat uses for every speak request")
        Log.d(TAG, "Sequence: stop() -> sleep(50ms) -> applyVoiceSettings() -> foreground -> sleep(100ms) -> speak()")
        
        showToast("Service: Testing SpeakThat COMPLETE execution pattern...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized for execution pattern test")
                
                try {
                    // STEP 1: STOP any existing TTS speech (even though there isn't any yet)
                    Log.d(TAG, "Step 1: Calling stop() on fresh TTS instance")
                    tts?.stop()
                    
                    // STEP 2: 50ms delay after stop
                    Log.d(TAG, "Step 2: Sleeping 50ms after stop()")
                    Thread.sleep(50)
                    
                    // STEP 3: Apply voice settings (reapply even if already set in onInit)
                    Log.d(TAG, "Step 3: Reapplying voice settings")
                    val locale = java.util.Locale("en", "US")
                    val langResult = tts?.setLanguage(locale)
                    Log.d(TAG, "  - setLanguage(en_US) returned: $langResult")
                    
                    tts?.setSpeechRate(1.0f)
                    tts?.setPitch(1.0f)
                    Log.d(TAG, "  - Speech rate and pitch set to 1.0")
                    
                    val attrs = android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA) // SpeakThat actually uses MEDIA
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(attrs)
                    Log.d(TAG, "  - Audio attributes set to USAGE_MEDIA")
                    
                    // STEP 4: Promote to foreground service
                    Log.d(TAG, "Step 4: Promoting to foreground service")
                    try {
                        val notification = android.app.Notification.Builder(this, "test_channel")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle("TTS Test Reading")
                            .setContentText("Testing SpeakThat Execution Pattern")
                            .build()
                        startForeground(1003, notification)
                        Log.d(TAG, "  - Service promoted to foreground")
                    } catch (e: Exception) {
                        Log.e(TAG, "  - Failed to promote to foreground: ${e.message}")
                    }
                    
                   // STEP 5: 100ms delay after foreground promotion
                    Log.d(TAG, "Step 5: Sleeping 100ms after foreground promotion")
                    Thread.sleep(100)
                    
                    // STEP 6: Finally, speak
                    Log.d(TAG, "Step 6: Calling speak()")
                    val speakResult = tts?.speak(
                        "Testing SpeakThat complete execution pattern",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "execution_pattern_test"
                    )
                    Log.d(TAG, "  - speak() returned: $speakResult")
                    
                    if (speakResult == TextToSpeech.SUCCESS) {
                        showToast("SpeakThat execution pattern: speak() SUCCESS")
                    } else {
                        showToast("SpeakThat execution pattern: speak() FAILED: $speakResult")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in SpeakThat execution pattern test", e)
                    showToast("Error: ${e.message}")
                }
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
                showToast("TTS init failed: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun testStopBeforeSpeak() {
        Log.d(TAG, "=== SERVICE TEST: stop() Before speak() ===")
        Log.d(TAG, "Testing if calling stop() on a fresh TTS instance causes issues")
        
        showToast("Service: Testing stop() before speak()...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized")
                
                try {
                    // Call stop() even though nothing is playing
                    Log.d(TAG, "Calling stop() on fresh TTS instance")
                    tts?.stop()
                    
                    // Small delay like SpeakThat
                    Thread.sleep(50)
                    
                    // Now try to speak
                    Log.d(TAG, "Attempting to speak after stop()")
                    val speakResult = tts?.speak(
                        "Testing speak after stop",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "stop_test"
                    )
                    Log.d(TAG, "speak() returned: $speakResult")
                    
                    if (speakResult == TextToSpeech.SUCCESS) {
                        showToast("stop() before speak(): SUCCESS")
                    } else {
                        showToast("stop() before speak(): FAILED: $speakResult")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in stop test", e)
                    showToast("Error: ${e.message}")
                }
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun testReapplySettingsBeforeSpeak() {
        Log.d(TAG, "=== SERVICE TEST: Reapply Settings Before Each speak() ===")
        Log.d(TAG, "Testing if reapplying settings before speak() causes issues")
        
        showToast("Service: Testing settings reapplication...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized")
                
                try {
                    // Apply settings once in onInit
                    Log.d(TAG, "First settings application (in onInit)")
                    val locale = java.util.Locale("en", "US")
                    tts?.setLanguage(locale)
                    tts?.setSpeechRate(1.0f)
                    tts?.setPitch(1.0f)
                    
                    // Wait a bit
                    Thread.sleep(100)
                    
                    // Reapply the SAME settings again (like SpeakThat does before every speak)
                    Log.d(TAG, "Second settings application (before speak)")
                    val langResult2 = tts?.setLanguage(locale)
                    Log.d(TAG, "  - setLanguage() second call returned: $langResult2")
                    tts?.setSpeechRate(1.0f)
                    tts?.setPitch(1.0f)
                    
                    // Now speak
                    Log.d(TAG, "Attempting to speak after reapplying settings")
                    val speakResult = tts?.speak(
                        "Testing after reapplying settings",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "reapply_test"
                    )
                    Log.d(TAG, "speak() returned: $speakResult")
                    
                    if (speakResult == TextToSpeech.SUCCESS) {
                        showToast("Settings reapplication: SUCCESS")
                    } else {
                        showToast("Settings reapplication: FAILED: $speakResult")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in reapply settings test", e)
                    showToast("Error: ${e.message}")
                }
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun testTtsRecoveryPattern() {
        Log.d(TAG, "=== SERVICE TEST: TTS Recovery Pattern (THE BUG!) ===")
        Log.d(TAG, "This replicates SpeakThat's BROKEN recovery logic")
        Log.d(TAG, "Bug: TTS recovery reinitializes WITHOUT the engine package!")
        
        showToast("Service: Testing TTS recovery pattern...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        
        // STEP 1: Initialize with selected engine (like normal)
        Log.d(TAG, "Step 1: Initializing TTS with selected engine: ${enginePackage ?: "default"}")
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "  - SUCCESS: TTS initialized")
                val engine1 = tts?.defaultEngine
                Log.d(TAG, "  - Current engine: $engine1")
                
                // Simulate speaking once successfully
                val firstSpeak = tts?.speak("First speak works", TextToSpeech.QUEUE_FLUSH, null, "first")
                Log.d(TAG, "  - First speak() returned: $firstSpeak")
                
                // Wait for first speak to complete
                Thread.sleep(2000)
                
                // STEP 2: Simulate recovery trigger (like when language returns -2)
                Log.d(TAG, "Step 2: SIMULATING TTS RECOVERY due to language error")
                
                // STEP 3: Shutdown existing TTS (SpeakThat recovery line 1199)
                Log.d(TAG, "Step 3: Shutting down TTS")
                try {
                    tts?.shutdown()
                    Log.d(TAG, "  - Shutdown complete")
                } catch (e: Exception) {
                    Log.e(TAG, "  - Error during shutdown: ${e.message}")
                }
                
                // STEP 4: Clear state (SpeakThat recovery lines 1206-1207)
                Log.d(TAG, "Step 4: Clearing TTS state")
                tts = null
                
                // STEP 5: Wait 500ms (SpeakThat recovery line 1211)
                Log.d(TAG, "Step 5: Waiting 500ms for cleanup")
                Thread.sleep(500)
                
                // STEP 6: REINITIALIZE WITHOUT ENGINE PACKAGE (THE BUG - line 1237)
                Log.d(TAG, "Step 6: Reinitializing TTS WITHOUT engine package (BUG!)")
                Log.d(TAG, "  - SpeakThat does: TextToSpeech(this, this)")
                Log.d(TAG, "  - This switches from selected engine to DEFAULT engine!")
                
                tts = TextToSpeech(this@TestNotificationService, { status2 ->
                    if (status2 == TextToSpeech.SUCCESS) {
                        Log.d(TAG, "  - SUCCESS: TTS reinitialized (but with DIFFERENT engine!)")
                        val engine2 = tts?.defaultEngine
                        Log.d(TAG, "  - NEW engine after recovery: $engine2")
                        
                        if (engine1 != engine2) {
                            Log.e(TAG, "  - ENGINE CHANGED! Was: $engine1, Now: $engine2")
                            showToast("BUG CONFIRMED: Engine switched from $engine1 to $engine2!")
                        } else {
                            Log.w(TAG, "  - Engine stayed the same")
                            showToast("Engine stayed same: $engine2")
                        }
                        
                        // Try to speak with the new engine
                        val secondSpeak = tts?.speak(
                            "After recovery with different engine",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "second"
                        )
                        Log.d(TAG, "  - Second speak() returned: $secondSpeak")
                        
                    } else {
                        Log.e(TAG, "  - FAILED: Recovery reinit failed, status: $status2")
                        showToast("Recovery reinit failed: $status2")
                    }
                }) // NOTE: NO ENGINE PACKAGE HERE - this is the bug!
                
            } else {
                Log.e(TAG, "FAILED: Initial TTS init failed, status: $status")
                showToast("Initial TTS init failed: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun testListenerAfterSpeak() {
        Log.d(TAG, "=== SERVICE TEST: setOnUtteranceProgressListener AFTER speak() ===")
        Log.d(TAG, "This replicates SpeakThat's pattern (Line 5038 speak vs Line 5060 listener)")
        
        showToast("Service: Testing listener AFTER speak()...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized")
                
                // STEP 1: Call speak()
                Log.d(TAG, "Step 1: Calling speak() BEFORE setting listener")
                val utteranceId = "after_speak_test"
                val result = tts?.speak("Testing listener set after speak", TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                Log.d(TAG, "  - speak() returned: $result")
                
                // STEP 2: IMMEDIATELY set listener
                Log.d(TAG, "Step 2: Setting UtteranceProgressListener")
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(id: String?) {
                        Log.d(TAG, "  - Callback RECEIVED: onStart for $id")
                        showToast("onStart received!")
                    }
                    override fun onDone(id: String?) {
                        Log.d(TAG, "  - Callback RECEIVED: onDone for $id")
                        showToast("onDone received!")
                    }
                    override fun onError(id: String?) {
                        Log.e(TAG, "  - Callback RECEIVED: onError for $id")
                        showToast("onError received!")
                    }
                })
                
                // Check if we missed it
                Log.d(TAG, "Listener applied. If onStart/onDone don't fire, we missed the window.")
                
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun testVolumeBundle() {
        Log.d(TAG, "=== SERVICE TEST: speak() with SpeakThat Volume Bundle ===")
        Log.d(TAG, "This tests if Ivona chokes on the specific Bundle parameters SpeakThat sends")
        
        showToast("Service: Testing speak() with volume bundle...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized")
                
                // Mimic SpeakThat's volume bundle creation
                val bundle = android.os.Bundle()
                bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_MUSIC)
                
                Log.d(TAG, "Step 1: Calling speak() with KEY_PARAM_VOLUME and KEY_PARAM_STREAM bundle")
                val result = tts?.speak("Testing volume bundle parameters", TextToSpeech.QUEUE_FLUSH, bundle, "bundle_test")
                Log.d(TAG, "  - speak() returned: $result")
                
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun testListenerAfterSpeakWithBundle() {
        Log.d(TAG, "=== SERVICE TEST: Listener AFTER speak() + Volume Bundle (SpeakThat Clone) ===")
        Log.d(TAG, "Combining the two suspicious patterns")
        
        showToast("Service: Testing SpeakThat Clone (Listener after + Bundle)...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized")
                
                val bundle = android.os.Bundle()
                bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_MUSIC)
                
                Log.d(TAG, "Step 1: Calling speak() with bundle")
                tts?.speak("Testing combined listener and bundle pattern", TextToSpeech.QUEUE_FLUSH, bundle, "combined_test")
                
                Log.d(TAG, "Step 2: Setting listener AFTER speak()")
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(id: String?) { Log.d(TAG, "  - onStart $id") }
                    override fun onDone(id: String?) { 
                        Log.d(TAG, "  - onDone $id")
                        showToast("Combined test completed!")
                    }
                    override fun onError(id: String?) { Log.e(TAG, "  - onError $id") }
                })
                
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }



    private fun testPrematureRecovery() {
        Log.d(TAG, "=== SERVICE TEST: Premature Recovery Pattern (SpeakThat Replication) ===")
        Log.d(TAG, "This test triggers recovery due to 'unsupported language' and then speaks")
        Log.d(TAG, "during the recovery delay. Matches SpeakThat logs exactly.")
        
        showToast("Service: Testing Premature Recovery...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: TTS initialized")
                
                // 1. Replicate SpeakThat: Set language to en_US which returns -2 (LANG_MISSING_DATA)
                val locale = java.util.Locale("en", "US")
                val langResult = tts?.setLanguage(locale)
                Log.d(TAG, "setLanguage(en_US) result: $langResult")
                
                // 2. Replicate SpeakThat checkTtsHealth(): check isLanguageAvailable(default)
                val isAvailable = tts?.isLanguageAvailable(java.util.Locale.getDefault()) ?: TextToSpeech.LANG_NOT_SUPPORTED
                Log.d(TAG, "isLanguageAvailable(default) result: $isAvailable")
                
                if (isAvailable == TextToSpeech.LANG_NOT_SUPPORTED || langResult == TextToSpeech.LANG_MISSING_DATA) {
                    Log.w(TAG, "UNHEALTHY: Triggering recovery pattern with 1000ms delay")
                    
                    // 3. Schedule recovery (SpeakThat recovery delay is 1000ms)
                    val handler = android.os.Handler(android.os.Looper.getMainLooper())
                    handler.postDelayed({
                        Log.w(TAG, "RECOVERY EXECUTING: Shutting down TTS instance mid-speech!")
                        tts?.shutdown()
                        tts = null
                        showToast("Recovery shut down TTS!")
                    }, 1000)
                    
                    // 4. SpeakThat returns false from health check, BUT if a speak call happens anyway:
                    Log.d(TAG, "Attempting speak call IMMEDIATELY after triggering recovery")
                    val result = tts?.speak("Testing speech that starts before the recovery shutdown happens.", TextToSpeech.QUEUE_FLUSH, null, "premature_recovery_test")
                    Log.d(TAG, "speak() returned: $result")
                    showToast("Speak call made! Watch for callbacks.")
                } else {
                    showToast("Language supported, recovery not triggered.")
                    speakFromService("Language supported")
                }
            } else {
                Log.e(TAG, "FAILED: TTS init, status: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun test3ArgTtsFromService() {
        Log.d(TAG, "=== SERVICE TEST: 3-arg TTS with Selected Engine ===")
        Log.d(TAG, "Context type: ${this.javaClass.simpleName}")
        
        showToast("Service: Testing 3-arg TTS with selected engine...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: 3-arg TTS initialized from NotificationListenerService")
                val engine = tts?.defaultEngine
                Log.d(TAG, "Reported engine: $engine")
                showToast("SUCCESS: 3-arg TTS from Service")
                speakFromService("3-arg service test successful")
            } else {
                Log.e(TAG, "FAILED: 3-arg TTS init from Service, status: $status")
                showToast("FAILED: 3-arg init from Service, status: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
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

    private fun testAppContextTts3Arg() {
        Log.d(TAG, "=== SERVICE TEST: 3-arg TTS with Selected Engine (ApplicationContext) ===")
        Log.d(TAG, "Context type: ApplicationContext (not service)")
        val enginePackage = getEnginePackage()
        
        Log.d(TAG, "Creating TTS with: TextToSpeech(applicationContext, listener, \"${enginePackage ?: "default"}\")")
        
        showToast("Service: Testing 3-arg TTS with selected engine (ApplicationContext)...")
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "SUCCESS: 3-arg TTS with applicationContext initialized")
                val engine = tts?.defaultEngine
                Log.d(TAG, "Reported engine: $engine")
                showToast("SUCCESS: 3-arg with appContext from Service")
                speakFromService("app context 3-arg test successful")
            } else {
                Log.e(TAG, "FAILED: 3-arg TTS with appContext, status: $status")
                showToast("FAILED: 3-arg with appContext, status: $status")
            }
        }

        tts = if (enginePackage != null) {
            TextToSpeech(applicationContext, listener, enginePackage)
        } else {
            TextToSpeech(applicationContext, listener)
        }
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
        val enginePackage = getEnginePackage()
        
        if (enginePackage == null) {
            showToast("System default engine used - skipping verification")
            return
        }

        showToast("Service: Verifying engine availability...")
        
        try {
            // Verify engine is available
            val ttsIntent = android.content.Intent(android.speech.tts.TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE)
            ttsIntent.setPackage(enginePackage)
            val resolveInfo = packageManager.resolveService(ttsIntent, android.content.pm.PackageManager.GET_RESOLVED_FILTER)
            
            if (resolveInfo != null) {
                Log.d(TAG, "SUCCESS: Engine $enginePackage is available - ${resolveInfo.serviceInfo.packageName}")
                showToast("Engine verification: $enginePackage is AVAILABLE")
                
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
                }, enginePackage)
            } else {
                Log.w(TAG, "WARNING: Engine $enginePackage is NOT available")
                showToast("Engine verification: $enginePackage is NOT AVAILABLE")
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
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
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
                    "Testing foreground service with listener and engine compatibility",
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
        }

        tts = if (enginePackage != null) {
            TextToSpeech(applicationContext, listener, enginePackage)
        } else {
            TextToSpeech(applicationContext, listener)
        }
    }

    private fun testLanguageAvailability() {
        Log.d(TAG, "=== SERVICE TEST: Language Availability Check (SpeakThat Pattern) ===")
        Log.d(TAG, "SpeakThat checks language availability with isLanguageAvailable()")
        
        showToast("Service: Testing Language Availability...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
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
        }

        tts = if (enginePackage != null) {
            TextToSpeech(applicationContext, listener, enginePackage)
        } else {
            TextToSpeech(applicationContext, listener)
        }
    }

    private fun testAudioAttributesUsage() {
        Log.d(TAG, "=== SERVICE TEST: Audio Attributes with Different USAGE Types ===")
        Log.d(TAG, "SpeakThat dynamically selects audio usage type based on settings")
        
        showToast("Service: Testing Audio Attributes (5 USAGE types)...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
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
        }

        tts = if (enginePackage != null) {
            TextToSpeech(applicationContext, listener, enginePackage)
        } else {
            TextToSpeech(applicationContext, listener)
        }
    }

    private fun testSpeechRateAndPitch() {
        Log.d(TAG, "=== SERVICE TEST: Speech Rate and Pitch Settings ===")
        Log.d(TAG, "SpeakThat applies speech rate and pitch from voice preferences")
        
        showToast("Service: Testing Speech Rate and Pitch...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
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
        }

        tts = if (enginePackage != null) {
            TextToSpeech(applicationContext, listener, enginePackage)
        } else {
            TextToSpeech(applicationContext, listener)
        }
    }

    private fun testRecoveryPattern() {
        Log.d(TAG, "=== SERVICE TEST: TTS Recovery Pattern (SpeakThat Resilience) ===")
        Log.d(TAG, "SpeakThat has built-in recovery with exponential backoff")
        Log.d(TAG, "Testing recovery from TTS stop and reinit")
        
        showToast("Service: Testing Recovery Pattern...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
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
        }

        tts = if (enginePackage != null) {
            TextToSpeech(applicationContext, listener, enginePackage)
        } else {
            TextToSpeech(applicationContext, listener)
        }
    }

    private fun testMultipleUsageTypes() {
        Log.d(TAG, "=== SERVICE TEST: Multiple USAGE Types Sequential Test ===")
        Log.d(TAG, "SpeakThat tries fallback usage types if primary fails")
        
        showToast("Service: Testing Multiple USAGE Types...")
        val enginePackage = getEnginePackage()
        
        tts?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
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
        }

        tts = if (enginePackage != null) {
            TextToSpeech(applicationContext, listener, enginePackage)
        } else {
            TextToSpeech(applicationContext, listener)
        }
    }
}
