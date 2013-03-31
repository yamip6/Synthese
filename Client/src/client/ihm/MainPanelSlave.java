package client.ihm;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import client.controller.JoinGroup;

public class MainPanelSlave extends JPanel {

	private static final long serialVersionUID = -918766493189830236L;
	
	private JButton _join;
	
	public MainPanelSlave () {
		_join = new JButton("Join a group");
		_join.addActionListener(new JoinGroup());
		setLayout(new FlowLayout());
		add(_join);
	}

}
