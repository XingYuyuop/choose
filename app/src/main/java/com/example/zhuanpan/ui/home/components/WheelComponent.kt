package com.example.zhuanpan.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhuanpan.data.model.ColorScheme
import com.example.zhuanpan.data.model.WheelOption
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.PrimaryRed
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 转盘组件。
 *
 * 使用 Canvas 绘制扇形、文字与中心圆，支持根据旋转角度重绘。
 * 支持手动拖拽旋转：用户手指在转盘上滑动时，转盘跟随手势平滑旋转。
 * 点击中心圆圈区域可触发自动旋转。
 *
 * @param options 转盘选项列表
 * @param rotationDegrees 当前旋转角度
 * @param colorSchemeName 配色方案名称
 * @param onManualRotation 手动旋转增量回调（单位：度）
 * @param onCenterClick 点击中心圆圈回调
 * @param modifier 修饰符
 */
@Composable
fun WheelComponent(
    options: List<WheelOption>,
    rotationDegrees: Float,
    colorSchemeName: String,
    highlightLabel: String? = null,
    onManualRotation: (Float) -> Unit = {},
    onCenterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val colorScheme = ColorScheme.fromName(colorSchemeName)
    val colors = colorScheme.toComposeColors()
    val density = LocalDensity.current.density

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp)
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
                            // 处理角度跨越 ±180° 的边界情况
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

        drawWheelSectors(
            options = options,
            colors = colors,
            rotationDegrees = rotationDegrees,
            highlightLabel = highlightLabel,
            radius = radius,
            center = center
        )

        drawWheelLabels(
            options = options,
            colors = colors,
            rotationDegrees = rotationDegrees,
            highlightLabel = highlightLabel,
            radius = radius,
            center = center,
            textMeasurer = textMeasurer,
            density = density
        )

        // 中心白色圆圈
        drawCircle(
            color = ColorWhite,
            radius = radius * 0.14f,
            center = center
        )

        // 中心圆圈上方的固定箭头指示标记（指向12点钟方向，作为结果判定基准线）
        drawFixedArrow(center = center, radius = radius)
    }
}

/**
 * 绘制中心圆圈上方的固定箭头标记。
 *
 * 箭头固定指向12点钟方向，作为转盘结果的判定基准线，
 * 无论转盘如何旋转，箭头始终指向同一个位置。
 */
private fun DrawScope.drawFixedArrow(center: Offset, radius: Float) {
    val arrowHeight = radius * 0.12f
    val arrowWidth = radius * 0.10f
    
    // 箭头位置：中心圆圈上方，尖头指向下方（12点钟方向）
    val arrowBaseY = center.y - radius * 0.14f
    val arrowTipY = arrowBaseY - arrowHeight
    
    val arrowPath = Path().apply {
        moveTo(center.x, arrowTipY)
        lineTo(center.x - arrowWidth / 2, arrowBaseY)
        lineTo(center.x + arrowWidth / 2, arrowBaseY)
        close()
    }
    
    // 箭头阴影（增加立体感）
    val shadowPath = Path().apply {
        moveTo(center.x, arrowTipY + 2f)
        lineTo(center.x - arrowWidth / 2, arrowBaseY + 2f)
        lineTo(center.x + arrowWidth / 2, arrowBaseY + 2f)
        close()
    }
    drawPath(path = shadowPath, color = Color.Black.copy(alpha = 0.2f))
    
    // 箭头主体
    drawPath(path = arrowPath, color = PrimaryRed)
}

/**
 * 绘制转盘扇形。
 */
