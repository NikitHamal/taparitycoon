package com.tapari.tycoon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp

/** Custom hand-drawn vector icon set (no emoji, no font dependency). Drawn in a 100x100 box. */
enum class Ic {
    COIN, GEM, BAG, CHILI, GEAR, CALENDAR, MAP, UP, BOLT, STAR, SHOP, CART, ROOF, GLOBE, SHOE, TAG,
    STEAMER, CHEF, POT, CUP, CASH, NOCAMP, SMOKE, LOCK, CHECK, CLOSE, PLAY, SOUND, MUTE, VIBRATE,
    TROPHY, REFRESH, MOMO, INFO, HOME, PAUSE
}

@Composable
fun GIcon(ic: Ic, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 100f
        withTransform({ scale(k, k, Offset.Zero) }) { drawIc(ic) }
    }
}

private fun P(x: Float, y: Float) = Offset(x, y)

private fun DrawScope.fillP(p: Path, c: Color) = drawPath(p, c)
private fun DrawScope.lineP(p: Path, c: Color, w: Float) =
    drawPath(p, c, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round))

private fun poly(vararg v: Float): Path {
    val p = Path()
    p.moveTo(v[0], v[1])
    var i = 2
    while (i + 1 < v.size) {
        p.lineTo(v[i], v[i + 1])
        i += 2
    }
    p.close()
    return p
}

private fun vg(a: Long, b: Long): Brush = Brush.verticalGradient(listOf(Color(a), Color(b)), 0f, 100f)

