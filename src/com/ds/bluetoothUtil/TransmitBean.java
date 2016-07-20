package com.ds.bluetoothUtil;

import java.io.Serializable;

/**
 * ���ڴ����������
 * @author Zhu Xiaoping
 *
 */
public class TransmitBean implements Serializable{

	/**
	 ��֪����ʾ�������ID��ʲô�ã� 
	 */
	private static final long serialVersionUID = -7421082160290690821L;
	private String msg = "";
	private byte[] stream = null;
	private int cmd = 0;
	private int dataType = 0;

	public TransmitBean(){

	}

	public TransmitBean(int cmd){
		dataType = BluetoothTools.DATA_TYPE_COMMAND;
		this.cmd = cmd;
	}

	public TransmitBean(String msg){
		dataType = BluetoothTools.DATA_TYPE_TEXT;
		this.msg = msg;
	}

	public TransmitBean(byte[] stream){
		dataType = BluetoothTools.DATA_TYPE_VOICE_STREAM;
		this.stream = stream;
	}
	
	public void setCmd(int cmd) {
		dataType = BluetoothTools.DATA_TYPE_COMMAND;
		this.cmd = cmd;
	}
	
	public void setMsg(String msg) {
		dataType = BluetoothTools.DATA_TYPE_TEXT;
		this.msg = msg;
	}
	
	public void setStream(byte[] stream){
		dataType = BluetoothTools.DATA_TYPE_VOICE_STREAM;
		this.stream = stream;
	}
	
	public int getCmd() {
		return this.cmd;
	}
	
	public String getMsg() {
		return this.msg;
	}

	public byte[] getStream(){
		return this.stream;
	}
	
	public int getDataType(){
		return this.dataType;
	}	
}
