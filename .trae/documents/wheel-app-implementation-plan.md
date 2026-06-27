# 转盘应用完善实施计划

## 1. Summary

在现有 Jetpack Compose + MVVM + DataStore 架构的转盘应用基础上，围绕“转盘本身”进行完善：修复现有小 bug，让设置面板中的开关真正生效，补齐编辑页权重调整，并增加历史记录功能。本计划不涉及发现页/随机数页的占位改造。

## 2. Current State Analysis

- **架构**：标准 MVVM，Repository 通过 DataStore 做 JSON 持久化，Compose 构建 UI。
- **已完成**：转盘绘制、旋转动画、结果计算、编辑页（标题/增删选项）、设置弹窗、底部导航框架。
- **缺陷与未生效功能**：
  1. `ColorScheme.kt:18` 配色方案名 `" pastel"` 带多余前导空格。
  2. `ResultDisplay.kt:48` 缩放动画 `targetValue` 恒为 `1f`，结果变化时不会重新触发。
  3. 设置中的 `manualSpin`（手动旋转）、`voiceEnabled`（语音播报）、`allowRepeat`（允许重复抽取）未在业务层使用。
  4. 编辑页 `OptionItem.kt` 仅显示“权重”文字，无法调整具体数值。
  5. `HomeScreen.kt` “更多”菜单里的“历史记录”没有对应实现。

## 3. Proposed Changes

### 3.1 修复已知小 bug

#### 3.1.1 修复配色方案显示名
- **文件**：`app/src/main/java/com/example/zhuanpan/data/model/ColorScheme.kt`
- **改动**：将 `PASTEL` 的 `schemeName` 从 `" pastel"` 改为 `"柔和"`（与 VIBRANT/ MONOCHROME 中文命名保持一致）。
- **原因**：设置弹窗中显示的名称带不明空格，影响视觉一致性。

#### 3.1.2 修复结果展示动画
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/home/components/ResultDisplay.kt`
- **改动**：
  - 新增 `val targetScale = if (displayedResult.isBlank()) 0.8f else 1f`。
  - `animateFloatAsState` 的 `targetValue` 绑定到 `targetScale`，并使用 `animateKey` 作为 `key`（或直接把 `displayedResult` 作为 key），确保每次结果变化都播放缩放+淡入动画。
- **原因**：当前动画只会在 Composable 首次进入时触发一次，结果更新时没有反馈。

### 3.2 实现“允许重复抽取”

#### 3.2.1 扩展状态与逻辑
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/home/HomeUiState.kt`
- **改动**：新增 `drawnOptionIds: Set<String> = emptySet()`，记录本轮已抽中的选项 ID。

#### 3.2.2 ViewModel 逻辑
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/home/HomeViewModel.kt`
- **改动**：
  - 在 `onSpinFinished` 中，如果 `settings.allowRepeat == false`，将中奖选项 ID 加入 `drawnOptionIds`。
  - 计算中奖时（可复用 `WheelMath.calculateWinner`），先过滤掉 `drawnOptionIds` 中的选项；若过滤后无可用选项，则 Toast 提示“所有选项已抽完”并清空集合重新开始。
  - `onResetWheel` 时同步清空 `drawnOptionIds`。
  - 转盘配置变化（选项列表变化）时，也清空 `drawnOptionIds`，避免 ID 失效导致空候选。
- **原因**：让设置弹窗中的“允许重复抽取”开关真正影响行为。

### 3.3 实现“语音播报”

#### 3.3.1 UI 层接入 TTS
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/home/HomeScreen.kt`
- **改动**：
  - 使用 `remember { TextToSpeech(context, ...) }` 创建 TTS 实例。
  - 在 `DisposableEffect` 中 `shutdown()` 释放。
  - 在 `onSpinFinished` 回调或 `LaunchedEffect(currentResult)` 中，当 `settings.voiceEnabled && currentResult.isNotBlank()` 时调用 `tts.speak(currentResult, TextToSpeech.QUEUE_FLUSH, null, null)`。
- **原因**：设置中已提供开关，需要与实际播报能力绑定。提示“部分机型不支持”保留。

### 3.4 实现“手动旋转”

