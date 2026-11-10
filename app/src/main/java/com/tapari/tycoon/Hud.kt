package com.tapari.tycoon

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

internal val Gold = Color(0xFFFFB02E)
internal val Cream = Color(0xFFFFF4DC)
internal val GreenOk = Color(0xFF3FBF5A)
internal val RedAlert = Color(0xFFE53935)
internal val PanelBg = Color(0xF21F1216)
internal val CardBg = Color(0xFF2E1D22)
internal val PillBg = Color(0xD9170D10)
internal val Muted = Color(0xFFBFA9A0)
internal val Ink = Color(0xFF2B1418)

@Composable
fun TapariTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(primary = Gold, onPrimary = Ink, secondary = GreenOk, surface = PanelBg),
        typography = poppinsTypography(),
        content = content
    )
}

@Composable
internal fun Pill(modifier: Modifier = Modifier, border: Color = Color(0x66FFFFFF), content: @Composable () -> Unit) {
    Box(
        modifier
            .background(PillBg, RoundedCornerShape(50))
            .border(1.5.dp, border, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
internal fun BigButton(
    text: String,
    modifier: Modifier = Modifier,
    top: Color = Color(0xFFFFD54A),
    bottom: Color = Color(0xFFFF9F1C),
    textColor: Color = Ink,
    enabled: Boolean = true,
    icon: Ic? = null,
    onClick: () -> Unit
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) 0.95f else 1f, label = "btn")
    Box(
        modifier
            .scale(sc)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .border(2.dp, Color(0x55000000), RoundedCornerShape(16.dp))
            .clickable(interactionSource = src, indication = null, enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                GIcon(icon, 22.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(text, color = textColor, fontWeight = FontWeight.Black, fontSize = 15.sp)
        }
    }
}

@Composable
private fun DockBtn(ic: Ic, label: String, badge: String, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) 0.92f else 1f, label = "dock")
    Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.scale(sc)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(interactionSource = src, indication = null) { onClick() }
                .padding(horizontal = 5.dp, vertical = 4.dp)
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(Brush.verticalGradient(listOf(Color(0xFF4A2D34), Color(0xFF2A171C))), RoundedCornerShape(15.dp))
                    .border(1.5.dp, Color(0x88FFB02E), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) { GIcon(ic, 26.dp) }
            Spacer(Modifier.height(2.dp))
            Text(label, color = Cream, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        if (badge.isNotEmpty()) {
            Box(
                Modifier
                    .padding(top = 0.dp, end = 4.dp)
                    .size(18.dp)
                    .background(RedAlert, CircleShape)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black) }
        }
    }
}

private fun locIcon(l: Int): Ic = when (l) {
    0 -> Ic.CART
    1 -> Ic.SHOP
    2 -> Ic.ROOF
    else -> Ic.GLOBE
}

private val UP_ICON = arrayOf(Ic.SHOE, Ic.BAG, Ic.TAG, Ic.STEAMER, Ic.CHILI, Ic.CHEF, Ic.POT, Ic.CUP, Ic.CASH, Ic.NOCAMP, Ic.SMOKE, Ic.MAGNET, Ic.FIRE, Ic.DASH)

