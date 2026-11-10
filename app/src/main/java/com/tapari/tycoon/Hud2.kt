package com.tapari.tycoon

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

internal val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold),
    Font(R.font.poppins_extrabold, FontWeight.Black)
)

internal fun poppinsTypography(): Typography {
    val d = Typography()
    return Typography(
        displayLarge = d.displayLarge.copy(fontFamily = Poppins),
        displayMedium = d.displayMedium.copy(fontFamily = Poppins),
        displaySmall = d.displaySmall.copy(fontFamily = Poppins),
        headlineLarge = d.headlineLarge.copy(fontFamily = Poppins),
        headlineMedium = d.headlineMedium.copy(fontFamily = Poppins),
        headlineSmall = d.headlineSmall.copy(fontFamily = Poppins),
        titleLarge = d.titleLarge.copy(fontFamily = Poppins),
        titleMedium = d.titleMedium.copy(fontFamily = Poppins),
        titleSmall = d.titleSmall.copy(fontFamily = Poppins),
        bodyLarge = d.bodyLarge.copy(fontFamily = Poppins),
        bodyMedium = d.bodyMedium.copy(fontFamily = Poppins),
        bodySmall = d.bodySmall.copy(fontFamily = Poppins),
        labelLarge = d.labelLarge.copy(fontFamily = Poppins),
        labelMedium = d.labelMedium.copy(fontFamily = Poppins),
        labelSmall = d.labelSmall.copy(fontFamily = Poppins)
    )
}

/** Draws a vector glyph inside a canvas, centred at [center] with the given pixel size. */
internal fun DrawScope.drawGlyph(ic: Ic, center: Offset, size: Float, color: Color, rotDeg: Float = 0f) {
    val p = GlyphLib.composePath(ic.key) ?: return
    val vb = GlyphLib.vb(ic.key)
    val k = size / vb
    withTransform({
        translate(center.x - size / 2f, center.y - size / 2f)
        rotate(rotDeg, Offset(size / 2f, size / 2f))
        scale(k, k, Offset.Zero)
    }) { drawPath(p, color) }
}

private val COMBO_COLS = arrayOf(Color(0xFF6BE07B), Color(0xFFFFE066), Color(0xFFFFA726), Color(0xFFFF5C8A), Color(0xFFB56CFF))

