package com.ds.bluetoothUtil;

/**
 * ����������
 * @author Zhu Xiaoping
 *
 */
public class NetworkDefinition {

	/**
	 * �ַ��������������Intent�е��豸����
	 */
	public static final String ADDRESS = "ADDRESS";
	
	/**
	 * �ַ���������Intent�е�����
	 */
	public static final String DATA = "DATA";
	
	/**
	 * Action���ͱ�ʶ����Action����Ϊ δ�����豸
	 */
	public static final String ACTION_HTTP_CONN = "ACTION_HTTP_CONN";

	public static final String ACTION_NW_CLOSE = "ACTION_NW_CLOSE";
	
	public static final String ACTION_READ_HTTP_DATA = "ACTION_READ_HTTP_DATA";
	
	
	public static final int MSG_NW_READ_DATA = 0x12000004;

	/**
	 * Message���ͱ�ʶ�������ӳɹ�
	 */
	public static final int MMG_CONNECT_SUCCESS = 0x12000002;
	
	/**
	 * Message������ʧ��
	 */
	public static final int MSG_CONNECT_ERROR = 0x12000003;
	
}
