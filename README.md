# 选择 · 转盘决策助手

一款基于 Jetpack Compose 构建的 Android 转盘决策应用，帮助你快速解决"选什么""吃什么""谁来"等纠结场景。支持自定义多个转盘、选项权重、配色方案、历史记录与备份还原，所有数据本地持久化，保护隐私。

## 应用截图

![主界面截图](docs/home_screenshot.png)
<img width="712" height="1243" alt="image" src="https://github.com/user-attachments/assets/94682ce0-bbc3-4b44-ba7c-9feca6b574d1" />
<img width="721" height="1321" alt="image" src="https://github.com/user-attachments/assets/be339be6-d69a-419f-94df-ff156b893005" />
<img width="721" height="1321" alt="image" src="https://github.com/user-attachments/assets/ea55070b-da7b-4750-88a8-0c1b79411bc2" />



> 上图展示转盘主页：顶部为转盘标题与持久结果展示区，中央为可旋转的彩色转盘（12 点钟方向有红色指针作为结果基准），底部为手动旋转与编辑入口。

## 功能特性

### 转盘核心
- **流畅旋转动画**：基于 `ease-out-cubic` 缓动曲线，60fps 更新，旋转时长可调（1~10 秒）
- **手动旋转**：一键触发 2~5 圈随机旋转，自动计算并展示中奖结果
- **指针基准**：12 点钟方向固定红色指针，作为结果判定基准
- **跨页面不中断**：旋转动画状态托管于 `ViewModel` + `viewModelScope`，切换页面不会被销毁

### 多转盘管理
- **多转盘列表**：创建、切换、删除多个独立转盘配置
- **内联编辑**：直接在列表中修改转盘标题（1~20 字符）
- **批量编辑**：选项批量增删改，支持拖拽排序

### 选项与抽取
- **选项权重**：每个选项可设置权重，影响中奖概率
- **重复抽取控制**：可选"允许重复"或"不重复抽取"，抽完自动重置
- **随机数生成**：独立随机数生成页，支持一键复制与大数字卡片展示

### 个性化设置
- **配色方案**：内置多种配色（Rainbow、Pastel 等），可实时切换
- **轮盘大小**：滑动条连续调节轮盘显示尺寸（0.5~1.0 倍屏宽）
- **结果字体大小**：滑动条连续调节（12~60sp）
- **旋转时长**：滑动条调节（1~10 秒）

### 数据与备份
- **历史记录**：完整记录每次抽取结果与所属转盘，支持清空
- **备份还原**：支持数据备份、文件导出、链接分享与导入还原
- **本地持久化**：基于 Jetpack DataStore，无需联网，隐私安全

## 技术栈

| 类别 | 技术 |
| --- | --- |
| 语言 | Kotlin 2.2.10 |
| UI 框架 | Jetpack Compose（BOM 2026.02.01） + Material 3 |
| 架构 | MVVM + 单 Activity + Navigation Compose |
| 异步 | Kotlin Coroutines + Flow / StateFlow |
| 持久化 | Jetpack DataStore (Preferences) + Kotlinx Serialization |
| 构建 | Gradle Kotlin DSL + Version Catalog |
| 最低 SDK | Android 7.0 (API 24) |
| 目标 SDK | Android 16 (API 36) |

## 项目结构

```
app/src/main/java/com/example/zhuanpan/
├── MainActivity.kt                 # 应用入口 Activity
├── ZhuanpanApplication.kt          # Application，初始化依赖
├── data/
│   ├── local/                      # DataStore 序列化器
│   ├── model/                      # 数据模型（WheelConfig、AppSettings 等）
│   └── repository/                 # 仓库层（Wheel/Settings/History/Backup）
├── ui/
│   ├── home/                       # 转盘主页（核心旋转逻辑）
│   ├── edit/                       # 转盘编辑页
│   ├── batch_edit/                 # 选项批量编辑页
│   ├── wheel_list/                 # 转盘列表页
│   ├── history/                    # 历史记录页
│   ├── random/                     # 随机数生成页
│   ├── settings/                   # 设置弹窗
│   ├── backup/                     # 备份还原弹窗
│   ├── navigation/                 # 导航图与路由
│   └── theme/                      # 主题、配色、字体
└── utils/WheelMath.kt              # 角度计算工具
```

## 安装步骤

### 方式一：直接安装 APK

1. 前往仓库 `app/release/` 目录下载最新的 `app-release.apk`
2. 在手机上允许"安装未知来源应用"
3. 点击安装即可使用

### 方式二：源码编译

#### 环境要求
- Android Studio（推荐最新稳定版）
- JDK 11+
- Android SDK，compileSdk 37

#### 编译运行
1. 克隆仓库
   ```bash
   git clone https://github.com/XingYuyuop/choose.git
   cd choose
   ```
2. 用 Android Studio 打开项目根目录
3. 等待 Gradle 同步完成（首次会下载依赖）
4. 连接 Android 设备或启动模拟器（API 24 及以上）
5. 点击 **Run 'app'** 编译并运行

#### 命令行编译
```bash
# Windows
./gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```
生成的 APK 位于 `app/build/outputs/apk/debug/`。

> 说明：Debug 包使用 `applicationIdSuffix = ".debug"`，包名为 `com.example.zhuanpan.debug`，可与 Release 版同时安装。

## 使用方法

1. **首次启动**：会显示引导提示，点击"新建轮盘"开始创建你的第一个转盘
2. **添加选项**：进入编辑页，添加你的选项（如"火锅""烧烤""日料"），可设置权重
3. **旋转抽取**：回到主页，点击"手动旋转"按钮，转盘旋转后指针所指即为结果
4. **切换转盘**：点击右上角菜单 → "所有列表"，可管理多个转盘
5. **查看历史**：点击右上角菜单 → "历史记录"，查看所有抽取记录
6. **个性化**：点击设置图标，调整配色、轮盘大小、字体大小、旋转时长
7. **备份还原**：点击右上角菜单 → "备份还原"，导出或导入数据

## 版本

- versionName: 1.1
- versionCode: 2

## 许可

本项目仅供学习交流使用。
