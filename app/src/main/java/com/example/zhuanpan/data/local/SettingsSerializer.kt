package com.example.zhuanpan.data.local

import com.example.zhuanpan.data.model.AppSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 应用设置 JSON 序列化器。
 */
object SettingsSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        coerceInputValues = true
    }

    /**
     * 将 [AppSettings] 序列化为 JSON 字符串。
     */
    fun serialize(settings: AppSettings): String =
        json.encodeToString(settings)

    /**
     * 将 JSON 字符串反序列化为 [AppSettings]，失败时返回默认设置。
     */
    fun deserialize(jsonString: String?): AppSettings =
        try {
            if (jsonString.isNullOrBlank()) {
                AppSettings()
            } else {
                json.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            AppSettings()
        }
}
