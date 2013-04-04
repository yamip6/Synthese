package client.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

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
			//slave.requestJoinGroup("toto", InetAddress.getByName("192.168.1.107"));
			slave.linkNeighboor(InetAddress.getByName(/*"192.168.1.109"*/"127.0.0.1"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

}
