package com.example.ttstest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DebugIvonaTTS"
        private const val CHANNEL_ID = "debug_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private lateinit var logTextView: TextView
    private lateinit var scrollView: ScrollView
    private val logBuilder = StringBuilder()

    private var activityTts2Arg: TextToSpeech? = null
    private var activityTts3Arg: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scrollView = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        logTextView = TextView(this).apply {
            textSize = 12f
            setTextIsSelectable(true)
        }

        val btn2ArgActivity = Button(this).apply {
            text = "Test 2-arg TTS (Activity Context)"
            setOnClickListener { test2ArgFromActivity() }
        }

        val btn3ArgActivity = Button(this).apply {
            text = "Test 3-arg TTS (Activity)"
            setOnClickListener { test3ArgFromActivity() }
        }

        val btnCheckPermission = Button(this).apply {
            text = "Check NotificationListener Permission"
            setOnClickListener { checkNotificationListenerPermission() }
        }

        val btnOpenSettings = Button(this).apply {
            text = "Open NotificationListener Settings"
            setOnClickListener { openNotificationListenerSettings() }
        }

        val btnSendNotification = Button(this).apply {
            text = "Send Test Notification"
            setOnClickListener { sendTestNotification() }
        }

        val btnServiceTest2Arg = Button(this).apply {
            text = "Service: Test 2-arg TTS"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_2ARG) }
        }

        val btnServiceTest3Arg = Button(this).apply {
            text = "Service: Test 3-arg TTS"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_3ARG) }
        }

        val btnServiceAppCtx2Arg = Button(this).apply {
            text = "Service: Test 2-arg (ApplicationContext)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_APP_CONTEXT_2ARG) }
        }

        val btnServiceAppCtx3Arg = Button(this).apply {
            text = "Service: Test 3-arg (ApplicationContext)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_APP_CONTEXT_3ARG) }
        }

        val btnServiceQueueAdd = Button(this).apply {
            text = "Service: Test QUEUE_ADD Mode"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_QUEUE_ADD) }
        }

        val btnServiceBundleParams = Button(this).apply {
            text = "Service: Test Bundle Parameters"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_BUNDLE_PARAMS) }
        }

        val btnServiceEngineVerify = Button(this).apply {
            text = "Service: Test Engine Verification"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_ENGINE_VERIFICATION) }
        }

        val btnServiceForegroundListener = Button(this).apply {
            text = "Service: Foreground Service + Listener (CRITICAL)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_FOREGROUND_SERVICE_LISTENER) }
        }

        val btnServiceLanguageAvail = Button(this).apply {
            text = "Service: Language Availability Check"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_LANGUAGE_AVAILABILITY) }
        }

        val btnServiceAudioUsage = Button(this).apply {
            text = "Service: Audio Attributes USAGE Types"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_AUDIO_ATTRIBUTES_USAGE) }
        }

        val btnServiceSpeechSettings = Button(this).apply {
            text = "Service: Speech Rate & Pitch"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_SPEECH_RATE_PITCH) }
        }

        val btnServiceRecovery = Button(this).apply {
            text = "Service: TTS Recovery Pattern"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_RECOVERY_PATTERN) }
        }

        val btnServiceMultipleUsage = Button(this).apply {
            text = "Service: Multiple USAGE Types"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_MULTIPLE_USAGE_TYPES) }
        }

        val btnServiceSpeakThatExact = Button(this).apply {
            text = "Service: SpeakThat Exact Pattern (CRITICAL)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_SPEAKTHAT_EXACT) }
        }

        val btnServiceUsageAssistant = Button(this).apply {
            text = "Service: Test USAGE_ASSISTANT"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_USAGE_ASSISTANT) }
        }

        val btnServiceLanguageEnUs = Button(this).apply {
            text = "Service: Force en_US Language (CRITICAL)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_LANGUAGE_EN_US_EXPLICIT) }
        }

        val btnServiceLanguageCheck = Button(this).apply {
            text = "Service: Proper Language Check"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_LANGUAGE_AVAILABILITY_CHECK) }
        }

        val btnServiceSpeakThatExecution = Button(this).apply {
            text = "Service: SpeakThat COMPLETE Execution (SUPER CRITICAL)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_SPEAKTHAT_EXECUTION_PATTERN) }
        }

        val btnServiceStopBeforeSpeak = Button(this).apply {
            text = "Service: stop() Before speak()"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_STOP_BEFORE_SPEAK) }
        }

        val btnServiceReapplySettings = Button(this).apply {
            text = "Service: Reapply Settings Before speak()"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_REAPPLY_SETTINGS_BEFORE_SPEAK) }
        }

        val btnServiceRecoveryPattern = Button(this).apply {
            text = "Service: TTS Recovery Pattern (THE BUG!)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_TTS_RECOVERY_PATTERN) }
        }

        val btnServicePrematureRecovery = Button(this).apply {
            text = "Service: Premature Recovery (SpeakThat Logic)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_PREMATURE_RECOVERY) }
        }

        val btnToggleEngine = Button(this).apply {
            val prefs = getSharedPreferences("debug_prefs", Context.MODE_PRIVATE)
            var useIvona = prefs.getBoolean("use_ivona", false)
            text = if (useIvona) "Using: IVONA" else "Using: DEFAULT"
            
            setOnClickListener {
                useIvona = !useIvona
                prefs.edit().putBoolean("use_ivona", useIvona).apply()
                text = if (useIvona) "Using: IVONA" else "Using: DEFAULT"
                showToast("Engine changed to ${if (useIvona) "Ivona" else "Default"}")
            }
        }

        val btnServiceListenerAfter = Button(this).apply {
            text = "Service: Listener AFTER speak()"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_LISTENER_AFTER_SPEAK) }
        }

        val btnServiceVolumeBundle = Button(this).apply {
            text = "Service: speak() with Volume Bundle"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_VOLUME_BUNDLE) }
        }

        val btnServiceSpeakThatClone = Button(this).apply {
            text = "Service: SpeakThat Clone (Listener After + Bundle)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_LISTENER_AFTER_SPEAK_WITH_BUNDLE) }
        }

        val btnListEngines = Button(this).apply {
            text = "List All TTS Engines"
            setOnClickListener { listAllTtsEngines() }
        }

        val btnClearLog = Button(this).apply {
            text = "Clear Log"
            setOnClickListener { clearLog() }
        }

        container.addView(btn2ArgActivity)
        container.addView(btn3ArgActivity)
        container.addView(btnCheckPermission)
        container.addView(btnOpenSettings)
        container.addView(btnSendNotification)
        container.addView(btnServiceTest2Arg)
        container.addView(btnServiceTest3Arg)
        container.addView(btnServiceAppCtx2Arg)
        container.addView(btnServiceAppCtx3Arg)
        container.addView(btnServiceQueueAdd)
        container.addView(btnServiceBundleParams)
        container.addView(btnServiceEngineVerify)
        container.addView(btnServiceForegroundListener)
        container.addView(btnServiceLanguageAvail)
        container.addView(btnServiceAudioUsage)
        container.addView(btnServiceSpeechSettings)
        container.addView(btnServiceRecovery)
        container.addView(btnServiceMultipleUsage)
        container.addView(btnServiceSpeakThatExact)
        container.addView(btnServiceUsageAssistant)
        container.addView(btnServiceLanguageEnUs)
        container.addView(btnServiceLanguageCheck)
        container.addView(btnServiceSpeakThatExecution)
        container.addView(btnServiceStopBeforeSpeak)
        container.addView(btnServiceReapplySettings)
        container.addView(btnServiceRecoveryPattern)
        container.addView(btnServicePrematureRecovery)
        container.addView(btnToggleEngine)
        container.addView(btnServiceListenerAfter)
        container.addView(btnServiceVolumeBundle)
        container.addView(btnServiceSpeakThatClone)
        container.addView(btnListEngines)
        container.addView(btnClearLog)
        container.addView(logTextView)

        scrollView.addView(container)
        setContentView(scrollView)

        createNotificationChannel()
        log("=== Debug Ivona TTS Test App ===")
        log("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        log("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    }

    private fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val logLine = "[$timestamp] $message\n"
        Log.d(TAG, message)
        logBuilder.append(logLine)
        runOnUiThread {
            logTextView.text = logBuilder.toString()
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }

    private fun clearLog() {
        logBuilder.clear()
        logTextView.text = ""
    }

    private fun test2ArgFromActivity() {
        log("--- TEST: 2-arg TTS from Activity ---")
        activityTts2Arg?.shutdown()
        activityTts2Arg = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                log("SUCCESS: 2-arg TTS initialized")
                speakTest(activityTts2Arg, "2-arg Activity test")
            } else {
                log("FAILED: 2-arg TTS init status: $status")
            }
        }
    }

    private fun test3ArgFromActivity() {
        Log.d(TAG, "--- TEST: 3-arg TTS with Selected Engine from Activity ---")
        val enginePackage = getEnginePackage()
        
        log("Creating TTS with: TextToSpeech(this, listener, \"${enginePackage ?: "default"}\")")
        
        activityTts3Arg?.shutdown()
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                log("SUCCESS: 3-arg TTS initialized")
                speakTest(activityTts3Arg, "3-arg Activity test")
            } else {
                log("FAILED: 3-arg TTS init status: $status")
            }
        }

        activityTts3Arg = if (enginePackage != null) {
            TextToSpeech(this, listener, enginePackage)
        } else {
            TextToSpeech(this, listener)
        }
    }

    private fun speakTest(tts: TextToSpeech?, label: String) {
        tts?.let {
            val utteranceId = "test_${System.currentTimeMillis()}"
            it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) { log("TTS started: $label") }
                override fun onDone(id: String?) { log("TTS finished: $label") }
                override fun onError(id: String?) { log("TTS error: $label") }
            })
            val result = it.speak("Testing $label", TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            log("speak() returned: $result")
        }
    }

    private fun checkNotificationListenerPermission() {
        val componentName = ComponentName(this, TestNotificationService::class.java)
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val isEnabled = enabledListeners?.contains(componentName.flattenToString()) == true
        log("NotificationListener enabled: $isEnabled")
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun triggerServiceTest(testType: String) {
        val componentName = ComponentName(this, TestNotificationService::class.java)
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (enabledListeners?.contains(componentName.flattenToString()) != true) {
            log("ERROR: Permission not granted")
            return
        }

        getSharedPreferences("debug_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("pending_test", testType)
            .apply()

        sendTestNotification()
    }

    private fun sendTestNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("TTS Test")
            .setContentText("Triggering service...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Debug", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun listAllTtsEngines() {
        log("--- Listing All TTS Engines ---")
        
        // FIX: Use a temporary variable and assign it to a var so it can be 
        // referenced inside the lambda listener correctly.
        var tempTts: TextToSpeech? = null
        tempTts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Now tempTts is accessible here
                val engines = tempTts?.engines
                engines?.forEach { engine ->
                    log("  Engine: ${engine.label} (${engine.name})")
                }
                tempTts?.shutdown()
            }
        }
    }

    private fun getEnginePackage(): String? {
        val prefs = getSharedPreferences("debug_prefs", Context.MODE_PRIVATE)
        val useIvona = prefs.getBoolean("use_ivona", false)
        return if (useIvona) "ivona.tts" else null
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityTts2Arg?.shutdown()
        activityTts3Arg?.shutdown()
    }
}