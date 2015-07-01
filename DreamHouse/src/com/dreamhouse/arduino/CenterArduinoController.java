package com.dreamhouse.arduino;

import com.dreamhouse.controller.VariablePool;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * CenterArduino�ĳ��������࣬��ʵ��
 * @author luyiping
 *
 */
public class CenterArduinoController implements ArduinoController {
	// control type
	public final static String GENERAL = "general";
	public final static String AV = "av";
	public final static String WIRE = "wire";
	public final static String CLOCK = "clock";

	// general
	public final static char RESET = 'z';
	public final static char DAY = 'd';
	public final static char NIGHT = 'e';
	public final static char WRONGID = 'f';
	public final static char OVERTIME = 'g';
	public final static char FLAG0 = 'o';
	public final static char FLAG1 = 'p';
	public final static char FLAG2 = 'q';
	// �����flag��addControl����Ҫ����

	// clock
	public final static char READY = 'd'; // ��arduino���� ��ʾʱ���Ѿ���λ
	public final static char STOP = 'E';
	public final static char FORWARD = 'f';
	public final static char SPEEDFORWARD = 'g';
	public final static char SPEEDBACKWORD = 'h';
	public final static char CLOCKREADY = 'i'; // ʱ�Ӹ�λ ������SPEEDBACKWORD ��READY

	// av
	public final static char AVSTOP = 'd';
	public final static char AVWORK = 'e';

	// wire
	public final static char WIRESTOP = 'd';
	public final static char WIREWORK = 'e';

	// all for clock,av,wire
	public final static char LOCALFIRST = 'x';

	// ����״̬
	public final static int NOT_WORK = 20;
	public final static int IS_WORK = 21;
	public final static int NOT_OWNED = 22;
	public final static int NOT_EXIST = 22;

	// �������״̬
	public final static int CONTROL_SUCCESS = 30;
	public final static int CONTROL_OVERTIME = 31;
	public final static int CONTROL_REPLACE = 32;
	public final static int CONTROL_CONFIRM = 33;

	// ���Ʒ���
	public final static int CONTROL_FAIL = 60;
	public final static int CONTROL_WAIT = 61;
	public final static int CONTROL_SENDED = 62;

	// timeschedule
	public final static long CONTROLSCHEDULE = 1000;

	// ���徲̬����
	private static final CenterArduinoController controlCenter = new CenterArduinoController();
	// ���ڻص����̳߳�
	private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	// ���������߳�
	private ScheduledExecutorService scheduledThreadPool = Executors
			.newSingleThreadScheduledExecutor();
	// �����ص�����
	private VariablePool variablePool = VariablePool.getVariablePool();
	// ��Ӧ��ArduinoID
	private int myArduinoID = VariablePool.ARDUINO_CENTER;
	// ����Controller
	private Controller generalController = new Controller(FLAG0);
	private Controller clockController = new Controller(LOCALFIRST);
	private Controller avController = new Controller(LOCALFIRST);
	private Controller wireController = new Controller(LOCALFIRST);
	// ��¼���һ�ο��Ƶ�ʱ��
	private long lastControlTime = 0;
	// ����ͨѶ����
	private SerialConnector port;
	// ��������
	public String portName;
	// Ĭ�ϴ���״̬Ϊ������,ԭ�Ӳ���
	private volatile int portStatus = NOT_EXIST;
	// �Ƿ�����ִ�У��������������߳����ж�
	private volatile boolean isSchedule = false;
	// �Ƿ����״̬���û�update���ж�
	private volatile boolean isUpdated = false;
	
	// ��controller��������
	private byte[] controlLock = new byte[0];

	/**
	 * ˽�й��캯��
	 */
	private CenterArduinoController() {
		port = new SerialConnector(this);
	}

	/**
	 * ���ʵ������
	 * 
	 * @return
	 */
	public static CenterArduinoController getControl() {
		return controlCenter;
	}

