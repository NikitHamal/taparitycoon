package com.tapari.tycoon

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

interface Fx {
    fun sfx(id: Int, rate: Float, vol: Float)
    fun haptic(kind: Int)
}

class Station(val id: Int) {
    var kind = 0
    var x = 0f
    var z = 0f
    var unlocked = false
    var cost = 0.0
    var paid = 0.0
    var stockA = 0
    var stockB = 0
    var raw = 0
    var prog = 0f
    var level = 0f
    var cashAmt = 0.0
    var acc = 0.0
    var bills = 0
    var occ0 = -1
    var occ1 = -1
    var flash = 0f
    var pop = 0f
}

class Cust {
    var active = false
    var type = 0
    var x = 0f
    var z = 0f
    var heading = 0f
    var walk = 0f
    var speed = 2.4f
    var state = 0
    var timer = 0f
    var patience = 0f
    var patMax = 1f
    var eatT = 0f
    var eatDur = 5f
    var seatSt = -1
    var seatI = 0
    var sx = 0f
    var sz = 0f
    var queueSlot = -1
    val wx = FloatArray(6)
    val wz = FloatArray(6)
    var wn = 0
    var wi = 0
    var jholReq = 0
    var jholAt = 0f
    var wantJhol = false
    var jholAnger = 0f
    var bubble = -1
    var bubbleT = 0f
    var shirt = 0
    var pants = 0
    var skin = 0
    var hair = 0
    var campT = 0f
    var upsellT = 0f
    var chatT = 0f
    var angry = false
    var flip = 0f
    var seated = false
    var fxT = 0f
}

class Staff {
    var active = false
    var role = 0
    var idx = 0
    var x = 0f
    var z = 0f
    var heading = 0f
    var walk = 0f
    var state = 0
    var timer = 0f
    var item = 0
    var n = 0
    var exp = 0
    var src = -1
    var dst = -1
    var moving = false
}

class Game {
    companion object {
        const val NST = Layout.N
        const val MAXC = 28
        const val MAXS = 20
        const val MAXCARRY = 64
        const val PMAX = 420
        const val QN = 6

        const val CS_TO_QUEUE = 1
        const val CS_QUEUE = 2
        const val CS_TO_SEAT = 3
        const val CS_WAIT = 4
        const val CS_EAT = 5
        const val CS_CAMP = 6
        const val CS_FLIP = 7
        const val CS_LEAVE = 8

        const val P_STEAM = 0
        const val P_RED = 1
        const val P_SPARK = 2
        const val P_FLY = 3
        const val P_SMOKE = 4
        const val P_NUM = 5
        const val P_JET = 6

        const val F_START_PLAYER = 1
        const val F_END_PLAYER = 2

        const val JHOL_DRAW = 6f
        const val JAR_UNITS = 20f
        const val JHOL_PATIENCE = 14f
        const val TABLE_CAP = 4
        const val BENCH_CAP = 3
        const val PREP_CAP = 8
        const val MOKTU_RAW_CAP = 10

        const val CH_PLATES = 0
        const val CH_CHIYA = 1
        const val CH_VIP = 2
        const val CH_JARS = 3
        const val CH_EARN = 4

        val QX = floatArrayOf(-5f, -3.5f, -2f, -0.5f, 1f, 2.5f)

        val SHIRTS = intArrayOf(
            Col.rgb(0xE5533D), Col.rgb(0x3D8BE5), Col.rgb(0x58B368), Col.rgb(0xF2B134),
            Col.rgb(0x9B59B6), Col.rgb(0x1ABC9C), Col.rgb(0xE67E9C), Col.rgb(0xF4F1E8)
        )
        val PANTS = intArrayOf(
            Col.rgb(0x2C3E50), Col.rgb(0x4A4A5A), Col.rgb(0x6B4E3D), Col.rgb(0x34495E)
        )
        val SKINS = intArrayOf(
            Col.rgb(0xF0C29A), Col.rgb(0xD9A074), Col.rgb(0xB9794E), Col.rgb(0xE8B48A)
        )
        val HAIRS = intArrayOf(
            Col.rgb(0x1E1A18), Col.rgb(0x2B2220), Col.rgb(0x40322A), Col.rgb(0x111111)
        )
        val ITEM_COL = intArrayOf(
            0, Col.rgb(0xF5EBD2), Col.rgb(0xE2574C), Col.rgb(0xC0201C),
            Col.rgb(0xFFFFFF), Col.rgb(0xB5703A)
        )
        val STAFF_COL = intArrayOf(
            Col.rgb(0xF28C28), Col.rgb(0xD6382F), Col.rgb(0xE85A9B), Col.rgb(0x3A6FD8)
        )
        val CH_NAME = arrayOf(
            "Serve momo plates", "Serve chiya cups", "Welcome VIP guests",
            "Refill Jhol jars", "Earn cash"
        )
    }

    val lock = Any()

    @Volatile var fx: Fx? = null
    var hudCb: ((Hud) -> Unit)? = null

    val st = Array(NST) { Station(it) }
    val cs = Array(MAXC) { Cust() }
    val staff = Array(MAXS) { Staff() }
    val queueOcc = IntArray(QN) { -1 }

    // ---- persistent progress ----
    var money = 0.0
    var gems = 0
    var lifetime = 0.0
    var runEarned = 0.0
    var loc = 0
    var locMask = 1
    val upg = Array(Locs.COUNT) { IntArray(Up.COUNT) }
    val padMask = IntArray(Locs.COUNT) { Layout.INITIAL_MASK }
    val padPaid = Array(Locs.COUNT) { DoubleArray(NST) }
    var incomeEma = 0.0
    var soundOn = true
    var hapticOn = true
    var platesServed = 0L
    var chiyaServed = 0L
    var vipServed = 0L
    var angryLeft = 0L
    var dayKey = 0
    val chType = IntArray(3)
    val chTarget = DoubleArray(3)
    val chProg = DoubleArray(3)
    val chDone = BooleanArray(3)
    val chReward = IntArray(3)
    var offlineEarn = 0.0
    var offlineSecs = 0L

    // ---- runtime ----
    @Volatile var inX = 0f
    @Volatile var inY = 0f
    var camYaw = 0.38f

    var time = 0f
    var px = 0.25f
    var pz = -1.0f
    var pvx = 0f
    var pvz = 0f
    var heading = 0f
    var walkPhase = 0f
    var swayX = 0f
    var swayZ = 0f
    var swayVX = 0f
    var swayVZ = 0f
    var moveAmt = 0f
    var ringPulse = 0f
    val carry = IntArray(MAXCARRY)
    var carryN = 0
    var interactT = 0f
    var chain = 0
    var chainT = 0f
    var padFx = 0f
    var hapT = 0f
    var dwellSt = -1
    var dwellT = 0f
    var slowOk = true

    var spawnT = 3f
    var queueT = 0f
    var jholCd = 0f
    var steamT = 0f
    var boostT = 0f
    var vlogT = 0f
    var jholAlert = false
    var jholLow = false
    var alertPulse = 0f
    var yellCd = 0f
    var staffN = 0
    val claimA = IntArray(NST)
    val claimB = IntArray(NST)

    var hudT = 0f
    var saveT = 0f
    var dayT = 0f
    var emaT = 0f
    var emaAcc = 0.0
    var toast = ""
    var toastSeq = 0
    var vipHere = 0
    var shake = 0f

    // particles (struct of arrays, zero allocation)
    var pN = 0
    val pKind = IntArray(PMAX)
    val pX = FloatArray(PMAX)
    val pY = FloatArray(PMAX)
    val pZ = FloatArray(PMAX)
    val pVX = FloatArray(PMAX)
    val pVY = FloatArray(PMAX)
    val pVZ = FloatArray(PMAX)
    val pAge = FloatArray(PMAX)
    val pLife = FloatArray(PMAX)
    val pSize = FloatArray(PMAX)
    val pCol = IntArray(PMAX)
    val pFlag = IntArray(PMAX)
    val pVal = DoubleArray(PMAX)

    private var seed = 0x2F6E2B1

