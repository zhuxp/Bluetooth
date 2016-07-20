package com.ds.bluetoothUtil;

import java.io.Serializable;
import java.util.LinkedList;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * ����ģ���������������Service
 * @author Zhu Xiaoping
 *
 */
public class BluetoothServerService extends Service {

	private static final String TAG = "zxp_BTSvrSevice";
	//����������
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	//����ͨѶ�߳�
	private BluetoothCommunThread communThread;
	private TalkingRecordThread mSrvTalkingRecordThread = null;
	private TalkingPlayThread mSrvTalkingPlayThread = null;
	private LinkedList <byte[]> mTalkingPlayBuff = new LinkedList<byte[]>();
	
	//������Ϣ�㲥������
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothTools.ACTION_SERVER_SEND_DATA_TO_CLIENT.equals(action)) {
				//��������
				Log.d(TAG, "ACTION_SERVER_SEND_DATA_TO_CLIENT");
				Object data = intent.getSerializableExtra(BluetoothTools.DATA);
				if (communThread != null) {
					communThread.writeObject(data);
				}else{
					Log.w(TAG, "comm thread null");
				}				
			}else if (BluetoothTools.ACTION_STOP_SERVICE.equals(action)) {
				//ֹͣ��̨����
				if (communThread != null) {
					communThread.isRun = false;
				}
				stopSelf();				
			}
		}
	};
	
	//���������߳���Ϣ��Handler
	private Handler serviceHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			
			case BluetoothTools.MESSAGE_READ_OBJECT:
				//��ȡ������
				//�������ݹ㲥���������ݶ���
				Log.d(TAG,"ACTION_SERVER_RCV_DATA_FROM_CLIENT");
				TransmitBean data = (TransmitBean)msg.obj;
				if(data.getDataType() == BluetoothTools.DATA_TYPE_VOICE_STREAM){
					mTalkingPlayBuff.add(data.getStream());
					Log.d(TAG, "received voice stream, mTalkingPlayBuff.size = "+mTalkingPlayBuff.size());
				}else if(data.getDataType() == BluetoothTools.DATA_TYPE_COMMAND){
					if(data.getCmd() == BluetoothTools.CMD_START_TALKING){
						Log.d(TAG, "CMD_START_TALKING");
						if(communThread == null){
							Log.d(TAG, "communThread null, failed");
							return;
						}
						if(mSrvTalkingRecordThread == null){
							mSrvTalkingRecordThread = new TalkingRecordThread(communThread);
							mSrvTalkingRecordThread.start();
						}
						if(mSrvTalkingPlayThread == null){
							mSrvTalkingPlayThread = new TalkingPlayThread(mTalkingPlayBuff);
							mSrvTalkingPlayThread.start();
						}
					}else if (data.getCmd() == BluetoothTools.CMD_STOP_TALKING) {
						Log.d(TAG, "CMD_STOP_TALKING");
						if(mSrvTalkingRecordThread != null){
							mSrvTalkingRecordThread.free();
							mSrvTalkingRecordThread = null;
						}
						if(mSrvTalkingPlayThread != null){
							mSrvTalkingPlayThread.free();
							mSrvTalkingPlayThread = null;
						}
					}
				}else{
					Log.d(TAG, "Text will processed in activity");
					Intent dataIntent = new Intent(BluetoothTools.ACTION_SERVER_RCV_DATA_FROM_CLIENT);
					dataIntent.putExtra(BluetoothTools.DATA, (Serializable)msg.obj);
					sendBroadcast(dataIntent);
				}
				break;
			
			case BluetoothTools.MESSAGE_CONNECT_SUCCESS:
				//���ӳɹ�
				//����ͨѶ�߳�
				communThread = new BluetoothCommunThread(serviceHandler, (BluetoothSocket)msg.obj);
				communThread.start();				
				//�������ӳɹ���Ϣ
				Intent connSuccIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
				sendBroadcast(connSuccIntent);
				break;
				
			case BluetoothTools.MESSAGE_CONNECT_ERROR:
				//���Ӵ���
				//�������Ӵ���㲥
				Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
				sendBroadcast(errorIntent);
				break;
			}
			
			super.handleMessage(msg);
		}
		
	};
	
	/**
	 * ��ȡͨѶ�߳�
	 * @return
	 */
	public BluetoothCommunThread getBluetoothCommunThread() {
		return communThread;
	}
	
	@Override
	public void onCreate() {
		//ControlReceiver��IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(BluetoothTools.ACTION_START_SERVER);
		controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
		controlFilter.addAction(BluetoothTools.ACTION_SERVER_SEND_DATA_TO_CLIENT);
		
		//ע��BroadcastReceiver
		registerReceiver(controlReceiver, controlFilter);
		
		//����������
		if(!bluetoothAdapter.isEnabled())
			bluetoothAdapter.enable();	//������
		//�����������ֹ��ܣ�
		Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		discoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(discoveryIntent);
		
		//������̨�����߳�
		new BluetoothServerConnThread(serviceHandler).start();
		
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		if (communThread != null) {
			communThread.isRun = false;
		}
		unregisterReceiver(controlReceiver);
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
