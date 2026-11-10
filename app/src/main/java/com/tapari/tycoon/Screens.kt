package com.tapari.tycoon

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TIPS = arrayOf(
    "Never let the Extra Jhol run dry. Angry customers leave without paying!",
    "Stand still near a crate for a moment to pick things up.",
    "Hire a Cashier Dai and he will collect the rupee piles for you.",
    "Chiya & Churot campers stay for hours. Ban camping or sell them Surya!",
    "VIP food vloggers bring a whole crowd. Serve them fast.",
    "Stand on a glowing pad to pay for new tables and stations.",
    "Tourist buses mean rush hour. Keep the Moktu full of momos!"
)

@Composable
private fun SkylineBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFF1B1030), Color(0xFF5A2A5A), Color(0xFFE8803A)), 0f, h))
        drawCircle(Color(0x55FFD27A), w * 0.45f, Offset(w * 0.5f, h * 0.72f))
        drawCircle(Color(0xFFFFE9A0), w * 0.12f, Offset(w * 0.5f, h * 0.7f))
        // himalayas
        val m = Path()
        m.moveTo(0f, h * 0.74f)
        m.lineTo(w * 0.18f, h * 0.55f); m.lineTo(w * 0.3f, h * 0.68f)
        m.lineTo(w * 0.5f, h * 0.48f); m.lineTo(w * 0.68f, h * 0.7f)
        m.lineTo(w * 0.85f, h * 0.56f); m.lineTo(w, h * 0.72f)
        m.lineTo(w, h); m.lineTo(0f, h); m.close()
        drawPath(m, Color(0xFF3B2A5E))
        // city blocks
        val cols = intArrayOf(0xFF2A1B45.toInt(), 0xFF332253.toInt(), 0xFF24173D.toInt())
        for (k in 0 until 11) {
            val bw = w / 10f
            val bh = h * (0.12f + 0.09f * ((k * 7) % 5) / 4f)
            drawRect(Color(cols[k % 3]), Offset(k * bw - bw * 0.2f, h * 0.9f - bh), androidx.compose.ui.geometry.Size(bw * 0.95f, bh + h * 0.1f))
            for (r in 0 until 3) for (c in 0 until 2) {
                if ((k + r * 3 + c) % 3 != 0) drawRect(Color(0xFFFFD27A), Offset(k * bw + c * bw * 0.4f, h * 0.9f - bh + r * bh * 0.28f + 10f), androidx.compose.ui.geometry.Size(bw * 0.14f, bh * 0.12f))
            }
        }
        // stupa silhouette
        val sx = w * 0.18f
        val sy = h * 0.9f
        drawRect(Color(0xFF3A2A60), Offset(sx - w * 0.07f, sy - h * 0.02f), androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.03f))
        drawCircle(Color(0xFF4A3A78), w * 0.055f, Offset(sx, sy - h * 0.025f))
        drawRect(Color(0xFFE0B23A), Offset(sx - w * 0.015f, sy - h * 0.085f), androidx.compose.ui.geometry.Size(w * 0.03f, h * 0.02f))
        val sp = Path()
        sp.moveTo(sx - w * 0.012f, sy - h * 0.085f); sp.lineTo(sx, sy - h * 0.13f); sp.lineTo(sx + w * 0.012f, sy - h * 0.085f); sp.close()
        drawPath(sp, Color(0xFFE0B23A))
    }
}

@Composable
private fun TitleBlock(big: Boolean) {
    val sh = Shadow(Color(0xCC000000), Offset(0f, 6f), 10f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "MOMO & CHIYA", color = Color(0xFFFFF4DC), fontSize = if (big) 30.sp else 26.sp, fontWeight = FontWeight.Black,
            style = TextStyle(fontFamily = Poppins, shadow = sh, letterSpacing = 3.sp)
        )
        Text(
            "EMPIRE", color = Gold, fontSize = if (big) 64.sp else 52.sp, fontWeight = FontWeight.Black, lineHeight = if (big) 62.sp else 50.sp,
            style = TextStyle(fontFamily = Poppins, shadow = sh, letterSpacing = 6.sp)
        )
        Box(Modifier.background(Color(0xFFD6382F), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 3.dp)) {
            Text("TAPARI TYCOON", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, style = TextStyle(fontFamily = Poppins, letterSpacing = 3.sp))
        }
    }
}

@Composable
fun LoadingScreen(progress: Float, message: String) {
    val inf = rememberInfiniteTransition(label = "load")
    val steam by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "steam")
    val tipIdx = remember { (System.currentTimeMillis() / 1000L % TIPS.size).toInt() }
    val p by animateFloatAsState(progress, tween(300), label = "prog")
    Box(Modifier.fillMaxSize()) {
        SkylineBackdrop(Modifier.fillMaxSize())
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.55f))
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.size(170.dp)) {
                Canvas(Modifier.fillMaxSize()) {
                    for (k in 0 until 4) {
                        val ph = (steam + k * 0.25f) % 1f
                        val x = size.width * (0.38f + 0.08f * k) + kotlin.math.sin((ph * 6.28f + k).toDouble()).toFloat() * 8f
                        val y = size.height * (0.24f - ph * 0.24f)
                        drawCircle(Color.White.copy(alpha = 0.55f * (1f - ph)), 10f + ph * 22f, Offset(x, y))
                    }
                }
                GIcon(Ic.STEAMER, 140.dp)
            }
            Spacer(Modifier.height(10.dp))
            TitleBlock(true)
            Spacer(Modifier.weight(1f))
            Text(message, color = Cream, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.72f)
                    .height(14.dp)
                    .background(Color(0x66000000), RoundedCornerShape(50))
                    .border(2.dp, Color(0x88FFFFFF), RoundedCornerShape(50))
                    .padding(2.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(p.coerceIn(0.03f, 1f))
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFFD54A), Color(0xFFFF8A1F))), RoundedCornerShape(50))
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                TIPS[tipIdx], color = Color(0xFFE9D8C8), fontSize = 12.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.8f).height(40.dp)
            )
        }
    }
}

