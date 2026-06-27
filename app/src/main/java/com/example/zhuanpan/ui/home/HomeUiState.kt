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
 * @property multiSpinTotal 批量旋转总次数（0 表示非批量模式）
 * @property multiSpinCurrent 当前已完成的批量旋转次数
 * @property multiSpinResults 批量旋转结果列表
 * @property showMultiSpinPicker 是否显示批量旋转次数选择弹窗
 * @property showMultiSpinModePicker 是否显示旋转模式选择弹窗
 * @property pendingMultiSpinCount 待确认的批量旋转次数
 * @property showMultiSpinResults 是否显示批量旋转结果弹窗
 * @property multiSpinResultMode 结果显示模式：0=逐条显示，1=合并显示
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
    val errorMessage: String? = null,
    val multiSpinTotal: Int = 0,
    val multiSpinCurrent: Int = 0,
    val multiSpinResults: List<String> = emptyList(),
    val showMultiSpinPicker: Boolean = false,
    val showMultiSpinModePicker: Boolean = false,
    val pendingMultiSpinCount: Int = 0,
    val showMultiSpinResults: Boolean = false,
    val multiSpinResultMode: Int = 0
)
