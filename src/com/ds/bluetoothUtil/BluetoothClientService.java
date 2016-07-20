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
 * 蓝牙模块客户端主控制Service
 * @author Zhu Xiaoping
 *
 */
public class BluetoothClientService extends Service {
	
	protected static final String TAG = "zxp_clnSevice";

	//搜索到的远程设备集合
	private List<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
	
	//蓝牙适配器
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	//蓝牙通讯线程
	private BluetoothCommunThread communThread;
	
	//当前连接的Server
	private BluetoothDevice curServerDevice = null;
	
	private TalkingRecordThread mClnTalkingRecordThread = null;
	private TalkingPlayThread mClnTalkingPlayThread = null;
	private LinkedList <byte[]> mTalkingPlayBuff = new LinkedList <byte[]>();
	
	//控制信息广播的接收器
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothTools.ACTION_CLIENT_SEND_DATA_TO_SERVER.equals(action)) {
				try{
					//获取数据
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
				//停止后台服务
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
	
	//蓝牙搜索广播的接收器
	private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//获取广播的Action
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				//发现远程蓝牙设备
				//获取设备
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				discoveredDevices.add(bluetoothDevice);

				//发送发现设备广播
				Intent deviceListIntent = new Intent(BluetoothTools.ACTION_FOUND_DEVICE);
				deviceListIntent.putExtra(BluetoothTools.DEVICE, bluetoothDevice);
				sendBroadcast(deviceListIntent);
				
			}else if (BluetoothTools.ACTION_SELECTED_DEVICE.equals(action)) {
				//选择了连接的服务器设备
				curServerDevice = (BluetoothDevice)intent.getExtras().get(BluetoothTools.DEVICE);
				//开启设备连接线程
				new BluetoothClientConnThread(handler, curServerDevice).start();
				Log.d(TAG, "ACTION_SELECTED_DEVICE");
				
			} else if (BluetoothTools.ACTION_START_DISCOVERY.equals(action)) {
				//开始搜索
				Log.d(TAG, "ACTION_START_DISCOVERY");
				discoveredDevices.clear();	//清空存放设备的集合
				if(! bluetoothAdapter.isEnabled())
					bluetoothAdapter.enable();	//打开蓝牙
				bluetoothAdapter.startDiscovery();	//开始搜索
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				//搜索结束
				if (discoveredDevices.isEmpty()) {
					//若未找到设备，则发动未发现设备广播
					Intent foundIntent = new Intent(BluetoothTools.ACTION_NOT_FOUND_SERVER);
					sendBroadcast(foundIntent);
				}
			}
		}
	};
	
	//接收其他线程消息的Handler
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			//处理消息
			switch (msg.what) {
			case BluetoothTools.MESSAGE_READ_OBJECT:
				//读取到对象
				//发送数据广播（包含数据对象）
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
				//连接错误
				//发送连接错误广播
				Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
				sendBroadcast(errorIntent);
				break;
				
			case BluetoothTools.MESSAGE_CONNECT_SUCCESS:
				//连接成功
				//结束之前异常停止的通信线程
				if (communThread != null) {
					communThread.isRun = false;
				}
				//开启通讯线程
				communThread = new BluetoothCommunThread(handler, (BluetoothSocket)msg.obj);
				communThread.start();
				
				//发送连接成功广播
				Intent succIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
				sendBroadcast(succIntent);
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	/**
	 * 获取通讯线程
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
	 * Service创建时的回调函数
	 */
	@Override
	public void onCreate() {
		//discoveryReceiver的IntentFilter
		IntentFilter discoveryFilter = new IntentFilter();
		discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);
		discoveryFilter.addAction(BluetoothTools.ACTION_START_DISCOVERY);
		discoveryFilter.addAction(BluetoothTools.ACTION_SELECTED_DEVICE);
		
		//controlReceiver的IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
		controlFilter.addAction(BluetoothTools.ACTION_CLIENT_SEND_DATA_TO_SERVER);
		controlFilter.addAction(BluetoothTools.ACTION_CLN_START_TALKING);
		controlFilter.addAction(BluetoothTools.ACTION_CLN_STOP_TALKING);
		
		//注册BroadcastReceiver
		registerReceiver(discoveryReceiver, discoveryFilter);
		registerReceiver(controlReceiver, controlFilter);
		super.onCreate();
	}
	
	/**
	 * Service销毁时的回调函数
	 */
	@Override
	public void onDestroy() {
		if (communThread != null) {
			communThread.isRun = false;
		}
		//解除绑定
		unregisterReceiver(discoveryReceiver);
		unregisterReceiver(controlReceiver);
		super.onDestroy();
	}

}
