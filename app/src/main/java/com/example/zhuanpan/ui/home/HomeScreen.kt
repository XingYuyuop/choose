package com.example.zhuanpan.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhuanpan.ZhuanpanApplication
import com.example.zhuanpan.data.model.AppSettings
import com.example.zhuanpan.data.model.WheelConfig
import com.example.zhuanpan.ui.random.RandomNumberScreen
import com.example.zhuanpan.ui.home.components.SpinButton
import com.example.zhuanpan.ui.home.components.WheelComponent
import com.example.zhuanpan.ui.home.components.WheelOptionsSheet
import com.example.zhuanpan.ui.backup.BackupRestoreBottomSheet
import com.example.zhuanpan.ui.settings.SettingsBottomSheet
import com.example.zhuanpan.ui.theme.Background
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.OnSurface
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed
import com.example.zhuanpan.utils.WheelMath
import kotlinx.coroutines.launch

/**
 * 大转盘首页。
 *
 * 通过 [HorizontalPager] 将"大转盘 / 随机数"两个功能模块整合在同一页面，
 * 支持底部 Tab 切换与左右滑动手势切换，并显示当前模块指示器。
 *
 * @param onNavigateToEdit 跳转编辑页回调
 * @param onNavigateToWheelList 跳转转盘列表页回调
 * @param onNavigateToHistory 跳转历史记录页回调
 * @param viewModel 首页 ViewModel
 */
