package com.tapari.tycoon

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.opengl.GLES30
import android.opengl.GLUtils
import androidx.core.content.res.ResourcesCompat

/** Procedurally drawn texture atlas: glyph icons (Material / game-icons), Poppins digits, speech bubbles and pop-up labels. */
object Atlas {
    const val W = 2048
    const val H = 1024

    const val SOFT = 0
    const val RING = 1
    const val STAR = 2
    const val COIN = 3
    const val GEM = 4
    const val I_MAIDA = 5
    const val I_MEAT = 6
    const val I_JHOL = 7
    const val I_PLATE = 8
    const val I_CUP = 9
    const val I_TABLE = 10
    const val I_BENCH = 11
    const val I_KETTLE = 12
    const val I_CAULDRON = 13
    const val I_MOKTU = 14
    const val I_PREP = 15
    const val I_EXCL = 16
    const val I_ARROW = 17
    const val I_BUILD = 18
    const val I_BAR = 19
    const val I_GOLD = 20
    const val G0 = 21
    const val NG = 17
    const val B0 = G0 + NG
    const val NB = 16
    const val L0 = B0 + NB
    const val NL = 10
    const val N = L0 + NL

    val u0 = FloatArray(N)
    val v0 = FloatArray(N)
    val u1 = FloatArray(N)
    val v1 = FloatArray(N)

    /** Glyph advance in multiples of glyph height (digits 0-9, K M B T Q . x). */
    val ADV = floatArrayOf(0.58f, 0.46f, 0.56f, 0.56f, 0.58f, 0.56f, 0.57f, 0.52f, 0.58f, 0.57f, 0.6f, 0.78f, 0.6f, 0.52f, 0.62f, 0.3f, 0.5f)
    const val GLYPH_ASPECT = 64f / 96f

    /** Pop-up label aspect (width / height). */
    const val LABEL_ASPECT = 4f

