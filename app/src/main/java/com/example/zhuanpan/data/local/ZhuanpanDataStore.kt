package com.example.zhuanpan.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore 封装类，负责转盘配置与应用设置的键值管理。
 *
 * @property dataStore Preferences DataStore 实例
 */
class ZhuanpanDataStore(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val WHEEL_CONFIG = stringPreferencesKey("wheel_config_json")
        val WHEELS_STORE = stringPreferencesKey("wheels_store_json")
        val APP_SETTINGS = stringPreferencesKey("app_settings_json")
        val SPIN_HISTORY = stringPreferencesKey("spin_history_json")
    }

    /**
     * 转盘配置 JSON 数据流（旧版单转盘，仅用于迁移）。
     */
    val wheelConfigJson: Flow<String?> = dataStore.data
        .map { preferences -> preferences[Keys.WHEEL_CONFIG] }

    /**
     * 转盘集合 JSON 数据流（多转盘单一数据源）。
     */
    val wheelsStoreJson: Flow<String?> = dataStore.data
        .map { preferences -> preferences[Keys.WHEELS_STORE] }

    /**
     * 应用设置 JSON 数据流。
     */
    val settingsJson: Flow<String?> = dataStore.data
        .map { preferences -> preferences[Keys.APP_SETTINGS] }

    /**
     * 历史记录 JSON 数据流。
     */
    val spinHistoryJson: Flow<String?> = dataStore.data
        .map { preferences -> preferences[Keys.SPIN_HISTORY] }

    /**
     * 保存转盘配置 JSON。
     */
    suspend fun saveWheelConfigJson(json: String) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                set(Keys.WHEEL_CONFIG, json)
            }
        }
    }

    /**
     * 保存转盘集合 JSON。
     */
    suspend fun saveWheelsStoreJson(json: String) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                set(Keys.WHEELS_STORE, json)
            }
        }
    }

    /**
     * 保存应用设置 JSON。
     */
    suspend fun saveSettingsJson(json: String) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                set(Keys.APP_SETTINGS, json)
            }
        }
    }

    /**
     * 保存历史记录 JSON。
     */
    suspend fun saveSpinHistoryJson(json: String) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                set(Keys.SPIN_HISTORY, json)
            }
        }
    }
}
