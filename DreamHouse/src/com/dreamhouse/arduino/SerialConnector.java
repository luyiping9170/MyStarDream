package com.dreamhouse.arduino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.ArrayList;
import java.util.Enumeration;

public class SerialConnector implements SerialPortEventListener {
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	// COM状态定义
	public final static int COM_NOT_INIT = 10;// 持久状态，尚未init
	public final static int COM_WORK = 11;// 持久状态，串口正常工作
	public final static int COM_CLOSE = 12;// 持久状态，串口未打开
	public final static int COM_NOT_EXIST = 13;// 持久状态，串口不存在
	public final static int COM_NOT_OWNED = 14;// 持久状态，串口被占用
	// 返回、回调类型
	public final static int DATA_UPDATE = 30;// 零时状态，仅用于回调，表示有数据更新
	public final static int COM_FAIL = 31;// 零时状态，表示当前任务失败
	public final static int COM_SENDED = 32;// 零时状态，表示当前任务成功

	/** The port we're normally going to use. */
	/**
	 * A BufferedReader which will be fed by a InputStreamReader converting the
	 * bytes into characters making the displayed results codepage independent
	 */
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	private ArrayList<String> serialReadList = new ArrayList<String>();
	private volatile int portStatus = COM_NOT_INIT;
	private String portName;
	private SerialPort serialPort;
	private CommPortIdentifier portId;
	private ArduinoController myObserver;
	
	/**
	 * 构造函数，注册观察者
	 * 
	 * @param observer
	 *            观察者对象
	 */
	public SerialConnector(ArduinoController observer) {
		myObserver = observer;
	}

	/**
	 * 根据portname初始化
	 * 
	 * @param portname
	 * @return 串口尝试初始化后的状态
	 */
	public synchronized int init(String portname) {
		return initialize(portname);
	}

	/**
	 * 打开com口
	 * 
	 * @return 串口尝试打开后的状态
	 */
	public synchronized int open() {
		return openSerial();
	}

	/**
	 * 关闭com口
	 * 
	 * @return 串口尝试关闭后的状态
	 */
	public synchronized int close() {
		return closeSerial();
	}

	/**
	 * 仅供COM_NOT_EXIST及COM_OWNED时重试
	 * 
	 * @return 串口尝试重试后的状态
	 */
	public synchronized int retry() {
		initialize(portName);
		return openSerial();
	}

	/**
	 * 获得readList，按时间排序
	 * 
	 * @return readList副本
	 */
	public ArrayList<String> read() {
		ArrayList<String> newReadList = new ArrayList<String>();
		// 对serialReadList操作前需要先获得锁
		synchronized (serialReadList) {
			newReadList.addAll(serialReadList);
			serialReadList.clear();
		}
		return newReadList;
	}

	/**
	 * 发送信息
	 * 
	 * @param controlid
	 *            发送的指令（需要已做校验）
	 * @return 串口发送指令后的状态
	 */
	public int write(String controlid) {
		if (portStatus == COM_WORK) {
			try {
				output.write(controlid.getBytes());
				return COM_SENDED;
			} catch (Exception e) {
				return setComStatus(e);
			}
		}
		return portStatus;
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 * 
	 */
	public void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			String inputLine;
			try {
				// 对serialReadList操作前需要先获得锁
				synchronized (serialReadList) {
					while ((inputLine = input.readLine()) != null) {
						serialReadList.add(inputLine);
					}
				}
				setComStatus(COM_WORK);
				updateObserver(DATA_UPDATE);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				setComStatus(e);
			}
		}
		// 对于com异常尚未做具体处理*********************
		// else{
		// System.out.print("COM_FAIL");
		// updateObserver();
		// }
	}

	/**
	 * 尝试根据PORT_NAME初始化
	 * @param portname
	 * 
	 * @return 串口尝试初始化后的状态
	 */
	private int initialize(String portname) {
		System.out.print(portName + " try init when" + portStatus);
		
		// portname为空时以空字符串处理，确保不会返回com_not_init状态
		portName=(portname!=null)?portname:"";
		
		if (portStatus != COM_NOT_INIT) {
			closeSerial();
			portId = null;
			setComStatus(COM_NOT_INIT);
		}

		System.setProperty("gnu.io.rxtx.SerialPorts", portName);
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		// First, Find an instance of serial port as PORT_NAME.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
					.nextElement();
			if (currPortId.getName().equals(portName)) {
				portId = currPortId;
				break;
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port：" + portName);
			return setComStatus(COM_NOT_EXIST);
		} else {
			return setComStatus(COM_CLOSE);
		}
	}

	/**
	 * 尝试打开com口
	 * 
	 * @return 串口尝试打开后的状态
	 */
	private int openSerial() {
		System.out.print(portName + " try open when" + portStatus);
		if (portStatus == COM_CLOSE && portId!=null) {
			try {
				// open serial port, and use class name for the appName.
				serialPort = (SerialPort) portId.open(
						this.getClass().getName(), TIME_OUT);

				// set port parameters
				serialPort.setSerialPortParams(DATA_RATE,
						SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				// open the streams
				input = new BufferedReader(new InputStreamReader(
						serialPort.getInputStream()));
				output = serialPort.getOutputStream();

				// add event listeners
				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
				return setComStatus(COM_WORK);
			} catch (Exception e) {
				System.err.println(e.toString());
				return setComStatus(e);
			}
		}
		return portStatus;
	}

	/**
	 * 尝试关闭com口
	 * 
	 * @return 串口尝试关闭后的状态
	 */
	private int closeSerial() {
		System.out.print(portName + " try close when" + portStatus);
		if (serialPort != null) {
			serialPort.notifyOnDataAvailable(false);
			serialPort.removeEventListener();
			try {
				input.close();
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			serialPort.close();
			serialPort = null;
		}
		// 对serialReadList操作前需要先获得锁
		synchronized (serialReadList) {
			serialReadList.clear();
		}
		return setComStatus(COM_CLOSE);
	}

	/**
	 * 设置当前状态
	 * 
	 * @param currentStatus
	 *            串口持久状态
	 * @return 串口持久状态
	 */
	private int setComStatus(int currentStatus) {
		return portStatus = currentStatus;
	}

	/**
	 * 对exception进行处理，设置当前状态
	 * 
	 * @param e
	 *            异常
	 * @return 串口持久状态/零时的COM_FAIL状态
	 */
	private int setComStatus(Exception e) {
		if (e instanceof gnu.io.PortInUseException) {
			return setComStatus(COM_NOT_OWNED);
		} else if (e instanceof NoSuchPortException) {
			return setComStatus(COM_NOT_EXIST);
		} else {
			return COM_FAIL;
		}

	}

	/**
	 * 通知观察者
	 * 
	 * @param type
	 */
	private void updateObserver(int type) {
		if (myObserver != null) {
			myObserver.update(type);
		}
	}

}