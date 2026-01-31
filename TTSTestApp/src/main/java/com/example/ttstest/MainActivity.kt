package com.example.ttstest

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val TAG = "TTSTest"
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity created - onCreate() called")

        val btn1 = findViewById<Button>(R.id.btn_tts_app_context)
        val btn2 = findViewById<Button>(R.id.btn_tts_service_context)
        val btn3 = findViewById<Button>(R.id.btn_test_speak)
        statusText = findViewById(R.id.status_text)

        btn1.setOnClickListener {
            Log.d(TAG, "Button 1 clicked: Testing TextToSpeech with app context")
            testTtsWithAppContext()
        }

        btn2.setOnClickListener {
            Log.d(TAG, "Button 2 clicked: Testing TextToSpeech with activity context")
            testTtsWithActivityContext()
        }

        btn3.setOnClickListener {
            Log.d(TAG, "Button 3 clicked: Triggering test speech")
            // This button is for testing an already initialized TTS engine
            speak("Hello, this is a direct test.")
        }

        Log.d(TAG, "MainActivity UI initialized successfully")
    }

    /**
     * Test 1: Using Application Context with a Lambda for the callback.
     * This is a modern and concise approach.
     */
    private fun testTtsWithAppContext() {
        statusText.text = "Testing: App Context TTS Init..."
        // Ensure any previous instance is shut down
        tts?.shutdown()
        
        tts = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TTS initialized successfully with app context")
                statusText.text = "✅ App Context: TTS Ready"
                tts?.language = Locale.getDefault()
                speak("TTS works with app context")
            } else {
                Log.e(TAG, "TTS initialization failed with app context, status=$status")
                statusText.text = "❌ App Context: TTS Failed (status=$status)"
            }
        }
    }

    /**
     * Test 2: Using Activity Context. The result is handled in the overridden onInit.
     */
    private fun testTtsWithActivityContext() {
        statusText.text = "Testing: Activity Context TTS Init..."
        // Ensure any previous instance is shut down
        tts?.shutdown()
        // The 'this' refers to MainActivity, which implements OnInitListener
        tts = TextToSpeech(this, this)
    }

    /**
     * The callback for when TTS is initialized.
     * This method will be called for the Activity context initialization.
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "onInit callback: TTS initialized successfully")
            statusText.text = "✅ Activity Context: TTS Ready"
            tts?.let {
                it.language = Locale.getDefault()
                Log.d(TAG, "TTS language set to: ${it.language}")
                // Speak right after initialization
                speak("TTS works with activity context")
            }
        } else {
            Log.e(TAG, "onInit callback: TTS initialization failed with status=$status")
            statusText.text = "❌ Activity Context: TTS Failed (status=$status)"
        }
    }

    /**
     * A unified function to handle speaking.
     */
    private fun speak(text: String) {
        if (tts == null) {
            statusText.text = "Error: TTS is not initialized."
            Log.e(TAG, "speak() called but TTS is null.")
            return
        }
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Error while trying to speak.")
            statusText.text = "Error speaking."
        } else {
            Log.d(TAG, "Successfully queued speech.")
        }
    }

    override fun onDestroy() {
        // Shutdown TTS when the activity is destroyed
        Log.d(TAG, "onDestroy: Shutting down TTS")
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