#### 3.4.1 手动模式交互
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/home/HomeScreen.kt`
- **改动**：
  - 当 `settings.manualSpin == true` 时，中间“点击旋转”按钮文案改为“停止”，点击后停止当前旋转并计算结果；或改为可拖动手势（推荐简化方案）：
    - **简化方案**：手动模式下，点击/长按转盘任意位置累加一个小角度（如 30°~60°），释放后根据当前角度计算结果。这样无需处理复杂的速度检测。
  - 自动旋转按钮在手动模式下禁用或隐藏，避免两种模式冲突。
  - 在 `HomeContent` 的转盘 Box 上添加 `pointerInput(manualSpin)`，手动模式下消费点击事件。
- **原因**：设置开关目前无对应行为，需要给用户提供一种手动控制转盘的方式。

### 3.5 编辑页支持权重调整

#### 3.5.1 选项行增加权重步进器
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/edit/components/OptionItem.kt`
- **改动**：
  - 在选项右侧“权重”文字旁添加当前权重数值显示。
  - 添加 `-` / `+` 圆形小按钮，点击调用 `onWeightChange(option.weight - 1)` / `+1`，并限制 `weight >= 0`（`EditViewModel` 已做约束）。
  - 权重为 0 时以灰色显示，提示该选项不参与抽取。
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/edit/EditScreen.kt`（如需要）
- **改动**：确认 `EditBottomBar` 中的“布局”图标可先保持预留，不做改动。
- **原因**：权重决定扇形大小，必须可编辑才能体现“多个均等/不均等分布”的需求。

### 3.6 增加历史记录功能

#### 3.6.1 数据模型
- **文件**：`app/src/main/java/com/example/zhuanpan/data/model/SpinHistory.kt`（新建）
- **内容**：
  ```kotlin
  @Serializable
  data class SpinHistory(
      val records: List<SpinHistoryItem> = emptyList()
  )

  @Serializable
  data class SpinHistoryItem(
      val result: String,
      val wheelTitle: String,
      val timestamp: Long = System.currentTimeMillis()
  )
  ```

#### 3.6.2 DataStore 与 Repository
- **文件**：`app/src/main/java/com/example/zhuanpan/data/local/ZhuanpanDataStore.kt`
- **改动**：新增 `SPIN_HISTORY` string key 与读写方法。
- **文件**：`app/src/main/java/com/example/zhuanpan/data/local/SpinHistorySerializer.kt`（新建）
- **内容**：JSON 序列化/反序列化，失败返回空历史。
- **文件**：`app/src/main/java/com/example/zhuanpan/data/repository/HistoryRepository.kt`（新建接口）
- **文件**：`app/src/main/java/com/example/zhuanpan/data/repository/HistoryRepositoryImpl.kt`（新建实现）
- **改动**：提供 `history: Flow<SpinHistory>`、`addRecord(item)`、`clearHistory()`。
- **文件**：`app/src/main/java/com/example/zhuanpan/ZhuanpanApplication.kt`
- **改动**：新增 `historyRepository` 单例。

#### 3.6.3 ViewModel 与 UI
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/home/HomeViewModel.kt`
- **改动**：注入 `HistoryRepository`，在 `onSpinFinished` 中奖后写入记录。
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/home/HomeUiState.kt`
- **改动**：新增 `showHistory: Boolean = false`。
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/history/HistoryBottomSheet.kt`（新建）
- **内容**：底部弹窗展示历史记录列表（时间 + 结果 + 转盘标题），支持“清空历史”按钮；空状态显示提示文字。
- **文件**：`app/src/main/java/com/example/zhuanpan/ui/home/HomeScreen.kt`
- **改动**：
  - “更多”菜单的“历史记录”项调用 `viewModel.onHistoryVisibilityChanged(true)`。
  - 根据 `uiState.showHistory` 显示 `HistoryBottomSheet`。

## 4. Assumptions & Decisions

- **范围限定**：只做“转盘本身”相关功能，发现页/随机数页保持占位，不纳入本次计划。
- **手动旋转简化**：采用“点击转盘累加角度并计算结果”的简化方案，而非完整拖拽+惯性滚动，以控制实现复杂度。
- **历史记录上限**：最多保存 100 条，新增时如果超出则移除最旧记录，避免 DataStore JSON 过大。
- **TTS 语言**：使用系统默认 TTS 语言，不额外做语言切换；若初始化失败则静默禁用播报。
- **重复抽取重置**：当所有非零权重选项都被抽中后，自动清空已抽取集合并提示用户，开始新一轮抽取。

## 5. Verification Steps

1. **编译通过**：执行 `./gradlew :app:assembleDebug`（或 IDE Build）无编译错误。
2. **配色名称**：进入设置 → 转盘配色，选项显示为“柔和/鲜艳/单色”，无多余空格。
3. **结果动画**：旋转结束后结果文字播放缩放+淡入动画；再次旋转到新结果时动画可重新触发。
4. **权重编辑**：编辑页每个选项可点击 +/- 调整权重，返回首页后转盘扇形大小随权重变化。
5. **重复抽取**：关闭“允许重复抽取”后，连续旋转不会重复命中同一选项；全部抽完后提示并自动重置。
6. **语音播报**：开启“语音播报”后，旋转结束自动朗读中奖结果。
7. **手动旋转**：开启“手动旋转”后，点击转盘可手动推进并计算结果，自动旋转按钮不可用。
8. **历史记录**：点击“更多 → 历史记录”可查看历次结果；清空按钮能清空列表；首页旋转后记录自动追加。
