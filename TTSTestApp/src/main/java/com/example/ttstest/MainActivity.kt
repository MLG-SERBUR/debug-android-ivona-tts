package com.example.ttstest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val TAG = "TTSTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "MainActivity created - onCreate() called")

        val btn1 = findViewById<Button>(R.id.btn_tts_app_context)
        val btn2 = findViewById<Button>(R.id.btn_tts_service_context)
        val btn3 = findViewById<Button>(R.id.btn_test_speak)
        val statusText = findViewById<TextView>(R.id.status_text)

        btn1.setOnClickListener {
            Log.d(TAG, "Button 1 clicked: Testing TextToSpeech with app context")
            testTtsWithAppContext(statusText)
        }

        btn2.setOnClickListener {
            Log.d(TAG, "Button 2 clicked: Testing TextToSpeech with activity context")
            testTtsWithActivityContext(statusText)
        }

        btn3.setOnClickListener {
            Log.d(TAG, "Button 3 clicked: Triggering test speech")
            testSpeak(statusText)
        }

        Log.d(TAG, "MainActivity UI initialized successfully")
    }

    /**
     * Test 1: TextToSpeech with app context (like VoiceNotify pattern)
     */
    private fun testTtsWithAppContext(statusText: TextView) {
        try {
            Log.d(TAG, "Initializing TTS with app context")
            statusText.text = "Testing: App Context TTS Init..."
            
            // VoiceNotify pattern: uses app context
            tts = TextToSpeech(applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "TTS initialized successfully with app context")
                    statusText.text = "✅ App Context: TTS Ready"
                    tts?.speak("TTS works with app context", TextToSpeech.QUEUE_FLUSH, null)
                } else {
                    Log.e(TAG, "TTS initialization failed with app context, status=$status")
                    statusText.text = "❌ App Context: TTS Failed (status=$status)"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in testTtsWithAppContext: ${e.message}", e)
            statusText.text = "❌ App Context Error: ${e.message}"
        }
    }

    /**
     * Test 2: TextToSpeech with activity context (like SpeakThat pattern)
     */
    private fun testTtsWithActivityContext(statusText: TextView) {
        try {
            Log.d(TAG, "Initializing TTS with activity context")
            statusText.text = "Testing: Activity Context TTS Init..."
            
            // Cleanup old TTS first
            tts?.stop()
            tts?.shutdown()
            
            // SpeakThat pattern: uses activity context with this
            tts = TextToSpeech(this, this)
            Log.d(TAG, "TTS object created with activity context, waiting for onInit callback")
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in testTtsWithActivityContext: ${e.message}", e)
            statusText.text = "❌ Activity Context Error: ${e.message}"
        }
    }

    /**
     * Test 3: Actual speech output
     */
    private fun testSpeak(statusText: TextView) {
        try {
            if (tts != null && tts!!.isSpeaking) {
                Log.d(TAG, "TTS is already speaking")
                statusText.text = "TTS already speaking"
                return
            }
            
            if (tts == null) {
                Log.w(TAG, "TTS not initialized, initializing now")
                statusText.text = "Initializing TTS..."
                tts = TextToSpeech(applicationContext, this)
                return
            }
            
            Log.d(TAG, "Attempting to speak test message")
            statusText.text = "Speaking..."
            val result = tts?.speak("Hello, TTS test working!", TextToSpeech.QUEUE_FLUSH, null)
            Log.d(TAG, "Speak result: $result")
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in testSpeak: ${e.message}", e)
            statusText.text = "❌ Speak Error: ${e.message}"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "onInit callback: TTS initialized successfully")
            tts?.let {
                it.language = android.icu.util.ULocale.getDefault().toLocale()
                Log.d(TAG, "TTS language set to: ${it.language}")
            }
        } else {
            Log.e(TAG, "onInit callback: TTS initialization failed with status=$status")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Shutting down TTS")
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
