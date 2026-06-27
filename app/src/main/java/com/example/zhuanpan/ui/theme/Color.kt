package com.example.zhuanpan.ui.theme

import androidx.compose.ui.graphics.Color

// 通用颜色
val ColorWhite = Color(0xFFFFFFFF)

// 应用主色调
val PrimaryRed = Color(0xFFFF4D4F)         // 激活态红色、旋转按钮文字
val PrimaryRedLight = Color(0xFFFF7875)    // 按钮按下态
val Background = Color(0xFFF5F6FA)         // 页面背景
val Surface = Color(0xFFFFFFFF)            // 卡片、弹窗表面
val OnSurface = Color(0xFF1F1F1F)          // 主要文字
val OnSurfaceVariant = Color(0xFF8C8C8C)   // 次要文字
val Divider = Color(0xFFE8E8E8)            // 分割线

// 转盘配色方案（截图中的 pastel 色系）
val WheelCyan = Color(0xFF00BCD4)          // 青色
val WheelLightBlue = Color(0xFFBBDEFB)     // 浅蓝
val WheelPink = Color(0xFFFFCDD2)          // 粉色
val WheelYellow = Color(0xFFFFECB3)        // 黄色
val WheelLightGreen = Color(0xFFDCEDC8)    // 浅绿
val WheelMint = Color(0xFFB2DFDB)          // 薄荷

// 默认转盘配色列表
val DefaultWheelColors = listOf(
    WheelCyan,
    WheelLightBlue,
    WheelPink,
    WheelYellow,
    WheelLightGreen,
    WheelMint
)