@Composable
internal fun ComboPill(hud: Hud, pulse: Float) {
    val tier = (hud.combo / 5).coerceIn(0, 4)
    val c = COMBO_COLS[tier]
    Column(
        Modifier
            .scale(1f + 0.04f * pulse * (tier + 1))
            .background(PillBg, RoundedCornerShape(18.dp))
            .border(2.dp, c, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GIcon(Ic.FIRE, 20.dp, tint = c)
            Spacer(Modifier.width(6.dp))
            Text("COMBO x" + hud.combo, color = c, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            Text("+" + ((hud.comboMult - 1f) * 100f).toInt() + "% cash", color = Cream, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { hud.comboFrac },
            modifier = Modifier.width(170.dp).height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = c, trackColor = Color(0xFF4A2A2A)
        )
    }
}

@Composable
internal fun DashButton(hud: Hud, modifier: Modifier = Modifier, onDash: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) 0.9f else 1f, label = "dashsc")
    val ready = hud.dashReady
    val frac = (1f - hud.dashCd / 5f).coerceIn(0f, 1f)
    Box(
        modifier
            .size(74.dp)
            .scale(sc)
            .clip(CircleShape)
            .clickable(interactionSource = src, indication = null) { onDash() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val top = if (ready || hud.dashing) Color(0xFF6BE07B) else Color(0xFF4A3A3E)
            val bot = if (ready || hud.dashing) Color(0xFF1F9A4B) else Color(0xFF2A1A1E)
            drawCircle(Brush.verticalGradient(listOf(top, bot)), radius = size.minDimension / 2f - 4.dp.toPx())
            drawCircle(Color(0x66000000), radius = size.minDimension / 2f - 4.dp.toPx(), style = Stroke(2.dp.toPx()))
            val sw = 5.dp.toPx()
            drawArc(
                Color(0x33FFFFFF), -90f, 360f, false,
                topLeft = Offset(sw / 2f, sw / 2f), size = Size(size.width - sw, size.height - sw), style = Stroke(sw)
            )
            drawArc(
                if (ready) Color(0xFFFFE066) else Color(0xFFFFB02E), -90f, 360f * (if (ready) 1f else frac), false,
                topLeft = Offset(sw / 2f, sw / 2f), size = Size(size.width - sw, size.height - sw), style = Stroke(sw, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GIcon(Ic.DASH, 28.dp, tint = if (ready || hud.dashing) Color.White else Color(0xFFBBA9A2))
            Text("DASH", color = if (ready || hud.dashing) Color.White else Color(0xFFBBA9A2), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
internal fun LevelPill(hud: Hud) {
    Pill(border = Color(0xFFB39DDB)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GIcon(Ic.RANK, 16.dp)
            Spacer(Modifier.width(4.dp))
            Text("Lv " + hud.level, color = Cream, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(6.dp))
            LinearProgressIndicator(
                progress = { hud.levelFrac },
                modifier = Modifier.width(46.dp).height(5.dp).clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFB39DDB), trackColor = Color(0xFF4A2A2A)
            )
        }
    }
}

@Composable
internal fun VignetteOverlay() {
    Canvas(Modifier.fillMaxSize()) {
        val r = size.maxDimension * 0.75f
        drawRect(
            Brush.radialGradient(
                listOf(Color.Transparent, Color.Transparent, Color(0x55120A10)),
                center = Offset(size.width / 2f, size.height * 0.5f), radius = r
            )
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Daily streak
// ---------------------------------------------------------------------------------------------
@Composable
internal fun StreakCard(game: Game, audio: AudioFx) {
    val idx = game.streakDayIdx()
    val ready = game.streakReady()
    Column(Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GIcon(Ic.CALENDAR, 22.dp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Daily login streak", color = Cream, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(
                    if (game.streak > 0) "Current streak: " + game.streak + " day" + (if (game.streak == 1) "" else "s") else "Come back every day for bigger rewards!",
                    color = Muted, fontSize = 11.sp
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (d in 0 until 7) {
                val done = d < idx || (!ready && d == idx)
                val cur = ready && d == idx
                Column(
                    Modifier
                        .weight(1f)
                        .background(if (done) Color(0xFF2E5A3A) else if (cur) Color(0xFF5A4217) else Color(0xFF3A2429), RoundedCornerShape(10.dp))
                        .border(if (cur) 2.dp else 0.dp, if (cur) Gold else Color.Transparent, RoundedCornerShape(10.dp))
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("D" + (d + 1), color = if (done) Color(0xFF9CF0AE) else Cream, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    if (done) GIcon(Ic.CHECK, 16.dp) else GIcon(Ic.GEM, 16.dp)
                    Text(Game.STREAK_GEMS[d].toString(), color = Color(0xFF8BE6FF), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        BigButton(
            if (ready) "Claim Day " + (idx + 1) + ":  Rs " + fmtMoney(game.streakCash(idx)) + " + " + Game.STREAK_GEMS[idx] + " gems" else "Claimed today - see you tomorrow!",
            Modifier.fillMaxWidth(), enabled = ready,
            top = Color(0xFF6BE07B), bottom = Color(0xFF2E9E4F), icon = Ic.GIFT
        ) { game.claimStreak() }
    }
}

// ---------------------------------------------------------------------------------------------
// Achievements
// ---------------------------------------------------------------------------------------------
private val ACH_IC = arrayOf(
    Ic.MOMO, Ic.MOMO, Ic.CROWN, Ic.CUP, Ic.CAMERA, Ic.FIRE, Ic.FIRE, Ic.STAR, Ic.DASH, Ic.CASH, Ic.GLOBE, Ic.REFRESH, Ic.CHILI, Ic.HEART
)

@Composable
internal fun AwardsPanel(game: Game, audio: AudioFx, onClose: () -> Unit) {
    SheetPanel("Achievements", onClose) {
        LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Game.NACH) { i ->
                val v = game.achVal(i)
                val t = Game.ACH_TARGET[i]
                val done = v >= t
                val claimed = game.achClaimedOf(i)
                Row(
                    Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp))
                        .border(if (done && !claimed) 2.dp else 0.dp, if (done && !claimed) Gold else Color.Transparent, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GBadge(
                        ACH_IC[i], 44.dp,
                        if (done) Color(0xFFFFD54A) else Color(0xFF5A464B), if (done) Color(0xFFFF9F1C) else Color(0xFF3A2A2E),
                        glyph = if (done) Color(0xFF2B1418) else Color(0xFFBFA9A0)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(Game.ACH_NAME[i], color = Cream, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text(Game.ACH_DESC[i], color = Muted, fontSize = 11.sp)
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (v / t).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (done) GreenOk else Gold, trackColor = Color(0xFF4A2A2A)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    BigButton(
                        if (claimed) "Done" else if (done) "+" + Game.ACH_GEMS[i] else Game.ACH_GEMS[i].toString(),
                        enabled = done && !claimed,
                        top = Color(0xFF8BE6FF), bottom = Color(0xFF35A6D8), icon = Ic.GEM
                    ) { game.claimAch(i) }
                }
            }
            items(1) {
                Text("Best combo: x" + game.bestCombo + "    Golden momos: " + game.goldCount + "    Dashes: " + game.dashCount, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(6.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Lucky spin
// ---------------------------------------------------------------------------------------------
private val SEG_COLS = arrayOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFB8C00),
    Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFFD81B60), Color(0xFFFFB300)
)
private val PRIZE_IC = arrayOf(Ic.CASH, Ic.GEM, Ic.CUP, Ic.COIN, Ic.BOLT, Ic.GEM, Ic.CASH, Ic.CROWN)

@Composable
internal fun SpinPanel(game: Game, hud: Hud, audio: AudioFx, onClose: () -> Unit) {
    val rot = remember { Animatable(0f) }
    var spinning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            now = System.currentTimeMillis()
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { (rot.value / 45f).toInt() }.collect { k ->
            if (spinning) audio.sfx(Sfx.SPIN, 0.9f + (k % 4) * 0.08f, 0.5f)
        }
    }
    val ready = now >= game.nextSpinAt
    val left = if (ready) 0L else (game.nextSpinAt - now) / 1000L

    fun doSpin(paid: Boolean) {
        if (spinning) return
        val idx = game.spinWheel(paid)
        if (idx < 0) return
        spinning = true
        result = ""
        scope.launch {
            val cur = rot.value
            val base = kotlin.math.ceil(cur / 360f) * 360f
            val target = base + 360f * 5f + ((360 - idx * 45) % 360).toFloat()
            rot.animateTo(target, tween(4300, easing = FastOutSlowInEasing))
            spinning = false
            result = game.lastPrize
            audio.sfx(Sfx.ACHIEVE, 1f, 0.9f)
        }
    }

    SheetPanel("Lucky Spin", { if (!spinning) onClose() }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(262.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(262.dp)) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val r = size.minDimension / 2f - 10.dp.toPx()
                    drawCircle(Color(0xFFFFB02E), r + 8.dp.toPx(), c)
                    drawCircle(Color(0xFF2B1418), r + 3.dp.toPx(), c)
                    rotate(rot.value, c) {
                        for (i in 0 until 8) {
                            drawArc(
                                SEG_COLS[i], -90f - 22.5f + i * 45f, 45f, true,
                                topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f)
                            )
                        }
                        for (i in 0 until 8) {
                            val a = Math.toRadians(-90.0 + i * 45.0)
                            val p = Offset(c.x + cos(a).toFloat() * r * 0.66f, c.y + sin(a).toFloat() * r * 0.66f)
                            drawGlyph(PRIZE_IC[i], p, r * 0.34f, Color.White, i * 45f)
                        }
                    }
                    for (i in 0 until 8) {
                        val a = Math.toRadians(-90.0 - 22.5 + i * 45.0)
                        drawLine(
                            Color(0x88FFFFFF), c,
                            Offset(c.x + cos(a).toFloat() * r, c.y + sin(a).toFloat() * r), 2.dp.toPx()
                        )
                    }
                    drawCircle(Color(0xFF2B1418), 26.dp.toPx(), c)
                    drawCircle(Color(0xFFFFB02E), 22.dp.toPx(), c)
                    val tri = Path()
                    tri.moveTo(c.x - 16.dp.toPx(), c.y - r - 12.dp.toPx())
                    tri.lineTo(c.x + 16.dp.toPx(), c.y - r - 12.dp.toPx())
                    tri.lineTo(c.x, c.y - r + 20.dp.toPx())
                    tri.close()
                    drawPath(tri, Color(0xFFFF3B3B))
                    drawPath(tri, Color.White, style = Stroke(2.dp.toPx()))
                }
                GIcon(Ic.STAR, 26.dp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (spinning) "Spinning..." else if (result.isNotEmpty()) "You won: $result" else "Spin the wheel for rupees, gems and boosts!",
                color = if (result.isNotEmpty()) Gold else Cream, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            BigButton(
                if (ready) "FREE SPIN" else "Next free spin in " + (left / 60L) + ":" + (left % 60L).toString().padStart(2, '0'),
                Modifier.fillMaxWidth(), enabled = ready && !spinning,
                top = Color(0xFF6BE07B), bottom = Color(0xFF2E9E4F), icon = Ic.SPIN
            ) { doSpin(false) }
            Spacer(Modifier.height(8.dp))
            BigButton(
                "Spin now  (2 gems)", Modifier.fillMaxWidth(), enabled = hud.gems >= 2 && !spinning,
                top = Color(0xFF8BE6FF), bottom = Color(0xFF35A6D8), icon = Ic.GEM
            ) { doSpin(true) }
            Spacer(Modifier.height(8.dp))
            Text(
                "Prizes: cash bags, gems, Chiya Rush, Double Cash and a rare JACKPOT.",
                color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center
            )
        }
    }
}
