package com.example.zhuanpan.data.model

import kotlinx.serialization.Serializable

/**
 * 应用全局设置。
 *
 * @property allowRepeat 是否允许重复抽取
 * @property spinDurationMs 旋转动画时长（毫秒）
 * @property colorSchemeName 当前转盘配色方案名称
 * @property resultFontSizeSp 结果字体大小（sp，12~60 连续调节）
 * @property hasSeenGuide 是否已查看首次启动引导提示
 * @property wheelSize 轮盘显示大小（0.5~1.0，占屏幕宽度比例）
 */
@Serializable
data class AppSettings(
    val allowRepeat: Boolean = true,
    val spinDurationMs: Int = 4000,
    val colorSchemeName: String = ColorScheme.PASTEL.name,
    val resultFontSizeSp: Float = 28f,
    val hasSeenGuide: Boolean = false,
    val wheelSize: Float = 0.78f
) {
    companion object {
        const val MIN_SPIN_DURATION_MS = 1000
        const val MAX_SPIN_DURATION_MS = 10000
        const val SPIN_DURATION_STEP_MS = 500
        const val MIN_WHEEL_SIZE = 0.5f
        const val MAX_WHEEL_SIZE = 1.0f
        const val MIN_FONT_SIZE = 12f
        const val MAX_FONT_SIZE = 60f
    }
}
