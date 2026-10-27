package com.tanno.assistant

object SpeakingState {
    @Volatile var isSpeaking: Boolean = false
    private val listeners = mutableListOf<(Boolean) -> Unit>()

    fun set(speaking: Boolean) {
        isSpeaking = speaking
        listeners.forEach { it(speaking) }
    }

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }
}
