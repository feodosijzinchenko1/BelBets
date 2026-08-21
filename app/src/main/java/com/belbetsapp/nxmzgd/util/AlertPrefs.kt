package com.belbetsapp.nxmzgd.util

import android.content.Context

class AlertPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDialogShown(): Boolean =
        prefs.getBoolean(KEY_DIALOG_SHOWN, false)

    fun markDialogShown() {
        prefs.edit().putBoolean(KEY_DIALOG_SHOWN, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "belbets_alert_prefs"
        private const val KEY_DIALOG_SHOWN = "alert_dialog_shown"
    }
}
