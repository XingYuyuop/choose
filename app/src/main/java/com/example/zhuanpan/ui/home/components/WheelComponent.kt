package com.example.zhuanpan.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhuanpan.data.model.AppSettings
import com.example.zhuanpan.data.model.ColorScheme
import com.example.zhuanpan.data.model.WheelOption
import com.example.zhuanpan.ui.theme.ColorWhite
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 预计算的扇区颜色信息，避免每帧重复解析。
 */
private data class SectorColors(
    val bgColor: Color,
    val textColor: Color
)

/**
 * 转盘组件。
 *
 * 使用 Canvas 绘制扇形和文字，通过 graphicsLayer.rotationZ 实现旋转。
 * 旋转时 Canvas 内容不重绘，仅由 GPU 执行矩阵变换，确保动画流畅。
 * 中心圆圈和箭头始终固定在12点钟方向，不随转盘旋转。
 *
 * @param options 转盘选项列表
 * @param rotationDegrees 当前旋转角度
 * @param colorSchemeName 配色方案名称
 * @param highlightLabel 高亮选项标签
 * @param onManualRotation 手动旋转增量回调（单位：度）
 * @param onCenterClick 点击中心圆圈回调
 * @param modifier 修饰符
 */
@Composable
fun WheelComponent(
    options: List<WheelOption>,
    rotationDegrees: Float,
    colorSchemeName: String,
    optionFontSizeSp: Float = AppSettings.DEFAULT_OPTION_FONT_SIZE,
    highlightLabel: String? = null,
    onManualRotation: (Float) -> Unit = {},
    onCenterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val colorScheme = ColorScheme.fromName(colorSchemeName)
    val colors = colorScheme.toComposeColors()
    val density = LocalDensity.current.density

    // 预计算每个选项的颜色，避免每帧在 draw scope 中重复解析 hex 颜色
    val sectorColors = remember(options, colorSchemeName) {
        options.mapIndexed { index, option ->
            val bgColor = option.colorHex?.let { parseHexColor(it) }
                ?: colors.getOrElse(index % colors.size) { Color.Gray }
            val textColor = if (isDarkColor(bgColor)) Color.White else Color(0xFF555555)
            SectorColors(bgColor, textColor)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        // 旋转层：扇形 + 文字标签，通过 graphicsLayer 旋转（GPU 变换，无需重绘）
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotationDegrees
                }
        ) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawWheelSectors(
                options = options,
                sectorColors = sectorColors,
                highlightLabel = highlightLabel,
                radius = radius,
                center = center
            )

            drawWheelLabels(
                options = options,
                sectorColors = sectorColors,
                highlightLabel = highlightLabel,
                radius = radius,
                center = center,
                textMeasurer = textMeasurer,
                density = density,
                optionFontSizeSp = optionFontSizeSp
            )
        }

        // 固定层：中心圆圈 + 箭头 + 手势（不随转盘旋转）
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // 拖拽旋转手势
                .pointerInput(Unit) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    var previousAngle: Float? = null

                    detectDragGestures(
                        onDragStart = { previousAngle = null },
                        onDragEnd = { previousAngle = null },
                        onDragCancel = { previousAngle = null },
                        onDrag = { change, _ ->
                            val currentAngle = atan2(
                                change.position.y - center.y,
                                change.position.x - center.x
                            )
                            previousAngle?.let { prev ->
                                var delta = Math.toDegrees(
                                    (currentAngle - prev).toDouble()
                                ).toFloat()
                                if (delta > 180f) delta -= 360f
                                if (delta < -180f) delta += 360f
                                onManualRotation(delta)
                            }
                            previousAngle = currentAngle
                            change.consume()
                        }
                    )
                }
                // 点击中心圆圈触发旋转
                .pointerInput(Unit) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = minOf(size.width, size.height) / 2f
                    val centerCircleRadius = radius * 0.14f

                    detectTapGestures(
                        onTap = { tapPosition ->
                            val dx = tapPosition.x - center.x
                            val dy = tapPosition.y - center.y
                            val distanceSquared = dx * dx + dy * dy
                            val clickRadius = centerCircleRadius * 2f
                            if (distanceSquared <= clickRadius * clickRadius) {
                                onCenterClick()
                            }
                        }
                    )
                }
        ) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // 中心白色圆圈
            drawCircle(
                color = ColorWhite,
                radius = radius * 0.14f,
                center = center
            )

            // 固定箭头
            drawFixedArrow(center = center, radius = radius)
        }
    }
}

