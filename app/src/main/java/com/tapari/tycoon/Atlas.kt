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
    const val I_BUILD = 18
    const val G0 = 19
    const val B0 = 35
    const val N = 51

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

    private fun sizeW(id: Int): Int = if (id < G0) 128 else if (id < B0) 64 else 128
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
            I_BUILD -> {
                c.drawRoundRect(RectF(14f, 40f, 114f, 118f), 6f, 6f, paint(0xFFE9D6B0.toInt()))
                c.drawRoundRect(RectF(14f, 40f, 114f, 118f), 6f, 6f, paint(0xFF6B4A2B.toInt(), Paint.Style.STROKE, 5f))
                for (k in 0 until 6) {
                    c.drawRect(14f + k * 16.6f, 22f, 14f + (k + 1) * 16.6f, 46f, paint(if (k % 2 == 0) 0xFFD6382F.toInt() else 0xFFFFFFFF.toInt()))
                }
                c.drawRect(14f, 22f, 114f, 46f, paint(0xFF6B4A2B.toInt(), Paint.Style.STROKE, 4f))
                c.drawRoundRect(RectF(50f, 66f, 78f, 118f), 4f, 4f, paint(0xFF8E5B2E.toInt()))
                c.drawRoundRect(RectF(22f, 58f, 44f, 84f), 4f, 4f, paint(0xFF9AD7F5.toInt()))
                c.drawRoundRect(RectF(84f, 58f, 106f, 84f), 4f, 4f, paint(0xFF9AD7F5.toInt()))
                c.drawCircle(72f, 94f, 3.5f, paint(0xFFFFD54A.toInt()))
            }
            I_ARROW -> {
                val path = Path()
                path.moveTo(cx, 120f); path.lineTo(112f, 56f); path.lineTo(80f, 56f); path.lineTo(80f, 8f)
                path.lineTo(48f, 8f); path.lineTo(48f, 56f); path.lineTo(16f, 56f); path.close()
                c.drawPath(path, paint(Color.WHITE))
            }
        }
    }

    private fun fillStroke(c: Canvas, path: Path, fill: Int, stroke: Int, sw: Float) {
        c.drawPath(path, paint(fill))
        val sp = paint(stroke, Paint.Style.STROKE, sw)
        sp.strokeJoin = Paint.Join.ROUND
        sp.strokeCap = Paint.Cap.ROUND
        c.drawPath(path, sp)
    }

    private fun drawBubble(c: Canvas, b: Int, w: Int, h: Int) {
        var bg = Color.WHITE
        when (b) {
            Bub.JHOL, Bub.EXCL -> bg = 0xFFE53935.toInt()
            Bub.ANGRY -> bg = 0xFF7F1010.toInt()
            Bub.HAPPY -> bg = 0xFFFFF3C4.toInt()
            Bub.VLOG -> bg = 0xFFFF8FC8.toInt()
            Bub.BIKE -> bg = 0xFFFFD66B.toInt()
            Bub.TOUR -> bg = 0xFF8FE3D8.toInt()
            Bub.SURYA -> bg = 0xFFFFD6A5.toInt()
            Bub.NAMASTE -> bg = 0xFFFFE9B0.toInt()
            Bub.THANKS -> bg = 0xFFD8F5D0.toInt()
        }
        val ink = 0xFF2A2230.toInt()
        val cx = w / 2f
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
        c.drawCircle(cx, cy, 40f, paint(0x22FFFFFF))
        c.save()
        c.translate(cx, cy)
        when (b) {
            Bub.JHOL -> {
                val p = Path()
                p.moveTo(-26f, 24f)
                p.cubicTo(-34f, 6f, -18f, -22f, 6f, -24f)
                p.cubicTo(22f, -25f, 28f, -12f, 20f, -8f)
                p.cubicTo(8f, -8f, 4f, 8f, -8f, 22f)
                p.cubicTo(-14f, 28f, -24f, 28f, -26f, 24f)
                p.close()
                fillStroke(c, p, 0xFFFFFFFF.toInt(), 0xFF7F1010.toInt(), 4f)
                c.drawRoundRect(RectF(2f, -34f, 20f, -20f), 5f, 5f, paint(0xFF2E7D32.toInt()))
                c.drawCircle(-6f, 4f, 5f, paint(0xFFE53935.toInt()))
            }
            Bub.MOMO -> {
                c.drawOval(RectF(-34f, 6f, 34f, 34f), paint(0xFFE9EEF2.toInt()))
                c.drawOval(RectF(-34f, 6f, 34f, 34f), paint(0xFF8996A3.toInt(), Paint.Style.STROKE, 3f))
                val p = Path()
                p.moveTo(-24f, 14f)
                p.quadTo(-24f, -26f, 0f, -26f)
                p.quadTo(24f, -26f, 24f, 14f)
                p.close()
                fillStroke(c, p, 0xFFFFEFC9.toInt(), 0xFFBE8E4A.toInt(), 4f)
                for (k in -2..2) c.drawLine(k * 5f, -22f, k * 3f, -6f, paint(0xFFBE8E4A.toInt(), Paint.Style.STROKE, 3f))
            }
            Bub.CHIYA -> {
                val p = Path()
                p.moveTo(-22f, -10f); p.lineTo(18f, -10f); p.lineTo(12f, 28f); p.lineTo(-16f, 28f); p.close()
                fillStroke(c, p, 0xFFC27A3E.toInt(), 0xFF6D3E19.toInt(), 4f)
                c.drawRect(-20f, -10f, 16f, 0f, paint(0xFFE9C79B.toInt()))
                c.drawArc(RectF(10f, -2f, 32f, 20f), -90f, 180f, false, paint(0xFF6D3E19.toInt(), Paint.Style.STROKE, 5f))
                val sp = paint(0xFFB0B8C0.toInt(), Paint.Style.STROKE, 4f)
                c.drawArc(RectF(-14f, -34f, -2f, -14f), 90f, 200f, false, sp)
                c.drawArc(RectF(2f, -38f, 14f, -16f), 270f, 200f, false, sp)
            }
            Bub.HAPPY -> {
                val hp = Path()
                hp.moveTo(0f, 26f)
                hp.cubicTo(-40f, -2f, -20f, -34f, 0f, -12f)
                hp.cubicTo(20f, -34f, 40f, -2f, 0f, 26f)
                fillStroke(c, hp, 0xFFE91E63.toInt(), 0xFF8E0F3A.toInt(), 3f)
                c.drawCircle(-9f, -9f, 4f, paint(0x88FFFFFF.toInt()))
            }
            Bub.ANGRY -> {
                c.drawCircle(0f, 2f, 28f, paint(0xFFFFB74D.toInt()))
                c.drawCircle(0f, 2f, 28f, paint(0xFF4E1010.toInt(), Paint.Style.STROKE, 4f))
                val br = paint(0xFF4E1010.toInt(), Paint.Style.STROKE, 6f)
                br.strokeCap = Paint.Cap.ROUND
                c.drawLine(-18f, -12f, -5f, -3f, br)
                c.drawLine(18f, -12f, 5f, -3f, br)
                c.drawCircle(-10f, 4f, 4f, paint(0xFF4E1010.toInt()))
                c.drawCircle(10f, 4f, 4f, paint(0xFF4E1010.toInt()))
                c.drawArc(RectF(-12f, 12f, 12f, 30f), 200f, 140f, false, br)
                val st = paint(0xFFFF5252.toInt(), Paint.Style.STROKE, 4f)
                c.drawLine(-22f, -30f, -14f, -22f, st)
                c.drawLine(22f, -30f, 14f, -22f, st)
            }
            Bub.CHAT0 -> {
                c.drawCircle(0f, -6f, 22f, paint(0xFFFFD84A.toInt()))
                c.drawCircle(0f, -6f, 22f, paint(0xFFB78A00.toInt(), Paint.Style.STROKE, 4f))
                c.drawRoundRect(RectF(-11f, 14f, 11f, 30f), 4f, 4f, paint(0xFF8E98A3.toInt()))
                c.drawLine(-12f, 20f, 12f, 20f, paint(0xFF5E6A76.toInt(), Paint.Style.STROKE, 3f))
                c.drawArc(RectF(-12f, -18f, 12f, 6f), 200f, 100f, false, paint(0xAAFFFFFF.toInt(), Paint.Style.STROKE, 4f))
            }
            Bub.CHAT1 -> {
                val p = Path()
                p.moveTo(-18f, -32f); p.lineTo(22f, 2f); p.lineTo(2f, 2f); p.lineTo(22f, 32f); p.lineTo(-18f, 32f); p.close()
                fillStroke(c, p, 0xFFDC143C.toInt(), 0xFF003893.toInt(), 5f)
                c.drawCircle(-8f, -6f, 5f, paint(Color.WHITE))
                c.drawCircle(-6f, 18f, 5f, paint(Color.WHITE))
            }
            Bub.CHAT2 -> {
                val wp = paint(0xFF1E88E5.toInt(), Paint.Style.STROKE, 7f)
                wp.strokeCap = Paint.Cap.ROUND
                c.drawArc(RectF(-32f, -26f, 32f, 38f), 225f, 90f, false, wp)
                c.drawArc(RectF(-22f, -14f, 22f, 30f), 225f, 90f, false, wp)
                c.drawArc(RectF(-12f, -2f, 12f, 22f), 225f, 90f, false, wp)
                c.drawCircle(0f, 26f, 5f, paint(0xFF1E88E5.toInt()))
            }
            Bub.CHAT3 -> {
                val p = Path()
                for (k in 0 until 6) {
                    val a = Math.toRadians(30.0 + 60.0 * k)
                    val px = (Math.cos(a) * 30.0).toFloat()
                    val py = (Math.sin(a) * 30.0).toFloat()
                    if (k == 0) p.moveTo(px, py) else p.lineTo(px, py)
                }
                p.close()
                fillStroke(c, p, 0xFF8E5CF7.toInt(), 0xFF3B1E8A.toInt(), 4f)
                val tp = Paint(Paint.ANTI_ALIAS_FLAG)
                tp.typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
                tp.textAlign = Paint.Align.CENTER
                tp.textSize = 30f
                tp.color = Color.WHITE
                c.drawText("W3", 0f, 11f, tp)
            }
            Bub.SURYA -> {
                c.save()
                c.rotate(-25f)
                c.drawRoundRect(RectF(-30f, -6f, 22f, 8f), 3f, 3f, paint(0xFFFFFFFF.toInt()))
                c.drawRect(RectF(6f, -6f, 22f, 8f), paint(0xFFE29A3C.toInt()))
                c.drawRoundRect(RectF(-34f, -6f, -26f, 8f), 3f, 3f, paint(0xFFE53935.toInt()))
                c.drawRoundRect(RectF(-30f, -6f, 22f, 8f), 3f, 3f, paint(ink, Paint.Style.STROKE, 3f))
                c.restore()
                val sp = paint(0xFF9AA3AB.toInt(), Paint.Style.STROKE, 4f)
                sp.strokeCap = Paint.Cap.ROUND
                val sm = Path()
                sm.moveTo(-26f, -10f)
                sm.cubicTo(-34f, -22f, -18f, -26f, -26f, -38f)
                c.drawPath(sm, sp)
            }
            Bub.THANKS -> {
                c.drawCircle(0f, 0f, 28f, paint(0xFFFFD84A.toInt()))
                c.drawCircle(0f, 0f, 28f, paint(0xFF8A6A00.toInt(), Paint.Style.STROKE, 4f))
                c.drawCircle(-10f, -6f, 4f, paint(ink))
                c.drawCircle(10f, -6f, 4f, paint(ink))
                val sm = paint(ink, Paint.Style.STROKE, 5f)
                sm.strokeCap = Paint.Cap.ROUND
                c.drawArc(RectF(-14f, -2f, 14f, 18f), 20f, 140f, false, sm)
                c.drawCircle(-18f, 8f, 5f, paint(0x66FF5A5A.toInt()))
                c.drawCircle(18f, 8f, 5f, paint(0x66FF5A5A.toInt()))
            }
            Bub.VLOG -> {
                c.drawRoundRect(RectF(-30f, -16f, 20f, 22f), 8f, 8f, paint(0xFF2A2230.toInt()))
                val tri = Path()
                tri.moveTo(20f, -4f); tri.lineTo(34f, -14f); tri.lineTo(34f, 20f); tri.lineTo(20f, 10f); tri.close()
                c.drawPath(tri, paint(0xFF2A2230.toInt()))
                c.drawCircle(-6f, 3f, 11f, paint(0xFF8BE6FF.toInt()))
                c.drawCircle(-6f, 3f, 5f, paint(0xFF136C8E.toInt()))
                c.drawCircle(-22f, -8f, 4f, paint(0xFFFF3B3B.toInt()))
            }
            Bub.BIKE -> {
                val p = Path()
                p.moveTo(-30f, 14f)
                p.cubicTo(-34f, -28f, 30f, -28f, 30f, 6f)
                p.lineTo(30f, 14f)
                p.close()
                fillStroke(c, p, 0xFF2A2A30.toInt(), ink, 3f)
                c.drawRoundRect(RectF(-4f, -4f, 34f, 12f), 6f, 6f, paint(0xFFFFC93C.toInt()))
                c.drawRect(-30f, 14f, 30f, 22f, paint(0xFF55555F.toInt()))
            }
            Bub.TOUR -> {
                c.drawRoundRect(RectF(-32f, -14f, 32f, 28f), 8f, 8f, paint(0xFF2A2230.toInt()))
                c.drawRoundRect(RectF(-14f, -24f, 8f, -12f), 4f, 4f, paint(0xFF2A2230.toInt()))
                c.drawCircle(0f, 7f, 15f, paint(0xFFDDE6EA.toInt()))
                c.drawCircle(0f, 7f, 9f, paint(0xFF2FB5A8.toInt()))
                c.drawCircle(-4f, 3f, 3f, paint(0x99FFFFFF.toInt()))
                c.drawCircle(22f, -6f, 3f, paint(0xFFFFC93C.toInt()))
            }
            Bub.NAMASTE -> {
                for (k in 0 until 8) {
                    val a = Math.toRadians(45.0 * k)
                    c.drawCircle((Math.cos(a) * 17.0).toFloat(), (Math.sin(a) * 17.0).toFloat(), 11f, paint(0xFFFF8F1F.toInt()))
                }
                c.drawCircle(0f, 0f, 12f, paint(0xFFFFC93C.toInt()))
            }
            Bub.EXCL -> {
                c.drawRoundRect(RectF(-7f, -30f, 7f, 8f), 6f, 6f, paint(Color.WHITE))
                c.drawCircle(0f, 22f, 8f, paint(Color.WHITE))
            }
        }
        c.restore()
    }
}
