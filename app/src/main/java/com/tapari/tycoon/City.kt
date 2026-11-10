package com.tapari.tycoon

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

object Theme {
    val CLEAR = arrayOf(
        floatArrayOf(0.56f, 0.79f, 0.96f), floatArrayOf(0.60f, 0.82f, 0.96f),
        floatArrayOf(0.98f, 0.70f, 0.45f), floatArrayOf(0.08f, 0.06f, 0.20f)
    )
    val SUN = arrayOf(
        floatArrayOf(0.86f, 0.78f, 0.62f), floatArrayOf(0.86f, 0.79f, 0.66f),
        floatArrayOf(0.90f, 0.62f, 0.40f), floatArrayOf(0.50f, 0.52f, 0.90f)
    )
    val SKYAMB = arrayOf(
        floatArrayOf(0.56f, 0.62f, 0.74f), floatArrayOf(0.58f, 0.63f, 0.74f),
        floatArrayOf(0.68f, 0.54f, 0.58f), floatArrayOf(0.42f, 0.40f, 0.70f)
    )
    val GNDAMB = arrayOf(
        floatArrayOf(0.44f, 0.40f, 0.36f), floatArrayOf(0.46f, 0.42f, 0.38f),
        floatArrayOf(0.48f, 0.36f, 0.32f), floatArrayOf(0.30f, 0.24f, 0.40f)
    )
    val FLOOR_A = intArrayOf(Col.rgb(0xE2DACB), Col.rgb(0xF0E8D6), Col.rgb(0xB8814F), Col.rgb(0x434A78))
    val FLOOR_B = intArrayOf(Col.rgb(0xCFC6B4), Col.rgb(0xD6C8AC), Col.rgb(0xA26E41), Col.rgb(0x353B66))
    val PAVE_A = Col.rgb(0xBDB8AE)
    val PAVE_B = Col.rgb(0xAEA9A0)
    val WALL = intArrayOf(Col.rgb(0xD9855A), Col.rgb(0xEBDDBC), Col.rgb(0x7B8FA3), Col.rgb(0x3A2F6E))
    val ACCENT = intArrayOf(Col.rgb(0xF2B134), Col.rgb(0xCC3B2E), Col.rgb(0xF6E3A1), Col.rgb(0xFFC247))
    val GROUND = intArrayOf(Col.rgb(0x7A7467), Col.rgb(0x77716A), Col.rgb(0x4A4F5C), Col.rgb(0x15122E))
    val BCOL = arrayOf(
        intArrayOf(Col.rgb(0xC9704A), Col.rgb(0xE0B05A), Col.rgb(0xB85C45), Col.rgb(0xD9C9A3), Col.rgb(0x8FA8B8), Col.rgb(0xC98F7A), Col.rgb(0xE8D2A0), Col.rgb(0xA7B38A)),
        intArrayOf(Col.rgb(0xB9B1A4), Col.rgb(0xC4856B), Col.rgb(0xD7C9A8), Col.rgb(0x9AA7B4), Col.rgb(0xCFA46E), Col.rgb(0x8F8A86), Col.rgb(0xBF7B66), Col.rgb(0xA9B7A0)),
        intArrayOf(Col.rgb(0x6C7A99), Col.rgb(0x7F6B8C), Col.rgb(0x5E6F8A), Col.rgb(0x8A7A9A), Col.rgb(0x6B8299), Col.rgb(0x7C6F8F), Col.rgb(0x58637F), Col.rgb(0x9A8AA6)),
        intArrayOf(Col.rgb(0x1E1B45), Col.rgb(0x241F57), Col.rgb(0x2B2563), Col.rgb(0x181440), Col.rgb(0x30286E), Col.rgb(0x211C4E), Col.rgb(0x2A2A6A), Col.rgb(0x151235))
    )
}

/** Everything that is not a station or a person: floor, building shell, street, skyline, traffic. */
class City(private val g: Game) {
    private lateinit var L: LitBatch
    private lateinit var SB: SpriteBatch
    private lateinit var rig: Rig
    private var t = 0f

    private val WHITE = Col.rgb(0xFFFFFF)
    private val DARK = Col.rgb(0x2A2D34)
    private val TIRE = Col.rgb(0x1E1F24)
    private val GLASS = Col.rgb(0x6FA6C8)
    private val WOOD = Col.rgb(0x9B6B43)
    private val WOOD_D = Col.rgb(0x6D4A2D)
    private val STEEL = Col.rgb(0xA7AEB5)
    private val STEEL_D = Col.rgb(0x8A9199)
    private val RED = Col.rgb(0xD6382F)
    private val CONE = Col.rgb(0xF28C28)

    private fun rnd(i: Int, k: Int): Float {
        val x = sin(i * 12.9898f + k * 78.233f) * 43758.547f
        return x - floor(x)
    }

    private fun ease(e: Float): Float = e * e * (3f - 2f * e)

