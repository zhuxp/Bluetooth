package com.ds.bluetooth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */

	private static final String TAG = "zxp_main";
	private Button startServerBtn, startClientBtn, startOtherBtn;
	private Button startNetworkBtn, startSQLBtn;
	private ButtonClickListener btnClickListener = new ButtonClickListener();
	private ImageView imView;
	private ImageButton imBtn;
	
	private static Handler myHandler;
	private Runnable myRunnable;
	private Bitmap mBitmap = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        /* 允许在Activity中进行网络连接
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }  */     
        
        startClientBtn = (Button)findViewById(R.id.startClientBtn);
        startServerBtn = (Button)findViewById(R.id.startServerBtn);
        startNetworkBtn = (Button)findViewById(R.id.startNetworkBtn);
        startSQLBtn = (Button)findViewById(R.id.startSQLBtn);
        startOtherBtn = (Button)findViewById(R.id.startOtherBtn);
        
        imView = (ImageView)findViewById(R.id.imageView);
        imBtn = (ImageButton)findViewById(R.id.imageButton);
        
        startClientBtn.setOnClickListener(btnClickListener);       
        startServerBtn.setOnClickListener(btnClickListener); 
        startNetworkBtn.setOnClickListener(btnClickListener); 
        startSQLBtn.setOnClickListener(btnClickListener);
        startOtherBtn.setOnClickListener(btnClickListener);
        imBtn.setOnClickListener(btnClickListener);

        startClientBtn.setOnTouchListener(btnClickListener);       
        startServerBtn.setOnTouchListener(btnClickListener); 
        startNetworkBtn.setOnTouchListener(btnClickListener); 
        startSQLBtn.setOnTouchListener(btnClickListener);
        startOtherBtn.setOnTouchListener(btnClickListener);
        imBtn.setOnTouchListener(btnClickListener);
        
        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String val = data.getString("value");
                Log.i(TAG,"handleMessage");
                imView.setImageAlpha(100);
        		imView.setImageBitmap(mBitmap);
            }
        };
        
        myRunnable = new Runnable(){
            @Override
            public void run() {
                //
                // TODO: http request.
            	mBitmap = httpGetBitmap("http://bbs.unpcn.com/attachment.aspx?attachmentid=3780879");
                //
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("Key","Value");
                msg.setData(data);
                myHandler.sendMessage(msg);
            }
        };
    }

	Bitmap httpGetBitmap(String str){
		URL picUrl = null;
		Bitmap map = null;
		try {
			picUrl = new URL(str);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		try {
			map = BitmapFactory.decodeStream(picUrl.openStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return map; 
	}
	
	class ButtonClickListener implements OnClickListener, OnTouchListener {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onTouch");
			switch (view.getId()) {
            case R.id.startOtherBtn:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startOtherBtn.setBackgroundColor(getResources().getColor(R.color.white));
                    Log.d(TAG, "Down");
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                	startOtherBtn.setBackgroundColor(getResources().getColor(R.color.blue));
                }
                break;
			}
			return false;
		}
		
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			
			case R.id.startServerBtn:
				//打开服务器
				Intent serverIntent = new Intent(MainActivity.this, ServerActivity.class);
				serverIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(serverIntent);
				break;
				
			case R.id.startClientBtn:
				//打开客户端
				Intent clientIntent = new Intent(MainActivity.this, ClientActivity.class);
				clientIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(clientIntent);
				break;
				
			case R.id.startNetworkBtn:
				//打开客户端
				Intent networkIntent = new Intent(MainActivity.this, NetworkActivity.class);
				networkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(networkIntent);
				break;
				
			case R.id.startSQLBtn:
				//打开客户端
				Intent sqlIntent = new Intent(MainActivity.this, SQLActivity.class);
				sqlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(sqlIntent);
				break;
				
			case R.id.startOtherBtn:
				//打开客户端
				Log.d(TAG, "OtherBtn");
				new Thread(myRunnable).start();
				break;
				
			case R.id.imageButton:
				//打开客户端
				Log.d(TAG, "ImgBtn");
				imView.setImageAlpha(80);
				break;
			}
		}
	}
    
}