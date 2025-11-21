# 配置指南 Configuration Guide

本指南详细说明如何配置所有AI功能，包括OpenSMILE、Baidu AI和OpenAI集成。

## 📋 目录

1. [OpenSMILE配置](#opensmile配置)
2. [百度AI配置](#百度ai配置)
3. [OpenAI配置](#openai配置)
4. [功能开关](#功能开关)

---

## 🎤 OpenSMILE配置

OpenSMILE是一个专业的音频特征提取工具，用于高级语音情感分析。

### 安装步骤

#### Linux/Ubuntu

```bash
# 方法1: 从源代码编译
sudo apt-get update
sudo apt-get install build-essential git cmake
git clone https://github.com/audeering/opensmile.git
cd opensmile
bash build.sh
sudo cp build/progsrc/smilextract/SMILExtract /usr/local/bin/
sudo mkdir -p /usr/local/share/opensmile
sudo cp -r config /usr/local/share/opensmile/

# 方法2: 下载预编译版本
# 访问 https://github.com/audeering/opensmile/releases
# 下载适合您系统的版本并解压到 /usr/local/
```

#### macOS

```bash
# 使用Homebrew
brew install opensmile

# 或从源代码编译
git clone https://github.com/audeering/opensmile.git
cd opensmile
bash build.sh
sudo cp build/progsrc/smilextract/SMILExtract /usr/local/bin/
sudo cp -r config /usr/local/share/opensmile/
```

#### Windows

1. 访问 https://github.com/audeering/opensmile/releases
2. 下载 `opensmile-3.0.2-windows-x86_64.zip`
3. 解压到 `C:\opensmile\`
4. 可执行文件路径为: `C:\opensmile\bin\SMILExtract.exe`

### 配置application.properties

编辑 `src/main/resources/application.properties`:

```properties
# 启用OpenSMILE
ai.opensmile.enabled=true

# 配置可执行文件路径（根据您的系统选择）
# Linux/Mac:
ai.opensmile.path=/usr/local/bin/SMILExtract

# Windows:
# ai.opensmile.path=C:/opensmile/bin/SMILExtract.exe

# 配置文件目录（可选，留空自动检测）
ai.opensmile.config.path=

# 配置类型（推荐eGeMAPSv02）
ai.opensmile.config.type=eGeMAPSv02
```

### 验证安装

```bash
# Linux/Mac
/usr/local/bin/SMILExtract -h

# Windows
C:\opensmile\bin\SMILExtract.exe -h

# 应该显示OpenSMILE的帮助信息
```

### 配置文件类型说明

| 类型 | 特征数 | 说明 | 适用场景 |
|------|-------|------|---------|
| **eGeMAPSv02** | 88 | 推荐使用，平衡性能和准确度 | 大多数应用 |
| GeMAPSv01b | 62 | 简化版本，处理速度快 | 快速分析 |
| emobase | 988 | 更全面的特征 | 研究用途 |
| ComParE_2016 | 6373 | 完整特征集 | 专业分析 |

---

## 🌐 百度AI配置

百度AI提供语音识别和自然语言处理服务。

### 获取API密钥

1. 访问 [百度智能云](https://cloud.baidu.com)
2. 注册/登录账号
3. 进入 **管理控制台**
4. 选择 **语音技术** 或 **自然语言处理**
5. 创建应用
6. 获取 **API Key** 和 **Secret Key**

### 配置步骤

编辑 `src/main/resources/application.properties`:

```properties
# 启用在线AI
ai.online.enabled=true
ai.provider=baidu

# 百度API配置
ai.baidu.api.key=YOUR_BAIDU_API_KEY_HERE
ai.baidu.api.secret=YOUR_BAIDU_SECRET_KEY_HERE
```

### 示例配置

```properties
# 实际配置示例（请替换为您自己的密钥）
ai.baidu.api.key=A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6
ai.baidu.api.secret=Q7R8S9T0U1V2W3X4Y5Z6A7B8C9D0E1F2
```

### 价格说明

百度AI提供免费额度：
- 语音识别: 每天50000次免费调用
- 自然语言处理: 每天50000次免费调用

详情查看: https://cloud.baidu.com/product/speech

---

## 🤖 OpenAI配置

OpenAI提供先进的大语言模型服务（GPT-3.5/GPT-4）。

### 获取API密钥

1. 访问 [OpenAI平台](https://platform.openai.com)
2. 注册/登录账号
3. 进入 **API Keys** 页面
4. 点击 **Create new secret key**
5. 复制生成的密钥（只显示一次！）

### 配置步骤

编辑 `src/main/resources/application.properties`:

```properties
# 启用在线AI
ai.online.enabled=true
ai.provider=openai

# OpenAI API配置
ai.openai.api.key=YOUR_OPENAI_API_KEY_HERE
ai.openai.api.endpoint=https://api.openai.com/v1
```

### 示例配置

```properties
# 实际配置示例（请替换为您自己的密钥）
ai.openai.api.key=sk-proj-1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z
ai.openai.api.endpoint=https://api.openai.com/v1
```

### 价格说明

OpenAI按使用量计费：
- GPT-3.5-turbo: $0.001 / 1K tokens
- GPT-4: $0.03 / 1K tokens (输入), $0.06 / 1K tokens (输出)

详情查看: https://openai.com/pricing

### 国内访问说明

如果在国内无法直接访问OpenAI API，可以：
1. 使用代理服务
2. 使用OpenAI兼容的国内服务（如Azure OpenAI Service）
3. 修改 `ai.openai.api.endpoint` 为代理地址

---

## ⚙️ 功能开关

### 启用/禁用功能

```properties
# 语音检测总开关
ai.voice.enabled=true

# OpenSMILE高级语音分析
ai.opensmile.enabled=true

# 在线AI增强（百度/OpenAI）
ai.online.enabled=true

# 摄像头动作检测
ai.camera.enabled=true
```

### 最小配置（仅本地功能）

如果您只想使用本地功能，不配置任何外部API：

```properties
# 基础语音检测（不使用OpenSMILE）
ai.voice.enabled=true
ai.opensmile.enabled=false

# 禁用在线AI
ai.online.enabled=false

# 启用摄像头检测
ai.camera.enabled=true
```

### 完整配置（所有功能）

```properties
# 启用所有功能
ai.voice.enabled=true
ai.opensmile.enabled=true
ai.opensmile.path=/usr/local/bin/SMILExtract
ai.opensmile.config.path=
ai.opensmile.config.type=eGeMAPSv02

ai.online.enabled=true
ai.provider=openai
ai.openai.api.key=sk-your-key-here
ai.openai.api.endpoint=https://api.openai.com/v1

# 或使用百度AI
# ai.provider=baidu
# ai.baidu.api.key=your-baidu-key
# ai.baidu.api.secret=your-baidu-secret

ai.camera.enabled=true
ai.camera.update.interval=5000
```

---

## 🔍 故障排查

### OpenSMILE问题

**问题**: "OpenSMILE未检测到"

**解决方案**:
```bash
# 检查可执行文件是否存在
ls -la /usr/local/bin/SMILExtract

# 添加执行权限
chmod +x /usr/local/bin/SMILExtract

# 测试运行
/usr/local/bin/SMILExtract -h
```

### 百度AI问题

**问题**: "API调用失败"

**解决方案**:
1. 检查API Key和Secret Key是否正确
2. 确认百度云账号已实名认证
3. 检查服务是否开通
4. 查看控制台配额是否用完

### OpenAI问题

**问题**: "无法访问OpenAI API"

**解决方案**:
1. 检查API Key是否有效
2. 确认账户有余额
3. 检查网络连接（国内可能需要代理）
4. 查看API限速配额

### 摄像头问题

**问题**: "摄像头启动失败"

**解决方案**:
1. 检查浏览器权限设置
2. 确认摄像头未被其他应用占用
3. 使用HTTPS访问（某些浏览器要求）
4. 尝试刷新页面重新授权

---

## 📞 技术支持

### OpenSMILE
- 官网: https://www.audeering.com/opensmile/
- GitHub: https://github.com/audeering/opensmile
- 文档: https://audeering.github.io/opensmile/

### 百度AI
- 官网: https://cloud.baidu.com
- 文档: https://cloud.baidu.com/doc/SPEECH/index.html
- 技术支持: https://cloud.baidu.com/forum

### OpenAI
- 官网: https://platform.openai.com
- 文档: https://platform.openai.com/docs
- 社区: https://community.openai.com

---

## 📝 配置检查清单

使用前请确认：

- [ ] OpenSMILE已安装并可执行（如果需要）
- [ ] application.properties中的路径配置正确
- [ ] API密钥已正确填写（如果使用在线服务）
- [ ] 网络连接正常（如果使用在线服务）
- [ ] 浏览器已授权摄像头和麦克风访问
- [ ] Java应用已重启以加载新配置

---

## 🚀 快速开始

1. **最简配置（仅本地功能）**:
   ```properties
   ai.voice.enabled=true
   ai.opensmile.enabled=false
   ai.online.enabled=false
   ai.camera.enabled=true
   ```

2. **启动应用**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **访问应用**:
   ```
   http://localhost:8080
   ```

4. **测试功能**:
   - 选择一个评估量表
   - 填写问卷
   - 尝试录音和摄像头检测
   - 提交查看AI分析结果

---

## 📄 更多信息

- 查看 `README.md` 了解项目整体信息
- 查看 `OPENSMILE_SETUP.md` 了解OpenSMILE详细配置
- 查看 `AI_FEATURES.md` 了解AI功能详情

---

*最后更新: 2024*
