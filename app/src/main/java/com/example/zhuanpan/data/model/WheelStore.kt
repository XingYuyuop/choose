package com.example.zhuanpan.data.model

import kotlinx.serialization.Serializable

/**
 * 转盘集合存储模型。
 *
 * 保存所有已创建的转盘以及当前选中的转盘 ID，作为多转盘管理的单一数据源。
 *
 * @property wheels 全部转盘配置列表
 * @property currentWheelId 当前选中的转盘 ID
 */
@Serializable
data class WheelStore(
    val wheels: List<WheelConfig> = emptyList(),
    val currentWheelId: String = ""
)
