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
    const val BUILDING = 9
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
    const val MAGNET = 11
    const val COMBOWIN = 12
    const val DASHCD = 13
    const val COUNT = 14
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
    const val DASH = 12
    const val COMBO = 13
    const val GOLD = 14
    const val SPIN = 15
    const val ACHIEVE = 16
    const val COUNT = 17
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

/** Fixed shop layout shared by all franchise locations (big, roomy plot with solid wall boundaries). */
object Layout {
    const val N = 18
    const val ID_MAIDA = 0
    const val ID_MEAT = 1
    const val ID_JHOL = 2
    const val ID_PREP = 3
    const val ID_MOKTU = 4
    const val ID_CAULDRON = 5
    const val ID_TABLE0 = 6
    const val ID_KETTLE = 12
    const val ID_BENCH0 = 13
    const val ID_BUILDING = 17
    const val INITIAL_MASK = 0x7F

    // plot geometry shared by game + renderer (strictly inside building walls and in front of back counter row)
    const val QZ = 12.4f          // queue line
    const val WALK_Z = 15.4f      // customers walk along this sidewalk line
    const val LANE_Z = -5.2f      // staff lane between kitchen row and floor
    const val DIV_Z = -4.7f
    const val MIN_X = -11.5f
    const val MAX_X = 14.5f
    const val MIN_Z = -6.7f       // keeps player in front of the z=-8 kitchen counter wall so they never go inside walls
    const val MAX_Z = 12.6f
    const val MAX_Z_THELA = 10.2f

