package dev.fslab.comunicacao.escolar.ui.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    private val dataStore = application.dataStore

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = dataStore.data
                .map { prefs -> prefs[THEME_KEY] }
                .first()
            _themeMode.value = when (saved) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
    }

    fun setThemeMode(newMode: ThemeMode) {
        _themeMode.value = newMode
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[THEME_KEY] = newMode.name
            }
        }
    }
}
