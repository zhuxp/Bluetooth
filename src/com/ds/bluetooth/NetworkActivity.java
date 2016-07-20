package com.ds.bluetooth;

import com.ds.bluetoothUtil.NetworkDefinition;
import com.ds.bluetoothUtil.NetworkService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class NetworkActivity extends Activity {
    private static final String TAG = "zxp_nwActivity";
	/** Called when the activity is first created. */
    
	private Button startBtn;
	private ButtonClickListener btnClickListener = new ButtonClickListener();
	private TextView tv;

	//广播接收器
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (NetworkDefinition.ACTION_READ_HTTP_DATA.equals(action)) {
				//接收数据
				byte[] str = new byte[1024];
				str = intent.getByteArrayExtra(NetworkDefinition.DATA);
				tv.setText(str.toString());
			} 
		}
	};
	
	@Override
	protected void onStart() {
		//开启后台service
		Intent startService = new Intent(NetworkActivity.this, NetworkService.class);
		startService(startService);
		
		//注册BoradcasrReceiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NetworkDefinition.ACTION_READ_HTTP_DATA);
		registerReceiver(broadcastReceiver, intentFilter);
		
		super.onStart();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network);
        
        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(btnClickListener); 
        tv = (TextView) findViewById(R.id.nw_textView);
        
        WebView webView = (WebView) findViewById(R.id.nw_webView);   
        String url = "http://www.baidu.com";  
        webView.loadUrl(url);  
    }
	
	class ButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			
			case R.id.startBtn:
				String addr = "http://image.baidu.com/detail/newindex?col=%E6%B1%BD%E8%BD%A6&tag=%E6%B3%95%E6%8B%89%E5%88%A9&pn=0&pid=9545910689&aid=&user_id=53581168&setid=-1&sort=0&newsPn=&star=&fr=&from=1";
				Intent httpCoonIntent = new Intent(NetworkDefinition.ACTION_HTTP_CONN);
				httpCoonIntent.putExtra(NetworkDefinition.ADDRESS, addr);
				sendBroadcast(httpCoonIntent);
				Log.d(TAG,"ACTION_HTTP_CONN");
				break;
			}
		}

	}

	@Override
	protected void onStop() {
		//关闭后台Service
		Intent stopService = new Intent(NetworkDefinition.ACTION_NW_CLOSE);
		sendBroadcast(stopService);
		unregisterReceiver(broadcastReceiver);
		Log.d(TAG,"ACTION_NW_CLOSE");
		
		super.onStop();
	}
}