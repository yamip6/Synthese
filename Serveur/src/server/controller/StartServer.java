package server.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import server.ihm.MainPanel;

public class StartServer implements ActionListener{

	private MainPanel _mainPanel;
	private int       _port;
	
	public StartServer (MainPanel m) {
		_mainPanel = m;
		_port = -1; 
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		_port = Integer.parseInt(_mainPanel.get_fieldPort().getText());
		Server s = new Server(_port);
		try {
			s.service();
			s.disconnection();
		} catch (Exception g) {
			g.printStackTrace();
		}
		
	}

}
