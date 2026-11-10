package com.tapari.tycoon

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object Theme {
    val CLEAR = arrayOf(
        floatArrayOf(0.55f, 0.78f, 0.95f), floatArrayOf(0.60f, 0.82f, 0.96f),
        floatArrayOf(0.98f, 0.70f, 0.45f), floatArrayOf(0.10f, 0.07f, 0.22f)
    )
    val SUN = arrayOf(
        floatArrayOf(0.82f, 0.74f, 0.60f), floatArrayOf(0.82f, 0.76f, 0.64f),
        floatArrayOf(0.86f, 0.60f, 0.38f), floatArrayOf(0.50f, 0.52f, 0.86f)
    )
    val SKYAMB = arrayOf(
        floatArrayOf(0.55f, 0.60f, 0.72f), floatArrayOf(0.58f, 0.62f, 0.72f),
        floatArrayOf(0.66f, 0.52f, 0.56f), floatArrayOf(0.40f, 0.38f, 0.68f)
    )
    val GNDAMB = arrayOf(
        floatArrayOf(0.42f, 0.38f, 0.34f), floatArrayOf(0.44f, 0.40f, 0.36f),
        floatArrayOf(0.46f, 0.34f, 0.30f), floatArrayOf(0.28f, 0.22f, 0.38f)
    )
    val FLOOR_A = intArrayOf(Col.rgb(0xA5A9AD), Col.rgb(0xF0E8D6), Col.rgb(0xB07A4C), Col.rgb(0x3A3F66))
    val FLOOR_B = intArrayOf(Col.rgb(0x92969B), Col.rgb(0xD6C8AC), Col.rgb(0x9C693E), Col.rgb(0x2E3254))
    val WALL = intArrayOf(Col.rgb(0xCC7A4E), Col.rgb(0xE8D8B6), Col.rgb(0x6F8191), Col.rgb(0x2A2250))
    val ACCENT = intArrayOf(Col.rgb(0xF2B134), Col.rgb(0xCC3B2E), Col.rgb(0xF6E3A1), Col.rgb(0xFFC247))
}

class Scene(private val g: Game) {
    private lateinit var L: LitBatch
    private lateinit var SG: SpriteBatch
    private lateinit var SB: SpriteBatch
    private lateinit var SO: SpriteBatch
    private var t = 0f
    private var rX = 1f
    private var rY = 0f
    private var rZ = 0f
    private var uX = 0f
    private var uY = 1f
    private var uZ = 0f
    private var gRX = 1f
    private var gRZ = 0f
    private var gUX = 0f
    private var gUZ = -1f
    private val gl = IntArray(24)

    private val WHITE = Col.rgb(0xFFFFFF)
    private val DARK = Col.rgb(0x2A2D34)
    private val WOOD = Col.rgb(0x9B6B43)
    private val WOOD_D = Col.rgb(0x6D4A2D)
    private val STEEL = Col.rgb(0xC3CBD3)
    private val STEEL_D = Col.rgb(0x8E98A3)
    private val CREAM = Col.rgb(0xF5EBD2)
    private val MOMO = Col.rgb(0xFFEFC9)
    private val RED = Col.rgb(0xD6382F)
    private val MEATC = Col.rgb(0xE2574C)
    private val PINK = Col.rgb(0xF4A9A0)
    private val GREEN = Col.rgb(0x3FAF55)
    private val BILL_RED = Col.rgb(0xD9483B)
    private val CLAY = Col.rgb(0xB5703A)
    private val EYE = Col.rgb(0x1B1B22)
    private val SHOE = Col.rgb(0x2B2B33)
    private val ALU = Col.rgb(0xD0D6DC)
    private val ALU2 = Col.rgb(0xB5BDC6)
    private val POT = Col.rgb(0x2A2A2E)
    private val JHOL_COL = Col.emis(0xE0391F, 170)
    private val FIRE = Col.emis(0xFF8A1F, 60)
    private val TOPI = Col.rgb(0xD9362B)
    private val TOPI2 = Col.rgb(0x1F1F28)
    private val BAG = Col.rgb(0x2E86C1)
    private val ORANGE = Col.rgb(0xF28C28)
    private val HAT = Col.rgb(0xE9D79B)
    private val HELMET = Col.rgb(0x1C1C20)
    private val VISOR = Col.rgb(0xFFC93C)
    private val PINKHAIR = Col.rgb(0xFF6FB5)
    private val SCARF = Col.rgb(0x7E3FB5)
    private val GLOW = Col.emis(0x7AE0FF, 60)
    private val LIGHT = Col.emis(0xFFE08A, 40)

    fun draw(lit: LitBatch, sg: SpriteBatch, sb: SpriteBatch, so: SpriteBatch, view: FloatArray) {
        L = lit; SG = sg; SB = sb; SO = so
        t = g.time
        rX = view[0]; rY = view[4]; rZ = view[8]
        uX = view[1]; uY = view[5]; uZ = view[9]
        val yaw = g.camYaw
        gRX = cos(yaw); gRZ = -sin(yaw)
        gUX = -sin(yaw); gUZ = -cos(yaw)
        L.reset(); SG.reset(); SB.reset(); SO.reset()
        drawWorld()
        for (i in 0 until Game.NST) drawStation(i)
        drawCustomers()
        drawStaff()
        drawPlayer()
        drawParticles()
    }

    // ------------------------------------------------------------------ helpers
    private fun shadow(x: Float, z: Float, size: Float, a: Float = 0.38f) {
        SG.quad(x, 0.03f, z, gRX, 0f, gRZ, gUX, 0f, gUZ, size * 0.5f, size * 0.5f, Atlas.SOFT, Col.tint(0f, 0f, 0f, a))
    }

    private fun ring(x: Float, z: Float, size: Float, col: Int) {
        SG.quad(x, 0.045f, z, gRX, 0f, gRZ, gUX, 0f, gUZ, size * 0.5f, size * 0.5f, Atlas.RING, col)
    }

    private fun gIcon(id: Int, x: Float, y: Float, z: Float, size: Float, col: Int) {
        SG.quad(x, y, z, gRX, 0f, gRZ, gUX, 0f, gUZ, size * 0.5f, size * 0.5f, id, col)
    }

