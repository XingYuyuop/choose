package com.example.zhuanpan

import com.example.zhuanpan.data.model.WheelOption
import com.example.zhuanpan.utils.WheelMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 转盘指针边界停留 Bug 修复验证测试。
 *
 * 验证 [WheelMath.generateSafeRotation] 生成的目标旋转角度，
 * 确保指针不会停留在选项分界线上，且概率分布与权重一致。
 */
class WheelMathBoundaryTest {

    /**
     * 计算指针距离最近边界的角度（度）。
     *
     * @param options 选项列表
     * @param rotation 旋转角度
     * @return 指针到最近边界的最小角度距离（度）
     */
    private fun distanceToNearestBoundary(options: List<WheelOption>, rotation: Float): Float {
        val validOptions = options.filter { it.weight > 0 }
        if (validOptions.isEmpty()) return Float.MAX_VALUE

        val totalWeight = validOptions.sumOf { it.weight }.toFloat()
        val normalizedRotation = WheelMath.normalizeAngle(rotation - 90f)
        val pointerAngle = WheelMath.POINTER_ANGLE

        // 计算所有边界角度
        val boundaries = mutableListOf<Float>()
        var currentStart = normalizedRotation
        validOptions.forEach { option ->
            boundaries.add(WheelMath.normalizeAngle(currentStart))
            currentStart += 360f * option.weight / totalWeight
        }

        // 计算指针到每个边界的最短角度距离
        var minDistance = Float.MAX_VALUE
        boundaries.forEach { boundary ->
            val diff = WheelMath.normalizeAngle(pointerAngle - boundary)
            val distance = minOf(diff, 360f - diff)
            if (distance < minDistance) minDistance = distance
        }

        return minDistance
    }

    /**
     * 测试：1000 次模拟，指针停留在分界线上的概率为 0%。
     *
     * 分界线判定标准：指针距最近边界 < 0.5°（视觉上无法区分）。
     */
    @Test
    fun `1000 simulations have zero boundary hits`() {
        val options = listOf(
            WheelOption(id = "1", label = "A", weight = 1),
            WheelOption(id = "2", label = "B", weight = 1),
            WheelOption(id = "3", label = "C", weight = 1),
            WheelOption(id = "4", label = "D", weight = 1)
        )

        val simulationCount = 1000
        val boundaryThreshold = 0.5f // 视觉上无法区分的边界距离
        var boundaryHits = 0
        var currentRotation = 0f

        repeat(simulationCount) {
            val targetRotation = WheelMath.generateSafeRotation(options, currentRotation)
            val distance = distanceToNearestBoundary(options, targetRotation)

            if (distance < boundaryThreshold) {
                boundaryHits++
            }
            currentRotation = targetRotation
        }

        assertEquals("1000 次模拟中不应有边界命中", 0, boundaryHits)
    }

    /**
     * 测试：使用不等权重的选项，验证 1000 次模拟零边界命中。
     */
    @Test
    fun `1000 simulations with unequal weights have zero boundary hits`() {
        val options = listOf(
            WheelOption(id = "1", label = "张三", weight = 3),
            WheelOption(id = "2", label = "李四", weight = 1),
            WheelOption(id = "3", label = "王五", weight = 2)
        )

        val simulationCount = 1000
        val boundaryThreshold = 0.5f
        var boundaryHits = 0
        var currentRotation = 0f

        repeat(simulationCount) {
            val targetRotation = WheelMath.generateSafeRotation(options, currentRotation)
            val distance = distanceToNearestBoundary(options, targetRotation)

            if (distance < boundaryThreshold) {
                boundaryHits++
            }
            currentRotation = targetRotation
        }

        assertEquals("不等权重 1000 次模拟中不应有边界命中", 0, boundaryHits)
    }

    /**
     * 测试：10000 次模拟，验证概率分布与权重基本一致（±5% 容差）。
     */
    @Test
    fun `10000 simulations preserve weight distribution`() {
        val options = listOf(
            WheelOption(id = "1", label = "A", weight = 3),
            WheelOption(id = "2", label = "B", weight = 1)
        )

        val simulationCount = 10000
        val winCounts = mutableMapOf("A" to 0, "B" to 0)
        var currentRotation = 0f

        repeat(simulationCount) {
            val targetRotation = WheelMath.generateSafeRotation(options, currentRotation)
            val winner = WheelMath.calculateWinner(options, targetRotation)
            winCounts[winner!!.label] = winCounts[winner.label]!! + 1
            currentRotation = targetRotation
        }

        val totalWeight = 4f
        val expectedARatio = 3f / totalWeight // 75%
        val actualARatio = winCounts["A"]!!.toFloat() / simulationCount

        assertTrue(
            "A 的中奖比例应接近 ${expectedARatio * 100}%，实际为 ${actualARatio * 100}%",
            kotlin.math.abs(actualARatio - expectedARatio) < 0.05f
        )
    }

    /**
     * 测试：单选项场景不崩溃。
     */
    @Test
    fun `single option does not crash`() {
        val options = listOf(WheelOption(id = "1", label = "Only", weight = 1))
        val target = WheelMath.generateSafeRotation(options, 0f)
        val winner = WheelMath.calculateWinner(options, target)

        assertTrue("单选项应返回该选项", winner != null)
        assertEquals("Only", winner!!.label)
    }

    /**
     * 测试：生成的旋转角度始终大于当前角度。
     */
    @Test
    fun `generated rotation always increases`() {
        val options = listOf(
            WheelOption(id = "1", label = "A", weight = 1),
            WheelOption(id = "2", label = "B", weight = 1)
        )

        var currentRotation = 1234.5f
        repeat(100) {
            val target = WheelMath.generateSafeRotation(options, currentRotation)
            assertTrue("目标角度应大于当前角度", target > currentRotation)
            currentRotation = target
        }
    }
}
