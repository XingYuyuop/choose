package com.example.zhuanpan.ui.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.PrimaryRed
import com.example.zhuanpan.ui.theme.PrimaryRedLight

/**
 * 旋转按钮组件。
 *
 * 圆角胶囊样式，红色文字，带轻微阴影与点击反馈。
 *
 * @param text 按钮文字
 * @param enabled 是否可点击
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun SpinButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .widthIn(min = 160.dp)
            .shadow(
                elevation = if (enabled) 4.dp else 0.dp,
                shape = RoundedCornerShape(percent = 50)
            ),
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorWhite,
            contentColor = PrimaryRed,
            disabledContainerColor = ColorWhite,
            disabledContentColor = PrimaryRedLight.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}
