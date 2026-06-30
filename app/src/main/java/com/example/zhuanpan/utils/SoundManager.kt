package com.example.zhuanpan.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import com.example.zhuanpan.R

/**
 * 音效管理器。
 *
 * 负责加载和播放旋转音效与结果音效。
 * 使用 SoundPool 实现低延迟播放，适合短促音效场景。
 */
class SoundManager(context: Context) {

    private val soundPool: SoundPool
    private var spinSoundId: Int = 0
    private var resultSoundId: Int = 0
    private var tickSoundId: Int = 0
    private var loaded = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, _ ->
            loaded = true
        }

        // 加载音效资源
        try {
            spinSoundId = soundPool.load(context, R.raw.spin_sound, 1)
        } catch (_: Exception) {
            spinSoundId = 0
        }
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
     * 播放旋转音效。
     */
    fun playSpinSound() {
        if (loaded && spinSoundId != 0) {
            soundPool.play(spinSoundId, 0.6f, 0.6f, 1, 0, 1.0f)
        }
    }

    /**
     * 播放结果音效。
     */
    fun playResultSound() {
        if (loaded && resultSoundId != 0) {
            soundPool.play(resultSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
        }
    }

    /**
     * 播放滴答音效（旋转过程中每经过一个选项边界时）。
     */
    fun playTickSound() {
        if (loaded && tickSoundId != 0) {
            soundPool.play(tickSoundId, 0.4f, 0.4f, 1, 0, 1.2f)
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
