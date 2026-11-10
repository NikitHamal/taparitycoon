package com.tapari.tycoon

import java.util.Locale

/** Item types that can be carried. */
object Item {
    const val NONE = 0
    const val MAIDA = 1
    const val MEAT = 2
    const val JHOL = 3
    const val PLATE = 4
    const val CUP = 5
}

/** Station kinds. */
object SK {
    const val MAIDA = 0
    const val MEAT = 1
    const val JHOL = 2
    const val PREP = 3
    const val MOKTU = 4
    const val CAULDRON = 5
    const val TABLE = 6
    const val KETTLE = 7
    const val BENCH = 8
}

/** Customer types. */
object CT {
    const val NORMAL = 0
    const val STUDENT = 1
    const val VLOGGER = 2
    const val BIKER = 3
    const val TOURIST = 4
}

/** Staff roles. */
object Role {
    const val KANCHA = 0
    const val JHOL = 1
    const val DIDI = 2
    const val DAI = 3
}

/** Upgrade ids. */
object Up {
    const val SPEED = 0
    const val CARRY = 1
    const val PRICE = 2
    const val MOKTU = 3
    const val CAULDRON = 4
    const val KANCHA = 5
    const val JHOLBHAI = 6
    const val DIDI = 7
    const val DAI = 8
    const val NOCAMP = 9
    const val UPSELL = 10
    const val COUNT = 11
}

/** Sound effect ids. */
object Sfx {
    const val POP = 0
    const val CASH = 1
    const val UNLOCK = 2
    const val YELL = 3
    const val ANGRY = 4
    const val STEAM = 5
    const val UPGRADE = 6
    const val HORN = 7
    const val POUR = 8
    const val BELL = 9
    const val VIP = 10
    const val TAP = 11
    const val COUNT = 12
}

/** Haptic kinds. */
object Hap {
    const val TICK = 0
    const val THUMP = 1
    const val BUZZ = 2
    const val SUCCESS = 3
}

/** Speech bubble ids (index into the atlas bubble table). */
object Bub {
    const val NONE = -1
    const val JHOL = 0
    const val MOMO = 1
    const val CHIYA = 2
    const val HAPPY = 3
    const val ANGRY = 4
    const val CHAT0 = 5   // Startup idea bro...
    const val CHAT1 = 6   // Politics ko kura
    const val CHAT2 = 7   // WiFi password?
    const val CHAT3 = 8   // Bro, Web3 is the future
    const val SURYA = 9
    const val THANKS = 10
    const val VLOG = 11
    const val BIKE = 12
    const val TOUR = 13
    const val NAMASTE = 14
    const val EXCL = 15
    const val COUNT = 16
}

object Col {
    fun rgb(hex: Int): Int {
        val r = (hex shr 16) and 255
        val g = (hex shr 8) and 255
        val b = hex and 255
        return (255 shl 24) or (b shl 16) or (g shl 8) or r
    }

    /** a < 255 makes the colour partially emissive (unlit). */
    fun emis(hex: Int, a: Int): Int {
        val r = (hex shr 16) and 255
        val g = (hex shr 8) and 255
        val b = hex and 255
        return (a shl 24) or (b shl 16) or (g shl 8) or r
    }

    fun tint(r: Float, g: Float, b: Float, a: Float): Int {
        val ri = (r.coerceIn(0f, 1f) * 255f).toInt()
        val gi = (g.coerceIn(0f, 1f) * 255f).toInt()
        val bi = (b.coerceIn(0f, 1f) * 255f).toInt()
        val ai = (a.coerceIn(0f, 1f) * 255f).toInt()
        return (ai shl 24) or (bi shl 16) or (gi shl 8) or ri
    }
}

/** Fixed shop layout shared by all franchise locations. */
object Layout {
    const val N = 17
    const val ID_MAIDA = 0
    const val ID_MEAT = 1
    const val ID_JHOL = 2
    const val ID_PREP = 3
    const val ID_MOKTU = 4
    const val ID_CAULDRON = 5
    const val ID_TABLE0 = 6
    const val ID_KETTLE = 12
    const val ID_BENCH0 = 13
    const val INITIAL_MASK = 0x7F

