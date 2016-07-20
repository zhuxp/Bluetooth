package com.ds.bluetoothUtil;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

/**
 * 蓝牙客户端连接线程
 * @author Zhu Xiaoping
 *
 */
public class BluetoothClientConnThread extends Thread{

	private Handler serviceHandler;		//用于向客户端Service回传消息的handler
	private BluetoothDevice serverDevice;	//服务器设备
	private BluetoothSocket socket;		//通信Socket
	
	/**
	 * 构造函数，记录handler和serverDevice，连接成功返回socket，并且发送 MESSAGE_CONNECT_SUCCESS到clntservice的handler处理
	 * @param handler
	 * @param serverDevice
	 */
	public BluetoothClientConnThread(Handler handler, BluetoothDevice serverDevice) {
		this.serviceHandler = handler;
		this.serverDevice = serverDevice;
	}
	
	@Override
	public void run() {
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		try {
			socket = serverDevice.createRfcommSocketToServiceRecord(BluetoothTools.PRIVATE_UUID);
			socket.connect();
		} catch (Exception ex) {
			try {
				if(socket != null) socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//发送连接失败消息
			serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			return;
		}
		
		//发送连接成功消息，消息的obj参数为连接的socket
		Message msg = serviceHandler.obtainMessage();
		msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
		msg.obj = socket;
		msg.sendToTarget();
	}
}
