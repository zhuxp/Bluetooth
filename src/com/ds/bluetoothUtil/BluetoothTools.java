package com.ds.bluetoothUtil;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

/**
 * ����������
 * @author Zhu Xiaoping
 *
 */
public class BluetoothTools {

	private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	
	/**
	 * ��������ʹ�õ�UUID
	 */
	public static final UUID PRIVATE_UUID = UUID.fromString("0f3561b9-bda5-4672-84ff-ab1f98e349b6");
	
	/**
	 * �ַ��������������Intent�е��豸����
	 */
	public static final String DEVICE = "DEVICE";
	
	/**
	 * �ַ��������������������豸�б��е�λ��
	 */
	public static final String SERVER_INDEX = "SERVER_INDEX";

	
	/**
	 * �ַ���������Intent�е�����
	 */
	public static final String DATA = "DATA";
	
	/**
	 * Action���ͱ�ʶ����Action����Ϊ δ�����豸
	 */
	public static final String ACTION_NOT_FOUND_SERVER = "ACTION_NOT_FOUND_DEVICE";
	
	/**
	 * Action���ͱ�ʶ����Action����Ϊ ��ʼ�����豸
	 */
	public static final String ACTION_START_DISCOVERY = "ACTION_START_DISCOVERY";
	
	/**
	 * Action���豸�б�
	 */
	public static final String ACTION_FOUND_DEVICE = "ACTION_FOUND_DEVICE";
	
	/**
	 * Action��ѡ����������ӵ��豸
	 */
	public static final String ACTION_SELECTED_DEVICE = "ACTION_SELECTED_DEVICE";
	
	/**
	 * Action������������
	 */
	public static final String ACTION_START_SERVER = "ACTION_STARRT_SERVER";
	
	/**
	 * Action���رպ�̨Service
	 */
	public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
	
	/**
	 * Action��Server�˿�ʼͨ��
	 */
	public static final String ACTION_SRV_START_TALKING = "ACTION_SRV_START_TALKING";

	/**
	 * Action��Server��ֹͣͨ��
	 */
	public static final String ACTION_SRV_STOP_TALKING = "ACTION_SRV_STOP_TALKING";
	
	/**
	 * Action��Client�˿�ʼͨ��
	 */
	public static final String ACTION_CLN_STOP_TALKING = "ACTION_CLN_STOP_TALKING";
	
	/**
	 * Action��Client��ֹͣͨ��
	 */
	public static final String ACTION_CLN_START_TALKING = "ACTION_CLN_START_TALKING";
	
	
	/**
	 * Action��Client��ʹ�ã��������ݵ�Server
	 */
	public static final String ACTION_CLIENT_SEND_DATA_TO_SERVER = "ACTION_CLIENT_SEND_DATA_TO_SERVER";
	
	/**
	 * Action��Client��ʹ�ã� �յ� comm thread�ĵ�����
	 */
	public static final String ACTION_CLIENT_RCV_DATA_FROM_SERVER = "ACTION_CLIENT_RCV_DATA_FROM_SERVER";	
	
	/**
	 * Action��Server��ʹ�ã��������ݵ�Client
	 */
	public static final String ACTION_SERVER_SEND_DATA_TO_CLIENT = "ACTION_SERVER_SEND_DATA_TO_CLIENT";
	/**
	 * Action��Server��ʹ�ã� �յ� comm thread�ĵ�����
	 */
	public static final String ACTION_SERVER_RCV_DATA_FROM_CLIENT = "ACTION_SERVER_RCV_DATA_FROM_CLIENT";

	/**
	 * Action�����ӳɹ�
	 */
	public static final String ACTION_CONNECT_SUCCESS = "ACTION_CONNECT_SUCCESS";
	
	/**
	 * Action�����Ӵ���
	 */
	public static final String ACTION_CONNECT_ERROR = "ACTION_CONNECT_ERROR";

	
	/**
	 * �ַ���������Intent�е����ݣ�������Intent�����ݵ�����
	 */
	public static final String DATA_TYPE = "DATA_TYPE";
	
	public static final int DATA_TYPE_VOICE_STREAM = 1;
	public static final int DATA_TYPE_TEXT = 2;
	public static final int DATA_TYPE_COMMAND = 3;

	/**
	 * �ַ���������֪ͨ�Զ˿���Talkingģʽ����
	 */
	public static final int CMD_START_TALKING = 31;
	/**
	 * �ַ���������֪ͨ�Զ�ֹͣTalkingģʽ����
	 */	
	public static final int CMD_STOP_TALKING = 30;
	/**
	 * Message���ͱ�ʶ�������ӳɹ�
	 */
	public static final int MESSAGE_CONNECT_SUCCESS = 0x11000002;
	
	/**
	 * Message������ʧ��
	 */
	public static final int MESSAGE_CONNECT_ERROR = 0x11000003;
	
	/**
	 * Message����ȡ��һ������
	 */
	public static final int MESSAGE_READ_OBJECT = 0x11000004;

	/**
	 * ����������
	 */
	public static void openBluetooth() {
		adapter.enable();
	}
	
	/**
	 * �ر���������
	 */
	public static void closeBluetooth() {
		adapter.disable();
	}
	
	/**
	 * �����������ֹ���
	 * @param duration �����������ֹ��ܴ򿪳���������ֵΪ0��300֮���������
	 */
	public static void openDiscovery(int duration) {
		if (duration <= 0 || duration > 300) {
			duration = 200;
		}
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
	}
	
	/**
	 * ֹͣ��������
	 */
	public static void stopDiscovery() {
		adapter.cancelDiscovery();
	}
	
}
