package com.example.zhuanpan.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.zhuanpan.ui.theme.PrimaryRed

/**
 * 转盘顶部指针组件。
 *
 * 绘制一个指向下方的三角形指针，无白色圆形底座。
 *
 * @param size 指针整体大小
 * @param modifier 修饰符
 */
@Composable
fun WheelPointer(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2
            val center = Offset(this.size.width / 2, this.size.height / 2)

            // 底部三角形指针（红色，指向下方）
            val trianglePath = Path().apply {
                val pointerWidth = radius * 0.5f
                val pointerHeight = radius * 0.7f
                moveTo(center.x, center.y + radius * 0.15f + pointerHeight)
                lineTo(center.x - pointerWidth / 2, center.y + radius * 0.15f)
                lineTo(center.x + pointerWidth / 2, center.y + radius * 0.15f)
                close()
            }
            drawPath(path = trianglePath, color = PrimaryRed)
        }
    }
}
