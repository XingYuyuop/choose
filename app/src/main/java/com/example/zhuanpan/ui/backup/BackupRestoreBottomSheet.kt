package com.example.zhuanpan.ui.backup

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.zhuanpan.ZhuanpanApplication
import com.example.zhuanpan.data.repository.BackupRestoreManager
import com.example.zhuanpan.ui.theme.Background
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.Divider
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 备份还原底部弹窗。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as ZhuanpanApplication

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showTextRestoreDialog by remember { mutableStateOf(false) }
    var textRestoreInput by remember { mutableStateOf("") }

    // 导出文件 launcher（.txt 格式）
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    val json = app.backupRestoreManager.export()
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(json.toByteArray())
                    }
                    true
                }
                statusMessage = if (result) "备份成功" else "备份失败"
            }
        }
    }

    // 导入文件 launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    val text = context.contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader().readText()
                    }
                    if (text != null) app.backupRestoreManager.import(text) else false
                }
                statusMessage = if (result) "还原成功" else "还原失败，文件格式错误"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = ColorWhite,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // 数据备份
            BackupActionItem(
                icon = Icons.Default.CloudUpload,
                title = "数据备份",
                subtitle = "导出全部转盘、设置和历史记录",
                onClick = {
                    val fileName = BackupRestoreManager.generateFileName()
                    exportLauncher.launch(fileName)
                }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // 数据还原
            BackupActionItem(
                icon = Icons.Default.CloudDownload,
                title = "数据还原",
                subtitle = "从备份文件恢复数据",
                onClick = {
                    importLauncher.launch(arrayOf("text/plain", "application/json", "*/*"))
                }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // 文本还原
            BackupActionItem(
                icon = Icons.Default.ContentPaste,
                title = "文本还原",
                subtitle = "粘贴备份文本直接还原",
                onClick = {
                    textRestoreInput = ""
                    showTextRestoreDialog = true
                }
            )

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // 分享备份
            BackupActionItem(
                icon = Icons.Default.Share,
                title = "分享备份",
                subtitle = "将备份文件分享到其他应用",
                onClick = {
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            val json = app.backupRestoreManager.export()
                            val fileName = BackupRestoreManager.generateFileName()
                            val file = File(context.cacheDir, fileName)
                            file.writeText(json)
                            file
                        }
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            result
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val chooser = Intent.createChooser(intent, "分享备份").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(chooser)
                    }
                }
            )
        }
    }

    // 文本还原弹窗
    if (showTextRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showTextRestoreDialog = false },
            title = { Text("文本还原", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = textRestoreInput,
                    onValueChange = { textRestoreInput = it },
                    label = { Text("粘贴备份文本") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                app.backupRestoreManager.import(textRestoreInput.trim())
                            }
                            statusMessage = if (result) "还原成功" else "还原失败，文本格式错误"
                            showTextRestoreDialog = false
                        }
                    }
                ) {
                    Text("还原", color = PrimaryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextRestoreDialog = false }) {
                    Text("取消", color = OnSurfaceVariant)
                }
            },
            containerColor = ColorWhite
        )
    }

    // 状态提示弹窗
    statusMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { statusMessage = null },
            title = { Text("提示", fontWeight = FontWeight.Bold) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    statusMessage = null
                    if (message == "还原成功") onDismiss()
                }) {
                    Text("确定", color = PrimaryRed)
                }
            },
            containerColor = ColorWhite
        )
    }
}

@Composable
private fun BackupActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BottomSheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 12.dp, bottom = 16.dp)
            .width(40.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Divider)
    )
}
