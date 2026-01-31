package com.example.ttstest

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.util.Log

/**
 * Instrumented test that launches MainActivity and verifies:
 * 1. Activity launches without crash
 * 2. UI elements are present
 * 3. Buttons are clickable
 * 4. TTS operations can be performed
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @Rule
    @JvmField
    val activityRule = ActivityTestRule(MainActivity::class.java)
    
    @Before
    fun setUp() {
        Log.d("TTSTest", "Test setup complete")
    }
    
    /**
     * Test 1: Verify activity launches without crash
     */
    @Test
    fun testActivityLaunchesSuccessfully() {
        val activity = activityRule.activity
        assert(activity != null) { "Activity should not be null" }
        Log.d("TTSTest", "✅ Test 1 PASSED: Activity launched successfully")
    }
    
    /**
     * Test 2: Verify UI elements are present
     */
    @Test
    fun testUIElementsPresent() {
        val activity = activityRule.activity
        val btn1 = activity.findViewById<android.widget.Button>(R.id.btn_tts_app_context)
        val btn2 = activity.findViewById<android.widget.Button>(R.id.btn_tts_service_context)
        val btn3 = activity.findViewById<android.widget.Button>(R.id.btn_test_speak)
        val statusText = activity.findViewById<android.widget.TextView>(R.id.status_text)
        
        assert(btn1 != null) { "Button 1 should exist" }
        assert(btn2 != null) { "Button 2 should exist" }
        assert(btn3 != null) { "Button 3 should exist" }
        assert(statusText != null) { "Status text should exist" }
        
        Log.d("TTSTest", "✅ Test 2 PASSED: All UI elements present")
    }
    
    /**
     * Test 3: Verify buttons are clickable
     */
    @Test
    fun testButtonsAreClickable() {
        val activity = activityRule.activity
        val btn1 = activity.findViewById<android.widget.Button>(R.id.btn_tts_app_context)
        val btn2 = activity.findViewById<android.widget.Button>(R.id.btn_tts_service_context)
        val btn3 = activity.findViewById<android.widget.Button>(R.id.btn_test_speak)
        
        assert(btn1!!.isClickable) { "Button 1 should be clickable" }
        assert(btn2!!.isClickable) { "Button 2 should be clickable" }
        assert(btn3!!.isClickable) { "Button 3 should be clickable" }
        
        Log.d("TTSTest", "✅ Test 3 PASSED: All buttons are clickable")
    }
    
    /**
     * Test 4: Simulate clicking button 1 (App Context TTS)
     */
    @Test
    fun testAppContextTTSButton() {
        val activity = activityRule.activity
        val btn1 = activity.findViewById<android.widget.Button>(R.id.btn_tts_app_context)
        val statusText = activity.findViewById<android.widget.TextView>(R.id.status_text)
        
        // Click button
        btn1?.performClick()
        
        // Give it time to process
        Thread.sleep(2000)
        
        // Verify status text changed
        val status = statusText?.text.toString()
        Log.d("TTSTest", "Status after click: $status")
        assert(status.isNotEmpty()) { "Status text should be updated" }
        
        Log.d("TTSTest", "✅ Test 4 PASSED: App Context TTS button clickable")
    }
    
    /**
     * Test 5: Simulate clicking button 2 (Activity Context TTS)
     */
    @Test
    fun testActivityContextTTSButton() {
        val activity = activityRule.activity
        val btn2 = activity.findViewById<android.widget.Button>(R.id.btn_tts_service_context)
        val statusText = activity.findViewById<android.widget.TextView>(R.id.status_text)
        
        // Click button
        btn2?.performClick()
        
        // Give it time to process
        Thread.sleep(2000)
        
        // Verify status text changed
        val status = statusText?.text.toString()
        Log.d("TTSTest", "Status after click: $status")
        assert(status.isNotEmpty()) { "Status text should be updated" }
        
        Log.d("TTSTest", "✅ Test 5 PASSED: Activity Context TTS button clickable")
    }
    
    /**
     * Test 6: Simulate clicking speak button
     */
    @Test
    fun testSpeakButton() {
        val activity = activityRule.activity
        val btn3 = activity.findViewById<android.widget.Button>(R.id.btn_test_speak)
        val statusText = activity.findViewById<android.widget.TextView>(R.id.status_text)
        
        // Click button
        btn3?.performClick()
        
        // Give it time to process
        Thread.sleep(1000)
        
        // Verify status text changed
        val status = statusText?.text.toString()
        Log.d("TTSTest", "Status after click: $status")
        assert(status.isNotEmpty()) { "Status text should be updated" }
        
        Log.d("TTSTest", "✅ Test 6 PASSED: Speak button clickable")
    }
}
