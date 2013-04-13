package client.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import client.ihm.MainPanelSlave;
import clientSlave.ClientSlave;

public class JoinGroup implements ActionListener {

	private ClientSlave _slave;
	private MainPanelSlave _pan;
	
	public JoinGroup (MainPanelSlave pan) {
		_pan   = pan;
		_slave = new ClientSlave();
	} // JoinGroup ()
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			_slave.receiveInvitation();
			// ComboList here... To do when we resolve invit for keypassword
			//slave.requestJoinGroup("toto", InetAddress.getByName("192.168.1.107"));
			_slave.linkNeighboor("192.168.0.15");
		} catch (Exception e1) {
			e1.printStackTrace();
		}	
	} // actionPerformed ()

} // JoinGroup
