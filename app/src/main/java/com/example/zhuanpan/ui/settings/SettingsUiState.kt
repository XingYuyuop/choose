package com.example.zhuanpan.ui.settings

import com.example.zhuanpan.data.model.AppSettings

/**
 * 设置面板 UI 状态。
 *
 * @property settings 当前应用设置
 */
data class SettingsUiState(
    val settings: AppSettings = AppSettings()
)
