package client.controller;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import client.ihm.MainPanel;
import clientMaster.ClientMaster;

public class CreateGroup implements ActionListener {
	
	private ClientMaster master;
	private String _adressServer;
	private int _portServer;
	private MainPanel _mainPanel;

	
	
	public CreateGroup (MainPanel pan) {
		_mainPanel = pan;
	}
	
	private void create () {
		String ip   = _mainPanel.get_ip().getText();
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
		master = new ClientMaster(_adressServer, _portServer);
		try {
			master.requestCreationGroup("toto");
			master.responseCreationGroup("toto");
			invitation();
		} catch (IOException e) {
			//JOptionPane.showConfirmDialog(_formIP, "Address IP and Port must be number", "Error parsing", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
	} // create ()
	
	private void invitation () {

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					master.Invitation("toto", 9301);
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
						master.set_start(true);
						try {
							// On considère 1 seul serveur 
							master.creationGroupDiscussion();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				f.add(start); f.pack(); f.setVisible(true);
				
			}
		});
		t.start();
		t2.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		create();
	}

}