    init {
        seed = seed xor (System.nanoTime().toInt() and 0x7FFFFFFF)
        if (seed == 0) seed = 12345
        ensureDay()
        enterLocation(0)
    }

    fun rnd(): Float {
        var s = seed
        s = s xor (s shl 13)
        s = s xor (s ushr 17)
        s = s xor (s shl 5)
        seed = s
        return ((s ushr 8) and 0xFFFFFF) / 16777216f
    }

    fun say(msg: String) {
        toast = msg
        toastSeq++
    }

    private fun sfx(id: Int, rate: Float = 1f, vol: Float = 1f) {
        val f = fx
        if (f != null) f.sfx(id, rate, vol)
    }

    private fun hap(kind: Int) {
        val f = fx
        if (f != null) f.haptic(kind)
    }

    // =====================================================================
    // Derived stats
    // =====================================================================
    fun lvl(id: Int): Int = upg[loc][id]
    fun playerSpeed(): Float = (4.4f + 0.3f * lvl(Up.SPEED)) * (if (boostT > 0f) 1.25f else 1f)
    fun carryCap(): Int = 6 + 2 * lvl(Up.CARRY)
    fun gemMult(): Double = 1.0 + 0.05 * gems
    fun platePrice(): Double = 30.0 * (1.0 + 0.2 * lvl(Up.PRICE)) * Locs.MULT[loc] * gemMult()
    fun chiyaPrice(): Double = 25.0 * (1.0 + 0.15 * lvl(Up.PRICE)) * Locs.MULT[loc] * gemMult()
    fun cauldronCap(): Float = 60f + 15f * lvl(Up.CAULDRON)
    fun plateCap(): Int = 10 + 2 * lvl(Up.MOKTU)
    fun staffCount(role: Int): Int = lvl(Up.KANCHA + role)
    fun staffCarry(): Int = 3 + lvl(Up.CARRY) / 4

    fun upgCost(l: Int, id: Int): Double =
        UpDef.BASE[id] * UpDef.GROW[id].pow(upg[l][id].toDouble()) * Locs.MULT[l]

    fun earn(amount: Double) {
        money += amount
        lifetime += amount
        runEarned += amount
        emaAcc += amount
        chAdd(CH_EARN, amount)
    }

    fun chAdd(type: Int, amount: Double) {
        for (i in 0 until 3) if (chType[i] == type && !chDone[i]) chProg[i] += amount
    }

    // =====================================================================
    // Daily challenges
    // =====================================================================
    fun ensureDay() {
        val day = (System.currentTimeMillis() / 86400000L).toInt()
        if (day == dayKey) return
        dayKey = day
        val a = day % 5
        chType[0] = a
        chType[1] = (a + 1) % 5
        chType[2] = (a + 3) % 5
        var best = 0
        for (i in 0 until Locs.COUNT) if ((locMask shr i) and 1 == 1) best = i
        for (i in 0 until 3) {
            chProg[i] = 0.0
            chDone[i] = false
            when (chType[i]) {
                CH_PLATES -> { chTarget[i] = (12 + (day % 3) * 8).toDouble(); chReward[i] = 2 }
                CH_CHIYA -> { chTarget[i] = (8 + (day % 3) * 4).toDouble(); chReward[i] = 2 }
                CH_VIP -> { chTarget[i] = (2 + day % 2).toDouble(); chReward[i] = 3 }
                CH_JARS -> { chTarget[i] = (6 + (day % 3) * 2).toDouble(); chReward[i] = 2 }
                else -> { chTarget[i] = 3000.0 * Locs.MULT[best]; chReward[i] = 3 }
            }
        }
    }

    fun chLabel(i: Int): String {
        val t = chTarget[i]
        return if (chType[i] == CH_EARN) "Earn NPR " + fmtMoney(t) else CH_NAME[chType[i]] + " x" + t.toLong()
    }

    fun claimChallenge(i: Int): Boolean {
        synchronized(lock) {
            if (chDone[i] || chProg[i] < chTarget[i]) return false
            chDone[i] = true
            gems += chReward[i]
            say("Challenge complete! +" + chReward[i] + " gems")
            sfx(Sfx.UNLOCK, 1.1f, 0.8f)
            hap(Hap.SUCCESS)
            return true
        }
    }

    // =====================================================================
    // Location handling
    // =====================================================================
    fun enterLocation(l: Int) {
        for (i in 0 until NST) {
            if (st[i].cashAmt > 0.0) earn(st[i].cashAmt)
        }
        loc = l
        for (i in 0 until NST) {
            val s = st[i]
            s.kind = Layout.KIND[i]
            s.x = Layout.X[i]
            s.z = Layout.Z[i]
            s.unlocked = ((padMask[l] shr i) and 1) == 1
            s.cost = Layout.BASE[i] * Locs.MULT[l]
            s.paid = padPaid[l][i]
            s.stockA = 0; s.stockB = 0; s.raw = 0; s.prog = 0f
            s.level = 0f
            s.cashAmt = 0.0; s.acc = 0.0; s.bills = 0
            s.occ0 = -1; s.occ1 = -1
            s.flash = 0f; s.pop = 0f
            claimA[i] = 0
            claimB[i] = 0
        }
        st[Layout.ID_CAULDRON].level = cauldronCap()
        for (i in 0 until MAXC) cs[i].active = false
        for (i in 0 until QN) queueOcc[i] = -1
        for (i in 0 until MAXS) staff[i].active = false
        staffN = 0
        for (r in 0 until 4) {
            val n = staffCount(r)
            for (k in 0 until n) addStaff(r)
        }
        pN = 0
        carryN = 0
        px = 0.25f
        pz = -1.0f
        pvx = 0f
        pvz = 0f
        spawnT = 2f
        jholAlert = false
        vlogT = 0f
        say(Locs.NAME[l])
    }

    fun travel(l: Int): Boolean {
        synchronized(lock) {
            if (l == loc) return true
            if (((locMask shr l) and 1) == 0) {
                if (money < Locs.COST[l]) return false
                money -= Locs.COST[l]
                locMask = locMask or (1 shl l)
                sfx(Sfx.UNLOCK, 0.9f, 1f)
                hap(Hap.SUCCESS)
            }
            enterLocation(l)
            return true
        }
    }

    fun prestigeGain(): Int = floor(sqrt(runEarned / 3000.0)).toInt()
    fun canPrestige(): Boolean = runEarned >= 20000.0 && prestigeGain() >= 1

    fun prestige(): Boolean {
        synchronized(lock) {
            if (!canPrestige()) return false
            val g = prestigeGain()
            gems += g
            money = 0.0
            runEarned = 0.0
            for (l in 0 until Locs.COUNT) {
                for (u in 0 until Up.COUNT) upg[l][u] = 0
                padMask[l] = Layout.INITIAL_MASK
                for (k in 0 until NST) padPaid[l][k] = 0.0
            }
            locMask = 1
            for (i in 0 until NST) st[i].cashAmt = 0.0
            enterLocation(0)
            say("Dhanyabaad! Franchise reborn. +" + g + " gems")
            sfx(Sfx.VIP, 1f, 1f)
            hap(Hap.SUCCESS)
            return true
        }
    }

    fun buyBoost(): Boolean {
        synchronized(lock) {
            if (gems < 3) return false
            gems -= 3
            boostT = 90f
            say("Chiya Rush! Double crowd for 90s")
            sfx(Sfx.UPGRADE, 1.2f, 1f)
            hap(Hap.SUCCESS)
            return true
        }
    }

    fun buyUpgrade(id: Int): Boolean {
        synchronized(lock) {
            if (upg[loc][id] >= UpDef.MAX[id]) return false
            if (id == Up.NOCAMP && upg[loc][Up.UPSELL] > 0) { say("You already chose to monetize the campers!"); return false }
            if (id == Up.UPSELL && upg[loc][Up.NOCAMP] > 0) { say("You already banned camping!"); return false }
            val c = upgCost(loc, id)
            if (money < c) return false
            money -= c
            upg[loc][id]++
            if (id >= Up.KANCHA && id <= Up.DAI) addStaff(id - Up.KANCHA)
            if (id == Up.CAULDRON) st[Layout.ID_CAULDRON].level += 15f
            sfx(Sfx.UPGRADE, 1f, 1f)
            hap(Hap.SUCCESS)
            return true
        }
    }

