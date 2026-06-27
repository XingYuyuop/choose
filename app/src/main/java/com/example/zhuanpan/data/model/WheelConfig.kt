package com.example.zhuanpan.data.model

import kotlinx.serialization.Serializable

/**
 * 转盘配置。
 *
 * @property id 配置唯一标识
 * @property title 转盘问题/标题
 * @property options 选项列表
 * @property colorSchemeName 当前配色方案名称
 */
@Serializable
data class WheelConfig(
    val id: String = "default",
    val title: String = "",
    val options: List<WheelOption> = defaultOptions(),
    val colorSchemeName: String = ColorScheme.RAINBOW.name
) {
    companion object {
        /**
         * 默认选项为空，首次使用时引导用户自行添加。
         */
        fun defaultOptions(): List<WheelOption> = emptyList()
    }
}
