package com.example.zhuanpan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.zhuanpan.ui.navigation.ZhuanpanNavHost
import com.example.zhuanpan.ui.theme.ZhuanpanTheme

/**
 * 应用主 Activity。
 *
 * 设置 Compose 内容与全局主题，并通过 [ZhuanpanNavHost] 管理页面导航。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZhuanpanTheme {
                ZhuanpanNavHost()
            }
        }
    }
}