    fun claimOffline(double: Boolean) {
        synchronized(lock) {
            if (offlineEarn <= 0.0) return
            var amt = offlineEarn
            if (double) {
                if (gems < 2) return
                gems -= 2
                amt *= 2.0
            }
            earn(amt)
            offlineEarn = 0.0
            sfx(Sfx.CASH, 1f, 1f)
            hap(Hap.SUCCESS)
        }
    }

    fun applyOffline(secs: Long) {
        if (secs < 60L) return
        val capped = min(secs, 8L * 3600L)
        val e = incomeEma * 0.4 * capped
        if (e >= 1.0) {
            offlineEarn = e
            offlineSecs = capped
        }
    }

    private fun addStaff(role: Int) {
        if (staffN >= MAXS) return
        val s = staff[staffN]
        var c = 0
        for (i in 0 until staffN) if (staff[i].role == role) c++
        s.active = true
        s.role = role
        s.idx = c
        s.state = 0
        s.timer = 0.5f
        s.item = 0
        s.n = 0
        s.exp = 0
        s.src = -1
        s.dst = -1
        s.x = homeX(s)
        s.z = -2.4f
        s.heading = 0f
        staffN++
    }

    private fun homeX(s: Staff): Float = when (s.role) {
        Role.KANCHA -> 2.2f + s.idx * 0.8f
        Role.JHOL -> 5.6f + s.idx * 0.5f
        Role.DIDI -> 8.2f + s.idx * 0.6f
        else -> -1.2f + s.idx * 0.8f
    }

    private fun homeZ(s: Staff): Float = when (s.role) {
        Role.DIDI -> -0.2f
        Role.DAI -> -2.3f
        else -> -2.6f
    }

    // =====================================================================
    // Main update
    // =====================================================================
    fun update(dt0: Float) {
        val dt = if (dt0 > 0.05f) 0.05f else dt0
        synchronized(lock) {
            time += dt
            if (boostT > 0f) boostT -= dt
            if (vlogT > 0f) vlogT -= dt
            alertPulse += dt * 7f
            if (yellCd > 0f) yellCd -= dt
            if (hapT > 0f) hapT -= dt
            if (shake > 0f) shake -= dt
            updatePlayer(dt)
            updatePads(dt)
            interact(dt)
            produce(dt)
            updateCustomers(dt)
            updateStaff(dt)
            updateParticles(dt)
            for (i in 0 until NST) {
                val s = st[i]
                if (s.flash > 0f) s.flash -= dt * 3f
                if (s.pop > 0f) s.pop -= dt * 1.6f
            }
            emaT += dt
            if (emaT >= 1f) {
                incomeEma = incomeEma * 0.96 + emaAcc * 0.04
                emaAcc = 0.0
                emaT = 0f
            }
            dayT += dt
            if (dayT > 30f) { dayT = 0f; ensureDay() }
            saveT += dt
            hudT += dt
            if (hudT >= 0.12f) {
                hudT = 0f
                val cb = hudCb
                if (cb != null) cb(buildHud())
            }
        }
    }

    // ---- Player -------------------------------------------------------------
    private fun updatePlayer(dt: Float) {
        val ix = inX
        val iy = inY
        val sy = kotlin.math.sin(camYaw)
        val cy = kotlin.math.cos(camYaw)
        // camera right = (cos, -sin), forward = (-sin, -cos)
        val dx = cy * ix + (-sy) * iy
        val dz = (-sy) * ix + (-cy) * iy
        val mag = min(1f, sqrt(ix * ix + iy * iy))
        val spd = playerSpeed() * mag
        var len = sqrt(dx * dx + dz * dz)
        if (len < 0.0001f) len = 1f
        val tvx = dx / len * spd
        val tvz = dz / len * spd
        val a = min(1f, dt * 14f)
        val oldVx = pvx
        val oldVz = pvz
        pvx += (tvx - pvx) * a
        pvz += (tvz - pvz) * a
        px += pvx * dt
        pz += pvz * dt
        // collisions
        for (i in 0 until NST) {
            val s = st[i]
            if (!s.unlocked) continue
            val r = Layout.OB[i] + 0.35f
            val ox = px - s.x
            val oz = pz - s.z
            val d2 = ox * ox + oz * oz
            if (d2 < r * r) {
                val d = sqrt(d2)
                if (d > 0.0001f) {
                    px = s.x + ox / d * r
                    pz = s.z + oz / d * r
                } else {
                    pz = s.z + r
                }
            }
        }
        px = px.coerceIn(-8.8f, 10.9f)
        pz = pz.coerceIn(-6.8f, 11.6f)
        val sp = sqrt(pvx * pvx + pvz * pvz)
        moveAmt = min(1f, sp / 4f)
        if (sp > 0.4f) {
            val th = atan2(pvx, pvz)
            heading = lerpAngle(heading, th, min(1f, dt * 14f))
            walkPhase += sp * dt * 2.3f
        }
        // sway spring: pulled by acceleration
        val ax = (pvx - oldVx) / max(dt, 0.001f)
        val az = (pvz - oldVz) / max(dt, 0.001f)
        val tgtX = -pvx * 0.035f - ax * 0.004f
        val tgtZ = -pvz * 0.035f - az * 0.004f
        swayVX += ((tgtX - swayX) * 90f - swayVX * 7f) * dt
        swayVZ += ((tgtZ - swayZ) * 90f - swayVZ * 7f) * dt
        swayX += swayVX * dt
        swayZ += swayVZ * dt
        ringPulse += dt * 3f
        if (chainT > 0f) { chainT -= dt; if (chainT <= 0f) chain = 0 }
    }

    fun lerpAngle(h: Float, t: Float, k: Float): Float {
        var d = t - h
        while (d > Math.PI.toFloat()) d -= (2.0 * Math.PI).toFloat()
        while (d < -Math.PI.toFloat()) d += (2.0 * Math.PI).toFloat()
        return h + d * k
    }

    private fun inZone(i: Int, x: Float, z: Float): Boolean {
        val dx = x - Layout.ZX[i]
        val dz = z - Layout.ZZ[i]
        val r = Layout.ZR[i]
        return dx * dx + dz * dz <= r * r
    }

    // ---- item flow ------------------------------------------------------------
    fun canGive(s: Station): Int {
        return when (s.kind) {
            SK.MAIDA -> Item.MAIDA
            SK.MEAT -> Item.MEAT
            SK.JHOL -> Item.JHOL
            SK.MOKTU -> if (s.stockA > 0) Item.PLATE else 0
            SK.KETTLE -> if (s.stockA > 0) Item.CUP else 0
            else -> 0
        }
    }

    fun accepts(s: Station, item: Int): Boolean {
        return when (s.kind) {
            SK.PREP -> (item == Item.MAIDA && s.stockA < PREP_CAP) || (item == Item.MEAT && s.stockB < PREP_CAP)
            SK.CAULDRON -> item == Item.JHOL && s.level + JAR_UNITS <= cauldronCap() + 8f
            SK.TABLE -> item == Item.PLATE && s.stockA < TABLE_CAP
            SK.BENCH -> item == Item.CUP && s.stockA < BENCH_CAP
            else -> false
        }
    }

    private fun give(s: Station, item: Int) {
        when (s.kind) {
            SK.PREP -> if (item == Item.MAIDA) s.stockA++ else s.stockB++
            SK.CAULDRON -> {
                s.level = min(cauldronCap(), s.level + JAR_UNITS)
                chAdd(CH_JARS, 1.0)
                sfx(Sfx.POUR, 1f, 0.7f)
            }
            SK.TABLE, SK.BENCH -> s.stockA++
        }
        s.flash = 1f
    }

    private fun takeFrom(s: Station) {
        if (s.kind == SK.MOKTU || s.kind == SK.KETTLE) s.stockA--
        s.flash = 0.6f
    }

    private fun addCarry(item: Int) {
        if (carryN < MAXCARRY) carry[carryN++] = item
    }

