package com.example.zhuanpan.data.local

import com.example.zhuanpan.data.model.SpinHistory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 历史记录 JSON 序列化器。
 */
object SpinHistorySerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    /**
     * 将 [SpinHistory] 序列化为 JSON 字符串。
     */
    fun serialize(history: SpinHistory): String =
        json.encodeToString(history)

    /**
     * 将 JSON 字符串反序列化为 [SpinHistory]，失败时返回空历史。
     */
    fun deserialize(jsonString: String?): SpinHistory =
        try {
            if (jsonString.isNullOrBlank()) {
                SpinHistory()
            } else {
                json.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            SpinHistory()
        }
}
