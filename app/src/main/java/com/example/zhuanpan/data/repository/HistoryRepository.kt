package com.example.zhuanpan.data.repository

import com.example.zhuanpan.data.model.SpinHistory
import com.example.zhuanpan.data.model.SpinHistoryItem
import kotlinx.coroutines.flow.Flow

/**
 * 转盘抽取历史记录仓库接口。
 */
interface HistoryRepository {

    /**
     * 当前历史记录数据流。
     */
    val history: Flow<SpinHistory>

    /**
     * 添加一条抽取记录。
     *
     * @param item 新的记录项
     */
    suspend fun addRecord(item: SpinHistoryItem)

    /**
     * 清空所有历史记录。
     */
    suspend fun clearHistory()
}