    private fun removeCarry(idx: Int) {
        var i = idx
        while (i < carryN - 1) {
            carry[i] = carry[i + 1]
            i++
        }
        carryN--
    }

    private fun updateDwell(dt: Float) {
        var cur = -1
        for (k in 0 until NST) {
            val s = st[k]
            if (s.unlocked && canGive(s) != 0 && inZone(k, px, pz)) { cur = k; break }
        }
        if (cur != dwellSt) {
            dwellSt = cur
            dwellT = 0f
        } else if (cur >= 0) {
            dwellT += dt
        }
        slowOk = pvx * pvx + pvz * pvz < 3.2f * 3.2f
    }

    private fun interact(dt: Float) {
        updateDwell(dt)
        interactT -= dt
        if (interactT > 0f) return
        // 1) drops
        var i = carryN - 1
        while (i >= 0) {
            val item = carry[i]
            for (k in 0 until NST) {
                val s = st[k]
                if (!s.unlocked || !inZone(k, px, pz)) continue
                if (accepts(s, item)) {
                    removeCarry(i)
                    give(s, item)
                    flyItem(item, px, 1.5f, pz, s.x, 1.2f, s.z, F_START_PLAYER, 0.32f)
                    chain++
                    chainT = 0.6f
                    sfx(Sfx.POP, 0.85f + min(chain, 14) * 0.045f, 0.55f)
                    tick()
                    interactT = 0.085f
                    return
                }
            }
            i--
        }
        // 2) cash
        for (k in 0 until NST) {
            val s = st[k]
            if (!s.unlocked || s.bills <= 0 || !inZone(k, px, pz)) continue
            collectTick(s)
            interactT = 0.06f
            return
        }
        // 3) picks: stand still on a source for a beat (no accidental grabs while running past)
        if (carryN < carryCap() && dwellSt >= 0 && dwellT >= 0.3f && slowOk) {
            val k = dwellSt
            val s = st[k]
            val item = canGive(s)
            if (item != 0) {
                takeFrom(s)
                addCarry(item)
                flyItem(item, s.x, 1.2f, s.z + 0.4f, px, 1.5f, pz, F_END_PLAYER, 0.3f)
                chain++
                chainT = 0.6f
                sfx(Sfx.POP, 1.0f + min(chain, 14) * 0.045f, 0.5f)
                tick()
                interactT = 0.09f
                return
            }
        }
        interactT = 0.03f
    }

    private fun tick() {
        if (hapT <= 0f) {
            hap(Hap.TICK)
            hapT = 0.05f
        }
    }

    private fun collectTick(s: Station) {
        val k = max(1, s.bills / 4)
        var amt = s.cashAmt * (k.toDouble() / s.bills.toDouble())
        s.cashAmt -= amt
        s.bills -= k
        if (s.bills <= 0) {
            amt += s.cashAmt
            s.cashAmt = 0.0
            s.bills = 0
        }
        earn(amt)
        s.acc += amt
        val id = s.id
        flyItem(-1, Layout.PX[id], 0.5f, Layout.PZ[id], px, 1.4f, pz, F_END_PLAYER, 0.35f)
        if (k > 2) flyItem(-1, Layout.PX[id], 0.7f, Layout.PZ[id], px, 1.4f, pz, F_END_PLAYER, 0.42f)
        chain++
        chainT = 0.6f
        sfx(Sfx.CASH, 0.9f + min(chain, 12) * 0.05f, 0.6f)
        tick()
        if (s.bills == 0) {
            spawnP(P_NUM, Layout.PX[id], 1.6f, Layout.PZ[id], 0f, 0.8f, 0f, 1.3f, 0.5f, Col.rgb(0x6BE36B), s.acc)
            s.acc = 0.0
            hap(Hap.THUMP)
        }
    }

    // ---- Pads -------------------------------------------------------------------
    private fun updatePads(dt: Float) {
        padFx -= dt
        for (k in 0 until NST) {
            val s = st[k]
            if (s.unlocked) continue
            if (abs(px - s.x) < Layout.HX[k] && abs(pz - s.z) < Layout.HZ[k]) {
                val remain = s.cost - s.paid
                val rate = max(s.cost / 2.0, 1.0)
                var amt = min(remain, rate * dt)
                if (amt > money) amt = money
                if (amt > 0.0) {
                    money -= amt
                    s.paid += amt
                    padPaid[loc][k] = s.paid
                    if (padFx <= 0f) {
                        padFx = 0.06f
                        flyItem(-1, px, 1.4f, pz, s.x, 0.3f, s.z, F_START_PLAYER, 0.4f)
                        sfx(Sfx.CASH, 1.3f + rnd() * 0.3f, 0.35f)
                        tick()
                    }
                }
                if (s.cost - s.paid <= 0.5) unlockStation(s)
            }
        }
    }

    private fun unlockStation(s: Station) {
        s.unlocked = true
        s.paid = s.cost
        padPaid[loc][s.id] = s.cost
        padMask[loc] = padMask[loc] or (1 shl s.id)
        s.pop = 1f
        sparkBurst(s.x, 0.5f, s.z, 18, Col.rgb(0xFFD54A))
        sfx(Sfx.UNLOCK, 1f, 1f)
        hap(Hap.SUCCESS)
        say(if (s.kind == SK.KETTLE) "Chiya & Churot corner is open!" else "Unlocked: " + Layout.NAME[s.id])
    }

    // ---- Production ---------------------------------------------------------------
    private fun produce(dt: Float) {
        val prep = st[Layout.ID_PREP]
        val mok = st[Layout.ID_MOKTU]
        if (prep.stockA > 0 && prep.stockB > 0 && mok.raw < MOKTU_RAW_CAP) {
            prep.prog += dt * (1f / 1.2f) * (1f + 0.2f * staffCount(Role.KANCHA))
            if (prep.prog >= 1f) {
                prep.prog = 0f
                prep.stockA--
                prep.stockB--
                mok.raw++
                prep.flash = 1f
                for (n in 0 until 3) spawnP(P_SPARK, prep.x, 1.4f, prep.z, (rnd() - 0.5f) * 2f, 2f + rnd(), (rnd() - 0.5f) * 2f, 0.5f, 0.12f, Col.rgb(0xFFF2CC), 0.0)
            }
        }
        if (mok.raw > 0 && mok.stockA < plateCap()) {
            mok.prog += dt * (1f / 2.4f) * (1f + 0.35f * lvl(Up.MOKTU))
            if (mok.prog >= 1f) {
                mok.prog -= 1f
                mok.raw--
                mok.stockA++
                mok.flash = 1f
                for (n in 0 until 5) spawnP(P_STEAM, mok.x + (rnd() - 0.5f), 3.0f, mok.z + (rnd() - 0.5f), (rnd() - 0.5f) * 0.6f, 1.4f, (rnd() - 0.5f) * 0.6f, 1.2f, 0.7f, Col.rgb(0xFFFFFF), 0.0)
                sfx(Sfx.STEAM, 1f + rnd() * 0.2f, 0.45f)
            }
        }
        steamT -= dt
        if (steamT <= 0f) {
            steamT = if (mok.raw > 0) 0.09f else 0.42f
            spawnP(P_STEAM, mok.x + (rnd() - 0.5f) * 1.2f, 3.1f, mok.z + (rnd() - 0.5f) * 1.2f,
                (rnd() - 0.5f) * 0.5f, 1.1f + rnd() * 0.6f, (rnd() - 0.5f) * 0.5f, 1.8f, 0.55f, Col.rgb(0xFFFFFF), 0.0)
        }
        val ket = st[Layout.ID_KETTLE]
        if (ket.unlocked && ket.stockA < 8) {
            ket.prog += dt * (1f / 3.2f) * (1f + 0.5f * staffCount(Role.DIDI))
            if (ket.prog >= 1f) {
                ket.prog = 0f
                ket.stockA++
                ket.flash = 1f
                spawnP(P_STEAM, ket.x, 1.5f, ket.z, 0f, 1f, 0f, 1.0f, 0.4f, Col.rgb(0xFFFFFF), 0.0)
            }
        }
        // jhol alert state
        val cd = st[Layout.ID_CAULDRON]
        var waiting = 0
        for (i in 0 until MAXC) if (cs[i].active && cs[i].wantJhol) waiting++
        jholAlert = cd.level < JHOL_DRAW && waiting > 0
        jholLow = cd.level < cauldronCap() * 0.25f
        // serve jhol
        jholCd -= dt
        if (jholCd <= 0f && waiting > 0 && cd.level >= JHOL_DRAW) {
            var best = -1
            var ba = -1f
            for (i in 0 until MAXC) {
                val c = cs[i]
                if (c.active && c.wantJhol && c.jholAnger > ba) { ba = c.jholAnger; best = i }
            }
            if (best >= 0) {
                val c = cs[best]
                cd.level -= JHOL_DRAW
                cd.flash = 1f
                c.wantJhol = false
                c.jholReq--
                c.jholAt = c.eatDur * 0.75f
                c.bubble = Bub.NONE
                c.bubbleT = 0f
                for (n in 0 until 3) spawnP(P_JET, cd.x, 1.8f, cd.z, c.x, 1.5f, c.z, 0.55f + n * 0.08f, 0.2f, Col.rgb(0xD62A1C), 0.0)
                sfx(Sfx.POUR, 1f + rnd() * 0.2f, 0.6f)
                jholCd = 0.45f
            }
        }
    }

