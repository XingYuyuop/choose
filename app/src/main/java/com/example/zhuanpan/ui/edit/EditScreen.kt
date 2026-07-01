package com.example.zhuanpan.ui.edit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.GridView
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhuanpan.ZhuanpanApplication
import com.example.zhuanpan.data.model.ColorScheme
import com.example.zhuanpan.ui.edit.components.OptionItem
import com.example.zhuanpan.ui.theme.Background
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed

/**
 * 编辑转盘页面。
 *
 * @param onBack 返回回调
 * @param viewModel 编辑 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    onBack: () -> Unit,
    onNavigateToBatchEdit: () -> Unit,
    isNew: Boolean = false,
    viewModel: EditViewModel = viewModel(
        factory = EditViewModel.provideFactory(
            wheelRepository = (LocalContext.current.applicationContext as ZhuanpanApplication).wheelRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.config

    // 进入页面时根据模式初始化：新建模式用空白草稿，编辑模式加载当前转盘
    LaunchedEffect(isNew) {
        if (isNew) viewModel.startNewWheel() else viewModel.reload()
    }

    // 保存校验错误提示
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.setErrorMessage(null) },
            title = { Text("无法保存", fontWeight = FontWeight.Bold) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.setErrorMessage(null) }) {
                    Text("知道了", color = PrimaryRed)
                }
            },
            containerColor = ColorWhite
        )
    }

    // 未保存变更确认弹窗
    if (uiState.showUnsavedDialog) {
        UnsavedChangesDialog(
            onCancel = { viewModel.setUnsavedDialogVisible(false) },
            onDiscard = {
                viewModel.setUnsavedDialogVisible(false)
                viewModel.discardChanges()
                onBack()
            },
            onSave = {
                viewModel.setUnsavedDialogVisible(false)
                viewModel.saveConfig(
                    onSaved = { onBack() },
                    onError = { viewModel.setErrorMessage(it) }
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isNew) "新建轮盘" else "编辑转盘",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (viewModel.hasChanges()) {
                                viewModel.setUnsavedDialogVisible(true)
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveConfig(
                                onSaved = { onBack() },
                                onError = { viewModel.setErrorMessage(it) }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "保存",
                            tint = PrimaryRed
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
        bottomBar = {
            EditBottomBar(
                optionCount = config.options.size,
                colorSchemeName = config.colorSchemeName,
                onAddClick = { viewModel.onOptionAdded() },
                onBatchEditClick = onNavigateToBatchEdit
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
            item {
                // 问题输入区
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "问题",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    BasicTextField(
                        value = config.title,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorWhite)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) { innerTextField ->
                        Box {
                            if (config.title.isBlank()) {
                                Text(
                                    text = "输入转盘问题",
                                    fontSize = 18.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            }

            item {
                Text(
                    text = "选项",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            items(
                items = config.options,
                key = { it.id }
            ) { option ->
                OptionItem(
                    option = option,
                    onLabelChange = { viewModel.onOptionLabelChanged(option.id, it) },
                    onWeightChange = { viewModel.onOptionWeightChanged(option.id, it) },
                    onRemove = { viewModel.onOptionRemoved(option.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * 编辑页底部工具栏。
 */
@Composable
private fun EditBottomBar(
    optionCount: Int,
    colorSchemeName: String,
    onAddClick: () -> Unit,
    onBatchEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧配色预览与选项数量
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorSchemePreview(colorSchemeName = colorSchemeName)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${optionCount}个选项",
                fontSize = 14.sp,
                color = OnSurfaceVariant
            )
        }

        // 右侧工具按钮：新增选项 + 批量编辑选项
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "新增选项",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }
            IconButton(onClick = onBatchEditClick) {
                Icon(
                    imageVector = Icons.Outlined.GridView,
                    contentDescription = "批量编辑选项",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 未保存变更确认对话框。
 */
@Composable
private fun UnsavedChangesDialog(
    onCancel: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("保存修改？") },
        text = { Text("当前编辑内容与之前有所不同，是否保存本次编辑？") },
        confirmButton = {
            Row {
                TextButton(onClick = onCancel) {
                    Text("取消", color = OnSurfaceVariant)
                }
                TextButton(onClick = onDiscard) {
                    Text("不保存", color = OnSurfaceVariant)
                }
                TextButton(onClick = onSave) {
                    Text("保存", color = PrimaryRed)
                }
            }
        },
        containerColor = ColorWhite
    )
}

/**
 * 配色方案预览小圆盘（多色环形显示）。
 *
 * 将配色方案的所有颜色以等分扇形绘制在圆形画布上，
 * 提供直观的彩色视觉辨识度。
 */
@Composable
private fun ColorSchemePreview(
    colorSchemeName: String,
    modifier: Modifier = Modifier
) {
    val colors = ColorScheme.fromName(colorSchemeName).toComposeColors()
    Canvas(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
    ) {
        val diameter = size.minDimension
        if (colors.isEmpty()) {
            drawCircle(color = Color.Gray)
            return@Canvas
        }
        val sweep = 360f / colors.size
        colors.forEachIndexed { index, color ->
            drawArc(
                color = color,
                startAngle = index * sweep - 90f,
                sweepAngle = sweep,
                useCenter = true
            )
        }
        // 中心留白，形成环形效果，增强视觉辨识度
        drawCircle(
            color = ColorWhite,
            radius = diameter * 0.28f
        )
    }
}
