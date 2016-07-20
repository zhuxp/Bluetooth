package com.ds.bluetoothUtil;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

/**
 * 蓝牙工具类
 * @author Zhu Xiaoping
 *
 */
public class BluetoothTools {

	private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	
	/**
	 * 本程序所使用的UUID
	 */
	public static final UUID PRIVATE_UUID = UUID.fromString("0f3561b9-bda5-4672-84ff-ab1f98e349b6");
	
	/**
	 * 字符串常量，存放在Intent中的设备对象
	 */
	public static final String DEVICE = "DEVICE";
	
	/**
	 * 字符串常量，服务器所在设备列表中的位置
	 */
	public static final String SERVER_INDEX = "SERVER_INDEX";

	
	/**
	 * 字符串常量，Intent中的数据
	 */
	public static final String DATA = "DATA";
	
	/**
	 * Action类型标识符，Action类型为 未发现设备
	 */
	public static final String ACTION_NOT_FOUND_SERVER = "ACTION_NOT_FOUND_DEVICE";
	
	/**
	 * Action类型标识符，Action类型为 开始搜索设备
	 */
	public static final String ACTION_START_DISCOVERY = "ACTION_START_DISCOVERY";
	
	/**
	 * Action：设备列表
	 */
	public static final String ACTION_FOUND_DEVICE = "ACTION_FOUND_DEVICE";
	
	/**
	 * Action：选择的用于连接的设备
	 */
	public static final String ACTION_SELECTED_DEVICE = "ACTION_SELECTED_DEVICE";
	
	/**
	 * Action：开启服务器
	 */
	public static final String ACTION_START_SERVER = "ACTION_STARRT_SERVER";
	
	/**
	 * Action：关闭后台Service
	 */
	public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
	
	/**
	 * Action：Server端开始通话
	 */
	public static final String ACTION_SRV_START_TALKING = "ACTION_SRV_START_TALKING";

	/**
	 * Action：Server端停止通话
	 */
	public static final String ACTION_SRV_STOP_TALKING = "ACTION_SRV_STOP_TALKING";
	
	/**
	 * Action：Client端开始通话
	 */
	public static final String ACTION_CLN_STOP_TALKING = "ACTION_CLN_STOP_TALKING";
	
	/**
	 * Action：Client端停止通话
	 */
	public static final String ACTION_CLN_START_TALKING = "ACTION_CLN_START_TALKING";
	
	
	/**
	 * Action：Client端使用，发送数据到Server
	 */
	public static final String ACTION_CLIENT_SEND_DATA_TO_SERVER = "ACTION_CLIENT_SEND_DATA_TO_SERVER";
	
	/**
	 * Action：Client端使用， 收到 comm thread的的数据
	 */
	public static final String ACTION_CLIENT_RCV_DATA_FROM_SERVER = "ACTION_CLIENT_RCV_DATA_FROM_SERVER";	
	
	/**
	 * Action：Server端使用，发送数据到Client
	 */
	public static final String ACTION_SERVER_SEND_DATA_TO_CLIENT = "ACTION_SERVER_SEND_DATA_TO_CLIENT";
	/**
	 * Action：Server端使用， 收到 comm thread的的数据
	 */
	public static final String ACTION_SERVER_RCV_DATA_FROM_CLIENT = "ACTION_SERVER_RCV_DATA_FROM_CLIENT";

	/**
	 * Action：连接成功
	 */
	public static final String ACTION_CONNECT_SUCCESS = "ACTION_CONNECT_SUCCESS";
	
	/**
	 * Action：连接错误
	 */
	public static final String ACTION_CONNECT_ERROR = "ACTION_CONNECT_ERROR";

	
	/**
	 * 字符串常量，Intent中的数据，表明该Intent中数据的类型
	 */
	public static final String DATA_TYPE = "DATA_TYPE";
	
	public static final int DATA_TYPE_VOICE_STREAM = 1;
	public static final int DATA_TYPE_TEXT = 2;
	public static final int DATA_TYPE_COMMAND = 3;

	/**
	 * 字符串常量，通知对端开启Talking模式命令
	 */
	public static final int CMD_START_TALKING = 31;
	/**
	 * 字符串常量，通知对端停止Talking模式命令
	 */	
	public static final int CMD_STOP_TALKING = 30;
	/**
	 * Message类型标识符，连接成功
	 */
	public static final int MESSAGE_CONNECT_SUCCESS = 0x11000002;
	
	/**
	 * Message：连接失败
	 */
	public static final int MESSAGE_CONNECT_ERROR = 0x11000003;
	
	/**
	 * Message：读取到一个对象
	 */
	public static final int MESSAGE_READ_OBJECT = 0x11000004;

	/**
	 * 打开蓝牙功能
	 */
	public static void openBluetooth() {
		adapter.enable();
	}
	
	/**
	 * 关闭蓝牙功能
	 */
	public static void closeBluetooth() {
		adapter.disable();
	}
	
	/**
	 * 设置蓝牙发现功能
	 * @param duration 设置蓝牙发现功能打开持续秒数（值为0至300之间的整数）
	 */
	public static void openDiscovery(int duration) {
		if (duration <= 0 || duration > 300) {
			duration = 200;
		}
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
	}
	
	/**
	 * 停止蓝牙搜索
	 */
	public static void stopDiscovery() {
		adapter.cancelDiscovery();
	}
	
}