    private val GLYPH_TEXT = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "K", "M", "B", "T", "Q", ".", "x")
    private val LABEL_TEXT = arrayOf("COMBO", "PERFECT!", "GOLDEN!", "TIP!", "+1 GEM", "LEVEL UP!", "SPEED!", "UNLOCKED!", "LUCKY!", "FRENZY!")

    private var face: Typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
    private var faceBold: Typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)

    var tex = 0

    fun init(ctx: Context) {
        try {
            val f = ResourcesCompat.getFont(ctx, R.font.poppins_extrabold)
            if (f != null) face = f
            val b = ResourcesCompat.getFont(ctx, R.font.poppins_bold)
            if (b != null) faceBold = b
        } catch (e: Throwable) {
            // keep the system fonts
        }
    }

    private fun sizeW(id: Int): Int = if (id >= L0) 256 else if (id >= G0 && id < B0) 64 else 128
    private fun sizeH(id: Int): Int = if (id >= L0) 64 else if (id >= G0 && id < B0) 96 else 128

    fun build() {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val pad = 4
        var cx = pad
        var cy = pad
        var rowH = 0
        for (id in 0 until N) {
            val w = sizeW(id)
            val h = sizeH(id)
            if (cx + w + pad > W) {
                cx = pad
                cy += rowH + pad
                rowH = 0
            }
            if (cy + h + pad > H) {
                u0[id] = u0[0]; v0[id] = v0[0]; u1[id] = u1[0]; v1[id] = v1[0]
                continue
            }
            u0[id] = cx.toFloat() / W
            v0[id] = cy.toFloat() / H
            u1[id] = (cx + w).toFloat() / W
            v1[id] = (cy + h).toFloat() / H
            c.save()
            c.translate(cx.toFloat(), cy.toFloat())
            c.clipRect(0f, 0f, w.toFloat(), h.toFloat())
            draw(c, id, w, h)
            c.restore()
            cx += w + pad
            if (h > rowH) rowH = h
        }
        val ids = IntArray(1)
        GLES30.glGenTextures(1, ids, 0)
        tex = ids[0]
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, tex)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        // sharper sprites at the oblique camera angle
        try {
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, 0x84FE, 4f)
        } catch (e: Throwable) {
        }
        bmp.recycle()
    }

    private fun paint(color: Int, style: Paint.Style = Paint.Style.FILL, stroke: Float = 0f): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        p.style = style
        p.strokeWidth = stroke
        return p
    }

    private fun draw(c: Canvas, id: Int, w: Int, h: Int) {
        when {
            id < G0 -> drawIcon(c, id)
            id < B0 -> drawGlyph(c, id - G0, w, h)
            id < L0 -> drawBubble(c, id - B0)
            else -> drawLabel(c, id - L0, w, h)
        }
    }

    private fun fitText(tp: Paint, txt: String, maxW: Float, startSize: Float) {
        tp.textSize = startSize
        val m = tp.measureText(txt)
        if (m > maxW) tp.textSize = startSize * maxW / m
    }

    private fun drawGlyph(c: Canvas, g: Int, w: Int, h: Int) {
        val tp = Paint(Paint.ANTI_ALIAS_FLAG)
        tp.typeface = face
        tp.textAlign = Paint.Align.CENTER
        val txt = GLYPH_TEXT[g]
        fitText(tp, txt, w - 14f, 76f)
        val x = w / 2f
        val y = h / 2f + tp.textSize * 0.36f
        tp.style = Paint.Style.STROKE
        tp.strokeWidth = 11f
        tp.strokeJoin = Paint.Join.ROUND
        tp.color = Color.argb(255, 28, 18, 26)
        c.drawText(txt, x, y, tp)
        tp.style = Paint.Style.FILL
        tp.color = Color.WHITE
        c.drawText(txt, x, y, tp)
    }

    private fun drawLabel(c: Canvas, l: Int, w: Int, h: Int) {
        val tp = Paint(Paint.ANTI_ALIAS_FLAG)
        tp.typeface = face
        tp.textAlign = Paint.Align.CENTER
        val txt = LABEL_TEXT[l]
        fitText(tp, txt, w - 24f, 50f)
        val x = w / 2f
        val y = h / 2f + tp.textSize * 0.36f
        tp.style = Paint.Style.STROKE
        tp.strokeWidth = 9f
        tp.strokeJoin = Paint.Join.ROUND
        tp.color = Color.argb(255, 28, 18, 26)
        c.drawText(txt, x, y, tp)
        tp.style = Paint.Style.FILL
        tp.color = Color.WHITE
        c.drawText(txt, x, y, tp)
    }

    /** Draws a glyph centred at (cx, cy) with the given pixel size: dark outline first, then the fill. */
    private fun glyph(c: Canvas, key: String, cx: Float, cy: Float, size: Float, fill: Int, outline: Int, outlineW: Float) {
        val p = GlyphLib.path(key) ?: return
        val vb = GlyphLib.vb(key)
        val k = size / vb
        c.save()
        c.translate(cx - size / 2f, cy - size / 2f)
        c.scale(k, k)
        if (outlineW > 0f) {
            val sp = Paint(Paint.ANTI_ALIAS_FLAG)
            sp.style = Paint.Style.STROKE
            sp.strokeJoin = Paint.Join.ROUND
            sp.strokeCap = Paint.Cap.ROUND
            sp.strokeWidth = outlineW / k
            sp.color = outline
            c.drawPath(p, sp)
        }
        val fp = Paint(Paint.ANTI_ALIAS_FLAG)
        fp.style = Paint.Style.FILL
        fp.color = fill
        c.drawPath(p, fp)
        c.restore()
    }

    private val INK = 0xFF2A1A20.toInt()

    private fun sticker(c: Canvas, key: String, size: Float = 100f) {
        glyph(c, key, 64f, 64f, size, Color.WHITE, INK, 9f)
    }

    private fun drawIcon(c: Canvas, id: Int) {
        val cx = 64f
        val cy = 64f
        when (id) {
            SOFT -> {
                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                p.shader = RadialGradient(cx, cy, 62f, intArrayOf(0xFFFFFFFF.toInt(), 0xCCFFFFFF.toInt(), 0x00FFFFFF), floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP)
                c.drawCircle(cx, cy, 62f, p)
            }
            RING -> {
                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                p.shader = RadialGradient(cx, cy, 62f,
                    intArrayOf(0x00FFFFFF, 0x22FFFFFF, 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0x00FFFFFF),
                    floatArrayOf(0f, 0.6f, 0.82f, 0.9f, 1f), Shader.TileMode.CLAMP)
                c.drawCircle(cx, cy, 62f, p)
            }
            STAR -> {
                val path = Path()
                path.moveTo(cx, 4f)
                path.quadTo(cx + 6f, cy - 6f, 124f, cy)
                path.quadTo(cx + 6f, cy + 6f, cx, 124f)
                path.quadTo(cx - 6f, cy + 6f, 4f, cy)
                path.quadTo(cx - 6f, cy - 6f, cx, 4f)
                c.drawPath(path, paint(Color.WHITE))
                c.drawCircle(cx, cy, 12f, paint(Color.WHITE))
            }
            COIN -> {
                c.drawCircle(cx, cy, 58f, paint(0xFFB8860B.toInt()))
                val g = Paint(Paint.ANTI_ALIAS_FLAG)
                g.shader = android.graphics.LinearGradient(0f, 10f, 0f, 118f, 0xFFFFE27A.toInt(), 0xFFFFA800.toInt(), Shader.TileMode.CLAMP)
                c.drawCircle(cx, cy, 50f, g)
                c.drawCircle(cx, cy, 40f, paint(0xFFFFF0B8.toInt(), Paint.Style.STROKE, 4f))
                val tp = Paint(Paint.ANTI_ALIAS_FLAG)
                tp.typeface = face
                tp.textAlign = Paint.Align.CENTER
                tp.textSize = 44f
                tp.color = 0xFF8A5A00.toInt()
                c.drawText("Rs", cx, cy + 15f, tp)
            }
            GEM -> glyph(c, "m_diamond", cx, cy, 112f, 0xFF5FD4FF.toInt(), 0xFF0E4E68.toInt(), 8f)
            I_MAIDA -> sticker(c, "g_flour")
            I_MEAT -> sticker(c, "g_meat")
            I_JHOL -> sticker(c, "g_mason_jar")
            I_PLATE -> sticker(c, "g_dumpling")
            I_CUP -> sticker(c, "g_coffee_cup")
            I_TABLE -> sticker(c, "g_round_table")
            I_BENCH -> sticker(c, "g_park_bench")
            I_KETTLE -> sticker(c, "g_teapot")
            I_CAULDRON -> sticker(c, "g_cauldron")
            I_MOKTU -> sticker(c, "g_rice_cooker")
            I_PREP -> sticker(c, "g_manual_meat_grinder")
            I_EXCL -> {
                c.drawCircle(cx, cy, 58f, paint(0xFFE53935.toInt()))
                c.drawCircle(cx, cy, 58f, paint(Color.WHITE, Paint.Style.STROKE, 7f))
                glyph(c, "m_priority_high", cx, cy, 86f, Color.WHITE, 0x00000000, 0f)
            }
            I_BUILD -> sticker(c, "g_shop", 106f)
            I_ARROW -> {
                val path = Path()
                path.moveTo(cx, 120f); path.lineTo(112f, 56f); path.lineTo(80f, 56f); path.lineTo(80f, 8f)
                path.lineTo(48f, 8f); path.lineTo(48f, 56f); path.lineTo(16f, 56f); path.close()
                c.drawPath(path, paint(Color.WHITE))
            }
            I_BAR -> c.drawRect(8f, 8f, 120f, 120f, paint(Color.WHITE))
            I_GOLD -> glyph(c, "g_dumpling", cx, cy, 108f, 0xFFFFD54A.toInt(), 0xFF7A5200.toInt(), 8f)
        }
    }

    private fun fillStroke(c: Canvas, path: Path, fill: Int, stroke: Int, sw: Float) {
        c.drawPath(path, paint(fill))
        val sp = paint(stroke, Paint.Style.STROKE, sw)
        sp.strokeJoin = Paint.Join.ROUND
        sp.strokeCap = Paint.Cap.ROUND
        c.drawPath(path, sp)
    }

    // bubble table: background, glyph key, glyph fill colour
    private val BG = intArrayOf(
        0xFFE53935.toInt(), 0xFFFFF6E4.toInt(), 0xFFFFF6E4.toInt(), 0xFFFFF3C4.toInt(), 0xFF7F1010.toInt(),
        0xFFFFE066.toInt(), 0xFFFFFFFF.toInt(), 0xFFE3F2FD.toInt(), 0xFF8E5CF7.toInt(), 0xFFFFD6A5.toInt(),
        0xFFD8F5D0.toInt(), 0xFFFF8FC8.toInt(), 0xFFFFD66B.toInt(), 0xFF8FE3D8.toInt(), 0xFFFFE9B0.toInt(), 0xFFE53935.toInt()
    )
    private val KEY = arrayOf(
        "g_chili_pepper", "g_dumpling", "g_coffee_cup", "m_favorite", "m_sentiment_very_dissatisfied",
        "m_lightbulb", "m_how_to_vote", "m_wifi", "m_currency_bitcoin", "g_cigarette",
        "m_sentiment_very_satisfied", "m_videocam", "g_full_motorcycle_helmet", "m_photo_camera", "m_self_improvement", "m_priority_high"
    )
    private val FG = intArrayOf(
        0xFFFFFFFF.toInt(), 0xFFE0912B.toInt(), 0xFF8D5524.toInt(), 0xFFFF3B6B.toInt(), 0xFFFFB74D.toInt(),
        0xFFF2A900.toInt(), 0xFFD3293F.toInt(), 0xFF1E88E5.toInt(), 0xFFFFFFFF.toInt(), 0xFF8A5A32.toInt(),
        0xFF2E9E4F.toInt(), 0xFFFFFFFF.toInt(), 0xFF2A2A30.toInt(), 0xFF176F66.toInt(), 0xFFE5731A.toInt(), 0xFFFFFFFF.toInt()
    )

    private fun drawBubble(c: Canvas, b: Int) {
        val bg = BG[b]
        val ink = 0xFF2A2230.toInt()
        val cx = 64f
        val cy = 50f
        val tail = Path()
        tail.moveTo(cx - 15f, 86f)
        tail.lineTo(cx, 122f)
        tail.lineTo(cx + 15f, 86f)
        tail.close()
        val stroke = paint(ink, Paint.Style.STROKE, 6f)
        stroke.strokeJoin = Paint.Join.ROUND
        c.drawPath(tail, paint(bg))
        c.drawPath(tail, stroke)
        c.drawCircle(cx, cy, 46f, paint(bg))
        c.drawCircle(cx, cy, 46f, stroke)
        c.drawRect(cx - 13f, 80f, cx + 13f, 92f, paint(bg))
        // soft inner highlight
        val hl = Paint(Paint.ANTI_ALIAS_FLAG)
        hl.shader = RadialGradient(cx - 10f, cy - 14f, 40f, intArrayOf(0x55FFFFFF, 0x00FFFFFF), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, 40f, hl)
        val dark = b == Bub.ANGRY
        glyph(c, KEY[b], cx, cy, 56f, FG[b], if (dark) 0xFF3A0A0A.toInt() else 0xFF2A1A20.toInt(), 4.5f)
    }
}
