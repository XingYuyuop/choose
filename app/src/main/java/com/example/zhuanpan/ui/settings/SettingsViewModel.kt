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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 设置面板 ViewModel。
 *
 * 采用乐观更新策略：点击 +/- 时立即更新本地 UI 状态，再异步持久化到 DataStore，
 * 避免每次点击都等待 DataStore 读写完成，确保按钮可连续快速点击无延迟。
 *
 * @property settingsRepository 应用设置仓库
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // 仅在初始化时从仓库加载一次，后续更新由本地乐观更新驱动，避免 DataStore 回写覆盖乐观值
        viewModelScope.launch {
            val initial = settingsRepository.settings.first()
            _uiState.value = SettingsUiState(initial)
        }
    }

    /**
     * 乐观更新设置：立即更新本地状态并异步持久化。
     */
    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val newSettings = transform(_uiState.value.settings)
        _uiState.value = SettingsUiState(newSettings)
        viewModelScope.launch {
            settingsRepository.saveSettings(newSettings)
        }
    }

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

    fun onColorSchemeChanged(scheme: ColorScheme) {
        updateSettings { it.copy(colorSchemeName = scheme.name) }
    }

    /**
     * 调整结果字体大小。
     */
    fun onFontSizeChanged(increment: Boolean) {
        updateSettings {
            val delta = if (increment) AppSettings.FONT_SIZE_STEP else -AppSettings.FONT_SIZE_STEP
            it.copy(
                resultFontSizeSp = (it.resultFontSizeSp + delta)
                    .coerceIn(AppSettings.MIN_FONT_SIZE, AppSettings.MAX_FONT_SIZE)
            )
        }
    }

    fun onResetFontSize() {
        updateSettings { it.copy(resultFontSizeSp = AppSettings.DEFAULT_FONT_SIZE) }
    }

    /**
     * 调整轮盘大小。
     */
    fun onWheelSizeChanged(increment: Boolean) {
        updateSettings {
            val delta = if (increment) AppSettings.WHEEL_SIZE_STEP else -AppSettings.WHEEL_SIZE_STEP
            it.copy(
                wheelSize = (it.wheelSize + delta)
                    .coerceIn(AppSettings.MIN_WHEEL_SIZE, AppSettings.MAX_WHEEL_SIZE)
            )
        }
    }

    fun onResetWheelSize() {
        updateSettings { it.copy(wheelSize = AppSettings.DEFAULT_WHEEL_SIZE) }
    }

    /**
     * 调整转盘选项文字大小。
     */
    fun onOptionFontSizeChanged(increment: Boolean) {
        updateSettings {
            val delta = if (increment) AppSettings.OPTION_FONT_SIZE_STEP else -AppSettings.OPTION_FONT_SIZE_STEP
            it.copy(
                optionFontSizeSp = (it.optionFontSizeSp + delta)
                    .coerceIn(AppSettings.MIN_OPTION_FONT_SIZE, AppSettings.MAX_OPTION_FONT_SIZE)
            )
        }
    }

    fun onResetOptionFontSize() {
        updateSettings { it.copy(optionFontSizeSp = AppSettings.DEFAULT_OPTION_FONT_SIZE) }
    }

    fun onSoundEnabledChanged(enabled: Boolean) {
        updateSettings { it.copy(soundEnabled = enabled) }
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
