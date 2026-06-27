package com.example.zhuanpan.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

// 浅色主题配色
private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = ColorWhite,
    secondary = WheelLightBlue,
    onSecondary = OnSurface,
    tertiary = WheelPink,
    onTertiary = OnSurface,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Background,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Divider
)

// 深色主题配色（首版与浅色保持一致，避免转盘颜色失真）
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRed,
    onPrimary = ColorWhite,
    secondary = WheelLightBlue,
    onSecondary = OnSurface,
    tertiary = WheelPink,
    onTertiary = OnSurface,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Background,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Divider
)

@Composable
fun ZhuanpanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 关闭动态取色，确保转盘配色在不同机型上保持一致
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowInsetsControllerCompat(window, view)
                    .isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
