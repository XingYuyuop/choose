package com.example.zhuanpan.data.repository

import com.example.zhuanpan.data.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * 应用设置仓库接口。
 */
interface SettingsRepository {

    /**
     * 当前应用设置数据流。
     */
    val settings: Flow<AppSettings>

    /**
     * 更新应用设置。
     *
     * @param transform 基于当前设置生成新设置的变换函数
     */
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)

    /**
     * 直接保存设置。
     */
    suspend fun saveSettings(settings: AppSettings)
}
