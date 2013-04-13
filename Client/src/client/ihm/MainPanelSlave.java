package client.ihm;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.FlowLayout;

import client.controller.JoinGroup;

public class MainPanelSlave extends JPanel {

	private static final long serialVersionUID = -918766493189830236L;
	
	private JLabel  _label;
	private JTextField _username;
	private JButton _join;
	
	public MainPanelSlave () {
		_label = new JLabel("Username : ");
		_join = new JButton("Join a group");
		_join.addActionListener(new JoinGroup(this));
		setLayout(new FlowLayout());
		add(_join);
	} // MainPanelSlave ()

} // MainPanelSlave
