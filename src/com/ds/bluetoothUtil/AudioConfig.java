package com.ds.bluetoothUtil;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class AudioConfig {

    public static final int AUDIO_SAMPLE_RATE = 8000;
    
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    
    public static final int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    
    public static final int AUDIO_CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    
    public static final int AUDIO_CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    
    public static final int AUDIO_PLAY_MODE = AudioTrack.MODE_STREAM;
    
    public static final int AUDIO_SOURCE_MIC = MediaRecorder.AudioSource.MIC;
    
    public static final int AUDIO_SOURCE_CALL_DOWNLINK = MediaRecorder.AudioSource.VOICE_DOWNLINK;
    
}
