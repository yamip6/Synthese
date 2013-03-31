package client.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;

import clientSlave.ClientSlave;

public class JoinGroup implements ActionListener {

	private ClientSlave slave;
	
	public JoinGroup (){
		slave = new ClientSlave();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			slave.receiveInvitation();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

}
