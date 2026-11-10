package com.tapari.tycoon

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/** Arm / body poses. */
object Pose {
    const val WALK = 0
    const val CARRY = 1
    const val EAT = 2
    const val CHEER = 3
    const val ANGRY = 4
    const val WORK = 5
    const val PHONE = 6
    const val YELL = 7
    const val COUNT = 8
}

object HairS {
    const val SHORT = 0
    const val SWOOP = 1
    const val LONG = 2
    const val BRAID = 3
    const val POMP = 4
    const val BUN = 5
    const val NONE = 6
}

object HatS {
    const val NONE = 0
    const val TOPI = 1
    const val CHEF = 2
    const val BANDANA = 3
    const val HELMET = 4
    const val SUN = 5
    const val HOOD = 6
    const val CAP = 7
}

object Acc {
    const val APRON = 1
    const val PACK = 2
    const val GLASSES = 4
    const val CAMERA = 8
    const val TIE = 16
    const val SCARF = 32
    const val JACKET = 64
    const val KURTA = 128
    const val RING = 256
}

fun shade(c: Int, k: Float): Int {
    val r = (((c and 255) * k).toInt()).coerceIn(0, 255)
    val g = ((((c shr 8) and 255) * k).toInt()).coerceIn(0, 255)
    val b = ((((c shr 16) and 255) * k).toInt()).coerceIn(0, 255)
    val a = (c ushr 24) and 255
    return (a shl 24) or (b shl 16) or (g shl 8) or r
}

/**
 * Articulated low-poly character. Caller fills the look + pose fields then calls [person].
 * Allocation free: everything is written straight into the shared LitBatch.
 */
class Rig {
    lateinit var L: LitBatch
    var t = 0f

    // ---- look ----
    var skin = 0
    var hair = 0
    var shirt = 0
    var pants = 0
    var accent = 0
    var hatCol = 0
    var shoe = Col.rgb(0x2B2B33)
    var hairStyle = 0
    var hat = 0
    var acc = 0

    // ---- pose ----
    var heading = 0f
    var phase = 0f
    var mv = 0f
    var seated = false
    var arm = 0
    var spin = 0f
    var mood = 0       // 0 neutral 1 happy 2 angry 3 shout 4 chew
    var seed = 0f
    var hop = 0f
    var lookX = 0f

    private val WHITE = Col.rgb(0xFFFFFF)
    private val EYE = Col.rgb(0x1B1B22)
    private val MOUTH = Col.rgb(0x6B1D1D)
    private val TONGUE = Col.rgb(0xE8707A)
    private val DARK = Col.rgb(0x2A2D34)
    private val GLOWSCR = Col.emis(0x7AE0FF, 70)
    private val RINGLIGHT = Col.emis(0xFFF6D8, 20)
    private val TRAY_WOOD = Col.rgb(0x8C5832)