    fun draw(lit: LitBatch, sb: SpriteBatch, r: Rig, time: Float) {
        L = lit; SB = sb; rig = r; t = time
        val loc = g.loc
        val e = ease(g.buildT.coerceIn(0f, 1f))
        L.loadId()
        L.plane(1.5f, -0.05f, 0f, 400f, 400f, Theme.GROUND[loc])
        drawFloor(loc, e)
        if (loc == 2) drawRoofRail() else drawStreet(loc)
        drawSkyline(loc)
        drawBuilding(loc, e)
        drawNeighbors(loc)
        if (loc == 0 && e < 0.35f) drawThelaProps(e)
        drawFlags(loc, e)
    }

    // ---------------------------------------------------------------- floor
    private fun drawFloor(loc: Int, e: Float) {
        val owned = e > 0.3f
        for (ix in 0 until 13) {
            for (iz in 0 until 12) {
                val x = -11f + 2f * ix
                val z = -9f + 2f * iz
                val chk = ((ix + iz) and 1) == 0
                val col: Int
                if (owned) {
                    col = if (chk) Theme.FLOOR_A[loc] else Theme.FLOOR_B[loc]
                } else if (x > 5.2f && z > 2.5f) {
                    col = if (chk) Col.rgb(0xA79A82) else Col.rgb(0x9A8E77)
                } else {
                    col = if (chk) Theme.PAVE_A else Theme.PAVE_B
                }
                L.plane(x, 0f, z, 2f, 2f, col)
            }
        }
        // grout / border lines at the building edge
        if (owned) {
            L.box(1.5f, 0.02f, 14.1f, 28f, 0.05f, 0.3f, Theme.ACCENT[loc])
        }
    }

