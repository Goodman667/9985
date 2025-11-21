# 🧠 智能PHQ-9抑郁风险评估系统

<div align="center">

![Java](https://img.shields.io/badge/Java-8-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.5-brightgreen.svg)
![AI](https://img.shields.io/badge/AI-Powered-blue.svg)
![ML](https://img.shields.io/badge/ML-Enabled-red.svg)

**一个融合人工智能和机器学习技术的现代化抑郁症自评系统**

</div>

## 📋 项目简介

本项目是一个基于Spring Boot的Web应用，实现了标准的PHQ-9（Patient Health Questionnaire-9）抑郁症自评量表，并**大幅增强了AI/ML功能**，提供更智能、更准确、更个性化的评估和建议。

### 什么是PHQ-9？

PHQ-9是一个广泛使用的抑郁症筛查工具，包含9个问题，基于DSM-IV抑郁症诊断标准。它简单、快速、有效，适用于临床和研究场景。

## ✨ 核心特性

### 🎯 传统功能
- ✅ 标准PHQ-9问卷评估（9个问题）
- ✅ 自动计算总分（0-27分）
- ✅ 风险等级分类（无/轻度/中度/中重度/重度）
- ✅ 基于答题的个性化建议
- ✅ 高危自杀意念检测
- ✅ 现代化响应式UI设计

### 🤖 AI/ML增强功能（新增）

#### 1. **机器学习风险预测模型**
- 多维度综合风险评分（0-100%）
- 不仅基于总分，还考虑答题模式、情感分析等因素
- 五级智能风险分类
- 比传统评分更敏感的风险识别

#### 2. **自然语言处理（NLP）情感分析**
- 用户可输入自由文本描述感受
- AI自动分析文本情感倾向
- 检测消极/积极词汇
- 识别抑郁、焦虑相关关键词
- 情感得分可视化展示

#### 3. **🎙️ OpenSMILE 专业语音特征提取** ⭐ 新功能
- 集成业界标准的 [openSMILE](https://github.com/audeering/opensmile) 音频分析工具
- 从语音中提取 88+ 专业声学特征（eGeMAPSv02 配置）
- 关键特征包括：
  - **基频 (F0)**: 检测音调单调性（抑郁症特征）
  - **响度**: 识别低能量语音（疲劳、抑郁）
  - **Jitter/Shimmer**: 音质微扰（压力、情绪不稳定）
  - **谐噪比 (HNR)**: 声音质量评估
  - **MFCC**: 全面的频谱特征
- 基于临床研究的抑郁风险评分算法
- 自动降级到内置方法（当 openSMILE 不可用时）
- 详见 [OpenSMILE 集成指南](OPENSMILE_SETUP.md)

#### 4. **智能推荐系统**
- 基于答题模式的个性化干预建议
- 包含10+类型的专业推荐
  - 睡眠改善、行为激活、正念冥想
  - 认知行为疗法、营养建议、运动疗法
  - 情绪日记、危机干预、社会支持
- 四级优先级分类（紧急/高/中/低）
- 每个推荐包含具体实施方法

#### 5. **异常检测系统**
- 自动检测答题模式异常
- 识别不认真作答（全选同一选项）
- 发现矛盾答案
- 检测极端响应模式
- 提高评估数据质量

#### 6. **历史趋势分析**
- 自动保存每次评估记录
- 分析评分变化趋势（改善/稳定/恶化）
- 使用线性回归预测下次评分
- Chart.js可视化趋势图表
- 支持多次评估对比

#### 7. **用户聚类分析**
- 基于答题模式智能分群
- 五种风险群体分类
- 每个群体对应定制化干预策略
- 提供针对性的专业建议

## 🛠️ 技术栈

### 后端
- **Java 8** - 编程语言
- **Spring Boot 2.7.5** - 核心框架
- **Spring MVC** - Web层
- **Spring Data JPA** - 数据持久化
- **Hibernate** - ORM框架
- **H2 Database** - 内存数据库
- **Apache Commons Math3** - 机器学习算法
- **Apache Commons Text** - NLP文本处理
- **Gson** - JSON处理
- **openSMILE** - 专业音频特征提取（可选）

### 前端
- **Thymeleaf** - 服务端模板引擎
- **Chart.js 3.9.1** - 数据可视化
- **现代CSS3** - 渐变、动画、响应式设计
- **原生JavaScript** - 前端交互

## 🚀 快速开始

### 前置要求
- Java 8 或更高版本
- Maven 3.6+

### 安装运行

```bash
# 克隆项目
git clone <repository-url>
cd phq9-assessment

# 编译项目
mvn clean package

# 运行应用
mvn spring-boot:run
```

### 🎙️ 可选：安装 openSMILE（语音分析增强）

为了获得最佳的语音分析效果，推荐安装 openSMILE：

```bash
# Ubuntu/Debian
git clone https://github.com/audeering/opensmile.git
cd opensmile
bash build.sh
sudo cp build/progsrc/smilextract/SMILExtract /usr/local/bin/
```

然后在 `application.properties` 中启用：
```properties
ai.opensmile.enabled=true
ai.opensmile.path=/usr/local/bin/SMILExtract
```

详细安装说明请参考 [OPENSMILE_SETUP.md](OPENSMILE_SETUP.md)

或者直接运行打包好的jar：

```bash
java -jar target/phq9-assessment-0.0.1-SNAPSHOT.jar
```

### 访问应用

打开浏览器访问：
```
http://localhost:8080
```

### 开发调试

H2数据库控制台（查看数据）：
```
http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:phq9db
用户名: sa
密码: (留空)
```

## 📊 AI/ML算法详解

### 机器学习风险评分
```
综合风险 = 基础风险(50%) + 模式风险(25%) + 情感风险(15%) + 一致性风险(10%)
```

- **基础风险**：PHQ-9总分归一化
- **模式风险**：检测高危答题组合和模式
- **情感风险**：基于NLP情感分析的消极程度
- **一致性风险**：答题可靠性评估

### 情感分析算法
1. 文本预处理和分词
2. 匹配中文情感词典（50+词汇）
3. 累加加权情感得分
4. 归一化到[-1, 1]区间
5. 分类为消极/中性/积极

### 趋势预测算法
- 使用最小二乘法进行线性回归
- 计算评分变化斜率
- 预测公式：下次得分 = 最近得分 + 斜率

## 📱 界面展示

### 评估表单
- 9个标准PHQ-9问题
- 单选按钮组（0-3分）
- **新增**：AI情感分析文本框
- 现代化紫色渐变背景
- AI标识和说明

### 结果页面（多维度展示）
1. **PHQ-9传统评分**：总分 + 风险等级
2. **ML风险评估**：百分比评分 + 五级分类
3. **情感分析结果**：情感倾向 + 检测词汇
4. **智能分群**：用户群体 + 干预策略
5. **趋势图表**：历史评分可视化
6. **异常检测**：答题模式警告
7. **AI推荐列表**：个性化干预建议
8. **危机预警**：高风险情况自动触发

## 🗂️ 项目结构

```
src/main/java/com/example/phq9assessment/
├── Phq9AssessmentApplication.java      # 主启动类
├── controller/
│   └── AssessmentController.java       # MVC控制器（已增强）
├── entity/
│   └── AssessmentRecord.java          # JPA实体（评估记录）
├── model/
│   └── AssessmentResult.java          # DTO结果对象（已扩展）
├── repository/
│   └── AssessmentRecordRepository.java # JPA数据仓库
└── service/                            # 业务逻辑层（全新）
    ├── SentimentAnalysisService.java   # NLP情感分析
    ├── MachineLearningService.java     # ML风险预测
    ├── AnomalyDetectionService.java    # 异常检测
    ├── RecommendationService.java      # 智能推荐
    ├── VoiceDetectionService.java      # 语音情感检测
    └── OpenSmileService.java           # OpenSMILE集成（新）

src/main/resources/
├── templates/
│   └── index.html                      # Thymeleaf模板（大幅增强）
├── static/
│   └── css/                            # CSS样式文件
└── application.properties              # 配置文件（已更新）
```

## 🔬 AI功能使用示例

### 1. 机器学习评分
```
传统PHQ-9得分：15分（中重度抑郁）
ML综合风险评分：68.5% （较高风险）

ML评分考虑了：
- 问题9有自杀意念 (+40%)
- 多个高分项组合 (+30%)
- 情感分析显示极度消极 (+15%)
```

### 2. 情感分析
```
输入文本："最近总是感觉很累，对什么都提不起兴趣，晚上也睡不好，
感觉很绝望..."

AI分析结果：
- 情感倾向：消极 (-0.78)
- 检测词汇：疲惫、失眠、绝望
- 关键标签：抑郁(失去兴趣)、焦虑(失眠)
```

### 3. 趋势分析
```
历史评分：[18, 16, 14, 13]
趋势判断：改善中 ↓
变化斜率：-1.67 分/次
预测下次：约 11 分
```

## ⚠️ 免责声明

本系统仅供**参考、辅助和教育**使用，不能替代专业医疗诊断。所有AI/ML功能都是基于统计模型和规则算法，可能存在误差。

**如果您或您认识的人有严重抑郁症状或自杀想法，请立即：**
- 拨打全国心理援助热线：**400-161-9995**（24小时）
- 前往最近的急诊室
- 联系心理健康专业人士

## 📚 参考资料

- [PHQ-9官方文档](https://www.phqscreeners.com/)
- [DSM-5抑郁症诊断标准](https://www.psychiatry.org/dsm5)
- [Apache Commons Math](https://commons.apache.org/proper/commons-math/)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [openSMILE GitHub](https://github.com/audeering/opensmile)
- [eGeMAPS 特征集](https://sail.usc.edu/publications/files/eyben-preprinttaffc-2015.pdf)

## 🤝 贡献

欢迎提交Issue和Pull Request！

如果您有任何改进建议或发现bug，请：
1. Fork本项目
2. 创建新分支 (`git checkout -b feature/AmazingFeature`)
3. 提交改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 📝 未来计划

- [x] ✅ 集成 openSMILE 专业音频特征提取
- [ ] 深度学习模型集成（TensorFlow/DL4J）
- [ ] 预训练中文BERT模型用于情感分析
- [ ] 语音特征的深度学习分类器
- [ ] 多用户系统和账号管理
- [ ] 移动端适配和App开发
- [ ] 专业医生审核系统
- [ ] 数据分析仪表板
- [ ] API接口开放
- [ ] 多语言支持

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 👨‍💻 作者

开发团队 - PHQ-9智能评估项目组

---

<div align="center">

**如果这个项目对您有帮助，请给我们一个 ⭐️**

Made with ❤️ and 🤖 AI

</div>
