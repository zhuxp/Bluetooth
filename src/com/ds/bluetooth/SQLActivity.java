package com.ds.bluetooth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SQLActivity extends Activity {
    private static final String TAG = "zxp_SQL";
	/** Called when the activity is first created. */
    
	private Button addBtn;
	private Button modiBtn;
	private Button cancelBtn;
	private Button queryBtn;
	private Button genDBTestBtn;
	private myButtonClickListener btnClickListener = new myButtonClickListener();
	private TextView querySum;
	private EditText mmid;
	private EditText article;
	private EditText price;
	private EditText quantity;
	private EditText date;
	private EditText mark;
	private EditText queryinfo;
	private ListView listview;
	private CheckBox cbOrdered;
	private CheckBox cbShipped;
   
	/* 数据库对象 */  
	private SQLiteDatabase      mSQLiteDatabase = null;
	/* 数据库名 */  
	private final static String DATABASE_NAME   = "mydb.db";  
	/* 表名 */  
	private final static String TABLE_NAME      = "WeiXin";
	private Cursor cursor; 
	private final String [] from = {MMID, ARTICLE, PRICE, QUANTITY, SUBSUM, DATE, ORDERED, SHIP, MARK};
	private final int []to = {R.id.mmid, R.id.article, R.id.price, R.id.quantity, R.id.subSum, R.id.date, 
			R.id.ordered, R.id.shipped, R.id.mark};
	/* 表中的字段 */ 
	private final static String NO  	= "_id";  
	private final static String MMID  	= "MMID";  
	private final static String ARTICLE = "Article";  
	private final static String PRICE   = "Price";  
	private final static String QUANTITY = "Quantity";  
	private final static String SUBSUM 	= "SubSum"; 
	private final static String DATE 	= "Date"; 
	private final static String ORDERED	= "Ordered"; 
	private final static String SHIP 	= "Ship"; 
	private final static String MARK 	= "Mark";  
	
	private int modifying_id = -1;
	private SimpleCursorAdapter adapter;
	  
	/* 创建表的sql语句 */  
	private final static String CREATE_TABLE 
	= "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("+NO+" INTEGER PRIMARY KEY AUTOINCREMENT,"+MMID+" TEXT,"
	+ARTICLE+" TEXT,"+PRICE+" INTEGER,"+QUANTITY+" INTEGER,"+SUBSUM+" INTEGER,"+DATE+" TEXT,"
	+ORDERED+" TEXT,"+SHIP+" TEXT,"+MARK+" TEXT)"; 

	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sqllite);
        
        addBtn = (Button)findViewById(R.id.addBtn);
        modiBtn = (Button)findViewById(R.id.modiBtn);
        queryBtn = (Button)findViewById(R.id.queryBtn);
        genDBTestBtn = (Button)findViewById(R.id.genDBTest);
        cancelBtn = (Button)findViewById(R.id.cancelBtn);
        addBtn.setOnClickListener(btnClickListener); 
        modiBtn.setOnClickListener(btnClickListener); 
        queryBtn.setOnClickListener(btnClickListener); 
        genDBTestBtn.setOnClickListener(btnClickListener); 
        cancelBtn.setOnClickListener(btnClickListener); 
        
        listview = (ListView)findViewById(R.id.queryResult);
        mmid = (EditText)findViewById(R.id.mmid);
        article = (EditText)findViewById(R.id.article);
        price = (EditText)findViewById(R.id.price);
        quantity = (EditText)findViewById(R.id.quantity);
        date = (EditText)findViewById(R.id.date);
        mark = (EditText)findViewById(R.id.mark);
        queryinfo = (EditText)findViewById(R.id.queryInput);
        
        querySum = (TextView)findViewById(R.id.querySum);
        cbOrdered = (CheckBox)findViewById(R.id.checkBoxOrdered);
        cbShipped = (CheckBox)findViewById(R.id.checkBoxShipped);
        
        
        mSQLiteDatabase = this.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null); 
        try  
        {  
            /* 在数据库mSQLiteDatabase中创建一个表 */  
            mSQLiteDatabase.execSQL(CREATE_TABLE);  
        }
        catch (SQLException sqlex)  
        {  
        	sqlex.printStackTrace();
        }  
		
        onListViewItemSingleClickHandler();
        onListViewItemLongPressHandler();
   	
		adapter = new SimpleCursorAdapter(this, R.layout.sqllite_query_listview, cursor, from, to);
		listview.setAdapter(adapter);
		
		Log.d(TAG, "OnCreate Finished!");
    }
	
	class myButtonClickListener implements OnClickListener {
		
		@Override
		public void onClick(View view) {			
			switch (view.getId()) {
	
			case R.id.addBtn:
				addItem();
				queryInfo(queryinfo.getText().toString());
				break;
				
			case R.id.modiBtn:
				updateItem(modifying_id);
				queryInfo(queryinfo.getText().toString());
				modiBtn.setVisibility(View.GONE);
				cancelBtn.setVisibility(View.GONE);
				addBtn.setVisibility(View.VISIBLE);
				break;

			case R.id.cancelBtn:
				modiBtn.setVisibility(View.GONE);
				cancelBtn.setVisibility(View.GONE);
				addBtn.setVisibility(View.VISIBLE);
				break;
				
			case R.id.genDBTest:
				//mSQLiteDatabase.delete(TABLE_NAME, null, null);
				mSQLiteDatabase.execSQL("DROP table if exists " + TABLE_NAME);
				mSQLiteDatabase.execSQL(CREATE_TABLE);  
				genDatabase();
				break;
				
			case R.id.queryBtn:
				queryInfo(queryinfo.getText().toString());
				break;
			}
		}
	}

	@SuppressLint("SimpleDateFormat")
	protected void addItem() {			
		ContentValues cv = new ContentValues(); 
	    String str;
	    int t_price = 0, t_quantity, subSum;
	    
	    str = mmid.getText().toString();
	    if (str.equals("")){
	    	Toast.makeText(getApplicationContext(), "请输入"+getString(R.string.sql_mmid), Toast.LENGTH_SHORT).show();
	    	return;
	    }
	    cv.put(MMID, str); 
	    
	    str = article.getText().toString();
	    if (str.equals("")){
	    	Toast.makeText(getApplicationContext(), "请输入"+getString(R.string.sql_article), Toast.LENGTH_SHORT).show();
	    	return;
	    }
	    cv.put(ARTICLE, str); 
	    
	    str = price.getText().toString();
	    try{
	    	t_price = Integer.parseInt(str);
	    	}catch (NumberFormatException e){
	    		Toast.makeText(getApplicationContext(), "请输入正确"+getString(R.string.sql_price), Toast.LENGTH_SHORT).show();
		    	return;
	    }
	    cv.put(PRICE, t_price); 
	    
	    str = quantity.getText().toString();
	    try{
	    	t_quantity = Integer.parseInt(str);
	    	}catch (NumberFormatException e){
	    		Toast.makeText(getApplicationContext(), "请输入正确"+getString(R.string.sql_quantity), Toast.LENGTH_SHORT).show();
		    	return;
	    }
	    cv.put(QUANTITY, t_quantity);
	
    	subSum = t_price * t_quantity;
    	if(subSum == 0){
    		Toast.makeText(getApplicationContext(), "数量和价格错误", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	cv.put(SUBSUM, subSum);
    	
	    str = date.getText().toString();
	    if (str.equals("")){
	    	SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");     
	    	Date curDate = new Date(System.currentTimeMillis());//获取当前时间     
	    	str = formatter.format(curDate);  
	    	Log.d(TAG, str);
	    }
	    cv.put(DATE, str); 

	    if(cbOrdered.isChecked()) 
	    	cv.put(ORDERED, getString(R.string.sql_ordered));
	    else 
	    	cv.put(ORDERED, getString(R.string.sql_not_ordered));

	    if(cbShipped.isChecked()) 
	    	cv.put(SHIP, getString(R.string.sql_shipped));
	    else 
	    	cv.put(SHIP, getString(R.string.sql_not_shipped));
	    
	    str = mark.getText().toString();
	    cv.put(MARK, str);  
	    
	    /* 插入数据 */  
	    mSQLiteDatabase.insert(TABLE_NAME, null, cv); 
	    Toast.makeText(getApplicationContext(), "Add Success", Toast.LENGTH_SHORT).show();
	
	    //mmid.getText().clear();
	    article.getText().clear();
	    //price.getText().clear();
	    //quantity.getText().clear();
	    //date.getText().clear();
	    mark.getText().clear();
	}

	protected void delItem(int id){
		//mSQLiteDatabase.delete(TABLE_NAME, NO+" = ?", new String(Integer.toString(id)));
		mSQLiteDatabase.execSQL("DELETE FROM " + TABLE_NAME + " WHERE _id is " + id );
	}
	@SuppressLint("SimpleDateFormat")
	protected void updateItem(int id) {			
		ContentValues cv = new ContentValues(); 
	    String str;
	    int t_price = 0, t_quantity, subSum;
	    
	    str = mmid.getText().toString();
	    if (str.equals("")){
	    	Toast.makeText(getApplicationContext(), "请输入"+getString(R.string.sql_mmid), Toast.LENGTH_SHORT).show();
	    	return;
	    }
	    cv.put(MMID, str); 
	    
	    str = article.getText().toString();
	    if (str.equals("")){
	    	Toast.makeText(getApplicationContext(), "请输入"+getString(R.string.sql_article), Toast.LENGTH_SHORT).show();
	    	return;
	    }
	    cv.put(ARTICLE, str); 
	    
	    str = price.getText().toString();
	    try{
	    	t_price = Integer.parseInt(str);
	    	}catch (NumberFormatException e){
	    		Toast.makeText(getApplicationContext(), "请输入正确"+getString(R.string.sql_price), Toast.LENGTH_SHORT).show();
		    	return;
	    }
	    cv.put(PRICE, t_price); 
	    
	    str = quantity.getText().toString();
	    try{
	    	t_quantity = Integer.parseInt(str);
	    	}catch (NumberFormatException e){
	    		Toast.makeText(getApplicationContext(), "请输入正确"+getString(R.string.sql_quantity), Toast.LENGTH_SHORT).show();
		    	return;
	    }
	    cv.put(QUANTITY, t_quantity);
	
    	subSum = t_price * t_quantity;
    	if(subSum == 0){
    		Toast.makeText(getApplicationContext(), "数量和价格错误", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	cv.put(SUBSUM, subSum);
    	
	    str = date.getText().toString();
	    if (str.equals("")){
	    	SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");     
	    	Date curDate = new Date(System.currentTimeMillis());//获取当前时间     
	    	str = formatter.format(curDate);  
	    	Log.d(TAG, str);
	    }
	    cv.put(DATE, str); 
	    
	    if(cbOrdered.isChecked()) 
	    	cv.put(ORDERED, getString(R.string.sql_ordered));
	    else 
	    	cv.put(ORDERED, getString(R.string.sql_not_ordered));

	    if(cbShipped.isChecked()) 
	    	cv.put(SHIP, getString(R.string.sql_shipped));
	    else 
	    	cv.put(SHIP, getString(R.string.sql_not_shipped));
	    
	    str = mark.getText().toString();
	    cv.put(MARK, str);  
	    
	    /* 插入数据 */
	    Log.d(TAG, "id=" + id);
	    mSQLiteDatabase.update(TABLE_NAME, cv, NO + " = " +id , null); 
	    Toast.makeText(getApplicationContext(), "Modi Success", Toast.LENGTH_SHORT).show();
	
	    mmid.getText().clear();
	    article.getText().clear();
	    price.getText().clear();
	    quantity.getText().clear();
	    date.getText().clear();
	    mark.getText().clear();
	}
	
	@SuppressLint("SimpleDateFormat")
	protected void genDatabase(){
		ProgressDialog mDialog = null;
		ContentValues cv = new ContentValues(); 
	    int price = 0, quantity, subSum, index;
	    String str;
	    mDialog = ProgressDialog.show(SQLActivity.this, "Gen Database", "Generating Database, please wait", true);  
	    
	    Random ran = new Random(200000);
	    
		for(index=0; index<200; index++){
		    cv.put(MMID, "ID_" + ran.nextInt(80)); 
		    
		    str = "物品_"+ran.nextInt(100);
		    cv.put(ARTICLE, str); 
		    
		    price = ran.nextInt(300)+12;
		    cv.put(PRICE, price); 
		    
		    quantity = ran.nextInt(3)+1;
		    cv.put(QUANTITY, quantity);
		
	    	subSum = price * quantity;
	    	if(subSum == 0){
	    		Toast.makeText(getApplicationContext(), "数量和价格错误", Toast.LENGTH_SHORT).show();
	    		continue;
	    	}
	    	cv.put(SUBSUM, subSum);
	    	
	    	int i = ran.nextInt(14);
		    cv.put(DATE, datestr[i]); 
		    
		    if(cbOrdered.isChecked()) 
		    	cv.put(ORDERED, getString(R.string.sql_ordered));
		    else 
		    	cv.put(ORDERED, getString(R.string.sql_not_ordered));

		    if(cbShipped.isChecked()) 
		    	cv.put(SHIP, getString(R.string.sql_shipped));
		    else 
		    	cv.put(SHIP, getString(R.string.sql_not_shipped));
		    
		    boolean tmp = ran.nextBoolean();
	    	if(tmp) str = "Here may show Address";
	    	else str = "Other comments";
		    cv.put(MARK, str);  
		    
		    /* 插入数据 */ 
		    try{
		    	mSQLiteDatabase.insert(TABLE_NAME, null, cv); 
		    }catch (SQLiteException e){
		    	Toast.makeText(getApplicationContext(), "插入数据失败", Toast.LENGTH_SHORT).show();
		    	return;
		    }
		}
		mDialog.dismiss();
	    Toast.makeText(getApplicationContext(), "生成数据成功", Toast.LENGTH_SHORT).show();
	}
	protected void queryInfo(String str){
		int query_sum = 0;
		if(cursor !=null) cursor.close();
		if(str.equals("")){
			Toast.makeText(getApplicationContext(), "查询所有记录", Toast.LENGTH_SHORT).show();
			cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY "+DATE+" ASC", null);
		}else if (str.startsWith("2014") || str.startsWith("2015")){
			Toast.makeText(getApplicationContext(), "按日期查询", Toast.LENGTH_SHORT).show();
			cursor = mSQLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_NAME +" WHERE " + DATE +" LIKE ?"+ " ORDER BY "+DATE+" ASC", 
												new String[]{new String(str +"%%")});  
		}else {
			Toast.makeText(getApplicationContext(), "按姓名查询", Toast.LENGTH_SHORT).show();
			cursor = mSQLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_NAME +" WHERE " + MMID +" LIKE ?"+ " ORDER BY "+DATE+" ASC", 
					new String[]{new String("%%"+ str +"%%")});  
		}

		while (cursor.moveToNext()) {  
			query_sum += cursor.getInt(cursor.getColumnIndex(SUBSUM));
        } 
		String reslut = getString(R.string.sql_query_sum) + query_sum;
		querySum.setText(reslut);
		adapter = new SimpleCursorAdapter(this, R.layout.sqllite_query_listview, cursor, from, to);
		listview.setAdapter(adapter);
	}	

	private void onListViewItemSingleClickHandler(){		
    	listview.setOnItemClickListener(new OnItemClickListener(){
    		@Override
    		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    			cursor.moveToPosition(arg2);
    			mmid.setText(cursor.getString(cursor.getColumnIndex(MMID)));
    			article.setText(cursor.getString(cursor.getColumnIndex(ARTICLE)));
    			price.setText(cursor.getString(cursor.getColumnIndex(PRICE)));
    			quantity.setText(cursor.getString(cursor.getColumnIndex(QUANTITY)));
    			date.setText(cursor.getString(cursor.getColumnIndex(DATE)));
    			
    			cbOrdered.setChecked(cursor.getString(cursor.getColumnIndex(ORDERED)).equals(getString(R.string.sql_ordered)));
    			cbShipped.setChecked(cursor.getString(cursor.getColumnIndex(SHIP)).equals(getString(R.string.sql_shipped)));
    			
    			mark.setText(cursor.getString(cursor.getColumnIndex(MARK)));
    			
    			addBtn.setVisibility(View.VISIBLE);
    			modiBtn.setVisibility(View.GONE);
    			cancelBtn.setVisibility(View.GONE);
    		}			
    	});
	}
	
	private void onListViewItemLongPressHandler(){ 
		//注：setOnCreateContextMenuListener是与下面onContextItemSelected配套使用的 
		listview.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				//menu.setHeaderTitle("ZHU");
				menu.add(0, 0, 0, R.string.sql_modi_one);
				menu.add(0, 1, 0, R.string.sql_del_one); 
                menu.add(0, 2, 0, R.string.sql_del_all); 
                menu.add(0, 3, 0, R.string.sql_cancel); 
			}
		}); 
	}
	//长按菜单响应函数 
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	     AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo(); 
	     // 这里的info.id对应的就是数据库中_id的值， Position对应Adapter中的索引
	     Log.d(TAG, "id=" + info.id + "。 position = " + info.position);
	     switch (item.getItemId()) { 
	     case 0: 
	    	 Toast.makeText(getApplicationContext(), getString(R.string.sql_modi_one), Toast.LENGTH_SHORT).show(); 
	    	 cursor.moveToPosition(info.position);
	    	 modifying_id = (int) info.id;
	    	 addBtn.setVisibility(View.GONE);
	    	 modiBtn.setVisibility(View.VISIBLE);
	    	 cancelBtn.setVisibility(View.VISIBLE);
	    	 mmid.setText(cursor.getString(cursor.getColumnIndex(MMID)));
	    	 article.setText(cursor.getString(cursor.getColumnIndex(ARTICLE)));
	    	 price.setText(cursor.getString(cursor.getColumnIndex(PRICE)));
	    	 quantity.setText(cursor.getString(cursor.getColumnIndex(QUANTITY)));
	    	 date.setText(cursor.getString(cursor.getColumnIndex(DATE)));
	    	 cbOrdered.setChecked(cursor.getString(cursor.getColumnIndex(ORDERED)).equals(getString(R.string.sql_ordered)));
 			 cbShipped.setChecked(cursor.getString(cursor.getColumnIndex(SHIP)).equals(getString(R.string.sql_shipped)));
	    	 mark.setText(cursor.getString(cursor.getColumnIndex(MARK)));
             break;
	     case 1: 
             Toast.makeText(getApplicationContext(), getString(R.string.sql_del_one), Toast.LENGTH_SHORT).show(); 
             delItem((int) (info.id));
             queryInfo(queryinfo.getText().toString());
             break;
	     case 2: 
	    	 Toast.makeText(getApplicationContext(), getString(R.string.sql_del_all), Toast.LENGTH_SHORT).show(); 
	    	 cursor.moveToFirst();
	    	do{  
	    		 delItem( cursor.getInt(cursor.getColumnIndex(NO)));
	    	 }while (cursor.moveToNext());
	    	 queryInfo(queryinfo.getText().toString());
             break;
	     case 3: 
	    	 Toast.makeText(getApplicationContext(), getString(R.string.sql_cancel), Toast.LENGTH_SHORT).show(); 
             break;
	     default: 
             break; 
	     }
	     return super.onContextItemSelected(item);
	}
	/* 这种方式要创建菜单的话格式比较复杂，采用上面创建菜单格式比较清晰的一种方式
	listview.setOnItemLongClickListener(new OnItemLongClickListener() {  
        @Override  
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,  int arg2, long arg3) {  
            // TODO Auto-generated method stub  
            Log.d(TAG, "index = " + arg2);  
            return false;  
        }  
    });  
    */	
	@Override
	protected void onStart() {
        mSQLiteDatabase = this.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null); 
        try  
        {  
            /* 在数据库mSQLiteDatabase中创建一个表 */  
            mSQLiteDatabase.execSQL(CREATE_TABLE);  
        }
        catch (SQLException sqlex)  
        {  
        	sqlex.printStackTrace();
        }  
		super.onStart();
	}

	@Override
	protected void onStop() {
		if(mSQLiteDatabase != null) mSQLiteDatabase.close();
		if(cursor != null) cursor.close();
		super.onStop();
	}
		
	String datestr[] = { "20141010", "20141012", "20141013", "20141014", "20141015", "20141017", "20141020",
			"20151110", "20151212", "20151113", "20151014", "20151215", "20141117", "20141222",
	};
}