    // ---------------------------------------------------------------- building shell
    private fun drawBuilding(loc: Int, e: Float) {
        val wall = Theme.WALL[loc]
        val acc = Theme.ACCENT[loc]
        val roof = loc == 2
        val bh = if (roof) 1.5f else 5.6f
        val sideH = if (roof) 1.3f else 4.2f
        // the old closed-up facade behind everything
        L.box(1.5f, bh / 2f, -11.5f, 32f, bh, 0.6f, Col.rgb(0xB4AEA2))
        if (e > 0.01f) {
            val h = bh * e
            L.box(1.5f, h / 2f, -10.9f, 29f, h, 0.5f, wall)
            val th = min(h, if (roof) 0.9f else 1.7f)
            L.box(1.5f, th / 2f, -10.6f, 29f, th, 0.06f, Col.rgb(0xF3F0E8))
            L.box(1.5f, th + 0.04f, -10.58f, 29f, 0.08f, 0.1f, acc)
            L.box(1.5f, h + 0.12f, -10.9f, 29.4f, 0.28f, 0.8f, acc)
            val sh = sideH * e
            L.box(-12.9f, sh / 2f, 1.8f, 0.6f, sh, 26f, wall)
            L.box(15.9f, sh / 2f, 1.8f, 0.6f, sh, 26f, wall)
            L.box(-12.9f, sh + 0.1f, 1.8f, 0.8f, 0.22f, 26.2f, acc)
            L.box(15.9f, sh + 0.1f, 1.8f, 0.8f, 0.22f, 26.2f, acc)
            if (e > 0.95f && !roof) {
                for (k in 0 until 5) {
                    val wx = -8f + k * 6f
                    L.box(wx, 3.0f, -10.55f, 2.4f, 1.5f, 0.08f, Col.emis(0x9AD7F5, 110))
                    L.box(wx, 3.0f, -10.5f, 2.6f, 0.1f, 0.12f, WHITE)
                    L.box(wx, 3.75f, -10.5f, 2.6f, 0.1f, 0.12f, WHITE)
                    L.box(wx, 2.25f, -10.5f, 2.6f, 0.1f, 0.12f, WHITE)
                    L.box(wx, 3.0f, -10.5f, 0.1f, 1.5f, 0.12f, WHITE)
                }
                // tube lights
                for (k in 0 until 6) L.box(-9f + k * 5.5f, 5.2f, -10.4f, 2.2f, 0.12f, 0.12f, Col.emis(0xFFF6D8, 20))
                // menu board
                L.box(11.2f, 2.9f, -10.5f, 3.2f, 1.9f, 0.1f, Col.rgb(0x20262E))
                L.box(11.2f, 2.9f, -10.44f, 3.0f, 1.7f, 0.04f, Col.rgb(0x2F3A46))
                SB.quad(10.4f, 3.2f, -10.36f, 1f, 0f, 0f, 0f, 1f, 0f, 0.42f, 0.42f, Atlas.I_PLATE, Col.tint(1f, 1f, 1f, 1f))
                SB.quad(11.2f, 3.2f, -10.36f, 1f, 0f, 0f, 0f, 1f, 0f, 0.42f, 0.42f, Atlas.I_CUP, Col.tint(1f, 1f, 1f, 1f))
                SB.quad(12.0f, 3.2f, -10.36f, 1f, 0f, 0f, 0f, 1f, 0f, 0.42f, 0.42f, Atlas.I_JHOL, Col.tint(1f, 1f, 1f, 1f))
                for (k in 0 until 3) L.box(10.4f + k * 0.8f, 2.35f, -10.38f, 0.6f, 0.07f, 0.03f, Col.rgb(0xFFE9A8))
            }
        }
        // closed shutter that rolls up when the shop is bought
        if (e < 0.999f) {
            val sh = 4.4f * (1f - e)
            if (sh > 0.05f) {
                L.box(1.5f, 4.6f - sh / 2f, -10.7f, 29f, sh, 0.25f, STEEL)
                val n = (sh / 0.42f).toInt()
                for (k in 0 until n) L.box(1.5f, 4.45f - k * 0.42f, -10.55f, 29f, 0.05f, 0.05f, STEEL_D)
                L.box(1.5f, 4.7f, -10.55f, 29f, 0.35f, 0.4f, STEEL_D)
            }
            if (e < 0.5f) {
                // "TO LET" board hanging over the shutter
                val by = 3.1f + e * 6f
                L.box(-6f, by + 0.9f, -10.3f, 0.07f, 1.0f, 0.07f, DARK)
                L.box(-2f, by + 0.9f, -10.3f, 0.07f, 1.0f, 0.07f, DARK)
                L.box(-4f, by, -10.3f, 4.6f, 1.15f, 0.12f, Col.rgb(0xF4F0E4))
                L.box(-4f, by + 0.38f, -10.22f, 4.2f, 0.28f, 0.04f, RED)
                L.box(-4f, by - 0.18f, -10.22f, 3.0f, 0.22f, 0.04f, Col.rgb(0x39424C))
            }
        }
        // shop signboard that drops in once built
        if (e > 0.4f && !roof) {
            val drop = if (e < 1f) (1f - e) * 3f else 0f
            val sy = 5.0f + drop
            L.box(1.5f, sy, -10.15f, 10.5f, 1.25f, 0.2f, Col.rgb(0x2A1A16))
            L.box(1.5f, sy, -10.03f, 10.1f, 1.0f, 0.06f, acc)
            L.box(1.5f, sy, -9.99f, 9.2f, 0.7f, 0.04f, Col.rgb(0x9E1B1B))
            SB.quad(-2.4f, sy, -9.9f, 1f, 0f, 0f, 0f, 1f, 0f, 0.55f, 0.55f, Atlas.I_MOKTU, Col.tint(1f, 1f, 1f, 1f))
            SB.quad(5.4f, sy, -9.9f, 1f, 0f, 0f, 0f, 1f, 0f, 0.55f, 0.55f, Atlas.I_CUP, Col.tint(1f, 1f, 1f, 1f))
            for (k in 0 until 7) L.box(-1.2f + k * 0.4f + 0.2f, sy, -9.92f, 0.22f, 0.3f, 0.03f, Col.rgb(0xFFE9A8))
        }
        if (roof && e > 0.9f) {
            for (k in 0 until 5) {
                val x = -10f + k * 6f
                L.cyl(x, 0.5f, -9.6f, 0.55f, 1.0f, Col.rgb(0x5A4636))
                L.sph(x, 1.35f, -9.6f, 0.55f, 0.5f, 0.55f, Col.rgb(0x3F9B4F))
                L.sph(x + 0.2f, 1.7f, -9.5f, 0.35f, 0.35f, 0.35f, Col.rgb(0x58B368))
            }
        }
    }

    private fun drawFlags(loc: Int, e: Float) {
        if (loc == 3) return
        val roof = loc == 2
        val y0 = if (roof) 3.3f else 6.2f
        val cols = intArrayOf(Col.rgb(0x2E6FD8), WHITE, Col.rgb(0xD6382F), Col.rgb(0x3FAF55), Col.rgb(0xF2C434))
        val z = if (roof) -9.7f else -11.0f
        for (k in 0 until 34) {
            val x = -13.5f + k * 0.9f
            val sag = 0.5f * sin((k / 33f) * 3.1416f)
            val fl = sin(t * 3f + k * 0.8f) * 0.06f
            L.box(x, y0 - sag + fl, z, 0.42f, 0.5f, 0.03f, cols[k % 5])
        }
        L.box(1.5f, y0 + 0.25f, z, 31f, 0.03f, 0.03f, Col.rgb(0x6B5B4B))
    }

