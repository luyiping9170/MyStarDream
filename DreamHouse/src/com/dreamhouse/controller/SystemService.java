package com.dreamhouse.controller;

import com.dreamhouse.arduino.ControlCallback;

public class SystemService {

	private SystemService(){
		
	}
	public static SystemService getSystemService() {
		// TODO �Զ����ɵķ������
		return null;
	}
	
	public void startService(){
		
	}
	
	public void stopService(){
		
	}
	
	/**
	 * ����userID��½ ����״̬��CUSTOMER_VALID��CUSTOMER_INVALID��CUSTOMER_GAMEOVER��CUSTOMER_PREPARE��
	 * @param userID
	 * @return
	 */
	public String loginCustomer(String userID){
		return null;
	}
	
	/**
	 * ����userID��userPW��½������Ȩ�ޣ�Ϊnullʱ��½ʧ��
	 * @param userID
	 * @param userPW
	 * @return
	 */
	public String loginCustomer(String userID,String userPW){
		return null;
	}
	
	/**
	 * ������Ӧarduino��comName������״̬��ARDUINO_NOT_WORK��ARDUINO_IS_WORK��ARDUINO_NOT_OWNED��ARDUINO_NOT_EXIST��
	 * @param arduinoID
	 * @param comName Ϊnullʱִ�а���ԭ��comNameִ��
	 * @return
	 */
	public String setComStatus(String arduinoID,String comName,String type){
		return null;
	}
	
	/**
	 * ����typeѡ��������ͣ�commandΪ�����������,callbackΪ�ص�����
	 * @param type
	 * @param command
	 * @return
	 */
	public String sendCommend(String type,String command,ControlCallback callback){
		return null;
	}

	
	private void updateGeneralStatus(){
		
	}

}
