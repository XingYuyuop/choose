package com.example.zhuanpan.data.repository

import com.example.zhuanpan.data.local.WheelConfigSerializer
import com.example.zhuanpan.data.local.WheelStoreSerializer
import com.example.zhuanpan.data.local.ZhuanpanDataStore
import com.example.zhuanpan.data.model.WheelConfig
import com.example.zhuanpan.data.model.WheelStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * 转盘配置仓库实现。
 *
 * 以 [WheelStore] 作为多转盘管理的单一数据源，提供转盘集合的增删改查与切换，
 * 并在首次使用时从旧版单转盘数据迁移，确保历史数据不丢失。
 *
 * @property dataStore DataStore 封装实例
 */
class WheelRepositoryImpl(
    private val dataStore: ZhuanpanDataStore
) : WheelRepository {

    private val store: Flow<WheelStore> = dataStore.wheelsStoreJson
        .map { json -> WheelStoreSerializer.deserialize(json) }

    override val wheels: Flow<List<WheelConfig>> = store.map { it.wheels }

    override val wheelConfig: Flow<WheelConfig> = store.map { s ->
        s.wheels.find { it.id == s.currentWheelId } ?: s.wheels.firstOrNull() ?: WheelConfig()
    }

    override suspend fun ensureInitialized() {
        val current = WheelStoreSerializer.deserialize(dataStore.wheelsStoreJson.first())
        if (current.wheels.isEmpty()) {
            // 迁移旧版单转盘数据；若无旧数据则创建一个空转盘
            val oldJson = dataStore.wheelConfigJson.first()
            val seed = if (!oldJson.isNullOrBlank()) {
                WheelConfigSerializer.deserialize(oldJson)
            } else {
                WheelConfig(id = UUID.randomUUID().toString(), title = "", options = emptyList())
            }
            val safeSeed = if (seed.id.isBlank()) seed.copy(id = UUID.randomUUID().toString()) else seed
            saveStore(WheelStore(listOf(safeSeed), safeSeed.id))
        }
    }

    override suspend fun createWheel(): String {
        val newId = UUID.randomUUID().toString()
        val newWheel = WheelConfig(id = newId, title = "新转盘", options = emptyList())
        val store = currentStore()
        saveStore(WheelStore(store.wheels + newWheel, newId))
        return newId
    }

    override suspend fun createWheel(title: String, options: List<com.example.zhuanpan.data.model.WheelOption>): String {
        val newId = UUID.randomUUID().toString()
        val newWheel = WheelConfig(id = newId, title = title, options = options)
        val store = currentStore()
        saveStore(WheelStore(store.wheels + newWheel, newId))
        return newId
    }

    override suspend fun selectWheel(id: String) {
        val store = currentStore()
        if (store.wheels.any { it.id == id }) {
            saveStore(store.copy(currentWheelId = id))
        }
    }

    override suspend fun saveWheelConfig(config: WheelConfig) {
        val store = currentStore()
        val exists = store.wheels.any { it.id == config.id }
        val newWheels = if (exists) {
            store.wheels.map { if (it.id == config.id) config else it }
        } else {
            store.wheels + config
        }
        val currentId = when {
            store.currentWheelId.isBlank() -> config.id
            newWheels.none { it.id == store.currentWheelId } -> config.id
            else -> store.currentWheelId
        }
        saveStore(WheelStore(newWheels, currentId))
    }

    override suspend fun deleteWheel(id: String) {
        val store = currentStore()
        val remaining = store.wheels.filter { it.id != id }
        if (remaining.isEmpty()) {
            // 始终保留至少一个空转盘
            val fallback = WheelConfig(id = UUID.randomUUID().toString(), title = "", options = emptyList())
            saveStore(WheelStore(listOf(fallback), fallback.id))
            return
        }
        val newCurrent = if (store.currentWheelId == id) remaining.first().id else store.currentWheelId
        saveStore(WheelStore(remaining, newCurrent))
    }

    override suspend fun copyWheel(id: String): String {
        val store = currentStore()
        val source = store.wheels.find { it.id == id } ?: return id
        val newId = UUID.randomUUID().toString()
        val copied = source.copy(
            id = newId,
            title = if (source.title.isBlank()) "未命名转盘 副本" else "${source.title} 副本",
            options = source.options.map { it.copy(id = UUID.randomUUID().toString()) }
        )
        saveStore(WheelStore(store.wheels + copied, store.currentWheelId))
        return newId
    }

    override suspend fun resetToDefault() {
        val newId = UUID.randomUUID().toString()
        val wheel = WheelConfig(id = newId, title = "", options = emptyList())
        saveStore(WheelStore(listOf(wheel), newId))
    }

    private suspend fun currentStore(): WheelStore =
        WheelStoreSerializer.deserialize(dataStore.wheelsStoreJson.first())

    private suspend fun saveStore(store: WheelStore) {
        dataStore.saveWheelsStoreJson(WheelStoreSerializer.serialize(store))
    }
}
