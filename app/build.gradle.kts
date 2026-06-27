import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.zhuanpan"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.example.zhuanpan"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 从环境变量读取签名信息（CI 自动打包用，本地无环境变量则忽略，行为不变）
    val signingKeystorePath = System.getenv("SIGNING_KEYSTORE_PATH")
    val signingKeystorePassword = System.getenv("SIGNING_KEYSTORE_PASSWORD")
    val signingKeyAlias = System.getenv("SIGNING_KEY_ALIAS")
    val signingKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")

    signingConfigs {
        if (signingKeystorePath != null && file(signingKeystorePath).exists()) {
            create("release") {
                storeFile = file(signingKeystorePath)
                storePassword = signingKeystorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 若已通过环境变量配置签名，则应用到 release 构建；否则保持原行为
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

tasks.matching { it.name.startsWith("package") && (it.name.endsWith("Debug") || it.name.endsWith("Release")) }.configureEach {
    doLast {
        val dir = layout.buildDirectory.dir("outputs/apk").get().asFile
        dir.walkTopDown().filter { it.extension == "apk" }.forEach { apk ->
            val buildType = apk.parentFile.name
            val date = SimpleDateFormat("yyyyMMddHHmm").format(Date())
            val newName = "app-$buildType-$date.apk"
            apk.renameTo(File(apk.parentFile, newName))
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}