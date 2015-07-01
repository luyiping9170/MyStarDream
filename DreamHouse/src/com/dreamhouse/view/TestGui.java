package com.dreamhouse.view;

import com.dreamhouse.view.ComPanel;

import javax.swing.*;

public class TestGui {

	public static void main(String[] args)
	{
		JFrame frame=new JFrame("√‹Ã∏");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new ComPanel());
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
	}
}
