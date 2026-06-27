package com.example.zhuanpan.data.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * 转盘配色方案。
 *
 * @property schemeName 方案显示名称
 * @property colors 扇形颜色十六进制字符串列表（ARGB 格式，如 #FF00BCD4）
 */
@Serializable
enum class ColorScheme(
    val schemeName: String,
    val colors: List<String>
) {
    PASTEL(
        "柔和",
        listOf(
            "#FF00BCD4", // 青色
            "#FFBBDEFB", // 浅蓝
            "#FFFFCDD2", // 粉色
            "#FFFFECB3", // 黄色
            "#FFDCEDC8", // 浅绿
            "#FFB2DFDB"  // 薄荷
        )
    ),
    VIBRANT(
        "鲜艳",
        listOf(
            "#FFF44336", // 红
            "#FF2196F3", // 蓝
            "#FF4CAF50", // 绿
            "#FFFFEB3B", // 黄
            "#FF9C27B0", // 紫
            "#FFFF9800"  // 橙
        )
    ),
    RAINBOW(
        "彩虹",
        listOf(
            "#FF00838F", // 深青
            "#FF1565C0", // 深蓝
            "#FF6A1B9A", // 深紫
            "#FFC2185B", // 深粉红
            "#FFC62828", // 深红
            "#FFEF6C00", // 深橙
            "#FFF9A825", // 深黄
            "#FF558B2F"  // 深绿
        )
    ),
    MONOCHROME(
        "单色",
        listOf(
            "#FF333333",
            "#FF666666",
            "#FF999999",
            "#FFCCCCCC",
            "#FFE0E0E0",
            "#FFEEEEEE"
        )
    );

    /**
     * 将配色方案转换为 Compose [Color] 列表。
     */
    fun toComposeColors(): List<Color> = colors.map { parseColor(it) }

    companion object {
        /**
         * 根据名称查找配色方案，找不到时返回默认 [PASTEL]。
         */
        fun fromName(name: String): ColorScheme =
            entries.find { it.name == name } ?: PASTEL

        private fun parseColor(hex: String): Color =
            Color(hex.removePrefix("#").toLong(16))
    }
}
