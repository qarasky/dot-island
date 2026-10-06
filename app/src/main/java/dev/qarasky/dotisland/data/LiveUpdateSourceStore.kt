/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.data

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.qarasky.dotisland.util.PersonalActivityPolicy
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.liveUpdateSources by preferencesDataStore(
    name = "dot_island_live_update_sources",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

/** Notification-source opt-in, separate from permission to use the publishing API. */
@Singleton
class LiveUpdateSourceStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val allowedKey = stringSetPreferencesKey("allowed_packages")
    val allowedPackages = context.liveUpdateSources.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { liveUpdateSourceDefaults(it[allowedKey]) }

    suspend fun setAllowed(packageName: String, allowed: Boolean) {
        require(isLiveUpdatePackageName(packageName))
        context.liveUpdateSources.edit { prefs ->
            val current = liveUpdateSourceDefaults(prefs[allowedKey])
            prefs[allowedKey] = if (allowed) current + packageName else current - packageName
        }
    }
}

internal fun liveUpdateSourceDefaults(stored: Set<String>?): Set<String> =
    stored ?: setOf(PersonalActivityPolicy.DODO)

internal fun isLiveUpdatePackageName(value: String): Boolean =
    value.length <= 200 && Regex("[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)+").matches(value)
