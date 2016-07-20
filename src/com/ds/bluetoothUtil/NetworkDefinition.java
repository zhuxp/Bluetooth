package com.ds.bluetoothUtil;

/**
 * 蓝牙工具类
 * @author Zhu Xiaoping
 *
 */
public class NetworkDefinition {

	/**
	 * 字符串常量，存放在Intent中的设备对象
	 */
	public static final String ADDRESS = "ADDRESS";
	
	/**
	 * 字符串常量，Intent中的数据
	 */
	public static final String DATA = "DATA";
	
	/**
	 * Action类型标识符，Action类型为 未发现设备
	 */
	public static final String ACTION_HTTP_CONN = "ACTION_HTTP_CONN";

	public static final String ACTION_NW_CLOSE = "ACTION_NW_CLOSE";
	
	public static final String ACTION_READ_HTTP_DATA = "ACTION_READ_HTTP_DATA";
	
	
	public static final int MSG_NW_READ_DATA = 0x12000004;

	/**
	 * Message类型标识符，连接成功
	 */
	public static final int MMG_CONNECT_SUCCESS = 0x12000002;
	
	/**
	 * Message：连接失败
	 */
	public static final int MSG_CONNECT_ERROR = 0x12000003;
	
}