    private fun bIcon(b: SpriteBatch, id: Int, x: Float, y: Float, z: Float, size: Float, col: Int) {
        b.quad(x, y, z, rX, rY, rZ, uX, uY, uZ, size * 0.5f, size * 0.5f, id, col)
    }

    private fun num(b: SpriteBatch, ground: Boolean, v: Double, cx: Float, cy: Float, cz: Float, h: Float, col: Int, coin: Boolean) {
        val n = Glyph.format(v, gl)
        var total = 0f
        for (k in 0 until n) total += Atlas.ADV[gl[k]] * h
        if (coin) total += h * 0.95f
        var off = -total / 2f
        val rx = if (ground) gRX else rX
        val ry = if (ground) 0f else rY
        val rz = if (ground) gRZ else rZ
        val ux = if (ground) gUX else uX
        val uy = if (ground) 0f else uY
        val uz = if (ground) gUZ else uZ
        if (coin) {
            val c = off + h * 0.45f
            b.quad(cx + rx * c, cy + ry * c, cz + rz * c, rx, ry, rz, ux, uy, uz, h * 0.45f, h * 0.45f, Atlas.COIN, WHITE)
            off += h * 0.95f
        }
        for (k in 0 until n) {
            val adv = Atlas.ADV[gl[k]] * h
            val c = off + adv / 2f
            b.quad(cx + rx * c, cy + ry * c, cz + rz * c, rx, ry, rz, ux, uy, uz,
                h * Atlas.GLYPH_ASPECT * 0.5f, h * 0.5f, Atlas.G0 + gl[k], col)
            off += adv
        }
    }

    // ------------------------------------------------------------------ world
    private fun drawWorld() {
        val loc = g.loc
        val fa = Theme.FLOOR_A[loc]
        val fb = Theme.FLOOR_B[loc]
        for (ix in 0 until 11) {
            for (iz in 0 until 9) {
                L.plane(-8.5f + ix * 2f, 0f, -7f + iz * 2f, 2f, 2f, if (((ix + iz) and 1) == 0) fa else fb)
            }
        }
        // sidewalk + road
        L.plane(1.5f, 0f, 11.9f, 40f, 3.8f, Col.rgb(0xB9B6AE))
        L.plane(1.5f, 0f, 17.4f, 40f, 7.2f, Col.rgb(0x4A4D52))
        for (k in 0 until 9) L.box(-9f + k * 3f, 0.02f, 17f, 1.4f, 0.03f, 0.18f, Col.rgb(0xEDE6C8))
        L.box(1.5f, 0.1f, 13.85f, 40f, 0.2f, 0.25f, Col.rgb(0x9A978F))
        val wall = Theme.WALL[loc]
        val acc = Theme.ACCENT[loc]
        // back wall + trim
        L.box(1.5f, 1.6f, -8.3f, 24f, 3.2f, 0.6f, wall)
        L.box(1.5f, 3.25f, -8.3f, 24.4f, 0.2f, 0.8f, acc)
        L.box(1.5f, 0.15f, -7.95f, 24f, 0.3f, 0.2f, Col.rgb(0x5A4636))
        // side walls
        L.box(-9.8f, 1.0f, 1.0f, 0.5f, 2.0f, 19.2f, wall)
        L.box(-9.8f, 2.05f, 1.0f, 0.7f, 0.12f, 19.2f, acc)
        L.box(12.4f, 0.45f, 1.0f, 0.3f, 0.9f, 19.2f, acc)
        // windows
        for (k in 0 until 5) {
            L.box(-6f + k * 4.5f, 2.0f, -7.95f, 1.5f, 1.0f, 0.08f, Col.emis(0x9AD7F5, 110))
            L.box(-6f + k * 4.5f, 2.0f, -7.9f, 1.6f, 0.08f, 0.1f, WHITE)
        }
        when (loc) {
            0 -> {
                L.push()
                L.translate(1.5f, 3.0f, -7.3f)
                L.rotX(-0.45f)
                for (k in 0 until 12) L.box(-11f + k * 2f, 0f, 0.7f, 1.0f, 0.07f, 1.5f, if (k % 2 == 0) Col.rgb(0x2E6FB7) else WHITE)
                L.pop()
                for (k in 0 until 12) {
                    val on = if (((t * 2f).toInt() + k) % 7 == 0) 0.4f else 1f
                    L.sphLo(-8f + k * 1.9f, 3.0f - 0.12f * sin(k * 1.7f), -7.2f, 0.1f, 0.1f, 0.1f, Col.emis(0xFFE08A, (40 * on).toInt()))
                }
            }
            1 -> {
                L.box(1.5f, 2.9f, -7.8f, 20f, 0.5f, 0.5f, Col.rgb(0x8D969E))
                for (k in 0 until 20) L.box(-8f + k, 2.9f, -7.52f, 0.95f, 0.38f, 0.05f, Col.rgb(0xA8B1B9))
                L.box(1.5f, 3.7f, -8.2f, 8f, 1.0f, 0.3f, Col.rgb(0xCC3B2E))
                L.box(1.5f, 3.7f, -8.0f, 7.4f, 0.7f, 0.1f, Col.emis(0xFFE9A8, 80))
                L.box(-9f, 0.35f, 8.2f, 1.2f, 0.7f, 1.2f, Col.rgb(0x4D8F4D))
                L.sph(-9f, 1.0f, 8.2f, 0.6f, 0.6f, 0.6f, Col.rgb(0x3E7E3E))
            }
            2 -> {
                for (k in 0 until 10) {
                    val h = 3f + ((k * 7) % 5) * 1.3f
                    L.box(-12f + k * 3.1f, h / 2f, -13f, 2.4f, h, 2f, Col.rgb(0x4A5A7A))
                    L.box(-12f + k * 3.1f, h * 0.7f, -11.95f, 0.5f, 0.35f, 0.05f, Col.emis(0xFFE08A, 50))
                }
                for (k in 0 until 14) {
                    val on = if (((t * 3f).toInt() + k) % 5 == 0) 0.5f else 1f
                    L.sphLo(-9f + k * 1.6f, 3.1f - 0.2f * sin(k * 1.3f), -7.2f, 0.11f, 0.11f, 0.11f, Col.emis(0xFFD27A, (40 * on).toInt()))
                }
                L.box(1.5f, 0.55f, 11.0f, 22f, 0.12f, 0.2f, Col.rgb(0x8A97A3))
                for (k in 0 until 12) L.box(-9f + k * 2f, 0.3f, 11.0f, 0.12f, 0.6f, 0.12f, Col.rgb(0x8A97A3))
            }
            3 -> {
                for (k in 0 until 10) {
                    val h = 5f + ((k * 5) % 7) * 1.6f
                    L.box(-12f + k * 3.1f, h / 2f, -13f, 2.4f, h, 2f, Col.rgb(0x1E1B45))
                    for (r in 0 until 4) L.box(-12.6f + k * 3.1f + (r % 2) * 1.2f, 1.4f + (r / 2) * 1.8f, -11.95f, 0.4f, 0.5f, 0.05f, Col.emis(0xFFF0A0, 40))
                }
                L.box(1.5f, 3.9f, -8.0f, 12f, 0.5f, 0.2f, Col.emis(0xFF3DAA, 30))
                L.box(1.5f, 3.35f, -8.0f, 9f, 0.18f, 0.2f, Col.emis(0x3DE8FF, 30))
                L.cyl(-9.3f, 1.6f, -7.5f, 0.35f, 3.2f, Col.rgb(0xE8B93A))
                L.cyl(12.0f, 1.6f, -7.5f, 0.35f, 3.2f, Col.rgb(0xE8B93A))
            }
        }
    }

