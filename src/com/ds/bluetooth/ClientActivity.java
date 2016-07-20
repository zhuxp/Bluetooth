package com.ds.bluetooth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.bluetoothUtil.BluetoothClientService;
import com.ds.bluetoothUtil.BluetoothTools;
import com.ds.bluetoothUtil.TransmitBean;

public class ClientActivity extends Activity {

	private static final String TAG = "zxp_clntactivity";
	private static final String BT_DEV_KEY_NAME = "name";
	private static final String BT_DEV_KEY_ADDR = "addr";
	private TextView chatHis;
	private EditText sendEditText;
	private Button sendBtn;
	private Button startSearchBtn;
	private Button talkBtn;	
	private boolean talking = false;
	
	private SimpleAdapter adapter = null;	
	private ArrayList<HashMap<String, String>> listViewData = null;
	private ListView subListView;	
	private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
	private BluetoothDevice srvDev = null;
	
	//广播接收器
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (BluetoothTools.ACTION_CLIENT_RCV_DATA_FROM_SERVER.equals(action)) {
				//接收数据
				TransmitBean data = (TransmitBean)intent.getExtras().getSerializable(BluetoothTools.DATA);
				
				if(data.getDataType() == BluetoothTools.DATA_TYPE_TEXT){
					Time time = new Time();
					time.set(new Date().getTime());
					Log.d(TAG, "ACTION_CLIENT_RCV_DATA_FROM_SERVER is Text");
					String msg = time.format2445() + " receive: " + "\r\n" + data.getMsg() + "\r\n";
					chatHis.append(msg);
				}else{
					Log.d(TAG, "ACTION_SERVER_RCV_DATA_FROM_CLIENT, this should be processed in service");
				}
			
			} else if (BluetoothTools.ACTION_NOT_FOUND_SERVER.equals(action)) {
				//未发现设备
				Toast.makeText(context, R.string.server_not_found, Toast.LENGTH_SHORT).show();
				
			} else if (BluetoothTools.ACTION_FOUND_DEVICE.equals(action)) {
				//获取到设备对象
				BluetoothDevice device = (BluetoothDevice)intent.getExtras().get(BluetoothTools.DEVICE);
				String devName = device.getName();
				Log.d(TAG, "find device: " + devName);
				if(null == devName) return;
				if(deviceList.size() < 10){
					deviceList.add(device);
					HashMap<String,String> map = new HashMap<String,String>();
					map.put(BT_DEV_KEY_NAME, devName);
					map.put(BT_DEV_KEY_ADDR, device.getAddress());
					listViewData.add(map);
					adapter.notifyDataSetChanged();
				}else{
					Log.d(TAG, "stop finding device");
					BluetoothTools.stopDiscovery();
				}
			} else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
				//连接成功
				sendBtn.setEnabled(true);	
				Toast.makeText(context, R.string.server_conn_suc, Toast.LENGTH_SHORT).show();
				deviceList.clear();
				listViewData.clear();
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(BT_DEV_KEY_NAME, " \"" + srvDev.getName() + "\" is connected!");
				map.put(BT_DEV_KEY_ADDR, srvDev.getAddress());
				listViewData.add(map);
				adapter.notifyDataSetChanged();
			} 
		}
	};
	
	@Override
	protected void onStart() {
		//清空设备列表
		deviceList.clear();
		listViewData.clear();
		
		//开启后台service
		Intent startService = new Intent(ClientActivity.this, BluetoothClientService.class);
		startService(startService);
		
		//注册BoradcasrReceiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothTools.ACTION_NOT_FOUND_SERVER);
		intentFilter.addAction(BluetoothTools.ACTION_FOUND_DEVICE);
		intentFilter.addAction(BluetoothTools.ACTION_CLIENT_RCV_DATA_FROM_SERVER);
		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
		
		registerReceiver(broadcastReceiver, intentFilter);
		
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		
		startSearchBtn = (Button)findViewById(R.id.startSearchBtn);		
		chatHis = (TextView)findViewById(R.id.clientChatHis);
		chatHis.setMovementMethod(new ScrollingMovementMethod());

		sendBtn = (Button)findViewById(R.id.clientSendMsgBtn);
		sendEditText = (EditText)findViewById(R.id.clientSendEditText);
		talkBtn = (Button)findViewById(R.id.clientTalkBtn);
		subListView = (ListView)findViewById(R.id.listView);

		listViewData = new ArrayList<HashMap<String,String>>();
		adapter = new SimpleAdapter(this, listViewData, R.layout.listview_in_client, 
				new String[]{BT_DEV_KEY_NAME , BT_DEV_KEY_ADDR}, new int[]{R.id.dev_name , R.id.dev_addr});
		subListView.setAdapter(adapter);
		
		subListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				//deviceList 和 listViewData 同步保存数据，所以listViewData index对应的信息就是 deviceList index对应的设备
				try{
					srvDev = deviceList.get(arg2);
					if(srvDev.getName().equals(listViewData.get(arg2).get(BT_DEV_KEY_NAME))){
						Intent selectDeviceIntent = new Intent(BluetoothTools.ACTION_SELECTED_DEVICE);
						selectDeviceIntent.putExtra(BluetoothTools.DEVICE, deviceList.get(arg2));
						sendBroadcast(selectDeviceIntent);
						Log.e(TAG,"Selected device NO." + arg2 + "devName="+ srvDev.getName());
						Toast.makeText(arg1.getContext(), "开始连接服务", Toast.LENGTH_SHORT).show();
					}else{
						Log.e(TAG, "found error when selected device");
						Toast.makeText(arg1.getContext(), "选择蓝牙服务器错误", Toast.LENGTH_LONG).show();
					}
				}catch (Exception e){
					Log.e(TAG,"maybe server already connected!");
					e.printStackTrace();
				}
			}
			
		});
		
		sendBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//发送消息
				if ("".equals(sendEditText.getText().toString().trim())) {
					Toast.makeText(ClientActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
				} else {
					//发送消息
					Time time = new Time();
					time.set(new Date().getTime());
					TransmitBean data = new TransmitBean();
					String str = new String(sendEditText.getText().toString());
					String msg = time.format2445() + " sent:" + "\r\n" + str + "\r\n";
					chatHis.append(msg);						
					Log.d(TAG, "send msg: " + str);
					data.setMsg(str);
					Intent sendDataIntent = new Intent(BluetoothTools.ACTION_CLIENT_SEND_DATA_TO_SERVER);
					sendDataIntent.putExtra(BluetoothTools.DATA, data);
					sendBroadcast(sendDataIntent);
					sendEditText.getText().clear();
				}
			}
		});
		//sendBtn.setEnabled(false);
		
		startSearchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//开始搜索
				BluetoothTools.stopDiscovery();
				deviceList.clear();
				listViewData.clear();
				Intent startSearchIntent = new Intent(BluetoothTools.ACTION_START_DISCOVERY);
				sendBroadcast(startSearchIntent);
			}
		});
		

		talkBtn.setText("Start Talk");	
		talkBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (talking == false){
					talkBtn.setText("Stop Talk");
					Intent startTalking = new Intent(BluetoothTools.ACTION_CLN_START_TALKING);
					sendBroadcast(startTalking);
					talking = true;
					
					TransmitBean data = new TransmitBean(BluetoothTools.CMD_START_TALKING); //tell server to start talking
					Intent sendDataIntent = new Intent(BluetoothTools.ACTION_CLIENT_SEND_DATA_TO_SERVER);
					sendDataIntent.putExtra(BluetoothTools.DATA, data);
					sendBroadcast(sendDataIntent);
				}else{
					talkBtn.setText("Start Talk");
					Intent stopTalking = new Intent(BluetoothTools.ACTION_CLN_STOP_TALKING);
					sendBroadcast(stopTalking);
					talking = false;
					
					TransmitBean data = new TransmitBean(BluetoothTools.CMD_STOP_TALKING); //tell server to start talking
					Intent sendDataIntent = new Intent(BluetoothTools.ACTION_CLIENT_SEND_DATA_TO_SERVER);
					sendDataIntent.putExtra(BluetoothTools.DATA, data);
					sendBroadcast(sendDataIntent);
				}
			}
			});
	}
 	
	@Override
	protected void onStop() {
		//关闭后台Service
		Intent stopService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
		sendBroadcast(stopService);
		unregisterReceiver(broadcastReceiver);
		
		Intent stopTalking = new Intent(BluetoothTools.ACTION_CLN_STOP_TALKING);
		sendBroadcast(stopTalking);
		TransmitBean data = new TransmitBean(BluetoothTools.CMD_STOP_TALKING); //tell server to start talking
		Intent sendDataIntent = new Intent(BluetoothTools.ACTION_CLIENT_SEND_DATA_TO_SERVER);
		sendDataIntent.putExtra(BluetoothTools.DATA, data);
		sendBroadcast(sendDataIntent);
		
		super.onStop();
	}

}