    private fun drawRoofRail() {
        // glass railing + fairy lights along the rooftop edge, skyline is below
        for (k in 0 until 24) {
            val x = -12f + k * 1.2f
            L.box(x, 0.55f, 14.0f, 0.1f, 1.1f, 0.1f, STEEL)
            val on = if (((t * 3f).toInt() + k) % 5 == 0) 0.55f else 1f
            L.sphLo(x + 0.6f, 3.1f - 0.25f * sin(k * 1.3f), 14.0f, 0.12f, 0.12f, 0.12f, Col.emis(0xFFD27A, (40 * on).toInt()))
        }
        L.box(1.5f, 1.1f, 14.0f, 28.6f, 0.08f, 0.14f, STEEL)
        L.box(1.5f, 0.55f, 14.0f, 28.4f, 0.9f, 0.03f, Col.rgb(0xBFE3F2))
        for (k in 0 until 4) L.box(-12f + k * 8.5f, 1.6f, 14.0f, 0.1f, 3.2f, 0.1f, STEEL_D)
        L.box(1.5f, 3.2f, 14.0f, 30f, 0.04f, 0.04f, Col.rgb(0x6B5B4B))
    }

    // ---------------------------------------------------------------- street
    private fun drawStreet(loc: Int) {
        val night = loc == 3
        // sidewalk, kerb, road
        L.plane(1.5f, 0f, 15.8f, 140f, 3.6f, if (night) Col.rgb(0x35335A) else Col.rgb(0xC4C0B6))
        for (k in 0 until 28) {
            if (k % 2 == 0) L.box(-30f + k * 2.8f, 0.01f, 15.8f, 1.4f, 0.03f, 3.6f, if (night) Col.rgb(0x3C3A66) else Col.rgb(0xB5B1A7))
        }
        L.box(1.5f, 0.015f, 14.55f, 140f, 0.03f, 0.35f, Col.rgb(0xE8C23A))
        L.box(1.5f, 0.09f, 17.7f, 140f, 0.18f, 0.3f, if (night) Col.rgb(0x57548A) else Col.rgb(0x9A978F))
        L.plane(1.5f, 0f, 22.2f, 140f, 8.6f, if (night) Col.rgb(0x1A1830) else Col.rgb(0x4A4D52))
        for (k in 0 until 24) L.box(-32f + k * 3f, 0.02f, 22.2f, 1.4f, 0.03f, 0.18f, Col.rgb(0xEDE6C8))
        L.box(1.5f, 0.02f, 18.2f, 140f, 0.03f, 0.1f, WHITE)
        L.box(1.5f, 0.02f, 26.2f, 140f, 0.03f, 0.1f, WHITE)
        L.plane(1.5f, 0f, 29.4f, 140f, 6f, Col.rgb(0xB1AEA6))
        // zebra crossing
        for (k in 0 until 9) L.box(-3.5f + k * 0.9f, 0.025f, 22.2f, 0.5f, 0.03f, 8.0f, Col.rgb(0xF2EFE6))
        // lamps, trees, poles
        for (k in 0 until 8) {
            val x = -34f + k * 10f
            L.cyl6(x, 2.1f, 14.9f, 0.09f, 4.2f, STEEL_D)
            L.box(x + 0.35f, 4.15f, 14.9f, 0.9f, 0.08f, 0.08f, STEEL_D)
            L.box(x + 0.75f, 4.08f, 14.9f, 0.4f, 0.12f, 0.3f, if (night) Col.emis(0xFFE08A, 10) else Col.rgb(0xF2EDD6))
            if (night) SB.quad(x + 0.75f, 4.0f, 14.9f, 1f, 0f, 0f, 0f, 1f, 0f, 1.4f, 1.4f, Atlas.SOFT, Col.tint(1f, 0.85f, 0.45f, 0.5f))
        }
        for (k in 0 until 7) {
            val x = -29f + k * 10f
            if (x > -13.5f && x < 16.5f) continue
            L.cyl6(x, 0.55f, 16.6f, 0.14f, 1.1f, WOOD_D)
            L.sphLo(x, 1.7f, 16.6f, 0.95f, 0.85f, 0.95f, Col.rgb(0x3F9B4F))
            L.sphLo(x + 0.4f, 2.3f, 16.5f, 0.6f, 0.55f, 0.6f, Col.rgb(0x58B368))
        }
        for (k in 0 until 5) {
            val x = -38f + k * 20f
            L.box(x, 4.8f, 17.4f, 0.22f, 9.6f, 0.22f, WOOD_D)
            L.box(x, 8.9f, 17.4f, 2.4f, 0.1f, 0.1f, WOOD_D)
            L.box(x, 8.2f, 17.4f, 2.0f, 0.1f, 0.1f, WOOD_D)
            L.box(x + 10f, 8.82f, 17.4f, 20f, 0.03f, 0.03f, Col.rgb(0x222222))
            L.box(x + 10f, 8.22f, 17.4f, 20f, 0.03f, 0.03f, Col.rgb(0x222222))
            L.box(x + 5f, 8.05f, 17.4f, 0.5f, 0.35f, 0.1f, Col.rgb(0x333333))
        }
        // dustbin + bench on the sidewalk
        L.cyl(16.9f, 0.45f, 15.2f, 0.32f, 0.9f, Col.rgb(0x3D8B4D))
        L.cyl(16.9f, 0.92f, 15.2f, 0.34f, 0.08f, Col.rgb(0x2C6A3A))
        drawTraffic(night)
        drawPeds()
    }

