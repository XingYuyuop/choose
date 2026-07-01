package com.example.zhuanpan.ui.navigation

/**
 * 应用路由枚举。
 *
 * @property route 导航路由字符串
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Edit : Screen("edit?isNew={isNew}")
    data object WheelList : Screen("wheel_list")
    data object History : Screen("history")
    data object BatchEdit : Screen("batch_edit")
    data object CreateWheel : Screen("create_wheel")
}
