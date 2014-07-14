/*******************************************************************************
 * Copyright (c) 2013 Hani Naguib.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Hani Naguib - initial API and implementation
 ******************************************************************************/
package com.gvmax.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField emailText;
	private JTextField pinText;
	private JButton lookupButt;
	private JTextArea userText;
	private JTextArea statsText;
	private JButton unregisterButton;

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("GVMax Admin");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 564, 300);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel lblNewLabel = new JLabel("Email");
		panel.add(lblNewLabel);

		emailText = new JTextField();
		panel.add(emailText);
		emailText.setColumns(20);

		JLabel lblNewLabel1 = new JLabel("PIN");
		panel.add(lblNewLabel1);

		pinText = new JTextField();
		panel.add(pinText);
		pinText.setColumns(20);

		lookupButt = new JButton("Look Up");
		panel.add(lookupButt);

		JPanel panel1 = new JPanel();
		getContentPane().add(panel1, BorderLayout.SOUTH);
		panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		unregisterButton = new JButton("Unregister");
		panel1.add(unregisterButton);

		JPanel panel2 = new JPanel();
		getContentPane().add(panel2, BorderLayout.CENTER);
		panel2.setLayout(new GridLayout(1, 0, 0, 0));

		JPanel panel3 = new JPanel();
		panel3.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel2.add(panel3);
		panel3.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel3.add(scrollPane);

		userText = new JTextArea();
		scrollPane.setViewportView(userText);

		JPanel panel4 = new JPanel();
		panel4.setBorder(new TitledBorder(null, "Stats", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel2.add(panel4);
		panel4.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane1 = new JScrollPane();
		panel4.add(scrollPane1);

		statsText = new JTextArea();
		scrollPane1.setViewportView(statsText);
	}

	public JTextField getEmailText() {
		return emailText;
	}
	public JTextField getPinText() {
		return pinText;
	}
	public JButton getLookupButt() {
		return lookupButt;
	}
	public JTextArea getUserText() {
		return userText;
	}
	public JTextArea getStatsText() {
		return statsText;
	}
	public JButton getUnregisterButton() {
		return unregisterButton;
	}
}
