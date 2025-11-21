# OpenSMILE 集成指南

本文档介绍如何在项目中安装和配置 openSMILE 音频特征提取工具。

## 什么是 openSMILE？

openSMILE (Open Source Media Interpretation by Large-feature-space Extraction) 是一个开源的音频特征提取工具，由德国 audEERING GmbH 开发。它可以从语音信号中提取数千个声学特征，广泛应用于情感识别、压力检测和抑郁症辅助诊断等研究。

GitHub 地址：https://github.com/audeering/opensmile

## 为什么使用 openSMILE？

在抑郁症检测中，openSMILE 可以提取以下关键特征：

- **基频 (F0)**: 抑郁症患者通常音调单调，F0 变化范围较小
- **响度 (Loudness)**: 抑郁症患者通常声音较小、能量较低
- **音质微扰 (Jitter/Shimmer)**: 反映声音的不稳定性，与压力和情绪障碍相关
- **谐噪比 (HNR)**: 抑郁症患者的 HNR 通常较低，表示声音质量下降
- **MFCC 和频谱特征**: 用于全面描述语音的声学特性

这些特征在多项临床研究中被证明与抑郁症状高度相关。

## 安装 openSMILE

### Ubuntu/Debian

```bash
# 方法1: 从源代码编译（推荐）
git clone https://github.com/audeering/opensmile.git
cd opensmile
bash build.sh

# 编译完成后，可执行文件位于 build/progsrc/smilextract/SMILExtract
sudo cp build/progsrc/smilextract/SMILExtract /usr/local/bin/
sudo cp -r config /usr/local/share/opensmile/

# 方法2: 使用预编译二进制文件
# 从 GitHub Releases 下载适合您系统的版本
# https://github.com/audeering/opensmile/releases
```

### macOS

```bash
# 使用 Homebrew（如果可用）
brew install opensmile

# 或从源代码编译（需要安装 Xcode 和命令行工具）
git clone https://github.com/audeering/opensmile.git
cd opensmile
bash build.sh
sudo cp build/progsrc/smilextract/SMILExtract /usr/local/bin/
sudo cp -r config /usr/local/share/opensmile/
```

### Windows

```bash
# 下载预编译的 Windows 版本
# https://github.com/audeering/opensmile/releases
# 解压到指定目录（如 C:\opensmile）
```

## 配置项目

安装完成后，需要在 `application.properties` 中配置 openSMILE：

```properties
# 启用 openSMILE
ai.opensmile.enabled=true

# SMILExtract 可执行文件路径
ai.opensmile.path=/usr/local/bin/SMILExtract

# 配置文件路径（可选，留空使用默认）
ai.opensmile.config.path=

# 配置类型（推荐使用 eGeMAPSv02）
ai.opensmile.config.type=eGeMAPSv02
```

### 可用的配置类型

1. **eGeMAPSv02** (推荐)
   - 88 个特征
   - 专门用于情感和情绪识别
   - 平衡了特征数量和计算效率
   - 包含最关键的声学指标

2. **GeMAPSv01b**
   - 62 个特征
   - eGeMAPS 的简化版本
   - 适合快速分析

3. **emobase**
   - 988 个特征
   - 包含更全面的声学特征
   - 计算时间较长

4. **ComParE_2016**
   - 6373 个特征
   - 竞赛级别的完整特征集
   - 需要更长的处理时间

## 验证安装

启动应用后，可以通过以下方式验证 openSMILE 是否正常工作：

1. **查看日志**: 应用启动时会尝试检测 openSMILE
2. **录制语音**: 提交评估时录制一段语音
3. **查看结果**: 如果 openSMILE 正常工作，结果页面会显示：
   - "✨ OpenSMILE 专业分析"标记
   - 提取的特征数量
   - 配置类型

如果 openSMILE 不可用，系统会自动降级使用内置的简单特征提取方法，不会影响正常使用。

## 工作原理

1. **音频录制**: 用户在前端录制语音（使用浏览器 MediaRecorder API）
2. **数据传输**: 音频数据以 Base64 格式发送到后端
3. **文件准备**: OpenSmileService 将数据解码并添加 WAV 文件头
4. **特征提取**: 调用 SMILExtract 命令行工具处理音频文件
5. **结果解析**: 解析 CSV 输出文件，提取关键特征
6. **风险评估**: 基于临床研究的声学指标计算抑郁风险评分

## 关键声学指标与抑郁症的关系

| 特征 | 正常范围 | 抑郁症特征 | 临床意义 |
|------|---------|-----------|---------|
| F0 标准差 | 较大变化 | 较小变化（单调） | 情绪平淡、缺乏情感表达 |
| 响度均值 | 正常音量 | 偏低 | 能量不足、疲劳 |
| Jitter | 低 | 偏高 | 声音不稳定、紧张 |
| Shimmer | 低 | 偏高 | 发声质量下降 |
| HNR | 高 | 偏低 | 声音嘶哑、气息声增加 |

## 故障排查

### openSMILE 未检测到

```bash
# 检查可执行文件是否存在
ls -la /usr/local/bin/SMILExtract

# 检查是否有执行权限
chmod +x /usr/local/bin/SMILExtract

# 手动测试 openSMILE
/usr/local/bin/SMILExtract -h
```

### 配置文件找不到

```bash
# 检查配置文件目录
ls -la /usr/local/share/opensmile/config/

# 或使用完整路径
ai.opensmile.config.path=/usr/local/share/opensmile/config/eGeMAPSv02.conf
```

### 权限问题

```bash
# 确保临时文件目录可写
sudo chmod 777 /tmp
```

## 性能优化

- **配置选择**: eGeMAPSv02 提供了最佳的性能/准确度平衡
- **缓存结果**: 对于相同的音频不重复提取特征
- **异步处理**: 考虑在后台队列中处理特征提取（未来优化）

## 参考文献

1. Eyben, F., et al. (2016). "The Geneva Minimalistic Acoustic Parameter Set (GeMAPS) for Voice Research and Affective Computing." IEEE Transactions on Affective Computing.

2. Cummins, N., et al. (2015). "A review of depression and suicide risk assessment using speech analysis." Speech Communication.

3. openSMILE 官方文档: https://audeering.github.io/opensmile/

## 支持与反馈

如有问题或建议，请参考：
- openSMILE GitHub Issues: https://github.com/audeering/opensmile/issues
- 项目文档：查看 README.md