    fun person(x: Float, y0: Float, z: Float) {
        val b = L
        val sp = sin(phase)
        val cp = cos(phase)
        val mvv = mv
        val idle = 1f - mvv
        val bob = if (seated) 0f else abs(sp) * 0.085f * mvv
        val dy = if (seated) -0.40f else 0f
        val br = sin(t * 2.5f + seed) * 0.015f * idle
        var hopY = hop
        var shake = 0f
        if (arm == Pose.CHEER) hopY += abs(sin(t * 10f + seed)) * 0.18f
        if (arm == Pose.ANGRY) {
            hopY += abs(sin(t * 16f)) * 0.06f
            shake = sin(t * 30f) * 0.055f
        }
        b.loadId()
        b.translate(x, y0 + bob + dy + hopY, z)
        b.rotY(heading + spin)

        val hip = 0.87f
        if ((acc and Acc.KURTA) != 0 && !seated) {
            b.box(0f, hip - 0.2f, 0f, 0.5f, 0.52f, 0.3f, shirt)
            b.box(0f, hip - 0.45f, 0f, 0.46f, 0.1f, 0.28f, shade(shirt, 0.85f))
        }
        // ---- legs ----
        for (sd in 0 until 2) {
            val s = if (sd == 0) -1f else 1f
            val ph = phase + (if (s > 0f) 3.1416f else 0f)
            val thigh: Float
            val knee: Float
            if (seated) {
                thigh = -1.5f + s * 0.05f
                knee = 1.5f
            } else {
                thigh = -sin(ph) * 0.78f * mvv + s * 0.03f * idle
                knee = mvv * (0.14f + 0.92f * max(0f, cos(ph)))
            }
            b.push()
            b.translate(s * 0.13f, hip, 0f)
            b.rotX(thigh)
            b.box(0f, -0.21f, 0f, 0.175f, 0.44f, 0.195f, pants)
            b.translate(0f, -0.42f, 0f)
            b.rotX(knee)
            b.box(0f, -0.2f, 0f, 0.155f, 0.42f, 0.175f, pants)
            b.box(0f, -0.41f, 0.05f, 0.175f, 0.1f, 0.31f, shoe)
            b.box(0f, -0.455f, 0.05f, 0.185f, 0.035f, 0.32f, shade(shoe, 1.8f))
            b.pop()
        }

        // ---- torso ----
        b.push()
        b.translate(0f, hip + 0.02f, 0f)
        var lean = mvv * 0.14f
        if (arm == Pose.WORK) lean += 0.17f
        if (arm == Pose.EAT) lean += 0.11f + 0.04f * sin(t * 7f + seed)
        if (arm == Pose.ANGRY) lean += 0.12f
        if (seated) lean += 0.05f
        b.rotX(lean)
        b.rotY(sp * 0.21f * mvv)
        b.rotZ(cp * 0.04f * mvv + shake)
        b.box(0f, 0.04f, 0f, 0.46f, 0.2f, 0.28f, pants)
        b.box(0f, 0.36f + br, 0f, 0.54f, 0.6f, 0.31f, shirt)
        b.box(0f, 0.34f, 0f, 0.5f, 0.12f, 0.33f, shade(shirt, 0.92f))
        b.cyl(0f, 0.7f, 0f, 0.075f, 0.1f, skin)
        if ((acc and Acc.APRON) != 0) {
            b.box(0f, 0.3f, 0.168f, 0.5f, 0.56f, 0.03f, accent)
            b.box(0.12f, 0.2f, 0.19f, 0.18f, 0.14f, 0.03f, shade(accent, 0.82f))
            b.box(0f, 0.62f, 0.15f, 0.34f, 0.07f, 0.04f, accent)
        }
        if ((acc and Acc.TIE) != 0) {
            b.box(0f, 0.55f, 0.165f, 0.09f, 0.08f, 0.03f, accent)
            b.box(0f, 0.38f, 0.168f, 0.075f, 0.34f, 0.03f, accent)
            b.box(0f, 0.6f, 0.15f, 0.24f, 0.05f, 0.05f, WHITE)
        }
        if ((acc and Acc.PACK) != 0) {
            b.box(0f, 0.34f, -0.26f, 0.44f, 0.54f, 0.2f, accent)
            b.box(0f, 0.2f, -0.37f, 0.3f, 0.2f, 0.04f, shade(accent, 0.8f))
            b.box(-0.17f, 0.4f, 0.164f, 0.06f, 0.46f, 0.03f, shade(accent, 0.7f))
            b.box(0.17f, 0.4f, 0.164f, 0.06f, 0.46f, 0.03f, shade(accent, 0.7f))
        }
        if ((acc and Acc.CAMERA) != 0) {
            b.box(0f, 0.22f, 0.2f, 0.26f, 0.16f, 0.12f, DARK)
            b.box(0f, 0.3f, 0.19f, 0.1f, 0.05f, 0.08f, Col.rgb(0xAAB2BA))
            b.push()
            b.translate(0f, 0.22f, 0.27f)
            b.rotX(1.5708f)
            b.cyl(0f, 0f, 0f, 0.065f, 0.08f, Col.rgb(0x9AA6B2))
            b.cyl(0f, 0.04f, 0f, 0.045f, 0.02f, Col.emis(0x3AA0FF, 90))
            b.pop()
            b.box(0f, 0.5f, 0.16f, 0.4f, 0.04f, 0.03f, DARK)
        }
        if ((acc and Acc.JACKET) != 0) {
            b.box(0f, 0.6f, 0f, 0.62f, 0.1f, 0.35f, shade(shirt, 1.25f))
            b.box(0f, 0.18f, 0.161f, 0.56f, 0.05f, 0.02f, accent)
            b.box(0f, 0.28f, 0.161f, 0.56f, 0.03f, 0.02f, accent)
            b.box(0f, 0.36f, 0.17f, 0.04f, 0.6f, 0.02f, shade(shirt, 1.6f))
        }
        if ((acc and Acc.SCARF) != 0) {
            b.box(0f, 0.62f, 0.02f, 0.5f, 0.12f, 0.38f, accent)
            b.box(0.13f, 0.4f, 0.19f, 0.15f, 0.42f, 0.03f, accent)
            b.box(-0.2f, 0.42f, -0.18f, 0.12f, 0.4f, 0.03f, accent)
        }

        // ---- wooden carry tray when holding items ----
        if (arm == Pose.CARRY) {
            b.box(0f, 0.12f, 0.56f, 0.66f, 0.045f, 0.48f, TRAY_WOOD)
            b.box(0f, 0.15f, 0.78f, 0.66f, 0.06f, 0.04f, shade(TRAY_WOOD, 0.8f))
            b.box(-0.31f, 0.15f, 0.56f, 0.04f, 0.06f, 0.48f, shade(TRAY_WOOD, 0.8f))
            b.box(0.31f, 0.15f, 0.56f, 0.04f, 0.06f, 0.48f, shade(TRAY_WOOD, 0.8f))
        }

        // ---- head ----
        b.push()
        b.translate(0f, 0.98f + br, 0f)
        val lookYaw = sin(t * 0.8f + seed) * 0.4f * idle + lookX
        val nod = sin(phase * 2f) * 0.045f * mvv + (if (arm == Pose.EAT) sin(t * 7f + seed) * 0.06f else 0f)
        b.rotY(lookYaw)
        b.rotX(nod)
        if (arm == Pose.ANGRY || arm == Pose.YELL) b.rotZ(sin(t * 24f) * 0.06f)
        drawHead(b)
        b.pop()

        // ---- arms ----
        for (sd in 0 until 2) {
            val s = if (sd == 0) -1f else 1f
            var sh: Float
            var el: Float
            var splay = 0.07f
            val right = s > 0f
            when (arm) {
                Pose.CARRY -> { sh = -1.15f; el = -0.45f; splay = 0.02f }
                Pose.EAT -> {
                    if (right) { sh = -0.95f; el = -1.2f - 0.7f * (0.5f + 0.5f * sin(t * 7f + seed)) }
                    else { sh = -0.7f; el = -0.5f }
                }
                Pose.CHEER -> { sh = -2.7f + 0.35f * sin(t * 13f + s); el = -0.25f; splay = 0.35f }
                Pose.ANGRY -> { sh = -0.6f + 0.3f * sin(t * 22f + s); el = -1.7f; splay = 0.3f }
                Pose.WORK -> {
                    val w = sin(t * 9f + seed)
                    if (right) { sh = -0.95f + 0.25f * w; el = -0.9f - 0.3f * w }
                    else { sh = -0.8f; el = -0.8f + 0.12f * w }
                    splay = 0.1f
                }
                Pose.PHONE -> {
                    if (right) { sh = -1.7f; el = -1.0f }
                    else { sh = -s * sp * 0.85f * mvv; el = -(0.12f + 0.55f * mvv) }
                }
                Pose.YELL -> {
                    if (right) { sh = -2.7f + 0.3f * sin(t * 14f); el = -0.2f; splay = 0.25f }
                    else { sh = -1.8f + 0.25f * cos(t * 14f); el = -0.5f; splay = 0.3f }
                }
                else -> {
                    sh = -s * sp * 0.85f * mvv + sin(t * 1.7f + seed + s) * 0.04f * idle
                    el = -(0.12f + 0.55f * mvv)
                }
            }
            val longSleeve = (acc and Acc.JACKET) != 0 || hat == HatS.HOOD
            b.push()
            b.translate(s * 0.34f, 0.57f, 0f)
            b.rotZ(s * splay)
            b.rotX(sh)
            b.sphLo(0f, 0f, 0f, 0.1f, 0.1f, 0.1f, shirt)
            b.box(0f, -0.14f, 0f, 0.14f, 0.3f, 0.14f, shirt)
            b.translate(0f, -0.28f, 0f)
            b.rotX(el)
            b.box(0f, -0.13f, 0f, 0.125f, 0.28f, 0.125f, if (longSleeve) shirt else skin)
            b.sphLo(0f, -0.29f, 0.01f, 0.075f, 0.075f, 0.075f, skin)
            if (arm == Pose.PHONE && right) {
                b.push()
                b.translate(0f, -0.3f, 0.1f)
                b.rotX(-0.35f)
                if ((acc and Acc.RING) != 0) {
                    b.push()
                    b.translate(0f, 0.04f, 0.05f)
                    b.rotX(1.5708f)
                    b.cyl(0f, 0f, 0f, 0.2f, 0.03f, RINGLIGHT)
                    b.cyl(0f, 0.01f, 0f, 0.12f, 0.04f, DARK)
                    b.pop()
                }
                b.box(0f, 0f, 0f, 0.11f, 0.19f, 0.025f, DARK)
                b.box(0f, 0.005f, 0.015f, 0.09f, 0.16f, 0.01f, GLOWSCR)
                b.pop()
            }
            b.pop()
        }
        b.pop()
        // Always reset matrix after drawing a character so callers never inherit transforms!
        b.loadId()
    }

