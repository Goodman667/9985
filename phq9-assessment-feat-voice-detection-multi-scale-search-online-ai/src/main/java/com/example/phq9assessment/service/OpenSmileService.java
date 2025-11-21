package com.example.phq9assessment.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * OpenSMILE 音频特征提取服务
 * OpenSMILE是一个专业的音频特征提取工具，可以提取大量声学特征用于情感识别和抑郁检测
 * GitHub: https://github.com/audeering/opensmile
 */
@Service
public class OpenSmileService {
    
    @Value("${ai.opensmile.enabled:false}")
    private boolean opensmileEnabled;
    
    @Value("${ai.opensmile.path:/usr/local/bin/SMILExtract}")
    private String opensmilePath;
    
    @Value("${ai.opensmile.config.path:}")
    private String configPath;
    
    @Value("${ai.opensmile.config.type:eGeMAPSv02}")
    private String configType;
    
    private final Gson gson = new Gson();
    
    /**
     * 使用 openSMILE 提取音频特征
     * @param audioBase64 Base64编码的音频数据
     * @return 提取的特征结果
     */
    public OpenSmileResult extractFeatures(String audioBase64) {
        OpenSmileResult result = new OpenSmileResult();
        result.setSuccess(false);
        
        if (!opensmileEnabled) {
            result.setErrorMessage("OpenSMILE未启用");
            return result;
        }
        
        if (audioBase64 == null || audioBase64.trim().isEmpty()) {
            result.setErrorMessage("音频数据为空");
            return result;
        }
        
        File tempAudioFile = null;
        File tempOutputFile = null;
        
        try {
            // 1. 将Base64音频数据保存为临时WAV文件
            tempAudioFile = saveAudioToTempFile(audioBase64);
            
            // 2. 创建临时输出CSV文件
            tempOutputFile = File.createTempFile("opensmile_output_", ".csv");
            
            // 3. 调用openSMILE提取特征
            boolean extracted = runOpenSmile(tempAudioFile, tempOutputFile);
            
            if (!extracted) {
                result.setErrorMessage("OpenSMILE特征提取失败");
                return result;
            }
            
            // 4. 解析CSV输出文件
            Map<String, Double> features = parseOpenSmileOutput(tempOutputFile);
            
            if (features.isEmpty()) {
                result.setErrorMessage("特征解析失败");
                return result;
            }
            
            // 5. 计算抑郁风险评分
            double depressionScore = calculateDepressionScore(features);
            String depressionLevel = categorizeDepressionLevel(depressionScore);
            
            // 6. 生成结构化声学分析
            Map<String, Double> acousticSummary = generateAcousticSummary(features);
            Map<String, Double> emotionalIndicators = generateEmotionalIndicators(features);
            Map<String, Object> audioStats = generateAudioStats(features, tempAudioFile);
            Map<String, Double> topFeatures = extractTopFeatures(features, 20);
            
            result.setSuccess(true);
            result.setFeatures(features);
            result.setDepressionScore(depressionScore);
            result.setDepressionLevel(depressionLevel);
            result.setConfigType(configType);
            result.setFeatureCount(features.size());
            result.setAcousticSummary(acousticSummary);
            result.setEmotionalIndicators(emotionalIndicators);
            result.setAudioStats(audioStats);
            result.setTopFeatures(topFeatures);
            
        } catch (Exception e) {
            result.setErrorMessage("处理异常: " + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempAudioFile != null && tempAudioFile.exists()) {
                tempAudioFile.delete();
            }
            if (tempOutputFile != null && tempOutputFile.exists()) {
                tempOutputFile.delete();
            }
        }
        
        return result;
    }
    
    /**
     * 将Base64音频数据保存为临时WAV文件
     */
    private File saveAudioToTempFile(String audioBase64) throws IOException {
        byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
        File tempFile = File.createTempFile("audio_", ".wav");
        
        // 如果是纯PCM数据，需要添加WAV头
        byte[] wavData = addWavHeader(audioBytes);
        
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(wavData);
        }
        
