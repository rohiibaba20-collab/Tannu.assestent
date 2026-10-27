package com.tanno.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService

class VoiceListenerService : Service(), RecognitionListener {

    private var speechService: SpeechService? = null
    private val channelId = "tanno_listening"

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        loadModelAndStart()
    }

    private fun loadModelAndStart() {
        StorageService.unpack(
            this, "model-ur", "model",
            { model: Model -> startListening(model) },
            { exception -> CommandRouter.speak(this, "Model load nahi ho saka: ${exception.message}") }
        )
    }

    private fun startListening(model: Model) {
        val recognizer = Recognizer(model, 16000.0f)
        speechService = SpeechService(recognizer, 16000.0f)
        speechService?.startListening(this)
    }

    override fun onResult(hypothesis: String?) {
        val text = hypothesis ?: return
        CommandRouter.handleRecognizedText(this, text)
    }

    override fun onPartialResult(hypothesis: String?) {}
    override fun onFinalResult(hypothesis: String?) {}
    override fun onError(exception: Exception?) {}
    override fun onTimeout() {}

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Tanno sun raha hai", NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tanno")
            .setContentText("Awaaz sun rahi hai...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    override fun onDestroy() {
        speechService?.stop()
        speechService?.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
