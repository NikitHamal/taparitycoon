package com.tapari.tycoon

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Gold = Color(0xFFFFB02E)
private val Cream = Color(0xFFFFF4DC)
private val GreenOk = Color(0xFF3FBF5A)
private val RedAlert = Color(0xFFE53935)
private val PanelBg = Color(0xF21F1216)
private val CardBg = Color(0xFF2E1D22)
private val PillBg = Color(0xCC1B1013)

@Composable
fun TapariTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(primary = Gold, onPrimary = Color(0xFF2B1418), secondary = GreenOk, surface = PanelBg),
        content = content
    )
}

@Composable
private fun Pill(modifier: Modifier = Modifier, border: Color = Gold, content: @Composable () -> Unit) {
    Box(
        modifier
            .background(PillBg, RoundedCornerShape(50))
            .border(2.dp, border, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun RoundBtn(emoji: String, label: String, badge: Int, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(54.dp)
                    .background(PillBg, CircleShape)
                    .border(2.dp, Gold, CircleShape)
                    .clip(CircleShape)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 26.sp) }
            Text(label, color = Cream, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        if (badge > 0) {
            Box(Modifier.size(20.dp).background(RedAlert, CircleShape), contentAlignment = Alignment.Center) {
                Text(badge.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun GameHud(game: Game, hud: Hud, audio: AudioFx, saveNow: () -> Unit) {
    var panel by remember { mutableIntStateOf(0) }
    var tab by remember { mutableIntStateOf(0) }
    var jActive by remember { mutableStateOf(false) }
    var jOrigin by remember { mutableStateOf(Offset.Zero) }
    var jThumb by remember { mutableStateOf(Offset.Zero) }
    var toastVisible by remember { mutableStateOf(false) }
    var confirmPrestige by remember { mutableStateOf(false) }
    val inf = rememberInfiniteTransition(label = "pulse")
    val flash by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(380), RepeatMode.Reverse), label = "flash")
    val breathe by inf.animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "breathe")

    LaunchedEffect(hud.toastSeq) {
        if (hud.toast.isNotEmpty()) {
            toastVisible = true
            delay(2600)
            toastVisible = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        // ---------------- floating joystick ----------------
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (down.position.y < size.height * 0.16f) return@awaitEachGesture
                        val origin = down.position
                        val maxR = 62.dp.toPx()
                        jOrigin = origin
                        jThumb = origin
                        jActive = true
                        val id = down.id
                        while (true) {
                            val ev = awaitPointerEvent()
                            val ch = ev.changes.firstOrNull { it.id == id } ?: break
                            if (!ch.pressed) break
                            val d = ch.position - origin
                            val len = d.getDistance()
                            val k = if (len > maxR) maxR / len else 1f
                            jThumb = origin + d * k
                            game.inX = d.x * k / maxR
                            game.inY = -(d.y * k / maxR)
                            ch.consume()
                        }
                        jActive = false
                        game.inX = 0f
                        game.inY = 0f
                    }
                }
        )
        Canvas(Modifier.fillMaxSize()) {
            val r = 62.dp.toPx()
            if (jActive) {
                drawCircle(Color.White.copy(alpha = 0.14f), r, jOrigin)
                drawCircle(Color.White.copy(alpha = 0.7f), r, jOrigin, style = Stroke(3.dp.toPx()))
                drawCircle(Color(0xFF39E05B).copy(alpha = 0.35f), 30.dp.toPx(), jThumb)
                drawCircle(Color.White.copy(alpha = 0.9f), 22.dp.toPx(), jThumb)
            } else {
                val c = Offset(size.width / 2f, size.height * 0.86f)
                drawCircle(Color.White.copy(alpha = 0.22f * breathe - 0.1f), r * breathe, c, style = Stroke(3.dp.toPx()))
            }
        }

        // ---------------- top bar ----------------
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(Modifier.clickable { panel = 2 }) {
                    Text(Locs.EMOJI[hud.loc] + " " + Locs.NAME[hud.loc], color = Cream, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                val low = hud.jholLow
                Pill(Modifier.width(168.dp), border = if (low) RedAlert else Gold) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("\uD83C\uDF36\uFE0F Extra Jhol", color = if (low) Color(0xFFFF8A80) else Cream, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.alpha(if (low) 0.55f + 0.45f * flash else 1f))
                            Spacer(Modifier.weight(1f))
                            Text((hud.jhol * 100).toInt().toString() + "%", color = Cream, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { hud.jhol },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (low) RedAlert else Color(0xFFFF7043),
                            trackColor = Color(0xFF4A2A2A)
                        )
                    }
                }
                if (hud.vip > 0) {
                    Pill(border = Color(0xFFFFE066)) {
                        Text("\u2B50 VIP in the shop!", color = Color(0xFFFFE066), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(26.dp).background(Gold, CircleShape), contentAlignment = Alignment.Center) {
                            Text("Rs", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF5A3A00))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(fmtMoney(hud.money), color = Cream, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
                Pill(border = Color(0xFF35C6F4)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\uD83D\uDC8E", fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(hud.gems.toString(), color = Color(0xFF8BE6FF), fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
                if (hud.boost > 0) {
                    Pill(border = GreenOk) {
                        Text("\u2615 Chiya Rush ${hud.boost}s", color = Color(0xFF9CF0AE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ---------------- EXTRA JHOL EMPTY alert ----------------
        if (hud.jholAlert) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(top = 150.dp)
                    .scale(1f + 0.08f * flash)
                    .background(RedAlert.copy(alpha = 0.75f + 0.25f * flash), RoundedCornerShape(16.dp))
                    .border(3.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("\u26A0 EXTRA JHOL EMPTY! \u26A0", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            }
        }

        // ---------------- toast ----------------
        if (toastVisible && hud.toast.isNotEmpty()) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(top = 212.dp)
                    .background(Color(0xE61F1216), RoundedCornerShape(14.dp))
                    .border(2.dp, Gold, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(hud.toast, color = Cream, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }

        // ---------------- bottom: hint, carry, buttons ----------------
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = 150.dp, start = 80.dp, end = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hud.hint.isNotEmpty()) {
                Box(Modifier.background(Color(0xB3000000), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(hud.hint, color = Cream, fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(14.dp)
        ) {
            val full = hud.carry >= hud.cap
            Pill(border = if (full) Color(0xFFFFE066) else Gold) {
                Text(
                    if (full) "\uD83C\uDF92 MAX ${hud.carry}/${hud.cap}" else "\uD83C\uDF92 ${hud.carry}/${hud.cap}",
                    color = if (full) Color(0xFFFFE066) else Cream, fontSize = 14.sp, fontWeight = FontWeight.Black
                )
            }
        }
        Column(
            Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RoundBtn("\u2699\uFE0F", "Settings", 0) { audio.sfx(Sfx.TAP, 1f, 0.5f); panel = 4 }
            RoundBtn("\uD83D\uDCC5", "Daily", hud.ready) { audio.sfx(Sfx.TAP, 1f, 0.5f); panel = 3 }
            RoundBtn("\uD83D\uDDFA\uFE0F", "Map", 0) { audio.sfx(Sfx.TAP, 1f, 0.5f); panel = 2 }
            RoundBtn("\u2B06\uFE0F", "Upgrade", 0) { audio.sfx(Sfx.TAP, 1f, 0.5f); panel = 1 }
        }

        // ---------------- panels ----------------
        if (panel == 1) {
            SheetPanel("Upgrades - " + Locs.NAME[hud.loc], { panel = 0 }) {
                val names = arrayOf("Skills", "Staff", "Chiya", "Gems")
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
                            Text(names[i], color = if (sel) Color(0xFF2B1418) else Cream, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                val list = when (tab) {
                    0 -> intArrayOf(Up.SPEED, Up.CARRY, Up.PRICE, Up.MOKTU, Up.CAULDRON)
                    1 -> intArrayOf(Up.KANCHA, Up.JHOLBHAI, Up.DIDI, Up.DAI)
                    2 -> intArrayOf(Up.NOCAMP, Up.UPSELL)
                    else -> intArrayOf()
                }
                if (tab == 3) {
                    GemTab(game, hud, audio, onPrestige = { confirmPrestige = true })
                } else {
                    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(list.size) { idx ->
                            UpgradeCard(game, list[idx], hud, audio)
                        }
                        if (tab == 2) {
                            items(1) {
                                Text(
                                    if (game.st[Layout.ID_KETTLE].unlocked) "Choose wisely: you can only pick ONE way to deal with the Chiya & Churot campers."
                                    else "Unlock the Chiya Kettle pad (right side of the shop) to open the Chiya & Churot corner!",
                                    color = Color(0xFFBFA9A0), fontSize = 12.sp, modifier = Modifier.padding(6.dp)
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
                            Text(Locs.EMOJI[l], fontSize = 32.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(Locs.NAME[l], color = Cream, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text(Locs.TAG[l], color = Color(0xFFBFA9A0), fontSize = 11.sp)
                                Text("Income x" + fmtMoney(Locs.MULT[l]), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (game.travel(l)) {
                                        audio.sfx(Sfx.UNLOCK, 1f, 0.6f)
                                        panel = 0
                                    }
                                },
                                enabled = can && !current,
                                colors = ButtonDefaults.buttonColors(containerColor = if (unlocked) GreenOk else Gold)
                            ) {
                                Text(if (current) "HERE" else if (unlocked) "Travel" else "Rs " + fmtMoney(Locs.COST[l]), fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        if (panel == 3) {
            SheetPanel("Daily Challenges", { panel = 0 }) {
                LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(3) { i ->
                        val prog = game.chProg[i]
                        val target = game.chTarget[i]
                        val done = game.chDone[i]
                        val ready = !done && prog >= target
                        Column(Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(game.chLabel(i), color = Cream, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        fmtMoney(minOf(prog, target)) + " / " + fmtMoney(target) + "   Reward: " + game.chReward[i] + " \uD83D\uDC8E",
                                        color = Color(0xFFBFA9A0), fontSize = 12.sp
                                    )
                                }
                                Button(
                                    onClick = { if (game.claimChallenge(i)) audio.sfx(Sfx.UNLOCK, 1f, 0.6f) },
                                    enabled = ready,
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenOk)
                                ) { Text(if (done) "Claimed" else if (ready) "Claim" else "...", fontWeight = FontWeight.Black) }
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
                        Text("Challenges refresh every day. Keep the jhol flowing!", color = Color(0xFFBFA9A0), fontSize = 12.sp, modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
        if (panel == 4) {
            SheetPanel("Settings", { panel = 0 }) {
                var snd by remember { mutableStateOf(game.soundOn) }
                var hap by remember { mutableStateOf(game.hapticOn) }
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sound & street ambience", color = Cream, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Switch(checked = snd, onCheckedChange = { snd = it; game.soundOn = it; audio.setSound(it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Haptic feedback", color = Cream, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Switch(checked = hap, onCheckedChange = { hap = it; game.hapticOn = it; if (it) audio.haptic(Hap.THUMP) })
                    }
                    Text(
                        "Plates served: ${game.platesServed}   Chiya served: ${game.chiyaServed}\nVIPs: ${game.vipServed}   Angry customers: ${game.angryLeft}\nLifetime earnings: Rs ${fmtMoney(game.lifetime)}",
                        color = Color(0xFFBFA9A0), fontSize = 13.sp
                    )
                    Button(onClick = { saveNow() }, colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                        Text("Save now", fontWeight = FontWeight.Black)
                    }
                    Text("Momo & Chiya Empire - Tapari Tycoon. Made with jhol.", color = Color(0xFF8C7A74), fontSize = 11.sp)
                }
            }
        }

        // ---------------- dialogs ----------------
        if (game.offlineEarn > 0.0) {
            val hrs = game.offlineSecs / 3600.0
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Welcome back, Boss!", fontWeight = FontWeight.Black) },
                text = {
                    Text(
                        "Your cart kept the steam going for " + String.format(java.util.Locale.US, "%.1f", hrs) +
                            " hours and earned Rs " + fmtMoney(game.offlineEarn) + " while you were away."
                    )
                },
                confirmButton = { TextButton(onClick = { game.claimOffline(false) }) { Text("Collect", fontWeight = FontWeight.Black) } },
                dismissButton = {
                    TextButton(onClick = { game.claimOffline(true) }, enabled = game.gems >= 2) { Text("x2 for 2 \uD83D\uDC8E", fontWeight = FontWeight.Black) }
                }
            )
        }
        if (confirmPrestige) {
            AlertDialog(
                onDismissRequest = { confirmPrestige = false },
                title = { Text("Franchise Rebirth?", fontWeight = FontWeight.Black) },
                text = {
                    Text(
                        "Reset your shops, upgrades and cash for +" + game.prestigeGain() + " \uD83D\uDC8E. Each gem permanently boosts all income by 5%."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { game.prestige(); confirmPrestige = false; panel = 0 }) { Text("Rebirth!", fontWeight = FontWeight.Black) }
                },
                dismissButton = { TextButton(onClick = { confirmPrestige = false }) { Text("Not yet") } }
            )
        }
    }
}

@Composable
private fun SheetPanel(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
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
                    ) { Text("\u2715", color = Cream, fontWeight = FontWeight.Black) }
                }
                content()
            }
        }
    }
}

@Composable
private fun UpgradeCard(game: Game, id: Int, hud: Hud, audio: AudioFx) {
    val l = game.upg[game.loc][id]
    val max = UpDef.MAX[id]
    val cost = game.upgCost(game.loc, id)
    val maxed = l >= max
    val can = !maxed && game.money >= cost
    Row(
        Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(UpDef.EMOJI[id], fontSize = 28.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(UpDef.NAME[id], color = Cream, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(UpDef.DESC[id], color = Color(0xFFBFA9A0), fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { l.toFloat() / max },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = GreenOk, trackColor = Color(0xFF4A2A2A)
            )
            Text(if (max == 1) (if (l > 0) "Active" else "Not bought") else "Lv $l / $max", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = { if (game.buyUpgrade(id)) audio.sfx(Sfx.UPGRADE, 1f, 0.5f) },
            enabled = can,
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF2B1418))
        ) {
            Text(if (maxed) (if (max == 1) "BOUGHT" else "MAX") else fmtMoney(cost), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun GemTab(game: Game, hud: Hud, audio: AudioFx, onPrestige: () -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp)) {
            Text("\uD83D\uDC8E ${hud.gems} gems  =  +${hud.gems * 5}% income", color = Color(0xFF8BE6FF), fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text("Earn gems from VIP guests, daily challenges and franchise rebirth.", color = Color(0xFFBFA9A0), fontSize = 11.sp)
        }
        Row(
            Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("\u2615", fontSize = 28.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Chiya Rush", color = Cream, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text("90s of double crowds and faster hustle.", color = Color(0xFFBFA9A0), fontSize = 11.sp)
            }
            Button(
                onClick = { if (game.buyBoost()) audio.sfx(Sfx.UPGRADE, 1.2f, 0.5f) },
                enabled = hud.gems >= 3 && hud.boost == 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35C6F4), contentColor = Color(0xFF05303D))
            ) { Text("3 \uD83D\uDC8E", fontWeight = FontWeight.Black) }
        }
        Row(
            Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("\uD83D\uDD04", fontSize = 28.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Franchise Rebirth", color = Cream, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(
                    if (hud.canPrestige) "Reset for +${game.prestigeGain()} gems (permanent +5% income each)."
                    else "Earn Rs 20K in this run to unlock (now Rs ${fmtMoney(game.runEarned)}).",
                    color = Color(0xFFBFA9A0), fontSize = 11.sp
                )
            }
            Button(
                onClick = onPrestige,
                enabled = hud.canPrestige,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White)
            ) { Text("Rebirth", fontWeight = FontWeight.Black) }
        }
    }
}
