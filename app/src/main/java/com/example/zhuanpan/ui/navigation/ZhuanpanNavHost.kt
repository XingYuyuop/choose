package com.example.zhuanpan.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.zhuanpan.ZhuanpanApplication
import com.example.zhuanpan.ui.batch_edit.BatchEditScreen
import com.example.zhuanpan.ui.create_wheel.CreateWheelScreen
import com.example.zhuanpan.ui.edit.EditScreen
import com.example.zhuanpan.ui.edit.EditViewModel
import com.example.zhuanpan.ui.history.HistoryScreen
import com.example.zhuanpan.ui.home.HomeScreen
import com.example.zhuanpan.ui.wheel_list.WheelListScreen

/** 统一转场动画时长（毫秒），控制在 0.3~0.5 秒之间。 */
private const val ANIM_DURATION_MS = 400

/**
 * 应用导航宿主。
 *
 * 所有子页面统一使用从下到上的滑入动画，确保转盘列表页、历史记录页、编辑页
 * 的动画风格、过渡速度和视觉效果保持一致。
 *
 * @param navController 导航控制器
 * @param startDestination 起始路由
 * @param modifier 修饰符
 */
@Composable
fun ZhuanpanNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ZhuanpanApplication

    // 在导航宿主层级创建共享的 EditViewModel，供编辑页与批量编辑页共用
    val editViewModel: EditViewModel = viewModel(
        factory = EditViewModel.provideFactory(
            wheelRepository = application.wheelRepository
        )
    )

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(ANIM_DURATION_MS)
            ) + fadeIn(animationSpec = tween(ANIM_DURATION_MS))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION_MS))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION_MS))
        },
        popExitTransition = {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(ANIM_DURATION_MS)
            ) + fadeOut(animationSpec = tween(ANIM_DURATION_MS))
        }
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToEdit = {
                    navController.navigate("edit")
                },
                onNavigateToWheelList = {
                    navController.navigate(Screen.WheelList.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToCreateWheel = {
                    navController.navigate(Screen.CreateWheel.route)
                }
            )
        }

        composable(route = Screen.WheelList.route) {
            // 列表页右上角"+"直接进入编辑页新建模式（跳过中转）
            WheelListScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate("edit")
                },
                onNavigateToCreateWheel = {
                    navController.navigate("edit?isNew=true")
                }
            )
        }

        composable(route = Screen.History.route) {
            HistoryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("isNew") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isNew = backStackEntry.arguments?.getBoolean("isNew") ?: false
            EditScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToBatchEdit = {
                    navController.navigate(Screen.BatchEdit.route)
                },
                isNew = isNew,
                viewModel = editViewModel
            )
        }

        composable(route = Screen.BatchEdit.route) {
            val editState by editViewModel.uiState.collectAsState()
            BatchEditScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    if (editState.isNew) {
                        // 新建模式保存成功：清除新建路由，跳转到编辑路由（显示刚创建的轮盘）
                        navController.popBackStack(Screen.Edit.route, inclusive = true)
                        navController.navigate("edit")
                    } else {
                        // 编辑模式保存成功：返回 EditScreen
                        navController.popBackStack()
                    }
                },
                viewModel = editViewModel
            )
        }

        composable(route = Screen.CreateWheel.route) {
            // "自定义"入口跳转到编辑页新建模式
            CreateWheelScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToCustomEdit = {
                    navController.navigate("edit?isNew=true")
                }
            )
        }
    }
}