private fun DrawScope.drawWheelSectors(
    options: List<WheelOption>,
    colors: List<Color>,
    rotationDegrees: Float,
    highlightLabel: String?,
    radius: Float,
    center: Offset
) {
    if (options.isEmpty()) return

    val totalWeight = options.sumOf { it.weight }.coerceAtLeast(1)
    // Canvas drawArc 的 0° 在 3 点钟方向，减去 90° 使其从 12 点钟方向开始
    var startAngle = rotationDegrees - 90f
    val hasHighlight = !highlightLabel.isNullOrBlank()

    options.forEachIndexed { index, option ->
        val sweep = 360f * option.weight / totalWeight
        val color = option.colorHex?.let { parseHexColor(it) }
            ?: colors.getOrElse(index % colors.size) { colors.firstOrNull() ?: Color.Gray }

        // 高亮逻辑：选中选项保持原色，其余选项降低亮度
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
 * 每个字符沿扇形径向排列并单独旋转，使其始终处于"竖排可读"状态，
 * 同时根据扇形大小和屏幕尺寸动态计算字号，确保文字完整显示在转盘内部。
 */
private fun DrawScope.drawWheelLabels(
    options: List<WheelOption>,
    colors: List<Color>,
    rotationDegrees: Float,
    highlightLabel: String?,
    radius: Float,
    center: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: Float
) {
    if (options.isEmpty()) return

    val totalWeight = options.sumOf { it.weight }.coerceAtLeast(1)
    var currentAngle = rotationDegrees - 90f
    val safeMarginPx = 8f * density
    val centerCircleRadius = radius * 0.16f
    val radialSpace = radius - centerCircleRadius - safeMarginPx * 2
    val hasHighlight = !highlightLabel.isNullOrBlank()

    options.forEachIndexed { index, option ->
        val sweep = 360f * option.weight / totalWeight
        val sectorCenterAngle = currentAngle + sweep / 2

        // 根据背景色亮度选择文字颜色
        val bgColor = option.colorHex?.let { parseHexColor(it) }
            ?: colors.getOrElse(index % colors.size) { colors.firstOrNull() ?: Color.Gray }
        val baseTextColor = if (isDarkColor(bgColor)) Color.White else Color(0xFF666666)

        // 高亮逻辑：选中选项文字保持原色，其余选项文字降低透明度
        val textColor = if (hasHighlight) {
            if (option.label == highlightLabel) baseTextColor else baseTextColor.copy(alpha = 0.35f)
        } else {
            baseTextColor
        }

        // 计算扇形在文字半径处的弦长，作为字符宽度的上限
        val labelRadius = radius * 0.62f
        val halfSweepRad = Math.toRadians((sweep / 2).toDouble().coerceAtMost(90.0))
        val chordLength = 2 * labelRadius * sin(halfSweepRad).toFloat()

        // 最大单字尺寸：受弦长、径向空间和全局上限共同约束
        val maxByChord = (chordLength - safeMarginPx * 2).coerceAtLeast(12f * density)
        val maxByRadial = radialSpace * 0.35f
        val maxCharSize = min(maxByChord, maxByRadial).coerceAtMost(radius * 0.14f)

        drawVerticalLabel(
            label = option.label,
            centerAngle = sectorCenterAngle,
            labelRadius = labelRadius,
            center = center,
            textColor = textColor,
            textMeasurer = textMeasurer,
            maxCharSize = maxCharSize,
            radialSpace = radialSpace,
            density = density
        )

        currentAngle += sweep
    }
}

/**
 * 在指定角度位置绘制竖排标签。
 *
 * 字符沿径向从外向内排列，每个字符单独旋转以保持可读方向。
 * 当标签超过4个字符时，自动分两列排列以节省径向空间。
 * 会根据总高度自动缩放字号，确保文字不超出转盘边界和中心圆。
 */
private fun DrawScope.drawVerticalLabel(
    label: String,
    centerAngle: Float,
    labelRadius: Float,
    center: Offset,
    textColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    maxCharSize: Float,
    radialSpace: Float,
    density: Float
) {
    val displayText = label.take(8) // 限制最大显示字符数，超出部分省略
    val minCharSizePx = 12f * density
    val maxCharSizePx = 22f * density

    // 超过4个字符时，分两列排列
    val splitIndex = if (displayText.length > 4) {
        (displayText.length + 1) / 2 // 第一列多一个字符（奇数时）
    } else {
        displayText.length // 不分行
    }

    val col1 = displayText.take(splitIndex)
    val col2 = displayText.drop(splitIndex)
    val isMultiColumn = col2.isNotEmpty()

    val idealCharSize = maxCharSize.coerceIn(minCharSizePx, maxCharSizePx)
    val columnHeight = if (isMultiColumn) {
        maxOf(col1.length, col2.length) * idealCharSize
    } else {
        col1.length * idealCharSize
    }

    val scaleFactor = if (columnHeight > radialSpace * 0.9f) {
        radialSpace * 0.9f / columnHeight
    } else 1f

    val charSize = (idealCharSize * scaleFactor).coerceAtLeast(minCharSizePx)
    val textStyle = TextStyle(
        color = textColor,
        fontSize = (charSize / density).sp,
        fontWeight = FontWeight.Medium
    )

    val angleRad = Math.toRadians(centerAngle.toDouble())
    val cosAngle = cos(angleRad).toFloat()
    val sinAngle = sin(angleRad).toFloat()

    // 垂直于径向的偏移方向（用于多列时横向排列）
    val perpCos = cos(Math.toRadians((centerAngle + 90f).toDouble())).toFloat()
    val perpSin = sin(Math.toRadians((centerAngle + 90f).toDouble())).toFloat()

    // 计算每列的径向总高度
    val col1Height = col1.length * charSize
    val col2Height = col2.length * charSize
    val totalHeight = if (isMultiColumn) maxOf(col1Height, col2Height) else col1Height

    // 列偏移（沿垂直于径向方向）：第一列偏左，第二列偏右
    val columnSpacing = charSize * 0.85f
    val col1Offset = if (isMultiColumn) -columnSpacing / 2f else 0f
    val col2Offset = if (isMultiColumn) columnSpacing / 2f else 0f

    // 绘制第一列
    col1.forEachIndexed { charIndex, char ->
        val distance = labelRadius + totalHeight / 2 - charIndex * charSize - charSize / 2
        val baseX = center.x + distance * cosAngle
        val baseY = center.y + distance * sinAngle
        val charCenterX = baseX + col1Offset * perpCos
        val charCenterY = baseY + col1Offset * perpSin

        rotate(degrees = centerAngle + 90f, pivot = Offset(charCenterX, charCenterY)) {
            drawText(
                textMeasurer = textMeasurer,
                text = char.toString(),
                topLeft = Offset(
                    x = charCenterX - charSize / 2,
                    y = charCenterY - charSize / 2
                ),
                style = textStyle
            )
        }
    }

    // 绘制第二列
    col2.forEachIndexed { charIndex, char ->
        val distance = labelRadius + totalHeight / 2 - charIndex * charSize - charSize / 2
        val baseX = center.x + distance * cosAngle
        val baseY = center.y + distance * sinAngle
        val charCenterX = baseX + col2Offset * perpCos
        val charCenterY = baseY + col2Offset * perpSin

        rotate(degrees = centerAngle + 90f, pivot = Offset(charCenterX, charCenterY)) {
            drawText(
                textMeasurer = textMeasurer,
                text = char.toString(),
                topLeft = Offset(
                    x = charCenterX - charSize / 2,
                    y = charCenterY - charSize / 2
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
