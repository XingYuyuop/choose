package com.example.zhuanpan.data.repository

import com.example.zhuanpan.data.model.WheelConfig
import kotlinx.coroutines.flow.Flow

/**
 * 转盘配置仓库接口。
 *
 * 支持多转盘管理：维护一个转盘集合，并提供当前选中转盘的数据流。
 */
interface WheelRepository {

    /**
     * 全部转盘列表数据流。
     */
    val wheels: Flow<List<WheelConfig>>

    /**
     * 当前选中转盘配置数据流。
     */
    val wheelConfig: Flow<WheelConfig>

    /**
     * 初始化数据：若集合为空则从旧版单转盘数据迁移或创建默认空转盘。
     * 幂等操作，可多次调用。
     */
    suspend fun ensureInitialized()

    /**
     * 新建一个空转盘并选中它，返回新转盘 ID。
     */
    suspend fun createWheel(): String

    /**
     * 选中指定转盘作为当前转盘。
     */
    suspend fun selectWheel(id: String)

    /**
     * 保存（更新）转盘配置。若该 ID 不存在则追加。
     */
    suspend fun saveWheelConfig(config: WheelConfig)

    /**
     * 删除指定转盘；若删除的是当前转盘，自动切换到首个剩余转盘。
     */
    suspend fun deleteWheel(id: String)

    /**
     * 复制指定转盘，返回新转盘 ID。
     */
    suspend fun copyWheel(id: String): String

    /**
     * 重置为默认状态：清空全部转盘并创建一个空转盘。
     */
    suspend fun resetToDefault()
}
