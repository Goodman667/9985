# Voice Analysis Features - OpenSMILE Integration

## Overview
The voice analysis module now provides comprehensive acoustic feature extraction using OpenSMILE, a professional audio analysis toolkit. This integration offers detailed insights into voice characteristics that can indicate emotional states and mental health indicators.

## Features

### 🔬 Acoustic Features
**Fundamental Frequency (Pitch) Analysis:**
- **基频均值**: Average pitch of the voice
- **基频标准差**: Pitch variation (monotony vs expressiveness)
- **基频范围**: Difference between low and high pitch

**Loudness Analysis:**
- **响度均值**: Average volume level
- **响度变化**: Volume variation throughout speech
- **响度范围**: Dynamic range of volume

**Voice Quality Metrics:**
- **音高微扰 (Jitter)**: Pitch stability - higher values indicate voice instability
- **振幅微扰 (Shimmer)**: Amplitude stability - indicates voice quality
- **谐噪比 (HNR)**: Harmonic-to-Noise Ratio - voice clarity

**Speech Characteristics:**
- **能量均值**: Overall speech energy
- **能量变化**: Energy variation patterns
- **语速指标**: Estimated speech rate from energy patterns

**Spectral Features:**
- **频谱流量**: Spectral flux - frequency changes over time
- **频谱重心**: Spectral centroid - brightness of sound
- **MFCC均值**: Mel-frequency cepstral coefficients

### 😊 Emotional Indicators
**Derived from acoustic features:**

- **活跃度**: Overall energy and engagement level
- **紧张度**: Voice tension and stress indicators
- **情绪稳定性**: Consistency of emotional expression
- **抑郁倾向**: Risk indicators based on acoustic patterns
- **能量水平**: Overall vocal energy and presence

### 📊 Audio Statistics
**Technical Information:**
- **特征总数**: Number of acoustic features extracted
- **有效特征数**: Count of valid, non-NaN features
- **估计时长**: Approximate audio duration
- **配置类型**: OpenSMILE configuration used
- **处理时间**: Timestamp of analysis

### 🔍 Detailed Analysis
**Top 20 Acoustic Features:**
- Prioritized list of most important features
- Includes both standard and advanced acoustic metrics
- Raw values for technical analysis

## Configuration

### Required Setup
1. **Install OpenSMILE:**
   ```bash
   # Linux/Mac
   wget https://github.com/audeering/opensmile/releases/download/v3.0.2/OpenSMILE-3.0.2-linux-x86_64.tar.gz
   tar -xzf OpenSMILE-3.0.2-linux-x86_64.tar.gz
   
   # Windows
   # Download and extract from GitHub releases
   ```

2. **Configure Application Properties:**
   ```properties
   # Enable OpenSMILE integration
   ai.opensmile.enabled=true
   
   # OpenSMILE executable path
   ai.opensmile.path=/usr/local/bin/SMILExtract
   
   # Configuration directory
   ai.opensmile.config.path=/usr/local/share/opensmile/config
   
   # Configuration type (recommended: eGeMAPSv02)
   ai.opensmile.config.type=eGeMAPSv02
   ```

3. **Verify Configuration:**
   - Access http://localhost:8080/test-opensmile
   - Check configuration status and paths

## Usage Instructions

### Recording Audio
1. **Enable Microphone**: Browser will request microphone permission
2. **Click Record**: Start recording your voice response
3. **Speak Naturally**: 10-30 seconds of speech recommended
4. **Stop Recording**: Click stop when finished
5. **Review Recording**: Playback to verify audio quality
6. **Submit Assessment**: Include voice recording with questionnaire

### Understanding Results

#### OpenSMILE Professional Analysis
When OpenSMILE is enabled, you'll see:
- ✨ **OpenSMILE专业分析** badge
- **88个特征** (eGeMAPSv02 configuration)
- Enhanced acoustic feature displays

#### Fallback Analysis
When OpenSMILE is unavailable:
- ⚠️ **Fallback message** displayed
- Basic acoustic features from simple analysis
- Limited but still informative results

## Feature Interpretation

