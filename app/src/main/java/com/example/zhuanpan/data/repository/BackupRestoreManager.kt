package com.example.zhuanpan.data.repository

import com.example.zhuanpan.data.local.ZhuanpanDataStore
import com.example.zhuanpan.data.model.BackupData
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 备份还原管理器，负责导出和导入全部用户数据。
 *
 * @property dataStore DataStore 封装实例
 */
class BackupRestoreManager(
    private val dataStore: ZhuanpanDataStore
) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * 导出全部数据为备份 JSON 字符串。
     */
    suspend fun export(): String {
        val backup = BackupData(
            wheelsStoreJson = dataStore.wheelsStoreJson.first() ?: "",
            appSettingsJson = dataStore.settingsJson.first() ?: "",
            spinHistoryJson = dataStore.spinHistoryJson.first() ?: ""
        )
        return json.encodeToString(backup)
    }

    /**
     * 从备份 JSON 字符串导入数据，覆盖当前数据。
     *
     * @return 导入成功返回 true，解析失败返回 false
     */
    suspend fun import(jsonString: String): Boolean {
        return try {
            val backup = json.decodeFromString<BackupData>(jsonString)
            if (backup.wheelsStoreJson.isNotBlank()) {
                dataStore.saveWheelsStoreJson(backup.wheelsStoreJson)
            }
            if (backup.appSettingsJson.isNotBlank()) {
                dataStore.saveSettingsJson(backup.appSettingsJson)
            }
            if (backup.spinHistoryJson.isNotBlank()) {
                dataStore.saveSpinHistoryJson(backup.spinHistoryJson)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        /**
         * 生成备份文件名：选择-年月日时分秒
         */
        fun generateFileName(timeMillis: Long = System.currentTimeMillis()): String {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = timeMillis
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)
            val second = calendar.get(java.util.Calendar.SECOND)
            return "选择-${year}年${month}月${day}日 ${hour}时${minute}分${second}秒.json"
        }
    }
}
