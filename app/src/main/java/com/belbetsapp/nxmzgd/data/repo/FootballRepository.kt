package com.belbetsapp.nxmzgd.data.repo

import com.belbetsapp.nxmzgd.data.api.FixtureItem
import com.belbetsapp.nxmzgd.data.api.FootballApi
import com.belbetsapp.nxmzgd.data.api.LeagueInfo
import com.belbetsapp.nxmzgd.data.api.StandingRow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object FootballRepository {

    private val api = FootballApi()
    private val mutex = Mutex()

    private var cachedLive: List<FixtureItem> = emptyList()
    private var cachedLiveAt: Long = 0L

    private val standingsCache = mutableMapOf<Pair<Int, Int>, Pair<Long, List<StandingRow>>>()
    private val dateCache = mutableMapOf<String, Pair<Long, List<FixtureItem>>>()

    private const val LIVE_TTL_MS = 60_000L
    private const val STANDINGS_TTL_MS = 12 * 60 * 60_000L
    private const val DATE_TTL_MS = 5 * 60_000L

    suspend fun getLiveFixtures(forceRefresh: Boolean = false): List<FixtureItem> = mutex.withLock {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedLive.isNotEmpty() && now - cachedLiveAt < LIVE_TTL_MS) {
            return@withLock cachedLive
        }
        val fresh = api.fetchLiveFixtures()
        if (fresh.isNotEmpty() || forceRefresh) {
            cachedLive = fresh
            cachedLiveAt = now
        }
        fresh
    }

    suspend fun getFixturesByDate(date: String, forceRefresh: Boolean = false): List<FixtureItem> = mutex.withLock {
        val now = System.currentTimeMillis()
        val cached = dateCache[date]
        if (!forceRefresh && cached != null && now - cached.first < DATE_TTL_MS) {
            return@withLock cached.second
        }
        val fresh = api.fetchFixturesByDate(date)
        if (fresh.isNotEmpty() || forceRefresh) {
            dateCache[date] = now to fresh
        }
        fresh
    }

    suspend fun getStandings(leagueId: Int, season: Int, forceRefresh: Boolean = false): List<StandingRow> = mutex.withLock {
        val now = System.currentTimeMillis()
        val key = leagueId to season
        val cached = standingsCache[key]
        if (!forceRefresh && cached != null && now - cached.first < STANDINGS_TTL_MS) {
            return@withLock cached.second
        }
        val fresh = api.fetchStandings(leagueId, season)
        if (fresh.isNotEmpty() || forceRefresh) {
            standingsCache[key] = now to fresh
        }
        fresh
    }

    val featuredLeagues: List<LeagueInfo> get() = FootballApi.FEATURED_LEAGUES
}
