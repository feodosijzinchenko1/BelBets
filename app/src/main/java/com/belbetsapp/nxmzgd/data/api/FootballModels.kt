package com.belbetsapp.nxmzgd.data.api

data class FixtureItem(
    val id: Long,
    val date: String,
    val status: String,
    val statusShort: String,
    val elapsed: Int?,
    val leagueId: Int,
    val leagueName: String,
    val leagueLogo: String?,
    val country: String,
    val round: String?,
    val homeId: Int,
    val homeName: String,
    val homeLogo: String?,
    val awayId: Int,
    val awayName: String,
    val awayLogo: String?,
    val homeGoals: Int?,
    val awayGoals: Int?
) {
    val isLive: Boolean get() = statusShort in liveStatuses
    val isFinished: Boolean get() = statusShort in finishedStatuses
    val isUpcoming: Boolean get() = statusShort in upcomingStatuses

    companion object {
        val liveStatuses = setOf("1H", "2H", "HT", "ET", "BT", "P", "LIVE", "INT")
        val finishedStatuses = setOf("FT", "AET", "PEN")
        val upcomingStatuses = setOf("TBD", "NS")
    }
}

data class StandingRow(
    val rank: Int,
    val teamId: Int,
    val teamName: String,
    val teamLogo: String?,
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int,
    val form: String?
)

data class LeagueInfo(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String?,
    val season: Int
)
