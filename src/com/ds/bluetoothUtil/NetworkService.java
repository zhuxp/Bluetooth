package com.ds.bluetoothUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * 蓝牙模块服务器端主控制Service
 * @author Zhu Xiaoping
 *
 */
public class NetworkService extends Service {

	private static final String TAG = "zxp_NWSevice";
	private static String url = null;
	
	//控制信息广播接收器
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (NetworkDefinition.ACTION_HTTP_CONN.equals(action)) {
				//发送数据
				Log.d(TAG, "Start ACTION_HTTP_CONN");
				String addr = intent.getStringExtra(NetworkDefinition.ADDRESS);
				//发送成功读取到对象的消息
				Log.d(TAG, "addr = " + addr);
				url = addr;
				new Thread(new httpThread()).start();

			}else if (NetworkDefinition.ACTION_NW_CLOSE.equals(action)) {
				//停止后台服务
				Log.d(TAG,"ACTION_NW_CLOSE");
				stopSelf();			
			}
		}
	};
	
	//接收其他线程消息的Handler
	private Handler serviceHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			
			case NetworkDefinition.MSG_NW_READ_DATA:
				//读取到数据
				InputStream inStream = (InputStream) msg.obj;
				//发送数据广播（包含数据对象）
				Log.d(TAG,"MSG_NW_READ_DATA");
				byte[] buffer = new byte[1024];
				
				try {
					inStream.read(buffer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Intent dataIntent = new Intent(NetworkDefinition.ACTION_READ_HTTP_DATA);
				dataIntent.putExtra(BluetoothTools.DATA, buffer);
				sendBroadcast(dataIntent);
				break;
				
			case NetworkDefinition.MSG_CONNECT_ERROR:
				Log.d(TAG,"Http MSG_CONNECT_ERROR");
				break;
				
			}
			
			super.handleMessage(msg);
		}
		
	};
	

	@Override
	public void onCreate() {
		//ControlReceiver的IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(NetworkDefinition.ACTION_HTTP_CONN);
		
		//注册BroadcastReceiver
		registerReceiver(controlReceiver, controlFilter);
		
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(controlReceiver);
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public class httpThread implements Runnable{
		URL httpUrl = null;
		HttpURLConnection httpConn = null;
		InputStream inStream = null;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				httpUrl  = new URL(url);
			} catch (MalformedURLException e) {	e.printStackTrace();}
			try {
				httpConn = (HttpURLConnection) httpUrl.openConnection();
				httpConn.setConnectTimeout(1*1000);
				if (httpConn.getResponseCode() != 200)    //从Internet获取网页,发送请求,将网页以流的形式读回来
					throw new RuntimeException("请求url失败");
				inStream = httpConn.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			byte[] buffer = new byte[1024];			
			try {
				inStream.read(buffer);
			} catch (IOException e) {e.printStackTrace();}
			httpConn.disconnect();	
			
			//发送数据广播（包含数据对象）
			Log.d(TAG,"httpThread read network data OK");			
			Message msg = serviceHandler.obtainMessage();
			msg.what = NetworkDefinition.MSG_NW_READ_DATA;
			msg.obj = inStream;
			msg.sendToTarget();
		}
		
	} 
}