        return tempFile;
    }
    
    /**
     * 为PCM数据添加WAV文件头
     */
    private byte[] addWavHeader(byte[] pcmData) throws IOException {
        int sampleRate = 16000; // 默认采样率
        int channels = 1; // 单声道
        int bitsPerSample = 16; // 16位
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // RIFF头
        outputStream.write("RIFF".getBytes());
        outputStream.write(intToByteArray(36 + pcmData.length), 0, 4);
        outputStream.write("WAVE".getBytes());
        
        // fmt子块
        outputStream.write("fmt ".getBytes());
        outputStream.write(intToByteArray(16), 0, 4); // fmt块大小
        outputStream.write(shortToByteArray((short) 1), 0, 2); // 音频格式（PCM=1）
        outputStream.write(shortToByteArray((short) channels), 0, 2); // 声道数
        outputStream.write(intToByteArray(sampleRate), 0, 4); // 采样率
        outputStream.write(intToByteArray(sampleRate * channels * bitsPerSample / 8), 0, 4); // 字节率
        outputStream.write(shortToByteArray((short) (channels * bitsPerSample / 8)), 0, 2); // 块对齐
        outputStream.write(shortToByteArray((short) bitsPerSample), 0, 2); // 位深度
        
        // data子块
        outputStream.write("data".getBytes());
        outputStream.write(intToByteArray(pcmData.length), 0, 4);
        outputStream.write(pcmData);
        
        return outputStream.toByteArray();
    }
    
    private byte[] intToByteArray(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 24) & 0xFF)
        };
    }
    
    private byte[] shortToByteArray(short value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF)
        };
    }
    
    /**
     * 调用openSMILE命令行工具提取特征
     */
    private boolean runOpenSmile(File inputFile, File outputFile) {
        try {
            // 构建openSMILE命令
            List<String> command = new ArrayList<String>();
            command.add(opensmilePath);
            command.add("-C");
            
            // 配置文件路径
            String configFilePath = getConfigFilePath();
            command.add(configFilePath);
            
            command.add("-I");
            command.add(inputFile.getAbsolutePath());
            command.add("-O");
            command.add(outputFile.getAbsolutePath());
            command.add("-csvoutput");
            command.add(outputFile.getAbsolutePath());
            command.add("-instname");
            command.add("audio");
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // 读取输出（用于调试）
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            
            // 如果失败，记录输出信息用于调试
            if (exitCode != 0) {
                System.err.println("OpenSMILE执行失败，退出码: " + exitCode);
                System.err.println("配置文件路径: " + configFilePath);
                System.err.println("配置文件是否存在: " + new File(configFilePath).exists());
                System.err.println("OpenSMILE可执行文件是否存在: " + new File(opensmilePath).exists());
                System.err.println("命令: " + String.join(" ", command));
                System.err.println("输出: " + output.toString());
            }
            
            return exitCode == 0;
            
        } catch (Exception e) {
            System.err.println("OpenSMILE执行异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取配置文件路径
     */
    private String getConfigFilePath() {
        String configFileName;
        String configSubDir = "";
        
        if ("eGeMAPSv02".equals(configType)) {
            configFileName = "eGeMAPSv02.conf";
            configSubDir = "egemaps/v02/";
        } else if ("GeMAPSv01b".equals(configType)) {
            configFileName = "GeMAPSv01b.conf";
            configSubDir = "gemaps/v01b/";
        } else if ("emobase".equals(configType)) {
            configFileName = "emobase.conf";
        } else if ("ComParE_2016".equals(configType)) {
            configFileName = "ComParE_2016.conf";
        } else {
            configFileName = "eGeMAPSv02.conf"; // 默认
            configSubDir = "egemaps/v02/";
        }
        
        if (configPath != null && !configPath.trim().isEmpty()) {
            try {
                // 使用java.nio.Path进行跨平台路径处理
                Path basePath = Paths.get(configPath.trim());
                
                // 构建完整的配置文件路径
                Path configFilePath;
                if (!configSubDir.isEmpty()) {
                    configFilePath = basePath.resolve(configSubDir).resolve(configFileName);
                } else {
                    configFilePath = basePath.resolve(configFileName);
                }
                
                String resolvedPath = configFilePath.toString();
                
                // 调试日志
                System.out.println("OpenSMILE配置路径调试:");
                System.out.println("  原始config.path: " + configPath);
                System.out.println("  配置类型: " + configType);
                System.out.println("  配置子目录: " + configSubDir);
                System.out.println("  配置文件名: " + configFileName);
                System.out.println("  解析后的路径: " + resolvedPath);
                System.out.println("  规范化路径: " + configFilePath.normalize().toString());
                System.out.println("  文件是否存在: " + configFilePath.toFile().exists());
                
                // 检查文件是否存在，如果存在则返回
                if (configFilePath.toFile().exists()) {
                    return resolvedPath;
                }
                
                // 如果文件不存在，尝试其他可能的路径组合
                System.out.println("配置文件不存在，尝试其他路径组合...");
                
                // 尝试不使用子目录
                Path directPath = basePath.resolve(configFileName);
                if (directPath.toFile().exists()) {
                    System.out.println("找到直接路径: " + directPath.toString());
                    return directPath.toString();
                }
                
                // 尝试在子目录中查找（递归搜索）
                if (basePath.toFile().exists() && basePath.toFile().isDirectory()) {
                    String foundPath = findConfigFileRecursively(basePath.toFile(), configFileName);
                    if (foundPath != null) {
                        System.out.println("递归找到配置文件: " + foundPath);
                        return foundPath;
                    }
                }
                
                System.out.println("所有路径尝试失败，返回默认构造路径");
                return resolvedPath;
                
            } catch (Exception e) {
                System.err.println("路径解析异常: " + e.getMessage());
                e.printStackTrace();
                // 降级到原始逻辑
                return configPath.replace('\\', '/') + "/" + configSubDir + configFileName;
            }
        }
        
        // 使用默认配置文件（通常在openSMILE安装目录下）
        String opensmileDir = new File(opensmilePath).getParent();
        if (opensmileDir == null) {
            opensmileDir = "";
        }
        
        try {
            // 使用Path类处理默认路径
            Path opensmileBasePath = Paths.get(opensmileDir);
            
            // 尝试多种可能的配置文件位置
            String[] possibleRelativePaths = {
                "../config/" + configSubDir + configFileName,
                "../../config/" + configSubDir + configFileName,
                "config/" + configSubDir + configFileName,
                "../share/opensmile/config/" + configSubDir + configFileName,
                "/usr/local/share/opensmile/config/" + configSubDir + configFileName,
                "/usr/share/opensmile/config/" + configSubDir + configFileName
            };
            
            for (String relativePath : possibleRelativePaths) {
                Path candidatePath;
                if (relativePath.startsWith("/")) {
                    // 绝对路径
                    candidatePath = Paths.get(relativePath);
                } else {
                    // 相对于opensmile目录的路径
                    candidatePath = opensmileBasePath.resolve(relativePath);
                }
                
                File configFile = candidatePath.toFile();
                if (configFile.exists()) {
                    System.out.println("找到默认配置文件: " + candidatePath.toString());
                    return candidatePath.toString();
                }
            }
            
            // 如果都没找到，返回第一个默认路径
            Path defaultPath = opensmileBasePath.resolve(possibleRelativePaths[0]);
            System.out.println("返回默认配置路径: " + defaultPath.toString());
            return defaultPath.toString();
            
        } catch (Exception e) {
            System.err.println("默认路径解析异常: " + e.getMessage());
            e.printStackTrace();
            // 降级到原始逻辑
            return opensmileDir.replace('\\', '/') + "/../config/" + configSubDir + configFileName;
        }
    }
    
    /**
     * 递归搜索配置文件
     */
    private String findConfigFileRecursively(File dir, String fileName) {
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                String found = findConfigFileRecursively(file, fileName);
                if (found != null) {
                    return found;
                }
            } else if (file.getName().equals(fileName)) {
                return file.getAbsolutePath();
            }
        }
        
        return null;
    }
    
    /**
     * 解析openSMILE输出的CSV文件
     */
    private Map<String, Double> parseOpenSmileOutput(File csvFile) {
        Map<String, Double> features = new HashMap<String, Double>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return features;
            }
            
            // 解析特征名称
            String[] headers = headerLine.split(";");
            
            // 读取特征值
            String dataLine = reader.readLine();
            if (dataLine == null) {
                return features;
            }
            
            String[] values = dataLine.split(";");
            
            // 跳过前面的元数据列（如name, frameTime等）
            int startIndex = Math.min(2, values.length);
            
            for (int i = startIndex; i < Math.min(headers.length, values.length); i++) {
                try {
                    String featureName = headers[i].trim();
                    double featureValue = Double.parseDouble(values[i].trim());
                    
                    // 处理NaN和无穷大
                    if (!Double.isNaN(featureValue) && !Double.isInfinite(featureValue)) {
                        features.put(featureName, featureValue);
                    }
                } catch (NumberFormatException e) {
                    // 跳过无法解析的值
                }
            }
            
        } catch (IOException e) {
            // 返回空特征
        }
        
        return features;
    }
    
    /**
     * 基于openSMILE特征计算抑郁风险评分
     * 使用研究表明的关键声学指标
     */
    private double calculateDepressionScore(Map<String, Double> features) {
        double score = 0.0;
        int validFeatures = 0;
        
        // 1. F0（基频）相关特征 - 抑郁症患者通常音调单调，F0变化小
        Double f0Mean = getFeature(features, "F0semitoneFrom27.5Hz_sma3nz_amean");
        Double f0Std = getFeature(features, "F0semitoneFrom27.5Hz_sma3nz_stddevNorm");
        Double f0Range = getFeature(features, "F0semitoneFrom27.5Hz_sma3nz_percentile20.0", 
                                     "F0semitoneFrom27.5Hz_sma3nz_percentile80.0");
        
        if (f0Std != null) {
            // F0标准差越小，音调越单调，抑郁风险越高
            double f0Score = 1.0 - Math.min(1.0, f0Std / 10.0);
            score += f0Score * 0.25;
            validFeatures++;
        }
        
        // 2. 响度（Loudness）特征 - 抑郁症患者通常声音较小
        Double loudnessMean = getFeature(features, "loudness_sma3_amean");
        Double loudnessStd = getFeature(features, "loudness_sma3_stddevNorm");
        
        if (loudnessMean != null) {
            // 响度低于阈值表示低能量
            double loudnessScore = 1.0 - Math.min(1.0, (loudnessMean + 60.0) / 60.0);
            score += Math.max(0, loudnessScore) * 0.20;
            validFeatures++;
        }
        
        // 3. Jitter（音高微扰）- 抑郁和压力会导致声音不稳定
        Double jitter = getFeature(features, "jitterLocal_sma3nz_amean");
        if (jitter != null) {
            // Jitter值越高，声音越不稳定
            double jitterScore = Math.min(1.0, jitter * 100.0);
            score += jitterScore * 0.15;
            validFeatures++;
        }
        
        // 4. Shimmer（振幅微扰）- 声音质量指标
        Double shimmer = getFeature(features, "shimmerLocaldB_sma3nz_amean");
        if (shimmer != null) {
            // Shimmer值越高，声音质量越差
            double shimmerScore = Math.min(1.0, shimmer / 2.0);
            score += shimmerScore * 0.15;
            validFeatures++;
        }
        
        // 5. HNR（谐噪比）- 抑郁症患者HNR通常较低
        Double hnr = getFeature(features, "HNRdBACF_sma3nz_amean");
        if (hnr != null) {
            // HNR越低，声音质量越差，抑郁风险越高
            double hnrScore = 1.0 - Math.min(1.0, hnr / 20.0);
            score += Math.max(0, hnrScore) * 0.15;
            validFeatures++;
        }
        
        // 6. 语速相关特征（通过loudness变化估计）
        if (loudnessStd != null) {
            // 标准差较小表示语音单调、缺乏变化
            double variabilityScore = 1.0 - Math.min(1.0, loudnessStd / 20.0);
            score += variabilityScore * 0.10;
            validFeatures++;
        }
        
        // 如果没有有效特征，返回中性值
        if (validFeatures == 0) {
            return 0.5;
        }
        
        // 归一化分数到0-1范围
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * 生成声学特征摘要
     */
    private Map<String, Double> generateAcousticSummary(Map<String, Double> features) {
        Map<String, Double> summary = new HashMap<>();
        
        // 基频（Pitch）特征
        Double f0Mean = getFeature(features, "F0semitoneFrom27.5Hz_sma3nz_amean", "F0final_sma3nz_amean");
        Double f0Std = getFeature(features, "F0semitoneFrom27.5Hz_sma3nz_stddevNorm", "F0final_sma3nz_stddevNorm");
        Double f0Min = getFeature(features, "F0semitoneFrom27.5Hz_sma3nz_percentile20.0", "F0final_sma3nz_percentile20.0");
        Double f0Max = getFeature(features, "F0semitoneFrom27.5Hz_sma3nz_percentile80.0", "F0final_sma3nz_percentile80.0");
        
        if (f0Mean != null) {
            summary.put("基频均值", f0Mean);
        }
        if (f0Std != null) {
            summary.put("基频标准差", f0Std);
        }
        if (f0Min != null && f0Max != null) {
            summary.put("基频范围", f0Max - f0Min);
        }
        
        // 响度（Loudness）特征
        Double loudnessMean = getFeature(features, "loudness_sma3_amean");
        Double loudnessStd = getFeature(features, "loudness_sma3_stddevNorm");
        Double loudnessMin = getFeature(features, "loudness_sma3_percentile20.0");
        Double loudnessMax = getFeature(features, "loudness_sma3_percentile80.0");
        
        if (loudnessMean != null) {
            summary.put("响度均值", loudnessMean);
        }
        if (loudnessStd != null) {
            summary.put("响度变化", loudnessStd);
        }
        if (loudnessMin != null && loudnessMax != null) {
            summary.put("响度范围", loudnessMax - loudnessMin);
        }
        
        // 音质特征
        Double jitter = getFeature(features, "jitterLocal_sma3nz_amean");
        Double shimmer = getFeature(features, "shimmerLocaldB_sma3nz_amean");
        Double hnr = getFeature(features, "HNRdBACF_sma3nz_amean");
        
        if (jitter != null) {
            summary.put("音高微扰", jitter * 100.0); // 转换为百分比
        }
        if (shimmer != null) {
            summary.put("振幅微扰", shimmer);
        }
        if (hnr != null) {
            summary.put("谐噪比", hnr);
        }
        
        // 语速相关特征（通过能量变化估计）
        Double energyMean = getFeature(features, "pcm_RMSenergy_sma3_amean");
        Double energyStd = getFeature(features, "pcm_RMSenergy_sma3_stddevNorm");
        
        if (energyMean != null) {
            summary.put("能量均值", energyMean);
        }
        if (energyStd != null) {
            summary.put("能量变化", energyStd);
            // 基于能量变化估计语速（变化越大，语速可能越快）
            summary.put("语速指标", Math.min(1.0, energyStd / 10.0));
        }
        
        // 频谱特征
        Double spectralFlux = getFeature(features, "spectralFlux_sma3_amean");
        Double spectralCentroid = getFeature(features, "spectralCentroid_sma3_amean");
        Double mfccMean = getFeature(features, "mfcc_sma3_amean");
        
        if (spectralFlux != null) {
            summary.put("频谱流量", spectralFlux);
        }
        if (spectralCentroid != null) {
            summary.put("频谱重心", spectralCentroid);
        }
        if (mfccMean != null) {
            summary.put("MFCC均值", mfccMean);
        }
        
        return summary;
    }
    
    /**
     * 生成情感指标
     */
    private Map<String, Double> generateEmotionalIndicators(Map<String, Double> features) {
        Map<String, Double> indicators = new HashMap<>();
        
        // 活跃度指标（基于响度和基频变化）
        Double loudnessStd = getFeature(features, "loudness_sma3_stddevNorm");
        Double f0Std = getFeature(features, "F0semitoneFrom27.5Hz_sma3nz_stddevNorm");
        
        if (loudnessStd != null && f0Std != null) {
            double activity = (loudnessStd / 20.0 + f0Std / 10.0) / 2.0;
            indicators.put("活跃度", Math.min(1.0, activity));
        }
        
        // 紧张度指标（基于jitter和shimmer）
        Double jitter = getFeature(features, "jitterLocal_sma3nz_amean");
        Double shimmer = getFeature(features, "shimmerLocaldB_sma3nz_amean");
        
        if (jitter != null && shimmer != null) {
            double tension = (jitter * 100.0 + shimmer / 2.0) / 2.0;
            indicators.put("紧张度", Math.min(1.0, tension));
        }
        
        // 情绪稳定性（基于HNR和基频稳定性）
        Double hnr = getFeature(features, "HNRdBACF_sma3nz_amean");
        Double f0Range = getFeature(features, "F0semitoneFrom27.5Hz_sma3nz_percentile20.0", 
                                     "F0semitoneFrom27.5Hz_sma3nz_percentile80.0");
        
        if (hnr != null && f0Range != null) {
            double stability = (hnr / 20.0 + (1.0 - Math.min(1.0, f0Range / 50.0))) / 2.0;
            indicators.put("情绪稳定性", Math.max(0.0, stability));
        }
        
        // 抑郁倾向（基于多个特征的组合）
        double depressionScore = calculateDepressionScore(features);
        indicators.put("抑郁倾向", depressionScore);
        
        // 能量水平
        Double energyMean = getFeature(features, "pcm_RMSenergy_sma3_amean");
        if (energyMean != null) {
            double energyLevel = Math.min(1.0, energyMean / 1000.0);
            indicators.put("能量水平", energyLevel);
        }
        
        return indicators;
    }
    
    /**
     * 生成音频统计信息
     */
    private Map<String, Object> generateAudioStats(Map<String, Double> features, File audioFile) {
        Map<String, Object> stats = new HashMap<>();
        
        // 文件信息
        if (audioFile != null && audioFile.exists()) {
            stats.put("文件大小", audioFile.length());
            stats.put("文件名", audioFile.getName());
        }
        
        // 特征统计
        stats.put("特征总数", features.size());
        stats.put("有效特征数", features.values().stream().mapToDouble(d -> d).filter(d -> !Double.isNaN(d)).count());
        
        // 音频时长估计（基于PCM数据）
        Double zeroCrossingRate = getFeature(features, "pcm_zcr_sma3_amean");
        if (zeroCrossingRate != null) {
            // 粗略估计：假设16kHz采样率
            double estimatedDuration = 1000.0 / (zeroCrossingRate * 16000.0 / 1000.0);
            stats.put("估计时长(ms)", (int) estimatedDuration);
        }
        
        // 音频质量指标
        Double signalToNoise = getFeature(features, "audSpec_Rflux_sma3_amean");
        if (signalToNoise != null) {
            stats.put("信噪比指标", signalToNoise);
        }
        
        // 配置信息
        stats.put("配置类型", configType);
        stats.put("处理时间", System.currentTimeMillis());
        
        return stats;
    }
    
    /**
     * 提取top-N特征（按方差或重要性排序）
     */
    private Map<String, Double> extractTopFeatures(Map<String, Double> features, int topN) {
        Map<String, Double> topFeatures = new LinkedHashMap<>();
        
        // 优先级特征列表（基于声学重要性）
        List<String> priorityFeatures = Arrays.asList(
            "F0semitoneFrom27.5Hz_sma3nz_amean",           // 基频均值
            "loudness_sma3_amean",                          // 响度均值
            "jitterLocal_sma3nz_amean",                    // 音高微扰
            "shimmerLocaldB_sma3nz_amean",                 // 振幅微扰
            "HNRdBACF_sma3nz_amean",                       // 谐噪比
            "pcm_RMSenergy_sma3_amean",                    // RMS能量
            "spectralFlux_sma3_amean",                     // 频谱流量
            "spectralCentroid_sma3_amean",                 // 频谱重心
            "mfcc_sma3_amean",                             // MFCC系数
            "pcm_zcr_sma3_amean",                          // 过零率
            "F0semitoneFrom27.5Hz_sma3nz_stddevNorm",     // 基频标准差
            "loudness_sma3_stddevNorm",                    // 响度标准差
            "audSpec_Rflux_sma3_amean",                    // 频谱相对流量
            "voiceProb_sma3nz_amean",                      // 语音概率
            "logRelF0HarmMean_sma3nz_amean",               // 相对基频均值
            "alphaRatio_sma3nz_amean",                     // 频谱倾斜度
            "hammarbergIndex_sma3nz_amean",                // Hammarberg指数
            "equivalentSoundLevel_dBp_sma3_amean",        // 等效声级
            "spectralSkewness_sma3nz_amean",               // 频谱偏度
            "spectralKurtosis_sma3nz_amean"                // 频谱峰度
        );
        
        // 首先添加优先级特征
        int count = 0;
        for (String featureName : priorityFeatures) {
            if (count >= topN) break;
            
            Double value = getFeature(features, featureName);
            if (value != null && !Double.isNaN(value)) {
                topFeatures.put(featureName, value);
                count++;
            }
        }
        
        // 如果优先级特征不够，添加其他特征
        if (count < topN) {
            for (Map.Entry<String, Double> entry : features.entrySet()) {
                if (count >= topN) break;
                
                if (!topFeatures.containsKey(entry.getKey()) && 
                    !Double.isNaN(entry.getValue()) && 
                    !Double.isInfinite(entry.getValue())) {
                    
                    topFeatures.put(entry.getKey(), entry.getValue());
                    count++;
                }
            }
        }
        
        return topFeatures;
    }
    
    /**
     * 从特征Map中获取特征值
     */
    private Double getFeature(Map<String, Double> features, String... featureNames) {
        for (String name : featureNames) {
            Double value = features.get(name);
            if (value != null) {
                return value;
            }
            // 尝试模糊匹配（包含关键词）
            for (Map.Entry<String, Double> entry : features.entrySet()) {
                if (entry.getKey().contains(name) || name.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * 计算两个特征的差值（用于范围计算）
     */
    private Double getFeature(Map<String, Double> features, String feature1, String feature2) {
        Double val1 = getFeature(features, feature1);
        Double val2 = getFeature(features, feature2);
        if (val1 != null && val2 != null) {
            return Math.abs(val2 - val1);
        }
        return null;
    }
    
    /**
     * 分类抑郁风险等级
     */
    private String categorizeDepressionLevel(double score) {
        if (score < 0.2) {
            return "低风险";
        } else if (score < 0.4) {
            return "轻度风险";
        } else if (score < 0.6) {
            return "中度风险";
        } else if (score < 0.8) {
            return "较高风险";
        } else {
            return "高风险";
        }
    }
    
    /**
     * 检查openSMILE是否可用
     */
    public boolean isAvailable() {
        if (!opensmileEnabled) {
            System.out.println("OpenSMILE未启用");
            return false;
        }
        
        File opensmileFile = new File(opensmilePath);
        boolean opensmileExists = opensmileFile.exists();
        boolean opensmileExecutable = opensmileFile.canExecute();
        
        System.out.println("OpenSMILE可执行文件路径: " + opensmilePath);
        System.out.println("OpenSMILE可执行文件存在: " + opensmileExists);
        System.out.println("OpenSMILE可执行文件可执行: " + opensmileExecutable);
        
        if (!opensmileExists) {
            System.err.println("OpenSMILE可执行文件不存在: " + opensmilePath);
            return false;
        }
        
        if (!opensmileExecutable) {
            System.err.println("OpenSMILE可执行文件不可执行: " + opensmilePath);
            return false;
        }
        
        // 检查配置文件
        String configFilePath = getConfigFilePath();
        File configFile = new File(configFilePath);
        boolean configExists = configFile.exists();
        
        System.out.println("OpenSMILE配置文件路径: " + configFilePath);
        System.out.println("OpenSMILE配置文件存在: " + configExists);
        
        if (!configExists) {
            System.err.println("OpenSMILE配置文件不存在: " + configFilePath);
            return false;
        }
        
        return true;
    }
    
    /**
     * 测试OpenSMILE配置并输出详细信息
     */
    public String testConfiguration() {
        StringBuilder info = new StringBuilder();
        
        info.append("=== OpenSMILE配置测试 ===\n");
        info.append("启用状态: ").append(opensmileEnabled ? "已启用" : "未启用").append("\n");
        
        if (!opensmileEnabled) {
            info.append("OpenSMILE未启用，请在application.properties中设置 ai.opensmile.enabled=true\n");
            return info.toString();
        }
        
        info.append("可执行文件路径: ").append(opensmilePath).append("\n");
        
        File opensmileFile = new File(opensmilePath);
        info.append("可执行文件存在: ").append(opensmileFile.exists()).append("\n");
        info.append("可执行文件可执行: ").append(opensmileFile.canExecute()).append("\n");
        
        info.append("配置类型: ").append(configType).append("\n");
        info.append("配置目录路径: ").append(configPath).append("\n");
        
        String configFilePath = getConfigFilePath();
        info.append("完整配置文件路径: ").append(configFilePath).append("\n");
        
        File configFile = new File(configFilePath);
        info.append("配置文件存在: ").append(configFile.exists()).append("\n");
        
        if (configFile.exists()) {
            info.append("配置文件大小: ").append(configFile.length()).append(" bytes\n");
        }
        
        // 尝试列出配置目录内容（用于调试）
        if (configPath != null && !configPath.trim().isEmpty()) {
            File configDir = new File(configPath);
            if (configDir.exists() && configDir.isDirectory()) {
                info.append("配置目录内容:\n");
                File[] files = configDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            info.append("  [DIR]  ").append(file.getName()).append("\n");
                        } else {
                            info.append("  [FILE] ").append(file.getName()).append("\n");
                        }
                    }
                }
            } else {
                info.append("配置目录不存在或不是目录\n");
            }
        }
        
        return info.toString();
    }
    
    /**
     * OpenSMILE特征提取结果
     */
    public static class OpenSmileResult {
        private boolean success;
        private String errorMessage;
        private Map<String, Double> features;
        private double depressionScore;
        private String depressionLevel;
        private String configType;
        private int featureCount;
        
        // New fields for enhanced acoustic analytics
        private Map<String, Double> acousticSummary;
        private Map<String, Double> emotionalIndicators;
        private Map<String, Object> audioStats;
        private Map<String, Double> topFeatures;
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public Map<String, Double> getFeatures() {
            return features;
        }
        
        public void setFeatures(Map<String, Double> features) {
            this.features = features;
        }
        
        public double getDepressionScore() {
            return depressionScore;
        }
        
        public void setDepressionScore(double depressionScore) {
            this.depressionScore = depressionScore;
        }
        
        public String getDepressionLevel() {
            return depressionLevel;
        }
        
        public void setDepressionLevel(String depressionLevel) {
            this.depressionLevel = depressionLevel;
        }
        
        public String getConfigType() {
            return configType;
        }
        
        public void setConfigType(String configType) {
            this.configType = configType;
        }
        
        public int getFeatureCount() {
            return featureCount;
        }
        
        public void setFeatureCount(int featureCount) {
            this.featureCount = featureCount;
        }
        
        public Map<String, Double> getAcousticSummary() {
            return acousticSummary;
        }
        
        public void setAcousticSummary(Map<String, Double> acousticSummary) {
            this.acousticSummary = acousticSummary;
        }
        
        public Map<String, Double> getEmotionalIndicators() {
            return emotionalIndicators;
        }
        
        public void setEmotionalIndicators(Map<String, Double> emotionalIndicators) {
            this.emotionalIndicators = emotionalIndicators;
        }
        
        public Map<String, Object> getAudioStats() {
            return audioStats;
        }
        
        public void setAudioStats(Map<String, Object> audioStats) {
            this.audioStats = audioStats;
        }
        
        public Map<String, Double> getTopFeatures() {
            return topFeatures;
        }
        
        public void setTopFeatures(Map<String, Double> topFeatures) {
            this.topFeatures = topFeatures;
        }
    }
}
