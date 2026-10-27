package com.tanno.assistant

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin
import kotlin.random.Random

class TannoCharacterView(context: Context) : View(context) {

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CDCBF6")
        style = Paint.Style.FILL
    }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4B2FA3")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#26215C")
        style = Paint.Style.FILL
    }
    private val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#26215C")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }
    private val cheekPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F0997B")
        alpha = 110
    }

    private var bounceOffset = 0f
    private var eyeScaleY = 1f
    private var mouthOpenAmount = 0f
    private var blinkRunnableTime = 0L

    private val speakingListener: (Boolean) -> Unit = { animateMouth() }

    init {
        startBounceAnimation()
        startBlinkLoop()
        SpeakingState.addListener(speakingListener)
    }

    private fun startBounceAnimation() {
        val animator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat())
        animator.duration = 2200
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {
            val t = it.animatedValue as Float
            bounceOffset = sin(t) * 10f
            invalidate()
        }
        animator.start()
    }

    private fun startBlinkLoop() {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 16000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener {
            val elapsed = it.currentPlayTime % 4000
            eyeScaleY = if (elapsed in 150..280) 0.1f else 1f
            invalidate()
        }
        animator.start()
    }

    private fun animateMouth() {
        if (!SpeakingState.isSpeaking) {
            mouthOpenAmount = 0f
            invalidate()
            return
        }
        val animator = ValueAnimator.ofFloat(0.2f, 1f)
        animator.duration = 140
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener {
            if (!SpeakingState.isSpeaking) {
                animator.cancel()
                mouthOpenAmount = 0f
            } else {
                mouthOpenAmount = (it.animatedValue as Float) * (0.8f + Random.nextFloat() * 0.2f)
            }
            invalidate()
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f + bounceOffset
        val radius = width.coerceAtMost(height) / 2.2f

        canvas.drawCircle(cx, cy, radius, bodyPaint)
        canvas.drawCircle(cx, cy, radius, outlinePaint)

        canvas.drawCircle(cx - radius * 0.55f, cy + radius * 0.15f, radius * 0.18f, cheekPaint)
        canvas.drawCircle(cx + radius * 0.55f, cy + radius * 0.15f, radius * 0.18f, cheekPaint)

        val eyeGap = radius * 0.4f
        val eyeY = cy - radius * 0.15f
        val eyeR = radius * 0.14f
        canvas.save()
        canvas.scale(1f, eyeScaleY, cx - eyeGap, eyeY)
        canvas.drawCircle(cx - eyeGap, eyeY, eyeR, eyePaint)
        canvas.restore()
        canvas.save()
        canvas.scale(1f, eyeScaleY, cx + eyeGap, eyeY)
        canvas.drawCircle(cx + eyeGap, eyeY, eyeR, eyePaint)
        canvas.restore()

        if (mouthOpenAmount < 0.05f) {
            val mouthRect = RectF(
                cx - radius * 0.25f, cy + radius * 0.25f,
                cx + radius * 0.25f, cy + radius * 0.4f
            )
            canvas.drawArc(mouthRect, 10f, 160f, false, mouthPaint)
        } else {
            val openHeight = radius * 0.3f * mouthOpenAmount
            val mouthRect = RectF(
                cx - radius * 0.22f, cy + radius * 0.25f,
                cx + radius * 0.22f, cy + radius * 0.25f + openHeight
            )
            val fillPaint = Paint(mouthPaint).apply { style = Paint.Style.FILL; color = Color.parseColor("#4B2FA3") }
            canvas.drawOval(mouthRect, fillPaint)
        }
    }

    fun detach() {
        SpeakingState.removeListener(speakingListener)
    }
}
