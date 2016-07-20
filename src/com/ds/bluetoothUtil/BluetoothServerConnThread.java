package com.ds.bluetoothUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 服务器连接线程
 * @author Zhu Xiaoping
 *
 */
public class BluetoothServerConnThread extends Thread {
	
	private static final String TAG = "zxp_SrvConnThread";
	private Handler serviceHandler;		//用于同Service通信的Handler
	private BluetoothAdapter adapter;
	private BluetoothSocket socket;		//用于通信的Socket
	private BluetoothServerSocket serverSocket;
	
	/**
	 * 构造函数
	 * @param handler
	 */
	public BluetoothServerConnThread(Handler handler) {
		this.serviceHandler = handler;
		adapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	@Override
	public void run() {
		
		try {
			serverSocket = adapter.listenUsingRfcommWithServiceRecord("Server", BluetoothTools.PRIVATE_UUID);
			socket = serverSocket.accept();
		} catch (Exception e) {
			//发送连接失败消息
			serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			e.printStackTrace();
			return;
		} finally {
			try {
				serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (socket != null) {
			//发送连接成功消息，消息的obj字段为连接的socket
			Log.d(TAG,"socket connected");
			Message msg = serviceHandler.obtainMessage();
			msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
			msg.obj = socket;
			msg.sendToTarget();
		} else {
			//发送连接失败消息
			serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			return;
		}
	}
	
	
}