@Composable
fun HomeScreen(
    onNavigateToEdit: () -> Unit,
    onNavigateToWheelList: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCreateWheel: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(
            wheelRepository = (LocalContext.current.applicationContext as ZhuanpanApplication).wheelRepository,
            settingsRepository = (LocalContext.current.applicationContext as ZhuanpanApplication).settingsRepository,
            historyRepository = (LocalContext.current.applicationContext as ZhuanpanApplication).historyRepository,
            soundManager = (LocalContext.current.applicationContext as ZhuanpanApplication).soundManager
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val rotationDegrees by viewModel.rotationDegrees.collectAsState()
    val context = LocalContext.current

    // 分页器：0=大转盘, 1=随机数
    val pagerState = rememberPagerState(pageCount = { Tab.entries.size })
    val coroutineScope = rememberCoroutineScope()

    // 实时计算当前指针所指选项
    val config = uiState.wheelConfig
    val realtimeResult = remember(rotationDegrees, config.options) {
        WheelMath.calculateWinner(config.options, rotationDegrees)?.label ?: ""
    }

    // 错误提示
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onErrorDismissed()
        }
    }

    // 设置底部弹窗
    if (uiState.showSettings) {
        SettingsBottomSheet(
            onDismiss = { viewModel.onSettingsVisibilityChanged(false) }
        )
    }

    // 选项管理底部弹窗
    if (uiState.showOptions) {
        WheelOptionsSheet(
            wheelTitle = uiState.wheelConfig.title,
            wheelId = uiState.wheelConfig.id,
            options = uiState.wheelConfig.options,
            onWheelTitleChanged = { newTitle ->
                viewModel.onWheelTitleChanged(uiState.wheelConfig.id, newTitle)
            },
            onOptionSelected = { optionId ->
                viewModel.onOptionsVisibilityChanged(false)
                viewModel.animateToOption(optionId)
            },
            onOptionLabelChanged = { optionId, label ->
                viewModel.onOptionLabelChanged(optionId, label)
            },
            onOptionRemoved = { optionId ->
                viewModel.onOptionRemoved(optionId)
            },
            onOptionAdded = { label ->
                viewModel.onOptionAdded(label)
            },
            onDismiss = { viewModel.onOptionsVisibilityChanged(false) }
        )
    }

    // 备份还原底部弹窗
    var showBackupRestoreSheet by remember { mutableStateOf(false) }
    if (showBackupRestoreSheet) {
        BackupRestoreBottomSheet(
            onDismiss = { showBackupRestoreSheet = false }
        )
    }

    // 批量旋转次数选择弹窗
    if (uiState.showMultiSpinPicker) {
        MultiSpinPickerDialog(
            onConfirm = { count ->
                viewModel.onMultiSpinCountConfirmed(count)
            },
            onDismiss = { viewModel.onMultiSpinPickerVisibilityChanged(false) }
        )
    }

    // 批量旋转模式选择弹窗
    if (uiState.showMultiSpinModePicker) {
        MultiSpinModePickerDialog(
            count = uiState.pendingMultiSpinCount,
            onSimultaneous = {
                viewModel.onMultiSpinModePickerVisibilityChanged(false)
                viewModel.startMultiSpinSimultaneous(uiState.pendingMultiSpinCount)
            },
            onSequential = {
                viewModel.onMultiSpinModePickerVisibilityChanged(false)
                viewModel.startMultiSpin(uiState.pendingMultiSpinCount)
            },
            onDismiss = { viewModel.onMultiSpinModePickerVisibilityChanged(false) }
        )
    }

    // 批量旋转结果弹窗
    if (uiState.showMultiSpinResults) {
        MultiSpinResultsDialog(
            results = uiState.multiSpinResults,
            resultMode = uiState.multiSpinResultMode,
            onModeChanged = { mode -> viewModel.onMultiSpinResultModeChanged(mode) },
            onDismiss = { viewModel.onMultiSpinResultsDismissed() }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        bottomBar = {
            Column {
                PageIndicator(
                    currentPage = pagerState.currentPage,
                    pageCount = Tab.entries.size
                )
                BottomTabBar(
                    selectedTab = Tab.entries[pagerState.currentPage.coerceIn(0, Tab.entries.lastIndex)],
                    onWheelClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(Tab.WHEEL.ordinal) }
                    },
                    onRandomClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(Tab.RANDOM.ordinal) }
                    }
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                Tab.WHEEL.ordinal -> HomeContent(
                    config = uiState.wheelConfig,
                    settings = uiState.settings,
                    isSpinning = uiState.isSpinning,
                    showMoreMenu = uiState.showMoreMenu,
                    rotationDegrees = rotationDegrees,
                    realtimeResult = realtimeResult,
                    currentResult = uiState.currentResult,
                    multiSpinTotal = uiState.multiSpinTotal,
                    multiSpinCurrent = uiState.multiSpinCurrent,
                    onSettingsClick = { viewModel.onSettingsVisibilityChanged(!uiState.showSettings) },
                    onMoreMenuToggle = { viewModel.onMoreMenuVisibilityChanged(it) },
                    onHistoryClick = onNavigateToHistory,
                    onOptionsClick = { viewModel.onOptionsVisibilityChanged(true) },
                    onWheelListClick = onNavigateToWheelList,
                    onCreateWheelClick = onNavigateToCreateWheel,
                    onEditClick = onNavigateToEdit,
                    onSpinClick = { viewModel.startSpin(uiState.settings.spinDurationMs) },
                    onManualRotation = { delta -> viewModel.updateRotation(delta) },
                    onResetWheel = { viewModel.onResetWheel() },
                    onBackupRestoreClick = { showBackupRestoreSheet = true },
                    onMultiSpinClick = { viewModel.onMultiSpinPickerVisibilityChanged(true) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )

                Tab.RANDOM.ordinal -> RandomNumberScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

/**
 * 首页内容区域。
 */
@Composable
private fun HomeContent(
    config: WheelConfig,
    settings: AppSettings,
    isSpinning: Boolean,
    showMoreMenu: Boolean,
    rotationDegrees: Float,
    realtimeResult: String,
    currentResult: String,
    multiSpinTotal: Int,
    multiSpinCurrent: Int,
    onSettingsClick: () -> Unit,
    onMoreMenuToggle: (Boolean) -> Unit,
    onHistoryClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onWheelListClick: () -> Unit,
    onCreateWheelClick: () -> Unit,
    onEditClick: () -> Unit,
    onSpinClick: () -> Unit,
    onManualRotation: (Float) -> Unit,
    onResetWheel: () -> Unit,
    onBackupRestoreClick: () -> Unit,
    onMultiSpinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部工具栏
        HomeTopBar(
            onSettingsClick = onSettingsClick,
            showMoreMenu = showMoreMenu,
            onMoreMenuToggle = onMoreMenuToggle,
            onCreateWheel = onCreateWheelClick,
            onListClick = onWheelListClick,
            onHistoryClick = onHistoryClick,
            onBackupRestoreClick = onBackupRestoreClick
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 标题按钮：点击跳转所有列表页
        TitleButton(
            title = config.title,
            onClick = onWheelListClick
        )

        // 实时结果显示区域（固定高度，避免行数变化导致转盘位置跳动）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (realtimeResult.isNotEmpty()) {
                Text(
                    text = realtimeResult,
                    fontSize = settings.resultFontSizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 转盘区域（支持拖动旋转、中央点击旋转，大小可设置）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (config.options.isEmpty()) {
                EmptyWheelState(
                    onAddClick = onOptionsClick,
                    modifier = Modifier.fillMaxWidth(settings.wheelSize)
                )
            } else {
                WheelComponent(
                    options = config.options,
                    rotationDegrees = rotationDegrees,
                    colorSchemeName = settings.colorSchemeName,
                    highlightLabel = if (!isSpinning) currentResult else null,
                    onManualRotation = onManualRotation,
                    onCenterClick = onSpinClick,
                    modifier = Modifier.fillMaxWidth(settings.wheelSize)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 底部控制按钮
        HomeBottomControls(
            isSpinning = isSpinning,
            hasOptions = config.options.isNotEmpty() && config.options.any { it.weight > 0 },
            multiSpinTotal = multiSpinTotal,
            multiSpinCurrent = multiSpinCurrent,
            onSettingsClick = onSettingsClick,
            onSpinClick = onSpinClick,
            onEditClick = onEditClick,
            onMultiSpinClick = onMultiSpinClick
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 顶部工具栏。
 */
@Composable
private fun HomeTopBar(
    onSettingsClick: () -> Unit,
    showMoreMenu: Boolean,
    onMoreMenuToggle: (Boolean) -> Unit,
    onCreateWheel: () -> Unit,
    onListClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBackupRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左上角透明可点击区域，点击打开设置
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSettingsClick
                )
        )

        Box {
            IconButton(onClick = { onMoreMenuToggle(true) }) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "更多",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }

            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { onMoreMenuToggle(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("新建轮盘") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        onMoreMenuToggle(false)
                        onCreateWheel()
                    }
                )
                DropdownMenuItem(
                    text = { Text("所有列表") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        onMoreMenuToggle(false)
                        onListClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("历史记录") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        onMoreMenuToggle(false)
                        onHistoryClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("备份还原") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        onMoreMenuToggle(false)
                        onBackupRestoreClick()
                    }
                )
            }
        }
    }
}

/**
 * 标题按钮。
 *
 * 点击转盘名称整体跳转到"所有列表"页面。
 *
 * @param title 转盘标题
 * @param onClick 点击回调（跳转所有列表页）
 */
@Composable
private fun TitleButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(ColorWhite)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = "所有列表",
            tint = Color.Black,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title.ifBlank { "点击设置标题" },
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 底部控制按钮区。
 */
@Composable
private fun HomeBottomControls(
    isSpinning: Boolean,
    hasOptions: Boolean,
    multiSpinTotal: Int,
    multiSpinCurrent: Int,
    onSettingsClick: () -> Unit,
    onSpinClick: () -> Unit,
    onEditClick: () -> Unit,
    onMultiSpinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧设置按钮，点击打开设置面板，旋转时禁用
            ControlIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "设置",
                onClick = onSettingsClick,
                enabled = !isSpinning
            )

            // 中间旋转按钮：无选项时禁用
            SpinButton(
                text = when {
                    !hasOptions -> "请先添加选项"
                    isSpinning && multiSpinTotal > 0 -> "旋转中 ${multiSpinCurrent}/${multiSpinTotal}"
                    isSpinning -> "旋转中..."
                    else -> "点击旋转"
                },
                enabled = !isSpinning && hasOptions,
                onClick = onSpinClick
            )

            // 右侧编辑选项按钮，旋转时禁用
            ControlIconButton(
                icon = Icons.Default.Edit,
                contentDescription = "编辑选项",
                onClick = onEditClick,
                enabled = !isSpinning
            )
        }

        // 批量旋转按钮
        TextButton(
            onClick = onMultiSpinClick,
            enabled = !isSpinning && hasOptions
        ) {
            Text(
                text = "旋转N次",
                fontSize = 13.sp,
                color = if (!isSpinning && hasOptions) PrimaryRed else OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 圆形图标按钮。
 */
@Composable
private fun ControlIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(
                elevation = if (isPressed && enabled) 1.dp else 2.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(
                when {
                    !enabled -> Background
                    isPressed -> Background
                    else -> ColorWhite
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = when {
                !enabled -> OnSurfaceVariant.copy(alpha = 0.3f)
                isPressed -> PrimaryRed
                else -> OnSurfaceVariant
            },
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 转盘空状态提示。
 */
@Composable
private fun EmptyWheelState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(ColorWhite)
            .clickable { onAddClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "暂无选项",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "点击添加第一个选项",
                fontSize = 14.sp,
                color = OnSurfaceVariant
            )
        }
    }
}

/**
 * 底部 Tab 枚举。
 */
private enum class Tab { WHEEL, RANDOM }

/**
 * 当前模块指示器：以圆点标识用户所在的功能模块位置。
 */
@Composable
private fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorWhite)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isActive) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (isActive) PrimaryRed else OnSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    }
}

