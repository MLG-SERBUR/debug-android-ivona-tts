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
}
