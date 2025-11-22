# 6大高级智能功能快速开始指南

## 🚀 快速启动

### 启动应用
```bash
cd phq9-assessment-feat-voice-detection-multi-scale-search-online-ai
# 如果有mvnw
./mvnw spring-boot:run

# 或使用maven
mvn spring-boot:run
```

### 访问地址
- **主评估**: http://localhost:8080
- **智能仪表板**: http://localhost:8080/dashboard

---

## 📊 6大功能概览

### 1️⃣ 实时情绪波动监测
- 📈 自动分析情绪趋势
- ⚠️ 智能告警系统
- 📉 检测情绪尖峰和危机信号

**快速体验**:
1. 完成3次以上PHQ-9评估
2. 访问仪表板 → "情绪波动监测"Tab
3. 查看波形图和告警

### 2️⃣ AI日记系统
- 📝 记录想法和感受
- 🧠 自动识别9种认知扭曲
- 💡 提供CBT反思建议

**快速体验**:
1. 访问仪表板 → "AI日记系统"Tab
2. 输入: "今天一切都糟糕透了，永远不会好起来"
3. 点击"即时分析"查看认知扭曲识别

### 3️⃣ 复发风险预测
- 🔮 7/14/30天风险预测
- 🎯 风险等级分类
- 📋 个性化预防策略

**快速体验**:
1. 完成5次以上评估
2. 访问仪表板 → "复发预测"Tab
3. 查看风险评估和预防建议

### 4️⃣ 睡眠-情绪分析
- 😴 PSQI与PHQ-9相关性
- 📊 睡眠质量维度分解
- ⏰ 个性化睡眠时间表

**快速体验**:
1. 完成至少1次PSQI和1次PHQ-9评估
2. 访问仪表板 → "睡眠-情绪分析"Tab
3. 查看相关性和改善建议

### 5️⃣ 行为激活计划
- ✅ 自动生成个性化任务
- 📈 难度自适应
- 📊 任务效能追踪

**快速体验**:
1. 访问仪表板 → "行为激活计划"Tab
2. 点击"生成个性化任务"
3. 接受任务并标记完成

### 6️⃣ 生活质量仪表板
- 🎯 8维度追踪
- 🕸️ 雷达图可视化
- 📈 趋势分析和洞察

**快速体验**:
1. 访问仪表板 → "生活质量仪表板"Tab
2. 点击"记录今日生活质量"
3. 用滑块评分8个维度
4. 查看雷达图和建议

---

## 🔧 API端点速查

### 情绪监测
```bash
# 获取情绪波动分析
curl "http://localhost:8080/api/emotion-wave?userId=demo_user"

# 获取未读告警
curl "http://localhost:8080/api/alerts/unread?userId=demo_user"
```

### 日记系统
```bash
# 创建日记
curl -X POST "http://localhost:8080/api/journal" \
  -d "userId=demo_user&content=今天感觉不错&entryType=TEXT"

# 即时分析文本
curl -X POST "http://localhost:8080/api/journal/analyze" \
  -d "content=我总是失败，永远不会成功"
```

### 复发预测
```bash
# 获取30天预测
curl "http://localhost:8080/api/relapse-prediction?userId=demo_user&forecastDays=30"
```

### 睡眠分析
```bash
# 获取睡眠-情绪相关性
curl "http://localhost:8080/api/sleep-mood-correlation?userId=demo_user"
```

### 行为任务
```bash
# 生成5个任务
curl "http://localhost:8080/api/behavioral-tasks/generate?userId=demo_user&count=5"

# 获取任务历史
curl "http://localhost:8080/api/behavioral-tasks/history?userId=demo_user"
```

### 生活质量
```bash
# 记录生活质量
curl -X POST "http://localhost:8080/api/life-quality/record" \
  -d "userId=demo_user&sleepQuality=7&socialInteraction=6&physicalActivity=8&workProductivity=5&satisfaction=6&relationships=7&selfCare=6&enjoyableActivities=7"

# 获取仪表板
curl "http://localhost:8080/api/life-quality/dashboard?userId=demo_user"
```

---

## 📚 文档索引

- **ADVANCED_FEATURES.md** - 完整功能文档
- **IMPLEMENTATION_SUMMARY.md** - 实施技术文档
- **6_FEATURES_README.md** - 本快速指南

---

## 🎯 最佳实践

### 数据准备建议
1. **情绪监测**: 至少3-5次评估记录
2. **复发预测**: 至少3次评估记录
3. **睡眠分析**: 至少1次PSQI + 1次PHQ-9/GAD-7
4. **日记分析**: 随时可用，无需预先准备
5. **行为任务**: 随时可用，基于最新评估
6. **生活质量**: 随时可用，独立记录

### 测试用户旅程
```
Day 1: 完成PHQ-9评估
Day 2: 完成PHQ-9评估（不同分数）
Day 3: 写第一篇日记，查看认知模式
Day 4: 完成PHQ-9评估，查看情绪波动
Day 5: 接受3个行为激活任务
Day 6: 完成PSQI评估
Day 7: 记录生活质量，查看完整仪表板
Day 8: 完成行为任务，查看效能
Day 9: 完成PHQ-9评估，查看复发预测
Day 10: 再次写日记，对比认知模式变化
```

---

## 🔍 故障排查

### 常见问题

**Q: 仪表板显示"暂无数据"**  
A: 请先完成至少1次PHQ-9评估

**Q: 复发预测显示"需要至少3次评估"**  
A: 完成3次或以上评估后再访问

**Q: 睡眠分析无数据**  
A: 需要完成至少1次PSQI和1次PHQ-9/GAD-7评估

**Q: 行为任务都是EASY级别**  
A: 这是正常的，当PHQ-9分数≥15时推荐简单任务

**Q: 认知模式识别不准确**  
A: 目前使用关键词匹配，建议完整表达想法以提高准确度

### 调试模式
```bash
# 查看H2数据库
访问: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:phq9db
Username: sa
Password: (留空)

# 查看所有表
SELECT * FROM ASSESSMENT_RECORDS;
SELECT * FROM EMOTION_ALERTS;
SELECT * FROM JOURNAL_ENTRIES;
SELECT * FROM COGNITIVE_PATTERNS;
SELECT * FROM COMPLETED_BEHAVIORAL_TASKS;
SELECT * FROM LIFE_QUALITY_METRICS;
```

---

## 🌟 功能亮点

✅ **零配置**: 所有功能开箱即用  
✅ **实时分析**: 所有计算<1秒完成  
✅ **科学严谨**: 基于Apache Commons Math  
✅ **隐私安全**: 数据本地存储（H2内存数据库）  
✅ **用户友好**: 全中文界面，清晰的术语解释  
✅ **可视化**: Chart.js图表，信息清晰直观  

---

## 📞 获取帮助

- 查看完整文档: ADVANCED_FEATURES.md
- 查看实施细节: IMPLEMENTATION_SUMMARY.md
- 检查代码注释: service/ 目录下的各个服务类

---

**提示**: 首次使用建议按照"测试用户旅程"完整体验所有功能。

**默认用户**: 当前系统使用 `demo_user` 作为默认用户ID。生产环境请集成真实的用户认证系统。
