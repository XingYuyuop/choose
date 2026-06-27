package com.example.zhuanpan.ui.wheel_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zhuanpan.data.model.WheelConfig
import com.example.zhuanpan.data.repository.WheelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 转盘列表页 ViewModel。
 *
 * 负责展示全部转盘、选中切换、编辑标题、复制与删除。
 *
 * @property wheelRepository 转盘配置仓库
 */
class WheelListViewModel(
    private val wheelRepository: WheelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WheelListUiState())
    val uiState: StateFlow<WheelListUiState> = _uiState.asStateFlow()

    init {
        combine(
            wheelRepository.wheels,
            wheelRepository.wheelConfig
        ) { wheels, config ->
            _uiState.update { it.copy(wheels = wheels, currentWheelId = config.id) }
        }.launchIn(viewModelScope)
    }

    /**
     * 选中指定转盘并作为当前转盘，完成后执行 [onDone]。
     */
    fun selectWheel(id: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            wheelRepository.selectWheel(id)
            onDone?.invoke()
        }
    }

    /**
     * 更新指定转盘标题。
     */
    fun updateTitle(id: String, title: String) {
        if (title.length > 20) return
        viewModelScope.launch {
            val target = wheelRepository.wheels.first().find { it.id == id } ?: return@launch
            wheelRepository.saveWheelConfig(target.copy(title = title))
        }
    }

    /**
     * 复制指定转盘。
     */
    fun copyWheel(id: String) {
        viewModelScope.launch {
            wheelRepository.copyWheel(id)
        }
    }

    /**
     * 删除指定转盘。
     */
    fun deleteWheel(id: String) {
        viewModelScope.launch {
            wheelRepository.deleteWheel(id)
        }
    }

    /**
     * 设置待删除的转盘 ID。
     */
    fun setPendingDelete(id: String?) {
        _uiState.update { it.copy(pendingDeleteId = id) }
    }

    /**
     * 新建一个空转盘并自动选中，完成后通过 [onCreated] 通知 UI 跳转编辑页。
     */
    fun addAndEditWheel(onCreated: () -> Unit) {
        viewModelScope.launch {
            wheelRepository.createWheel()
            onCreated()
        }
    }

    companion object {
        /**
         * 创建带依赖注入的 [WheelListViewModel] Factory。
         */
        fun provideFactory(
            wheelRepository: WheelRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WheelListViewModel(wheelRepository) as T
            }
        }
    }
}

/**
 * 转盘列表页 UI 状态。
 *
 * @property wheels 全部转盘列表
 * @property pendingDeleteId 待删除的转盘 ID
 */
data class WheelListUiState(
    val wheels: List<WheelConfig> = emptyList(),
    val currentWheelId: String = "",
    val pendingDeleteId: String? = null
)
