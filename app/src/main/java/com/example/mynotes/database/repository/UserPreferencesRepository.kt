package com.example.mynotes.database.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mynotes.database.model.UserPreferences
import com.example.mynotes.database.repository.UserPreferencesRepository.PreferencesKeys.LAYOUT_MODE
import com.example.mynotes.ui.enums.LayoutMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val LAYOUT_MODE = stringPreferencesKey("layout_mode")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
        val layoutMode = preferences[LAYOUT_MODE] ?: LayoutMode.STAGGERED_GRID_LAYOUT.name
        UserPreferences(layoutMode)
    }

    suspend fun updateLayoutMode(layoutMode: LayoutMode) {
        dataStore.edit { preferences ->
            preferences[LAYOUT_MODE] = layoutMode.name
        }
    }
}