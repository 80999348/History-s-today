# 历史上的今天 · On This Day

> 一款完全离线、可自由翻阅历史的中文 Android 应用。
> 输入任意日期，看看这一天在世界历史上发生过什么。

<div align="center">

[![Android](https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![MinSDK](https://img.shields.io/badge/minSdk-21-brightgreen)](https://developer.android.com/studio)
[![TargetSDK](https://img.shields.io/badge/targetSdk-34-blue)](https://developer.android.com/studio)
[![Language](https://img.shields.io/badge/语言-Java%201.8-007396?logo=java&logoColor=white)](https://www.java.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📖 简介

「历史上的今天」是一款轻量、**完全离线**的 Android 应用。它收录了从古至今、跨越全球的数千条历史事件、人物诞生与逝世、以及节日信息，数据整理自百度百科。

打开应用，默认展示 **今天** 在世界历史上发生的所有事；你也可以前后翻页、随机跳转、选择任意日期，或按关键词/年份搜索。每一条记载都可以点开查看详细介绍，并一键复制分享。

**数据量**：全库 **3,281** 则记载 · **54** 个节日 · 数据随包内置，无需联网。

---

## ✨ 功能特性

- 📅 **按日浏览**：默认展示今天的全部记载，支持上一日 / 下一日切换
- 🎲 **随机跳转**：一键随机到一年中的任意一天
- 🗓️ **日期选择**：点击日期可弹出日历，跳转到任意年月日
- 🔍 **全文搜索**：按人物、事件关键词或年份检索（最多返回 80 条）
- 🧭 **返回今天**：随时一键回到当前日期
- 📜 **时间线视图**：每条记载按年份排序，带时间线节点与分类色标
- 📤 **详情面板**：底部弹出，展示事件标题、详细描述、距今多少年
- 📋 **一键复制**：将事件文本复制到剪贴板
- 🌙 **农历与干支**：自动换算农历日期、干支纪年与生肖，显示距年末天数
- 🧩 **完全离线**：数据内置于 `assets/`，无任何网络依赖
- 🎨 **暗色沉浸式 UI**：透明状态栏/导航栏，沉浸式全屏体验

---

## 🗂️ 项目结构

```
history/
├── AndroidManifest.xml          # 应用清单（单 Activity）
├── release.jks                  # 签名密钥（请勿提交到公开仓库）
├── assets/
│   ├── history.dat              # 历史记载数据（3,281 则）
│   └── festival.dat             # 节日数据（54 个）
├── res/
│   ├── layout/activity_main.xml # 主界面布局
│   ├── drawable/                # 背景与控件样式
│   ├── mipmap-*/                # 应用图标
│   └── values/                  # 颜色、字符串、主题
└── src/com/today/history/
    ├── MainActivity.java        # 主界面与全部交互逻辑
    ├── HistoryRepository.java   # 数据加载 / 索引 / 搜索
    ├── LunarCalendar.java       # 公历→农历转换（1900-2100）
    └── Entry.java               # 记载数据模型
```

### 数据格式

`history.dat` 每行一条记录，字段以 `|` 分隔：

```
MMDD|年份|类型|标题|详细介绍
0101|1823|birth|匈牙利诗人裴多菲出生|裴多菲·山陀尔（...），是匈牙利的爱国诗人...
```

| 字段 | 说明 |
| ---- | ---- |
| `MMDD` | 月日（如 `0101` 表示 1 月 1 日） |
| 年份 | 事件发生的年份 |
| 类型 | `birth` 诞生 / `death` 逝世 / `festival` 节日 / `event` 事件 |
| 标题 | 事件简述 |
| 详细介绍 | 人物生平或事件背景 |

`festival.dat` 每行一个节日：`MMDD|节日名称`。

---

## 🚀 构建与运行

### 环境要求

- **Android Studio**（或任意支持 Gradle 的 Android 构建环境）
- **minSdk 21**（Android 5.0）· **targetSdk 34**（Android 14）
- **Java 8+**

### 步骤

1. 将项目导入 Android Studio（或手动配置 Gradle 构建脚本）。
2. 添加 `com.today.history` 作为主包名，配置签名（建议使用 `release.jks` 或自行生成）。
3. 构建并运行：

```bash
# 调试构建
./gradlew assembleDebug

# 生成签名 APK
./gradlew assembleRelease
```

> **提示**：本仓库当前为**纯源码结构**，未包含 Gradle Wrapper 与构建脚本。你可以用 Android Studio 的「Open an existing project」直接打开 `AndroidManifest.xml` 所在目录，Android Studio 会自动补齐构建配置；或手动创建 `build.gradle`、`settings.gradle` 后构建。

---

## 🛡️ 安全提示

> ⚠️ **`release.jks` 为签名密钥文件**。出于安全考虑，**请勿将其提交到公开的 GitHub 仓库**。建议：
>
> - 从版本控制中移除：`git rm --cached release.jks`
> - 在 `.gitignore` 中加入 `*.jks`
> - 使用本地密钥或 CI 注入环境变量管理签名

---

## 📄 数据来源

历史记载与节日数据整理自 [百度百科](https://baike.baidu.com/)，内容版权归原作者所有。本仓库仅供学习交流使用。

---

## 📜 License

本项目基于 [MIT License](LICENSE) 发布，可自由使用与修改。请保留原始版权声明。

---

<div align="center">

**Made with ❤️ · 每天都是历史**

</div>
