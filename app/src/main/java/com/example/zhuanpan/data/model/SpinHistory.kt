package com.example.zhuanpan.data.model

import kotlinx.serialization.Serializable

/**
 * 转盘历史记录集合。
 *
 * @property records 历次抽取记录，按时间从新到旧排列
 */
@Serializable
data class SpinHistory(
    val records: List<SpinHistoryItem> = emptyList()
) {
    companion object {
        /** 最大保存条数，防止 DataStore JSON 过大。 */
        const val MAX_RECORDS = 100
    }
}

/**
 * 单条抽取记录。
 *
 * @property id 记录唯一标识（用于 Compose LazyColumn key，避免重复 key 崩溃）
 * @property result 中奖结果文字
 * @property wheelTitle 当前转盘标题
 * @property timestamp 抽取时间戳（毫秒）
 */
@Serializable
data class SpinHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val result: String,
    val wheelTitle: String,
    val timestamp: Long = System.currentTimeMillis()
)