    // ------------------------------------------------------------------ stations
    private fun drawStation(i: Int) {
        val s = g.st[i]
        val x = s.x
        val z = s.z
        if (!s.unlocked) {
            drawPad(i, s)
            return
        }
        val k = s.kind
        val pulse = 0.5f + 0.5f * sin(t * 3f + i)
        val inZone = run {
            val dx = g.px - Layout.ZX[i]
            val dz = g.pz - Layout.ZZ[i]
            dx * dx + dz * dz <= Layout.ZR[i] * Layout.ZR[i]
        }
        when (k) {
            SK.MAIDA -> {
                shadow(x, z, 3.0f)
                L.loadId(); L.translate(x, 0f, z)
                L.box(0f, 0.12f, 0f, 2.2f, 0.24f, 1.5f, WOOD)
                for (n in 0 until 6) {
                    val row = n / 3
                    val col = n % 3
                    L.box(-0.7f + col * 0.7f, 0.24f + 0.2f + row * 0.4f, (row % 2) * 0.08f - 0.04f, 0.62f, 0.38f, 0.9f, CREAM)
                    L.box(-0.7f + col * 0.7f, 0.24f + 0.2f + row * 0.4f, 0.0f, 0.64f, 0.1f, 0.5f, Col.rgb(0xD9532D))
                }
                zoneDecor(i, Atlas.I_MAIDA, inZone, pulse, Col.tint(0.75f, 1f, 0.7f, 1f))
                bIcon(SB, Atlas.I_MAIDA, x, 2.7f + 0.1f * sin(t * 2f), z, 1.0f, WHITE)
            }
            SK.MEAT -> {
                shadow(x, z, 3.0f)
                L.loadId(); L.translate(x, 0f, z)
                L.box(0f, 0.45f, 0f, 2.3f, 0.9f, 1.4f, STEEL_D)
                L.box(0f, 0.93f, 0f, 2.4f, 0.07f, 1.5f, STEEL)
                L.box(-0.4f, 1.05f, 0f, 0.9f, 0.1f, 0.7f, WOOD)
                for (n in 0 until 4) L.box(-0.7f + n * 0.3f, 1.2f, (n % 2) * 0.2f - 0.1f, 0.3f, 0.2f, 0.26f, if (n % 2 == 0) MEATC else PINK)
                L.box(0.6f, 1.05f, 0.1f, 0.5f, 0.05f, 0.3f, STEEL)
                L.box(0.85f, 1.1f, 0.1f, 0.2f, 0.05f, 0.1f, WOOD_D)
                zoneDecor(i, Atlas.I_MEAT, inZone, pulse, Col.tint(1f, 0.8f, 0.75f, 1f))
                bIcon(SB, Atlas.I_MEAT, x, 2.7f + 0.1f * sin(t * 2f + 1f), z, 1.0f, WHITE)
            }
            SK.JHOL -> {
                shadow(x, z, 3.0f)
                L.loadId(); L.translate(x, 0f, z)
                L.box(0f, 0.35f, 0f, 2.0f, 0.7f, 1.3f, WOOD)
                L.box(0f, 0.72f, 0f, 2.1f, 0.06f, 1.4f, WOOD_D)
                for (n in 0 until 5) {
                    val ox = -0.7f + (n % 3) * 0.7f
                    val oz = if (n < 3) -0.25f else 0.3f
                    L.cyl(ox, 1.0f, oz, 0.24f, 0.55f, RED)
                    L.cyl(ox, 1.3f, oz, 0.15f, 0.08f, POT)
                }
                zoneDecor(i, Atlas.I_JHOL, inZone, pulse, Col.tint(1f, 0.7f, 0.65f, 1f))
                bIcon(SB, Atlas.I_JHOL, x, 2.7f + 0.1f * sin(t * 2f + 2f), z, 1.0f, WHITE)
            }
            SK.PREP -> {
                shadow(x, z, 3.4f)
                L.loadId(); L.translate(x, 0f, z)
                L.box(0f, 0.45f, 0f, 2.6f, 0.9f, 1.5f, WOOD)
                L.box(0f, 0.93f, 0f, 2.7f, 0.07f, 1.6f, Col.rgb(0xC48F5C))
                for (n in 0 until g.st[i].stockA) L.box(-1.0f + (n % 4) * 0.27f, 1.1f + (n / 4) * 0.16f, -0.45f, 0.25f, 0.15f, 0.3f, CREAM)
                for (n in 0 until g.st[i].stockB) L.box(0.35f + (n % 4) * 0.25f, 1.08f + (n / 4) * 0.13f, -0.45f, 0.23f, 0.12f, 0.28f, MEATC)
                val mok = g.st[Layout.ID_MOKTU]
                for (n in 0 until min(mok.raw, 10)) L.sphLo(-0.5f + (n % 5) * 0.25f, 1.08f, 0.3f + (n / 5) * 0.25f, 0.1f, 0.08f, 0.1f, MOMO)
                L.box(0.9f, 1.0f, 0.4f, 0.5f, 0.05f, 0.14f, Col.rgb(0xE3B57A))
                if (s.flash > 0f) L.sphLo(0f, 1.3f + s.flash * 0.3f, 0f, 0.15f, 0.15f, 0.15f, Col.emis(0xFFF2CC, 40))
                zoneDecor(i, Atlas.I_PREP, inZone, pulse, Col.tint(1f, 1f, 0.8f, 1f))
                bIcon(SB, Atlas.I_PREP, x, 2.5f + 0.1f * sin(t * 2f + 3f), z, 1.0f, WHITE)
            }
            SK.MOKTU -> drawMoktu(s, inZone, pulse)
            SK.CAULDRON -> drawCauldron(s, inZone, pulse)
            SK.TABLE -> drawTable(i, s)
            SK.KETTLE -> drawKettle(i, s, inZone, pulse)
            SK.BENCH -> drawBench(i, s)
        }
        if (s.bills > 0) drawCash(i, s)
    }

