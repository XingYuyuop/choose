package com.example.zhuanpan.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhuanpan.data.model.SpinHistory
import com.example.zhuanpan.data.model.SpinHistoryItem
import com.example.zhuanpan.ui.theme.ColorWhite
import com.example.zhuanpan.ui.theme.Divider
import com.example.zhuanpan.ui.theme.OnSurfaceVariant
import com.example.zhuanpan.ui.theme.PrimaryRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 历史记录底部弹窗。
 *
 * @param history 当前历史记录
 * @param onClear 清空历史回调
 * @param onDismiss 关闭弹窗回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    history: SpinHistory,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "历史记录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (history.records.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "清空历史",
                            tint = PrimaryRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (history.records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无记录\n旋转转盘后将自动保存",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(history.records, key = { it.id }) { record ->
                        HistoryItemRow(record = record)
                    }
                }
            }
        }
    }
}

/**
 * 单条历史记录行。
 */
@Composable
private fun HistoryItemRow(
    record: SpinHistoryItem,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.result,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = record.wheelTitle,
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
            }

            Text(
                text = formatTimestamp(record.timestamp),
                fontSize = 12.sp,
                color = OnSurfaceVariant
            )
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)
    }
}

/**
 * 底部弹窗拖拽指示条。
 */
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

/**
 * 格式化时间戳为可读字符串。
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-M-d HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
