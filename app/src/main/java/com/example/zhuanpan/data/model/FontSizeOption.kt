package com.example.zhuanpan.data.model

import kotlinx.serialization.Serializable

/**
 * 结果字体大小选项。
 *
 * @property label 显示名称
 * @property size 字体大小（sp）
 */
@Serializable
enum class FontSizeOption(
    val label: String,
    val size: Int
) {
    SMALL("小号", 22),
    MEDIUM("标准", 28),
    LARGE("大号", 34),
    EXTRA_LARGE("超大", 42);

    companion object {
        /**
         * 根据 sp 值查找字体选项，找不到时返回 [MEDIUM]。
         */
        fun fromSize(size: Int): FontSizeOption =
            entries.find { it.size == size } ?: MEDIUM
    }
}
