package client.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;

import client.ihm.MainPanel;
import clientMaster.ClientMaster;

import java.io.IOException;

public class CreateGroup implements ActionListener {
	
	private ClientMaster _master;
	private String _adressServer;
	private int _portServer;
	private MainPanel _mainPanel;
	
	public CreateGroup (MainPanel panel) {
		_mainPanel = panel;
	} // CreateGroup()
	
	private void create () throws Exception {
		String ip = _mainPanel.get_ip().getText();
		_adressServer = ip;
		String portS = _mainPanel.get_port().getText();
		int port;
		try {
			port = Integer.parseInt(portS);
			_portServer = port;
		}
		catch (NumberFormatException e){
			e.printStackTrace();
		}
		_master = new ClientMaster(_adressServer, _portServer, "Yassine");
		try {
			_master.requestCreationGroup("toto"); // Test
			_master.responseCreationGroup();
			invitation();
		} catch (IOException | ClassNotFoundException e) {
			//JOptionPane.showConfirmDialog(_formIP, "Address IP and Port must be number", "Error parsing", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
	} // create ()
	
	private void invitation () {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					_master.Invitation("toto", 9301);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				JFrame f = new JFrame(); JButton start = new JButton("Start discussion");
				start.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						_master.set_start(true);
						try {
							// On considère un seul serveur 
							_master.creationGroupDiscussion();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				f.add(start); f.pack(); f.setVisible(true);			
			}
		});
		t.start();
		t2.start();
	} // invitation ()

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			create();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	} // actionPerformed ()

} // CreateGroup
