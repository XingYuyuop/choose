package com.example.zhuanpan.utils

import com.example.zhuanpan.data.model.WheelOption
import kotlin.math.abs

/**
 * 转盘相关数学计算工具。
 */
object WheelMath {

    /**
     * 顶部固定指针角度（Canvas 坐标系，12 点钟方向为 270°）。
     */
    const val POINTER_ANGLE = 270f

    /**
     * 将角度归一化到 [0, 360) 范围。
     */
    fun normalizeAngle(angle: Float): Float {
        var normalized = angle % 360f
        if (normalized < 0) normalized += 360f
        return normalized
    }

    /**
     * 根据当前旋转角度计算中奖选项。
     *
     * 转盘顺时针旋转，扇形相对指针逆时针移动。指针固定在 [POINTER_ANGLE]（12 点钟方向），
     * 因此需要找到覆盖该角度的扇形。
     *
     * 注意：绘制代码中扇形起始角度为 `rotationDegrees - 90f`（使 0° 旋转时第一个扇形
     * 从 12 点钟方向开始），计算时需保持一致，起始角度同样减去 90°。
     *
     * @param options 选项列表
     * @param rotationDegrees 当前旋转角度（顺时针为正）
     * @param pointerAngle 指针固定角度，默认为 270°（12 点钟方向）
     * @return 中奖选项，若列表为空返回 null
     */
    fun calculateWinner(
        options: List<WheelOption>,
        rotationDegrees: Float,
        pointerAngle: Float = POINTER_ANGLE
    ): WheelOption? {
        if (options.isEmpty()) return null

        val validOptions = options.filter { it.weight > 0 }
        if (validOptions.isEmpty()) return options.firstOrNull()

        val normalizedPointer = normalizeAngle(pointerAngle)
        val normalizedRotation = normalizeAngle(rotationDegrees - 90f)
        val totalWeight = validOptions.sumOf { it.weight }.toFloat()

        var currentStart = normalizedRotation
        validOptions.forEach { option ->
            val sweep = 360f * option.weight / totalWeight
            val start = normalizeAngle(currentStart)
            val end = normalizeAngle(currentStart + sweep)

            if (isAngleInRange(normalizedPointer, start, end)) {
                return option
            }
            currentStart += sweep
        }

        // 兜底返回最后一个有效选项
        return validOptions.last()
    }

    /**
     * 判断 [angle] 是否落在 [start] 到 [end] 的扇形范围内。
     *
     * 已处理跨越 0° 的边界情况。
     */
    internal fun isAngleInRange(angle: Float, start: Float, end: Float): Boolean {
        val normalizedAngle = normalizeAngle(angle)
        val normalizedStart = normalizeAngle(start)
        val normalizedEnd = normalizeAngle(end)

        return if (normalizedStart <= normalizedEnd) {
            normalizedAngle in normalizedStart..normalizedEnd
        } else {
            // 跨越 0° 的情况
            normalizedAngle >= normalizedStart || normalizedAngle <= normalizedEnd
        }
    }

    /**
     * 计算当前旋转角度下指针指向的扇形索引。
     *
     * 与 [calculateWinner] 一致，扇形起始角度需减去 90° 以匹配绘制逻辑。
     *
     * @return 扇形索引，若选项为空返回 -1
     */
    fun calculateWinnerIndex(
        options: List<WheelOption>,
        rotationDegrees: Float,
        pointerAngle: Float = POINTER_ANGLE
    ): Int {
        if (options.isEmpty()) return -1

        val validOptions = options.filter { it.weight > 0 }
        if (validOptions.isEmpty()) return 0

        val normalizedPointer = normalizeAngle(pointerAngle)
        val normalizedRotation = normalizeAngle(rotationDegrees - 90f)
        val totalWeight = validOptions.sumOf { it.weight }.toFloat()

        var currentStart = normalizedRotation
        validOptions.forEachIndexed { index, option ->
            val sweep = 360f * option.weight / totalWeight
            val start = normalizeAngle(currentStart)
            val end = normalizeAngle(currentStart + sweep)

            if (isAngleInRange(normalizedPointer, start, end)) {
                return options.indexOf(option)
            }
            currentStart += sweep
        }

        return options.indexOf(validOptions.last())
    }

    /**
     * 计算两点间角度差，结果始终为正。
     */
    fun angleDifference(a: Float, b: Float): Float {
        return abs(normalizeAngle(a) - normalizeAngle(b))
    }

    /**
     * 计算将指定选项旋转到指针位置所需的目标角度。
     *
     * 返回的目标角度会大于 [currentRotation]，并额外旋转 2 圈以形成过渡动画。
     *
     * @param options 选项列表
     * @param optionId 目标选项 ID
     * @param currentRotation 当前旋转角度
     * @return 目标旋转角度
     */
    fun calculateRotationToCenterOption(
        options: List<WheelOption>,
        optionId: String,
        currentRotation: Float
    ): Float {
        val validOptions = options.filter { it.weight > 0 }
        if (validOptions.isEmpty()) return currentRotation

        val totalWeight = validOptions.sumOf { it.weight }.toFloat()
        var currentAngle = -90f // 相对 rotation 的角度，从 12 点钟方向开始
        var targetRelativeAngle = 0f
        var found = false

        validOptions.forEach { option ->
            val sweep = 360f * option.weight / totalWeight
            if (!found && option.id == optionId) {
                targetRelativeAngle = currentAngle + sweep / 2
                found = true
            }
            currentAngle += sweep
        }

        if (!found) return currentRotation

        // 目标：rotation + targetRelativeAngle = POINTER_ANGLE (270°)
        val baseTarget = normalizeAngle(POINTER_ANGLE - targetRelativeAngle)
        var target = baseTarget
        while (target <= currentRotation) {
            target += 360f
        }
        // 额外旋转 2 圈，形成平滑过渡
        return target + 360f * 2
    }
}
