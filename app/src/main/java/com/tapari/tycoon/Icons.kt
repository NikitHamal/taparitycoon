package com.tapari.tycoon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.PathParser

/**
 * Vector icon set. Glyphs come from Google Material Icons (Apache 2.0) and game-icons.net (CC BY 3.0).
 * Each icon is a single-colour glyph, tinted per use, so the whole UI reads as one consistent family.
 */
enum class Ic(val key: String, val tint: Long) {
    COIN("g_coins", 0xFFFFC93C),
    GEM("m_diamond", 0xFF5FD4FF),
    BAG("g_backpack", 0xFFFFE9C2),
    CHILI("g_chili_pepper", 0xFFFF5A47),
    GEAR("m_settings", 0xFFE6E0DA),
    CALENDAR("m_event", 0xFFFFD08A),
    MAP("m_map", 0xFF8FE3C0),
    UP("m_upgrade", 0xFFFFD54A),
    BOLT("m_bolt", 0xFFFFE066),
    STAR("m_star", 0xFFFFC93C),
    SHOP("m_storefront", 0xFFFFB86B),
    CART("g_shopping_cart", 0xFFFFB86B),
    ROOF("m_roofing", 0xFFB5E3FF),
    GLOBE("m_public", 0xFF7FD6FF),
    SHOE("g_running_shoe", 0xFF9CF0AE),
    TAG("m_sell", 0xFFFFA37A),
    STEAMER("g_rice_cooker", 0xFFDCE3EA),
    CHEF("g_cook", 0xFFFFFFFF),
    POT("g_cauldron", 0xFFFF7A59),
    CUP("g_coffee_cup", 0xFFE9B27A),
    CASH("g_money_stack", 0xFF6BE07B),
    NOCAMP("m_wifi_password", 0xFF8BC4FF),
    SMOKE("g_cigarette", 0xFFE0C9B0),
    LOCK("m_lock", 0xFFD7CFC9),
    CHECK("m_check", 0xFF6BE07B),
    CLOSE("m_close", 0xFFF2E8E0),
    PLAY("m_play_arrow", 0xFF2B1418),
    SOUND("m_volume_up", 0xFFFFE0A8),
    MUTE("m_volume_off", 0xFFB9A39A),
    VIBRATE("m_vibration", 0xFFFFE0A8),
    TROPHY("m_emoji_events", 0xFFFFC93C),
    REFRESH("m_autorenew", 0xFFFF8A80),
    MOMO("g_dumpling", 0xFFFFE7B8),
    INFO("m_info", 0xFFBFE3FF),
    HOME("m_home", 0xFFFFFFFF),
    PAUSE("m_pause", 0xFFF2E8E0),
    DASH("m_directions_run", 0xFFFFFFFF),
    FIRE("m_local_fire_department", 0xFFFF9A3C),
    GIFT("m_redeem", 0xFFFF8FC8),
    SPIN("m_casino", 0xFFFFD54A),
    SPARK("g_sparkles", 0xFFFFE066),
    CROWN("g_crown", 0xFFFFC93C),
    BELL("g_ringing_bell", 0xFFFFD54A),
    HEART("m_favorite", 0xFFFF6B8B),
    PARTY("g_party_popper", 0xFFFF8FC8),
    RANK("m_military_tech", 0xFFFFC93C),
    MEDAL("m_workspace_premium", 0xFFFFC93C),
    TREND("m_trending_up", 0xFF6BE07B),
    TAP("m_touch_app", 0xFFFFFFFF),
    BOARD("m_leaderboard", 0xFFFFC93C),
    ROCKET("m_rocket_launch", 0xFFFF8A65),
    TASK("m_task_alt", 0xFF6BE07B),
    HELMET("g_full_motorcycle_helmet", 0xFFFFD66B),
    MOUNTAIN("g_mountain", 0xFFB5E3FF),
    TEMPLE("g_temple_gate", 0xFFFFB86B),
    SPEEDO("g_speedometer", 0xFFFFE066),
    VIDEO("m_videocam", 0xFFFF8FC8),
    CAMERA("m_photo_camera", 0xFF8FE3D8),
    BULB("m_lightbulb", 0xFFFFE066),
    FLOUR("g_flour", 0xFFF3E7C9),
    MEAT("g_meat", 0xFFFF8A80),
    JAR("g_mason_jar", 0xFFFF7A59),
    CHEVRON("m_chevron_right", 0xFFF2E8E0),
    MAGNET("m_touch_app", 0xFF8BE6FF),
    BOOK("m_leaderboard", 0xFFFFC93C),
    HIRE("g_cook", 0xFFFFFFFF)
}

/** Parses and caches glyph paths. Used by the Compose UI and by the 3D texture atlas. */
object GlyphLib {
    private val idx = HashMap<String, Int>()
    private val cache = arrayOfNulls<android.graphics.Path>(GlyphData.KEYS.size)
    private val bad = BooleanArray(GlyphData.KEYS.size)
    private val cCache = arrayOfNulls<androidx.compose.ui.graphics.Path>(GlyphData.KEYS.size)

    init {
        for (i in GlyphData.KEYS.indices) idx[GlyphData.KEYS[i]] = i
    }

    fun vb(key: String): Float {
        val i = idx[key] ?: return 24f
        return GlyphData.VB[i]
    }

    fun path(key: String): android.graphics.Path? {
        val i = idx[key] ?: return null
        if (bad[i]) return null
        var p = cache[i]
        if (p == null) {
            try {
                p = PathParser.createPathFromPathData(GlyphData.PATHS[i])
                cache[i] = p
            } catch (e: Throwable) {
                bad[i] = true
                return null
            }
        }
        return p
    }

    fun composePath(key: String): androidx.compose.ui.graphics.Path? {
        val i = idx[key] ?: return null
        var c = cCache[i]
        if (c == null) {
            val p = path(key) ?: return null
            c = p.asComposePath()
            cCache[i] = c
        }
        return c
    }
}

@Composable
fun GIcon(ic: Ic, size: Dp, modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
    val path = remember(ic) { GlyphLib.composePath(ic.key) }
    val vb = remember(ic) { GlyphLib.vb(ic.key) }
    val col = if (tint == Color.Unspecified) Color(ic.tint) else tint
    Canvas(modifier.size(size)) {
        val p = path ?: return@Canvas
        val k = this.size.minDimension / vb
        withTransform({ scale(k, k, Offset.Zero) }) { drawPath(p, col) }
    }
}

/** A glyph on a round gradient badge, for hero spots (rewards, achievements, locations). */
@Composable
fun GBadge(ic: Ic, size: Dp, top: Color, bottom: Color, modifier: Modifier = Modifier, glyph: Color = Color.White, ring: Color = Color(0x66FFFFFF)) {
    Box(
        modifier
            .size(size)
            .background(Brush.verticalGradient(listOf(top, bottom)), CircleShape)
            .border(2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        GIcon(ic, size * 0.58f, tint = glyph)
    }
}