    private fun zoneDecor(i: Int, icon: Int, inZone: Boolean, pulse: Float, tint: Int) {
        val zx = Layout.ZX[i]
        val zz = Layout.ZZ[i]
        val r = Layout.ZR[i]
        val a = if (inZone) 0.95f else 0.5f + 0.15f * pulse
        val sz = r * (if (inZone) 1.75f else 1.6f)
        SG.quad(zx, 0.045f, zz, gRX, 0f, gRZ, gUX, 0f, gUZ, sz * 0.5f, sz * 0.5f, Atlas.RING, Col.tint(
            ((tint and 255) / 255f), (((tint shr 8) and 255) / 255f), (((tint shr 16) and 255) / 255f), a))
        SG.quad(zx, 0.05f, zz, gRX, 0f, gRZ, gUX, 0f, gUZ, 0.55f, 0.55f, icon, Col.tint(1f, 1f, 1f, 0.85f))
    }

    private fun drawPad(i: Int, s: Station) {
        val hx = Layout.HX[i]
        val hz = Layout.HZ[i]
        val frac = if (s.cost > 0.0) (s.paid / s.cost).toFloat().coerceIn(0f, 1f) else 0f
        val active = abs(g.px - s.x) < hx && abs(g.pz - s.z) < hz
        L.loadId()
        L.translate(s.x, 0f, s.z)
        L.box(0f, 0.03f, 0f, 2f * hx + 0.18f, 0.06f, 2f * hz + 0.18f, if (active) Col.rgb(0xFFE58A) else WHITE)
        L.box(0f, 0.045f, 0f, 2f * hx, 0.07f, 2f * hz, DARK)
        if (frac > 0f) L.box(-hx + hx * frac, 0.075f, 0f, 2f * hx * frac, 0.03f, 2f * hz - 0.1f, Col.emis(0x1F7A3A, 150))
        val icon = when (s.kind) {
            SK.TABLE -> Atlas.I_TABLE
            SK.KETTLE -> Atlas.I_KETTLE
            else -> Atlas.I_BENCH
        }
        val bob = if (active) 0.05f else 0f
        gIcon(icon, s.x, 0.12f + bob, s.z - hz * 0.22f, min(1.6f, hz * 1.35f), WHITE)
        val remain = s.cost - s.paid
        num(SG, true, remain, s.x, 0.12f, s.z + hz * 0.58f, 0.62f, if (active) Col.rgb(0xFFE58A) else WHITE, true)
    }

    private fun drawMoktu(s: Station, inZone: Boolean, pulse: Float) {
        val x = s.x
        val z = s.z
        shadow(x, z, 4.2f)
        L.loadId(); L.translate(x, 0f, z)
        L.box(0f, 0.5f, 0f, 2.9f, 1.0f, 2.5f, Col.rgb(0x3B3F46))
        L.box(0f, 0.3f, 1.26f, 1.8f, 0.3f, 0.06f, FIRE)
        L.box(0f, 0.97f, 1.55f, 2.2f, 0.08f, 0.8f, STEEL)
        val bounce = s.flash * 0.05f * sin(t * 40f)
        for (k in 0 until 4) {
            L.cyl(0f, 1.25f + k * 0.5f + bounce * k, 0f, 1.15f - k * 0.03f, 0.5f, if (k % 2 == 0) ALU else ALU2)
            L.box(1.2f, 1.25f + k * 0.5f, 0f, 0.14f, 0.1f, 0.3f, STEEL_D)
            L.box(-1.2f, 1.25f + k * 0.5f, 0f, 0.14f, 0.1f, 0.3f, STEEL_D)
        }
        L.sph(0f, 3.02f, 0f, 1.0f, 0.38f, 1.0f, Col.rgb(0xD8DEE3))
        L.sph(0f, 3.4f, 0f, 0.14f, 0.14f, 0.14f, DARK)
        // plates stacked on the front ledge
        val n = s.stockA
        for (k in 0 until n) {
            val col = k % 2
            val lvl = k / 2
            val ox = -0.5f + col * 1.0f
            L.cyl(ox, 1.06f + lvl * 0.11f, 1.55f, 0.36f, 0.07f, WHITE)
            L.sphLo(ox - 0.1f, 1.14f + lvl * 0.11f, 1.5f, 0.1f, 0.08f, 0.1f, MOMO)
            L.sphLo(ox + 0.1f, 1.14f + lvl * 0.11f, 1.6f, 0.1f, 0.08f, 0.1f, MOMO)
        }
        zoneDecor(Layout.ID_MOKTU, Atlas.I_PLATE, inZone, pulse, Col.tint(1f, 0.95f, 0.7f, 1f))
        if (s.stockA > 0) bIcon(SB, Atlas.I_PLATE, x + 1.6f, 2.0f + 0.1f * sin(t * 4f), z + 1.2f, 0.8f, WHITE)
    }

