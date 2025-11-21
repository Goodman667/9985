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
            
            result.setSuccess(true);
            result.setFeatures(features);
            result.setDepressionScore(depressionScore);
            result.setDepressionLevel(depressionLevel);
            result.setConfigType(configType);
            result.setFeatureCount(features.size());
            
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
            
            // 读取输出（避免进程阻塞）
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                // 可选：记录日志
            }
            
            int exitCode = process.waitFor();
            return exitCode == 0;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取配置文件路径
     */
    private String getConfigFilePath() {
        if (configPath != null && !configPath.trim().isEmpty()) {
            return configPath;
        }
        
        // 使用默认配置文件（通常在openSMILE安装目录下）
        String opensmileDir = new File(opensmilePath).getParent();
        String configFileName;
        
        if ("eGeMAPSv02".equals(configType)) {
            configFileName = "eGeMAPSv02.conf";
        } else if ("GeMAPSv01b".equals(configType)) {
            configFileName = "GeMAPSv01b.conf";
        } else if ("emobase".equals(configType)) {
            configFileName = "emobase.conf";
        } else if ("ComParE_2016".equals(configType)) {
            configFileName = "ComParE_2016.conf";
        } else {
            configFileName = "eGeMAPSv02.conf"; // 默认
        }
        
        return opensmileDir + "/../config/" + configFileName;
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
            return false;
        }
        
        File opensmileFile = new File(opensmilePath);
        return opensmileFile.exists() && opensmileFile.canExecute();
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
    }
}
