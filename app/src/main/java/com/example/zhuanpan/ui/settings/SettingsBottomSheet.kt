package com.example.zhuanpan.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhuanpan.ZhuanpanApplication
import com.example.zhuanpan.data.model.AppSettings
import com.example.zhuanpan.data.model.ColorScheme
import com.example.zhuanpan.ui.theme.Background
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.Divider
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed

/**
 * 设置底部弹窗。
 *
 * @param onDismiss 关闭弹窗回调
 * @param viewModel 设置 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            settingsRepository = (LocalContext.current.applicationContext as ZhuanpanApplication).settingsRepository
        )
    )
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    var showColorSchemeDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorWhite,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // 允许重复抽取
            SettingsToggleItem(
                icon = Icons.Default.ContentCopy,
                title = "允许重复抽取",
                checked = settings.allowRepeat,
                onCheckedChange = { viewModel.onAllowRepeatChanged(it) }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // 声音开关
            SettingsToggleItem(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "旋转音效",
                checked = settings.soundEnabled,
                onCheckedChange = { viewModel.onSoundEnabledChanged(it) }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // 旋转时长
            SettingsStepperItem(
                icon = Icons.Default.AccessTime,
                title = "旋转时长",
                value = "${settings.spinDurationMs / 1000}s",
                onDecrease = { viewModel.onSpinDurationChanged(false) },
                onIncrease = { viewModel.onSpinDurationChanged(true) }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // 转盘配色
            SettingsOptionItem(
                icon = Icons.Default.Palette,
                title = "转盘配色",
                value = ColorScheme.fromName(settings.colorSchemeName).schemeName,
                onClick = { showColorSchemeDialog = true }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // 结果字体大小
            SettingsSliderItem(
                icon = Icons.Default.FormatSize,
                title = "结果字体大小",
                value = settings.resultFontSizeSp,
                valueRange = AppSettings.MIN_FONT_SIZE..AppSettings.MAX_FONT_SIZE,
                valueText = "${settings.resultFontSizeSp.toInt()}sp",
                onValueChange = { viewModel.onFontSizeChanged(it) }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // 转盘大小
            SettingsSliderItem(
                icon = Icons.Default.AspectRatio,
                title = "转盘大小",
                value = settings.wheelSize,
                onValueChange = { viewModel.onWheelSizeChanged(it) }
            )
        }
    }

    // 配色方案选择弹窗
    if (showColorSchemeDialog) {
        SelectionDialog(
            title = "选择转盘配色",
            items = ColorScheme.entries.map { it.schemeName },
            selectedIndex = ColorScheme.entries.indexOfFirst { it.name == settings.colorSchemeName },
            onItemSelected = { index ->
                viewModel.onColorSchemeChanged(ColorScheme.entries[index])
                showColorSchemeDialog = false
            },
            onDismiss = { showColorSchemeDialog = false }
        )
    }
}

/**
 * 通用单选弹窗。
 */
@Composable
private fun SelectionDialog(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(index) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (index == selectedIndex) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "已选择",
                                tint = PrimaryRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = OnSurfaceVariant)
            }
        },
        containerColor = ColorWhite
    )
}

/**
 * 底部弹窗拖拽指示条。
 */
@Composable
private fun BottomSheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 12.dp, bottom = 16.dp)
            .width(40.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Divider)
    )
}

/**
 * 设置开关项。
 */
@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ColorWhite,
                checkedTrackColor = PrimaryRed,
                uncheckedThumbColor = ColorWhite,
                uncheckedTrackColor = OnSurfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * 设置步进器项。
 */
@Composable
private fun SettingsStepperItem(
    icon: ImageVector,
    title: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 12.dp)
            )
            StepperButton(
                icon = Icons.Default.Remove,
                onClick = onDecrease
            )
            Spacer(modifier = Modifier.width(8.dp))
            StepperButton(
                icon = Icons.Default.Add,
                onClick = onIncrease
            )
        }
    }
}

/**
 * 设置选项项（带箭头）。
 */
@Composable
private fun SettingsOptionItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 步进器按钮。
 */
@Composable
private fun StepperButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 设置滑动条项（用于转盘大小、字体大小等连续值调节）。
 */
@Composable
private fun SettingsSliderItem(
    icon: ImageVector,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = AppSettings.MIN_WHEEL_SIZE..AppSettings.MAX_WHEEL_SIZE,
    valueText: String = "${(value * 100).toInt()}%",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(16.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryRed,
                activeTrackColor = PrimaryRed,
                inactiveTrackColor = OnSurfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = valueText,
            fontSize = 14.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.width(48.dp)
        )
    }
}