@Composable
fun GameHud(game: Game, hud: Hud, audio: AudioFx, pauseSignal: Int, onMainMenu: () -> Unit, saveNow: () -> Unit) {
    var panel by remember { mutableIntStateOf(0) }
    var tab by remember { mutableIntStateOf(0) }
    var jActive by remember { mutableStateOf(false) }
    var jOrigin by remember { mutableStateOf(Offset.Zero) }
    var jThumb by remember { mutableStateOf(Offset.Zero) }
    var moved by remember { mutableStateOf(false) }
    var toastVisible by remember { mutableStateOf(false) }
    var confirmPrestige by remember { mutableStateOf(false) }
    val inf = rememberInfiniteTransition(label = "pulse")
    val flash by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(380), RepeatMode.Reverse), label = "flash")
    val breathe by inf.animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "breathe")

    LaunchedEffect(hud.toastSeq) {
        if (hud.toast.isNotEmpty()) {
            toastVisible = true
            delay(2800)
            toastVisible = false
        }
    }
    LaunchedEffect(pauseSignal) {
        if (pauseSignal > 0) panel = 4
    }
    LaunchedEffect(panel) {
        game.inX = 0f
        game.inY = 0f
        game.paused = panel == 4
    }

    Box(Modifier.fillMaxSize()) {
        // ---------------- floating joystick ----------------
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (down.position.y < size.height * 0.14f) return@awaitEachGesture
                        if (down.position.y > size.height - 96.dp.toPx()) return@awaitEachGesture
                        var origin = down.position
                        val maxR = 54.dp.toPx()
                        jOrigin = origin
                        jThumb = origin
                        jActive = true
                        val id = down.id
                        while (true) {
                            val ev = awaitPointerEvent()
                            val ch = ev.changes.firstOrNull { it.id == id } ?: break
                            if (!ch.pressed) break
                            var d = ch.position - origin
                            val len = d.getDistance()
                            if (len > maxR) {
                                // the stick base follows the thumb so direction changes are always instant
                                origin += d / len * (len - maxR)
                                jOrigin = origin
                                d = ch.position - origin
                            }
                            jThumb = origin + d
                            val mag = (d.getDistance() / maxR).coerceIn(0f, 1f)
                            if (mag < 0.12f) {
                                game.inX = 0f
                                game.inY = 0f
                            } else {
                                val m = ((mag - 0.12f) / 0.88f).coerceIn(0f, 1f)
                                val dl = d.getDistance()
                                game.inX = d.x / dl * m
                                game.inY = -(d.y / dl * m)
                                moved = true
                            }
                            ch.consume()
                        }
                        jActive = false
                        game.inX = 0f
                        game.inY = 0f
                    }
                }
        )
        Canvas(Modifier.fillMaxSize()) {
            val r = 54.dp.toPx()
            if (jActive) {
                drawCircle(Color.Black.copy(alpha = 0.16f), r, jOrigin)
                drawCircle(Color.White.copy(alpha = 0.55f), r, jOrigin, style = Stroke(2.5.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.25f), r * 0.55f, jOrigin, style = Stroke(1.5.dp.toPx()))
                drawCircle(Color(0xFF39E05B).copy(alpha = 0.35f), 30.dp.toPx(), jThumb)
                drawCircle(Color.White.copy(alpha = 0.92f), 21.dp.toPx(), jThumb)
                drawCircle(Color(0xFF39E05B), 21.dp.toPx(), jThumb, style = Stroke(3.dp.toPx()))
            } else if (!moved) {
                val c = Offset(size.width / 2f, size.height * 0.72f)
                drawCircle(Color.White.copy(alpha = 0.18f), r * breathe, c, style = Stroke(3.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.3f), 18.dp.toPx(), c)
            }
        }

        VignetteOverlay()

        // ---------------- dash button ----------------
        DashButton(
            hud,
            Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(end = 14.dp, bottom = 112.dp)
        ) { if (game.dash()) { moved = true } }

        // ---------------- top area ----------------
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(Modifier.clickable { panel = 2 }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GIcon(locIcon(hud.loc), 20.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(Locs.NAME[hud.loc], color = Cream, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // objective card
                Column(
                    Modifier
                        .width(176.dp)
                        .background(PillBg, RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GIcon(Ic.STAR, 16.dp)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            hud.goal, color = Cream, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 2,
                            overflow = TextOverflow.Ellipsis, lineHeight = 13.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { hud.goalFrac },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (hud.goalFrac >= 1f) GreenOk else Gold, trackColor = Color(0xFF4A2A2A)
                    )
                }
                if (hud.jhol < 0.8f || hud.jholAlert) {
                    val low = hud.jholLow
                    Pill(Modifier.width(176.dp), border = if (low) RedAlert else Color(0x66FFFFFF)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.CHILI, 18.dp, Modifier.alpha(if (low) 0.55f + 0.45f * flash else 1f))
                            Spacer(Modifier.width(6.dp))
                            LinearProgressIndicator(
                                progress = { hud.jhol },
                                modifier = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)),
                                color = if (low) RedAlert else Color(0xFFFF7043), trackColor = Color(0xFF4A2A2A)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(hud.jholPct.toString() + "%", color = Cream, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(border = Gold) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GIcon(Ic.COIN, 26.dp)
                        Spacer(Modifier.width(7.dp))
                        Text(fmtMoney(hud.money), color = Cream, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(border = Color(0xFF35C6F4)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.GEM, 18.dp)
                            Spacer(Modifier.width(5.dp))
                            Text(hud.gems.toString(), color = Color(0xFF8BE6FF), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Box(
                        Modifier
                            .size(36.dp)
                            .background(PillBg, CircleShape)
                            .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
                            .clip(CircleShape)
                            .clickable { audio.sfx(Sfx.TAP, 1f, 0.5f); panel = 4 },
                        contentAlignment = Alignment.Center
                    ) { GIcon(Ic.PAUSE, 18.dp) }
                }
                LevelPill(hud)
                if (hud.dbl > 0) {
                    Pill(border = Color(0xFF6BE07B)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.COIN, 15.dp)
                            Spacer(Modifier.width(4.dp))
                            Text("Double cash ${hud.dbl}s", color = Color(0xFF9CF0AE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (hud.gold) {
                    Pill(border = Color(0xFFFFD54A)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.MOMO, 15.dp, tint = Color(0xFFFFD54A))
                            Spacer(Modifier.width(4.dp))
                            Text("Golden Momo!", color = Color(0xFFFFE066), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (hud.rush > 0) {
                    Pill(border = Color(0xFFFFD54A)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.BOLT, 15.dp)
                            Spacer(Modifier.width(4.dp))
                            Text("Rush ${hud.rush}s", color = Color(0xFFFFE066), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (hud.dark > 0) {
                    Pill(border = Color(0xFFB39DDB)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.BOLT, 15.dp)
                            Spacer(Modifier.width(4.dp))
                            Text("Load-shedding ${hud.dark}s  tips x1.5", color = Color(0xFFD1C4E9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (hud.boost > 0) {
                    Pill(border = GreenOk) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.CUP, 15.dp)
                            Spacer(Modifier.width(4.dp))
                            Text("Chiya Rush ${hud.boost}s", color = Color(0xFF9CF0AE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (hud.vip > 0) {
                    Pill(border = Color(0xFFFFE066)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GIcon(Ic.STAR, 15.dp)
                            Spacer(Modifier.width(4.dp))
                            Text("VIP here", color = Color(0xFFFFE066), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ---------------- alert + toast (stacked, below the top bars) ----------------
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = 168.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hud.combo >= 2) ComboPill(hud, flash)
            if (hud.jholAlert) {
                Row(
                    Modifier
                        .scale(1f + 0.07f * flash)
                        .background(RedAlert.copy(alpha = 0.8f + 0.2f * flash), RoundedCornerShape(16.dp))
                        .border(2.5.dp, Color.White, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GIcon(Ic.CHILI, 22.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("EXTRA JHOL EMPTY!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                }
            }
            if (toastVisible && hud.toast.isNotEmpty()) {
                Box(
                    Modifier
                        .background(Color(0xE61F1216), RoundedCornerShape(14.dp))
                        .border(1.5.dp, Gold, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(hud.toast, color = Cream, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }

        // ---------------- first-run hint ----------------
        if (!moved) {
            Text(
                "Drag anywhere to move", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center).padding(top = 260.dp)
            )
        }

        // ---------------- bottom: carry chip, hint, dock ----------------
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = 12.dp, bottom = 92.dp)
        ) {
            val full = hud.carry >= hud.cap
            Pill(border = if (full) Color(0xFFFFE066) else Color(0x66FFFFFF)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GIcon(Ic.BAG, 18.dp)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        if (full) "MAX ${hud.carry}/${hud.cap}" else "${hud.carry}/${hud.cap}",
                        color = if (full) Color(0xFFFFE066) else Cream, fontSize = 13.sp, fontWeight = FontWeight.Black
                    )
                }
            }
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (hud.hint.isNotEmpty()) {
                Box(Modifier.background(Color(0xA6000000), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                    Text(hud.hint, color = Cream, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                }
            }
            var canUp = false
            for (id in intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 12, 13)) {
                if (game.upg[game.loc][id] < UpDef.MAX[id] && game.money >= game.upgCost(game.loc, id)) { canUp = true; break }
            }
            Row(
                Modifier
                    .background(Color(0xCC170D10), RoundedCornerShape(22.dp))
                    .border(1.5.dp, Color(0x55FFFFFF), RoundedCornerShape(22.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                DockBtn(Ic.UP, "Upgrade", if (canUp) "!" else "") { audio.sfx(Sfx.TAP, 1f, 0.5f); tab = 0; panel = 1 }
                DockBtn(Ic.MAP, "Franchise", "") { audio.sfx(Sfx.TAP, 1f, 0.5f); panel = 2 }
                DockBtn(Ic.CALENDAR, "Daily", if (hud.ready > 0 || hud.streakReady) (if (hud.ready > 0) hud.ready.toString() else "!") else "") { audio.sfx(Sfx.TAP, 1f, 0.5f); panel = 3 }
                DockBtn(Ic.SPIN, "Spin", if (hud.spinReady) "!" else "") { audio.sfx(Sfx.TAP, 1f, 0.5f); panel = 5 }
                DockBtn(Ic.TROPHY, "Awards", if (hud.achReady > 0) hud.achReady.toString() else "") { audio.sfx(Sfx.TAP, 1f, 0.5f); panel = 6 }
                DockBtn(Ic.GEM, "Perks", if (hud.canPrestige) "!" else "") { audio.sfx(Sfx.TAP, 1f, 0.5f); tab = 3; panel = 1 }
            }
        }

        // ---------------- panels ----------------
        if (panel == 1) {
            SheetPanel("Upgrades", { panel = 0 }) {
                val names = arrayOf("Skills", "Staff", "Chiya", "Perks")
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 0 until 4) {
                        val sel = tab == i
                        Box(
                            Modifier
                                .weight(1f)
                                .background(if (sel) Gold else CardBg, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { tab = i; audio.sfx(Sfx.TAP, 1.2f, 0.4f) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(names[i], color = if (sel) Ink else Cream, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                val list = when (tab) {
                    0 -> intArrayOf(Up.SPEED, Up.CARRY, Up.PRICE, Up.MOKTU, Up.CAULDRON, Up.MAGNET, Up.COMBOWIN, Up.DASHCD)
                    1 -> intArrayOf(Up.KANCHA, Up.JHOLBHAI, Up.DIDI, Up.DAI)
                    2 -> intArrayOf(Up.NOCAMP, Up.UPSELL)
                    else -> intArrayOf()
                }
                if (tab == 3) {
                    GemTab(game, hud, audio, onPrestige = { confirmPrestige = true })
                } else {
                    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(list.size) { idx ->
                            UpgradeCard(game, list[idx], audio)
                        }
                        if (tab == 2) {
                            items(1) {
                                Text(
                                    if (game.st[Layout.ID_KETTLE].unlocked) "Choose wisely: you can only pick ONE way to deal with the Chiya & Churot campers."
                                    else if (!hud.bldg) "Buy the shop building first, then unlock the Chiya Kettle pad to open the Chiya & Churot corner!"
                                    else "Unlock the Chiya Kettle pad (right side of the shop) to open the Chiya & Churot corner!",
                                    color = Muted, fontSize = 12.sp, modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        if (panel == 2) {
            SheetPanel("Franchise Map", { panel = 0 }) {
                LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Locs.COUNT) { l ->
                        val unlocked = ((game.locMask shr l) and 1) == 1
                        val current = hud.loc == l
                        val can = unlocked || game.money >= Locs.COST[l]
                        Row(
                            Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp))
                                .border(2.dp, if (current) GreenOk else Color.Transparent, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GIcon(locIcon(l), 40.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(Locs.NAME[l], color = Cream, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text(Locs.TAG[l], color = Muted, fontSize = 11.sp)
                                Text("Income x" + fmtMoney(Locs.MULT[l]), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            BigButton(
                                if (current) "HERE" else if (unlocked) "Travel" else "Rs " + fmtMoney(Locs.COST[l]),
                                enabled = can && !current,
                                top = if (unlocked) Color(0xFF6BE07B) else Color(0xFFFFD54A),
                                bottom = if (unlocked) Color(0xFF2E9E4F) else Color(0xFFFF9F1C),
                                icon = if (!unlocked) Ic.LOCK else null
                            ) {
                                if (game.travel(l)) {
                                    audio.sfx(Sfx.UNLOCK, 1f, 0.6f)
                                    panel = 0
                                }
                            }
                        }
                    }
                }
            }
        }
        if (panel == 3) {
            SheetPanel("Daily Rewards", { panel = 0 }) {
                LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(1) { StreakCard(game, audio) }
                    items(3) { i ->
                        val prog = game.chProg[i]
                        val target = game.chTarget[i]
                        val done = game.chDone[i]
                        val ready = !done && prog >= target
                        Column(Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(game.chLabel(i), color = Cream, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(fmtMoney(minOf(prog, target)) + " / " + fmtMoney(target) + "   Reward: " + game.chReward[i], color = Muted, fontSize = 12.sp)
                                        Spacer(Modifier.width(4.dp))
                                        GIcon(Ic.GEM, 14.dp)
                                    }
                                }
                                BigButton(
                                    if (done) "Claimed" else if (ready) "Claim" else "...",
                                    enabled = ready,
                                    top = Color(0xFF6BE07B), bottom = Color(0xFF2E9E4F)
                                ) { if (game.claimChallenge(i)) audio.sfx(Sfx.UNLOCK, 1f, 0.6f) }
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (prog / target).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = GreenOk, trackColor = Color(0xFF4A2A2A)
                            )
                        }
                    }
                    items(1) {
                        Text("Challenges refresh every day. Keep the jhol flowing!", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
        if (panel == 5) SpinPanel(game, hud, audio) { panel = 0 }
        if (panel == 6) AwardsPanel(game, audio) { panel = 0 }
        if (panel == 4) {
            SheetPanel("Paused", { panel = 0 }) {
                var snd by remember { mutableStateOf(game.soundOn) }
                var hap by remember { mutableStateOf(game.hapticOn) }
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigButton("Resume", Modifier.fillMaxWidth(), icon = Ic.PLAY) { panel = 0 }
                    SettingRow(Ic.SOUND, "Sound & street ambience", snd) { snd = it; game.soundOn = it; audio.setSound(it) }
                    SettingRow(Ic.VIBRATE, "Haptic feedback", hap) { hap = it; game.hapticOn = it; if (it) audio.haptic(Hap.THUMP) }
                    Text(
                        "Plates served: ${game.platesServed}   Chiya served: ${game.chiyaServed}\nVIPs: ${game.vipServed}   Angry customers: ${game.angryLeft}\nLifetime earnings: Rs ${fmtMoney(game.lifetime)}",
                        color = Muted, fontSize = 12.sp
                    )
                    Text(
                        "Icons: Google Material Icons (Apache 2.0) and game-icons.net (CC BY 3.0) by " + GlyphData.CREDITS + ". Font: Poppins (SIL OFL).",
                        color = Color(0xFF8A766E), fontSize = 9.sp, lineHeight = 12.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BigButton("Save", Modifier.weight(1f), top = Color(0xFF8BE6FF), bottom = Color(0xFF35A6D8), icon = Ic.CHECK) { saveNow() }
                        BigButton("Main Menu", Modifier.weight(1f), top = Color(0xFFFF8A80), bottom = Color(0xFFE53935), textColor = Color.White, icon = Ic.HOME) {
                            onMainMenu()
                        }
                    }
                }
            }
        }

        // ---------------- dialogs ----------------
        if (game.offlineEarn > 0.0) {
            val hrs = game.offlineSecs / 3600.0
            ModalCard {
                GIcon(Ic.COIN, 64.dp)
                Spacer(Modifier.height(8.dp))
                Text("Welcome back, Boss!", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Your thela kept the steam going for " + String.format(java.util.Locale.US, "%.1f", hrs) +
                        " hours and earned Rs " + fmtMoney(game.offlineEarn) + " while you were away.",
                    color = Cream, fontSize = 14.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                BigButton("Collect", Modifier.fillMaxWidth(), icon = Ic.CASH) { game.claimOffline(false) }
                Spacer(Modifier.height(8.dp))
                BigButton(
                    "Collect x2  (2 gems)", Modifier.fillMaxWidth(), enabled = game.gems >= 2,
                    top = Color(0xFF8BE6FF), bottom = Color(0xFF35A6D8), icon = Ic.GEM
                ) { game.claimOffline(true) }
            }
        }
        if (confirmPrestige) {
            ModalCard {
                GIcon(Ic.REFRESH, 54.dp)
                Spacer(Modifier.height(8.dp))
                Text("Franchise Rebirth?", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Reset your shops, upgrades and cash for +" + game.prestigeGain() + " gems. Each gem permanently boosts all income by 5%.",
                    color = Cream, fontSize = 14.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                BigButton("Rebirth!", Modifier.fillMaxWidth(), top = Color(0xFFFF8A80), bottom = Color(0xFFE53935), textColor = Color.White) {
                    game.prestige(); confirmPrestige = false; panel = 0
                }
                Spacer(Modifier.height(8.dp))
                BigButton("Not yet", Modifier.fillMaxWidth(), top = Color(0xFF5A4248), bottom = Color(0xFF3A2A2E), textColor = Cream) { confirmPrestige = false }
            }
        }
    }
}

@Composable
internal fun SettingRow(ic: Ic, label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GIcon(ic, 24.dp)
        Spacer(Modifier.width(10.dp))
        Text(label, color = Cream, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
internal fun ModalCard(content: @Composable ColumnScope.() -> Unit) {
    val none = remember { MutableInteractionSource() }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(interactionSource = none, indication = null) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .padding(28.dp)
                .background(Color(0xFF241519), RoundedCornerShape(24.dp))
                .border(2.dp, Gold, RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
internal fun SheetPanel(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val none = remember { MutableInteractionSource() }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(interactionSource = none, indication = null) { onClose() }
    ) {
        Surface(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.74f)
                .clickable(interactionSource = none, indication = null) { },
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            color = PanelBg,
            border = BorderStroke(2.dp, Gold)
        ) {
            Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Box(
                        Modifier.size(36.dp).background(CardBg, CircleShape).clip(CircleShape).clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) { GIcon(Ic.CLOSE, 16.dp) }
                }
                content()
            }
        }
    }
}

@Composable
private fun UpgradeCard(game: Game, id: Int, audio: AudioFx) {
    val l = game.upg[game.loc][id]
    val max = UpDef.MAX[id]
    val cost = game.upgCost(game.loc, id)
    val maxed = l >= max
    val can = !maxed && game.money >= cost
    Row(
        Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(46.dp).background(Color(0xFF3A2429), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) { GIcon(UP_ICON[id], 30.dp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(UpDef.NAME[id], color = Cream, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(UpDef.DESC[id], color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { l.toFloat() / max },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = GreenOk, trackColor = Color(0xFF4A2A2A)
            )
            Text(if (max == 1) (if (l > 0) "Active" else "Not bought") else "Lv $l / $max", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        BigButton(
            if (maxed) (if (max == 1) "BOUGHT" else "MAX") else fmtMoney(cost),
            enabled = can,
            icon = if (!maxed) Ic.COIN else null
        ) { if (game.buyUpgrade(id)) audio.sfx(Sfx.UPGRADE, 1f, 0.5f) }
    }
}

@Composable
private fun GemTab(game: Game, hud: Hud, audio: AudioFx, onPrestige: () -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GIcon(Ic.GEM, 34.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("${hud.gems} gems  =  +${hud.gems * 5}% income", color = Color(0xFF8BE6FF), fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text("Earn gems from VIP guests, daily challenges and franchise rebirth.", color = Muted, fontSize = 11.sp)
            }
        }
        Row(
            Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GIcon(Ic.CUP, 34.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Chiya Rush", color = Cream, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text("90s of double crowds and faster hustle.", color = Muted, fontSize = 11.sp)
            }
            BigButton(
                "3 gems", enabled = hud.gems >= 3 && hud.boost == 0,
                top = Color(0xFF8BE6FF), bottom = Color(0xFF35A6D8), icon = Ic.GEM
            ) { if (game.buyBoost()) audio.sfx(Sfx.UPGRADE, 1.2f, 0.5f) }
        }
        Row(
            Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GIcon(Ic.REFRESH, 34.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Franchise Rebirth", color = Cream, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(
                    if (hud.canPrestige) "Reset for +${game.prestigeGain()} gems (permanent +5% income each)."
                    else "Earn Rs 20K in this run to unlock (now Rs ${fmtMoney(game.runEarned)}).",
                    color = Muted, fontSize = 11.sp
                )
            }
            BigButton(
                "Rebirth", enabled = hud.canPrestige,
                top = Color(0xFFFF8A80), bottom = Color(0xFFE53935), textColor = Color.White, onClick = onPrestige
            )
        }
    }
}
