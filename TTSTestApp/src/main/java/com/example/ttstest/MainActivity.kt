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
            text = "Test 3-arg TTS with Ivona (Activity)"
            setOnClickListener { test3ArgFromActivityWithIvona() }
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
            text = "Service: Test 3-arg TTS (Ivona)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_3ARG_IVONA) }
        }

        val btnServiceAppCtx2Arg = Button(this).apply {
            text = "Service: Test 2-arg (ApplicationContext)"
            setOnClickListener { triggerServiceTest(TestNotificationService.TEST_APP_CONTEXT_2ARG) }
        }

        val btnServiceAppCtx3Arg = Button(this).apply {
            text = "Service: Test 3-arg Ivona (ApplicationContext)"
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

    private fun test3ArgFromActivityWithIvona() {
        log("--- TEST: 3-arg TTS with Ivona from Activity ---")
        val ivonaPackage = "ivona.tts"
        activityTts3Arg?.shutdown()
        activityTts3Arg = TextToSpeech(this, { status ->
            if (status == TextToSpeech.SUCCESS) {
                log("SUCCESS: 3-arg TTS (Ivona) initialized")
                speakTest(activityTts3Arg, "3-arg Ivona Activity test")
            } else {
                log("FAILED: 3-arg TTS (Ivona) init status: $status")
            }
        }, ivonaPackage)
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

    override fun onDestroy() {
        super.onDestroy()
        activityTts2Arg?.shutdown()
        activityTts3Arg?.shutdown()
    }
}