private fun DrawScope.drawIc(ic: Ic) {
    val ink = Color(0xFF2A1A20)
    when (ic) {
        Ic.COIN -> {
            drawCircle(Color(0xFFB8860B), 48f, P(50f, 50f))
            drawCircle(vg(0xFFFFE27A, 0xFFFFB300), 42f, P(50f, 50f))
            drawCircle(Color(0xFFFFF0B8), 33f, P(50f, 50f), style = Stroke(3f))
            val r = Path()
            r.moveTo(40f, 70f); r.lineTo(40f, 30f); r.lineTo(55f, 30f)
            r.cubicTo(70f, 30f, 70f, 52f, 55f, 52f); r.lineTo(40f, 52f)
            lineP(r, Color(0xFF8A5A00), 7f)
            val leg = Path()
            leg.moveTo(52f, 52f); leg.lineTo(66f, 70f)
            lineP(leg, Color(0xFF8A5A00), 7f)
        }
        Ic.GEM -> {
            fillP(poly(50f, 94f, 12f, 34f, 88f, 34f), Color(0xFF1EA5D8))
            fillP(poly(50f, 8f, 88f, 34f, 12f, 34f), Color(0xFF9BEBFF))
            fillP(poly(50f, 8f, 65f, 34f, 35f, 34f), Color(0xFFD4F8FF))
            fillP(poly(12f, 34f, 35f, 34f, 50f, 94f), Color(0xFF35C6F4))
            lineP(poly(50f, 8f, 88f, 34f, 50f, 94f, 12f, 34f), Color(0xFF0E5A78), 5f)
            lineP(poly(12f, 34f, 88f, 34f, 88f, 34f), Color(0xFF0E5A78), 4f)
        }
        Ic.BAG -> {
            drawArc(Color(0xFF7A1F1A), 180f, 180f, false, P(36f, 8f), Size(28f, 28f), style = Stroke(7f, cap = StrokeCap.Round))
            drawRoundRect(vg(0xFFFF6A4D, 0xFFC62828), P(20f, 22f), Size(60f, 70f), CornerRadius(20f))
            drawRoundRect(Color(0xFF7A1F1A), P(20f, 22f), Size(60f, 70f), CornerRadius(20f), style = Stroke(4f))
            drawRoundRect(Color(0xFFA11F1F), P(31f, 54f), Size(38f, 28f), CornerRadius(8f))
            drawLine(Color(0xFFFFD54A), P(36f, 60f), P(64f, 60f), 4f, StrokeCap.Round)
            drawLine(Color(0xFF7A1F1A), P(50f, 30f), P(50f, 44f), 4f, StrokeCap.Round)
        }
        Ic.CHILI -> {
            val b = Path()
            b.moveTo(26f, 70f)
            b.cubicTo(12f, 48f, 34f, 16f, 62f, 18f)
            b.cubicTo(82f, 16f, 90f, 30f, 78f, 36f)
            b.cubicTo(62f, 36f, 56f, 58f, 40f, 74f)
            b.cubicTo(34f, 82f, 28f, 80f, 26f, 70f)
            b.close()
            drawPath(b, vg(0xFFFF5A3C, 0xFFB71C1C))
            lineP(b, Color(0xFF6E100A), 4f)
            val s = Path()
            s.moveTo(62f, 18f); s.cubicTo(64f, 8f, 72f, 6f, 80f, 10f)
            lineP(s, Color(0xFF2E9E4F), 8f)
            val h = Path()
            h.moveTo(34f, 56f); h.cubicTo(36f, 40f, 48f, 28f, 60f, 26f)
            lineP(h, Color(0x99FFFFFF), 4f)
        }
        Ic.GEAR -> {
            for (i in 0 until 8) {
                rotate(i * 45f, P(50f, 50f)) {
                    drawRoundRect(Color(0xFFB8C0CC), P(41f, 4f), Size(18f, 26f), CornerRadius(5f))
                }
            }
            drawCircle(vg(0xFFE9EDF2, 0xFF9AA5B3), 34f, P(50f, 50f))
            drawCircle(Color(0xFF4A5560), 34f, P(50f, 50f), style = Stroke(4f))
            drawCircle(Color(0xFF39424C), 14f, P(50f, 50f))
            drawCircle(Color(0xFF6B7885), 14f, P(50f, 50f), style = Stroke(3f))
        }
        Ic.CALENDAR -> {
            drawRoundRect(Color.White, P(12f, 18f), Size(76f, 72f), CornerRadius(12f))
            drawRoundRect(Color(0xFF5E6A76), P(12f, 18f), Size(76f, 72f), CornerRadius(12f), style = Stroke(4f))
            drawRoundRect(Color(0xFFE53935), P(12f, 18f), Size(76f, 24f), CornerRadius(12f))
            drawRect(Color(0xFFE53935), P(12f, 30f), Size(76f, 12f))
            drawLine(Color(0xFF3A3F45), P(32f, 8f), P(32f, 26f), 7f, StrokeCap.Round)
            drawLine(Color(0xFF3A3F45), P(68f, 8f), P(68f, 26f), 7f, StrokeCap.Round)
            for (r in 0 until 2) for (c in 0 until 3) {
                drawRoundRect(if (r == 1 && c == 1) Color(0xFF3FBF5A) else Color(0xFFB0B8C2), P(23f + c * 21f, 50f + r * 18f), Size(13f, 11f), CornerRadius(3f))
            }
        }
        Ic.MAP -> {
            fillP(poly(8f, 24f, 34f, 14f, 34f, 78f, 8f, 88f), Color(0xFF7BCB6B))
            fillP(poly(34f, 14f, 66f, 24f, 66f, 88f, 34f, 78f), Color(0xFFF2D56B))
            fillP(poly(66f, 24f, 92f, 14f, 92f, 78f, 66f, 88f), Color(0xFF6FC3E8))
            lineP(poly(8f, 24f, 34f, 14f, 66f, 24f, 92f, 14f, 92f, 78f, 66f, 88f, 34f, 78f, 8f, 88f), Color(0xFF3F4A56), 4f)
            drawLine(Color(0xFF3F4A56), P(34f, 14f), P(34f, 78f), 3f)
            drawLine(Color(0xFF3F4A56), P(66f, 24f), P(66f, 88f), 3f)
            drawCircle(Color(0xFFE53935), 10f, P(54f, 42f))
            drawCircle(Color.White, 4f, P(54f, 42f))
        }
        Ic.UP -> {
            val a = poly(50f, 8f, 88f, 48f, 64f, 48f, 64f, 90f, 36f, 90f, 36f, 48f, 12f, 48f)
            drawPath(a, vg(0xFF7BE06B, 0xFF1F9E4A))
            lineP(a, Color(0xFF0E5A2A), 5f)
        }
        Ic.BOLT -> {
            val a = poly(60f, 4f, 20f, 56f, 44f, 56f, 36f, 96f, 82f, 38f, 56f, 38f)
            drawPath(a, vg(0xFFFFF176, 0xFFFFA000))
            lineP(a, Color(0xFF8A5A00), 4f)
        }
        Ic.STAR -> {
            val p = Path()
            for (i in 0 until 10) {
                val ang = Math.toRadians(-90.0 + 36.0 * i)
                val rr = if (i % 2 == 0) 46.0 else 20.0
                val x = (50.0 + Math.cos(ang) * rr).toFloat()
                val y = (54.0 + Math.sin(ang) * rr).toFloat()
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            p.close()
            drawPath(p, vg(0xFFFFEB7A, 0xFFFFB300))
            lineP(p, Color(0xFF8A5A00), 4f)
        }
        Ic.SHOP -> {
            drawRoundRect(Color(0xFFF3E4C2), P(14f, 40f), Size(72f, 52f), CornerRadius(5f))
            for (k in 0 until 6) drawRect(if (k % 2 == 0) Color(0xFFD6382F) else Color.White, P(10f + k * 13.3f, 18f), Size(13.4f, 26f))
            drawRect(Color(0xFF6B4A2B), P(10f, 18f), Size(80f, 26f), style = Stroke(4f))
            drawRoundRect(Color(0xFF8E5B2E), P(42f, 58f), Size(18f, 34f), CornerRadius(3f))
            drawRoundRect(Color(0xFF9AD7F5), P(20f, 54f), Size(16f, 18f), CornerRadius(3f))
            drawRoundRect(Color(0xFF9AD7F5), P(66f, 54f), Size(16f, 18f), CornerRadius(3f))
        }
        Ic.CART -> {
            drawArc(Color(0xFFD6382F), 180f, 180f, true, P(16f, 8f), Size(68f, 56f))
            drawArc(Color.White, 215f, 40f, true, P(16f, 8f), Size(68f, 56f))
            drawLine(Color(0xFF6B4A2B), P(50f, 34f), P(50f, 60f), 4f)
            drawRoundRect(Color(0xFF9B6B43), P(14f, 52f), Size(72f, 22f), CornerRadius(5f))
            drawRoundRect(Color(0xFF6D4A2D), P(14f, 52f), Size(72f, 22f), CornerRadius(5f), style = Stroke(3f))
            drawCircle(Color(0xFFFFEFC9), 8f, P(34f, 50f))
            drawCircle(Color(0xFFFFEFC9), 8f, P(52f, 50f))
            drawCircle(Color(0xFF2A2D34), 11f, P(30f, 84f))
            drawCircle(Color(0xFF2A2D34), 11f, P(72f, 84f))
            drawCircle(Color(0xFFB8C0CC), 4f, P(30f, 84f))
            drawCircle(Color(0xFFB8C0CC), 4f, P(72f, 84f))
        }
        Ic.ROOF -> {
            drawCircle(Color(0xFFFFE9A0), 14f, P(72f, 24f))
            drawRect(Color(0xFF5A4E8A), P(8f, 52f), Size(20f, 40f))
            drawRect(Color(0xFF6D5FA3), P(28f, 34f), Size(24f, 58f))
            drawRect(Color(0xFF4A3F78), P(52f, 46f), Size(18f, 46f))
            drawRect(Color(0xFF6D5FA3), P(70f, 58f), Size(22f, 34f))
            for (k in 0 until 4) drawRect(Color(0xFFFFD27A), P(33f + (k % 2) * 10f, 42f + (k / 2) * 14f), Size(5f, 7f))
            drawRect(Color(0xFFB8C0CC), P(4f, 88f), Size(92f, 6f))
        }
        Ic.GLOBE -> {
            drawCircle(vg(0xFF6FC3E8, 0xFF2A7FB8), 44f, P(50f, 50f))
            val c1 = Path()
            c1.moveTo(24f, 36f); c1.cubicTo(30f, 24f, 46f, 26f, 48f, 38f); c1.cubicTo(44f, 50f, 30f, 52f, 24f, 36f)
            drawPath(c1, Color(0xFF59B35A))
            val c2 = Path()
            c2.moveTo(56f, 56f); c2.cubicTo(70f, 46f, 82f, 56f, 76f, 70f); c2.cubicTo(66f, 80f, 54f, 70f, 56f, 56f)
            drawPath(c2, Color(0xFF59B35A))
            drawCircle(Color(0xFF1B5A85), 44f, P(50f, 50f), style = Stroke(4f))
            drawArc(Color(0x66FFFFFF), 90f, 180f, false, P(30f, 6f), Size(40f, 88f), style = Stroke(2.5f))
        }
        Ic.SHOE -> {
            val s = Path()
            s.moveTo(8f, 66f); s.lineTo(8f, 46f); s.cubicTo(28f, 46f, 40f, 28f, 50f, 28f)
            s.lineTo(60f, 44f); s.cubicTo(70f, 54f, 90f, 52f, 94f, 66f); s.lineTo(94f, 76f); s.lineTo(8f, 76f); s.close()
            drawPath(s, vg(0xFF5AA7FF, 0xFF2C62C8))
            lineP(s, Color(0xFF173A7A), 4f)
            drawRoundRect(Color.White, P(6f, 72f), Size(90f, 12f), CornerRadius(6f))
            drawLine(Color.White, P(46f, 38f), P(54f, 46f), 4f, StrokeCap.Round)
            drawLine(Color.White, P(56f, 36f), P(64f, 46f), 4f, StrokeCap.Round)
        }
        Ic.TAG -> {
            val t = poly(6f, 50f, 40f, 12f, 92f, 12f, 92f, 60f, 56f, 92f)
            drawPath(t, vg(0xFFFFD54A, 0xFFFFA000))
            lineP(t, Color(0xFF8A5A00), 4f)
            drawCircle(Color(0xFF8A5A00), 7f, P(72f, 30f))
            val r = Path()
            r.moveTo(40f, 66f); r.lineTo(40f, 40f); r.lineTo(50f, 40f)
            r.cubicTo(60f, 40f, 60f, 54f, 50f, 54f); r.lineTo(40f, 54f)
            lineP(r, Color(0xFF8A5A00), 5f)
        }
        Ic.STEAMER -> {
            for (i in 0 until 3) {
                val y = 56f - i * 17f
                drawRoundRect(if (i % 2 == 0) Color(0xFFDDE3E9) else Color(0xFFBCC5CE), P(16f + i * 2f, y), Size(68f - i * 4f, 20f), CornerRadius(6f))
                drawRoundRect(Color(0xFF5E6A76), P(16f + i * 2f, y), Size(68f - i * 4f, 20f), CornerRadius(6f), style = Stroke(3f))
            }
            drawArc(Color(0xFFE6EBF0), 180f, 180f, true, P(30f, 2f), Size(40f, 20f))
            drawRoundRect(Color(0xFF3B3F46), P(12f, 76f), Size(76f, 14f), CornerRadius(4f))
            drawRoundRect(Color(0xFFFF8A1F), P(26f, 80f), Size(48f, 5f), CornerRadius(2f))
        }
        Ic.CHEF -> {
            drawCircle(Color.White, 17f, P(32f, 36f))
            drawCircle(Color.White, 19f, P(50f, 26f))
            drawCircle(Color.White, 17f, P(68f, 36f))
            drawRoundRect(Color.White, P(30f, 38f), Size(40f, 34f), CornerRadius(4f))
            drawRoundRect(Color(0xFFB8C0CC), P(30f, 60f), Size(40f, 12f), CornerRadius(3f))
            drawCircle(Color(0xFFE8B48A), 20f, P(50f, 82f))
            drawCircle(Color(0xFF2A1A20), 3f, P(43f, 80f))
            drawCircle(Color(0xFF2A1A20), 3f, P(57f, 80f))
        }
        Ic.POT -> {
            val b = Path()
            b.moveTo(14f, 44f); b.lineTo(86f, 44f); b.cubicTo(86f, 84f, 66f, 90f, 50f, 90f); b.cubicTo(34f, 90f, 14f, 84f, 14f, 44f); b.close()
            drawPath(b, vg(0xFF4A4A52, 0xFF202026))
            drawOval(Color(0xFF45454E), P(10f, 34f), Size(80f, 22f))
            drawOval(Color(0xFFD2331F), P(16f, 38f), Size(68f, 14f))
            drawOval(Color(0xFFFF7A59), P(30f, 40f), Size(22f, 6f))
            drawRoundRect(Color(0xFF45454E), P(4f, 50f), Size(12f, 8f), CornerRadius(3f))
            drawRoundRect(Color(0xFF45454E), P(84f, 50f), Size(12f, 8f), CornerRadius(3f))
            val st = Path()
            st.moveTo(38f, 28f); st.cubicTo(30f, 18f, 46f, 14f, 38f, 4f)
            lineP(st, Color(0xAAFFFFFF), 4f)
        }
        Ic.CUP -> {
            val c = poly(22f, 40f, 74f, 40f, 66f, 88f, 30f, 88f)
            drawPath(c, vg(0xFFD98A4A, 0xFFA45A24))
            lineP(c, Color(0xFF5E3410), 4f)
            drawRect(Color(0xFFF1D3A8), P(23f, 40f), Size(50f, 12f))
            drawArc(Color(0xFF5E3410), -90f, 180f, false, P(62f, 46f), Size(26f, 28f), style = Stroke(6f, cap = StrokeCap.Round))
            val s1 = Path()
            s1.moveTo(38f, 30f); s1.cubicTo(30f, 20f, 46f, 14f, 38f, 4f)
            lineP(s1, Color(0xBBFFFFFF), 4f)
            val s2 = Path()
            s2.moveTo(56f, 30f); s2.cubicTo(48f, 20f, 64f, 14f, 56f, 4f)
            lineP(s2, Color(0xBBFFFFFF), 4f)
        }
        Ic.CASH -> {
            drawRoundRect(vg(0xFF7BE06B, 0xFF2E9E4F), P(6f, 24f), Size(88f, 52f), CornerRadius(8f))
            drawRoundRect(Color(0xFF14602C), P(6f, 24f), Size(88f, 52f), CornerRadius(8f), style = Stroke(4f))
            drawCircle(Color(0xFFB9F5A8), 14f, P(50f, 50f))
            drawCircle(Color(0xFF14602C), 14f, P(50f, 50f), style = Stroke(3f))
            drawCircle(Color(0xFF14602C), 4f, P(20f, 50f))
            drawCircle(Color(0xFF14602C), 4f, P(80f, 50f))
        }
        Ic.NOCAMP -> {
            drawRoundRect(Color(0xFFB57A47), P(22f, 52f), Size(56f, 10f), CornerRadius(3f))
            drawRect(Color(0xFF6D4220), P(28f, 62f), Size(7f, 20f))
            drawRect(Color(0xFF6D4220), P(65f, 62f), Size(7f, 20f))
            drawCircle(Color(0xFFE53935), 44f, P(50f, 50f), style = Stroke(9f))
            drawLine(Color(0xFFE53935), P(20f, 80f), P(80f, 20f), 9f, StrokeCap.Round)
        }
        Ic.SMOKE -> {
            rotate(-30f, P(50f, 60f)) {
                drawRoundRect(Color.White, P(14f, 54f), Size(60f, 12f), CornerRadius(3f))
                drawRect(Color(0xFFE29A3C), P(50f, 54f), Size(24f, 12f))
                drawRoundRect(Color(0xFFE53935), P(8f, 54f), Size(10f, 12f), CornerRadius(3f))
                drawRoundRect(Color(0xFF5E6A76), P(14f, 54f), Size(60f, 12f), CornerRadius(3f), style = Stroke(2.5f))
            }
            val s = Path()
            s.moveTo(18f, 50f); s.cubicTo(8f, 36f, 28f, 30f, 18f, 12f)
            lineP(s, Color(0xFF9AA3AB), 5f)
        }
        Ic.LOCK -> {
            drawArc(Color(0xFFB8C0CC), 180f, 180f, false, P(28f, 10f), Size(44f, 52f), style = Stroke(9f, cap = StrokeCap.Round))
            drawRoundRect(vg(0xFFFFD54A, 0xFFE09A00), P(18f, 42f), Size(64f, 50f), CornerRadius(10f))
            drawRoundRect(Color(0xFF8A5A00), P(18f, 42f), Size(64f, 50f), CornerRadius(10f), style = Stroke(4f))
            drawCircle(Color(0xFF5A3A00), 7f, P(50f, 62f))
            drawRect(Color(0xFF5A3A00), P(47f, 62f), Size(6f, 16f))
        }
        Ic.CHECK -> {
            val p = Path()
            p.moveTo(14f, 54f); p.lineTo(40f, 78f); p.lineTo(88f, 24f)
            lineP(p, Color(0xFF14602C), 18f)
            lineP(p, Color(0xFF5BE07B), 11f)
        }
        Ic.CLOSE -> {
            drawLine(Color(0xFFFFF4DC), P(22f, 22f), P(78f, 78f), 12f, StrokeCap.Round)
            drawLine(Color(0xFFFFF4DC), P(78f, 22f), P(22f, 78f), 12f, StrokeCap.Round)
        }
        Ic.PLAY -> {
            val a = poly(26f, 12f, 88f, 50f, 26f, 88f)
            fillP(a, Color(0xFF2B1418))
            lineP(a, Color(0xFF2B1418), 10f)
        }
        Ic.SOUND, Ic.MUTE -> {
            val s = poly(10f, 38f, 28f, 38f, 52f, 16f, 52f, 84f, 28f, 62f, 10f, 62f)
            fillP(s, Color(0xFFFFF4DC))
            if (ic == Ic.SOUND) {
                drawArc(Color(0xFFFFF4DC), -45f, 90f, false, P(40f, 30f), Size(30f, 40f), style = Stroke(7f, cap = StrokeCap.Round))
                drawArc(Color(0xFFFFF4DC), -50f, 100f, false, P(40f, 14f), Size(48f, 72f), style = Stroke(7f, cap = StrokeCap.Round))
            } else {
                drawLine(Color(0xFFFF6A5A), P(62f, 36f), P(90f, 64f), 9f, StrokeCap.Round)
                drawLine(Color(0xFFFF6A5A), P(90f, 36f), P(62f, 64f), 9f, StrokeCap.Round)
            }
        }
        Ic.VIBRATE -> {
            drawRoundRect(Color(0xFFFFF4DC), P(34f, 12f), Size(32f, 76f), CornerRadius(8f))
            drawRoundRect(Color(0xFF2B1418), P(39f, 20f), Size(22f, 54f), CornerRadius(3f))
            drawArc(Color(0xFFFFB02E), 140f, 80f, false, P(10f, 30f), Size(16f, 40f), style = Stroke(6f, cap = StrokeCap.Round))
            drawArc(Color(0xFFFFB02E), -40f, 80f, false, P(74f, 30f), Size(16f, 40f), style = Stroke(6f, cap = StrokeCap.Round))
        }
        Ic.TROPHY -> {
            val c = Path()
            c.moveTo(26f, 10f); c.lineTo(74f, 10f); c.lineTo(70f, 46f); c.cubicTo(66f, 62f, 34f, 62f, 30f, 46f); c.close()
            drawPath(c, vg(0xFFFFE27A, 0xFFE09A00))
            lineP(c, Color(0xFF8A5A00), 4f)
            drawArc(Color(0xFFE09A00), 90f, 180f, false, P(8f, 14f), Size(24f, 26f), style = Stroke(6f))
            drawArc(Color(0xFFE09A00), -90f, 180f, false, P(68f, 14f), Size(24f, 26f), style = Stroke(6f))
            drawRect(Color(0xFFE09A00), P(44f, 58f), Size(12f, 18f))
            drawRoundRect(Color(0xFF8A5A00), P(28f, 76f), Size(44f, 14f), CornerRadius(4f))
        }
        Ic.REFRESH -> {
            drawArc(Color(0xFFFFF4DC), -30f, 270f, false, P(16f, 16f), Size(68f, 68f), style = Stroke(11f, cap = StrokeCap.Round))
            fillP(poly(66f, 4f, 94f, 22f, 62f, 34f), Color(0xFFFFF4DC))
        }
        Ic.MOMO -> {
            drawOval(Color(0xFFE9EEF2), P(6f, 62f), Size(88f, 30f))
            drawOval(Color(0xFF8996A3), P(6f, 62f), Size(88f, 30f), style = Stroke(3f))
            val m = Path()
            m.moveTo(16f, 72f); m.cubicTo(16f, 22f, 40f, 16f, 50f, 16f); m.cubicTo(60f, 16f, 84f, 22f, 84f, 72f); m.close()
            drawPath(m, vg(0xFFFFF6DC, 0xFFFFE2A6))
            lineP(m, Color(0xFFBE8E4A), 4f)
            for (k in -2..2) drawLine(Color(0xFFBE8E4A), P(50f + k * 7f, 24f), P(50f + k * 4f, 46f), 3f, StrokeCap.Round)
        }
        Ic.INFO -> {
            drawCircle(Color(0xFF35C6F4), 46f, P(50f, 50f))
            drawCircle(Color(0xFF0E5A78), 46f, P(50f, 50f), style = Stroke(4f))
            drawCircle(Color.White, 6f, P(50f, 28f))
            drawRoundRect(Color.White, P(43f, 42f), Size(14f, 36f), CornerRadius(5f))
        }
        Ic.HOME -> {
            val r = poly(8f, 48f, 50f, 10f, 92f, 48f)
            fillP(r, Color(0xFFD6382F))
            drawRect(Color(0xFFF3E4C2), P(20f, 48f), Size(60f, 42f))
            drawRect(Color(0xFF8E5B2E), P(42f, 62f), Size(16f, 28f))
            drawRect(Color(0xFF9AD7F5), P(26f, 56f), Size(11f, 11f))
            drawRect(Color(0xFF9AD7F5), P(63f, 56f), Size(11f, 11f))
        }
        Ic.PAUSE -> {
            drawRoundRect(Color(0xFFFFF4DC), P(24f, 16f), Size(20f, 68f), CornerRadius(6f))
            drawRoundRect(Color(0xFFFFF4DC), P(56f, 16f), Size(20f, 68f), CornerRadius(6f))
        }
    }
}
