package com.dreamhouse.arduino;

public interface ArduinoController {
	
	/**
	 * ��ʼ�����ڣ���ʹ֮��������
	 * 
	 * @param myPort
	 *            Port����
	 * @return ��ʼ�������󴮿�״̬��ͬ����״̬��
	 */
	public int startControl(String myPort);

	/**
	 * ֹͣ���ڹ���
	 * 
	 * @return ֹͣ�����󴮿�״̬��ͬ����״̬��
	 */
	public int stopControl();

	/**
	 * @return ��ǰ����״̬��ͬ����״̬��
	 */
	public int getPortStatus();

	/**
	 * ��ӿ�����Ŀ
	 * 
	 * @param thisType
	 *            �������ͣ���ÿ�������������嶨��
	 * @param thisDuration
	 *            ����ʱ��/Ŀ�Ĳ����ĳ�ʱʱ��
	 * @param thisCommand
	 *            ����ָ���ÿ�������������嶨��
	 * @param thisTegart
	 *            ����Ŀ�꣬��ɽ����Իص�����������ǰ����
	 * @param thisConfirm
	 *            ����ȷ�ϣ���ɽ����Իص�������������ǰ����
	 * @param thisCallback
	 *            �ص����󣬽���ȡ����update����
	 * @return ָ��ͺ󣬴���״̬
	 */
	public int addControl(String thisType, long thisDuration, char thisCommand,
						  char thisTegart, char thisConfirm, ControlCallback thisCallback);

	/**
	 * �ɱ��۲��߻ص�
	 * 
	 * @param type
	 *            �ص����ͣ������жϸ��²���
	 */
	public void update(int type);
}
