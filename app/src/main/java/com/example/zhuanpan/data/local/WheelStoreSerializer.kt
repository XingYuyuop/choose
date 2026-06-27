package com.example.zhuanpan.data.local

import com.example.zhuanpan.data.model.WheelStore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 转盘集合 JSON 序列化器。
 */
object WheelStoreSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    /**
     * 将 [WheelStore] 序列化为 JSON 字符串。
     */
    fun serialize(store: WheelStore): String =
        json.encodeToString(store)

    /**
     * 将 JSON 字符串反序列化为 [WheelStore]，失败时返回空集合。
     */
    fun deserialize(jsonString: String?): WheelStore =
        try {
            if (jsonString.isNullOrBlank()) {
                WheelStore()
            } else {
                json.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            WheelStore()
        }
}
