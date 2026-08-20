package com.belbetsapp.nxmzgd.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.appStorage: DataStore<Preferences> by preferencesDataStore(name = "belbets_storage")

class AppStorage(private val context: Context) {

    private val ds get() = context.appStorage

    val usernameFlow: Flow<String> = ds.data.map { it[USERNAME_KEY] ?: "BelBets Fan" }
    val playgroundBalanceFlow: Flow<Int> = ds.data.map { it[BALANCE_KEY] ?: DEFAULT_BALANCE }
    val playgroundHistoryFlow: Flow<String> = ds.data.map { it[HISTORY_KEY] ?: "" }

    suspend fun saveUsername(name: String) = ds.edit { it[USERNAME_KEY] = name }
    suspend fun savePlaygroundBalance(balance: Int) = ds.edit { it[BALANCE_KEY] = balance }
    suspend fun savePlaygroundHistory(serialized: String) = ds.edit { it[HISTORY_KEY] = serialized }

    suspend fun resetPlayground() = ds.edit {
        it[BALANCE_KEY] = DEFAULT_BALANCE
        it[HISTORY_KEY] = ""
    }

    suspend fun getAccessToken(): String? = ds.data.first()[TOKEN_KEY]
    suspend fun saveAccessToken(value: String) = ds.edit { it[TOKEN_KEY] = value }

    suspend fun getRemoteDestination(): String? = ds.data.first()[DESTINATION_KEY]
    suspend fun saveRemoteDestination(value: String) = ds.edit { it[DESTINATION_KEY] = value }

    companion object {
        const val DEFAULT_BALANCE = 1000
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val BALANCE_KEY = intPreferencesKey("playground_balance")
        private val HISTORY_KEY = stringPreferencesKey("playground_history")
        private val TOKEN_KEY = stringPreferencesKey("access_token")
        private val DESTINATION_KEY = stringPreferencesKey("remote_destination")
    }
}
