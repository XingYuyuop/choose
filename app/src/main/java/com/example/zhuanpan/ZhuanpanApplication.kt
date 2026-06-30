package com.example.zhuanpan

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.zhuanpan.data.local.ZhuanpanDataStore
import com.example.zhuanpan.data.repository.BackupRestoreManager
import com.example.zhuanpan.data.repository.HistoryRepository
import com.example.zhuanpan.data.repository.HistoryRepositoryImpl
import com.example.zhuanpan.data.repository.SettingsRepository
import com.example.zhuanpan.data.repository.SettingsRepositoryImpl
import com.example.zhuanpan.data.repository.WheelRepository
import com.example.zhuanpan.utils.SoundManager
import com.example.zhuanpan.data.repository.WheelRepositoryImpl

/**
 * 应用入口类，负责提供 Repository 单例。
 *
 * 采用延迟初始化避免启动时立即创建 DataStore，同时确保 ViewModel 可通过
 * [application] 获取同一实例，维持数据一致性。
 */
class ZhuanpanApplication : Application() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zhuanpan_prefs")

    private val zhuanpanDataStore by lazy { ZhuanpanDataStore(dataStore) }

    val wheelRepository: WheelRepository by lazy {
        WheelRepositoryImpl(zhuanpanDataStore)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(zhuanpanDataStore)
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepositoryImpl(zhuanpanDataStore)
    }

    val backupRestoreManager: BackupRestoreManager by lazy {
        BackupRestoreManager(zhuanpanDataStore)
    }

    val soundManager: SoundManager by lazy {
        SoundManager(this)
    }
}
