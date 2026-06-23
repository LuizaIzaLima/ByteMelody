package com.codex.ninhosonoro

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Process
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class SleepSoundKind {
    WOMB,
    RAIN,
    WHITE_NOISE,
    WIND,
    LULLABY,
    HEARTBEAT
}

class SleepAudioEngine {
    private val sampleRate = 44_100
    private val running = AtomicBoolean(false)
    private var playThread: Thread? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var volume = 0.58f

    fun play(kind: SleepSoundKind, newVolume: Float) {
        stop()
        volume = newVolume.coerceIn(0f, 1f)
        running.set(true)

        playThread = Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            val minBufferBytes = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSamples = maxOf(minBufferBytes / 2, 2_048)
            val buffer = ShortArray(bufferSamples)
            val random = Random(System.nanoTime())
            var frame = 0L

            @Suppress("DEPRECATION")
            val track = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSamples * 2,
                AudioTrack.MODE_STREAM
            )

            audioTrack = track
            track.play()

            try {
                while (running.get()) {
                    for (index in buffer.indices) {
                        val t = frame.toDouble() / sampleRate.toDouble()
                        val sample = sampleFor(kind, t, random) * volume
                        buffer[index] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                        frame++
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } finally {
                runCatching { track.pause() }
                runCatching { track.flush() }
                runCatching { track.release() }
            }
        }.apply {
            name = "NinhoSonoroAudio"
            isDaemon = true
            start()
        }
    }

    fun setVolume(newVolume: Float) {
        volume = newVolume.coerceIn(0f, 1f)
    }

    fun stop() {
        running.set(false)
        runCatching { audioTrack?.pause() }
        runCatching { audioTrack?.flush() }
        playThread?.join(180)
        audioTrack = null
        playThread = null
    }

    fun release() {
        stop()
    }

    private fun sampleFor(kind: SleepSoundKind, t: Double, random: Random): Double {
        return when (kind) {
            SleepSoundKind.WOMB -> womb(t, random)
            SleepSoundKind.RAIN -> rain(t, random)
            SleepSoundKind.WHITE_NOISE -> random.nextDouble(-0.36, 0.36)
            SleepSoundKind.WIND -> wind(t, random)
            SleepSoundKind.LULLABY -> lullaby(t)
            SleepSoundKind.HEARTBEAT -> heartbeat(t)
        }
    }

    private fun womb(t: Double, random: Random): Double {
        val lowPulse = sin(TWO_PI * 56.0 * t) * 0.16
        val softAir = random.nextDouble(-0.06, 0.06)
        val movement = sin(TWO_PI * 0.21 * t) * 0.08
        return lowPulse + softAir + movement
    }

    private fun rain(t: Double, random: Random): Double {
        val drops = random.nextDouble(-0.28, 0.28)
        val softRoll = sin(TWO_PI * 5.0 * t) * 0.035
        val distant = random.nextDouble(-0.08, 0.08) * (0.65 + 0.35 * sin(TWO_PI * 0.09 * t))
        return drops + distant + softRoll
    }

    private fun wind(t: Double, random: Random): Double {
        val breath = 0.45 + 0.55 * sin(TWO_PI * 0.065 * t)
        val shimmer = random.nextDouble(-0.20, 0.20)
        val low = sin(TWO_PI * 93.0 * t) * 0.035
        return shimmer * breath + low
    }

    private fun lullaby(t: Double): Double {
        val notes = doubleArrayOf(261.63, 329.63, 392.0, 523.25, 392.0, 329.63, 293.66, 261.63)
        val beatLength = 0.64
        val noteIndex = ((t / beatLength).toInt() % notes.size).coerceIn(0, notes.lastIndex)
        val noteTime = t % beatLength
        val envelope = exp(-noteTime * 2.1) * (1.0 - 0.25 * abs(sin(TWO_PI * noteTime)))
        val base = sin(TWO_PI * notes[noteIndex] * t)
        val overtone = sin(TWO_PI * notes[noteIndex] * 2.0 * t) * 0.23
        return (base + overtone) * envelope * 0.18
    }

    private fun heartbeat(t: Double): Double {
        val beat = t % 1.15
        val first = exp(-beat * 26.0) * sin(TWO_PI * 74.0 * t)
        val secondDelay = (beat - 0.19).coerceAtLeast(0.0)
        val second = if (beat > 0.19) exp(-secondDelay * 24.0) * sin(TWO_PI * 63.0 * t) else 0.0
        val roomTone = sin(TWO_PI * 38.0 * t) * 0.025
        return first * 0.35 + second * 0.24 + roomTone
    }

    private companion object {
        const val TWO_PI = PI * 2.0
    }
}
