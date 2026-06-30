package com.example.zhuanpan.ui.create_wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zhuanpan.data.model.WheelOption
import com.example.zhuanpan.data.repository.WheelRepository
import kotlinx.coroutines.launch
import java.util.UUID

class CreateWheelViewModel(
    private val wheelRepository: WheelRepository
) : ViewModel() {

    val templates = listOf(
        WheelTemplate(
            title = "此事能否成功",
            options = listOf(
                WheelOption(id = UUID.randomUUID().toString(), label = "能", weight = 5),
                WheelOption(id = UUID.randomUUID().toString(), label = "不能", weight = 1)
            )
        )
    )

    fun createEmptyWheel() {
        viewModelScope.launch {
            wheelRepository.createWheel()
        }
    }

    fun createFromTemplate(template: WheelTemplate) {
        viewModelScope.launch {
            val options = template.options.map {
                it.copy(id = UUID.randomUUID().toString())
            }
            wheelRepository.createWheel(template.title, options)
        }
    }

    companion object {
        fun provideFactory(
            wheelRepository: WheelRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateWheelViewModel(wheelRepository) as T
            }
        }
    }
}