    // ---- Customers ------------------------------------------------------------------
    private fun seatsOpen(): Int {
        var n = 0
        for (i in 0 until NST) if (st[i].unlocked && (st[i].kind == SK.TABLE || st[i].kind == SK.BENCH)) n += 2
        return n
    }

    private fun benchesOpen(): Boolean = st[Layout.ID_KETTLE].unlocked && (st[13].unlocked || st[14].unlocked || st[15].unlocked || st[16].unlocked)

    private fun findSeat(student: Boolean): Int {
        val kind = if (student) SK.BENCH else SK.TABLE
        var best = -1
        var bestScore = -1
        for (i in 0 until NST) {
            val s = st[i]
            if (!s.unlocked || s.kind != kind) continue
            if (s.occ0 == -1 || s.occ1 == -1) {
                val seat = if (s.occ0 == -1) 0 else 1
                var score = 1
                if (s.stockA > 0) score = 2
                if (s.occ0 == -1 && s.occ1 == -1) score += 0
                if (score > bestScore) { bestScore = score; best = i * 2 + seat }
            }
        }
        return best
    }

    private fun setBubble(c: Cust, id: Int, t: Float) {
        c.bubble = id
        c.bubbleT = t
    }

    private fun spawnCust() {
        var idx = -1
        for (i in 0 until MAXC) if (!cs[i].active) { idx = i; break }
        if (idx < 0) return
        var type = CT.NORMAL
        val r = rnd()
        val vipChance = 0.08f + 0.02f * loc
        if (r < vipChance) {
            type = CT.VLOGGER + (rnd() * 3f).toInt().coerceIn(0, 2)
        } else if (benchesOpen() && rnd() < 0.32f) {
            type = CT.STUDENT
        }
        val seatCode = findSeat(type == CT.STUDENT)
        var slot = -1
        if (seatCode < 0) {
            for (i in 0 until QN) if (queueOcc[i] == -1) { slot = i; break }
            if (slot < 0) return
        }
        val c = cs[idx]
        c.active = true
        c.type = type
        c.speed = 2.3f + rnd() * 0.5f
        if (type == CT.BIKER) c.speed = 3.6f
        c.state = 0
        c.timer = 0f
        c.walk = rnd() * 6f
        c.seated = false
        c.angry = false
        c.flip = 0f
        c.wantJhol = false
        c.jholAnger = 0f
        c.bubble = Bub.NONE
        c.bubbleT = 0f
        c.seatSt = -1
        c.queueSlot = -1
        c.eatT = 0f
        c.fxT = 0f
        c.shirt = SHIRTS[(rnd() * SHIRTS.size).toInt().coerceIn(0, SHIRTS.size - 1)]
        c.pants = PANTS[(rnd() * PANTS.size).toInt().coerceIn(0, PANTS.size - 1)]
        c.skin = SKINS[(rnd() * SKINS.size).toInt().coerceIn(0, SKINS.size - 1)]
        c.hair = HAIRS[(rnd() * HAIRS.size).toInt().coerceIn(0, HAIRS.size - 1)]
        c.patMax = 28f + rnd() * 8f
        c.eatDur = 4.5f + rnd() * 1.5f
        c.jholReq = if (type == CT.STUDENT) 0 else 1
        when (type) {
            CT.STUDENT -> { c.shirt = Col.rgb(0x3B4560); c.patMax = 45f; c.eatDur = 3f }
            CT.VLOGGER -> { c.shirt = Col.rgb(0xFF4FA3); c.eatDur = 7f; c.patMax = 40f }
            CT.BIKER -> { c.shirt = Col.rgb(0x222222); c.eatDur = 3.5f; c.patMax = 24f }
            CT.TOURIST -> { c.shirt = Col.rgb(0x2FB5A8); c.eatDur = 6f; c.jholReq = 2; c.patMax = 45f }
        }
        c.jholAt = c.eatDur * 0.4f
        c.patience = c.patMax
        if (type >= CT.VLOGGER) {
            vipHere++
            spawnP(P_SPARK, 0.25f, 2f, 12f, 0f, 1f, 0f, 0.8f, 0.3f, Col.rgb(0xFFE066), 0.0)
            sfx(Sfx.BELL, 1f, 0.7f)
            if (type == CT.BIKER) sfx(Sfx.HORN, 1f, 0.7f)
            setBubble(c, if (type == CT.VLOGGER) Bub.VLOG else if (type == CT.BIKER) Bub.BIKE else Bub.TOUR, 2.6f)
        } else {
            setBubble(c, Bub.NAMASTE, 1.6f)
        }
        c.z = 13.3f
        if (seatCode >= 0) {
            val si = seatCode shr 1
            val seat = seatCode and 1
            reserve(c, idx, si, seat)
            c.x = Layout.aisleFor(si, Layout.seatX(si, seat))
            planToSeat(c)
            c.state = CS_TO_SEAT
        } else {
            queueOcc[slot] = idx
            c.queueSlot = slot
            c.x = QX[slot]
            c.wx[0] = QX[slot]
            c.wz[0] = 11.2f
            c.wn = 1
            c.wi = 0
            c.state = CS_TO_QUEUE
        }
        c.heading = Math.PI.toFloat()
    }

    private fun reserve(c: Cust, idx: Int, si: Int, seat: Int) {
        val s = st[si]
        if (seat == 0) s.occ0 = idx else s.occ1 = idx
        c.seatSt = si
        c.seatI = seat
        c.sx = Layout.seatX(si, seat)
        c.sz = Layout.seatZ(si)
    }

    private fun planToSeat(c: Cust) {
        val si = c.seatSt
        val ax = Layout.aisleFor(si, c.sx)
        val cz = Layout.corZ(si)
        c.wx[0] = ax; c.wz[0] = min(c.z, 11.2f)
        c.wx[1] = ax; c.wz[1] = cz
        c.wx[2] = c.sx; c.wz[2] = cz
        c.wx[3] = c.sx; c.wz[3] = c.sz
        c.wn = 4
        c.wi = 0
    }

    private fun planLeave(c: Cust) {
        if (c.seatSt >= 0) {
            val si = c.seatSt
            val ax = Layout.aisleFor(si, c.sx)
            val cz = Layout.corZ(si)
            c.wx[0] = c.sx; c.wz[0] = cz
            c.wx[1] = ax; c.wz[1] = cz
            c.wx[2] = ax; c.wz[2] = 14.5f
            c.wn = 3
        } else {
            c.wx[0] = c.x; c.wz[0] = 14.5f
            c.wn = 1
        }
        c.wi = 0
    }

    private fun releaseSeat(c: Cust, idx: Int) {
        if (c.seatSt >= 0) {
            val s = st[c.seatSt]
            if (c.seatI == 0) { if (s.occ0 == idx) s.occ0 = -1 } else { if (s.occ1 == idx) s.occ1 = -1 }
        }
        if (c.queueSlot >= 0) {
            queueOcc[c.queueSlot] = -1
            c.queueSlot = -1
        }
    }

