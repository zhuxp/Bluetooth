package com.ds.bluetoothUtil;

import android.media.AudioRecord;
import android.util.Log;

public class TalkingRecordThread extends Thread {
	private AudioRecord record;
	private int inBufferSize;
	private int cnt = 0;
	private byte[] inBytes;
	private boolean keepRunning = true;
	private BluetoothCommunThread commThread = null;
    
	private static final String TAG = "zxp_talkRecordThread";
    
	public TalkingRecordThread(BluetoothCommunThread communThread) {
		commThread = communThread;
		inBufferSize = AudioRecord.getMinBufferSize(
				AudioConfig.AUDIO_SAMPLE_RATE, AudioConfig.AUDIO_CHANNEL_IN, AudioConfig.AUDIO_FORMAT);
		
		record = new AudioRecord(AudioConfig.AUDIO_SOURCE_MIC, AudioConfig.AUDIO_SAMPLE_RATE,
				AudioConfig.AUDIO_CHANNEL_IN, AudioConfig.AUDIO_FORMAT, inBufferSize);		

		Log.d(TAG,"getMinBufferSize = " + inBufferSize);		
	}

	public void free() {
		keepRunning = false;
	}

	@Override
	public void run() {
		try {
			record.startRecording();
			while (keepRunning) {
				inBytes = new byte[inBufferSize];
				cnt = record.read(inBytes, 0, inBufferSize);
				Log.d(TAG, "readSize="+ cnt +", buffSize=" + inBufferSize);
				//yield(); //it may let the same priority thread to be scheduled
				TransmitBean data = new TransmitBean(inBytes);
				if(null != commThread){
					commThread.writeObject(data);
				}
			}
			Log.d(TAG,"free recording called");
			record.stop();
			record.release();
			record = null;
			inBytes = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