    private fun drawHead(b: LitBatch) {
        val dark = shade(skin, 0.82f)
        b.sph(0f, 0f, 0f, 0.30f, 0.31f, 0.29f, skin)
        b.sphLo(-0.295f, -0.01f, 0f, 0.055f, 0.07f, 0.04f, skin)
        b.sphLo(0.295f, -0.01f, 0f, 0.055f, 0.07f, 0.04f, skin)
        b.sphLo(0f, -0.04f, 0.285f, 0.042f, 0.042f, 0.04f, dark)
        val blink = sin(t * 1.9f + seed * 3f) > 0.985f
        val eh = if (blink) 0.018f else if (mood == 2) 0.07f else 0.105f
        for (sd in 0 until 2) {
            val s = if (sd == 0) -1f else 1f
            b.box(s * 0.115f, 0.04f, 0.268f, 0.095f, eh, 0.04f, WHITE)
            if (!blink) b.box(s * 0.115f + lookX * 0.02f, 0.035f, 0.29f, 0.052f, eh * 0.75f, 0.02f, EYE)
            val ang = when (mood) {
                2 -> 0.55f
                1 -> -0.12f
                3 -> -0.25f
                else -> 0f
            }
            b.push()
            b.translate(s * 0.115f, 0.145f + (if (mood == 3) 0.025f else 0f), 0.235f)
            b.rotZ(s * ang)
            b.box(0f, 0f, 0f, 0.11f, 0.028f, 0.04f, shade(hair, 0.9f))
            b.pop()
        }
        when (mood) {
            1 -> {
                b.box(0f, -0.14f, 0.252f, 0.15f, 0.06f, 0.04f, MOUTH)
                b.box(0f, -0.117f, 0.262f, 0.12f, 0.024f, 0.04f, WHITE)
                b.sphLo(-0.2f, -0.07f, 0.19f, 0.05f, 0.035f, 0.03f, Col.rgb(0xFF8A8A))
                b.sphLo(0.2f, -0.07f, 0.19f, 0.05f, 0.035f, 0.03f, Col.rgb(0xFF8A8A))
            }
            2 -> {
                b.box(0f, -0.17f, 0.25f, 0.13f, 0.03f, 0.04f, MOUTH)
                b.box(-0.07f, -0.155f, 0.25f, 0.035f, 0.045f, 0.04f, MOUTH)
                b.box(0.07f, -0.155f, 0.25f, 0.035f, 0.045f, 0.04f, MOUTH)
                b.sphLo(-0.19f, -0.04f, 0.2f, 0.06f, 0.045f, 0.03f, Col.rgb(0xFF4A3A))
                b.sphLo(0.19f, -0.04f, 0.2f, 0.06f, 0.045f, 0.03f, Col.rgb(0xFF4A3A))
            }
            3 -> {
                b.box(0f, -0.15f, 0.25f, 0.15f, 0.125f, 0.045f, MOUTH)
                b.box(0f, -0.195f, 0.256f, 0.08f, 0.04f, 0.045f, TONGUE)
                b.box(0f, -0.1f, 0.262f, 0.12f, 0.025f, 0.04f, WHITE)
            }
            4 -> {
                val h = 0.025f + 0.05f * (0.5f + 0.5f * sin(t * 10f + seed))
                b.box(0f, -0.14f, 0.252f, 0.1f, h, 0.04f, MOUTH)
            }
            else -> b.box(0f, -0.14f, 0.255f, 0.09f, 0.022f, 0.04f, MOUTH)
        }
        if ((acc and Acc.GLASSES) != 0) {
            val fr = Col.rgb(0x1E1E26)
            for (sd in 0 until 2) {
                val s = if (sd == 0) -1f else 1f
                val gx = s * 0.115f
                b.box(gx, 0.1f, 0.3f, 0.19f, 0.022f, 0.02f, fr)
                b.box(gx, -0.02f, 0.3f, 0.19f, 0.022f, 0.02f, fr)
                b.box(gx - 0.085f, 0.04f, 0.3f, 0.022f, 0.14f, 0.02f, fr)
                b.box(gx + 0.085f, 0.04f, 0.3f, 0.022f, 0.14f, 0.02f, fr)
            }
            b.box(0f, 0.05f, 0.3f, 0.05f, 0.022f, 0.02f, fr)
        }
        // ---- hair ----
        val h = hair
        when (hairStyle) {
            HairS.SHORT -> b.sph(0f, 0.13f, -0.08f, 0.32f, 0.24f, 0.31f, h)
            HairS.SWOOP -> {
                b.sph(0f, 0.13f, -0.08f, 0.32f, 0.24f, 0.31f, h)
                b.sphLo(0.07f, 0.27f, 0.1f, 0.24f, 0.1f, 0.2f, h)
            }
            HairS.LONG -> {
                b.sph(0f, 0.13f, -0.08f, 0.32f, 0.24f, 0.31f, h)
                b.box(0f, -0.2f, -0.2f, 0.52f, 0.66f, 0.15f, h)
                b.box(-0.29f, -0.1f, -0.02f, 0.07f, 0.42f, 0.2f, h)
                b.box(0.29f, -0.1f, -0.02f, 0.07f, 0.42f, 0.2f, h)
            }
            HairS.BRAID -> {
                b.sph(0f, 0.13f, -0.08f, 0.32f, 0.24f, 0.31f, h)
                for (k in 0 until 5) {
                    val sw = sin(t * 3f + k * 0.7f + seed) * 0.02f * k + sin(phase) * 0.03f * k * mv
                    b.sphLo(sw, -0.12f - k * 0.14f, -0.3f - k * 0.01f, 0.07f, 0.09f, 0.07f, h)
                }
                b.sphLo(0f, -0.82f, -0.31f, 0.05f, 0.05f, 0.05f, Col.rgb(0xD6382F))
            }
            HairS.POMP -> {
                b.sph(0f, 0.14f, -0.08f, 0.32f, 0.24f, 0.31f, h)
                b.sph(0f, 0.26f, 0.04f, 0.31f, 0.2f, 0.34f, h)
            }
            HairS.BUN -> {
                b.sph(0f, 0.13f, -0.08f, 0.32f, 0.24f, 0.31f, h)
                b.sph(0f, 0.38f, -0.12f, 0.14f, 0.14f, 0.14f, h)
            }
        }
        // ---- hats ----
        when (hat) {
            HatS.TOPI -> {
                b.push()
                b.translate(0f, 0.28f, 0f)
                b.rotX(0.14f)
                b.cyl(0f, 0f, 0f, 0.29f, 0.2f, hatCol)
                b.cyl(0f, -0.085f, 0f, 0.297f, 0.05f, shade(hatCol, 0.7f))
                b.cyl(0f, 0.1f, 0f, 0.26f, 0.02f, shade(hatCol, 1.25f))
                b.box(0f, 0.02f, 0.285f, 0.12f, 0.12f, 0.025f, accent)
                b.box(-0.2f, 0.02f, 0.2f, 0.07f, 0.1f, 0.025f, accent)
                b.box(0.2f, 0.02f, 0.2f, 0.07f, 0.1f, 0.025f, accent)
                b.pop()
            }
            HatS.CHEF -> {
                b.cyl(0f, 0.3f, -0.02f, 0.28f, 0.14f, WHITE)
                b.sphLo(0f, 0.44f, -0.02f, 0.34f, 0.2f, 0.34f, WHITE)
                b.cyl(0f, 0.27f, -0.02f, 0.285f, 0.04f, accent)
            }
            HatS.BANDANA -> {
                b.cyl(0f, 0.25f, -0.08f, 0.305f, 0.09f, hatCol)
                b.box(0f, 0.22f, -0.4f, 0.14f, 0.14f, 0.1f, hatCol)
                b.box(-0.06f, 0.1f, -0.44f, 0.07f, 0.2f, 0.03f, hatCol)
                b.box(0.07f, 0.12f, -0.44f, 0.07f, 0.17f, 0.03f, hatCol)
            }
            HatS.HELMET -> {
                b.sph(0f, 0.17f, -0.06f, 0.35f, 0.27f, 0.35f, hatCol)
                b.box(0f, 0.2f, 0.3f, 0.44f, 0.1f, 0.1f, accent)
                b.box(0f, 0.3f, 0.0f, 0.06f, 0.05f, 0.62f, accent)
                b.box(-0.3f, -0.02f, 0.0f, 0.04f, 0.3f, 0.3f, shade(hatCol, 1.3f))
                b.box(0.3f, -0.02f, 0.0f, 0.04f, 0.3f, 0.3f, shade(hatCol, 1.3f))
            }
            HatS.SUN -> {
                b.cyl(0f, 0.3f, -0.02f, 0.56f, 0.035f, hatCol)
                b.cyl(0f, 0.38f, -0.02f, 0.27f, 0.16f, hatCol)
                b.cyl(0f, 0.33f, -0.02f, 0.28f, 0.04f, accent)
            }
            HatS.HOOD -> {
                b.sph(0f, 0.07f, -0.1f, 0.36f, 0.33f, 0.34f, shirt)
                b.sphLo(0f, -0.22f, -0.2f, 0.28f, 0.2f, 0.2f, shirt)
            }
            HatS.CAP -> {
                b.sph(0f, 0.15f, -0.05f, 0.33f, 0.22f, 0.33f, hatCol)
                b.box(0f, 0.12f, 0.31f, 0.3f, 0.03f, 0.22f, shade(hatCol, 0.8f))
            }
        }
    }
}