    private fun drawCauldron(s: Station, inZone: Boolean, pulse: Float) {
        val x = s.x
        val z = s.z
        shadow(x, z, 2.8f)
        L.loadId(); L.translate(x, 0f, z)
        for (k in 0 until 3) {
            val a = k * 2.094f
            L.push()
            L.rotY(a)
            L.translate(0.7f, 0.3f, 0f)
            L.rotZ(0.35f)
            L.box(0f, 0f, 0f, 0.1f, 0.8f, 0.1f, POT)
            L.pop()
        }
        L.cyl(0f, 1.0f, 0f, 0.95f, 0.8f, POT)
        L.cyl(0f, 1.42f, 0f, 1.0f, 0.1f, Col.rgb(0x4A4A52))
        val f = (s.level / g.cauldronCap()).coerceIn(0f, 1f)
        if (f > 0.03f) {
            val flash = s.flash * 0.1f
            L.cyl(0f, 0.9f + 0.48f * f + flash, 0f, 0.84f, 0.05f, JHOL_COL)
            L.sphLo(0.2f, 0.95f + 0.48f * f, 0.1f, 0.12f, 0.06f, 0.12f, Col.emis(0xFF8A5A, 60))
        }
        L.cyl(0.9f, 0.9f, 0.0f, 0.1f, 0.5f, Col.rgb(0x6A6A74))
        L.box(0f, 1.72f, 0f, 0.12f, 0.5f, 0.12f, WOOD_D)
        val low = f < 0.2f
        val col = if (low) Col.tint(1f, 0.55f, 0.5f, 1f) else Col.tint(1f, 0.8f, 0.75f, 1f)
        zoneDecor(Layout.ID_CAULDRON, Atlas.I_JHOL, inZone, pulse, col)
        bIcon(SB, Atlas.I_CAULDRON, x, 2.9f + 0.1f * sin(t * 2f), z, 1.0f, WHITE)
        if (s.level < Game.JHOL_DRAW || low) {
            val sc = 1f + 0.18f * sin(g.alertPulse)
            bIcon(SO, Atlas.I_EXCL, x, 3.9f, z, 1.0f * sc, WHITE)
        }
    }

    private fun drawTable(i: Int, s: Station) {
        val x = s.x
        val z = s.z
        shadow(x, z + 0.5f, 3.4f)
        L.loadId(); L.translate(x, 0f, z)
        L.box(0f, 0.85f, 0f, 2.1f, 0.1f, 1.4f, WOOD)
        L.box(0f, 0.915f, 0f, 2.0f, 0.03f, 1.3f, RED)
        L.box(0f, 0.93f, 0f, 0.5f, 0.02f, 1.3f, WHITE)
        for (a in 0 until 4) L.box(-0.9f + (a % 2) * 1.8f, 0.4f, -0.5f + (a / 2) * 1.0f, 0.12f, 0.8f, 0.12f, WOOD_D)
        L.cyl(0f, 1.0f, 0.45f, 0.14f, 0.14f, Col.rgb(0xE8601C))
        for (n in 0 until s.stockA) {
            val ox = -0.5f + (n % 2) * 1.0f
            val oz = -0.2f + (n / 2) * 0.5f
            L.cyl(ox, 0.98f, oz, 0.32f, 0.07f, WHITE)
            L.sphLo(ox - 0.1f, 1.06f, oz, 0.1f, 0.08f, 0.1f, MOMO)
            L.sphLo(ox + 0.1f, 1.06f, oz + 0.05f, 0.1f, 0.08f, 0.1f, MOMO)
        }
        // stools
        for (sd in 0 until 2) {
            val sx = Layout.seatX(i, sd) - x
            val sz = Layout.seatZ(i) - z + 0.08f
            L.cyl(sx, 0.2f, sz, 0.26f, 0.4f, WOOD_D)
            L.cyl(sx, 0.43f, sz, 0.3f, 0.06f, Col.rgb(0xCC3B2E))
        }
    }

    private fun drawKettle(i: Int, s: Station, inZone: Boolean, pulse: Float) {
        val x = s.x
        val z = s.z
        shadow(x, z, 3.0f)
        L.loadId(); L.translate(x, 0f, z)
        L.box(0f, 0.5f, 0f, 1.9f, 1.0f, 1.2f, Col.rgb(0x7A4B2A))
        L.box(0f, 1.02f, 0f, 2.0f, 0.06f, 1.3f, WOOD)
        L.box(0f, 0.3f, 0.62f, 1.1f, 0.25f, 0.05f, FIRE)
        L.cyl(0.1f, 1.45f, 0f, 0.45f, 0.75f, STEEL)
        L.cyl(0.1f, 1.88f, 0f, 0.3f, 0.1f, STEEL_D)
        L.push()
        L.translate(0.55f, 1.5f, 0f)
        L.rotZ(-0.7f)
        L.box(0f, 0.2f, 0f, 0.1f, 0.5f, 0.1f, STEEL)
        L.pop()
        L.box(0.1f, 2.05f, 0f, 0.7f, 0.06f, 0.06f, DARK)
        for (n in 0 until s.stockA) {
            val ox = -0.7f + (n % 4) * 0.28f
            val oz = 0.35f + (n / 4) * 0.25f
            L.cyl(ox, 1.17f, oz, 0.1f, 0.16f, CLAY)
        }
        // awning
        L.box(-0.85f, 1.6f, -0.55f, 0.08f, 3.2f, 0.08f, WOOD_D)
        L.box(0.85f, 1.6f, -0.55f, 0.08f, 3.2f, 0.08f, WOOD_D)
        L.push()
        L.translate(0f, 3.1f, 0f)
        L.rotX(-0.2f)
        for (k in 0 until 5) L.box(-0.96f + k * 0.48f, 0f, 0f, 0.48f, 0.07f, 1.9f, if (k % 2 == 0) Col.rgb(0xD04A3A) else WHITE)
        L.pop()
        zoneDecor(Layout.ID_KETTLE, Atlas.I_CUP, inZone, pulse, Col.tint(1f, 0.85f, 0.6f, 1f))
        bIcon(SB, Atlas.I_KETTLE, x, 3.9f + 0.1f * sin(t * 2f), z, 1.0f, WHITE)
    }

