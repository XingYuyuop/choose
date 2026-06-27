package com.example.zhuanpan.ui.wheel_list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhuanpan.ZhuanpanApplication
import com.example.zhuanpan.data.model.WheelConfig
import com.example.zhuanpan.ui.theme.Background
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed

/**
 * 转盘列表页面。
 *
 * 以新页面形式展示全部转盘，支持选中切换、编辑、复制、删除。
 *
 * @param onBack 返回回调
 * @param onNavigateToEdit 跳转编辑页回调
 * @param viewModel 转盘列表 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelListScreen(
    onBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: WheelListViewModel = viewModel(
        factory = WheelListViewModel.provideFactory(
            wheelRepository = (LocalContext.current.applicationContext as ZhuanpanApplication).wheelRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val wheels = uiState.wheels
    val pendingDeleteId = uiState.pendingDeleteId

    // 删除确认弹窗
    pendingDeleteId?.let { id ->
        val wheel = wheels.find { it.id == id }
        wheel?.let {
            DeleteConfirmDialog(
                title = wheel.title.ifBlank { "未命名转盘" },
                onConfirm = {
                    viewModel.deleteWheel(id)
                    viewModel.setPendingDelete(null)
                },
                onDismiss = { viewModel.setPendingDelete(null) }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "转盘列表",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // 新建一个空转盘并进入编辑页
                            viewModel.addAndEditWheel(onCreated = { onNavigateToEdit() })
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "新建转盘",
                            tint = OnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(wheels, key = { it.id }) { wheel ->
                WheelListItem(
                    wheel = wheel,
                    isSelected = wheel.id == uiState.currentWheelId,
                    onClick = {
                        viewModel.selectWheel(wheel.id) { onBack() }
                    },
                    onEditClick = {
                        viewModel.selectWheel(wheel.id) { onNavigateToEdit() }
                    },
                    onCopyClick = { viewModel.copyWheel(wheel.id) },
                    onDeleteClick = { viewModel.setPendingDelete(wheel.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * 单个转盘列表项。
 */
@Composable
private fun WheelListItem(
    wheel: WheelConfig,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorWhite)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) PrimaryRed else ColorWhite,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = wheel.title.ifBlank { "未命名转盘" },
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${wheel.options.size}个选项",
                fontSize = 13.sp,
                color = OnSurfaceVariant
            )

            // 右下角操作按钮组
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "编辑",
                    onClick = onEditClick
                )
                ActionIconButton(
                    icon = Icons.Outlined.ContentCopy,
                    contentDescription = "复制",
                    onClick = onCopyClick
                )
                ActionIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "删除",
                    onClick = onDeleteClick
                )
            }
        }
    }
}

/**
 * 带按压动画的操作图标按钮。
 */
@Composable
private fun ActionIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        label = "ActionIconButtonScale"
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .size(36.dp)
            .scale(scale)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isPressed) PrimaryRed else OnSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * 删除确认对话框。
 */
@Composable
private fun DeleteConfirmDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除转盘") },
        text = { Text("确定要删除“${title}”吗？") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", color = PrimaryRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = OnSurfaceVariant)
            }
        },
        containerColor = ColorWhite
    )
}
