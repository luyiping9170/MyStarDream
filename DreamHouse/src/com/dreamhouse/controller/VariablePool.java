package com.dreamhouse.controller;

import java.util.ArrayList;

public class VariablePool {
	public static final int ARDUINO_CENTER = 10;
	private static final VariablePool myVariablePool=new VariablePool();
	private VariablePool(){
		
	}
	public static VariablePool getVariablePool(){
		return myVariablePool;
	}
	
	/**
	 * 	根据ArduinoID和statusList完成更新
	 * @param ArduinoID
	 * @param statusList
	 */
	public void updateFromArduino(int ArduinoID, ArrayList<Character> statusList) {
		// TODO 自动生成的方法存根
		
	}
	
	/**
	 *  根据ArduinoID更新时间戳
	 * @param ArduinoID
	 */
	public void updateFromArduino(int ArduinoID) {
		// TODO 自动生成的方法存根
		
	}
	
}