	@Override
	public synchronized int startControl(String myPort) {
		int currentstatus = portStatus;// ���ڼ�¼ִ�в������״̬����ֹȫ�ֱ����䶯
		portName = myPort;
		setPortStatus(port.init(portName));
		currentstatus = setPortStatus(port.open());
		if (!isSchedule && portStatus == IS_WORK) {
			scheduledThreadPool.schedule(new Runnable() {
				@Override
				public void run() {
					if (isSchedule && portStatus == IS_WORK) {
						// ��ʱ����״̬���¡�����ָ��
						updateData();
						sendCommand();
					}
				}
			}, (long) (CONTROLSCHEDULE * 0.2), TimeUnit.MICROSECONDS);
			isSchedule = true;
		}
		return currentstatus;
	}

	@Override
	public synchronized int stopControl() {
		int currentstatus = portStatus;// ���ڼ�¼ִ�в������״̬����ֹȫ�ֱ����䶯
		if (isSchedule) {
			isSchedule = false;
			// ��ʱ����ȷ���߳�shutdownʱ���ڲ��ڱ�����
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			scheduledThreadPool.shutdown();
		}
		currentstatus = setPortStatus(port.close());
		// ��controller����ǰ��Ҫ�Ȼ����
		synchronized (controlLock) {
			// �������δ���ָ����ձ��������ص�
			callback(generalController, CONTROL_REPLACE);
			callback(clockController, CONTROL_REPLACE);
			callback(avController, CONTROL_REPLACE);
			callback(wireController, CONTROL_REPLACE);
		}
		return currentstatus;
	}

	@Override
	public synchronized int addControl(String thisType, long thisDuration,
			char thisCommand, char thisTegart, char thisConfirm,
			ControlCallback thisCallback) {
		// ������������ʱ����
		if (portStatus == IS_WORK) {
			// ��controller����ǰ��Ҫ�Ȼ����
			synchronized (controlLock) {
				// �����ж����ͣ����ԭcontroller���ڻص�����ص�����֪��ȡ��������controller���и�ֵ��
				switch (thisType) {
				case GENERAL:
					callback(generalController, CONTROL_REPLACE);
					generalController.setControl(thisDuration, thisCommand,
							thisTegart, thisConfirm, thisCallback);
					// ���Ϊflag����Ҫ�����趨Ĭ��ֵ
					if (thisCommand == FLAG0 || thisCommand == FLAG1
							|| thisCommand == FLAG2)
						generalController.defaultCommand = thisCommand;
					break;
				case AV:
					callback(avController, CONTROL_REPLACE);
					avController.setControl(thisDuration, thisCommand,
							thisTegart, thisConfirm, thisCallback);
					break;
				case WIRE:
					callback(wireController, CONTROL_REPLACE);
					wireController.setControl(thisDuration, thisCommand,
							thisTegart, thisConfirm, thisCallback);
					break;
				case CLOCK:
					callback(clockController, CONTROL_REPLACE);
					clockController.setControl(thisDuration, thisCommand,
							thisTegart, thisConfirm, thisCallback);
					break;
				default:
					break;
				}
				lastControlTime = 0;// ����ʱ�����ÿ���ָ�������̱�����
			}
			return sendCommand();// ����ָ��õ�������ʱ״̬
		}
		return portStatus;
	}
	
	@Override
	public int getPortStatus() {
		return portStatus;
	}
	
	/**
	 * ��ʶ��currentstatusֵ����¼״��״̬��������ʵ��������س־�״̬/��ʱ״̬
	 * 
	 * @param currentstatus
	 * @return
	 */
	private int setPortStatus(int currentstatus) {
		if (currentstatus == SerialConnector.COM_NOT_INIT)
			currentstatus = port.init(portName);// ��鵽��ûinitʱ�Զ�init
		switch (currentstatus) {
		case SerialConnector.COM_CLOSE:
			portStatus = NOT_WORK;
			break;
		case SerialConnector.COM_NOT_EXIST:
			portStatus = NOT_EXIST;
			break;
		case SerialConnector.COM_NOT_OWNED:
			portStatus = NOT_OWNED;
			break;
		case SerialConnector.COM_WORK:
			portStatus = IS_WORK;
			break;
		case SerialConnector.COM_SENDED:
			portStatus = IS_WORK;
			break;
		default:
			portStatus = NOT_EXIST;
			break;
		}
		return currentstatus;
	}