    val KIND = intArrayOf(
        SK.MAIDA, SK.MEAT, SK.JHOL, SK.PREP, SK.MOKTU, SK.CAULDRON,
        SK.TABLE, SK.TABLE, SK.TABLE, SK.TABLE, SK.TABLE, SK.TABLE,
        SK.KETTLE, SK.BENCH, SK.BENCH, SK.BENCH, SK.BENCH, SK.BUILDING
    )
    val X = floatArrayOf(
        -10f, -6.5f, 9.5f, -2f, 3.5f, 9.5f,
        -7f, -2.5f, 2f, -7f, -2.5f, 2f,
        13f, 8.2f, 12f, 8.2f, 12f, 9.8f
    )
    val Z = floatArrayOf(
        -8f, -8f, -8f, -8f, -8f, -3.2f,
        1.5f, 1.5f, 1.5f, 6.5f, 6.5f, 6.5f,
        1.8f, 5.4f, 5.4f, 9.4f, 9.4f, 6.8f
    )
    val BASE = doubleArrayOf(
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 150.0, 450.0, 1000.0, 2200.0, 4500.0,
        800.0, 350.0, 800.0, 1600.0, 3000.0, 1800.0
    )
    val NAME = arrayOf(
        "Maida", "Meat", "Jhol Jars", "Prep Table", "Moktu", "Jhol Cauldron",
        "Table 1", "Table 2", "Table 3", "Table 4", "Table 5", "Table 6",
        "Chiya Kettle", "Chiya Bench 1", "Chiya Bench 2", "Chiya Bench 3", "Chiya Bench 4", "The Shop Building"
    )
    /** Stations that only exist once the street thela has grown into a real building. */
    val NEEDB = booleanArrayOf(
        false, false, false, false, false, false,
        false, false, false, true, true, true,
        true, true, true, true, true, false
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

    val AISLE = floatArrayOf(-4.75f, -0.25f, 5.0f)

    init {
        for (i in 0 until N) {
            val x = X[i]
            val z = Z[i]
            ZX[i] = x; ZZ[i] = z; ZR[i] = 1.65f
            PX[i] = x; PZ[i] = z
            WX[i] = ZX[i]; WZ[i] = ZZ[i]
            HX[i] = 1.5f; HZ[i] = 1.5f
            OB[i] = 1.2f
            when (KIND[i]) {
                SK.MAIDA, SK.MEAT -> { ZZ[i] = z + 1.65f; ZR[i] = 1.45f; OB[i] = 1.3f }
                SK.JHOL -> { ZZ[i] = z + 1.65f; ZR[i] = 1.45f; OB[i] = 1.25f }
                SK.PREP -> { ZZ[i] = z + 1.65f; ZR[i] = 1.55f; OB[i] = 1.45f }
                SK.MOKTU -> { ZZ[i] = z + 1.9f; ZR[i] = 1.65f; OB[i] = 1.65f; HX[i] = 1.8f; HZ[i] = 1.8f }
                SK.CAULDRON -> { ZZ[i] = z; ZR[i] = 1.65f; OB[i] = 1.0f }
                SK.TABLE -> {
                    ZX[i] = x + 0.5f; ZZ[i] = z + 0.8f; ZR[i] = 2.35f
                    PX[i] = x + 1.5f; PZ[i] = z - 0.55f
                    WX[i] = x + 1.75f; WZ[i] = z + 0.2f
                    HX[i] = 1.7f; HZ[i] = 1.4f; OB[i] = 1.15f
                }
                SK.KETTLE -> {
                    ZX[i] = x - 0.3f; ZZ[i] = z + 0.5f; ZR[i] = 1.65f; OB[i] = 1.1f
                    PX[i] = x - 1.4f; PZ[i] = z
                }
                SK.BENCH -> {
                    ZX[i] = x; ZZ[i] = z - 0.3f; ZR[i] = 2.1f
                    PX[i] = x + 1.4f; PZ[i] = z - 0.3f
                    WX[i] = x; WZ[i] = z - 1.35f
                    HX[i] = 1.6f; HZ[i] = 1.3f; OB[i] = 1.15f
                }
                SK.BUILDING -> {
                    HX[i] = 3.2f; HZ[i] = 2.6f; OB[i] = 0f; ZR[i] = 0f
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
        var bd = kotlin.math.abs(sx - best)
        for (k in 1 until 3) {
            val d = kotlin.math.abs(sx - AISLE[k])
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
}

/** Upgrade definitions. */
object UpDef {
    val NAME = arrayOf(
        "Player Speed", "Carry Capacity", "Momo Price", "Moktu Tiers", "Jhol Cauldron",
        "Kitchen Kancha", "Jhol Bhai", "Chiya Didi", "Cashier Dai",
        "No Camping! (Wi-Fi password changed)", "Surya & Samosa Upsell",
        "Cash Magnet", "Combo Master", "Dash Boots"
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
        "Campers keep buying Surya & samosas while they debate.",
        "Pull cash piles from further away. No more tiptoeing!",
        "Combo timer lasts longer and multipliers grow higher.",
        "Dash recharges faster and lasts longer."
    )
    val BASE = doubleArrayOf(60.0, 90.0, 120.0, 200.0, 150.0, 250.0, 300.0, 350.0, 400.0, 3000.0, 4500.0, 180.0, 220.0, 160.0)
    val GROW = doubleArrayOf(1.55, 1.6, 1.7, 1.75, 1.7, 2.1, 2.1, 2.1, 2.1, 1.0, 1.0, 1.8, 1.8, 1.7)
    val MAX = intArrayOf(15, 15, 25, 10, 8, 5, 5, 5, 5, 1, 1, 8, 6, 6)
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
    val vip: Int = 0,
    val bldg: Boolean = false,
    val goal: String = "",
    val goalFrac: Float = 0f,
    val rush: Int = 0,
    val jholPct: Int = 100,
    val dark: Int = 0,
    val combo: Int = 0,
    val dashCd: Float = 0f,
    val comboFrac: Float = 0f,
    val comboMult: Float = 1f,
    val dashing: Boolean = false,
    val dashReady: Boolean = true,
    val gold: Boolean = false,
    val spinReady: Boolean = false,
    val achReady: Int = 0,
    val streakReady: Boolean = false,
    val dbl: Int = 0,
    val level: Int = 1,
    val levelFrac: Float = 0f,
    val bestCombo: Int = 0
)

/** Pop-up label ids (index into the atlas label table). */
object Lbl {
    const val COMBO = 0
    const val PERFECT = 1
    const val GOLD = 2
    const val TIP = 3
    const val GEM = 4
    const val LEVEL = 5
    const val SPEED = 6
    const val UNLOCK = 7
    const val LUCKY = 8
    const val FRENZY = 9
}

object Glyph {
    const val K = 10
    const val M = 11
    const val B = 12
    const val T = 13
    const val Q = 14
    const val DOT = 15
    const val X = 16
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

    fun format(v0: Double, out: IntArray): Int = formatAt(v0, out, 0)

    /** Formats v into glyph codes without allocating, starting at out[start]. Returns the end index. */
    fun formatAt(v0: Double, out: IntArray, start: Int): Int {
        var v = if (v0 < 0.0) 0.0 else v0
        var suf = 0
        while (v >= 1000.0 && suf < 5) {
            v /= 1000.0
            suf++
        }
        var n: Int
        if (suf == 0) {
            n = writeInt(v.toLong(), out, start)
        } else {
            if (v >= 100.0) {
                n = writeInt(v.toLong(), out, start)
            } else {
                val scaled = (v * 10.0).toLong()
                n = writeInt(scaled / 10L, out, start)
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
