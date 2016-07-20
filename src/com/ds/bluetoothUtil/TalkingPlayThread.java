package com.ds.bluetoothUtil;

import java.util.LinkedList;

import android.media.AudioTrack;
import android.util.Log;

/**
 * play audio data of the thread
 */
public class TalkingPlayThread extends Thread {
	private static final String TAG = "zxp_TalkPlayThread";
	private AudioTrack player;
	private LinkedList<byte[]> outData;
	private int outBufferSize;
	private boolean keepRunning = true;

	public TalkingPlayThread(LinkedList<byte[]> data) {
		outData = data;
		outBufferSize = AudioTrack.getMinBufferSize(
				AudioConfig.AUDIO_SAMPLE_RATE, AudioConfig.AUDIO_CHANNEL_OUT, AudioConfig.AUDIO_FORMAT);
		
		player = new AudioTrack(AudioConfig.AUDIO_STREAM_TYPE, AudioConfig.AUDIO_SAMPLE_RATE,
				AudioConfig.AUDIO_CHANNEL_OUT, AudioConfig.AUDIO_FORMAT, outBufferSize,
				AudioConfig.AUDIO_PLAY_MODE);
		
		Log.d(TAG, "outBufferSize = " + outBufferSize);
	}

	public void free() {
		keepRunning = false;
		outData.clear();
	}

	@Override
	public void run() {
		try {
			player.play();
			
			while (keepRunning) {
				if (!outData.isEmpty()) {
					byte[] outBytes = outData.removeFirst();
					if (outBytes != null) {
						player.write(outBytes, 0, outBytes.length);
						Log.d(TAG, "write bytes = "+ outBytes.length + ". outData.size = " + outData.size());
					}
				}
				//yield();
			}
			player.stop();
			player.release();
			player = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

