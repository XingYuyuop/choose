package com.example.zhuanpan.ui.batch_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhuanpan.ZhuanpanApplication
import com.example.zhuanpan.ui.edit.EditViewModel
import com.example.zhuanpan.ui.theme.Background
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed

/**
 * 批量编辑选项页面。
 *
 * 以整页形式展示多行文本输入框，每行对应一个选项，保存后替换当前转盘的全部选项。
 *
 * @param onBack 返回回调
 * @param viewModel 编辑 ViewModel（与编辑页共享同一份内存状态）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEditScreen(
    onBack: () -> Unit,
    viewModel: EditViewModel = viewModel(
        factory = EditViewModel.provideFactory(
            wheelRepository = (LocalContext.current.applicationContext as ZhuanpanApplication).wheelRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // 使用 rememberSaveable 保留用户输入与初始化标记，避免旋转等配置变更导致内容丢失
    val optionsText = rememberSaveable { mutableStateOf("") }
    var initialized by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // 仅在首次拿到配置时初始化一次文本，后续编辑不再覆盖
    LaunchedEffect(uiState.config.options, initialized) {
        if (!initialized) {
            optionsText.value = uiState.config.options.joinToString("\n") { it.label }
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "批量编辑选项",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val (labels, error) = validateBatchText(optionsText.value)
                            if (error != null) {
                                errorMessage = error
                                return@IconButton
                            }
                            errorMessage = null
                            viewModel.applyBatchOptions(labels)
                            viewModel.saveConfig(
                                onSaved = onBack,
                                onError = { errorMessage = it }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "保存",
                            tint = PrimaryRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "每行一个选项",
                fontSize = 13.sp,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            BasicTextField(
                value = optionsText.value,
                onValueChange = {
                    optionsText.value = it
                    if (errorMessage != null) errorMessage = null
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(ColorWhite)
                    .padding(16.dp)
            ) { innerTextField ->
                Box {
                    if (optionsText.value.isBlank()) {
                        Text(
                            text = "每行输入一个选项",
                            fontSize = 16.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 24.sp
                        )
                    }
                    innerTextField()
                }
            }

            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    fontSize = 13.sp,
                    color = PrimaryRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 4.dp)
                )
            }
        }
    }
}

/**
 * 校验批量编辑文本。
 *
 * 返回解析后的选项文本列表与错误信息（无错误时为 null）。
 */
private fun validateBatchText(text: String): Pair<List<String>, String?> {
    val labels = text
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    if (labels.isEmpty()) {
        return Pair(emptyList(), "请至少输入一个选项")
    }

    return Pair(labels, null)
}
