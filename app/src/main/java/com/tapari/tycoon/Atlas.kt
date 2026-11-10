package com.tapari.tycoon

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

/** Procedurally drawn texture atlas: icons, digit glyphs and speech bubbles. */
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
    const val G0 = 18
    const val B0 = 34
    const val N = 50

    val u0 = FloatArray(N)
    val v0 = FloatArray(N)
    val u1 = FloatArray(N)
    val v1 = FloatArray(N)

    /** Glyph advance in multiples of glyph height. */
    val ADV = floatArrayOf(0.56f, 0.46f, 0.56f, 0.56f, 0.58f, 0.56f, 0.56f, 0.54f, 0.58f, 0.56f, 0.6f, 0.78f, 0.6f, 0.52f, 0.62f, 0.3f)
    const val GLYPH_ASPECT = 64f / 96f

    private val BUB_TEXT = arrayOf(
        "EXTRA JHOL!", "MOMO DAI!", "EK CHIYA!", "Mitho cha!", "#@%&*!",
        "Startup idea bro...", "Politics ko kura", "WiFi password?", "Web3 is the future",
        "Dai, ek Surya!", "Dhanyabaad!", "Like & Subscribe!", "Vroom vroom!", "Namaste! Spicy?",
        "Namaste!", "!"
    )

    private val GLYPH_TEXT = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "K", "M", "B", "T", "Q", ".")

    var tex = 0

    private fun sizeW(id: Int): Int = if (id < G0) 128 else if (id < B0) 64 else 256
    private fun sizeH(id: Int): Int = if (id < G0) 128 else if (id < B0) 96 else 128

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
                // out of room: alias to the soft cell so nothing crashes
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
            else -> drawBubble(c, id - B0, w, h)
        }
    }

    private fun drawGlyph(c: Canvas, g: Int, w: Int, h: Int) {
        val tp = Paint(Paint.ANTI_ALIAS_FLAG)
        tp.typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
        tp.textSize = 82f
        tp.textAlign = Paint.Align.CENTER
        val txt = GLYPH_TEXT[g]
        val x = w / 2f
        val y = h / 2f + 29f
        tp.style = Paint.Style.STROKE
        tp.strokeWidth = 12f
        tp.strokeJoin = Paint.Join.ROUND
        tp.color = Color.argb(255, 25, 20, 30)
        c.drawText(txt, x, y, tp)
        tp.style = Paint.Style.FILL
        tp.color = Color.WHITE
        c.drawText(txt, x, y, tp)
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
                c.drawCircle(cx, cy, 50f, paint(0xFFFFC93C.toInt()))
                c.drawCircle(cx, cy, 40f, paint(0xFFFFE27A.toInt(), Paint.Style.STROKE, 5f))
                val tp = Paint(Paint.ANTI_ALIAS_FLAG)
                tp.typeface = Typeface.DEFAULT_BOLD
                tp.textAlign = Paint.Align.CENTER
                tp.textSize = 46f
                tp.color = 0xFF8A5A00.toInt()
                c.drawText("Rs", cx, cy + 16f, tp)
            }
            GEM -> {
                val path = Path()
                path.moveTo(cx, 8f); path.lineTo(112f, 44f); path.lineTo(cx, 120f); path.lineTo(16f, 44f); path.close()
                c.drawPath(path, paint(0xFF35C6F4.toInt()))
                val top = Path()
                top.moveTo(cx, 8f); top.lineTo(112f, 44f); top.lineTo(16f, 44f); top.close()
                c.drawPath(top, paint(0xFF8BE6FF.toInt()))
                c.drawPath(path, paint(0xFF136C8E.toInt(), Paint.Style.STROKE, 5f))
                c.drawLine(16f, 44f, 112f, 44f, paint(0xFF136C8E.toInt(), Paint.Style.STROKE, 4f))
            }
            I_MAIDA -> {
                c.drawRoundRect(RectF(26f, 34f, 102f, 116f), 18f, 18f, paint(0xFFF3E7C9.toInt()))
                c.drawRoundRect(RectF(26f, 34f, 102f, 116f), 18f, 18f, paint(0xFF9C7B3F.toInt(), Paint.Style.STROKE, 5f))
                val tie = Path()
                tie.moveTo(46f, 36f); tie.lineTo(54f, 10f); tie.lineTo(64f, 30f); tie.lineTo(74f, 10f); tie.lineTo(82f, 36f); tie.close()
                c.drawPath(tie, paint(0xFFE3D2A6.toInt()))
                c.drawRoundRect(RectF(40f, 62f, 88f, 92f), 8f, 8f, paint(0xFFD9532D.toInt()))
            }
            I_MEAT -> {
                c.drawOval(RectF(14f, 30f, 100f, 108f), paint(0xFFD9473E.toInt()))
                c.drawOval(RectF(30f, 44f, 70f, 74f), paint(0xFFF2A6A0.toInt()))
                c.drawOval(RectF(14f, 30f, 100f, 108f), paint(0xFF7A1F1A.toInt(), Paint.Style.STROKE, 5f))
                c.drawRoundRect(RectF(92f, 60f, 122f, 74f), 7f, 7f, paint(0xFFF7F2E7.toInt()))
                c.drawCircle(120f, 58f, 9f, paint(0xFFF7F2E7.toInt()))
                c.drawCircle(120f, 78f, 9f, paint(0xFFF7F2E7.toInt()))
            }
            I_JHOL -> {
                c.drawRoundRect(RectF(30f, 30f, 98f, 116f), 20f, 20f, paint(0xFFD02A1E.toInt()))
                c.drawRoundRect(RectF(30f, 30f, 98f, 116f), 20f, 20f, paint(0xFF6E100A.toInt(), Paint.Style.STROKE, 5f))
                c.drawRoundRect(RectF(38f, 12f, 90f, 36f), 8f, 8f, paint(0xFF3B2A25.toInt()))
                c.drawRoundRect(RectF(42f, 56f, 86f, 90f), 8f, 8f, paint(0xFFFFE9A8.toInt()))
                c.drawCircle(64f, 73f, 9f, paint(0xFFD02A1E.toInt()))
            }
            I_PLATE -> {
                c.drawOval(RectF(8f, 56f, 120f, 116f), paint(0xFFEFF3F6.toInt()))
                c.drawOval(RectF(8f, 56f, 120f, 116f), paint(0xFF8996A3.toInt(), Paint.Style.STROKE, 5f))
                for (i in 0 until 3) {
                    val mx = 34f + i * 30f
                    val my = 70f + (if (i == 1) 12f else 0f)
                    val path = Path()
                    path.moveTo(mx - 20f, my + 20f)
                    path.quadTo(mx - 20f, my - 26f, mx, my - 26f)
                    path.quadTo(mx + 20f, my - 26f, mx + 20f, my + 20f)
                    path.close()
                    c.drawPath(path, paint(0xFFFFEFC9.toInt()))
                    c.drawPath(path, paint(0xFFBE8E4A.toInt(), Paint.Style.STROKE, 4f))
                    c.drawLine(mx, my - 22f, mx, my - 4f, paint(0xFFBE8E4A.toInt(), Paint.Style.STROKE, 3f))
                }
            }
            I_CUP -> {
                val path = Path()
                path.moveTo(30f, 52f); path.lineTo(92f, 52f); path.lineTo(82f, 108f); path.lineTo(40f, 108f); path.close()
                c.drawPath(path, paint(0xFFC27A3E.toInt()))
                c.drawPath(path, paint(0xFF6D3E19.toInt(), Paint.Style.STROKE, 5f))
                c.drawRect(32f, 52f, 90f, 66f, paint(0xFFE9C79B.toInt()))
                c.drawArc(RectF(82f, 60f, 112f, 90f), -90f, 180f, false, paint(0xFF6D3E19.toInt(), Paint.Style.STROKE, 6f))
                val sp = paint(0xCCFFFFFF.toInt(), Paint.Style.STROKE, 5f)
                c.drawArc(RectF(40f, 14f, 58f, 44f), 90f, 200f, false, sp)
                c.drawArc(RectF(62f, 8f, 80f, 40f), 270f, 200f, false, sp)
            }
            I_TABLE -> {
                c.drawRoundRect(RectF(10f, 40f, 118f, 62f), 8f, 8f, paint(0xFFB57A47.toInt()))
                c.drawRoundRect(RectF(10f, 40f, 118f, 62f), 8f, 8f, paint(0xFF5E3A1D.toInt(), Paint.Style.STROKE, 5f))
                c.drawRect(22f, 62f, 32f, 112f, paint(0xFF8E5B2E.toInt()))
                c.drawRect(96f, 62f, 106f, 112f, paint(0xFF8E5B2E.toInt()))
                c.drawOval(RectF(40f, 24f, 88f, 44f), paint(0xFFFFFFFF.toInt()))
            }
            I_BENCH -> {
                c.drawRoundRect(RectF(8f, 70f, 120f, 86f), 6f, 6f, paint(0xFFA9703B.toInt()))
                c.drawRect(18f, 86f, 28f, 112f, paint(0xFF6D4220.toInt()))
                c.drawRect(100f, 86f, 110f, 112f, paint(0xFF6D4220.toInt()))
                val path = Path()
                path.moveTo(44f, 22f); path.lineTo(80f, 22f); path.lineTo(74f, 62f); path.lineTo(50f, 62f); path.close()
                c.drawPath(path, paint(0xFFC27A3E.toInt()))
                c.drawPath(path, paint(0xFF6D3E19.toInt(), Paint.Style.STROKE, 4f))
            }
            I_KETTLE -> {
                c.drawRoundRect(RectF(26f, 46f, 94f, 108f), 22f, 22f, paint(0xFFC8D0D8.toInt()))
                c.drawRoundRect(RectF(26f, 46f, 94f, 108f), 22f, 22f, paint(0xFF5E6A76.toInt(), Paint.Style.STROKE, 5f))
                c.drawRoundRect(RectF(40f, 34f, 80f, 50f), 6f, 6f, paint(0xFF9AA6B2.toInt()))
                val sp = Path()
                sp.moveTo(92f, 70f); sp.lineTo(120f, 48f); sp.lineTo(124f, 56f); sp.lineTo(94f, 88f); sp.close()
                c.drawPath(sp, paint(0xFFC8D0D8.toInt()))
                c.drawArc(RectF(24f, 8f, 96f, 70f), 200f, 140f, false, paint(0xFF3A3F45.toInt(), Paint.Style.STROKE, 6f))
            }
            I_CAULDRON -> {
                c.drawArc(RectF(14f, 28f, 114f, 118f), 0f, 180f, true, paint(0xFF2C2C32.toInt()))
                c.drawOval(RectF(10f, 24f, 118f, 56f), paint(0xFF45454E.toInt()))
                c.drawOval(RectF(18f, 30f, 110f, 52f), paint(0xFFD2331F.toInt()))
                c.drawOval(RectF(36f, 34f, 70f, 44f), paint(0xFFFF7A59.toInt()))
                c.drawRect(30f, 110f, 40f, 126f, paint(0xFF45454E.toInt()))
                c.drawRect(88f, 110f, 98f, 126f, paint(0xFF45454E.toInt()))
            }
            I_MOKTU -> {
                for (i in 0 until 3) {
                    val y = 96f - i * 28f
                    c.drawRoundRect(RectF(22f + i * 4f, y, 106f - i * 4f, y + 30f), 8f, 8f, paint(if (i % 2 == 0) 0xFFD2D8DE.toInt() else 0xFFB6BEC6.toInt()))
                    c.drawRoundRect(RectF(22f + i * 4f, y, 106f - i * 4f, y + 30f), 8f, 8f, paint(0xFF5E6A76.toInt(), Paint.Style.STROKE, 4f))
                }
                c.drawArc(RectF(34f, 2f, 94f, 30f), 180f, 180f, true, paint(0xFFE1E6EA.toInt()))
                c.drawRoundRect(RectF(14f, 112f, 114f, 124f), 4f, 4f, paint(0xFF3B3F46.toInt()))
            }
            I_PREP -> {
                c.drawRoundRect(RectF(10f, 64f, 118f, 92f), 8f, 8f, paint(0xFFB57A47.toInt()))
                c.drawCircle(46f, 54f, 24f, paint(0xFFFFF5DC.toInt()))
                c.drawCircle(46f, 54f, 24f, paint(0xFFC9AE74.toInt(), Paint.Style.STROKE, 4f))
                c.drawRoundRect(RectF(70f, 40f, 118f, 54f), 7f, 7f, paint(0xFFE3B57A.toInt()))
                c.drawRect(22f, 92f, 32f, 118f, paint(0xFF8E5B2E.toInt()))
                c.drawRect(96f, 92f, 106f, 118f, paint(0xFF8E5B2E.toInt()))
            }
            I_EXCL -> {
                c.drawCircle(cx, cy, 58f, paint(0xFFE53935.toInt()))
                c.drawCircle(cx, cy, 58f, paint(Color.WHITE, Paint.Style.STROKE, 7f))
                c.drawRoundRect(RectF(56f, 24f, 72f, 74f), 8f, 8f, paint(Color.WHITE))
                c.drawCircle(cx, 94f, 9f, paint(Color.WHITE))
            }
            I_ARROW -> {
                val path = Path()
                path.moveTo(cx, 120f); path.lineTo(112f, 56f); path.lineTo(80f, 56f); path.lineTo(80f, 8f)
                path.lineTo(48f, 8f); path.lineTo(48f, 56f); path.lineTo(16f, 56f); path.close()
                c.drawPath(path, paint(Color.WHITE))
            }
        }
    }

    private fun drawBubble(c: Canvas, b: Int, w: Int, h: Int) {
        var bg = Color.WHITE
        var fg = 0xFF2A2230.toInt()
        when (b) {
            Bub.JHOL -> { bg = 0xFFE53935.toInt(); fg = Color.WHITE }
            Bub.ANGRY -> { bg = 0xFFB71C1C.toInt(); fg = Color.WHITE }
            Bub.EXCL -> { bg = 0xFFE53935.toInt(); fg = Color.WHITE }
            Bub.HAPPY -> { bg = 0xFFFFF3C4.toInt() }
            Bub.VLOG, Bub.BIKE, Bub.TOUR -> { bg = 0xFFFFE08A.toInt() }
            Bub.SURYA -> { bg = 0xFFFFD6A5.toInt() }
        }
        val body = RectF(6f, 6f, w - 6f, 92f)
        val tail = Path()
        tail.moveTo(w / 2f - 16f, 88f)
        tail.lineTo(w / 2f, 124f)
        tail.lineTo(w / 2f + 16f, 88f)
        tail.close()
        val stroke = paint(0xFF2A2230.toInt(), Paint.Style.STROKE, 6f)
        stroke.strokeJoin = Paint.Join.ROUND
        c.drawPath(tail, paint(bg))
        c.drawRoundRect(body, 30f, 30f, paint(bg))
        c.drawRoundRect(body, 30f, 30f, stroke)
        c.drawLine(w / 2f - 12f, 90f, w / 2f + 12f, 90f, paint(bg, Paint.Style.STROKE, 8f))
        val tp = Paint(Paint.ANTI_ALIAS_FLAG)
        tp.typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
        tp.textAlign = Paint.Align.CENTER
        tp.color = fg
        val text = BUB_TEXT[b]
        var size = if (b == Bub.EXCL) 78f else 44f
        tp.textSize = size
        val maxW = w - 56f
        while (tp.measureText(text) > maxW && size > 16f) {
            size -= 2f
            tp.textSize = size
        }
        var tx = w / 2f
        if (b == Bub.HAPPY) tx -= 14f
        c.drawText(text, tx, 49f + size * 0.35f, tp)
        if (b == Bub.HAPPY) {
            val hp = Path()
            val hx = w - 44f
            val hy = 42f
            hp.moveTo(hx, hy + 22f)
            hp.cubicTo(hx - 34f, hy - 4f, hx - 14f, hy - 28f, hx, hy - 8f)
            hp.cubicTo(hx + 14f, hy - 28f, hx + 34f, hy - 4f, hx, hy + 22f)
            c.drawPath(hp, paint(0xFFE91E63.toInt()))
        }
    }
}
