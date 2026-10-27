package com.tanno.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val micPermissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val enableAccessibilityBtn = findViewById<Button>(R.id.enableAccessibilityBtn)
        val enableNotificationBtn = findViewById<Button>(R.id.enableNotificationBtn)
        val startListeningBtn = findViewById<Button>(R.id.startListeningBtn)

        requestMicPermissionIfNeeded()

        enableAccessibilityBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        enableNotificationBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        startListeningBtn.setOnClickListener {
            statusText.text = "👂 Sun raha hoon..."
            startService(Intent(this, VoiceListenerService::class.java))
        }

        val enableOverlayBtn = findViewById<Button>(R.id.enableOverlayBtn)
        enableOverlayBtn.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, OverlayService::class.java))
                statusText.text = "✨ Tanno ab har screen par nazar aayega"
            } else {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }
    }

    private fun requestMicPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), micPermissionCode
            )
        }
    }
}
