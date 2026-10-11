/*
 * Dot Island (2026)
 * Copyright Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 */

package dev.qarasky.dotisland.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import dev.qarasky.dotisland.service.DotIslandNotificationListenerService
import dev.qarasky.dotisland.service.DotIslandOverlayService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock

/**
 * Reconnects system-managed services after the package leaves Android's force-stopped state.
 *
 * NotificationListenerService provides an official rebind API. AccessibilityService does not,
 * so an enabled-state package-change signal is only a best-effort refresh, not a
 * guaranteed rebind. Never disable the component: on the A069 that revokes its grant.
 * Never edit ENABLED_ACCESSIBILITY_SERVICES or any other app's access grants.
 */
object SystemServiceRecovery {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastRestartMs = 0L

    fun requestRecovery(context: Context, retry: Boolean = false) {
        val appContext = context.applicationContext
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { requestRecovery(appContext, retry) }
            return
        }
        runCatchingLogged(TAG, "System-service recovery failed") {
            requestNotificationListenerRebind(appContext)
            val now = SystemClock.elapsedRealtime()
            if (shouldRestartAccessibility(DotIslandOverlayService.isSystemConnected,
                    isAccessibilityPermissionGranted(appContext), now, lastRestartMs, retry)) {
                lastRestartMs = now
                refreshAccessibilityComponent(appContext)
            }
        }
    }

    private fun requestNotificationListenerRebind(context: Context) {
        if (!isNotificationListenerPermissionGranted(context) ||
            DotIslandNotificationListenerService.isSystemConnected
        ) return

        runCatchingLogged(TAG, "Notification-listener rebind failed") {
            NotificationListenerService.requestRebind(
                ComponentName(context, DotIslandNotificationListenerService::class.java)
            )
        }
    }

    private fun refreshAccessibilityComponent(context: Context) {
        runCatchingLogged(TAG, "Accessibility component refresh failed") {
            val component = ComponentName(context, DotIslandOverlayService::class.java)
            val packageManager = context.packageManager
            val currentState = packageManager.getComponentEnabledSetting(component)
            val refreshedState = when (currentState) {
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                else -> return@runCatchingLogged
            }
            packageManager.setComponentEnabledSetting(
                component,
                refreshedState,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    private fun isAccessibilityPermissionGranted(context: Context): Boolean {
        val expected = ComponentName(context, DotIslandOverlayService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(':').any {
            ComponentName.unflattenFromString(it) == expected
        }
    }

    private fun isNotificationListenerPermissionGranted(context: Context): Boolean {
        val expected = ComponentName(context, DotIslandNotificationListenerService::class.java)
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabledListeners.split(':').any {
            ComponentName.unflattenFromString(it) == expected
        }
    }

    private const val TAG = "SystemServiceRecovery"
}

internal fun shouldRestartAccessibility(connected: Boolean, granted: Boolean, now: Long,
    previous: Long, retry: Boolean = false): Boolean = !connected && granted &&
    (retry || previous == 0L || now - previous >= 10_000L)
