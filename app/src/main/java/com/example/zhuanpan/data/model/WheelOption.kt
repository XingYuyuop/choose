package com.example.zhuanpan.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 转盘单个选项。
 *
 * @property id 唯一标识
 * @property label 显示文本
 * @property weight 权重，默认 1，权重越高扇形越大
 * @property colorHex 自定义颜色（ARGB 十六进制字符串），为空时使用配色方案循环颜色
 */
@Serializable
data class WheelOption(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val weight: Int = 1,
    val colorHex: String? = null
) {
    init {
        require(weight >= 0) { "选项权重不能为负数" }
    }
}
