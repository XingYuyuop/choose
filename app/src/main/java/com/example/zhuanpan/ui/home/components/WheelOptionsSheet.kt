package com.example.zhuanpan.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhuanpan.data.model.WheelOption
import com.example.zhuanpan.ui.theme.Background
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.Divider
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed

/**
 * 转盘选项管理底部弹窗。
 *
 * @param wheelTitle 当前转盘名称
 * @param wheelId 当前转盘 ID
 * @param options 当前选项列表
 * @param onWheelTitleChanged 转盘名称修改回调
 * @param onOptionSelected 选项被点击（切换）回调
 * @param onOptionLabelChanged 选项名称变化回调
 * @param onOptionRemoved 删除选项回调
 * @param onOptionAdded 添加选项回调
 * @param onDismiss 关闭弹窗回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelOptionsSheet(
    wheelTitle: String,
    wheelId: String,
    options: List<WheelOption>,
    onWheelTitleChanged: (String) -> Unit,
    onOptionSelected: (String) -> Unit,
    onOptionLabelChanged: (String, String) -> Unit,
    onOptionRemoved: (String) -> Unit,
    onOptionAdded: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var newOptionName by remember { mutableStateOf("") }
    var optionToDelete by remember { mutableStateOf<WheelOption?>(null) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var titleText by remember(wheelId) { mutableStateOf(wheelTitle) }

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
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "管理选项",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 转盘名称（点击可编辑）
            if (isEditingTitle) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BasicTextField(
                        value = titleText,
                        onValueChange = {
                            if (it.length <= 20) titleText = it
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Background)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        decorationBox = { innerTextField ->
                            Box {
                                if (titleText.isBlank()) {
                                    Text(
                                        text = "输入转盘名称",
                                        fontSize = 16.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    TextButton(
                        onClick = {
                            if (titleText.trim().isNotEmpty()) {
                                onWheelTitleChanged(titleText.trim())
                            }
                            isEditingTitle = false
                        }
                    ) {
                        Text("完成", color = PrimaryRed, fontSize = 14.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isEditingTitle = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = wheelTitle.ifBlank { "未命名转盘" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (wheelTitle.isBlank()) OnSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "修改名称",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(16.dp))

            if (options.isEmpty()) {
                EmptyOptionsState(
                    onAddClick = {
                        onOptionAdded("新选项")
                        newOptionName = ""
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(options, key = { it.id }) { option ->
                        OptionManageItem(
                            option = option,
                            onClick = { onOptionSelected(option.id) },
                            onLabelChange = { onOptionLabelChanged(option.id, it) },
                            onRemove = { optionToDelete = option }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // 添加新选项输入区
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = newOptionName,
                    onValueChange = {
                        if (it.length <= 20) newOptionName = it
                    },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Background)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (newOptionName.isBlank()) {
                                Text(
                                    text = "输入选项名称",
                                    fontSize = 16.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = {
                        if (newOptionName.trim().isNotEmpty()) {
                            onOptionAdded(newOptionName.trim())
                            newOptionName = ""
                        }
                    },
                    enabled = newOptionName.trim().isNotEmpty(),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (newOptionName.trim().isNotEmpty()) PrimaryRed else OnSurfaceVariant.copy(
                                    alpha = 0.3f
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加选项",
                            tint = ColorWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Text(
                text = "${newOptionName.length}/20",
                fontSize = 12.sp,
                color = OnSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End
            )
        }
    }

    // 删除确认对话框
    optionToDelete?.let { option ->
        AlertDialog(
            onDismissRequest = { optionToDelete = null },
            title = { Text("删除选项") },
            text = { Text("确定要删除“${option.label}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onOptionRemoved(option.id)
                        optionToDelete = null
                    }
                ) {
                    Text("删除", color = PrimaryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { optionToDelete = null }) {
                    Text("取消", color = OnSurfaceVariant)
                }
            },
            containerColor = ColorWhite
        )
    }
}

/**
 * 空状态提示。
 */
@Composable
private fun EmptyOptionsState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "还没有选项",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击下方按钮添加第一个选项",
            fontSize = 14.sp,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(PrimaryRed)
                .clickable { onAddClick() }
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = ColorWhite,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "添加选项",
                fontSize = 15.sp,
                color = ColorWhite,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 选项管理行。
 */
@Composable
private fun OptionManageItem(
    option: WheelOption,
    onClick: () -> Unit,
    onLabelChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Background)
            .clickable { onClick() }
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BasicTextField(
            value = option.label,
            onValueChange = {
                if (it.length <= 20) onLabelChange(it)
            },
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "删除选项",
                tint = PrimaryRed,
                modifier = Modifier.size(18.dp)
            )
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
            .background(Divider)
    )
}