	/**
	 * һ����scheduledThreadPool�еĶ�ʱ������
	 * 
	 */
	private void updateData() {
		if (isUpdated) {
			ArrayList<String> newReadList = new ArrayList<String>();
				isUpdated = false;
				newReadList.addAll(port.read());
			if (!newReadList.isEmpty()) {
				// ���ζ�arduino���صĵ�״ֵ̬���з�����������Ӧ����
				for (String s : newReadList) {
					boolean isValid = false;
					char general = 'a', clock = 'a', av = 'a', wire = 'a';
					// ����ֵ��Ҫ����λ���������һλ��У��λ
					if (s.length() == 5) {
						general = s.charAt(0);
						clock = s.charAt(1);
						av = s.charAt(2);
						wire = s.charAt(3);
						char check = (char) (general + clock + av + wire - 388);
						if (check == s.charAt(4))
							isValid = true;
					}
					// ��֤���ص���Ϣ�Ϸ���Ա���״̬���и��£�������������Իص�
					if (isValid) {
						ArrayList<Character> statusList = new ArrayList<Character>();
						boolean isChange = false;
						// ��controller����ǰ��Ҫ�Ȼ����
						synchronized (controlLock) {
							isChange |= (generalController.status != general);
							generalController.status = general;
							isChange |= (clockController.status != clock);
							clockController.status = clock;
							isChange |= (avController.status != av);
							avController.status = av;
							isChange |= (wireController.status != wire);
							wireController.status = wire;

							if (isChange) {
								statusList.add(generalController.status);
								statusList.add(clockController.status);
								statusList.add(avController.status);
								statusList.add(wireController.status);
							}

							// �ж��Ƿ����Confirm�����������Իص�
							if (generalController.isConfirm())
								callback(generalController, CONTROL_CONFIRM);
							if (clockController.isConfirm())
								callback(clockController, CONTROL_CONFIRM);
							if (avController.isConfirm())
								callback(avController, CONTROL_CONFIRM);
							if (wireController.isConfirm())
								callback(wireController, CONTROL_CONFIRM);

							// �ж��Ƿ����Tegart�����������Իص�
							if (generalController.isTegart())
								callback(generalController, CONTROL_SUCCESS);
							if (clockController.isTegart())
								callback(clockController, CONTROL_SUCCESS);
							if (avController.isTegart())
								callback(avController, CONTROL_SUCCESS);
							if (wireController.isTegart())
								callback(wireController, CONTROL_SUCCESS);
						}
						// ״̬�仯ʱupdate������
						if (isChange) {
							variablePool.updateFromArduino(myArduinoID,
									statusList);
						} else {
							variablePool.updateFromArduino(myArduinoID);
						}
					}

				}
				System.out.println("�������ݣ�");
			}
		}
	}

	/**
	 * ����controller����ָ��
	 * 
	 * @return ���ͺ󴮿�״̬
	 */
	private int sendCommand() {
		int currentstatus = portStatus;//���ڼ�¼ִ�в������״̬����ֹȫ�ֱ����䶯
		long curtime = System.currentTimeMillis();
		// ��Ҫ����������ڣ���ִ����������
		if (curtime - lastControlTime > CONTROLSCHEDULE) {
			// ����ָ����c��ͷ������λ�����һλ��У��λ
			String myCommand = "c";
			char general = 'a', clock = 'a', av = 'a', wire = 'a';
			// ��controller����ǰ��Ҫ�Ȼ����
			synchronized (controlLock) {
				general = getCommand(generalController, curtime);
				myCommand += general;
				clock = getCommand(clockController, curtime);
				myCommand += clock;
				av = getCommand(avController, curtime);
				myCommand += av;
				wire = getCommand(wireController, curtime);
				myCommand += wire;

				// У��λ
				char check = (char) (general + clock + av + wire - 388);
				myCommand += check;
				currentstatus = setPortStatus(port.write(myCommand));
				if (currentstatus == CONTROL_SENDED)
					lastControlTime = curtime;
			}

		}
		return currentstatus;
	}

