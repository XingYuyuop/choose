package com.example.zhuanpan.ui.random

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhuanpan.ui.theme.Background
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.OnSurface
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 随机数生成模块。
 *
 * 提供最小值、最大值、数量三个整数输入框以及“允许重复”开关，按要求生成
 * 范围内（含边界）的随机数集合，并提供输入校验与错误提示。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RandomNumberScreen(modifier: Modifier = Modifier) {
    var minText by remember { mutableStateOf("1") }
    var maxText by remember { mutableStateOf("30") }
    var countText by remember { mutableStateOf("1") }
    var allowRepeat by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Int>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCopyToast by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Numbers,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "随机数生成",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
            // 复制按钮（始终显示，无结果时不可点击）
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (results.isNotEmpty()) Background else Color.Transparent)
                    .clickable(enabled = results.isNotEmpty()) {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        clipboard?.setPrimaryClip(
                            android.content.ClipData.newPlainText(
                                "随机数",
                                results.joinToString(", ")
                            )
                        )
                        showCopyToast = true
                        coroutineScope.launch {
                            delay(1500)
                            showCopyToast = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "复制结果",
                    tint = if (results.isNotEmpty()) OnSurfaceVariant else OnSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 最小值 / 最大值
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberInputField(
                value = minText,
                onValueChange = { minText = it.filter { ch -> ch.isDigit() || ch == '-' } },
                label = "最小值",
                modifier = Modifier.weight(1f)
            )
            NumberInputField(
                value = maxText,
                onValueChange = { maxText = it.filter { ch -> ch.isDigit() || ch == '-' } },
                label = "最大值",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 数量
        NumberInputField(
            value = countText,
            onValueChange = { countText = it.filter { ch -> ch.isDigit() } },
            label = "生成数量",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 允许重复开关
        ToggleRow(
            checked = allowRepeat,
            onCheckedChange = { allowRepeat = it },
            label = "允许重复"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 错误提示
        errorMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryRed.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = msg,
                    fontSize = 13.sp,
                    color = PrimaryRed,
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 生成按钮
        GenerateButton(
            onClick = {
                val error = validate(minText, maxText, countText, allowRepeat)
                if (error != null) {
                    errorMessage = error
                    results = emptyList()
                    return@GenerateButton
                }
                errorMessage = null
                val min = minText.toInt()
                val max = maxText.toInt()
                val count = countText.toInt()
                results = generateNumbers(min, max, count, allowRepeat)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 结果展示
        if (results.isNotEmpty()) {
            Text(
                text = "生成结果",
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // 大号数字展示
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                results.forEach { value ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Background)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                }
            }
        }
    }

    // 复制成功提示
    if (showCopyToast) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryRed)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "已复制到剪贴板",
                    color = ColorWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 校验输入并返回错误信息，合法时返回 null。
 */
private fun validate(
    minText: String,
    maxText: String,
    countText: String,
    allowRepeat: Boolean
): String? {
    if (minText.isBlank() || maxText.isBlank() || countText.isBlank()) {
        return "请填写完整的数值"
    }
    val min = minText.toIntOrNull() ?: return "最小值需为有效整数"
    val max = maxText.toIntOrNull() ?: return "最大值需为有效整数"
    val count = countText.toIntOrNull() ?: return "数量需为有效正整数"

    if (count <= 0) return "数量必须为正整数"
    if (max <= min) return "最大值必须大于最小值"
    if (!allowRepeat) {
        val available = (max - min + 1).toLong()
        if (count.toLong() > available) return "不重复时数量不能超过 $available（可用组合数）"
    }
    return null
}

/**
 * 生成随机数集合。
 *
 * - 允许重复：直接在 [min, max] 闭区间内随机取 count 个。
 * - 不允许重复：使用集合去重，支持超大范围而不会一次性展开区间。
 */
private fun generateNumbers(
    min: Int,
    max: Int,
    count: Int,
    allowRepeat: Boolean
): List<Int> {
    return if (allowRepeat) {
        List(count) { Random.nextInt(min, max + 1) }
    } else {
        val range = (max - min + 1).toLong()
        // 范围较小：直接打乱取前 count 个，分布更均匀
        if (range <= 100_000L) {
            (min..max).shuffled(Random).take(count)
        } else {
            // 范围较大：使用集合去重抽样，避免内存爆炸
            val picked = LinkedHashSet<Int>()
            while (picked.size < count) {
                picked.add(Random.nextInt(min, max + 1))
            }
            picked.toList()
        }
    }
}

/**
 * 整数输入框。
 */
@Composable
private fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryRed,
            focusedLabelColor = PrimaryRed,
            cursorColor = PrimaryRed,
            unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.4f),
            focusedContainerColor = ColorWhite,
            unfocusedContainerColor = ColorWhite
        ),
        modifier = modifier
    )
}

/**
 * 允许重复开关行。
 */
@Composable
private fun ToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Background)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = OnSurface,
            fontWeight = FontWeight.Medium
        )
        CheckBox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * 简洁复选框。
 */
@Composable
private fun CheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) PrimaryRed else ColorWhite)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Text(
                text = "✓",
                color = ColorWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 生成按钮。
 */
@Composable
private fun GenerateButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(if (isPressed) PrimaryRed.copy(alpha = 0.9f) else PrimaryRed)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Casino,
            contentDescription = null,
            tint = ColorWhite,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "生成随机数",
            color = ColorWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 结果展示卡片。
 */
@Composable
private fun ResultChip(
    index: Int,
    value: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index",
            fontSize = 11.sp,
            color = OnSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
    }
}