    private fun drawBench(i: Int, s: Station) {
        val x = s.x
        val z = s.z
        shadow(x, z + 0.4f, 3.0f)
        L.loadId(); L.translate(x, 0f, z)
        // low table north of the seats
        L.box(0f, 0.7f, -0.05f, 1.9f, 0.08f, 0.8f, WOOD)
        L.box(-0.8f, 0.35f, -0.05f, 0.1f, 0.7f, 0.1f, WOOD_D)
        L.box(0.8f, 0.35f, -0.05f, 0.1f, 0.7f, 0.1f, WOOD_D)
        for (n in 0 until s.stockA) L.cyl(-0.5f + n * 0.5f, 0.84f, -0.05f, 0.11f, 0.18f, CLAY)
        if (g.lvl(Up.UPSELL) > 0) {
            L.cone(0.7f, 0.85f, 0.1f, 0.14f, 0.16f, Col.rgb(0xD39B4A))
            L.cyl(0.35f, 0.76f, 0.1f, 0.1f, 0.03f, Col.rgb(0xFFB02E))
        }
        // bench seat
        L.box(0f, 0.42f, 0.95f, 2.0f, 0.12f, 0.55f, WOOD)
        L.box(-0.85f, 0.2f, 0.95f, 0.12f, 0.4f, 0.4f, WOOD_D)
        L.box(0.85f, 0.2f, 0.95f, 0.12f, 0.4f, 0.4f, WOOD_D)
        if (g.lvl(Up.NOCAMP) > 0) {
            L.box(0f, 1.3f, -0.55f, 0.1f, 1.2f, 0.1f, WOOD_D)
            L.box(0f, 1.8f, -0.55f, 1.2f, 0.5f, 0.08f, Col.rgb(0xE53935))
            bIcon(SB, Atlas.I_EXCL, x, 2.7f, z - 0.5f, 0.6f, WHITE)
        }
    }

    private fun drawCash(i: Int, s: Station) {
        val px = Layout.PX[i]
        val pz = Layout.PZ[i]
        shadow(px, pz, 1.8f, 0.3f)
        L.loadId()
        L.translate(px, 0f, pz)
        val stacks = (s.bills + 9) / 10
        for (k in 0 until stacks) {
            val cnt = if (k == stacks - 1) s.bills - k * 10 else 10
            val h = cnt * 0.065f
            val ox = (k % 3) * 0.52f - 0.52f
            val oz = (k / 3) * 0.4f - 0.4f
            val col = if (k % 3 == 2) BILL_RED else GREEN
            L.push()
            L.translate(ox, 0f, oz)
            L.rotY(k * 0.37f)
            L.box(0f, h * 0.5f + 0.02f, 0f, 0.46f, h, 0.28f, col)
            L.box(0f, h + 0.025f, 0f, 0.44f, 0.03f, 0.26f, if (k % 3 == 2) Col.rgb(0xF08A7E) else Col.rgb(0x8FE29A))
            L.box(0f, h * 0.5f + 0.02f, 0f, 0.12f, h + 0.01f, 0.29f, Col.rgb(0xF5EBD2))
            L.pop()
        }
    }

    // ------------------------------------------------------------------ people
    private fun leg(side: Float, sw: Float, seated: Boolean, pants: Int) {
        L.push()
        L.translate(side * 0.15f, 0.72f, 0f)
        if (seated) {
            L.rotX(-1.5f)
            L.box(0f, -0.18f, 0f, 0.2f, 0.36f, 0.22f, pants)
            L.translate(0f, -0.36f, 0f)
            L.rotX(1.5f)
            L.box(0f, -0.2f, 0f, 0.2f, 0.4f, 0.22f, pants)
            L.box(0f, -0.42f, 0.06f, 0.22f, 0.1f, 0.32f, SHOE)
        } else {
            L.rotX(sw * side)
            L.box(0f, -0.34f, 0f, 0.2f, 0.68f, 0.22f, pants)
            L.box(0f, -0.7f, 0.05f, 0.22f, 0.1f, 0.32f, SHOE)
        }
        L.pop()
    }

    private fun arm(side: Float, ang: Float, shirt: Int, skin: Int) {
        L.push()
        L.translate(side * 0.38f, 1.34f, 0f)
        L.rotX(ang)
        L.box(0f, -0.26f, 0f, 0.15f, 0.52f, 0.15f, shirt)
        L.sph(0f, -0.56f, 0f, 0.09f, 0.09f, 0.09f, skin)
        L.pop()
    }

    /** armMode: 0 swing, 1 forward (carrying), 2 eating. style: see switch below. */
    private fun person(
        x: Float, y0: Float, z: Float, heading: Float, phase: Float, mv: Float, seated: Boolean,
        shirt: Int, pants: Int, skin: Int, hair0: Int, style: Int, armMode: Int, spin: Float
    ) {
        val sw = sin(phase) * 0.75f * mv
        val bob = if (seated) 0f else abs(sin(phase)) * 0.07f * mv
        val dy = if (seated) -0.27f else 0f
        val hair = if (style == 3) PINKHAIR else hair0
        L.push()
        L.translate(x, y0 + bob + dy, z)
        L.rotY(heading + spin)
        leg(-1f, sw, seated, pants)
        leg(1f, sw, seated, pants)
        L.box(0f, 1.08f, 0f, 0.56f, 0.62f, 0.34f, shirt)
        val a1: Float
        val a2: Float
        when (armMode) {
            1 -> { a1 = -1.35f; a2 = -1.35f }
            2 -> { a1 = -0.9f + 0.35f * sin(t * 9f); a2 = -0.3f }
            else -> { a1 = sw * 1.1f; a2 = -sw * 1.1f }
        }
        arm(-1f, a1, shirt, skin)
        arm(1f, a2, shirt, skin)
        L.sph(0f, 1.64f, 0f, 0.27f, 0.29f, 0.27f, skin)
        L.sph(0f, 1.72f, -0.03f, 0.285f, 0.2f, 0.285f, hair)
        L.box(-0.09f, 1.66f, 0.245f, 0.05f, 0.07f, 0.03f, EYE)
        L.box(0.09f, 1.66f, 0.245f, 0.05f, 0.07f, 0.03f, EYE)
        when (style) {
            0 -> {
                L.box(0f, 1.0f, 0.18f, 0.5f, 0.55f, 0.04f, WHITE)
                L.cyl(0f, 1.95f, 0f, 0.25f, 0.2f, TOPI)
                L.cyl(0f, 1.9f, 0f, 0.256f, 0.06f, TOPI2)
            }
            2 -> {
                L.sph(0f, 1.6f, -0.1f, 0.34f, 0.32f, 0.3f, shirt)
                L.box(0f, 1.12f, -0.27f, 0.42f, 0.5f, 0.2f, BAG)
            }
            3 -> {
                L.box(0f, 1.67f, 0.26f, 0.34f, 0.07f, 0.03f, EYE)
                L.box(0.5f, 1.45f, 0.35f, 0.08f, 0.3f, 0.08f, DARK)
                L.box(0.5f, 1.65f, 0.35f, 0.22f, 0.14f, 0.04f, GLOW)
            }
            4 -> {
                L.sph(0f, 1.72f, 0f, 0.32f, 0.28f, 0.32f, HELMET)
                L.box(0f, 1.66f, 0.26f, 0.4f, 0.1f, 0.08f, VISOR)
            }
            5 -> {
                L.cyl(0f, 1.95f, 0f, 0.5f, 0.04f, HAT)
                L.cyl(0f, 2.03f, 0f, 0.26f, 0.15f, HAT)
                L.box(0f, 1.15f, -0.3f, 0.5f, 0.65f, 0.28f, ORANGE)
            }
            6 -> L.cyl(0f, 1.82f, 0f, 0.285f, 0.07f, RED)
            7 -> {
                L.sph(0f, 1.8f, 0f, 0.3f, 0.17f, 0.3f, RED)
                L.box(0f, 1.76f, 0.28f, 0.3f, 0.04f, 0.16f, RED)
            }
            8 -> {
                L.box(0f, 1.4f, -0.18f, 0.42f, 0.7f, 0.1f, hair0)
                L.box(0f, 1.15f, 0f, 0.6f, 0.1f, 0.4f, SCARF)
            }
            9 -> {
                L.box(0f, 1.67f, 0.26f, 0.32f, 0.06f, 0.03f, EYE)
                L.box(0f, 1.1f, 0.18f, 0.08f, 0.4f, 0.03f, RED)
            }
        }
        L.pop()
    }

