package client.ihm;

import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.FlowLayout;

import client.controller.JoinGroup;

public class MainPanelSlave extends JPanel {

	private static final long serialVersionUID = -918766493189830236L;
	
	private JButton _join;
	
	public MainPanelSlave () {
		_join = new JButton("Join a group");
		_join.addActionListener(new JoinGroup());
		setLayout(new FlowLayout());
		add(_join);
	} // MainPanelSlave ()

} // MainPanelSlave
