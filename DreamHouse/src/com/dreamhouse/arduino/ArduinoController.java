package com.dreamhouse.arduino;

public interface ArduinoController {
	
	/**
	 * 初始化串口，并使之正常工作
	 * 
	 * @param myPort
	 *            Port名称
	 * @return 初始化操作后串口状态（同步的状态）
	 */
	public int startControl(String myPort);

	/**
	 * 停止串口工作
	 * 
	 * @return 停止操作后串口状态（同步的状态）
	 */
	public int stopControl();

	/**
	 * @return 当前串口状态（同步的状态）
	 */
	public int getPortStatus();

	/**
	 * 添加控制条目
	 * 
	 * @param thisType
	 *            控制类型，由每个具体控制类具体定义
	 * @param thisDuration
	 *            持续时间/目的操作的超时时间
	 * @param thisCommand
	 *            控制指令，由每个具体控制类具体定义
	 * @param thisTegart
	 *            控制目标，达成将尝试回调，并结束当前控制
	 * @param thisConfirm
	 *            控制确认，达成将尝试回调，但不结束当前控制
	 * @param thisCallback
	 *            回调对象，将调取其中update方法
	 * @return 指令发送后，串口状态
	 */
	public int addControl(String thisType, long thisDuration, char thisCommand,
						  char thisTegart, char thisConfirm, ControlCallback thisCallback);

	/**
	 * 由被观察者回调
	 * 
	 * @param type
	 *            回调类型，用于判断更新操作
	 */
	public void update(int type);
}
