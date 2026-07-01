package com.example.zhuanpan.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zhuanpan.data.model.WheelConfig
import com.example.zhuanpan.data.model.WheelOption
import com.example.zhuanpan.data.repository.WheelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 编辑转盘页面 ViewModel。
 *
 * 在内存中维护一份可编辑的转盘配置副本，保存时才写入仓库。
 * 同时支持编辑已有转盘（[reload]）与新建转盘（[startNewWheel]）两种模式，
 * 供编辑页与批量编辑页共享同一份内存状态。
 *
 * @property wheelRepository 转盘配置仓库
 */
class EditViewModel(
    private val wheelRepository: WheelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    /**
     * 从仓库重新加载当前转盘配置（编辑模式）。
     *
     * 每次进入编辑页时调用，确保 ViewModel 内存状态与仓库中当前选中的转盘保持同步。
     */
    fun reload() {
        viewModelScope.launch {
            val config = wheelRepository.wheelConfig.first()
            _uiState.value = EditUiState(
                config = config,
                originalConfig = config,
                isNew = false
            )
        }
    }

    /**
     * 进入新建模式：使用空白草稿，保存时通过 createWheel 创建新转盘。
     */
    fun startNewWheel() {
        val blank = WheelConfig(title = "", options = emptyList())
        _uiState.value = EditUiState(
            config = blank,
            originalConfig = blank,
            isNew = true
        )
    }

    /**
     * 判断是否有未保存变更。
     *
     * 新建模式：标题或选项非空即视为已有内容；编辑模式：与原始配置不一致。
     */
    fun hasChanges(): Boolean {
        val state = _uiState.value
        return if (state.isNew) {
            state.config.title.isNotBlank() || state.config.options.isNotEmpty()
        } else {
            state.config != state.originalConfig
        }
    }

    /**
     * 更新转盘标题。
     */
    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(config = it.config.copy(title = title)) }
    }

    /**
     * 更新指定选项的文本。
     */
    fun onOptionLabelChanged(optionId: String, label: String) {
        updateOption(optionId) { it.copy(label = label) }
    }

    /**
     * 更新指定选项的权重。
     */
    fun onOptionWeightChanged(optionId: String, weight: Int) {
        updateOption(optionId) { it.copy(weight = weight.coerceAtLeast(0)) }
    }

    /**
     * 删除指定选项。
     */
    fun onOptionRemoved(optionId: String) {
        _uiState.update { current ->
            current.copy(
                config = current.config.copy(
                    options = current.config.options.filter { it.id != optionId }
                )
            )
        }
    }

    /**
     * 添加新选项。
     */
    fun onOptionAdded() {
        _uiState.update { current ->
            val newOption = WheelOption(
                id = UUID.randomUUID().toString(),
                label = "新选项",
                weight = 1
            )
            current.copy(
                config = current.config.copy(
                    options = current.config.options + newOption
                )
            )
        }
    }

    /**
     * 保存当前配置并返回是否成功。
     *
     * 新建模式：强制校验选项数量不少于 [MIN_OPTIONS_TO_SAVE]，通过后调用 createWheel；
     * 编辑模式：直接调用 saveWheelConfig 更新当前转盘。
     *
     * @param onSaved 保存成功回调
     * @param onError 校验失败回调，携带错误提示
     */
    fun saveConfig(onSaved: () -> Unit, onError: (String) -> Unit = {}) {
        val state = _uiState.value
        if (state.isNew) {
            if (state.config.options.size < MIN_OPTIONS_TO_SAVE) {
                onError("至少添加 $MIN_OPTIONS_TO_SAVE 个选项才能保存")
                return
            }
            viewModelScope.launch {
                wheelRepository.createWheel(state.config.title, state.config.options)
                // 新建模式保存成功后清除草稿，防止残留数据导致返回时弹出未保存对话框
                _uiState.value = EditUiState()
                onSaved()
            }
        } else {
            viewModelScope.launch {
                wheelRepository.saveWheelConfig(state.config)
                // 更新原始配置快照，使 hasChanges() 正确返回 false
                _uiState.update { it.copy(originalConfig = it.config) }
                onSaved()
            }
        }
    }

    /**
     * 丢弃当前编辑，直接返回。
     */
    fun discardChanges() {
        // 无需持久化操作，UI 层直接返回即可
    }

    /**
     * 使用选项文本列表替换当前所有选项。
     *
     * 调用方应已完成文本校验（非空、长度等）。每个选项分配新的唯一 ID，
     * 权重默认设为 1。
     *
     * @param labels 选项文本列表
     */
    fun applyBatchOptions(labels: List<String>) {
        _uiState.update { current ->
            val newOptions = labels.map { label ->
                WheelOption(id = UUID.randomUUID().toString(), label = label, weight = 1)
            }
            current.copy(config = current.config.copy(options = newOptions))
        }
    }

    /**
     * 显示/隐藏未保存变更确认对话框。
     */
    fun setUnsavedDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showUnsavedDialog = visible) }
    }

    /**
     * 设置/清除校验错误提示。
     */
    fun setErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun updateOption(optionId: String, transform: (WheelOption) -> WheelOption) {
        _uiState.update { current ->
            current.copy(
                config = current.config.copy(
                    options = current.config.options.map { option ->
                        if (option.id == optionId) transform(option) else option
                    }
                )
            )
        }
    }

    companion object {
        /** 保存所需的最少选项数量。 */
        const val MIN_OPTIONS_TO_SAVE = 2

        /**
         * 创建带依赖注入的 [EditViewModel] Factory。
         */
        fun provideFactory(
            wheelRepository: WheelRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditViewModel(wheelRepository) as T
            }
        }
    }
}
