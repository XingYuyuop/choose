package com.example.zhuanpan.ui.home

import com.example.zhuanpan.data.model.AppSettings
import com.example.zhuanpan.data.model.WheelConfig

/**
 * 大转盘首页 UI 状态。
 *
 * @property wheelConfig 当前转盘配置
 * @property settings 当前应用设置
 * @property currentResult 当前中奖结果
 * @property isSpinning 是否正在旋转
 * @property showSettings 是否显示设置弹窗
 * @property showMoreMenu 是否显示更多菜单
 * @property errorMessage 错误提示
 */
data class HomeUiState(
    val wheelConfig: WheelConfig = WheelConfig(),
    val settings: AppSettings = AppSettings(),
    val currentResult: String = "",
    val isSpinning: Boolean = false,
    val showSettings: Boolean = false,
    val showMoreMenu: Boolean = false,
    val showHistory: Boolean = false,
    val showOptions: Boolean = false,
    val showWheelList: Boolean = false,
    val drawnOptionIds: Set<String> = emptySet(),
    val errorMessage: String? = null
)
