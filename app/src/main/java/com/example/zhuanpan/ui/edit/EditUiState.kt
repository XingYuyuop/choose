package com.example.zhuanpan.ui.edit

import com.example.zhuanpan.data.model.WheelConfig

/**
 * 编辑转盘页面 UI 状态。
 *
 * @property config 当前编辑中的转盘配置
 * @property originalConfig 进入页面时的原始配置，用于判断是否有变更
 * @property showUnsavedDialog 是否显示未保存确认对话框
 * @property isNew 是否为新建模式（true 时保存走 createWheel 并校验选项数量）
 * @property errorMessage 校验错误提示（如选项不足）
 */
data class EditUiState(
    val config: WheelConfig = WheelConfig(),
    val originalConfig: WheelConfig = WheelConfig(),
    val showUnsavedDialog: Boolean = false,
    val isNew: Boolean = false,
    val errorMessage: String? = null
)
