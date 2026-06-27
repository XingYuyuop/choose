package com.example.zhuanpan.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zhuanpan.data.model.SpinHistoryItem
import com.example.zhuanpan.data.model.WheelConfig
import com.example.zhuanpan.data.model.WheelOption
import com.example.zhuanpan.data.repository.HistoryRepository
import com.example.zhuanpan.data.repository.SettingsRepository
import com.example.zhuanpan.data.repository.WheelRepository
import com.example.zhuanpan.utils.WheelMath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

/**
 * 大转盘首页 ViewModel。
 *
 * 负责聚合转盘配置、应用设置与旋转状态，并提供旋转完成后的结果计算。
 *
 * @property wheelRepository 转盘配置仓库
 * @property settingsRepository 应用设置仓库
 */
class HomeViewModel(
    private val wheelRepository: WheelRepository,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** 旋转角度（顺时针为正），动画过程中持续更新。 */
    private val _rotationDegrees = MutableStateFlow(0f)
    val rotationDegrees: StateFlow<Float> = _rotationDegrees.asStateFlow()

    /** 当前动画协程，用于在需要时取消。 */
    private var spinJob: kotlinx.coroutines.Job? = null

    /** 历史记录数据流，直接透传 Repository。 */
    val history: StateFlow<com.example.zhuanpan.data.model.SpinHistory> =
        historyRepository.history
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                initialValue = com.example.zhuanpan.data.model.SpinHistory()
            )

    /** 全部转盘列表数据流，供转盘列表弹窗展示。 */
    val wheels: StateFlow<List<WheelConfig>> =
        wheelRepository.wheels
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        // 首次使用时迁移旧版单转盘数据或创建默认空转盘（幂等）
        viewModelScope.launch {
            wheelRepository.ensureInitialized()
        }

        // 合并转盘配置与设置流，自动更新 UI 状态
        combine(
            wheelRepository.wheelConfig,
            settingsRepository.settings
        ) { config, settings ->
            _uiState.update { current ->
                // 配置发生变更时清空已抽取记录，避免选项 ID 失效或新转盘沿用旧状态
                val shouldClearDrawn = current.wheelConfig.id != config.id ||
                        current.wheelConfig.options.size != config.options.size
                current.copy(
                    wheelConfig = config,
                    settings = settings,
                    drawnOptionIds = if (shouldClearDrawn) emptySet() else current.drawnOptionIds,
                    errorMessage = validateConfig(config)
                )
            }
        }.launchIn(viewModelScope)
    }

    /**
     * 校验转盘配置是否有效。
     */
    private fun validateConfig(config: WheelConfig): String? {
        return when {
            config.options.size < 2 -> "请至少添加两个选项"
            config.options.all { it.weight <= 0 } -> "所有选项权重不能为 0"
            else -> null
        }
    }

    /**
     * 开始旋转，UI 层调用此方法更新状态并禁用按钮。
     */
    fun onSpinStarted() {
        _uiState.update { it.copy(isSpinning = true) }
    }

    /**
     * 启动旋转动画。
     *
     * 使用 viewModelScope 运行动画，即使离开页面也不会取消。
     * 采用 ease-out-cubic 缓动曲线，前期快速旋转后期平滑减速。
     * 每 16ms 更新一次角度（约 60fps）。
     *
     * @param spinDurationMs 旋转时长（毫秒）
     */
    fun startSpin(spinDurationMs: Int) {
        spinJob?.cancel()

        val config = _uiState.value.wheelConfig
        if (config.options.isEmpty() || config.options.all { it.weight <= 0 }) return

        spinJob = viewModelScope.launch {
            onSpinStarted()

            val startRotation = _rotationDegrees.value
            val extraSpins = Random.nextInt(5, 10)
            val randomOffset = Random.nextFloat() * 360f
            val totalDelta = 360f * extraSpins + randomOffset
            val targetRotation = startRotation + totalDelta

            val durationMs = spinDurationMs.toLong()
            val startTime = System.currentTimeMillis()

            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= durationMs) break

                val progress = elapsed.toFloat() / durationMs
                // ease-out-cubic: 1 - (1-t)^3
                val easedProgress = 1f - (1f - progress).let { it * it * it }
                _rotationDegrees.value = startRotation + totalDelta * easedProgress

                kotlinx.coroutines.delay(16)
            }

            _rotationDegrees.value = targetRotation
            onSpinFinished(targetRotation)
        }
    }

    /**
     * 启动选项切换动画（从弹窗选择某个选项时）。
     *
     * 采用 ease-out-quart 缓动曲线，过渡更柔和。
     *
     * @param optionId 目标选项 ID
     */
    fun animateToOption(optionId: String) {
        val config = _uiState.value.wheelConfig
        val currentRotation = _rotationDegrees.value

        val targetRotation = WheelMath.calculateRotationToCenterOption(
            options = config.options,
            optionId = optionId,
            currentRotation = currentRotation
        )

        spinJob?.cancel()

        spinJob = viewModelScope.launch {
            onSpinStarted()

            val startRotation = currentRotation
            val totalDelta = targetRotation - startRotation
            val durationMs = 400L
            val startTime = System.currentTimeMillis()

            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= durationMs) break

                val progress = elapsed.toFloat() / durationMs
                // ease-out-quart: 1 - (1-t)^4
                val easedProgress = 1f - (1f - progress).let { it * it * it * it }
                _rotationDegrees.value = startRotation + totalDelta * easedProgress

                kotlinx.coroutines.delay(16)
            }

            _rotationDegrees.value = targetRotation
            onSpinFinished(targetRotation)
        }
    }

    /**
     * 手动更新旋转角度（拖拽时调用）。
     *
     * @param delta 角度增量（度）
     */
    fun updateRotation(delta: Float) {
        if (!_uiState.value.isSpinning) {
            _rotationDegrees.value += delta
        }
    }

    /**
     * 旋转动画结束时调用，计算并展示中奖结果。
     *
     * 若关闭“允许重复抽取”，会自动排除本轮已中选项；全部抽完后提示并重新开始。
     *
     * @param finalRotation 最终旋转角度（顺时针为正）
     */
    fun onSpinFinished(finalRotation: Float) {
        val config = _uiState.value.wheelConfig
        val settings = _uiState.value.settings
        val drawnIds = _uiState.value.drawnOptionIds

        val (winner, nextDrawnIds, resetMessage) = resolveWinner(
            options = config.options,
            drawnIds = drawnIds,
            allowRepeat = settings.allowRepeat,
            rotation = finalRotation
        )

        _uiState.update { current ->
            current.copy(
                isSpinning = false,
                currentResult = winner?.label ?: "",
                drawnOptionIds = nextDrawnIds,
                errorMessage = resetMessage
            )
        }

        // 保存抽取历史
        winner?.let {
            viewModelScope.launch {
                historyRepository.addRecord(
                    SpinHistoryItem(
                        result = it.label,
                        wheelTitle = config.title
                    )
                )
            }
        }
    }

    /**
     * 解析本次旋转的中奖选项，同时处理“不允许重复抽取”的抽取池逻辑。
     *
     * @return Triple(中奖选项, 更新后的已抽取集合, 是否需要提示用户已重置)
     */
    private fun resolveWinner(
        options: List<WheelOption>,
        drawnIds: Set<String>,
        allowRepeat: Boolean,
        rotation: Float
    ): Triple<WheelOption?, Set<String>, String?> {
        val baseCandidates = options.filter { it.weight > 0 }
        if (baseCandidates.isEmpty()) return Triple(null, emptySet(), null)

        val candidates = if (allowRepeat) {
            baseCandidates
        } else {
            baseCandidates.filter { it.id !in drawnIds }
        }

        return if (candidates.isEmpty()) {
            // 本轮已全部抽完，自动重置并重新开始
            val winner = WheelMath.calculateWinner(baseCandidates, rotation)
            val newDrawnIds = winner?.let { setOf(it.id) } ?: emptySet()
            Triple(winner, newDrawnIds, "所有选项已抽完，重新开始")
        } else {
            val winner = WheelMath.calculateWinner(candidates, rotation)
            val newDrawnIds = winner?.let { drawnIds + it.id } ?: drawnIds
            Triple(winner, newDrawnIds, null)
        }
    }

    /**
     * 设置弹窗显示/隐藏。
     */
    fun onSettingsVisibilityChanged(visible: Boolean) {
        _uiState.update { it.copy(showSettings = visible) }
    }

    /**
     * 更多菜单显示/隐藏。
     */
    fun onMoreMenuVisibilityChanged(visible: Boolean) {
        _uiState.update { it.copy(showMoreMenu = visible) }
    }

    /**
     * 历史记录弹窗显示/隐藏。
     */
    fun onHistoryVisibilityChanged(visible: Boolean) {
        _uiState.update { it.copy(showHistory = visible) }
    }

    /**
     * 清空历史记录。
     */
    fun onClearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    /**
     * 选项管理弹窗显示/隐藏。
     */
    fun onOptionsVisibilityChanged(visible: Boolean) {
        _uiState.update { it.copy(showOptions = visible) }
    }

    /**
     * 转盘列表弹窗显示/隐藏。
     */
    fun onWheelListVisibilityChanged(visible: Boolean) {
        _uiState.update { it.copy(showWheelList = visible) }
    }

    /**
     * 新建转盘：创建一个空转盘并选中它。
     */
    fun onCreateWheel() {
        viewModelScope.launch {
            wheelRepository.createWheel()
        }
    }

    /**
     * 选中指定转盘作为当前转盘。
     */
    fun onSelectWheel(id: String) {
        viewModelScope.launch {
            wheelRepository.selectWheel(id)
        }
    }

    /**
     * 删除指定转盘。
     */
    fun onDeleteWheel(id: String) {
        viewModelScope.launch {
            wheelRepository.deleteWheel(id)
        }
    }

    /**
     * 更新指定转盘标题（支持内联编辑，允许空标题）。
     *
     * @param wheelId 转盘 ID
     * @param title 新标题，长度限制 1~20 字符
     */
    fun onWheelTitleChanged(wheelId: String, title: String) {
        if (title.length > 20) return

        viewModelScope.launch {
            val target = wheelRepository.wheels.first().find { it.id == wheelId } ?: return@launch
            wheelRepository.saveWheelConfig(target.copy(title = title))
        }
    }

    /**
     * 添加新选项。
     *
     * @param label 选项名称，长度限制 1~20 字符
     */
    fun onOptionAdded(label: String) {
        val trimmed = label.trim()
        if (trimmed.isEmpty() || trimmed.length > 20) return

        viewModelScope.launch {
            val config = wheelRepository.wheelConfig.first()
            val newOption = WheelOption(
                id = UUID.randomUUID().toString(),
                label = trimmed,
                weight = 1
            )
            wheelRepository.saveWheelConfig(
                config.copy(options = config.options + newOption)
            )
        }
    }

    /**
     * 更新指定选项名称。
     *
     * @param optionId 选项 ID
     * @param label 新名称，长度限制 1~20 字符
     */
    fun onOptionLabelChanged(optionId: String, label: String) {
        val trimmed = label.trim()
        if (trimmed.isEmpty() || trimmed.length > 20) return

        viewModelScope.launch {
            val config = wheelRepository.wheelConfig.first()
            wheelRepository.saveWheelConfig(
                config.copy(
                    options = config.options.map { option ->
                        if (option.id == optionId) option.copy(label = trimmed) else option
                    }
                )
            )
        }
    }

    /**
     * 删除指定选项。
     *
     * @param optionId 选项 ID
     */
    fun onOptionRemoved(optionId: String) {
        viewModelScope.launch {
            val config = wheelRepository.wheelConfig.first()
            wheelRepository.saveWheelConfig(
                config.copy(
                    options = config.options.filter { it.id != optionId }
                )
            )
        }
    }

    /**
     * 重置转盘为默认配置。
     */
    fun onResetWheel() {
        viewModelScope.launch {
            wheelRepository.resetToDefault()
        }
    }

    /**
     * 清除错误提示。
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 标记首次启动引导提示已查看，后续启动不再显示。
     */
    fun onGuideSeen() {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(hasSeenGuide = true) }
        }
    }

    companion object {
        /**
         * 创建带依赖注入的 [HomeViewModel] Factory。
         */
        fun provideFactory(
            wheelRepository: WheelRepository,
            settingsRepository: SettingsRepository,
            historyRepository: HistoryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(wheelRepository, settingsRepository, historyRepository) as T
            }
        }
    }
}