    private val VZ = floatArrayOf(19.6f, 19.6f, 19.6f, 24.6f, 24.6f, 24.6f, 20.2f, 24.0f)
    private val VD = floatArrayOf(1f, 1f, 1f, -1f, -1f, -1f, 1f, -1f)
    private val VS = floatArrayOf(5.5f, 7.5f, 4.2f, 6.0f, 4.8f, 8.0f, 9.5f, 7.0f)
    private val VO = floatArrayOf(0f, 31f, 62f, 10f, 45f, 70f, 20f, 88f)
    private val VT = intArrayOf(0, 1, 3, 0, 1, 2, 3, 3)
    private val VC = intArrayOf(
        Col.rgb(0xE0A82E), Col.rgb(0xF2F2F2), Col.rgb(0xC0392B), Col.rgb(0x2E86C1),
        Col.rgb(0xF4D03F), Col.rgb(0x3FAF55), Col.rgb(0x222222), Col.rgb(0xD6382F)
    )

    private fun drawTraffic(night: Boolean) {
        for (i in 0 until VZ.size) {
            val raw = t * VS[i] * VD[i] + VO[i]
            val m = raw + 60f
            val x = m - floor(m / 120f) * 120f - 60f
            if (abs(x - g.px) > 22f) continue
            L.loadId()
            L.translate(x, 0f, VZ[i])
            if (VD[i] < 0f) L.rotY(3.1416f)
            val col = VC[i]
            val head = if (night) Col.emis(0xFFF2B0, 20) else Col.rgb(0xFFF2B0)
            L.box(0f, 0.04f, 0f, 0.1f, 0.01f, 0.1f, col)
            when (VT[i]) {
                0 -> {
                    L.box(0f, 1.3f, 0f, 6.6f, 2.0f, 2.3f, col)
                    L.box(0f, 1.6f, 0f, 6.7f, 0.75f, 2.32f, Col.rgb(0x3A5568))
                    L.box(0f, 0.65f, 0f, 6.65f, 0.3f, 2.32f, Col.rgb(0xD6382F))
                    L.box(0f, 2.35f, 0f, 6.4f, 0.12f, 2.1f, Col.rgb(0xDADFE3))
                    L.box(3.32f, 1.55f, 0f, 0.06f, 0.8f, 2.0f, Col.rgb(0x7FB6D6))
                    for (w in 0 until 2) for (s in 0 until 2) L.box(-2.0f + w * 4.0f, 0.45f, -1.15f + s * 2.3f, 0.95f, 0.9f, 0.3f, TIRE)
                    L.box(3.33f, 0.7f, -0.8f, 0.08f, 0.22f, 0.4f, head)
                    L.box(3.33f, 0.7f, 0.8f, 0.08f, 0.22f, 0.4f, head)
                }
                1 -> {
                    L.box(0f, 0.7f, 0f, 3.5f, 0.8f, 1.65f, col)
                    L.box(-0.2f, 1.35f, 0f, 1.9f, 0.62f, 1.5f, shade(col, 0.9f))
                    L.box(-0.2f, 1.36f, 0f, 1.95f, 0.42f, 1.52f, GLASS)
                    for (w in 0 until 2) for (s in 0 until 2) L.box(-1.1f + w * 2.2f, 0.34f, -0.82f + s * 1.64f, 0.7f, 0.7f, 0.22f, TIRE)
                    L.box(1.76f, 0.7f, -0.55f, 0.06f, 0.2f, 0.3f, head)
                    L.box(1.76f, 0.7f, 0.55f, 0.06f, 0.2f, 0.3f, head)
                    if (i == 1 || i == 4) L.box(-0.2f, 1.8f, 0f, 0.5f, 0.18f, 0.25f, Col.rgb(0xFFE066))
                }
                2 -> {
                    L.box(0f, 0.6f, 0f, 1.5f, 0.4f, 0.4f, col)
                    L.box(-0.7f, 0.35f, 0f, 0.7f, 0.7f, 0.18f, TIRE)
                    L.box(0.7f, 0.35f, 0f, 0.7f, 0.7f, 0.18f, TIRE)
                    L.box(-0.1f, 1.15f, 0f, 0.4f, 0.7f, 0.4f, Col.rgb(0x39424C))
                    L.sph(-0.05f, 1.7f, 0f, 0.25f, 0.25f, 0.25f, Col.rgb(0xE8B48A))
                    L.sph(-0.05f, 1.78f, 0f, 0.28f, 0.2f, 0.28f, Col.rgb(0x1C1C20))
                    L.box(0.8f, 0.95f, 0f, 0.1f, 0.1f, 0.7f, STEEL_D)
                }
                else -> {
                    L.box(2.0f, 1.2f, 0f, 1.6f, 1.6f, 2.2f, col)
                    L.box(2.5f, 1.45f, 0f, 0.7f, 0.7f, 2.0f, GLASS)
                    L.box(-1.0f, 1.55f, 0f, 4.8f, 1.8f, 2.3f, Col.rgb(0xD94A3A))
                    for (k in 0 until 4) L.box(-1.0f, 0.9f + k * 0.5f, 0f, 4.85f, 0.18f, 2.32f, if (k % 2 == 0) Col.rgb(0xF2C434) else Col.rgb(0x2E8B57))
                    for (w in 0 until 3) for (s in 0 until 2) L.box(-2.6f + w * 2.2f, 0.45f, -1.15f + s * 2.3f, 0.95f, 0.9f, 0.3f, TIRE)
                    L.box(2.82f, 0.65f, -0.7f, 0.08f, 0.22f, 0.4f, head)
                    L.box(2.82f, 0.65f, 0.7f, 0.08f, 0.22f, 0.4f, head)
                }
            }
        }
    }