    private fun startLeave(c: Cust, idx: Int, angry: Boolean) {
        planLeave(c)
        releaseSeat(c, idx)
        c.seatSt = -1
        c.seated = false
        c.state = CS_LEAVE
        c.angry = angry
        c.wantJhol = false
        if (c.type >= CT.VLOGGER) vipHere = max(0, vipHere - 1)
    }

    private fun flipOut(c: Cust, idx: Int) {
        planLeave(c)
        releaseSeat(c, idx)
        c.seatSt = -1
        c.seated = false
        c.wantJhol = false
        c.state = CS_FLIP
        c.flip = 0f
        c.angry = true
        setBubble(c, Bub.ANGRY, 2.2f)
        angryLeft++
        sfx(Sfx.ANGRY, 1f, 0.9f)
        hap(Hap.BUZZ)
        shake = 0.25f
        for (n in 0 until 8) spawnP(P_RED, c.x + (rnd() - 0.5f) * 0.4f, 1.9f, c.z + (rnd() - 0.5f) * 0.4f,
            (rnd() - 0.5f) * 1.6f, 1.6f + rnd(), (rnd() - 0.5f) * 1.6f, 1.2f, 0.5f, Col.rgb(0xFF3B2F), 0.0)
        if (c.type >= CT.VLOGGER) vipHere = max(0, vipHere - 1)
    }

    private fun moveAlong(c: Cust, dt: Float, spd: Float): Boolean {
        if (c.wi >= c.wn) return true
        val tx = c.wx[c.wi]
        val tz = c.wz[c.wi]
        val dx = tx - c.x
        val dz = tz - c.z
        val d = sqrt(dx * dx + dz * dz)
        val step = spd * dt
        if (d <= step + 0.02f) {
            c.x = tx
            c.z = tz
            c.wi++
            return c.wi >= c.wn
        }
        c.x += dx / d * step
        c.z += dz / d * step
        c.heading = lerpAngle(c.heading, atan2(dx, dz), min(1f, dt * 10f))
        c.walk += spd * dt * 2.4f
        return false
    }

    private fun payCustomer(c: Cust) {
        val s = st[c.seatSt]
        val student = c.type == CT.STUDENT
        val base = if (student) chiyaPrice() else platePrice()
        val mul = when (c.type) {
            CT.VLOGGER -> 5.0
            CT.BIKER -> 3.0
            CT.TOURIST -> 4.0
            else -> 1.0
        }
        val amt = base * mul * (1.0 + 0.25 * rnd())
        s.cashAmt += amt
        s.bills = min(90, s.bills + (if (c.type >= CT.VLOGGER) 4 else 2))
        setBubble(c, Bub.HAPPY, 1.6f)
        sparkBurst(c.x, 1.8f, c.z, 5, Col.rgb(0xFFE066))
        sfx(Sfx.CASH, 1.5f, 0.3f)
        if (student) { chiyaServed++; chAdd(CH_CHIYA, 1.0) } else { platesServed++; chAdd(CH_PLATES, 1.0) }
        if (c.type >= CT.VLOGGER) {
            vipServed++
            chAdd(CH_VIP, 1.0)
            if (rnd() < 0.3f) {
                gems += 1
                say("VIP served! +1 gem")
            } else {
                say("VIP guest paid a fat tip!")
            }
            sfx(Sfx.VIP, 1f, 0.8f)
            hap(Hap.SUCCESS)
        }
    }

    private fun updateCustomers(dt: Float) {
        // spawning
        var speedMul = 1f
        if (boostT > 0f) speedMul += 1f
        if (vlogT > 0f) speedMul += 0.5f
        spawnT -= dt * speedMul
        if (spawnT <= 0f) {
            val seats = seatsOpen()
            var iv = 7.5f - 0.5f * seats
            if (iv < 1.6f) iv = 1.6f
            iv *= 1f - 0.07f * loc
            spawnT = iv * (0.8f + rnd() * 0.5f)
            spawnCust()
        }
        // queue assignment
        queueT -= dt
        if (queueT <= 0f) {
            queueT = 0.3f
            for (q in 0 until QN) {
                val ci = queueOcc[q]
                if (ci < 0) continue
                val c = cs[ci]
                if (!c.active || c.state != CS_QUEUE) continue
                val code = findSeat(c.type == CT.STUDENT)
                if (code >= 0) {
                    reserve(c, ci, code shr 1, code and 1)
                    queueOcc[q] = -1
                    c.queueSlot = -1
                    planToSeat(c)
                    c.state = CS_TO_SEAT
                }
            }
        }
        for (i in 0 until MAXC) {
            val c = cs[i]
            if (!c.active) continue
            if (c.bubbleT > 0f) {
                c.bubbleT -= dt
                if (c.bubbleT <= 0f) c.bubble = Bub.NONE
            }
            when (c.state) {
                CS_TO_QUEUE -> if (moveAlong(c, dt, c.speed)) {
                    c.state = CS_QUEUE
                    c.heading = Math.PI.toFloat()
                    if (c.bubble == Bub.NONE) setBubble(c, if (c.type == CT.STUDENT) Bub.CHIYA else Bub.MOMO, -1f)
                }
                CS_QUEUE -> {
                    c.timer += dt
                    if (c.timer > 32f) {
                        setBubble(c, Bub.NONE, 0f)
                        startLeave(c, i, false)
                    }
                }
                CS_TO_SEAT -> if (moveAlong(c, dt, c.speed)) {
                    c.state = CS_WAIT
                    c.seated = true
                    c.heading = Math.PI.toFloat()
                    setBubble(c, if (c.type == CT.STUDENT) Bub.CHIYA else Bub.MOMO, -1f)
                }
                CS_WAIT -> {
                    val s = st[c.seatSt]
                    c.patience -= dt
                    if (s.stockA > 0) {
                        s.stockA--
                        s.flash = 1f
                        c.state = CS_EAT
                        c.eatT = 0f
                        setBubble(c, Bub.NONE, 0f)
                        if (c.type == CT.VLOGGER) {
                            vlogT = 30f
                            say("Food vlogger posted a reel! Crowd incoming!")
                        }
                    } else if (c.patience <= 0f) {
                        flipOut(c, i)
                    }
                }
                CS_EAT -> {
                    if (c.wantJhol) {
                        c.jholAnger += dt / JHOL_PATIENCE
                        c.fxT -= dt
                        if (c.jholAnger > 0.55f && c.fxT <= 0f) {
                            c.fxT = 0.25f
                            spawnP(P_RED, c.x, 1.95f, c.z, (rnd() - 0.5f) * 0.6f, 1.2f, (rnd() - 0.5f) * 0.6f, 0.9f, 0.35f, Col.rgb(0xFF3B2F), 0.0)
                        }
                        if (c.jholAnger >= 1f) flipOut(c, i)
                    } else {
                        c.eatT += dt
                        if (c.jholReq > 0 && c.eatT >= c.jholAt) {
                            c.wantJhol = true
                            c.jholAnger = 0f
                            setBubble(c, Bub.JHOL, -1f)
                            if (yellCd <= 0f) {
                                yellCd = 1.2f
                                sfx(Sfx.YELL, 0.9f + rnd() * 0.3f, 0.8f)
                            }
                        }
                        if (c.eatT >= c.eatDur && !c.wantJhol) {
                            payCustomer(c)
                            if (c.type == CT.STUDENT && lvl(Up.NOCAMP) == 0) {
                                c.state = CS_CAMP
                                c.campT = 30f
                                c.chatT = 1.2f
                                c.upsellT = 4f
                            } else {
                                startLeave(c, i, false)
                                setBubble(c, Bub.THANKS, 1.8f)
                            }
                        }
                    }
                }
                CS_CAMP -> {
                    c.campT -= dt
                    c.chatT -= dt
                    c.fxT -= dt
                    if (c.chatT <= 0f) {
                        c.chatT = 3.6f + rnd() * 2f
                        setBubble(c, Bub.CHAT0 + (rnd() * 4f).toInt().coerceIn(0, 3), 2.6f)
                    }
                    if (c.fxT <= 0f && lvl(Up.NOCAMP) == 0) {
                        c.fxT = 0.8f
                        spawnP(P_SMOKE, c.x + 0.3f, 1.5f, c.z - 0.1f, 0.1f, 0.6f, -0.1f, 1.6f, 0.3f, Col.rgb(0xBBBBBB), 0.0)
                    }
                    if (lvl(Up.UPSELL) > 0) {
                        c.upsellT -= dt
                        if (c.upsellT <= 0f) {
                            c.upsellT = 5f
                            val s = st[c.seatSt]
                            val extra = chiyaPrice() * 1.8
                            s.cashAmt += extra
                            s.bills = min(90, s.bills + 1)
                            setBubble(c, Bub.SURYA, 2.2f)
                            sparkBurst(c.x, 1.8f, c.z, 3, Col.rgb(0xFFB02E))
                        }
                    }
                    if (lvl(Up.NOCAMP) > 0 || c.campT <= 0f) {
                        startLeave(c, i, false)
                        setBubble(c, Bub.THANKS, 1.8f)
                    }
                }
                CS_FLIP -> {
                    c.flip += dt
                    c.fxT -= dt
                    if (c.fxT <= 0f) {
                        c.fxT = 0.06f
                        spawnP(P_RED, c.x + (rnd() - 0.5f) * 0.5f, 1.9f, c.z + (rnd() - 0.5f) * 0.5f,
                            (rnd() - 0.5f) * 2f, 1.5f + rnd(), (rnd() - 0.5f) * 2f, 1.0f, 0.5f, Col.rgb(0xFF3B2F), 0.0)
                    }
                    if (c.flip >= 0.9f) c.state = CS_LEAVE
                }
                CS_LEAVE -> {
                    if (c.angry) {
                        c.fxT -= dt
                        if (c.fxT <= 0f) {
                            c.fxT = 0.12f
                            spawnP(P_RED, c.x, 1.9f, c.z, (rnd() - 0.5f) * 0.8f, 1.2f, (rnd() - 0.5f) * 0.8f, 0.9f, 0.4f, Col.rgb(0xFF3B2F), 0.0)
                        }
                    }
                    if (moveAlong(c, dt, if (c.angry) c.speed * 1.7f else c.speed)) c.active = false
                }
            }
        }
    }

