# ========================================
# 转盘应用 ProGuard / R8 规则
# ========================================

# 保留源文件名和行号信息，便于调试崩溃日志
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========================================
# Kotlin 相关
# ========================================

# Kotlin 元数据
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Kotlin 协程
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin Serialization - 最关键！
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# 保留所有 @Serializable 类及其 Companion 和 serializer
-keep @kotlinx.serialization.Serializable class * {
    *;
}
-keepclassmembers class * {
    static *** Companion;
}
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# 保留 serializer 生成的类
-keep class *$$serializer { *; }
-keepclassmembers class *$$serializer {
    *;
}

# ========================================
# Compose 相关
# ========================================

# 保留所有 Composable 函数
-keep class * {
    @androidx.compose.runtime.Composable <methods>;
}

# 保留 Compose 编译器生成的代码
-keep class * extends androidx.compose.runtime.Composer {
    *;
}
-keepclassmembers class * extends androidx.compose.runtime.Composer {
    *;
}
-dontwarn androidx.compose.runtime.**

# 保留 Compose Material Icons
-keep class androidx.compose.material.icons.** { *; }

# ========================================
# DataStore 相关
# ========================================

-keep class androidx.datastore.** { *; }
-keepclassmembers class androidx.datastore.** { *; }

# ========================================
# AndroidX Lifecycle 相关
# ========================================

# ViewModel 及其 factory
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
    <methods>;
}
-keep class * extends androidx.lifecycle.ViewModelProvider.Factory {
    <methods>;
}
-keepclassmembers class * extends androidx.lifecycle.ViewModelProvider.Factory {
    *;
}

# ========================================
# Navigation 相关
# ========================================

-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# ========================================
# 应用自身规则 - 全部保留以防闪退
# ========================================

# 保留整个应用包（最安全）
-keep class com.example.zhuanpan.** { *; }
-keepclassmembers class com.example.zhuanpan.** { *; }

# 特别保留数据模型（用于序列化）
-keep class com.example.zhuanpan.data.model.** { *; }
-keepclassmembers class com.example.zhuanpan.data.model.** { *; }

# 特别保留 ViewModel
-keep class com.example.zhuanpan.ui.**ViewModel { *; }
-keepclassmembers class com.example.zhuanpan.ui.**ViewModel { *; }

# 保留 Application
-keep class com.example.zhuanpan.ZhuanpanApplication { *; }