package com.example.zhuanpan.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * 结果展示组件。
 *
 * 在中奖结果变化时播放缩放与淡入动画。
 *
 * @param result 当前结果文字
 * @param fontSizeSp 字体大小（sp）
 * @param modifier 修饰符
 */
@Composable
fun ResultDisplay(
    result: String,
    fontSizeSp: Int,
    modifier: Modifier = Modifier
) {
    var displayedResult by remember { mutableStateOf(result) }
    var animateKey by remember { mutableStateOf(0) }

    LaunchedEffect(result) {
        if (result.isNotBlank() && result != displayedResult) {
            displayedResult = result
            animateKey++
        }
    }

    // 结果变化时重新触发缩放动画：空状态收缩，有结果时恢复
    val targetScale = if (displayedResult.isBlank()) 0.8f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 400),
        label = "result_scale_$animateKey"
    )

    AnimatedVisibility(
        visible = displayedResult.isNotBlank(),
        enter = scaleIn(initialScale = 0.8f, animationSpec = tween(400)) +
                fadeIn(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Text(
            text = displayedResult,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .scale(scale)
                .alpha(if (displayedResult.isBlank()) 0f else 1f)
        )
    }
}
