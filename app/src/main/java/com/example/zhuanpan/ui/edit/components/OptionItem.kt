package com.example.zhuanpan.ui.edit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhuanpan.data.model.WheelOption
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed

/**
 * 选项编辑行。
 *
 * @param option 当前选项
 * @param onLabelChange 选项文本变化回调
 * @param onWeightChange 选项权重变化回调
 * @param onRemove 删除选项回调
 */
@Composable
fun OptionItem(
    option: WheelOption,
    onLabelChange: (String) -> Unit,
    onWeightChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorWhite)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BasicTextField(
                value = option.label,
                onValueChange = {
                    if (it.length <= MAX_OPTION_LABEL_LENGTH) onLabelChange(it)
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            ) { innerTextField ->
                Box {
                    if (option.label.isBlank()) {
                        Text(
                            text = "输入选项",
                            fontSize = 16.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 权重调整区
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "权重",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )

                WeightStepperButton(
                    icon = Icons.Default.Remove,
                    contentDescription = "减少权重",
                    onClick = { onWeightChange(option.weight - 1) }
                )

                Text(
                    text = "${option.weight}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (option.weight <= 0) OnSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center
                )

                WeightStepperButton(
                    icon = Icons.Default.Add,
                    contentDescription = "增加权重",
                    onClick = { onWeightChange(option.weight + 1) }
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "删除选项",
                    tint = PrimaryRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 字数计数提示
        Text(
            text = "${option.label.length}/$MAX_OPTION_LABEL_LENGTH",
            fontSize = 11.sp,
            color = if (option.label.length >= MAX_OPTION_LABEL_LENGTH) PrimaryRed else OnSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, end = 40.dp),
            textAlign = TextAlign.End
        )
    }
}

/** 选项名称最大字符数。 */
const val MAX_OPTION_LABEL_LENGTH = 20

/**
 * 权重步进器小按钮。
 */
@Composable
private fun WeightStepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(28.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(OnSurfaceVariant.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