    // ---- Staff ---------------------------------------------------------------------
    private fun stationNeedsPlates(s: Station): Int {
        var occ = 0
        if (s.occ0 >= 0) occ++
        if (s.occ1 >= 0) occ++
        val want = min(TABLE_CAP, occ + 1)
        return want - (s.stockA + claimA[s.id])
    }

    private fun stationNeedsCups(s: Station): Int {
        var occ = 0
        if (s.occ0 >= 0) occ++
        if (s.occ1 >= 0) occ++
        val want = min(BENCH_CAP, occ + 1)
        return want - (s.stockA + claimA[s.id])
    }

    private fun chooseTask(s: Staff) {
        val cap = staffCarry()
        when (s.role) {
            Role.KANCHA -> {
                val mok = st[Layout.ID_MOKTU]
                val avail = mok.stockA - claimA[Layout.ID_MOKTU]
                if (avail > 0) {
                    var best = -1
                    var bn = 0
                    for (i in Layout.ID_TABLE0 until Layout.ID_TABLE0 + 6) {
                        val t = st[i]
                        if (!t.unlocked) continue
                        val need = stationNeedsPlates(t)
                        if (need > bn) { bn = need; best = i }
                    }
                    if (best >= 0) {
                        val n = min(min(cap, avail), bn)
                        s.src = Layout.ID_MOKTU; s.dst = best; s.item = Item.PLATE; s.exp = n
                        claimA[Layout.ID_MOKTU] += n
                        claimA[best] += n
                        s.state = 1
                        return
                    }
                }
                val prep = st[Layout.ID_PREP]
                val needM = 6 - (prep.stockA + claimA[Layout.ID_PREP])
                if (needM > 0) {
                    val n = min(cap, needM)
                    s.src = Layout.ID_MAIDA; s.dst = Layout.ID_PREP; s.item = Item.MAIDA; s.exp = n
                    claimA[Layout.ID_PREP] += n
                    s.state = 1
                    return
                }
                val needE = 6 - (prep.stockB + claimB[Layout.ID_PREP])
                if (needE > 0) {
                    val n = min(cap, needE)
                    s.src = Layout.ID_MEAT; s.dst = Layout.ID_PREP; s.item = Item.MEAT; s.exp = n
                    claimB[Layout.ID_PREP] += n
                    s.state = 1
                    return
                }
            }
            Role.JHOL -> {
                val cd = st[Layout.ID_CAULDRON]
                val projected = cd.level + claimA[Layout.ID_CAULDRON] * JAR_UNITS
                if (projected < cauldronCap() - JAR_UNITS * 0.6f) {
                    val n = min(2, cap)
                    s.src = Layout.ID_JHOL; s.dst = Layout.ID_CAULDRON; s.item = Item.JHOL; s.exp = n
                    claimA[Layout.ID_CAULDRON] += n
                    s.state = 1
                    return
                }
            }
            Role.DIDI -> {
                val ket = st[Layout.ID_KETTLE]
                if (!ket.unlocked) return
                val avail = ket.stockA - claimA[Layout.ID_KETTLE]
                if (avail <= 0) return
                var best = -1
                var bn = 0
                for (i in Layout.ID_BENCH0 until Layout.ID_BENCH0 + 4) {
                    val b = st[i]
                    if (!b.unlocked) continue
                    val need = stationNeedsCups(b)
                    if (need > bn) { bn = need; best = i }
                }
                if (best >= 0) {
                    val n = min(min(cap, avail), bn)
                    s.src = Layout.ID_KETTLE; s.dst = best; s.item = Item.CUP; s.exp = n
                    claimA[Layout.ID_KETTLE] += n
                    claimA[best] += n
                    s.state = 1
                }
            }
            Role.DAI -> {
                var best = -1
                var bb = 2
                for (i in 0 until NST) {
                    val t = st[i]
                    if (!t.unlocked || claimB[i] != 0 || t.bills < bb) continue
                    if (t.kind != SK.TABLE && t.kind != SK.BENCH) continue
                    bb = t.bills
                    best = i
                }
                if (best >= 0) {
                    s.src = best; s.dst = -1; s.item = 0; s.exp = 0
                    claimB[best] = 1
                    s.state = 1
                }
            }
        }
    }

    private fun staffGo(s: Staff, tx: Float, tz: Float, dt: Float, spd: Float): Boolean {
        var wx = tx
        var wz = tz
        if (tz > -2.0f && s.z < -2.0f) {
            if (abs(s.x - tx) > 0.3f) { wx = tx; wz = -2.6f }
        } else if (tz < -2.0f && s.z > -2.0f) {
            if (s.z > -2.6f) { wx = s.x; wz = -2.7f }
        }
        val dx = wx - s.x
        val dz = wz - s.z
        val d = sqrt(dx * dx + dz * dz)
        val step = spd * dt
        if (d <= step + 0.02f) {
            s.x = wx
            s.z = wz
            s.moving = false
            return abs(s.x - tx) < 0.05f && abs(s.z - tz) < 0.05f
        }
        s.x += dx / d * step
        s.z += dz / d * step
        s.heading = lerpAngle(s.heading, atan2(dx, dz), min(1f, dt * 10f))
        s.walk += spd * dt * 2.3f
        s.moving = true
        return false
    }

    private fun workX(i: Int): Float = Layout.WX[i]
    private fun workZ(i: Int): Float = Layout.WZ[i]