@Composable
private fun RoundMenuBtn(ic: Ic, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(54.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF4A2D34), Color(0xFF2A171C))), CircleShape)
                .border(2.dp, Color(0x99FFB02E), CircleShape)
                .clip(CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) { GIcon(ic, 28.dp) }
        Spacer(Modifier.height(3.dp))
        Text(label, color = Cream, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MainMenu(game: Game, audio: AudioFx, hasSave: Boolean, onPlay: () -> Unit, onNewGame: () -> Unit) {
    var sheet by remember { mutableIntStateOf(0) }
    var confirmNew by remember { mutableStateOf(false) }
    val inf = rememberInfiniteTransition(label = "menu")
    val pulse by inf.animateFloat(0.97f, 1.04f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse")
    val bob by inf.animateFloat(-6f, 6f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "bob")
    Box(Modifier.fillMaxSize()) {
        // soft scrims so the live 3D shop shows through in the middle
        Box(Modifier.fillMaxWidth().height(300.dp).background(Brush.verticalGradient(listOf(Color(0xCC12081E), Color.Transparent))))
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(340.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD12081E))))
        )
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.padding(top = (bob / 2f + 6f).dp)) { TitleBlock(false) }
            Spacer(Modifier.weight(1f))
            if (hasSave) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(border = Gold) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.COIN, 22.dp); Spacer(Modifier.width(6.dp))
                            Text(fmtMoney(game.money), color = Cream, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Pill(border = Color(0xFF35C6F4)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.GEM, 18.dp); Spacer(Modifier.width(5.dp))
                            Text(game.gems.toString(), color = Color(0xFF8BE6FF), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Pill {
                        Text(Locs.NAME[game.loc], color = Cream, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            BigButton(
                if (hasSave) "CONTINUE" else "PLAY",
                Modifier.fillMaxWidth(0.78f).scale(pulse),
                icon = Ic.PLAY
            ) { audio.sfx(Sfx.UNLOCK, 1f, 0.6f); onPlay() }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                RoundMenuBtn(Ic.INFO, "How to play") { audio.sfx(Sfx.TAP, 1f, 0.5f); sheet = 1 }
                RoundMenuBtn(Ic.GEAR, "Settings") { audio.sfx(Sfx.TAP, 1f, 0.5f); sheet = 2 }
                if (hasSave) RoundMenuBtn(Ic.REFRESH, "New game") { audio.sfx(Sfx.TAP, 1f, 0.5f); confirmNew = true }
            }
            Spacer(Modifier.height(14.dp))
            Text("Baneshwor thela to global momo empire", color = Color(0xFFD9C3B5), fontSize = 11.sp)
        }

        if (sheet == 1) {
            SheetPanel("How to play", { sheet = 0 }) {
                val steps = arrayOf(
                    Triple(Ic.PLAY, "Drag anywhere to walk", "Your thumb makes a floating joystick. Stand still near a crate to pick up."),
                    Triple(Ic.STEAMER, "Make momos", "Carry Maida and Meat to the Prep table. The Moktu steams them into plates."),
                    Triple(Ic.MOMO, "Serve the tables", "Carry plates to tables with hungry customers and collect the rupee piles."),
                    Triple(Ic.CHILI, "Extra Jhol!", "Keep the Jhol cauldron full. Angry customers flip out and leave without paying."),
                    Triple(Ic.LOCK, "Unlock and expand", "Stand still on a glowing pad to pay. Start on the footpath, then buy the shop!"),
                    Triple(Ic.CUP, "Chiya & Churot", "Open the chiya corner. Campers stay for hours unless you ban or monetise them."),
                    Triple(Ic.CHEF, "Hire your crew", "Kancha, Jhol Bhai, Chiya Didi and Cashier Dai work for you, even while you are away.")
                )
                LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(steps.size) { i ->
                        Row(
                            Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(44.dp).background(Color(0xFF3A2429), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                GIcon(steps[i].first, 28.dp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(steps[i].second, color = Cream, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text(steps[i].third, color = Muted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
        if (sheet == 2) {
            SheetPanel("Settings", { sheet = 0 }) {
                var snd by remember { mutableStateOf(game.soundOn) }
                var hap by remember { mutableStateOf(game.hapticOn) }
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingRow(Ic.SOUND, "Sound & street ambience", snd) { snd = it; game.soundOn = it; audio.setSound(it) }
                    SettingRow(Ic.VIBRATE, "Haptic feedback", hap) { hap = it; game.hapticOn = it; if (it) audio.haptic(Hap.THUMP) }
                    Text("Momo & Chiya Empire - Tapari Tycoon. Made with jhol.", color = Color(0xFF8C7A74), fontSize = 11.sp)
                }
            }
        }
        if (confirmNew) {
            ModalCard {
                GIcon(Ic.REFRESH, 54.dp)
                Spacer(Modifier.height(8.dp))
                Text("Start a new game?", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(6.dp))
                Text("This erases your cash, gems, shops and upgrades.", color = Cream, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                BigButton("Erase and restart", Modifier.fillMaxWidth(), top = Color(0xFFFF8A80), bottom = Color(0xFFE53935), textColor = Color.White) {
                    confirmNew = false
                    onNewGame()
                }
                Spacer(Modifier.height(8.dp))
                BigButton("Cancel", Modifier.fillMaxWidth(), top = Color(0xFF5A4248), bottom = Color(0xFF3A2A2E), textColor = Cream) { confirmNew = false }
            }
        }
    }
}