    val KIND = intArrayOf(
        SK.MAIDA, SK.MEAT, SK.JHOL, SK.PREP, SK.MOKTU, SK.CAULDRON,
        SK.TABLE, SK.TABLE, SK.TABLE, SK.TABLE, SK.TABLE, SK.TABLE,
        SK.KETTLE, SK.BENCH, SK.BENCH, SK.BENCH, SK.BENCH
    )
    val X = floatArrayOf(
        -6.5f, -3.5f, 7.5f, -0.5f, 3.5f, 7.5f,
        -5f, -1.5f, 2f, -5f, -1.5f, 2f,
        9.5f, 6.4f, 9.4f, 6.4f, 9.4f
    )
    val Z = floatArrayOf(
        -5.5f, -5.5f, -5.5f, -5.5f, -5.5f, -1.0f,
        0.5f, 0.5f, 0.5f, 4.5f, 4.5f, 4.5f,
        1.2f, 4.2f, 4.2f, 7.6f, 7.6f
    )
    val BASE = doubleArrayOf(
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 150.0, 450.0, 1000.0, 2200.0, 4500.0,
        800.0, 350.0, 800.0, 1600.0, 3000.0
    )
    val NAME = arrayOf(
        "Maida", "Meat", "Jhol Jars", "Prep Table", "Moktu", "Jhol Cauldron",
        "Table 1", "Table 2", "Table 3", "Table 4", "Table 5", "Table 6",
        "Chiya Kettle", "Chiya Bench 1", "Chiya Bench 2", "Chiya Bench 3", "Chiya Bench 4"
    )

    val ZX = FloatArray(N)
    val ZZ = FloatArray(N)
    val ZR = FloatArray(N)
    val PX = FloatArray(N)   // cash pile
    val PZ = FloatArray(N)
    val WX = FloatArray(N)   // staff work spot
    val WZ = FloatArray(N)
    val HX = FloatArray(N)   // pad half extents
    val HZ = FloatArray(N)
    val OB = FloatArray(N)   // obstacle radius

    val AISLE = floatArrayOf(-3.25f, 0.25f, 4.6f)

    init {
        for (i in 0 until N) {
            val x = X[i]
            val z = Z[i]
            ZX[i] = x; ZZ[i] = z; ZR[i] = 1.7f
            PX[i] = x; PZ[i] = z
            WX[i] = ZX[i]; WZ[i] = ZZ[i]
            HX[i] = 1.5f; HZ[i] = 1.5f
            OB[i] = 1.2f
            when (KIND[i]) {
                SK.MAIDA, SK.MEAT -> { ZZ[i] = z + 1.6f; ZR[i] = 1.3f; OB[i] = 1.25f }
                SK.JHOL -> { ZZ[i] = z + 1.6f; ZR[i] = 1.3f; OB[i] = 1.2f }
                SK.PREP -> { ZZ[i] = z + 1.6f; ZR[i] = 1.55f; OB[i] = 1.4f }
                SK.MOKTU -> { ZZ[i] = z + 1.9f; ZR[i] = 1.6f; OB[i] = 1.6f; HX[i] = 1.8f; HZ[i] = 1.8f }
                SK.CAULDRON -> { ZZ[i] = z; ZR[i] = 1.6f; OB[i] = 0.95f }
                SK.TABLE -> {
                    ZX[i] = x + 0.5f; ZZ[i] = z + 0.8f; ZR[i] = 2.4f
                    PX[i] = x + 1.5f; PZ[i] = z - 0.55f
                    WX[i] = x + 1.75f; WZ[i] = z + 0.2f
                    HX[i] = 1.7f; HZ[i] = 1.4f; OB[i] = 1.05f
                }
                SK.KETTLE -> {
                    ZX[i] = x - 0.3f; ZZ[i] = z + 0.5f; ZR[i] = 1.6f; OB[i] = 1.0f
                    PX[i] = x - 1.4f; PZ[i] = z
                }
                SK.BENCH -> {
                    ZX[i] = x; ZZ[i] = z - 0.3f; ZR[i] = 2.2f
                    PX[i] = x + 1.4f; PZ[i] = z - 0.3f
                    WX[i] = x; WZ[i] = z - 1.35f
                    HX[i] = 1.6f; HZ[i] = 1.3f; OB[i] = 1.1f
                }
            }
        }
    }

    fun seatX(i: Int, seat: Int): Float {
        return if (KIND[i] == SK.TABLE) X[i] + (if (seat == 0) -0.7f else 0.7f)
        else X[i] + (if (seat == 0) -0.8f else 0.8f)
    }

    fun seatZ(i: Int): Float = if (KIND[i] == SK.TABLE) Z[i] + 1.0f else Z[i] + 0.95f

    fun corZ(i: Int): Float {
        return if (KIND[i] == SK.TABLE) Z[i] + 2.45f
        else Z[i] + (if (Z[i] < 6f) 1.9f else 2.4f)
    }

    fun aisleFor(i: Int, sx: Float): Float {
        if (KIND[i] == SK.BENCH) return AISLE[2]
        var best = AISLE[0]
        var bd = Math.abs(sx - best)
        for (k in 1 until 3) {
            val d = Math.abs(sx - AISLE[k])
            if (d < bd) { bd = d; best = AISLE[k] }
        }
        return best
    }
}

