package com.tapari.tycoon

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Fully synthesised audio: SFX are generated as WAV files at first launch and played via SoundPool,
 * the Kathmandu street ambience (traffic, horns, temple bells, bansuri flute) is a looping AudioTrack.
 */
class AudioFx(private val ctx: Context, private val game: Game) : Fx {
    private val rate = 22050
    private var pool: SoundPool? = null
    private val ids = IntArray(Sfx.COUNT)
    @Volatile private var ready = false
    private var amb: AudioTrack? = null
    private var vib: Vibrator? = null
    private var lastHap = 0L
    private var eTick: VibrationEffect? = null
    private var eThump: VibrationEffect? = null
    private var eBuzz: VibrationEffect? = null
    private var eSuccess: VibrationEffect? = null

    fun init() {
        try {
            vib = if (Build.VERSION.SDK_INT >= 31) {
                ctx.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            eTick = VibrationEffect.createOneShot(10, 70)
            eThump = VibrationEffect.createOneShot(35, 160)
            eBuzz = VibrationEffect.createWaveform(longArrayOf(0, 70, 40, 110), intArrayOf(0, 200, 0, 255), -1)
            eSuccess = VibrationEffect.createWaveform(longArrayOf(0, 25, 50, 45), intArrayOf(0, 140, 0, 230), -1)
        } catch (e: Throwable) {
            vib = null
        }
        val t = Thread {
            try {
                buildSfx()
                buildAmbience()
            } catch (e: Throwable) {
                // audio is optional; never crash the game
            }
        }
        t.priority = Thread.MIN_PRIORITY
        t.start()
    }

    fun setSound(on: Boolean) {
        try {
            val a = amb ?: return
            if (on) a.play() else a.pause()
        } catch (e: Throwable) {
        }
    }

    fun pause() {
        try {
            amb?.pause()
        } catch (e: Throwable) {
        }
    }

    fun resume() {
        try {
            if (game.soundOn) amb?.play()
        } catch (e: Throwable) {
        }
    }

    fun release() {
        try {
            amb?.stop()
            amb?.release()
            pool?.release()
        } catch (e: Throwable) {
        }
        amb = null
        pool = null
    }

    override fun sfx(id: Int, rate: Float, vol: Float) {
        if (!game.soundOn || !ready) return
        val p = pool ?: return
        val v = if (vol > 1f) 1f else vol
        p.play(ids[id], v, v, 1, 0, if (rate < 0.5f) 0.5f else if (rate > 2f) 2f else rate)
    }

    override fun haptic(kind: Int) {
        if (!game.hapticOn) return
        val v = vib ?: return
        val now = SystemClock.uptimeMillis()
        if (kind == Hap.TICK && now - lastHap < 45) return
        lastHap = now
        try {
            val e = when (kind) {
                Hap.TICK -> eTick
                Hap.THUMP -> eThump
                Hap.BUZZ -> eBuzz
                else -> eSuccess
            }
            if (e != null) v.vibrate(e)
        } catch (ex: Throwable) {
        }
    }

    // ------------------------------------------------------------------ synthesis
    private fun noise(seedRef: IntArray): Float {
        var s = seedRef[0]
        s = s xor (s shl 13)
        s = s xor (s ushr 17)
        s = s xor (s shl 5)
        seedRef[0] = s
        return ((s ushr 8) and 0xFFFF) / 32768f - 1f
    }

    private fun tw(f: Double, t: Double): Double = sin(2.0 * PI * f * t)

    private fun toPcm(buf: FloatArray): ShortArray {
        val out = ShortArray(buf.size)
        for (i in buf.indices) {
            var v = buf[i]
            if (v > 1f) v = 1f
            if (v < -1f) v = -1f
            out[i] = (v * 32000f).toInt().toShort()
        }
        return out
    }

    private fun writeWav(name: String, pcm: ShortArray): String {
        val f = File(ctx.cacheDir, "$name.wav")
        val dataLen = pcm.size * 2
        FileOutputStream(f).use { o ->
            fun i32(v: Int) { o.write(v and 255); o.write((v shr 8) and 255); o.write((v shr 16) and 255); o.write((v shr 24) and 255) }
            fun i16(v: Int) { o.write(v and 255); o.write((v shr 8) and 255) }
            o.write("RIFF".toByteArray()); i32(36 + dataLen); o.write("WAVE".toByteArray())
            o.write("fmt ".toByteArray()); i32(16); i16(1); i16(1); i32(rate); i32(rate * 2); i16(2); i16(16)
            o.write("data".toByteArray()); i32(dataLen)
            val bb = ByteArray(dataLen)
            for (i in pcm.indices) {
                bb[i * 2] = (pcm[i].toInt() and 255).toByte()
                bb[i * 2 + 1] = ((pcm[i].toInt() shr 8) and 255).toByte()
            }
            o.write(bb)
        }
        return f.absolutePath
    }

    private fun buildSfx() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sp = SoundPool.Builder().setMaxStreams(12).setAudioAttributes(attrs).build()
        val seed = intArrayOf(0x1234567)
        for (id in 0 until Sfx.COUNT) {
            val dur = when (id) {
                Sfx.POP -> 0.11f
                Sfx.CASH -> 0.45f
                Sfx.UNLOCK -> 0.8f
                Sfx.YELL -> 0.42f
                Sfx.ANGRY -> 0.6f
                Sfx.STEAM -> 0.55f
                Sfx.UPGRADE -> 0.55f
                Sfx.HORN -> 0.5f
                Sfx.POUR -> 0.4f
                Sfx.BELL -> 0.7f
                Sfx.VIP -> 0.7f
                else -> 0.05f
            }
            val n = (dur * rate).toInt()
            val b = FloatArray(n)
            var lp = 0f
            for (i in 0 until n) {
                val t = i.toDouble() / rate
                val d = dur.toDouble()
                var s = 0.0
                when (id) {
                    Sfx.POP -> {
                        val ph = 2.0 * PI * (520.0 * t + (900.0 - 520.0) * t * t / (2.0 * d))
                        s = sin(ph) * exp(-t * 32.0) * 0.8
                    }
                    Sfx.CASH -> {
                        val e1 = exp(-t * 9.0)
                        s = (tw(1318.5, t) * 0.6 + tw(2637.0, t) * 0.25) * e1 * 0.7
                        if (t > 0.07) {
                            val t2 = t - 0.07
                            val e2 = exp(-t2 * 8.0)
                            s += (tw(1975.5, t2) * 0.6 + tw(3951.0, t2) * 0.2) * e2 * 0.7
                        }
                    }
                    Sfx.UNLOCK -> {
                        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.5)
                        for (k in 0 until 4) {
                            val tk = t - k * 0.11
                            if (tk >= 0.0) s += (tw(notes[k], tk) * 0.5 + tw(notes[k] * 2.0, tk) * 0.15) * exp(-tk * 5.0) * 0.6
                        }
                    }
                    Sfx.YELL -> {
                        val f = 430.0 - 170.0 * (t / d) + 14.0 * sin(2.0 * PI * 26.0 * t)
                        val saw = ((f * t) % 1.0) * 2.0 - 1.0
                        val env = Math.min(1.0, t * 60.0) * exp(-Math.max(0.0, t - 0.2) * 9.0)
                        s = (saw * 0.45 + noise(seed) * 0.12) * env * 0.8
                    }
                    Sfx.ANGRY -> {
                        val f = 120.0 - 50.0 * (t / d)
                        val sq = if (((f * t) % 1.0) < 0.5) 1.0 else -1.0
                        val env = Math.min(1.0, t * 40.0) * exp(-t * 3.5)
                        s = (sq * 0.35 + noise(seed) * 0.2) * env * 0.8
                    }
                    Sfx.STEAM -> {
                        val x = noise(seed)
                        lp += 0.35f * (x - lp)
                        val hp = x - lp
                        val env = Math.min(1.0, t * 25.0) * exp(-t * 5.0)
                        s = hp * env * 0.55
                    }
                    Sfx.UPGRADE -> {
                        val ph1 = 2.0 * PI * (440.0 * t + (660.0) * t * t / (2.0 * d))
                        s = (sin(ph1) * 0.5 + tw(1760.0, t) * 0.15 * exp(-t * 6.0)) * Math.min(1.0, t * 50.0) * exp(-t * 4.0)
                    }
                    Sfx.HORN -> {
                        val sq1 = if (((420.0 * t) % 1.0) < 0.5) 1.0 else -1.0
                        val sq2 = if (((530.0 * t) % 1.0) < 0.5) 1.0 else -1.0
                        s = (sq1 + sq2) * 0.18 * Math.min(1.0, t * 80.0) * exp(-Math.max(0.0, t - 0.3) * 20.0)
                    }
                    Sfx.POUR -> {
                        val x = noise(seed)
                        lp += 0.12f * (x - lp)
                        val bub = 0.5 + 0.5 * sin(2.0 * PI * (14.0 + 22.0 * t) * t)
                        s = lp * 2.6 * bub * Math.min(1.0, t * 30.0) * exp(-t * 4.0)
                    }
                    Sfx.BELL -> {
                        s = (tw(1568.0, t) * 0.5 + tw(2351.0, t) * 0.3 + tw(3136.0, t) * 0.15) * exp(-t * 6.0) * 0.8
                    }
                    Sfx.VIP -> {
                        val notes = doubleArrayOf(783.99, 987.77, 1174.66, 1567.98)
                        for (k in 0 until 4) {
                            val tk = t - k * 0.075
                            if (tk >= 0.0) s += tw(notes[k], tk) * exp(-tk * 7.0) * 0.5
                        }
                        s += tw(3135.9, t) * 0.12 * exp(-t * 4.0)
                    }
                    else -> {
                        s = tw(1100.0, t) * exp(-t * 90.0) * 0.6
                    }
                }
                b[i] = s.toFloat()
            }
            val path = writeWav("sfx_$id", toPcm(b))
            ids[id] = sp.load(path, 1)
        }
        pool = sp
        ready = true
    }

    private fun buildAmbience() {
        val secs = 12
        val n = rate * secs
        val b = FloatArray(n)
        val seed = intArrayOf(0x7A3C91B)
        var brown = 0f
        var murmur = 0f
        val flute = doubleArrayOf(440.0, 523.25, 587.33, 523.25, 440.0, 392.0, 349.23, 392.0)
        for (i in 0 until n) {
            val t = i.toDouble() / rate
            // traffic rumble
            val w = noise(seed)
            brown = (brown + 0.02f * w) * 0.985f
            val lfo = 0.75 + 0.25 * sin(2.0 * PI * 0.17 * t)
            var s = brown * 2.2 * lfo
            // crowd murmur
            murmur += 0.08f * (w - murmur)
            s += murmur * (0.05 + 0.03 * sin(2.0 * PI * 3.7 * t))
            // horns
            val hornTimes = doubleArrayOf(2.1, 5.7, 9.4)
            for (h in hornTimes) {
                val th = t - h
                if (th in 0.0..0.55) {
                    val on = if (th < 0.2 || th in 0.28..0.48) 1.0 else 0.0
                    val sq = if (((392.0 * th) % 1.0) < 0.5) 1.0 else -1.0
                    s += sq * 0.025 * on
                }
            }
            // temple bell
            val bellTimes = doubleArrayOf(0.4, 6.6)
            for (bt in bellTimes) {
                val tb = t - bt
                if (tb >= 0.0 && tb < 3.5) s += (tw(880.0, tb) * 0.5 + tw(1760.0, tb) * 0.2 + tw(2640.0, tb) * 0.1) * exp(-tb * 1.6) * 0.07
            }
            // bansuri flute
            val seg = (t / 1.5).toInt().coerceIn(0, 7)
            val tn = t - seg * 1.5
            val f = flute[seg] * (1.0 + 0.006 * sin(2.0 * PI * 5.5 * t))
            val env = Math.min(1.0, tn / 0.14) * Math.min(1.0, (1.5 - tn) / 0.4)
            s += (tw(f, t) * 0.6 + tw(f * 2.0, t) * 0.12 + w * 0.025) * env * 0.075
            // chiya glass clinks
            val ct = (t * 1.9) % 1.0
            if (ct < 0.02 && ((t * 1.9).toInt() % 3 == 0)) s += tw(3200.0, t) * 0.03
            // fade at loop edges to avoid clicks
            val edge = Math.min(1.0, Math.min(t / 0.08, (secs - t) / 0.08))
            b[i] = (s * edge).toFloat()
        }
        val pcm = toPcm(b)
        val bytes = pcm.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(rate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcm, 0, pcm.size)
        track.setLoopPoints(0, pcm.size, -1)
        track.setVolume(0.55f)
        amb = track
        if (game.soundOn) track.play()
    }
}