    private fun updateStaff(dt: Float) {
        val spd = 3.5f * (if (boostT > 0f) 1.3f else 1f)
        for (k in 0 until staffN) {
            val s = staff[k]
            if (!s.active) continue
            when (s.state) {
                0 -> {
                    s.timer -= dt
                    if (s.timer <= 0f) {
                        s.timer = 0.4f
                        chooseTask(s)
                    }
                    if (s.state == 0) {
                        staffGo(s, homeX(s), homeZ(s), dt, spd * 0.6f)
                    }
                }
                1 -> {
                    val si = s.src
                    val tx: Float
                    val tz: Float
                    if (s.role == Role.DAI) { tx = Layout.PX[si]; tz = Layout.PZ[si] + 0.7f }
                    else { tx = Layout.ZX[si]; tz = Layout.ZZ[si] }
                    if (staffGo(s, tx, tz, dt, spd)) {
                        if (s.role == Role.DAI) {
                            val t = st[si]
                            if (t.cashAmt > 0.0) {
                                val amt = t.cashAmt
                                earn(amt)
                                for (n in 0 until min(4, t.bills)) flyItem(-1, Layout.PX[si], 0.6f + n * 0.1f, Layout.PZ[si], s.x, 1.5f, s.z, 0, 0.4f + n * 0.06f)
                                spawnP(P_NUM, Layout.PX[si], 1.6f, Layout.PZ[si], 0f, 0.8f, 0f, 1.3f, 0.5f, Col.rgb(0x6BE36B), amt)
                                t.cashAmt = 0.0
                                t.bills = 0
                                sfx(Sfx.CASH, 1.2f, 0.4f)
                            }
                            claimB[si] = 0
                            s.state = 0
                            s.timer = 0.2f
                        } else {
                            val src = st[si]
                            var got = 0
                            if (src.kind == SK.MOKTU || src.kind == SK.KETTLE) {
                                got = min(s.exp, src.stockA)
                                src.stockA -= got
                                claimA[si] = max(0, claimA[si] - s.exp)
                            } else {
                                got = s.exp
                                if (src.kind == SK.JHOL) claimB[si] = 0
                            }
                            src.flash = 0.8f
                            s.n = got
                            if (got > 0) {
                                flyItem(s.item, src.x, 1.2f, src.z + 0.4f, s.x, 1.4f, s.z, 0, 0.3f)
                                s.state = 2
                            } else {
                                releaseClaims(s)
                                s.state = 0
                            }
                        }
                    }
                }
                2 -> {
                    val di = s.dst
                    if (staffGo(s, workX(di).let { if (st[di].kind == SK.PREP || st[di].kind == SK.CAULDRON) Layout.ZX[di] else it }, if (st[di].kind == SK.PREP || st[di].kind == SK.CAULDRON) Layout.ZZ[di] else workZ(di), dt, spd)) {
                        val d = st[di]
                        var dropped = 0
                        for (n in 0 until s.n) {
                            if (accepts(d, s.item)) {
                                give(d, s.item)
                                dropped++
                            }
                        }
                        if (dropped > 0) {
                            flyItem(s.item, s.x, 1.4f, s.z, d.x, 1.2f, d.z, 0, 0.3f)
                            sfx(Sfx.POP, 1.2f, 0.25f)
                        }
                        releaseClaims(s)
                        s.n = 0
                        s.state = 0
                        s.timer = 0.15f
                    }
                }
            }
        }
    }

    private fun releaseClaims(s: Staff) {
        val d = s.dst
        if (d < 0) return
        if (st[d].kind == SK.PREP) {
            if (s.item == Item.MAIDA) claimA[d] = max(0, claimA[d] - s.exp) else claimB[d] = max(0, claimB[d] - s.exp)
        } else {
            claimA[d] = max(0, claimA[d] - s.exp)
        }
    }

    // ---- Particles ---------------------------------------------------------------------
    fun spawnP(kind: Int, x: Float, y: Float, z: Float, vx: Float, vy: Float, vz: Float, life: Float, size: Float, col: Int, v: Double) {
        if (pN >= PMAX) return
        val i = pN++
        pKind[i] = kind
        pX[i] = x; pY[i] = y; pZ[i] = z
        pVX[i] = vx; pVY[i] = vy; pVZ[i] = vz
        pAge[i] = 0f
        pLife[i] = life
        pSize[i] = size
        pCol[i] = col
        pFlag[i] = 0
        pVal[i] = v
    }

    fun flyItem(item: Int, sx: Float, sy: Float, sz: Float, tx: Float, ty: Float, tz: Float, flags: Int, life: Float) {
        if (pN >= PMAX) return
        val col = if (item < 0) (if (rnd() < 0.5f) Col.rgb(0x4CAF50) else Col.rgb(0xE05A47)) else ITEM_COL[item]
        spawnP(P_FLY, sx, sy, sz, tx, ty, tz, life, 0.3f, col, 0.0)
        pFlag[pN - 1] = flags
    }

    fun sparkBurst(x: Float, y: Float, z: Float, n: Int, col: Int) {
        for (i in 0 until n) {
            spawnP(P_SPARK, x, y, z, (rnd() - 0.5f) * 4f, 2f + rnd() * 3f, (rnd() - 0.5f) * 4f, 0.7f + rnd() * 0.3f, 0.16f + rnd() * 0.1f, col, 0.0)
        }
    }

    private fun copyP(from: Int, to: Int) {
        pKind[to] = pKind[from]
        pX[to] = pX[from]; pY[to] = pY[from]; pZ[to] = pZ[from]
        pVX[to] = pVX[from]; pVY[to] = pVY[from]; pVZ[to] = pVZ[from]
        pAge[to] = pAge[from]; pLife[to] = pLife[from]; pSize[to] = pSize[from]
        pCol[to] = pCol[from]; pFlag[to] = pFlag[from]; pVal[to] = pVal[from]
    }

    private fun updateParticles(dt: Float) {
        var i = 0
        while (i < pN) {
            pAge[i] += dt
            if (pAge[i] >= pLife[i]) {
                pN--
                if (i != pN) copyP(pN, i)
                continue
            }
            when (pKind[i]) {
                P_STEAM, P_RED, P_SMOKE -> {
                    pX[i] += pVX[i] * dt
                    pY[i] += pVY[i] * dt
                    pZ[i] += pVZ[i] * dt
                }
                P_SPARK -> {
                    pVY[i] -= 9f * dt
                    pX[i] += pVX[i] * dt
                    pY[i] += pVY[i] * dt
                    pZ[i] += pVZ[i] * dt
                    if (pY[i] < 0.05f) { pY[i] = 0.05f; pVY[i] = 0f }
                }
                P_NUM -> pY[i] += pVY[i] * dt
            }
            i++
        }
    }

    // ---- HUD -------------------------------------------------------------------------------
    fun hint(): String {
        val cd = st[Layout.ID_CAULDRON]
        val prep = st[Layout.ID_PREP]
        val mok = st[Layout.ID_MOKTU]
        if (jholAlert) return "Grab red jars from the Jhol crate and refill the cauldron!"
        var bills = 0
        for (i in 0 until NST) bills += st[i].bills
        if (bills >= 3) return "Walk over the cash piles to collect your rupees!"
        if (mok.stockA > 0 && carryN == 0) return "Pick up steaming momo plates from the Moktu."
        if (carryN > 0 && carry[carryN - 1] == Item.PLATE) return "Deliver the plates to tables with hungry customers!"
        if (prep.stockA < 2 || prep.stockB < 2) return "Carry Maida and Meat to the Prep table."
        if (cd.level < cauldronCap() * 0.3f) return "Jhol is running low - refill the cauldron soon."
        for (i in 0 until NST) {
            if (!st[i].unlocked) return "Stand on a glowing pad to unlock: " + Layout.NAME[i]
        }
        return "Keep the customers happy!"
    }

    fun buildHud(): Hud {
        val cd = st[Layout.ID_CAULDRON]
        var ready = 0
        for (i in 0 until 3) if (!chDone[i] && chProg[i] >= chTarget[i]) ready++
        return Hud(
            money = money, gems = gems,
            jhol = (cd.level / cauldronCap()).coerceIn(0f, 1f),
            jholAlert = jholAlert, jholLow = jholLow,
            carry = carryN, cap = carryCap(), loc = loc, hint = hint(),
            boost = if (boostT > 0f) boostT.toInt() else 0,
            ready = ready, canPrestige = canPrestige(),
            tick = (time * 8f).toInt(), toast = toast, toastSeq = toastSeq, vip = vipHere
        )
    }
}