/**
 * 首次启动引导提示对话框。
 *
 * 仅会在用户首次启动应用时展示，任意选项都会标记为已查看。
 */
@Composable
private fun FirstLaunchGuideDialog(
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onPrimaryClick,
        title = {
            Text(
                text = "欢迎使用选择",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "在这里你可以使用大转盘、随机数等功能快速做出决定。点击下方按钮开始体验吧！",
                fontSize = 14.sp,
                color = OnSurface,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onPrimaryClick) {
                Text(
                    text = "开始使用",
                    color = PrimaryRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onSecondaryClick) {
                Text(
                    text = "我知道了",
                    color = OnSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        },
        containerColor = ColorWhite,
        modifier = modifier
    )
}

/**
 * 底部导航栏。
 */
@Composable
private fun BottomTabBar(
    selectedTab: Tab,
    onWheelClick: () -> Unit,
    onRandomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = ColorWhite,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == Tab.WHEEL,
            onClick = onWheelClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = "大转盘",
                    modifier = Modifier.size(26.dp)
                )
            },
            label = { Text("大转盘", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryRed,
                selectedTextColor = PrimaryRed,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == Tab.RANDOM,
            onClick = onRandomClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Numbers,
                    contentDescription = "随机数",
                    modifier = Modifier.size(26.dp)
                )
            },
            label = { Text("随机数", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryRed,
                selectedTextColor = PrimaryRed,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
    }
}

/**
 * 批量旋转次数选择弹窗。
 *
 * 提供快捷选项（3 / 5 / 10 / 20 次）及自定义输入，
 * 用户选择次数后点击"确定"进入模式选择。
 */
@Composable
private fun MultiSpinPickerDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    val parsedCount = inputText.toIntOrNull()
    val isValid = parsedCount != null && parsedCount in 1..100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "批量旋转",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "输入或选择需要旋转的次数（1~100）",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )

                // 快捷选项
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5, 10, 20).forEach { times ->
                        TextButton(
                            onClick = { onConfirm(times) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(PrimaryRed.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = "${times}次",
                                color = PrimaryRed,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // 自定义输入
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { value ->
                        inputText = value.filter { it.isDigit() }
                    },
                    label = { Text("自定义次数", fontSize = 14.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsedCount?.let(onConfirm) },
                enabled = isValid
            ) {
                Text(
                    text = "确定",
                    color = if (isValid) PrimaryRed else OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = OnSurfaceVariant)
            }
        },
        containerColor = ColorWhite,
        modifier = modifier
    )
}

/**
 * 旋转模式选择弹窗。
 *
 * 提供两种模式：同时旋转（快速）和依次旋转（带动画）。
 */
@Composable
private fun MultiSpinModePickerDialog(
    count: Int,
    onSimultaneous: () -> Unit,
    onSequential: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择旋转模式",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "即将旋转 ${count} 次，请选择旋转模式：",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )

                // 同时旋转
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryRed.copy(alpha = 0.08f))
                        .clickable { onSimultaneous() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "同时旋转",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                        Text(
                            text = "快速生成 ${count} 个结果，无需逐次等待",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // 依次旋转
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Background)
                        .clickable { onSequential() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "依次旋转",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "逐次旋转动画，每次记录结果",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = OnSurfaceVariant)
            }
        },
        containerColor = ColorWhite,
        modifier = modifier
    )
}

