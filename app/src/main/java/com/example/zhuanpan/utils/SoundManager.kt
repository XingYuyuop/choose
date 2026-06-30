package com.example.zhuanpan.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.zhuanpan.R

/**
 * 音效管理器。
 *
 * 负责加载和播放旋转过程中的滴答音效与结果音效。
 * 使用 SoundPool 实现低延迟播放，适合短促音效场景。
 * 旋转过程中每经过一个选项边界播放一次滴答声，与转盘转动视觉同步。
 */
class SoundManager(context: Context) {

    private val soundPool: SoundPool
    private var resultSoundId: Int = 0
    private var tickSoundId: Int = 0
    private var loaded = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, _ ->
            loaded = true
        }

        // 加载音效资源
        try {
            resultSoundId = soundPool.load(context, R.raw.result_sound, 1)
        } catch (_: Exception) {
            resultSoundId = 0
        }
        try {
            tickSoundId = soundPool.load(context, R.raw.tick_sound, 1)
        } catch (_: Exception) {
            tickSoundId = 0
        }
    }

    /**
     * 播放滴答音效（旋转过程中每经过一个选项边界时）。
     * 旋转越快播放越密集，减速时间隔变长，自然产生"快转-减速-停止"的听觉反馈。
     *
     * @param volume 音量（0.0~1.0），由动画循环根据进度动态控制，实现渐入渐出效果
     */
    fun playTickSound(volume: Float = 0.25f) {
        if (loaded && tickSoundId != 0) {
            val clampedVolume = volume.coerceIn(0.05f, 0.4f)
            soundPool.play(tickSoundId, clampedVolume, clampedVolume, 0, 0, 1.0f)
        }
    }

    /**
     * 播放结果音效（旋转结束时）。
     */
    fun playResultSound() {
        if (loaded && resultSoundId != 0) {
            soundPool.play(resultSoundId, 0.35f, 0.35f, 1, 0, 1.0f)
        }
    }

    /**
     * 释放资源。
     */
    fun release() {
        soundPool.release()
        loaded = false
    }
}
