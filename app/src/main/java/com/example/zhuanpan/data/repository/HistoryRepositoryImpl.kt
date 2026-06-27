package com.example.zhuanpan.data.repository

import com.example.zhuanpan.data.local.SpinHistorySerializer
import com.example.zhuanpan.data.local.ZhuanpanDataStore
import com.example.zhuanpan.data.model.SpinHistory
import com.example.zhuanpan.data.model.SpinHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 历史记录仓库实现。
 *
 * @property dataStore DataStore 封装实例
 */
class HistoryRepositoryImpl(
    private val dataStore: ZhuanpanDataStore
) : HistoryRepository {

    override val history: Flow<SpinHistory> = dataStore.spinHistoryJson
        .map { json -> SpinHistorySerializer.deserialize(json) }

    override suspend fun addRecord(item: SpinHistoryItem) {
        val current = history.first()
        val newRecords = listOf(item) + current.records
        val trimmed = if (newRecords.size > SpinHistory.MAX_RECORDS) {
            newRecords.take(SpinHistory.MAX_RECORDS)
        } else {
            newRecords
        }
        dataStore.saveSpinHistoryJson(SpinHistorySerializer.serialize(SpinHistory(trimmed)))
    }

    override suspend fun clearHistory() {
        dataStore.saveSpinHistoryJson(SpinHistorySerializer.serialize(SpinHistory()))
    }
}
