package com.tapari.tycoon

import android.content.Context

/** SharedPreferences based save / load with offline earnings. */
object Persist {
    private const val PREFS = "tapari_save_v1"

    private fun csvD(a: DoubleArray): String {
        val sb = StringBuilder()
        for (i in a.indices) {
            if (i > 0) sb.append(',')
            sb.append(a[i])
        }
        return sb.toString()
    }

    private fun csvI(a: IntArray): String {
        val sb = StringBuilder()
        for (i in a.indices) {
            if (i > 0) sb.append(',')
            sb.append(a[i])
        }
        return sb.toString()
    }

    private fun readD(s: String?, out: DoubleArray) {
        if (s.isNullOrEmpty()) return
        val p = s.split(',')
        for (i in out.indices) if (i < p.size) out[i] = p[i].toDoubleOrNull() ?: 0.0
    }

    private fun readI(s: String?, out: IntArray) {
        if (s.isNullOrEmpty()) return
        val p = s.split(',')
        for (i in out.indices) if (i < p.size) out[i] = p[i].toIntOrNull() ?: 0
    }

    fun hasSave(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains("savedAt")

    fun wipe(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().commit()
    }

    fun save(ctx: Context, g: Game) {
        synchronized(g.lock) {
            val e = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            e.putString("money", g.money.toString())
            e.putInt("gems", g.gems)
            e.putString("lifetime", g.lifetime.toString())
            e.putString("runEarned", g.runEarned.toString())
            e.putInt("loc", g.loc)
            e.putInt("locMask", g.locMask)
            for (l in 0 until Locs.COUNT) {
                e.putString("upg$l", csvI(g.upg[l]))
                e.putInt("pad$l", g.padMask[l])
                e.putString("paid$l", csvD(g.padPaid[l]))
            }
            e.putString("ema", g.incomeEma.toString())
            e.putBoolean("sound", g.soundOn)
            e.putBoolean("haptic", g.hapticOn)
            e.putLong("plates", g.platesServed)
            e.putLong("chiya", g.chiyaServed)
            e.putLong("vip", g.vipServed)
            e.putLong("angry", g.angryLeft)
            e.putInt("dayKey", g.dayKey)
            e.putString("chType", csvI(g.chType))
            e.putString("chTarget", csvD(g.chTarget))
            e.putString("chProg", csvD(g.chProg))
            e.putString("chReward", csvI(g.chReward))
            var done = 0
            for (i in 0 until 3) if (g.chDone[i]) done = done or (1 shl i)
            e.putInt("chDone", done)
            e.putInt("bestCombo", g.bestCombo)
            e.putInt("goldCount", g.goldCount)
            e.putInt("dashCount", g.dashCount)
            e.putInt("dogsShooed", g.dogsShooed)
            e.putInt("rebirths", g.rebirths)
            e.putInt("jars", g.jarsFilled)
            e.putInt("achClaimed", g.achClaimed)
            e.putLong("nextSpin", g.nextSpinAt)
            e.putInt("streak", g.streak)
            e.putInt("lastClaimDay", g.lastClaimDay)
            e.putLong("savedAt", System.currentTimeMillis())
            e.putString("offlineEarn", g.offlineEarn.toString())
            e.putLong("offlineSecs", g.offlineSecs)
            e.apply()
        }
    }

    fun load(ctx: Context, g: Game) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!p.contains("savedAt")) return
        synchronized(g.lock) {
            g.money = p.getString("money", "0")?.toDoubleOrNull() ?: 0.0
            g.gems = p.getInt("gems", 0)
            g.lifetime = p.getString("lifetime", "0")?.toDoubleOrNull() ?: 0.0
            g.runEarned = p.getString("runEarned", "0")?.toDoubleOrNull() ?: 0.0
            g.locMask = p.getInt("locMask", 1) or 1
            for (l in 0 until Locs.COUNT) {
                readI(p.getString("upg$l", null), g.upg[l])
                g.padMask[l] = p.getInt("pad$l", Layout.INITIAL_MASK) or Layout.INITIAL_MASK
                if ((g.padMask[l] and 0x1FE00) != 0) g.padMask[l] = g.padMask[l] or (1 shl Layout.ID_BUILDING)
                readD(p.getString("paid$l", null), g.padPaid[l])
            }
            g.incomeEma = p.getString("ema", "0")?.toDoubleOrNull() ?: 0.0
            g.soundOn = p.getBoolean("sound", true)
            g.hapticOn = p.getBoolean("haptic", true)
            g.platesServed = p.getLong("plates", 0L)
            g.chiyaServed = p.getLong("chiya", 0L)
            g.vipServed = p.getLong("vip", 0L)
            g.angryLeft = p.getLong("angry", 0L)
            g.dayKey = p.getInt("dayKey", 0)
            readI(p.getString("chType", null), g.chType)
            readD(p.getString("chTarget", null), g.chTarget)
            readD(p.getString("chProg", null), g.chProg)
            readI(p.getString("chReward", null), g.chReward)
            val done = p.getInt("chDone", 0)
            for (i in 0 until 3) g.chDone[i] = (done shr i) and 1 == 1
            g.bestCombo = p.getInt("bestCombo", 0)
            g.goldCount = p.getInt("goldCount", 0)
            g.dashCount = p.getInt("dashCount", 0)
            g.dogsShooed = p.getInt("dogsShooed", 0)
            g.rebirths = p.getInt("rebirths", 0)
            g.jarsFilled = p.getInt("jars", 0)
            g.achClaimed = p.getInt("achClaimed", 0)
            g.nextSpinAt = p.getLong("nextSpin", 0L)
            g.streak = p.getInt("streak", 0)
            g.lastClaimDay = p.getInt("lastClaimDay", 0)
            val loc = p.getInt("loc", 0).coerceIn(0, Locs.COUNT - 1)
            g.enterLocation(if (((g.locMask shr loc) and 1) == 1) loc else 0)
            g.ensureDay()
            val prevEarn = p.getString("offlineEarn", "0")?.toDoubleOrNull() ?: 0.0
            val savedAt = p.getLong("savedAt", System.currentTimeMillis())
            val secs = (System.currentTimeMillis() - savedAt) / 1000L
            g.offlineEarn = prevEarn
            g.applyOffline(secs)
            if (prevEarn > 0.0 && g.offlineEarn < prevEarn) g.offlineEarn = prevEarn
        }
    }
}
