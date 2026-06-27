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
 *
 * @property wheelRepository 转盘配置仓库
 */
class EditViewModel(
    private val wheelRepository: WheelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    init {
        reload()
    }

    /**
     * 从仓库重新加载当前转盘配置。
     *
     * 每次进入编辑页时调用，确保 ViewModel 内存状态与仓库中当前选中的转盘保持同步，
     * 避免新建轮盘后仍展示旧轮盘的选项。
     */
    fun reload() {
        viewModelScope.launch {
            val config = wheelRepository.wheelConfig.first()
            _uiState.value = EditUiState(config = config, originalConfig = config)
        }
    }

    /**
     * 判断当前配置是否与原始配置有差异。
     */
    fun hasChanges(): Boolean {
        val state = _uiState.value
        return state.config != state.originalConfig
    }

    /**
     * 更新转盘标题。
     */
    fun onTitleChanged(title: String) {
        _uiState.update { current ->
            current.copy(config = current.config.copy(title = title))
        }
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
     */
    fun saveConfig(onSaved: () -> Unit) {
        viewModelScope.launch {
            wheelRepository.saveWheelConfig(_uiState.value.config)
            onSaved()
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
