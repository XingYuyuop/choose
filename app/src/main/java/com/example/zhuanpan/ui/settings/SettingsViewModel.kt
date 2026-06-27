package com.example.zhuanpan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zhuanpan.data.model.AppSettings
import com.example.zhuanpan.data.model.ColorScheme
import com.example.zhuanpan.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 设置面板 ViewModel。
 *
 * @property settingsRepository 应用设置仓库
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        settingsRepository.settings
            .onEach { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 切换允许重复抽取开关。
     */
    fun onAllowRepeatChanged(allow: Boolean) {
        updateSettings { it.copy(allowRepeat = allow) }
    }

    /**
     * 调整旋转时长。
     *
     * @param increment 是否为增加，false 表示减少
     */
    fun onSpinDurationChanged(increment: Boolean) {
        updateSettings { current ->
            val delta = if (increment) AppSettings.SPIN_DURATION_STEP_MS else -AppSettings.SPIN_DURATION_STEP_MS
            val newDuration = (current.spinDurationMs + delta)
                .coerceIn(AppSettings.MIN_SPIN_DURATION_MS, AppSettings.MAX_SPIN_DURATION_MS)
            current.copy(spinDurationMs = newDuration)
        }
    }

    /**
     * 切换转盘配色方案。
     */
    fun onColorSchemeChanged(scheme: ColorScheme) {
        updateSettings { it.copy(colorSchemeName = scheme.name) }
    }

    /**
     * 调整结果字体大小。
     */
    fun onFontSizeChanged(size: Float) {
        updateSettings {
            it.copy(resultFontSizeSp = size.coerceIn(AppSettings.MIN_FONT_SIZE, AppSettings.MAX_FONT_SIZE))
        }
    }

    /**
     * 调整轮盘大小。
     */
    fun onWheelSizeChanged(size: Float) {
        updateSettings {
            it.copy(wheelSize = size.coerceIn(AppSettings.MIN_WHEEL_SIZE, AppSettings.MAX_WHEEL_SIZE))
        }
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(transform)
        }
    }

    companion object {
        /**
         * 创建带依赖注入的 [SettingsViewModel] Factory。
         */
        fun provideFactory(
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsRepository) as T
            }
        }
    }
}
