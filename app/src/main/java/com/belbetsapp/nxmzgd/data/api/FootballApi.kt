package com.belbetsapp.nxmzgd.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FootballApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .cache(null)
        .build()

    private fun request(path: String): JSONObject? {
        return try {
            val req = Request.Builder()
                .url(BASE_URL + path)
                .cacheControl(CacheControl.Builder().noCache().noStore().build())
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("x-apisports-key", API_KEY)
                .get()
                .build()
            val resp = client.newCall(req).execute()
            val body = resp.body?.string()
            resp.close()
            if (resp.isSuccessful && !body.isNullOrBlank()) JSONObject(body) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchLiveFixtures(): List<FixtureItem> = withContext(Dispatchers.IO) {
        val json = request("/fixtures?live=all") ?: return@withContext emptyList()
        parseFixtures(json)
    }

    suspend fun fetchFixturesByDate(date: String): List<FixtureItem> = withContext(Dispatchers.IO) {
        val json = request("/fixtures?date=$date") ?: return@withContext emptyList()
        parseFixtures(json)
    }

    suspend fun fetchStandings(leagueId: Int, season: Int): List<StandingRow> = withContext(Dispatchers.IO) {
        val json = request("/standings?league=$leagueId&season=$season") ?: return@withContext emptyList()
        parseStandings(json)
    }

    private fun parseFixtures(json: JSONObject): List<FixtureItem> {
        val response = json.optJSONArray("response") ?: return emptyList()
        val out = mutableListOf<FixtureItem>()
        for (i in 0 until response.length()) {
            val item = response.optJSONObject(i) ?: continue
            val fixture = item.optJSONObject("fixture") ?: continue
            val league = item.optJSONObject("league") ?: continue
            val teams = item.optJSONObject("teams") ?: continue
            val goals = item.optJSONObject("goals")
            val status = fixture.optJSONObject("status")
            val home = teams.optJSONObject("home") ?: continue
            val away = teams.optJSONObject("away") ?: continue
            out += FixtureItem(
                id = fixture.optLong("id"),
                date = fixture.optString("date"),
                status = status?.optString("long").orEmpty(),
                statusShort = status?.optString("short").orEmpty(),
                elapsed = status?.optInt("elapsed", -1)?.takeIf { it >= 0 },
                leagueId = league.optInt("id"),
                leagueName = league.optString("name"),
                leagueLogo = league.optString("logo").takeIf { it.isNotBlank() && it != "null" },
                country = league.optString("country"),
                round = league.optString("round").takeIf { it.isNotBlank() && it != "null" },
                homeId = home.optInt("id"),
                homeName = home.optString("name"),
                homeLogo = home.optString("logo").takeIf { it.isNotBlank() && it != "null" },
                awayId = away.optInt("id"),
                awayName = away.optString("name"),
                awayLogo = away.optString("logo").takeIf { it.isNotBlank() && it != "null" },
                homeGoals = goals?.optInt("home", -1)?.takeIf { it >= 0 },
                awayGoals = goals?.optInt("away", -1)?.takeIf { it >= 0 }
            )
        }
        return out
    }

    private fun parseStandings(json: JSONObject): List<StandingRow> {
        val response = json.optJSONArray("response") ?: return emptyList()
        if (response.length() == 0) return emptyList()
        val league = response.optJSONObject(0)?.optJSONObject("league") ?: return emptyList()
        val standingsArr = league.optJSONArray("standings") ?: return emptyList()
        if (standingsArr.length() == 0) return emptyList()
        val rows = standingsArr.optJSONArray(0) ?: return emptyList()
        val out = mutableListOf<StandingRow>()
        for (i in 0 until rows.length()) {
            val r = rows.optJSONObject(i) ?: continue
            val team = r.optJSONObject("team") ?: continue
            val all = r.optJSONObject("all")
            val goals = all?.optJSONObject("goals")
            out += StandingRow(
                rank = r.optInt("rank"),
                teamId = team.optInt("id"),
                teamName = team.optString("name"),
                teamLogo = team.optString("logo").takeIf { it.isNotBlank() && it != "null" },
                played = all?.optInt("played") ?: 0,
                win = all?.optInt("win") ?: 0,
                draw = all?.optInt("draw") ?: 0,
                lose = all?.optInt("lose") ?: 0,
                goalsFor = goals?.optInt("for") ?: 0,
                goalsAgainst = goals?.optInt("against") ?: 0,
                points = r.optInt("points"),
                form = r.optString("form").takeIf { it.isNotBlank() && it != "null" }
            )
        }
        return out
    }

    companion object {
        private const val BASE_URL = "https://sghfdva.top/api-belbets/"
        const val API_KEY = ""

        val FEATURED_LEAGUES = listOf(
            LeagueInfo(39, "Premier League", "England", "https://media.api-sports.io/football/leagues/39.png", currentSeason()),
            LeagueInfo(140, "La Liga", "Spain", "https://media.api-sports.io/football/leagues/140.png", currentSeason()),
            LeagueInfo(135, "Serie A", "Italy", "https://media.api-sports.io/football/leagues/135.png", currentSeason()),
            LeagueInfo(78, "Bundesliga", "Germany", "https://media.api-sports.io/football/leagues/78.png", currentSeason()),
            LeagueInfo(61, "Ligue 1", "France", "https://media.api-sports.io/football/leagues/61.png", currentSeason()),
            LeagueInfo(2, "UEFA Champions League", "World", "https://media.api-sports.io/football/leagues/2.png", currentSeason()),
            LeagueInfo(3, "UEFA Europa League", "World", "https://media.api-sports.io/football/leagues/3.png", currentSeason()),
            LeagueInfo(88, "Eredivisie", "Netherlands", "https://media.api-sports.io/football/leagues/88.png", currentSeason()),
            LeagueInfo(94, "Primeira Liga", "Portugal", "https://media.api-sports.io/football/leagues/94.png", currentSeason()),
            LeagueInfo(203, "Süper Lig", "Turkey", "https://media.api-sports.io/football/leagues/203.png", currentSeason())
        )

        private fun currentSeason(): Int {
            val cal = java.util.Calendar.getInstance()
            val y = cal.get(java.util.Calendar.YEAR)
            val m = cal.get(java.util.Calendar.MONTH) + 1
            return if (m >= 8) y else y - 1
        }
    }
}
