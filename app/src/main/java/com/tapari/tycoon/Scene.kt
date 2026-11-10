package com.tapari.tycoon

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class Scene(private val g: Game) {
    private lateinit var L: LitBatch
    private lateinit var SG: SpriteBatch
    private lateinit var SB: SpriteBatch
    private lateinit var SO: SpriteBatch
    private val rig = Rig()
    private val city = City(g)
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
    private val SHOE = Col.rgb(0x2B2B33)
    private val ALU = Col.rgb(0xD0D6DC)
    private val ALU2 = Col.rgb(0xB5BDC6)
    private val POT = Col.rgb(0x2A2A2E)
    private val JHOL_COL = Col.emis(0xE0391F, 170)
    private val FIRE = Col.emis(0xFF8A1F, 60)
    private val TOPI2 = Col.rgb(0x1F1F28)
    private val ORANGE = Col.rgb(0xF28C28)
    private val HAT = Col.rgb(0xE9D79B)
    private val HELMET = Col.rgb(0x1C1C20)
    private val VISOR = Col.rgb(0xFFC93C)
    private val PINKHAIR = Col.rgb(0xFF6FB5)
    private val ACCS = intArrayOf(
        Col.rgb(0xD6382F), Col.rgb(0x2E6FD8), Col.rgb(0xF2B134), Col.rgb(0x3FAF55),
        Col.rgb(0x8E44AD), Col.rgb(0xF28C28), Col.rgb(0x1ABC9C), Col.rgb(0xE84393)
    )
    private val TOPIS = intArrayOf(Col.rgb(0xD9362B), Col.rgb(0x1F1F28), Col.rgb(0xE98A1F), Col.rgb(0x3A3F8F))
    private val TSKIN = intArrayOf(Col.rgb(0xF6D2B5), Col.rgb(0xF2C6A0), Col.rgb(0xFFDDC4), Col.rgb(0xE9B98F))

    fun draw(lit: LitBatch, sg: SpriteBatch, sb: SpriteBatch, so: SpriteBatch, view: FloatArray) {
        L = lit; SG = sg; SB = sb; SO = so
        t = g.time
        rX = view[0]; rY = view[4]; rZ = view[8]
        uX = view[1]; uY = view[5]; uZ = view[9]
        val yaw = g.camYaw
        gRX = cos(yaw); gRZ = -sin(yaw)
        gUX = -sin(yaw); gUZ = -cos(yaw)
        L.reset(); SG.reset(); SB.reset(); SO.reset()
        rig.L = L
        rig.t = t
        city.draw(L, SB, rig, t)
        for (i in 0 until Game.NST) drawStation(i)
        drawCustomers()
        drawStaff()
        if (!g.paused) drawPlayer()
        drawGold()
        drawParticles()
        if (!g.paused) drawGuide()
        drawBubbles()
    }

    private fun vis(x: Float, z: Float): Boolean = g.paused || (abs(x - g.px) < 11f && abs(z - g.pz) < 17f)

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

    private fun num(b: SpriteBatch, ground: Boolean, v: Double, cx: Float, cy: Float, cz: Float, h: Float, col: Int, coin: Boolean, xPrefix: Boolean = false) {
        val n: Int
        if (xPrefix) {
            gl[0] = Glyph.X
            n = Glyph.formatAt(v, gl, 1)
        } else {
            n = Glyph.format(v, gl)
        }
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

    // ------------------------------------------------------------------ stations
    private fun drawStation(i: Int) {
        val s = g.st[i]
        val x = s.x
        val z = s.z
        if (!s.avail) return
        if (!s.unlocked) {
            drawPad(i, s)
            return
        }
        if (s.kind == SK.BUILDING) return
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
        val sz = r * (if (inZone) 2.3f else 2.1f)
        SG.quad(zx, 0.045f, zz, gRX, 0f, gRZ, gUX, 0f, gUZ, sz * 0.5f, sz * 0.5f, Atlas.RING, Col.tint(
            ((tint and 255) / 255f), (((tint shr 8) and 255) / 255f), (((tint shr 16) and 255) / 255f), a))
        SG.quad(zx, 0.05f, zz, gRX, 0f, gRZ, gUX, 0f, gUZ, 0.55f, 0.55f, icon, Col.tint(1f, 1f, 1f, 0.85f))
    }

    private fun drawPad(i: Int, s: Station) {
        val hx = Layout.HX[i]
        val hz = Layout.HZ[i]
        val frac = if (s.cost > 0.0) (s.paid / s.cost).toFloat().coerceIn(0f, 1f) else 0f
        val active = abs(g.px - s.x) < hx && abs(g.pz - s.z) < hz
        val remain = s.cost - s.paid
        val afford = g.money >= remain
        val pulse = 0.5f + 0.5f * sin(t * 4f + i)
        val big = s.kind == SK.BUILDING
        L.loadId()
        L.translate(s.x, 0f, s.z)
        val border = if (active) Col.rgb(0xFFE58A) else if (afford) Col.emis(0xFFD54A, (110 + 120 * pulse).toInt()) else WHITE
        L.box(0f, 0.03f, 0f, 2f * hx + 0.2f, 0.06f, 2f * hz + 0.2f, border)
        L.box(0f, 0.045f, 0f, 2f * hx, 0.07f, 2f * hz, DARK)
        if (frac > 0f) L.box(-hx + hx * frac, 0.075f, 0f, 2f * hx * frac, 0.03f, 2f * hz - 0.1f, Col.emis(0x1F7A3A, 150))
        if (active && g.padDwellT > 0f) {
            val f = (g.padDwellT / 0.4f).coerceIn(0f, 1f)
            L.box(-hx + hx * f, 0.1f, hz - 0.2f, 2f * hx * f, 0.04f, 0.14f, Col.emis(0xFFE58A, 150))
        }
        val icon = when (s.kind) {
            SK.TABLE -> Atlas.I_TABLE
            SK.KETTLE -> Atlas.I_KETTLE
            SK.BUILDING -> Atlas.I_BUILD
            else -> Atlas.I_BENCH
        }
        val bob = if (active) 0.05f else 0f
        val isz = if (big) 3.4f else min(1.6f, hz * 1.35f)
        gIcon(icon, s.x, 0.12f + bob, s.z - hz * (if (big) 0.12f else 0.22f), isz, WHITE)
        num(SG, true, remain, s.x, 0.12f, s.z + hz * (if (big) 0.66f else 0.58f), if (big) 0.95f else 0.62f, if (active) Col.rgb(0xFFE58A) else WHITE, true)
    }

    private fun drawMoktu(s: Station, inZone: Boolean, pulse: Float) {
        val x = s.x
        val z = s.z
        shadow(x, z, 4.2f)
        L.loadId(); L.translate(x, 0f, z)
        L.box(0f, 0.5f, 0f, 2.9f, 1.0f, 2.5f, Col.rgb(0x3B3F46))
        L.box(0f, 0.3f, 1.26f, 1.8f, 0.3f, 0.06f, Col.emis(0xFF8A1F, 40 + (45f * (0.5f + 0.5f * sin(t * 17f))).toInt()))
        L.box(0f, 0.97f, 1.55f, 2.2f, 0.08f, 0.8f, STEEL)
        val bounce = s.flash * 0.05f * sin(t * 40f)
        for (k in 0 until 4) {
            L.cyl(0f, 1.25f + k * 0.5f + bounce * k, 0f, 1.15f - k * 0.03f, 0.5f, if (k % 2 == 0) ALU else ALU2)
            L.box(1.2f, 1.25f + k * 0.5f, 0f, 0.14f, 0.1f, 0.3f, STEEL_D)
            L.box(-1.2f, 1.25f + k * 0.5f, 0f, 0.14f, 0.1f, 0.3f, STEEL_D)
        }
        val rat = if (s.raw > 0) sin(t * 38f) * 0.03f else 0f
        val lift = abs(rat) * 1.6f
        L.sph(rat, 3.02f + lift, 0f, 1.0f, 0.38f, 1.0f, Col.rgb(0xD8DEE3))
        L.sph(rat, 3.4f + lift, 0f, 0.14f, 0.14f, 0.14f, DARK)
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
    private fun custLook(c: Cust, i: Int) {
        val r = rig
        r.skin = c.skin; r.hair = c.hair; r.shirt = c.shirt; r.pants = c.pants
        r.shoe = SHOE
        r.hatCol = WHITE
        r.accent = ACCS[(c.look + i) and 7]
        r.hairStyle = HairS.SHORT
        r.hat = HatS.NONE
        r.acc = 0
        when (c.type) {
            CT.STUDENT -> {
                r.hat = HatS.HOOD
                r.acc = if ((c.look and 1) == 0) (Acc.PACK or Acc.GLASSES) else Acc.PACK
            }
            CT.VLOGGER -> {
                r.hairStyle = HairS.POMP
                r.hair = PINKHAIR
                r.acc = Acc.RING
                r.accent = Col.rgb(0x8E44AD)
            }
            CT.BIKER -> {
                r.hat = HatS.HELMET
                r.hatCol = HELMET
                r.acc = Acc.JACKET
                r.accent = VISOR
                r.pants = DARK
                r.shoe = Col.rgb(0x3A3A44)
            }
            CT.TOURIST -> {
                r.hat = HatS.SUN
                r.hatCol = HAT
                r.acc = Acc.PACK or Acc.CAMERA
                r.accent = Col.rgb(0x2FB5A8)
                r.skin = TSKIN[c.look and 3]
                r.hairStyle = if (c.look > 3) HairS.SWOOP else HairS.SHORT
                r.hair = if (c.look > 3) Col.rgb(0xC98F3A) else Col.rgb(0x7A4A22)
            }
            else -> when (c.look) {
                0 -> { r.hat = HatS.TOPI; r.hatCol = TOPIS[(i) and 3] }
                1 -> { r.hairStyle = HairS.SWOOP; r.acc = Acc.TIE }
                2 -> { r.hairStyle = HairS.LONG; r.acc = Acc.SCARF or Acc.KURTA }
                3 -> { r.hat = HatS.CAP; r.hatCol = r.accent }
                4 -> { r.hairStyle = HairS.BRAID; r.acc = Acc.KURTA }
                5 -> { r.acc = Acc.GLASSES }
                6 -> { r.hairStyle = HairS.BUN; r.acc = Acc.SCARF }
                else -> { r.hairStyle = HairS.NONE; r.hat = HatS.TOPI; r.hatCol = TOPIS[(i + 1) and 3]; r.acc = Acc.GLASSES or Acc.KURTA }
            }
        }
    }

    private fun drawCustomers() {
        for (i in 0 until Game.MAXC) {
            val c = g.cs[i]
            if (!c.active) continue
            if (!vis(c.x, c.z)) continue
            val seated = c.state == Game.CS_WAIT || c.state == Game.CS_EAT || c.state == Game.CS_CAMP
            val moving = c.state == Game.CS_TO_QUEUE || c.state == Game.CS_TO_SEAT || c.state == Game.CS_LEAVE
            var y0 = 0f
            var spin = 0f
            if (c.state == Game.CS_FLIP) {
                y0 = abs(sin(c.flip * 11f)) * 0.5f
                spin = c.flip * 14f
            }
            shadow(c.x, c.z, 1.1f)
            custLook(c, i)
            val r = rig
            var arm = Pose.WALK
            var mood = 0
            val phoney = c.type == CT.VLOGGER || (c.type == CT.STUDENT && (c.look and 1) == 0)
            when (c.state) {
                Game.CS_WAIT -> { mood = if (c.patience < c.patMax * 0.3f) 2 else 0; if (phoney) arm = Pose.PHONE }
                Game.CS_EAT -> {
                    if (c.wantJhol) {
                        arm = Pose.YELL
                        mood = if (c.jholAnger > 0.6f) 2 else 3
                    } else {
                        arm = Pose.EAT
                        mood = 4
                    }
                }
                Game.CS_CAMP -> { arm = if (phoney) Pose.PHONE else Pose.WALK; mood = if (sin(t * 7f + i) > 0.2f) 4 else 1 }
                Game.CS_FLIP -> { arm = Pose.ANGRY; mood = 2 }
                Game.CS_LEAVE -> { if (c.angry) { arm = Pose.ANGRY; mood = 2 } else mood = 1 }
                Game.CS_QUEUE -> { if (phoney) arm = Pose.PHONE; mood = if (c.timer > 18f) 2 else 0 }
                Game.CS_TO_QUEUE, Game.CS_TO_SEAT -> if (c.type == CT.VLOGGER) arm = Pose.PHONE
            }
            if (c.cheer > 0f) { arm = Pose.CHEER; mood = 1 }
            r.heading = c.heading
            r.phase = c.walk
            r.mv = if (moving) 1f else 0f
            r.seated = seated
            r.arm = arm
            r.spin = spin
            r.mood = mood
            r.seed = i * 1.37f
            r.hop = 0f
            r.lookX = 0f
            r.person(c.x, y0, c.z)
            if (c.state == Game.CS_WAIT && c.patience < c.patMax * 0.8f) bar(c.x, 2.15f, c.z, c.patience / c.patMax)
            else if (c.state == Game.CS_EAT && c.wantJhol) bar(c.x, 2.15f, c.z, 1f - c.jholAnger)
            if (c.type >= CT.VLOGGER && c.bubble < 0) {
                val sc = 0.3f + 0.05f * sin(t * 5f + i)
                bIcon(SB, Atlas.STAR, c.x, 2.85f + 0.1f * sin(t * 3f + i), c.z, sc * 2f, Col.tint(1f, 0.85f, 0.25f, 1f))
            }
        }
    }

    private fun bar(x: Float, y: Float, z: Float, f0: Float) {
        val f = f0.coerceIn(0f, 1f)
        val hw = 0.5f
        SB.quad(x, y, z, rX, rY, rZ, uX, uY, uZ, hw + 0.05f, 0.1f, Atlas.I_BAR, Col.tint(0.1f, 0.06f, 0.08f, 0.75f))
        val fw = hw * f
        val off = -hw + fw
        SB.quad(x + rX * off, y + rY * off, z + rZ * off, rX, rY, rZ, uX, uY, uZ, fw, 0.065f, Atlas.I_BAR,
            Col.tint(min(1f, 2f * (1f - f)), min(1f, 2f * f), 0.15f, 1f))
    }

    private fun drawGold() {
        if (!g.goldOn) return
        val gx = g.goldX
        val gz = g.goldZ
        val blink = if (g.goldT < 4f) (if (sin(t * 18f) > 0f) 1f else 0.35f) else 1f
        val gold = Col.emis(0xFFC21A, 150)
        shadow(gx, gz, 1.8f, 0.3f)
        val pulse = 1f + 0.12f * sin(t * 5f)
        SG.quad(gx, 0.05f, gz, gRX, 0f, gRZ, gUX, 0f, gUZ, 1.7f * pulse, 1.7f * pulse, Atlas.RING, Col.tint(1f, 0.82f, 0.2f, blink))
        SG.quad(gx, 0.048f, gz, gRX, 0f, gRZ, gUX, 0f, gUZ, 1.4f, 1.4f, Atlas.SOFT, Col.tint(1f, 0.8f, 0.2f, 0.35f * blink))
        if (blink > 0.5f) {
            val y = 1.15f + 0.25f * sin(t * 3f)
            L.loadId()
            L.translate(gx, y, gz)
            L.rotY(t * 2.4f)
            L.sphLo(0f, 0f, 0f, 0.62f, 0.5f, 0.62f, gold)
            L.cone(0f, 0.38f, 0f, 0.2f, 0.28f, Col.emis(0xFFE066, 150))
            for (k in 0 until 8) {
                val a = k * 0.7854f
                L.box(cos(a) * 0.4f, 0.22f, sin(a) * 0.4f, 0.07f, 0.28f, 0.07f, Col.emis(0xFFD54A, 120))
            }
            L.loadId()
            SB.quad(gx, 3.2f, gz, rX, rY, rZ, uX, uY, uZ, 0.9f, 3.2f, Atlas.SOFT, Col.tint(1f, 0.85f, 0.3f, 0.28f))
            for (k in 0 until 3) {
                val a = t * 2.2f + k * 2.094f
                SB.quad(gx + cos(a) * 0.9f, y + 0.2f * sin(t * 6f + k), gz + sin(a) * 0.9f, rX, rY, rZ, uX, uY, uZ, 0.17f, 0.17f, Atlas.STAR, Col.tint(1f, 0.92f, 0.4f, 1f))
            }
            bIcon(SO, Atlas.I_GOLD, gx, y + 1.6f + 0.15f * sin(t * 4f), gz, 1.0f, WHITE)
        }
    }

    private fun lblCol(id: Int): Int = when (id) {
        Lbl.PERFECT -> Col.tint(0.6f, 1f, 0.85f, 1f)
        Lbl.GOLD -> Col.tint(1f, 0.85f, 0.3f, 1f)
        Lbl.COMBO -> Col.tint(1f, 0.7f, 0.25f, 1f)
        Lbl.LEVEL -> Col.tint(0.6f, 0.9f, 1f, 1f)
        Lbl.SPEED -> Col.tint(0.8f, 1f, 1f, 1f)
        Lbl.LUCKY -> Col.tint(1f, 0.6f, 0.85f, 1f)
        Lbl.UNLOCK -> Col.tint(0.6f, 1f, 0.6f, 1f)
        else -> WHITE
    }

    private fun drawStaff() {
        for (k in 0 until g.staffN) {
            val s = g.staff[k]
            if (!s.active) continue
            if (!vis(s.x, s.z)) continue
            shadow(s.x, s.z, 1.1f)
            val r = rig
            r.skin = Game.SKINS[(k * 3 + 1) % 4]
            r.hair = Game.HAIRS[k % 4]
            r.pants = Game.PANTS[k % 4]
            r.shoe = SHOE
            r.hairStyle = HairS.SHORT
            r.hat = HatS.NONE
            r.acc = 0
            when (s.role) {
                Role.KANCHA -> {
                    r.shirt = Game.STAFF_COL[0]; r.hat = HatS.CHEF; r.accent = WHITE; r.acc = Acc.APRON
                }
                Role.JHOL -> {
                    r.shirt = Game.STAFF_COL[1]; r.hat = HatS.BANDANA; r.hatCol = RED; r.accent = Col.rgb(0x8E1B1B); r.acc = Acc.APRON
                }
                Role.DIDI -> {
                    r.shirt = Game.STAFF_COL[2]; r.hairStyle = HairS.BRAID; r.accent = Col.rgb(0xFF8FC8); r.acc = Acc.KURTA or Acc.SCARF
                }
                else -> {
                    r.shirt = Col.rgb(0x9CC4F0); r.hat = HatS.TOPI; r.hatCol = TOPI2; r.accent = Col.rgb(0xD6382F); r.acc = Acc.GLASSES or Acc.TIE
                }
            }
            val working = s.state != 0 && !s.moving
            r.heading = s.heading
            r.phase = s.walk
            r.mv = if (s.moving) 1f else 0f
            r.seated = false
            r.arm = if (s.n > 0) Pose.CARRY else if (working) Pose.WORK else Pose.WALK
            r.spin = 0f
            r.mood = 1
            r.seed = k * 2.1f + 5f
            r.hop = 0f
            r.lookX = 0f
            r.person(s.x, 0f, s.z)
            if (s.n > 0) {
                var y = 1.0f
                for (n in 0 until s.n) {
                    L.loadId()
                    L.translate(s.x + sin(s.heading) * 0.6f, y, s.z + cos(s.heading) * 0.6f)
                    L.rotY(s.heading)
                    drawItem(s.item, true)
                    y += itemStep(s.item)
                }
            }
        }
    }

    private fun drawPlayer() {
        val h = g.heading
        shadow(g.px, g.pz, 1.2f)
        val pulse = 1f + 0.06f * sin(g.ringPulse)
        val cmb = g.combo
        var cr = 0.25f
        var cg = 1f
        var cb = 0.4f
        if (cmb >= 20) {
            cr = 0.65f + 0.35f * sin(t * 5f); cg = 0.65f + 0.35f * sin(t * 5f + 2.1f); cb = 0.65f + 0.35f * sin(t * 5f + 4.2f)
        } else if (cmb >= 15) {
            cr = 1f; cg = 0.3f; cb = 0.45f
        } else if (cmb >= 10) {
            cr = 1f; cg = 0.6f; cb = 0.15f
        } else if (cmb >= 5) {
            cr = 1f; cg = 0.92f; cb = 0.25f
        }
        val ringSz = 1.35f * pulse * (1f + 0.12f * g.dashK)
        SG.quad(g.px, 0.05f, g.pz, gRX, 0f, gRZ, gUX, 0f, gUZ, ringSz, ringSz, Atlas.RING, Col.tint(cr, cg, cb, 1f))
        SG.quad(g.px, 0.048f, g.pz, gRX, 0f, gRZ, gUX, 0f, gUZ, 1.15f, 1.15f, Atlas.SOFT, Col.tint(cr * 0.8f, cg * 0.9f, cb * 0.9f, 0.35f))
        if (cmb >= 5) {
            val p2 = 1.95f + 0.25f * sin(t * 6f)
            SG.quad(g.px, 0.046f, g.pz, gRX, 0f, gRZ, gUX, 0f, gUZ, p2, p2, Atlas.RING, Col.tint(cr, cg, cb, 0.4f))
            for (k in 0 until 3) {
                val a = t * 3f + k * 2.094f
                SB.quad(g.px + cos(a) * 1.55f, 0.55f + 0.2f * sin(t * 4f + k), g.pz + sin(a) * 1.55f, rX, rY, rZ, uX, uY, uZ, 0.2f, 0.2f, Atlas.STAR, Col.tint(cr, cg, cb, 0.9f))
            }
        }
        if (g.dashK > 0.15f) {
            val sx = sin(h)
            val sz = cos(h)
            for (k in -1..1) {
                val back = 1.1f + 0.35f * abs(k)
                SG.quad(g.px - sx * back + sz * k * 0.42f, 0.2f, g.pz - sz * back - sx * k * 0.42f, sx, 0f, sz, sz, 0f, -sx, 0.9f * g.dashK, 0.1f, Atlas.SOFT, Col.tint(1f, 1f, 1f, 0.4f * g.dashK))
            }
        }
        val r = rig
        r.skin = Col.rgb(0xE8B48A)
        r.hair = Col.rgb(0x1E1A18)
        r.shirt = WHITE
        r.pants = Col.rgb(0x2C3E50)
        r.shoe = Col.rgb(0xEDEDED)
        r.accent = RED
        r.hatCol = TOPI2
        r.hairStyle = HairS.SHORT
        r.hat = HatS.TOPI
        r.acc = Acc.APRON
        r.heading = h
        r.phase = g.walkPhase
        r.mv = g.moveAmt
        r.seated = false
        r.arm = if (g.carryN > 0) Pose.CARRY else Pose.WALK
        r.spin = 0f
        r.mood = if (g.chainT > 0f) 1 else 0
        r.seed = 0.5f
        r.hop = if (g.pickPulse > 0f) g.pickPulse * 0.12f else 0f
        r.lookX = 0f
        r.sq = (if (g.pickPulse > 0f) sin(g.pickPulse * 3.1416f) * 0.07f else 0f) - 0.08f * g.dashK
        r.lean2 = 0.35f * g.dashK
        r.person(g.px, 0f, g.pz)
        if (g.carryN > 0) {
            val n = g.carryN
            var total = 0f
            for (k in 0 until n) total += itemStep(g.carry[k])
            val ks = if (total > 3.2f) 3.2f / total else 1f
            val bx = g.px + sin(h) * 0.66f
            val bz = g.pz + cos(h) * 0.66f
            var y = 1.0f
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

    // ------------------------------------------------------------------ guidance arrow
    private fun nearestNeeding(first: Int, count: Int, bench: Boolean): Int {
        var best = -1
        var bd = 1e9f
        for (i in first until first + count) {
            val s = g.st[i]
            if (!s.unlocked) continue
            var occ = 0
            if (s.occ0 >= 0) occ++
            if (s.occ1 >= 0) occ++
            if (occ > s.stockA) {
                val dx = s.x - g.px
                val dz = s.z - g.pz
                val d = dx * dx + dz * dz
                if (d < bd) { bd = d; best = i }
            }
        }
        return best
    }

    private fun guideTarget(): Int {
        val b = g.st[Layout.ID_BUILDING]
        if (b.avail && !b.unlocked && g.money >= b.cost - b.paid) return Layout.ID_BUILDING
        if (g.carryN > 0) {
            return when (g.carry[g.carryN - 1]) {
                Item.PLATE -> nearestNeeding(Layout.ID_TABLE0, 6, false)
                Item.CUP -> nearestNeeding(Layout.ID_BENCH0, 4, true)
                Item.MAIDA, Item.MEAT -> Layout.ID_PREP
                Item.JHOL -> Layout.ID_CAULDRON
                else -> -1
            }
        }
        val cd = g.st[Layout.ID_CAULDRON]
        if (g.jholAlert || cd.level < Game.JHOL_DRAW * 2f) return Layout.ID_JHOL
        var best = -1
        var bb = 5
        for (i in 0 until Game.NST) {
            val s = g.st[i]
            if (s.unlocked && s.bills >= bb) { bb = s.bills; best = i }
        }
        if (best >= 0) return best
        if (g.st[Layout.ID_MOKTU].stockA > 0) return Layout.ID_MOKTU
        val prep = g.st[Layout.ID_PREP]
        if (prep.stockA <= prep.stockB && prep.stockA < Game.PREP_CAP) return Layout.ID_MAIDA
        if (prep.stockB < Game.PREP_CAP) return Layout.ID_MEAT
        var cheap = -1
        var cc = 1e18
        for (i in 0 until Game.NST) {
            val s = g.st[i]
            if (s.avail && !s.unlocked && g.money >= s.cost - s.paid && s.cost < cc) { cc = s.cost; cheap = i }
        }
        return cheap
    }

    private fun drawGuide() {
        val id = guideTarget()
        if (id < 0) return
        val s = g.st[id]
        val big = s.kind == SK.BUILDING
        val y = (if (big) 2.6f else 4.7f) + 0.25f * sin(t * 5f)
        bIcon(SO, Atlas.I_ARROW, s.x, y, s.z, 0.9f, Col.tint(1f, 0.9f, 0.3f, 1f))
    }

    // ------------------------------------------------------------------ speech bubbles (priority + de-cluttered)
    private val cMark = BooleanArray(Game.MAXC)
    private val pX = FloatArray(10)
    private val pZ = FloatArray(10)
    private val pL = IntArray(10)

    private fun bubbleScore(c: Cust): Float {
        var s = when (c.bubble) {
            Bub.JHOL -> 100f + c.jholAnger * 60f
            Bub.ANGRY -> 95f
            Bub.VLOG, Bub.BIKE, Bub.TOUR -> 70f
            Bub.MOMO, Bub.CHIYA -> 40f + (if (c.state == Game.CS_WAIT) (1f - c.patience / c.patMax) * 40f else 0f)
            Bub.HAPPY, Bub.SURYA -> 35f
            Bub.THANKS, Bub.NAMASTE -> 15f
            else -> 20f
        }
        val dx = c.x - g.px
        val dz = c.z - g.pz
        s -= sqrt(dx * dx + dz * dz) * 1.2f
        return s
    }

    private fun drawBubbles() {
        for (i in 0 until Game.MAXC) cMark[i] = false
        var placed = 0
        var tries = 0
        while (placed < 7 && tries < 16) {
            tries++
            var best = -1
            var bs = -1e9f
            for (i in 0 until Game.MAXC) {
                val c = g.cs[i]
                if (!c.active || c.bubble < 0 || cMark[i] || !vis(c.x, c.z)) continue
                val s = bubbleScore(c)
                if (s > bs) { bs = s; best = i }
            }
            if (best < 0) break
            cMark[best] = true
            val c = g.cs[best]
            var lvl = 0
            for (k in 0 until placed) {
                val dx = pX[k] - c.x
                val dz = pZ[k] - c.z
                if (dx * dx + dz * dz < 2.25f && pL[k] >= lvl) lvl = pL[k] + 1
            }
            if (lvl >= 2) continue
            pX[placed] = c.x; pZ[placed] = c.z; pL[placed] = lvl
            placed++
            var sc = 0.95f
            var a = 1f
            if (c.bubble == Bub.JHOL) sc = 1.0f + 0.1f * sin(g.alertPulse + best)
            if (c.state == Game.CS_WAIT && c.patience < c.patMax * 0.3f) {
                a = 0.7f + 0.3f * sin(t * 12f)
                sc = 1f + 0.08f * sin(t * 12f)
            }
            if (c.state == Game.CS_EAT && c.wantJhol && c.jholAnger > 0.6f) sc *= 1f + 0.06f * sin(t * 20f)
            val seated = c.state == Game.CS_WAIT || c.state == Game.CS_EAT || c.state == Game.CS_CAMP
            val base = (if (seated) 2.35f else 2.75f) + lvl * 1.0f
            val hh = 0.5f * sc
            SO.quad(c.x, base + hh, c.z, rX, rY, rZ, uX, uY, uZ, hh, hh, Atlas.B0 + c.bubble, Col.tint(1f, 1f, 1f, a))
        }
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
                Game.P_LABEL -> {
                    val id = g.pVal[i].toInt()
                    val sc = (0.55f + 0.55f * min(1f, k * 7f)) * (if (k > 0.8f) 1f - (k - 0.8f) * 2.5f else 1f)
                    val a = if (k > 0.7f) (1f - k) / 0.3f else 1f
                    val hh = 0.3f * sc
                    val c = lblCol(id)
                    val col = (c and 0x00FFFFFF) or ((a * 255f).toInt().coerceIn(0, 255) shl 24)
                    SO.quad(x, y, z, rX, rY, rZ, uX, uY, uZ, hh * Atlas.LABEL_ASPECT, hh, Atlas.L0 + id, col)
                }
                Game.P_COMBO -> {
                    val sc = (0.6f + 0.6f * min(1f, k * 6f)) * (if (k > 0.8f) 1f - (k - 0.8f) * 2.5f else 1f)
                    val a = if (k > 0.7f) (1f - k) / 0.3f else 1f
                    val hh = 0.34f * sc
                    val c = lblCol(Lbl.COMBO)
                    val col = (c and 0x00FFFFFF) or ((a * 255f).toInt().coerceIn(0, 255) shl 24)
                    SO.quad(x, y, z, rX, rY, rZ, uX, uY, uZ, hh * Atlas.LABEL_ASPECT, hh, Atlas.L0 + Lbl.COMBO, col)
                    num(SO, false, g.pVal[i], x, y - 0.75f * sc, z, 0.95f * sc, (c and 0x00FFFFFF) or ((a * 255f).toInt().coerceIn(0, 255) shl 24), false, true)
                }
                Game.P_RIPPLE -> {
                    val e = 1f - (1f - k) * (1f - k)
                    val sz = g.pSize[i] * (0.15f + 0.85f * e)
                    val col = (g.pCol[i] and 0x00FFFFFF) or (((1f - k) * 255f).toInt().coerceIn(0, 255) shl 24)
                    SG.quad(x, y, z, gRX, 0f, gRZ, gUX, 0f, gUZ, sz, sz, Atlas.RING, col)
                }
                Game.P_CONF -> {
                    val s = g.pSize[i] * (if (k > 0.75f) 1f - (k - 0.75f) * 4f else 1f)
                    L.loadId()
                    L.translate(x, y, z)
                    L.rotY(g.pAge[i] * 9f + i)
                    L.rotX(g.pAge[i] * 12f)
                    L.box(0f, 0f, 0f, s, s * 0.14f, s * 0.7f, (g.pCol[i] and 0x00FFFFFF) or (150 shl 24))
                    L.loadId()
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
