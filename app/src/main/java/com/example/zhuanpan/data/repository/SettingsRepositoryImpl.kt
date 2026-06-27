package com.example.zhuanpan.data.repository

import com.example.zhuanpan.data.local.SettingsSerializer
import com.example.zhuanpan.data.local.ZhuanpanDataStore
import com.example.zhuanpan.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 应用设置仓库实现。
 *
 * @property dataStore DataStore 封装实例
 */
class SettingsRepositoryImpl(
    private val dataStore: ZhuanpanDataStore
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.settingsJson
        .map { json -> SettingsSerializer.deserialize(json) }

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = settings.first()
        saveSettings(transform(current))
    }

    override suspend fun saveSettings(settings: AppSettings) {
        dataStore.saveSettingsJson(SettingsSerializer.serialize(settings))
    }
}