    private val PS = floatArrayOf(1.1f, -0.9f, 1.4f, -1.2f, 0.8f)
    private val PO = floatArrayOf(0f, 18f, 40f, 62f, 9f)
    private val PZ = floatArrayOf(16.3f, 16.9f, 15.0f, 16.5f, 15.6f)

    private fun drawPeds() {
        for (i in 0 until 5) {
            val raw = t * PS[i] + PO[i]
            val m = raw + 45f
            val x = m - floor(m / 90f) * 90f - 45f
            if (abs(x - g.px) > 16f) continue
            val dir = if (PS[i] > 0f) 1f else -1f
            val ph = t * 5.2f + i * 1.7f
            rig.skin = Game.SKINS[i % 4]
            rig.hair = Game.HAIRS[i % 4]
            rig.shirt = Game.SHIRTS[(i * 3 + 1) % 8]
            rig.pants = Game.PANTS[i % 4]
            rig.accent = Game.SHIRTS[(i * 5 + 2) % 8]
            rig.hatCol = if (i % 2 == 0) Col.rgb(0xD9362B) else Col.rgb(0x1F1F28)
            rig.hairStyle = if (i % 3 == 0) HairS.BRAID else HairS.SHORT
            rig.hat = if (i % 3 == 1) HatS.TOPI else HatS.NONE
            rig.acc = if (i % 3 == 0) (Acc.KURTA or Acc.SCARF) else if (i == 4) Acc.PACK else 0
            rig.heading = if (dir > 0f) 1.5708f else -1.5708f
            rig.phase = ph
            rig.mv = 1f
            rig.seated = false
            rig.arm = Pose.WALK
            rig.spin = 0f
            rig.mood = 0
            rig.seed = i * 2.3f
            rig.hop = 0f
            rig.lookX = 0f
            rig.person(x, 0f, PZ[i])
        }
    }

