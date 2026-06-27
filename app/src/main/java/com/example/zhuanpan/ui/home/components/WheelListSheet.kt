package com.example.zhuanpan.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhuanpan.data.model.WheelConfig
import com.example.zhuanpan.ui.theme.Background
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.OnSurface
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed

/**
 * 转盘列表管理底部弹窗。
 *
 * 以可滚动列表集合形式展示所有已创建的转盘，支持新建、查看（选中切换）、
 * 内联编辑名称、删除等管理操作。在列表中对当前转盘的修改会实时同步到转盘界面。
 *
 * @param wheels 全部转盘列表
 * @param currentWheelId 当前选中的转盘 ID
 * @param onCreateClick 新建转盘回调
 * @param onSelectWheel 选中转盘回调
 * @param onTitleChanged 转盘标题变化回调（wheelId, newTitle）
 * @param onDeleteWheel 删除转盘回调
 * @param onDismiss 关闭弹窗回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelListSheet(
    wheels: List<WheelConfig>,
    currentWheelId: String,
    onCreateClick: () -> Unit,
    onSelectWheel: (String) -> Unit,
    onTitleChanged: (String, String) -> Unit,
    onDeleteWheel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var pendingDelete by remember { mutableStateOf<WheelConfig?>(null) }

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
            // 标题栏 + 新建按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "转盘列表（${wheels.size}）",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(PrimaryRed)
                        .clickable { onCreateClick() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "新建转盘",
                        tint = ColorWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "新建",
                        fontSize = 13.sp,
                        color = ColorWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 可滚动的转盘集合
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wheels, key = { it.id }) { wheel ->
                    WheelItem(
                        wheel = wheel,
                        isCurrent = wheel.id == currentWheelId,
                        onSelect = { onSelectWheel(wheel.id) },
                        onTitleChange = { onTitleChanged(wheel.id, it) },
                        onDelete = { pendingDelete = wheel }
                    )
                }
            }
        }
    }

    // 删除确认对话框
    pendingDelete?.let { wheel ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除转盘") },
            text = { Text("确定要删除“${wheel.title.ifBlank { "未命名转盘" }}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteWheel(wheel.id)
                        pendingDelete = null
                    }
                ) {
                    Text("删除", color = PrimaryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("取消", color = OnSurfaceVariant)
                }
            },
            containerColor = ColorWhite
        )
    }
}

/**
 * 单个转盘列表项。
 */
@Composable
private fun WheelItem(
    wheel: WheelConfig,
    isCurrent: Boolean,
    onSelect: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrent) PrimaryRed.copy(alpha = 0.08f) else Background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (!isCurrent) onSelect() }
            .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            BasicTextField(
                value = wheel.title,
                onValueChange = { if (it.length <= 20) onTitleChange(it) },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (wheel.title.isBlank()) {
                            Text(
                                text = "未命名转盘",
                                fontSize = 16.sp,
                                color = OnSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${wheel.options.size} 个选项",
                fontSize = 12.sp,
                color = OnSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isCurrent) {
                Text(
                    text = "当前",
                    fontSize = 11.sp,
                    color = PrimaryRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(PrimaryRed.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除转盘",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
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
            .background(OnSurfaceVariant.copy(alpha = 0.3f))
    )
}
