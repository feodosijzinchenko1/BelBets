package com.belbetsapp.nxmzgd.presentation.features.playground

import org.json.JSONArray
import org.json.JSONObject

enum class WagerPick { HOME, DRAW, AWAY }

enum class WagerStatus { PENDING, WON, LOST, REFUND }

data class WagerRecord(
    val id: String,
    val fixtureId: Long,
    val homeName: String,
    val awayName: String,
    val leagueName: String,
    val pick: WagerPick,
    val odds: Double,
    val stake: Int,
    val placedAtMs: Long,
    val status: WagerStatus,
    val finalHome: Int? = null,
    val finalAway: Int? = null
) {
    val potentialReturn: Int get() = (stake * odds).toInt()
}

object WagerSerializer {

    fun encode(records: List<WagerRecord>): String {
        val arr = JSONArray()
        records.forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("fid", r.fixtureId)
                put("home", r.homeName)
                put("away", r.awayName)
                put("league", r.leagueName)
                put("pick", r.pick.name)
                put("odds", r.odds)
                put("stake", r.stake)
                put("placed", r.placedAtMs)
                put("status", r.status.name)
                r.finalHome?.let { put("fh", it) }
                r.finalAway?.let { put("fa", it) }
            })
        }
        return arr.toString()
    }

    fun decode(text: String): List<WagerRecord> {
        if (text.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(text)
            val out = mutableListOf<WagerRecord>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                out += WagerRecord(
                    id = o.optString("id"),
                    fixtureId = o.optLong("fid"),
                    homeName = o.optString("home"),
                    awayName = o.optString("away"),
                    leagueName = o.optString("league"),
                    pick = runCatching { WagerPick.valueOf(o.optString("pick")) }.getOrDefault(WagerPick.HOME),
                    odds = o.optDouble("odds", 2.0),
                    stake = o.optInt("stake", 0),
                    placedAtMs = o.optLong("placed", 0L),
                    status = runCatching { WagerStatus.valueOf(o.optString("status")) }.getOrDefault(WagerStatus.PENDING),
                    finalHome = if (o.has("fh")) o.optInt("fh") else null,
                    finalAway = if (o.has("fa")) o.optInt("fa") else null
                )
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }
}

fun syntheticOdds(fixtureId: Long): Triple<Double, Double, Double> {
    val seed = (fixtureId * 2654435761L) and 0x7fffffffL
    val h = 1.40 + (seed % 350) / 100.0
    val a = 1.50 + ((seed shr 8) % 380) / 100.0
    val d = 2.80 + ((seed shr 16) % 200) / 100.0
    return Triple(round2(h), round2(d), round2(a))
}

private fun round2(v: Double) = kotlin.math.round(v * 100) / 100.0