    // ---------------------------------------------------------------- skyline
    private fun drawSkyline(loc: Int) {
        val pal = Theme.BCOL[loc]
        val night = loc == 3
        val dusk = loc == 2
        val low = loc == 2
        for (row in 0 until 2) {
            for (k in 0 until 14) {
                val x = -44f + k * 7.2f + row * 3.3f + rnd(k, row) * 2f
                val z = -17f - row * 11f - rnd(k, 3) * 2f
                val w = 4.8f + rnd(k, 1) * 3f
                var h = (if (row == 0) 6.5f else 11f) + rnd(k, 2) * (if (row == 0) 6f else 12f)
                if (night) h *= 1.5f
                val col = pal[(k * 3 + row) % 8]
                val bot = if (low) -0.5f else 0f
                L.box(x, (h + bot) / 2f, z, w, h - bot, w * 0.9f, col)
                L.box(x, h + 0.15f, z, w + 0.3f, 0.3f, w * 0.9f + 0.3f, shade(col, 0.8f))
                val wn = 3 + (k % 2)
                val rows = (h / 2.9f).toInt().coerceIn(1, 4)
                for (r in 0 until rows) {
                    for (c in 0 until wn) {
                        val lit = rnd(k * 7 + r, c + row * 5) > 0.42f
                        val wc = if (night) (if (lit) Col.emis(0xFFE08A, 90) else Col.emis(0x28285A, 130))
                        else if (dusk) (if (lit) Col.emis(0xFFD27A, 90) else Col.emis(0x6A7A9A, 130))
                        else Col.emis(0x8FC4E8, 130)
                        val wx = x - w * 0.33f + c * (w * 0.66f / (wn - 1))
                        L.box(wx, 1.7f + r * 2.5f, z + w * 0.45f + 0.03f, 0.7f, 0.95f, 0.05f, wc)
                    }
                }
                if (k % 3 == 0) L.cyl(x + w * 0.2f, h + 0.9f, z, 0.55f, 1.2f, Col.rgb(0x2F3338))
                if (k % 4 == 1) L.box(x - w * 0.2f, h + 1.6f, z, 0.08f, 2.6f, 0.08f, STEEL_D)
                if (night && k % 2 == 0) L.box(x, h * 0.6f, z + w * 0.46f + 0.05f, w * 0.7f, 0.18f, 0.05f, if (k % 4 == 0) Col.emis(0xFF3DAA, 20) else Col.emis(0x3DE8FF, 20))
            }
        }
        if (loc == 2) {
            // below-the-roof city blocks all around the front
            for (k in 0 until 16) {
                val x = -46f + k * 6.4f + rnd(k, 8) * 2f
                val z = 22f + rnd(k, 9) * 14f
                val h = 5f + rnd(k, 5) * 7f
                L.box(x, -10f + (h - 2f) / 2f, z, 5f, h + 8f, 5f, pal[k % 8])
                for (r in 0 until 3) for (c in 0 until 3) {
                    if (rnd(k * 3 + r, c) > 0.4f) L.box(x - 1.4f + c * 1.4f, -2.6f + r * -1.8f + h - 5f, z - 2.55f, 0.6f, 0.8f, 0.05f, Col.emis(0xFFD27A, 90))
                }
            }
        }
        // Himalayas
        if (loc != 3) {
            for (k in 0 until 7) {
                val x = -78f + k * 26f + rnd(k, 11) * 6f
                val h = 34f + rnd(k, 12) * 18f
                val r = 24f + rnd(k, 13) * 8f
                val base = if (dusk) Col.rgb(0x8C7A9A) else Col.rgb(0x8FA3B8)
                L.cone(x, h / 2f - 2f, -88f, r, h, base)
                val sh = h * 0.38f
                L.cone(x, -2f + h * 0.62f + sh / 2f, -88f, r * 0.38f, sh, Col.rgb(0xF4F8FC))
            }
        }
        // Boudha-style stupa landmark
        val sx = -24f
        val sz = -27f
        val sc = if (night) shade(Col.rgb(0xF0EBDD), 0.55f) else Col.rgb(0xF4F0E4)
        L.cyl(sx, 0.7f, sz, 6.6f, 1.4f, shade(sc, 0.9f))
        L.cyl(sx, 1.7f, sz, 5.6f, 0.8f, sc)
        L.cyl(sx, 2.4f, sz, 4.6f, 0.8f, shade(sc, 0.95f))
        L.sph(sx, 3.2f, sz, 4.2f, 3.6f, 4.2f, sc)
        L.box(sx, 7.4f, sz, 1.9f, 1.7f, 1.9f, Col.rgb(0xE0B23A))
        L.box(sx - 0.45f, 7.5f, sz + 0.96f, 0.36f, 0.16f, 0.04f, Col.rgb(0x1E4FB8))
        L.box(sx + 0.45f, 7.5f, sz + 0.96f, 0.36f, 0.16f, 0.04f, Col.rgb(0x1E4FB8))
        L.box(sx, 7.25f, sz + 0.96f, 0.12f, 0.3f, 0.04f, Col.rgb(0xC0392B))
        L.cone(sx, 9.6f, sz, 1.0f, 2.8f, Col.rgb(0xE0B23A))
        L.sph(sx, 11.2f, sz, 0.3f, 0.3f, 0.3f, Col.rgb(0xFFD54A))
        // pagoda temple
        val px = 24f
        val pz = -28f
        L.box(px, 1.0f, pz, 6.5f, 2.0f, 6.5f, Col.rgb(0xC9704A))
        for (k in 0 until 3) {
            val y = 3.0f + k * 2.6f
            val s = 1.0f - k * 0.18f
            L.box(px, y - 0.9f, pz, 4.6f * s, 1.6f, 4.6f * s, Col.rgb(0xE8D2A0))
            L.shape(Shapes.CONE5, px, y + 0.5f, pz, 6.4f * s, 1.4f, 6.4f * s, Col.rgb(0x9A3B2E))
        }
        L.cone(px, 11.6f, pz, 0.45f, 2.2f, Col.rgb(0xE0B23A))
    }

