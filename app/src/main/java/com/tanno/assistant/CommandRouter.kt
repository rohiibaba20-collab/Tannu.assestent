package com.tanno.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

object CommandRouter {

    private var tts: TextToSpeech? = null

    fun handleRecognizedText(context: Context, text: String) {
        val lower = text.lowercase(Locale.getDefault()).trim()
        if (lower.isEmpty()) return

        when {
            lower.contains("khol") && lower.contains("camera") ->
                openApp(context, "com.android.camera")

            lower.contains("khol") && lower.contains("whatsapp") ->
                openApp(context, "com.whatsapp")

            lower.contains("ghar") || lower.contains("home") ->
                TannoAccessibilityService.instance?.goHome()

            lower.contains("wapas") || lower.contains("back") ->
                TannoAccessibilityService.instance?.goBack()

            lower.contains("call kar") ->
                speak(context, "Kis ko call karna hai, naam boliye.")

            (lower.contains("message") || lower.contains("massage")) &&
                (lower.contains("kiska") || lower.contains("kaun") || lower.contains("naya")) ->
                replyWithLastNotification(context)

            lower.contains("kisne banaya") || lower.contains("tumhara naam") || lower.contains("tumhe kisne") ->
                speak(context, AppInfo.creatorIntroLine())

            else -> askBrainOrFallback(context, text)
        }
    }

    private fun replyWithLastNotification(context: Context) {
        val app = TannoNotificationListener.lastApp
        val sender = TannoNotificationListener.lastSender
        val message = TannoNotificationListener.lastMessage

        if (app == null) {
            speak(context, "Abhi tak koi naya message nahi aaya, ya notification access on nahi hai.")
        } else {
            speak(context, "$app par $sender ka message tha: $message")
        }
    }

    private fun askBrainOrFallback(context: Context, originalText: String) {
        if (LlmBrain.isReady()) {
            val reply = LlmBrain.ask(originalText)
            speak(context, reply ?: "Mujhe jawab sochne mein mushkil ho rahi hai.")
        } else {
            speak(context, "Mujhe samajh nahi aaya, dobara boliye.")
        }
    }

    private fun openApp(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            speak(context, "Yeh app phone mein nahi mili.")
        }
    }

    fun availableVoices(context: Context): List<String> {
        if (tts == null) speak(context, "")
        return tts?.voices?.map { it.name } ?: emptyList()
    }

    fun setVoiceByName(voiceName: String) {
        val voice = tts?.voices?.firstOrNull { it.name == voiceName } ?: return
        tts?.voice = voice
    }

    fun setVoiceTone(pitch: Float, rate: Float) {
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)
    }

    fun speak(context: Context, message: String) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("ur", "PK")
                    attachSpeakingListener()
                    tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "tanno_utterance")
                }
            }
        } else {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "tanno_utterance")
        }
    }

    private fun attachSpeakingListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                SpeakingState.set(true)
            }
            override fun onDone(utteranceId: String?) {
                SpeakingState.set(false)
            }
            override fun onError(utteranceId: String?) {
                SpeakingState.set(false)
            }
        })
    }
}
