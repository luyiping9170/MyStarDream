package com.dreamhouse.controller;

import com.dreamhouse.arduino.ControlCallback;

public class SystemService {

	private SystemService(){
		
	}
	public static SystemService getSystemService() {
		// TODO 自动生成的方法存根
		return null;
	}
	
	public void startService(){
		
	}
	
	public void stopService(){
		
	}
	
	/**
	 * 根据userID登陆 返回状态（CUSTOMER_VALID、CUSTOMER_INVALID、CUSTOMER_GAMEOVER、CUSTOMER_PREPARE）
	 * @param userID
	 * @return
	 */
	public String loginCustomer(String userID){
		return null;
	}
	
	/**
	 * 根据userID，userPW登陆，返回权限，为null时登陆失败
	 * @param userID
	 * @param userPW
	 * @return
	 */
	public String loginCustomer(String userID,String userPW){
		return null;
	}
	
	/**
	 * 设置相应arduino的comName，返回状态（ARDUINO_NOT_WORK、ARDUINO_IS_WORK、ARDUINO_NOT_OWNED、ARDUINO_NOT_EXIST）
	 * @param arduinoID
	 * @param comName 为null时执行按照原有comName执行
	 * @return
	 */
	public String setComStatus(String arduinoID,String comName,String type){
		return null;
	}
	
	/**
	 * 根据type选择控制类型，command为具体控制内容,callback为回调对象
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