    // ---------------------------------------------------------------- neighbours
    private fun drawNeighbors(loc: Int) {
        val pal = Theme.BCOL[loc]
        val night = loc == 3
        for (side in 0 until 2) {
            for (k in 0 until 3) {
                val w = 8f + rnd(k, side + 20) * 3f
                val xc = if (side == 0) -13.4f - w / 2f - k * (w + 0.3f) else 16.4f + w / 2f + k * (w + 0.3f)
                val h = 7f + rnd(k, side + 30) * 5f
                val col = pal[(k * 2 + side * 3 + 1) % 8]
                val depth = 24f
                val zf = 13.9f
                val bot = if (loc == 2) -0.5f else 0f
                L.box(xc, (h + bot) / 2f, zf - depth / 2f, w, h - bot, depth, col)
                L.box(xc, h + 0.15f, zf - depth / 2f, w + 0.3f, 0.3f, depth + 0.3f, shade(col, 0.8f))
                // ground-floor shopfront
                L.box(xc, 1.4f, zf + 0.06f, w - 0.8f, 2.4f, 0.12f, if (night) Col.rgb(0x2B2563) else Col.rgb(0xA7AEB5))
                for (s in 0 until 7) L.box(xc, 0.35f + s * 0.32f, zf + 0.14f, w - 0.8f, 0.04f, 0.04f, STEEL_D)
                val aw = pal[(k + side * 2 + 4) % 8]
                for (s in 0 until 6) {
                    L.push()
                    L.translate(xc - (w - 1f) / 2f + s * ((w - 1f) / 5.5f) + 0.5f, 2.9f, zf + 0.7f)
                    L.rotX(-0.45f)
                    L.box(0f, 0f, 0f, (w - 1f) / 5.6f, 0.07f, 1.5f, if (s % 2 == 0) shade(aw, 1.15f) else WHITE)
                    L.pop()
                }
                L.box(xc, 3.55f, zf + 0.12f, w - 1.5f, 0.7f, 0.1f, if (night) Col.emis(0xFF3DAA, 40) else Col.rgb(0xCC3B2E))
                L.box(xc, 3.55f, zf + 0.18f, w - 2.1f, 0.4f, 0.03f, if (night) Col.emis(0x3DE8FF, 60) else Col.rgb(0xFFE9A8))
                // upper windows + balconies
                val fl = ((h - 4.4f) / 2.8f).toInt().coerceIn(1, 3)
                for (f in 0 until fl) {
                    for (c in 0 until 3) {
                        val wx = xc - w * 0.3f + c * w * 0.3f
                        val wy = 5.2f + f * 2.8f
                        L.box(wx, wy, zf + 0.06f, 1.1f, 1.4f, 0.1f, if (night) Col.emis(0xFFE08A, 90) else Col.emis(0x8FC4E8, 130))
                        L.box(wx, wy - 0.85f, zf + 0.35f, 1.5f, 0.1f, 0.6f, shade(col, 0.7f))
                        L.box(wx, wy - 0.5f, zf + 0.62f, 1.5f, 0.06f, 0.05f, STEEL_D)
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------- stage 0 props
    private fun drawThelaProps(e: Float) {
        val rise = e * 6f
        L.loadId()
        // striped awning on the back facade
        for (k in 0 until 21) {
            L.push()
            L.translate(-12.3f + k * 1.4f, 3.7f + rise, -10.0f)
            L.rotX(-0.38f)
            L.box(0f, 0f, 0f, 1.4f, 0.08f, 2.4f, if (k % 2 == 0) RED else WHITE)
            L.pop()
        }
        // hanging bulbs
        for (k in 0 until 20) {
            val x = -11f + k * 1.3f
            val on = if (((t * 2f).toInt() + k) % 6 == 0) 0.5f else 1f
            L.sphLo(x, 3.05f - 0.14f * sin(k * 1.1f) + rise, -9.4f, 0.1f, 0.1f, 0.1f, Col.emis(0xFFE08A, (40 * on).toInt()))
        }
        L.box(1.5f, 3.2f + rise, -9.4f, 26f, 0.02f, 0.02f, Col.rgb(0x333333))
        // gas cylinder, water drum, sacks
        L.cyl(6.4f, 0.55f, -9.6f, 0.3f, 1.1f, RED)
        L.cyl(6.4f, 1.15f, -9.6f, 0.12f, 0.15f, STEEL_D)
        L.cyl(-11.2f, 0.6f, -9.5f, 0.5f, 1.2f, Col.rgb(0x2E6FB7))
        L.cyl(-11.2f, 1.22f, -9.5f, 0.52f, 0.06f, Col.rgb(0x1F4F8A))
        for (k in 0 until 3) L.box(11.9f + (k % 2) * 0.9f, 0.3f + (k / 2) * 0.6f, -9.7f, 0.85f, 0.55f, 0.6f, Col.rgb(0xD8C9A0))
        // FOR SALE board and cones on the empty lot
        L.box(7.4f, 1.1f, 3.3f, 0.12f, 2.2f, 0.12f, WOOD_D)
        L.box(12.2f, 1.1f, 3.3f, 0.12f, 2.2f, 0.12f, WOOD_D)
        L.box(9.8f, 1.9f, 3.3f, 5.4f, 1.5f, 0.14f, Col.rgb(0xF4F0E4))
        L.box(9.8f, 2.35f, 3.38f, 5.0f, 0.35f, 0.04f, RED)
        SB.quad(9.8f, 1.7f, 3.46f, 1f, 0f, 0f, 0f, 1f, 0f, 0.62f, 0.62f, Atlas.I_BUILD, Col.tint(1f, 1f, 1f, 1f))
        for (k in 0 until 4) {
            val cx = if (k % 2 == 0) 6.0f else 13.6f
            val cz = if (k < 2) 3.9f else 10.0f
            L.cone(cx, 0.4f, cz, 0.3f, 0.8f, CONE)
            L.cyl(cx, 0.42f, cz, 0.22f, 0.1f, WHITE)
            L.box(cx, 0.03f, cz, 0.6f, 0.06f, 0.6f, CONE)
        }
    }
}
