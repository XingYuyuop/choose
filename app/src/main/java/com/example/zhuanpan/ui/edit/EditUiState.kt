package com.example.zhuanpan.ui.edit

import com.example.zhuanpan.data.model.WheelConfig

/**
 * 编辑转盘页面 UI 状态。
 *
 * @property config 当前编辑中的转盘配置
 * @property originalConfig 进入页面时的原始配置，用于判断是否有变更
 * @property showUnsavedDialog 是否显示未保存确认对话框
 */
data class EditUiState(
    val config: WheelConfig = WheelConfig(),
    val originalConfig: WheelConfig = WheelConfig(),
    val showUnsavedDialog: Boolean = false
)
