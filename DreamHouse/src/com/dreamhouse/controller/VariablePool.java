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
	 * 	����ArduinoID��statusList��ɸ���
	 * @param ArduinoID
	 * @param statusList
	 */
	public void updateFromArduino(int ArduinoID, ArrayList<Character> statusList) {
		// TODO �Զ����ɵķ������
		
	}
	
	/**
	 *  ����ArduinoID����ʱ���
	 * @param ArduinoID
	 */
	public void updateFromArduino(int ArduinoID) {
		// TODO �Զ����ɵķ������
		
	}
	
}
