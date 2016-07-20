package com.ds.bluetoothUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
 * ����ģ��ͻ���������Service
 * @author Zhu Xiaoping
 *
 */
public class BluetoothClientService extends Service {
	
	protected static final String TAG = "zxp_clnSevice";

	//��������Զ���豸����
	private List<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
	
	//����������
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	//����ͨѶ�߳�
	private BluetoothCommunThread communThread;
	
	//��ǰ���ӵ�Server
	private BluetoothDevice curServerDevice = null;
	
	private TalkingRecordThread mClnTalkingRecordThread = null;
	private TalkingPlayThread mClnTalkingPlayThread = null;
	private LinkedList <byte[]> mTalkingPlayBuff = new LinkedList <byte[]>();
	
	//������Ϣ�㲥�Ľ�����
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothTools.ACTION_CLIENT_SEND_DATA_TO_SERVER.equals(action)) {
				try{
					//��ȡ����
					Log.d(TAG, "ACTION_CLIENT_SEND_DATA_TO_SERVER");
					Object data = intent.getSerializableExtra(BluetoothTools.DATA);
					if (communThread != null) {
						communThread.writeObject(data);
					}else{
						Log.d(TAG, "comm thread null, Auto reconnect to server");
						new BluetoothClientConnThread(handler, curServerDevice).start();					
					}				
				}catch (Exception e){
					Log.e(TAG, "ACTION_CLIENT_SEND_DATA_TO_SERVER error!");
				}
			}else if (BluetoothTools.ACTION_STOP_SERVICE.equals(action)) {
				//ֹͣ��̨����
				if (communThread != null) {
					communThread.isRun = false;
				}
				stopSelf();				
			} else if (BluetoothTools.ACTION_CLN_START_TALKING.equals(action)) {
				Log.d(TAG, "ACTION_CLN_START_TALKING");
				if(communThread == null){
					Log.d(TAG, "communThread null, failed");
					return;
				}
				if(mClnTalkingRecordThread == null){
					mClnTalkingRecordThread = new TalkingRecordThread(communThread);
					mClnTalkingRecordThread.start();
				}
				if(mClnTalkingPlayThread == null){
					mClnTalkingPlayThread = new TalkingPlayThread(mTalkingPlayBuff);
					mClnTalkingPlayThread.start();
				}
			} 
			else if (BluetoothTools.ACTION_CLN_STOP_TALKING.equals(action)) {
				Log.d(TAG, "ACTION_CLN_STOP_TALKING");
				if(mClnTalkingRecordThread != null){
					mClnTalkingRecordThread.free();
					mClnTalkingRecordThread = null;
				}
				if(mClnTalkingPlayThread != null){
					mClnTalkingPlayThread.free();
					mClnTalkingPlayThread = null;
				}
			} 
		}
	};
	
	//���������㲥�Ľ�����
	private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//��ȡ�㲥��Action
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				//����Զ�������豸
				//��ȡ�豸
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				discoveredDevices.add(bluetoothDevice);

				//���ͷ����豸�㲥
				Intent deviceListIntent = new Intent(BluetoothTools.ACTION_FOUND_DEVICE);
				deviceListIntent.putExtra(BluetoothTools.DEVICE, bluetoothDevice);
				sendBroadcast(deviceListIntent);
				
			}else if (BluetoothTools.ACTION_SELECTED_DEVICE.equals(action)) {
				//ѡ�������ӵķ������豸
				curServerDevice = (BluetoothDevice)intent.getExtras().get(BluetoothTools.DEVICE);
				//�����豸�����߳�
				new BluetoothClientConnThread(handler, curServerDevice).start();
				Log.d(TAG, "ACTION_SELECTED_DEVICE");
				
			} else if (BluetoothTools.ACTION_START_DISCOVERY.equals(action)) {
				//��ʼ����
				Log.d(TAG, "ACTION_START_DISCOVERY");
				discoveredDevices.clear();	//��մ���豸�ļ���
				if(! bluetoothAdapter.isEnabled())
					bluetoothAdapter.enable();	//������
				bluetoothAdapter.startDiscovery();	//��ʼ����
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				//��������
				if (discoveredDevices.isEmpty()) {
					//��δ�ҵ��豸���򷢶�δ�����豸�㲥
					Intent foundIntent = new Intent(BluetoothTools.ACTION_NOT_FOUND_SERVER);
					sendBroadcast(foundIntent);
				}
			}
		}
	};
	
	//���������߳���Ϣ��Handler
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			//������Ϣ
			switch (msg.what) {
			case BluetoothTools.MESSAGE_READ_OBJECT:
				//��ȡ������
				//�������ݹ㲥���������ݶ���
				Log.d(TAG,"ACTION_CLIENT_RCV_DATA_FROM_SERVER");
				TransmitBean data = (TransmitBean)msg.obj;
				if(data.getDataType() == BluetoothTools.DATA_TYPE_VOICE_STREAM){
					mTalkingPlayBuff.add(data.getStream());
					Log.d(TAG, "received voice stream, mTalkingPlayBuff.size = "+mTalkingPlayBuff.size());
				}else{
					Log.d(TAG, "Text and cmd will processed in activity");
					Intent dataIntent = new Intent(BluetoothTools.ACTION_CLIENT_RCV_DATA_FROM_SERVER);
					dataIntent.putExtra(BluetoothTools.DATA, (Serializable)msg.obj);
					sendBroadcast(dataIntent);
				}
				break;
				
			case BluetoothTools.MESSAGE_CONNECT_ERROR:
				//���Ӵ���
				//�������Ӵ���㲥
				Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
				sendBroadcast(errorIntent);
				break;
				
			case BluetoothTools.MESSAGE_CONNECT_SUCCESS:
				//���ӳɹ�
				//����֮ǰ�쳣ֹͣ��ͨ���߳�
				if (communThread != null) {
					communThread.isRun = false;
				}
				//����ͨѶ�߳�
				communThread = new BluetoothCommunThread(handler, (BluetoothSocket)msg.obj);
				communThread.start();
				
				//�������ӳɹ��㲥
				Intent succIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
				sendBroadcast(succIntent);
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
	public void onStart(Intent intent, int startId) {
		
		super.onStart(intent, startId);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * Service����ʱ�Ļص�����
	 */
	@Override
	public void onCreate() {
		//discoveryReceiver��IntentFilter
		IntentFilter discoveryFilter = new IntentFilter();
		discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);
		discoveryFilter.addAction(BluetoothTools.ACTION_START_DISCOVERY);
		discoveryFilter.addAction(BluetoothTools.ACTION_SELECTED_DEVICE);
		
		//controlReceiver��IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
		controlFilter.addAction(BluetoothTools.ACTION_CLIENT_SEND_DATA_TO_SERVER);
		controlFilter.addAction(BluetoothTools.ACTION_CLN_START_TALKING);
		controlFilter.addAction(BluetoothTools.ACTION_CLN_STOP_TALKING);
		
		//ע��BroadcastReceiver
		registerReceiver(discoveryReceiver, discoveryFilter);
		registerReceiver(controlReceiver, controlFilter);
		super.onCreate();
	}
	
	/**
	 * Service����ʱ�Ļص�����
	 */
	@Override
	public void onDestroy() {
		if (communThread != null) {
			communThread.isRun = false;
		}
		//�����
		unregisterReceiver(discoveryReceiver);
		unregisterReceiver(controlReceiver);
		super.onDestroy();
	}

}