/**
 * 批量旋转结果展示弹窗。
 *
 * 支持两种显示模式切换：
 * - 模式0（逐条）：显示每次旋转的具体结果
 * - 模式1（合并）：合并相同结果，按次数从多到少排序，显示"名称 xN"
 */
@Composable
private fun MultiSpinResultsDialog(
    results: List<String>,
    resultMode: Int,
    onModeChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "旋转结果",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // 切换显示模式
                TextButton(onClick = {
                    onModeChanged(if (resultMode == 0) 1 else 0)
                }) {
                    Text(
                        text = if (resultMode == 0) "切换为合并" else "切换为逐条",
                        fontSize = 13.sp,
                        color = PrimaryRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                if (resultMode == 0) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(results, key = { index, _ -> index }) { index, result ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        color = ColorWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = result.ifBlank { "—" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurface
                                )
                            }
                        }
                    }
                } else {
                    val merged = results.groupingBy { it }
                        .eachCount()
                        .toList()
                        .sortedByDescending { it.second }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(merged, key = { index, _ -> index }) { index, (name, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        color = ColorWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = name.ifBlank { "—" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2
                                )
                                if (count > 1) {
                                    Text(
                                        text = "x${count}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定", color = PrimaryRed, fontWeight = FontWeight.Medium)
            }
        },
        containerColor = ColorWhite,
        modifier = modifier
    )
}
