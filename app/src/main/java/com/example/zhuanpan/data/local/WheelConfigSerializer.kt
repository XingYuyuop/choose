package com.example.zhuanpan.data.local

import com.example.zhuanpan.data.model.WheelConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 转盘配置 JSON 序列化器。
 */
object WheelConfigSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    /**
     * 将 [WheelConfig] 序列化为 JSON 字符串。
     */
    fun serialize(config: WheelConfig): String =
        json.encodeToString(config)

    /**
     * 将 JSON 字符串反序列化为 [WheelConfig]，失败时返回默认配置。
     */
    fun deserialize(jsonString: String?): WheelConfig =
        try {
            if (jsonString.isNullOrBlank()) {
                WheelConfig()
            } else {
                json.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            WheelConfig()
        }
}
