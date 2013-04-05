package client.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import clientSlave.ClientSlave;

public class JoinGroup implements ActionListener {

	private ClientSlave _slave;
	
	public JoinGroup () {
		_slave = new ClientSlave();
	} // JoinGroup ()
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			_slave.receiveInvitation();
			//slave.requestJoinGroup("toto", InetAddress.getByName("192.168.1.107"));
			_slave.linkNeighboor(/*"192.168.1.109"*/"127.0.0.1");
		} catch (IOException | ClassNotFoundException e1) {
			e1.printStackTrace();
		}	
	} // actionPerformed ()

} // JoinGroup
