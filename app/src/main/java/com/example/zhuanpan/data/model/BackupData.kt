package com.example.zhuanpan.data.model

import kotlinx.serialization.Serializable

/**
 * 备份数据模型，包含应用全部用户数据。
 *
 * @property wheelsStoreJson 转盘集合 JSON
 * @property appSettingsJson 应用设置 JSON
 * @property spinHistoryJson 历史记录 JSON
 * @property backupTime 备份时间戳（毫秒）
 */
@Serializable
data class BackupData(
    val wheelsStoreJson: String = "",
    val appSettingsJson: String = "",
    val spinHistoryJson: String = "",
    val backupTime: Long = System.currentTimeMillis()
)
