package com.tanno.assistant

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions

object LlmBrain {

    private const val MODEL_PATH = "/data/local/tmp/llm/gemma-2b-it-cpu-int4.task"

    private var llmInference: LlmInference? = null

    fun init(context: Context) {
        if (llmInference != null) return
        val options = LlmInferenceOptions.builder()
            .setModelPath(MODEL_PATH)
            .setMaxTokens(512)
            .build()
        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun ask(prompt: String): String? {
        val model = llmInference ?: return null
        return model.generateResponse(prompt)
    }

    fun isReady(): Boolean = llmInference != null
}
