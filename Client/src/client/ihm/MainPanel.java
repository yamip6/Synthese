package client.ihm;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import client.controller.CreateGroup;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = -6890520086780246974L;
	
	private JButton _createGroup;
	private JTextField _ip;
	private JTextField _port;
	
	private void formCreateGroupe () {
		JFrame f = new JFrame();
		f.setLayout(new GridLayout(3, 2));
		_ip = new JTextField("");
		_port = new JTextField("");
		JButton confirm = new JButton("Confirm");
		JButton cancel = new JButton("Cancel");
		confirm.addActionListener(new CreateGroup(this));
		f.add(new Label("Adress ip : ")); f.add(_ip);
		f.add(new Label("Port : ")); f.add(_port); 
		f.add(confirm); f.add(cancel);
		f.pack(); f.setVisible(true);	
	} // formCreateGroupe ()
	
	public MainPanel () {		
		setLayout(new FlowLayout());
		_createGroup = new JButton("Create group", new ImageIcon("img\\add.png"));
		_createGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				formCreateGroupe();
			}
		});
		add(_createGroup);
	} // MainPanel ()

	public JTextField get_ip() {
		return _ip;
	}

	public void set_ip(JTextField _ip) {
		this._ip = _ip;
	}

	public JTextField get_port() {
		return _port;
	}

	public void set_port(JTextField _port) {
		this._port = _port;
	}
	
} // MainPanel
