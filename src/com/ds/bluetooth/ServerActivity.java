package com.ds.bluetooth;

import java.util.Date;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.bluetoothUtil.BluetoothServerService;
import com.ds.bluetoothUtil.BluetoothTools;
import com.ds.bluetoothUtil.TransmitBean;

public class ServerActivity extends Activity {

	private static final String TAG = "zxp_srvactivity";
	private TextView serverStateTextView;
	private TextView chatHis;
	private EditText sendMsgEditText;
	private Button sendBtn;

    
	//�㲥������
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			
			if (BluetoothTools.ACTION_SERVER_RCV_DATA_FROM_CLIENT.equals(action)) {
				//��������
				TransmitBean data = (TransmitBean)intent.getExtras().getSerializable(BluetoothTools.DATA);
				if(data.getDataType() == BluetoothTools.DATA_TYPE_TEXT){
					Time time = new Time();
					time.set(new Date().getTime());
					Log.d(TAG, "ACTION_SERVER_RCV_DATA_FROM_CLIENT is Text");
					String msg = time.format2445() + " receive: " + "\r\n" + data.getMsg() + "\r\n";
					chatHis.append(msg);
				}else if(data.getDataType() == BluetoothTools.DATA_TYPE_VOICE_STREAM){
					Log.e(TAG, "Error found, stream should be processed in service");
				}else if (data.getDataType() == BluetoothTools.DATA_TYPE_COMMAND){
					Log.d(TAG, "Error found, command should be processed in service");
				}
			
			} else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
				//���ӳɹ�
				serverStateTextView.setText("���ӳɹ�");
				sendBtn.setEnabled(true);
			}			
		}
	};
	
	@Override
	protected void onStart() {
		//������̨service
		Intent startService = new Intent(ServerActivity.this, BluetoothServerService.class);
		startService(startService);
		
		//ע��BoradcasrReceiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothTools.ACTION_SERVER_RCV_DATA_FROM_CLIENT);
		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
		
		registerReceiver(broadcastReceiver, intentFilter);
		super.onStart();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server);
	    
		serverStateTextView = (TextView)findViewById(R.id.serverStateText);
		serverStateTextView.setText("��������...");
		
		chatHis = (TextView)findViewById(R.id.serverChatHis);
		chatHis.setMovementMethod(new ScrollingMovementMethod());
		sendMsgEditText = (EditText)findViewById(R.id.serverSendEditText);
		
		sendBtn = (Button)findViewById(R.id.serverSendMsgBtn);
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ("".equals(sendMsgEditText.getText().toString().trim())) {
					Toast.makeText(ServerActivity.this, "���벻��Ϊ��", Toast.LENGTH_SHORT).show();
				} else {
					//������Ϣ
					Time time = new Time();
					time.set(new Date().getTime());
					TransmitBean data = new TransmitBean();
					String str = new String(sendMsgEditText.getText().toString());
					String msg = time.format2445() + " sent: " + "\r\n" + str + "\r\n";
					chatHis.append(msg);
					Log.d(TAG, "send msg: " + str);
					data.setMsg(str);
					Intent sendDataIntent = new Intent(BluetoothTools.ACTION_SERVER_SEND_DATA_TO_CLIENT);
					sendDataIntent.putExtra(BluetoothTools.DATA, data);
					sendBroadcast(sendDataIntent);
					sendMsgEditText.getText().clear();
				}
			}
		});
		//sendBtn.setEnabled(false);
		

	}
	
	@Override
	protected void onStop() {
		//�رպ�̨Service
		Intent stoptService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
		sendBroadcast(stoptService);		
		unregisterReceiver(broadcastReceiver);
		super.onStop();
	}

}
