package com.dreamhouse.arduino;

import com.dreamhouse.controller.VariablePool;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * CenterArduino的超级控制类，单实例
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
	// 定义的flag在addControl中需要跟进

	// clock
	public final static char READY = 'd'; // 由arduino返回 表示时钟已经复位
	public final static char STOP = 'E';
	public final static char FORWARD = 'f';
	public final static char SPEEDFORWARD = 'g';
	public final static char SPEEDBACKWORD = 'h';
	public final static char CLOCKREADY = 'i'; // 时钟复位 区别于SPEEDBACKWORD 和READY

	// av
	public final static char AVSTOP = 'd';
	public final static char AVWORK = 'e';

	// wire
	public final static char WIRESTOP = 'd';
	public final static char WIREWORK = 'e';

	// all for clock,av,wire
	public final static char LOCALFIRST = 'x';

	// 串口状态
	public final static int NOT_WORK = 20;
	public final static int IS_WORK = 21;
	public final static int NOT_OWNED = 22;
	public final static int NOT_EXIST = 22;

	// 控制完成状态
	public final static int CONTROL_SUCCESS = 30;
	public final static int CONTROL_OVERTIME = 31;
	public final static int CONTROL_REPLACE = 32;
	public final static int CONTROL_CONFIRM = 33;

	// 控制返回
	public final static int CONTROL_FAIL = 60;
	public final static int CONTROL_WAIT = 61;
	public final static int CONTROL_SENDED = 62;

	// timeschedule
	public final static long CONTROLSCHEDULE = 1000;

	// 定义静态单例
	private static final CenterArduinoController controlCenter = new CenterArduinoController();
	// 用于回调的线程池
	private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	// 周期运行线程
	private ScheduledExecutorService scheduledThreadPool = Executors
			.newSingleThreadScheduledExecutor();
	// 变量池的引用
	private VariablePool variablePool = VariablePool.getVariablePool();
	// 对应的ArduinoID
	private int myArduinoID = VariablePool.ARDUINO_CENTER;
	// 所有Controller
	private Controller generalController = new Controller(FLAG0);
	private Controller clockController = new Controller(LOCALFIRST);
	private Controller avController = new Controller(LOCALFIRST);
	private Controller wireController = new Controller(LOCALFIRST);
	// 记录最后一次控制的时间
	private long lastControlTime = 0;
	// 串口通讯对象
	private SerialConnector port;
	// 串口名称
	public String portName;
	// 默认串口状态为不存在,原子操作
	private volatile int portStatus = NOT_EXIST;
	// 是否周期执行，用于周期运行线程内判断
	private volatile boolean isSchedule = false;
	// 是否更新状态，用户update是判断
	private volatile boolean isUpdated = false;
	
	// 对controller操作的锁
	private byte[] controlLock = new byte[0];

	/**
	 * 私有构造函数
	 */
	private CenterArduinoController() {
		port = new SerialConnector(this);
	}

	/**
	 * 获得实例引用
	 * 
	 * @return
	 */
	public static CenterArduinoController getControl() {
		return controlCenter;
	}

	@Override
	public synchronized int startControl(String myPort) {
		int currentstatus = portStatus;// 用于记录执行操作后的状态，防止全局变量变动
		portName = myPort;
		setPortStatus(port.init(portName));
		currentstatus = setPortStatus(port.open());
		if (!isSchedule && portStatus == IS_WORK) {
			scheduledThreadPool.schedule(new Runnable() {
				@Override
				public void run() {
					if (isSchedule && portStatus == IS_WORK) {
						// 定时进行状态更新、发送指令
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
		int currentstatus = portStatus;// 用于记录执行操作后的状态，防止全局变量变动
		if (isSchedule) {
			isSchedule = false;
			// 延时，以确保线程shutdown时串口不在被调用
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			scheduledThreadPool.shutdown();
		}
		currentstatus = setPortStatus(port.close());
		// 对controller操作前需要先获得锁
		synchronized (controlLock) {
			// 如果尚有未完成指令，按照被替代情况回调
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
		// 串口正常工作时受理
		if (portStatus == IS_WORK) {
			// 对controller操作前需要先获得锁
			synchronized (controlLock) {
				// 首先判断类型；如果原controller存在回调，则回调并告知被取代；其后对controller进行赋值。
				switch (thisType) {
				case GENERAL:
					callback(generalController, CONTROL_REPLACE);
					generalController.setControl(thisDuration, thisCommand,
							thisTegart, thisConfirm, thisCallback);
					// 如果为flag，需要额外设定默认值
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
				lastControlTime = 0;// 清零时间以让控制指令能立刻被发送
			}
			return sendCommand();// 发送指令并得到串口零时状态
		}
		return portStatus;
	}
	
	@Override
	public int getPortStatus() {
		return portStatus;
	}
	
	/**
	 * 将识别currentstatus值，记录状况状态，并根据实际情况返回持久状态/零时状态
	 * 
	 * @param currentstatus
	 * @return
	 */
	private int setPortStatus(int currentstatus) {
		if (currentstatus == SerialConnector.COM_NOT_INIT)
			currentstatus = port.init(portName);// 检查到还没init时自动init
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
	 * 一般由scheduledThreadPool中的定时器调用
	 * 
	 */
	private void updateData() {
		if (isUpdated) {
			ArrayList<String> newReadList = new ArrayList<String>();
				isUpdated = false;
				newReadList.addAll(port.read());
			if (!newReadList.isEmpty()) {
				// 依次对arduino返回的的状态值进行分析，并作相应处理
				for (String s : newReadList) {
					boolean isValid = false;
					char general = 'a', clock = 'a', av = 'a', wire = 'a';
					// 返回值需要是五位，并且最后一位是校验位
					if (s.length() == 5) {
						general = s.charAt(0);
						clock = s.charAt(1);
						av = s.charAt(2);
						wire = s.charAt(3);
						char check = (char) (general + clock + av + wire - 388);
						if (check == s.charAt(4))
							isValid = true;
					}
					// 验证返回的信息合法后对本地状态进行更新，并根据情况尝试回调
					if (isValid) {
						ArrayList<Character> statusList = new ArrayList<Character>();
						boolean isChange = false;
						// 对controller操作前需要先获得锁
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

							// 判断是否符合Confirm条件，并尝试回调
							if (generalController.isConfirm())
								callback(generalController, CONTROL_CONFIRM);
							if (clockController.isConfirm())
								callback(clockController, CONTROL_CONFIRM);
							if (avController.isConfirm())
								callback(avController, CONTROL_CONFIRM);
							if (wireController.isConfirm())
								callback(wireController, CONTROL_CONFIRM);

							// 判断是否符合Tegart条件，并尝试回调
							if (generalController.isTegart())
								callback(generalController, CONTROL_SUCCESS);
							if (clockController.isTegart())
								callback(clockController, CONTROL_SUCCESS);
							if (avController.isTegart())
								callback(avController, CONTROL_SUCCESS);
							if (wireController.isTegart())
								callback(wireController, CONTROL_SUCCESS);
						}
						// 状态变化时update变量池
						if (isChange) {
							variablePool.updateFromArduino(myArduinoID,
									statusList);
						} else {
							variablePool.updateFromArduino(myArduinoID);
						}
					}

				}
				System.out.println("读到数据！");
			}
		}
	}

	/**
	 * 根据controller发送指令
	 * 
	 * @return 发送后串口状态
	 */
	private int sendCommand() {
		int currentstatus = portStatus;//用于记录执行操作后的状态，防止全局变量变动
		long curtime = System.currentTimeMillis();
		// 需要超过间隔周期，才执行周期任务
		if (curtime - lastControlTime > CONTROLSCHEDULE) {
			// 控制指令以c起头，共五位，最后一位是校验位
			String myCommand = "c";
			char general = 'a', clock = 'a', av = 'a', wire = 'a';
			// 对controller操作前需要先获得锁
			synchronized (controlLock) {
				general = getCommand(generalController, curtime);
				myCommand += general;
				clock = getCommand(clockController, curtime);
				myCommand += clock;
				av = getCommand(avController, curtime);
				myCommand += av;
				wire = getCommand(wireController, curtime);
				myCommand += wire;

				// 校验位
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
	 * 获得controller的commend，并且进行超时判断 注意：需要在获得controller锁的方法/方法块中调用
	 * 
	 * @param thisController
	 * @param curTime
	 * @return 控制指令
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
	 * 尝试进行回调，并根据情况重置controller 注意：需要在获得controller锁的方法/方法块中调用
	 * 
	 * @param thisController
	 * @param controlType
	 */
	private void callback(Controller thisController, final int controlType) {
		// 终态Controller用于新建线程
		final Controller callbackController = thisController;
		if (thisController.controlCallback != null) {
			// 存在callback对象时另启现场调用对象的update方法
			cachedThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					callbackController.controlCallback.callback(controlType);
				}
			});
		}
		// 回调类型为CONTROL_SUCCESS、CONTROL_OVERTIME、CONTROL_REPLACE时重置controller
		if (controlType == CONTROL_SUCCESS || controlType == CONTROL_OVERTIME
				|| controlType == CONTROL_REPLACE) {
			thisController.resetControl();
		}
	}

	/**
	 * 默认指令、本轮控制开始时间、是否已经confirm、本轮控制时长/超时、控制指令、目标、确认、当前状态
	 * 
	 * @author luyiping
	 *
	 */
	private class Controller {
		public char defaultCommand;
		
		public long startTime = 0;
		public boolean isConfirmed=false;//目前策略根据isConfirmed仅confirm一次
		
		public long duration = 0;
		public char command = 0;
		public char tegart = 0;
		public char confirm = 0;
		public char status = 0;
		public ControlCallback controlCallback = null;

		/**
		 * 构造函数 传入默认指令
		 * 
		 * @param thisCommand
		 */
		public Controller(char thisCommand) {
			defaultCommand = thisCommand;
		}

		/**
		 * 置空状态变量 设置controller
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
		 * 判断是否达到confirm，每轮控制至多返回一次true
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
		 * 判断是否达到tegart
		 * 
		 * @return
		 */
		public boolean isTegart() {
			return status == tegart;
		}
		
		/**
		 * 重置Controller
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