### Acoustic Features Meaning

| Feature | Low Values | High Values | Clinical Significance |
|----------|-------------|--------------|---------------------|
| **基频均值** | Low-pitched voice | High-pitched voice | Can indicate mood and energy |
| **基频标准差** | Monotone speech | Expressive speech | Low variation may indicate depression |
| **响度均值** | Quiet speech | Loud speech | May indicate confidence or energy |
| **音高微扰** | Stable pitch | Unstable pitch | High values suggest stress/tension |
| **振幅微扰** | Stable volume | Unstable volume | Voice quality indicator |
| **谐噪比** | Noisy voice | Clear voice | Lower values may indicate vocal strain |
| **语速指标** | Slow speech | Fast speech | Can indicate anxiety or agitation |

### Emotional Indicators Interpretation

| Indicator | 0-25% | 25-50% | 50-75% | 75-100% |
|------------|----------|-----------|-----------|-----------|
| **活跃度** | Low energy | Calm | Engaged | High energy |
| **紧张度** | Relaxed | Slight tension | Moderate stress | High stress |
| **情绪稳定性** | Variable | Somewhat stable | Stable | Very stable |
| **抑郁倾向** | Low risk | Mild risk | Moderate risk | High risk |
| **能量水平** | Low energy | Moderate energy | Good energy | High energy |

## Technical Details

### OpenSMILE Configuration Types

1. **eGeMAPSv02** (Recommended)
   - 88 features
   - Comprehensive acoustic analysis
   - Good for emotion recognition

2. **GeMAPSv01b**
   - 62 features
   - Basic acoustic feature set
   - Faster processing

3. **emobase**
   - 988 features
   - Extensive feature set
   - Higher computational cost

4. **ComParE_2016**
   - 6373 features
   - Research-grade features
   - Maximum detail

### Feature Processing Pipeline

1. **Audio Input**: Base64 encoded WAV audio
2. **Preprocessing**: WAV header generation, normalization
3. **OpenSMILE Extraction**: Feature computation using configured set
4. **Post-processing**: Feature validation, NaN removal
5. **Analysis Generation**: Acoustic summary, emotional indicators
6. **Result Packaging**: Structured JSON output
7. **Database Storage**: Persistent storage with @Lob annotation
8. **UI Display**: Multi-section visualization

## Troubleshooting

### Common Issues

1. **OpenSMILE Not Found**
   - Check executable path in application.properties
   - Verify file permissions and existence
   - Use absolute paths if needed

2. **Configuration File Missing**
   - Verify config.path points to correct directory
   - Check subdirectory structure (egemaps/v02/)
   - Use test-opensmile endpoint for debugging

3. **No Audio Features**
   - Verify audio recording quality
   - Check microphone permissions
   - Ensure sufficient audio duration (>5 seconds)

4. **Memory Issues**
   - Monitor temporary file cleanup
   - Check available disk space
   - Consider audio length limits

### Debug Information

Access debugging endpoints:
- **OpenSMILE Config**: http://localhost:8080/test-opensmile
- **H2 Console**: http://localhost:8080/h2-console
- **Application Logs**: Console output for detailed status

## Research Background

### Clinical Relevance

Research has shown strong correlations between acoustic features and mental health:

- **Pitch Variation**: Depression often associated with reduced pitch range
- **Speech Rate**: Can indicate anxiety or agitation levels
- **Voice Quality**: Stress and tension affect voice stability
- **Energy Patterns**: Overall engagement and depressive symptoms

### Feature Selection

The implemented features are based on:
- **eGeMAPSv02**: Standardized set for emotion recognition
- **Research Literature**: Peer-reviewed studies on vocal biomarkers
- **Clinical Validation**: Features with proven diagnostic value

## Privacy and Security

- **Audio Processing**: Local processing, no cloud transmission
- **Data Storage**: Encrypted database storage
- **Temporary Files**: Automatic cleanup after processing
- **No Persistence**: Audio files not permanently stored

---

This enhanced voice analysis provides professional-grade acoustic insights while maintaining user privacy and system reliability.