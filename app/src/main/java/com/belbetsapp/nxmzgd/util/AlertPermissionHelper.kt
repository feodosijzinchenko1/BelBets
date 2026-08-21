package com.belbetsapp.nxmzgd.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.belbetsapp.nxmzgd.R

object AlertPermissionHelper {

    fun showPrePermissionDialog(
        activity: Activity,
        requestNotificationPermission: () -> Unit
    ) {
        val dialog = AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.push_dialog_title))
            .setMessage(activity.getString(R.string.push_dialog_message))
            .setPositiveButton(activity.getString(R.string.push_dialog_enable)) { _, _ ->
                handlePermissionFlow(activity, requestNotificationPermission)
            }
            .setNegativeButton(activity.getString(R.string.push_dialog_later), null)
            .create()

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun handlePermissionFlow(
        activity: Activity,
        requestNotificationPermission: () -> Unit
    ) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openNotificationSettings(activity)
                } else {
                    requestNotificationPermission()
                }
            }
            else -> {
                openNotificationSettings(activity)
            }
        }
    }

    fun onPermissionResult(activity: Activity) {
        openNotificationSettings(activity)
    }

    private fun openNotificationSettings(activity: Activity) {
        try {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                    }
                    else -> {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.parse("package:${activity.packageName}")
                    }
                }
            }
            activity.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        }
    }
}
