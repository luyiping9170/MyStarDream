package com.dreamhouse.view;

import com.dreamhouse.arduino.ArduinoController;
import com.dreamhouse.arduino.ControlCallback;
import com.dreamhouse.arduino.SerialConnector;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

public class ComPanel extends JPanel implements ArduinoController {
	String status;
	JTextArea show;
	JTextField text;
	JButton submit, connect, stop,retry;
	SerialConnector port;
	JPanel write;
	JPanel read;
	String testcom="COM3";

	public ComPanel() {
		status = new String("");
		port = new SerialConnector(this);
		port.init(testcom);
		show = new JTextArea(20, 25);
		show.setLineWrap(true);
		show.setAutoscrolls(true);
		text = new JTextField("", 25);
		submit = new JButton("�ύ");
		connect = new JButton("����");
		stop = new JButton("�Ͽ�");
		retry = new JButton("����");
		write = new JPanel();
		read = new JPanel();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		submit.addActionListener(new submitListener());
		connect.addActionListener(new connectListener());
		stop.addActionListener(new stopListener());
		retry.addActionListener(new retryListener());
		write.setPreferredSize(new Dimension(300, 70));
		write.add(text);
		write.add(connect);
		write.add(submit);
		write.add(stop);
		write.add(retry);
		write.setBackground(Color.CYAN);
		read.setPreferredSize(new Dimension(300, 375));
		read.setBackground(Color.yellow);
		read.add(show);
		add(read);
		add(write);
	}

	private class submitListener implements ActionListener {// �ύ��ť�ļ�����
		public void actionPerformed(ActionEvent event) {
			String controlid = text.getText();
			int status=port.write(controlid);
			System.out.println("�ύ��");
			if(status==SerialConnector.COM_NOT_EXIST){
				show.append("COM_NOT_EXIST");
			}else if(status==SerialConnector.COM_NOT_OWNED){
				show.append("COM_OWNED");
			}else if(status==SerialConnector.COM_CLOSE){
				show.append("COM_CLOSE");
			}else if(status==SerialConnector.COM_FAIL){
				show.append("COM_FAIL");
			}
			repaint();
		}
	}

	private class connectListener implements ActionListener {// ���Ӱ�ť�ļ�����
		public void actionPerformed(ActionEvent event) {
			port.open();
			System.out.println("���ӣ�");
		}
	}

	private class stopListener implements ActionListener {// �Ͽ���ť�ļ�����
		public void actionPerformed(ActionEvent event) {
			port.close();
			System.out.println("�Ͽ���");
		}
	}
	private class retryListener implements ActionListener {// �Ͽ���ť�ļ�����
		public void actionPerformed(ActionEvent event) {
			port.retry();
			System.out.println("���ԣ�");
		}
	}
	
	public void updateData() {
		ArrayList<String> newReadList=port.read();
		if (!newReadList.isEmpty()) {
			for(String s:newReadList){
				status = s+"";
				show.append(status);	
				repaint();
			}
			System.out.println("�������ݣ�");
		}
	}

	@Override
	public int startControl(String myPort) {
		return 0;
	}

	@Override
	public int stopControl() {
		return 0;
	}

	@Override
	public int getPortStatus() {
		return 0;
	}

	@Override
	public int addControl(String thisType, long thisDuration, char thisCommand, char thisTegart, char thisConfirm, ControlCallback thisCallback) {
		return 0;
	}

	public synchronized void update(int type) {
		if(type==SerialConnector.DATA_UPDATE){
			updateData();
		}else{
			show.append("���ӶϿ�");
			repaint();
		}
	}

}
