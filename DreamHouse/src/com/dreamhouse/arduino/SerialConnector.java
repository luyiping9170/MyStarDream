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

	// COM״̬����
	public final static int COM_NOT_INIT = 10;// �־�״̬����δinit
	public final static int COM_WORK = 11;// �־�״̬��������������
	public final static int COM_CLOSE = 12;// �־�״̬������δ��
	public final static int COM_NOT_EXIST = 13;// �־�״̬�����ڲ�����
	public final static int COM_NOT_OWNED = 14;// �־�״̬�����ڱ�ռ��
	// ���ء��ص�����
	public final static int DATA_UPDATE = 30;// ��ʱ״̬�������ڻص�����ʾ�����ݸ���
	public final static int COM_FAIL = 31;// ��ʱ״̬����ʾ��ǰ����ʧ��
	public final static int COM_SENDED = 32;// ��ʱ״̬����ʾ��ǰ����ɹ�

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
	 * ���캯����ע��۲���
	 * 
	 * @param observer
	 *            �۲��߶���
	 */
	public SerialConnector(ArduinoController observer) {
		myObserver = observer;
	}

	/**
	 * ����portname��ʼ��
	 * 
	 * @param portname
	 * @return ���ڳ��Գ�ʼ�����״̬
	 */
	public synchronized int init(String portname) {
		return initialize(portname);
	}

	/**
	 * ��com��
	 * 
	 * @return ���ڳ��Դ򿪺��״̬
	 */
	public synchronized int open() {
		return openSerial();
	}

	/**
	 * �ر�com��
	 * 
	 * @return ���ڳ��Թرպ��״̬
	 */
	public synchronized int close() {
		return closeSerial();
	}

	/**
	 * ����COM_NOT_EXIST��COM_OWNEDʱ����
	 * 
	 * @return ���ڳ������Ժ��״̬
	 */
	public synchronized int retry() {
		initialize(portName);
		return openSerial();
	}

	/**
	 * ���readList����ʱ������
	 * 
	 * @return readList����
	 */
	public ArrayList<String> read() {
		ArrayList<String> newReadList = new ArrayList<String>();
		// ��serialReadList����ǰ��Ҫ�Ȼ����
		synchronized (serialReadList) {
			newReadList.addAll(serialReadList);
			serialReadList.clear();
		}
		return newReadList;
	}

	/**
	 * ������Ϣ
	 * 
	 * @param controlid
	 *            ���͵�ָ���Ҫ����У�飩
	 * @return ���ڷ���ָ����״̬
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
				// ��serialReadList����ǰ��Ҫ�Ȼ����
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
		// ����com�쳣��δ�����崦��*********************
		// else{
		// System.out.print("COM_FAIL");
		// updateObserver();
		// }
	}

	/**
	 * ���Ը���PORT_NAME��ʼ��
	 * @param portname
	 * 
	 * @return ���ڳ��Գ�ʼ�����״̬
	 */
	private int initialize(String portname) {
		System.out.print(portName + " try init when" + portStatus);
		
		// portnameΪ��ʱ�Կ��ַ�������ȷ�����᷵��com_not_init״̬
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
			System.out.println("Could not find COM port��" + portName);
			return setComStatus(COM_NOT_EXIST);
		} else {
			return setComStatus(COM_CLOSE);
		}
	}

	/**
	 * ���Դ�com��
	 * 
	 * @return ���ڳ��Դ򿪺��״̬
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
	 * ���Թر�com��
	 * 
	 * @return ���ڳ��Թرպ��״̬
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
		// ��serialReadList����ǰ��Ҫ�Ȼ����
		synchronized (serialReadList) {
			serialReadList.clear();
		}
		return setComStatus(COM_CLOSE);
	}

	/**
	 * ���õ�ǰ״̬
	 * 
	 * @param currentStatus
	 *            ���ڳ־�״̬
	 * @return ���ڳ־�״̬
	 */
	private int setComStatus(int currentStatus) {
		return portStatus = currentStatus;
	}

	/**
	 * ��exception���д������õ�ǰ״̬
	 * 
	 * @param e
	 *            �쳣
	 * @return ���ڳ־�״̬/��ʱ��COM_FAIL״̬
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
	 * ֪ͨ�۲���
	 * 
	 * @param type
	 */
	private void updateObserver(int type) {
		if (myObserver != null) {
			myObserver.update(type);
		}
	}

}