	/**
	 * ���controller��commend�����ҽ��г�ʱ�ж� ע�⣺��Ҫ�ڻ��controller���ķ���/�������е���
	 * 
	 * @param thisController
	 * @param curTime
	 * @return ����ָ��
	 */
	private char getCommand(Controller thisController, long curTime) {
		char nowCommand;
		if (thisController.command != 0) {
			if (thisController.startTime == 0)
				thisController.startTime = curTime;
			if (thisController.duration != 0
					&& curTime - thisController.startTime > wireController.duration) {
				nowCommand = thisController.defaultCommand;
				callback(thisController, CONTROL_OVERTIME);
			} else {
				nowCommand = thisController.command;
			}
		} else {
			nowCommand = thisController.defaultCommand;
		}
		return nowCommand;
	}

	/**
	 * ���Խ��лص����������������controller ע�⣺��Ҫ�ڻ��controller���ķ���/�������е���
	 * 
	 * @param thisController
	 * @param controlType
	 */
	private void callback(Controller thisController, final int controlType) {
		// ��̬Controller�����½��߳�
		final Controller callbackController = thisController;
		if (thisController.controlCallback != null) {
			// ����callback����ʱ�����ֳ����ö����update����
			cachedThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					callbackController.controlCallback.callback(controlType);
				}
			});
		}
		// �ص�����ΪCONTROL_SUCCESS��CONTROL_OVERTIME��CONTROL_REPLACEʱ����controller
		if (controlType == CONTROL_SUCCESS || controlType == CONTROL_OVERTIME
				|| controlType == CONTROL_REPLACE) {
			thisController.resetControl();
		}
	}

	/**
	 * Ĭ��ָ����ֿ��ƿ�ʼʱ�䡢�Ƿ��Ѿ�confirm�����ֿ���ʱ��/��ʱ������ָ�Ŀ�ꡢȷ�ϡ���ǰ״̬
	 * 
	 * @author luyiping
	 *
	 */
	private class Controller {
		public char defaultCommand;
		
		public long startTime = 0;
		public boolean isConfirmed=false;//Ŀǰ���Ը���isConfirmed��confirmһ��
		
		public long duration = 0;
		public char command = 0;
		public char tegart = 0;
		public char confirm = 0;
		public char status = 0;
		public ControlCallback controlCallback = null;

		/**
		 * ���캯�� ����Ĭ��ָ��
		 * 
		 * @param thisCommand
		 */
		public Controller(char thisCommand) {
			defaultCommand = thisCommand;
		}

		/**
		 * �ÿ�״̬���� ����controller
		 * 
		 * @param thisDuration
		 * @param thisCommand
		 * @param thisTegart
		 * @param thisConfirm
		 * @param thisCallback
		 */
		public void setControl(long thisDuration, char thisCommand,
				char thisTegart, char thisConfirm, ControlCallback thisCallback) {
			startTime = 0;
			isConfirmed=false;
			
			duration = thisDuration;
			command = thisCommand;
			tegart = thisTegart;
			confirm = thisConfirm;
			controlCallback = thisCallback;
		}

		/**
		 * �ж��Ƿ�ﵽconfirm��ÿ�ֿ������෵��һ��true
		 * @return
		 */
		public boolean isConfirm() {
			if(status == confirm){
				if(isConfirmed){
					return false;
				}
				return isConfirmed=true;
			}
			return false;
		}

		/**
		 * �ж��Ƿ�ﵽtegart
		 * 
		 * @return
		 */
		public boolean isTegart() {
			return status == tegart;
		}
		
		/**
		 * ����Controller
		 */
		public void resetControl() {
			startTime = 0;
			isConfirmed=false;
			
			duration = 0;
			command = 0;
			tegart = 0;
			confirm = 0;
			controlCallback = null;
		}

	}

	@Override
	public void update(int type) {
		if (type == SerialConnector.DATA_UPDATE) {
			isUpdated = true;
		} else {
			setPortStatus(type);
		}
	}

}