/**
 * 绘制中心圆圈上方的固定箭头标记。
 *
 * 箭头固定指向12点钟方向，作为转盘结果的判定基准线。
 */
private fun DrawScope.drawFixedArrow(center: Offset, radius: Float) {
    val arrowHeight = radius * 0.12f
    val arrowWidth = radius * 0.10f

    val centerCircleRadius = radius * 0.14f
    val arrowBaseY = center.y - centerCircleRadius
    val arrowTipY = arrowBaseY - arrowHeight

    val arrowPath = Path().apply {
        moveTo(center.x, arrowTipY)
        lineTo(center.x - arrowWidth / 2, arrowBaseY)
        lineTo(center.x + arrowWidth / 2, arrowBaseY)
        close()
    }

    drawPath(path = arrowPath, color = Color.White)
}

/**
 * 绘制转盘扇形。
 *
 * 使用 graphicsLayer 旋转后，startAngle 直接从 -90° 开始（12 点钟方向），
 * 不再需要加上 rotationDegrees。
 */
private fun DrawScope.drawWheelSectors(
    options: List<WheelOption>,
    sectorColors: List<SectorColors>,
    highlightLabel: String?,
    radius: Float,
    center: Offset
) {
    if (options.isEmpty()) return

    val totalWeight = options.sumOf { it.weight }.coerceAtLeast(1)
    var startAngle = -90f
    val hasHighlight = !highlightLabel.isNullOrBlank()

    options.forEachIndexed { index, option ->
        val sweep = 360f * option.weight / totalWeight
        val color = sectorColors.getOrElse(index) { SectorColors(Color.Gray, Color.White) }.bgColor

        val finalColor = if (hasHighlight) {
            if (option.label == highlightLabel) color else color.copy(alpha = 0.35f)
        } else {
            color
        }

        drawArc(
            color = finalColor,
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        startAngle += sweep
    }
}

/**
 * 绘制转盘标签文字。
 *
 * 使用 graphicsLayer 旋转后，角度从 -90° 开始，不再需要加上 rotationDegrees。
 * 文字颜色使用预计算的 sectorColors，避免每帧重复解析 hex 颜色。
 */
private fun DrawScope.drawWheelLabels(
    options: List<WheelOption>,
    sectorColors: List<SectorColors>,
    highlightLabel: String?,
    radius: Float,
    center: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: Float,
    optionFontSizeSp: Float
) {
    if (options.isEmpty()) return

    val totalWeight = options.sumOf { it.weight }.coerceAtLeast(1)
    var currentAngle = -90f
    val safeMarginPx = 6f * density
    val centerCircleRadius = radius * 0.16f
    val radialSpace = radius - centerCircleRadius - safeMarginPx * 2
    val hasHighlight = !highlightLabel.isNullOrBlank()

    options.forEachIndexed { index, option ->
        val sweep = 360f * option.weight / totalWeight
        val sectorCenterAngle = currentAngle + sweep / 2

        val colors = sectorColors.getOrElse(index) { SectorColors(Color.Gray, Color.White) }
        val baseTextColor = colors.textColor

        val textColor = if (hasHighlight) {
            if (option.label == highlightLabel) baseTextColor else baseTextColor.copy(alpha = 0.35f)
        } else {
            baseTextColor
        }

        val labelRadius = radius * 0.62f
        val halfSweepRad = Math.toRadians((sweep / 2).toDouble().coerceAtMost(90.0))
        val arcLength = 2 * labelRadius * sin(halfSweepRad).toFloat()

        drawRadialLabel(
            label = option.label,
            centerAngle = sectorCenterAngle,
            labelRadius = labelRadius,
            arcLength = arcLength,
            radialSpace = radialSpace,
            center = center,
            textColor = textColor,
            textMeasurer = textMeasurer,
            density = density,
            optionFontSizeSp = optionFontSizeSp
        )

        currentAngle += sweep
    }
}

/**
 * 沿射线方向绘制标签。
 *
 * 射线状排列规则：
 * - 文字沿半径方向（从圆心向外）排列，每个字符的方向与所在选项的射线方向一致
 * - 字符大小根据扇形弧长（切线方向）和径向空间（半径方向）自适应缩放，
 *   不再依据选项数量分档，确保任意选项数量下均清晰可读
 * - 当扇形位于左半圈（90°~270°）时自动翻转 180°，使文字"字头朝外"保持可读
 */
private fun DrawScope.drawRadialLabel(
    label: String,
    centerAngle: Float,
    labelRadius: Float,
    arcLength: Float,
    radialSpace: Float,
    center: Offset,
    textColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: Float,
    optionFontSizeSp: Float
) {
    val minCharSizePx = AppSettings.MIN_OPTION_FONT_SIZE * density
    val maxCharSizePx = AppSettings.MAX_OPTION_FONT_SIZE * density
    // 射线方向空间充裕，允许显示较多字符；超出部分截断
    val displayText = label.take(10)

    if (displayText.isEmpty()) return

    val charCount = displayText.length
    val safeMarginPx = 4f * density

    // 字符大小自适应：受弧长（切线方向）和径向空间共同限制，移除选项数量的硬性分档
    val maxByArc = (arcLength - safeMarginPx * 2).coerceAtLeast(minCharSizePx)
    val maxByRadial = (radialSpace * 0.85f) / charCount
    // 用户设定的字号作为目标上限，受弧长与径向空间约束，确保不重叠
    val userSizePx = optionFontSizeSp * density
    val charSize = min(min(maxByArc, maxByRadial), userSizePx).coerceIn(minCharSizePx, maxCharSizePx)

    val textStyle = TextStyle(
        color = textColor,
        fontSize = (charSize / density).sp,
        fontWeight = FontWeight.Medium
    )

    val angleRad = Math.toRadians(centerAngle.toDouble())
    val cosAngle = cos(angleRad).toFloat()
    val sinAngle = sin(angleRad).toFloat()

    // 左半圈（90°~270°）文字会倒置，翻转 180° 使字头朝外，保持可读
    val normalizedAngle = ((centerAngle % 360f) + 360f) % 360f
    val isFlipped = normalizedAngle > 90f && normalizedAngle < 270f
    val rotation = if (isFlipped) centerAngle + 180f else centerAngle

    // 翻转时反转字符顺序，保证阅读方向仍然从内向外
    val chars = if (isFlipped) displayText.reversed().toString() else displayText.toString()

    // 文字沿半径方向排列，整体中心对齐到 labelRadius
    val textLength = chars.length * charSize
    val startDistance = labelRadius - textLength / 2f

    chars.forEachIndexed { index, char ->
        val distance = startDistance + index * charSize + charSize / 2f
        val charCenterX = center.x + distance * cosAngle
        val charCenterY = center.y + distance * sinAngle

        rotate(degrees = rotation, pivot = Offset(charCenterX, charCenterY)) {
            drawText(
                textMeasurer = textMeasurer,
                text = char.toString(),
                topLeft = Offset(
                    x = charCenterX - charSize / 2f,
                    y = charCenterY - charSize / 2f
                ),
                style = textStyle
            )
        }
    }
}

/**
 * 解析十六进制颜色字符串。
 */
private fun parseHexColor(hex: String): Color {
    return try {
        Color(hex.removePrefix("#").toLong(16))
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * 判断颜色是否偏暗，用于选择文字颜色。
 */
private fun isDarkColor(color: Color): Boolean {
    val r = color.red
    val g = color.green
    val b = color.blue
    val luminance = 0.299f * r + 0.587f * g + 0.114f * b
    return luminance < 0.5f
}