/** Franchise locations. */
object Locs {
    const val COUNT = 4
    val NAME = arrayOf(
        "Baneshwor Thela", "New Road Shutter", "Jhamsikhel Rooftop", "Global Momo Empire"
    )
    val TAG = arrayOf(
        "A humble street cart with huge dreams.",
        "Tiled floors, tilted prices. Real shutter, real rent.",
        "Fairy lights, skyline views and 600 rupee lattes.",
        "Kathmandu Momo goes worldwide. Neon, gold, Wi-Fi."
    )
    val MULT = doubleArrayOf(1.0, 8.0, 60.0, 500.0)
    val COST = doubleArrayOf(0.0, 15000.0, 250000.0, 5000000.0)
    val EMOJI = arrayOf("\uD83D\uDED2", "\uD83C\uDFEA", "\uD83C\uDF06", "\uD83C\uDF0F")
}

/** Upgrade definitions. */
object UpDef {
    val NAME = arrayOf(
        "Player Speed", "Carry Capacity", "Momo Price", "Moktu Tiers", "Jhol Cauldron",
        "Kitchen Kancha", "Jhol Bhai", "Chiya Didi", "Cashier Dai",
        "No Camping! (Wi-Fi password changed)", "Surya & Samosa Upsell"
    )
    val DESC = arrayOf(
        "Run faster between stations.",
        "Carry more bags, jars and plates.",
        "Customers pay more per plate and cup.",
        "More steamer tiers cook momos faster.",
        "Bigger Piro Jhol cauldron holds more spice.",
        "Hauls maida, meat and plates. Speeds up prep.",
        "Keeps the Extra Jhol cauldron topped up.",
        "Serves chiya to benches and brews faster.",
        "Collects cash piles from tables for you.",
        "Students leave right after one cup. Free the benches!",
        "Campers keep buying Surya & samosas while they debate."
    )
    val EMOJI = arrayOf(
        "\uD83D\uDC5F", "\uD83C\uDF92", "\uD83D\uDCB0", "\u2668\uFE0F", "\uD83C\uDF36\uFE0F",
        "\uD83D\uDC68\u200D\uD83C\uDF73", "\uD83E\uDDC2", "\u2615", "\uD83D\uDCB5",
        "\uD83D\uDEAB", "\uD83D\uDEAC"
    )
    val BASE = doubleArrayOf(60.0, 90.0, 120.0, 200.0, 150.0, 250.0, 300.0, 350.0, 400.0, 3000.0, 4500.0)
    val GROW = doubleArrayOf(1.55, 1.6, 1.7, 1.75, 1.7, 2.1, 2.1, 2.1, 2.1, 1.0, 1.0)
    val MAX = intArrayOf(15, 15, 25, 10, 8, 5, 5, 5, 5, 1, 1)
}

/** A snapshot of everything the Compose HUD needs. Rebuilt ~8x per second. */
data class Hud(
    val money: Double = 0.0,
    val gems: Int = 0,
    val jhol: Float = 1f,
    val jholAlert: Boolean = false,
    val jholLow: Boolean = false,
    val carry: Int = 0,
    val cap: Int = 6,
    val loc: Int = 0,
    val hint: String = "",
    val boost: Int = 0,
    val ready: Int = 0,
    val canPrestige: Boolean = false,
    val tick: Int = 0,
    val toast: String = "",
    val toastSeq: Int = 0,
    val vip: Int = 0
)

object Glyph {
    const val K = 10
    const val M = 11
    const val B = 12
    const val T = 13
    const val Q = 14
    const val DOT = 15
    private val tmp = IntArray(24)

    private fun writeInt(x0: Long, out: IntArray, n0: Int): Int {
        var n = n0
        var x = x0
        if (x <= 0L) {
            out[n++] = 0
            return n
        }
        var t = 0
        while (x > 0L) {
            tmp[t++] = (x % 10L).toInt()
            x /= 10L
        }
        while (t > 0) {
            t--
            out[n++] = tmp[t]
        }
        return n
    }

    /** Formats v into glyph codes without allocating. Returns glyph count. */
    fun format(v0: Double, out: IntArray): Int {
        var v = if (v0 < 0.0) 0.0 else v0
        var suf = 0
        while (v >= 1000.0 && suf < 5) {
            v /= 1000.0
            suf++
        }
        var n: Int
        if (suf == 0) {
            n = writeInt(v.toLong(), out, 0)
        } else {
            if (v >= 100.0) {
                n = writeInt(v.toLong(), out, 0)
            } else {
                val scaled = (v * 10.0).toLong()
                n = writeInt(scaled / 10L, out, 0)
                out[n++] = DOT
                out[n++] = (scaled % 10L).toInt()
            }
            out[n++] = 9 + suf
        }
        return n
    }
}

fun fmtMoney(v: Double): String {
    if (v < 1000.0) return v.toLong().toString()
    val suf = arrayOf("", "K", "M", "B", "T", "Qa", "Qi")
    var x = v
    var i = 0
    while (x >= 1000.0 && i < suf.size - 1) {
        x /= 1000.0
        i++
    }
    return if (x >= 100.0) String.format(Locale.US, "%d%s", x.toLong(), suf[i])
    else if (x >= 10.0) String.format(Locale.US, "%.1f%s", x, suf[i])
    else String.format(Locale.US, "%.2f%s", x, suf[i])
}