    private fun itemStep(item: Int): Float = when (item) {
        Item.MAIDA -> 0.3f
        Item.MEAT -> 0.26f
        Item.JHOL -> 0.42f
        Item.PLATE -> 0.1f
        else -> 0.22f
    }

    private fun drawItem(item: Int, detail: Boolean) {
        when (item) {
            Item.MAIDA -> {
                L.box(0f, 0.14f, 0f, 0.62f, 0.28f, 0.42f, CREAM)
                L.box(0f, 0.14f, 0f, 0.64f, 0.07f, 0.2f, Col.rgb(0xD9532D))
            }
            Item.MEAT -> {
                L.box(0f, 0.12f, 0f, 0.5f, 0.24f, 0.36f, MEATC)
                L.box(0f, 0.25f, 0f, 0.3f, 0.04f, 0.2f, PINK)
            }
            Item.JHOL -> {
                L.cyl(0f, 0.2f, 0f, 0.2f, 0.4f, RED)
                L.cyl(0f, 0.42f, 0f, 0.12f, 0.06f, POT)
            }
            Item.PLATE -> {
                L.cyl(0f, 0.04f, 0f, 0.34f, 0.07f, WHITE)
                if (detail) {
                    L.sphLo(-0.1f, 0.1f, 0f, 0.1f, 0.08f, 0.1f, MOMO)
                    L.sphLo(0.1f, 0.1f, 0.04f, 0.1f, 0.08f, 0.1f, MOMO)
                }
            }
            Item.CUP -> {
                L.cyl(0f, 0.11f, 0f, 0.13f, 0.22f, CLAY)
                L.cyl(0f, 0.22f, 0f, 0.115f, 0.02f, Col.rgb(0xE9C79B))
            }
        }
    }

    private fun drawPlayer() {
        val h = g.heading
        shadow(g.px, g.pz, 1.1f)
        // glowing green ring
        val pulse = 1f + 0.06f * sin(g.ringPulse)
        SG.quad(g.px, 0.05f, g.pz, gRX, 0f, gRZ, gUX, 0f, gUZ, 1.35f * pulse, 1.35f * pulse, Atlas.RING, Col.tint(0.25f, 1f, 0.4f, 1f))
        SG.quad(g.px, 0.048f, g.pz, gRX, 0f, gRZ, gUX, 0f, gUZ, 1.15f, 1.15f, Atlas.SOFT, Col.tint(0.2f, 0.9f, 0.35f, 0.35f))
        person(g.px, 0f, g.pz, h, g.walkPhase, g.moveAmt, false, WHITE, Col.rgb(0x2C3E50), Col.rgb(0xE8B48A), HELMET, 0,
            if (g.carryN > 0) 1 else 0, 0f)
        if (g.carryN > 0) {
            val n = g.carryN
            var total = 0f
            for (k in 0 until n) total += itemStep(g.carry[k])
            val ks = if (total > 3.2f) 3.2f / total else 1f
            val bx = g.px + sin(h) * 0.58f
            val bz = g.pz + cos(h) * 0.58f
            var y = 0.95f
            for (k in 0 until n) {
                val it = g.carry[k]
                val f = (k + 1) * 0.07f
                L.loadId()
                L.translate(bx + g.swayX * (k + 1) * 0.55f, y, bz + g.swayZ * (k + 1) * 0.55f)
                L.rotY(h + sin(t * 3f + k) * 0.02f * f)
                drawItem(it, n <= 8 || k >= n - 3)
                y += itemStep(it) * ks
            }
            if (n >= g.carryCap()) {
                bIcon(SO, Atlas.I_ARROW, g.px, y + 0.9f + 0.1f * sin(t * 6f), g.pz, 0.7f, WHITE)
            }
        }
    }

    private fun drawStaff() {
        for (k in 0 until g.staffN) {
            val s = g.staff[k]
            if (!s.active) continue
            shadow(s.x, s.z, 1.0f)
            val col = Game.STAFF_COL[s.role]
            val style = when (s.role) {
                Role.KANCHA -> 6
                Role.JHOL -> 7
                Role.DIDI -> 8
                else -> 9
            }
            val skin = Game.SKINS[(k * 3 + 1) % 4]
            val mv = if (s.moving) 1f else 0f
            person(s.x, 0f, s.z, s.heading, s.walk, mv, false, col, Game.PANTS[k % 4], skin, Game.HAIRS[k % 4], style,
                if (s.n > 0) 1 else 0, 0f)
            if (s.n > 0) {
                var y = 0.95f
                for (n in 0 until s.n) {
                    L.loadId()
                    L.translate(s.x + sin(s.heading) * 0.55f, y, s.z + cos(s.heading) * 0.55f)
                    L.rotY(s.heading)
                    drawItem(s.item, true)
                    y += itemStep(s.item)
                }
            }
        }
    }

    private fun drawCustomers() {
        for (i in 0 until Game.MAXC) {
            val c = g.cs[i]
            if (!c.active) continue
            val seated = c.state == Game.CS_WAIT || c.state == Game.CS_EAT || c.state == Game.CS_CAMP
            val moving = c.state == Game.CS_TO_QUEUE || c.state == Game.CS_TO_SEAT || c.state == Game.CS_LEAVE
            val style = when (c.type) {
                CT.STUDENT -> 2
                CT.VLOGGER -> 3
                CT.BIKER -> 4
                CT.TOURIST -> 5
                else -> 1
            }
            var y0 = 0f
            var spin = 0f
            if (c.state == Game.CS_FLIP) {
                y0 = abs(sin(c.flip * 11f)) * 0.5f
                spin = c.flip * 14f
            }
            shadow(c.x, c.z, 1.0f)
            val armMode = if (c.state == Game.CS_EAT && !c.wantJhol) 2 else 0
            person(c.x, y0, c.z, c.heading, c.walk, if (moving) 1f else 0f, seated, c.shirt, c.pants, c.skin, c.hair, style, armMode, spin)
            if (c.type >= CT.VLOGGER) {
                val sc = 0.3f + 0.05f * sin(t * 5f + i)
                bIcon(SB, Atlas.STAR, c.x, 2.25f + 0.1f * sin(t * 3f + i), c.z, sc * 2f, Col.tint(1f, 0.85f, 0.25f, 1f))
            }
            if (c.bubble >= 0) {
                var sc = 1f
                var a = 1f
                if (c.bubble == Bub.JHOL) sc = 1f + 0.1f * sin(g.alertPulse + i)
                if (c.state == Game.CS_WAIT && c.patience < c.patMax * 0.3f) {
                    a = 0.65f + 0.35f * sin(t * 12f)
                    sc = 1f + 0.08f * sin(t * 12f)
                }
                if (c.state == Game.CS_EAT && c.wantJhol && c.jholAnger > 0.6f) sc *= 1f + 0.06f * sin(t * 20f)
                val hh = 0.5f * sc
                val hw = 1.0f * sc
                SO.quad(c.x, 2.15f + hh, c.z, rX, rY, rZ, uX, uY, uZ, hw, hh, Atlas.B0 + c.bubble, Col.tint(1f, 1f, 1f, a))
            }
        }
    }

    // ------------------------------------------------------------------ particles
    private fun drawParticles() {
        for (i in 0 until g.pN) {
            val k = g.pAge[i] / g.pLife[i]
            val x = g.pX[i]
            val y = g.pY[i]
            val z = g.pZ[i]
            when (g.pKind[i]) {
                Game.P_STEAM -> {
                    val sz = g.pSize[i] * (1f + k * 1.8f)
                    SB.quad(x, y, z, rX, rY, rZ, uX, uY, uZ, sz, sz, Atlas.SOFT, Col.tint(1f, 1f, 1f, 0.5f * (1f - k)))
                }
                Game.P_RED -> {
                    val sz = g.pSize[i] * (1f + k * 1.4f)
                    SB.quad(x, y, z, rX, rY, rZ, uX, uY, uZ, sz, sz, Atlas.SOFT, Col.tint(1f, 0.18f, 0.12f, 0.7f * (1f - k)))
                }
                Game.P_SMOKE -> {
                    val sz = g.pSize[i] * (1f + k * 1.6f)
                    SB.quad(x, y, z, rX, rY, rZ, uX, uY, uZ, sz, sz, Atlas.SOFT, Col.tint(0.75f, 0.75f, 0.78f, 0.45f * (1f - k)))
                }
                Game.P_SPARK -> {
                    val sz = g.pSize[i] * (1f - k * 0.7f)
                    val a = ((1f - k) * 255f).toInt().coerceIn(0, 255)
                    SB.quad(x, y, z, rX, rY, rZ, uX, uY, uZ, sz, sz, Atlas.STAR, (g.pCol[i] and 0x00FFFFFF) or (a shl 24))
                }
                Game.P_FLY -> {
                    val f = g.pFlag[i]
                    val sx = if ((f and Game.F_START_PLAYER) != 0) g.px else x
                    val sy = if ((f and Game.F_START_PLAYER) != 0) 1.4f else y
                    val sz = if ((f and Game.F_START_PLAYER) != 0) g.pz else z
                    val ex = if ((f and Game.F_END_PLAYER) != 0) g.px else g.pVX[i]
                    val ey = if ((f and Game.F_END_PLAYER) != 0) 1.4f else g.pVY[i]
                    val ez = if ((f and Game.F_END_PLAYER) != 0) g.pz else g.pVZ[i]
                    val e = k * k * (3f - 2f * k)
                    L.loadId()
                    L.translate(sx + (ex - sx) * e, sy + (ey - sy) * e + sin(3.1416f * e) * 0.9f, sz + (ez - sz) * e)
                    L.rotY(k * 9f)
                    val s = g.pSize[i]
                    L.box(0f, 0f, 0f, s, s * 0.5f, s * 1.4f, g.pCol[i])
                }
                Game.P_JET -> {
                    val e = k
                    val sx = g.pX[i]
                    val sz = g.pZ[i]
                    L.loadId()
                    L.translate(sx + (g.pVX[i] - sx) * e, g.pY[i] + (g.pVY[i] - g.pY[i]) * e + sin(3.1416f * e) * 0.8f, sz + (g.pVZ[i] - sz) * e)
                    val s = g.pSize[i]
                    L.sphLo(0f, 0f, 0f, s, s, s, Col.emis(0xE0391F, 120))
                }
                Game.P_NUM -> {
                    val a = (1f - k * k * k)
                    val sc = 0.55f + 0.25f * min(1f, k * 6f)
                    num(SO, false, g.pVal[i], x, y, z, sc, Col.tint(0.55f, 1f, 0.55f, a), true)
                }
            }
        }
    }
}
