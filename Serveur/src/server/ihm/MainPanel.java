package server.ihm;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import server.controller.StartServer;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = -8975331919799183825L;

	private JLabel  	_labelPort;
	private JTextField 	_fieldPort;
	private JButton 	_ok;
	
	
	public MainPanel () {
		_ok = new JButton("OK");
		_labelPort = new JLabel("Port listening : ");
		_fieldPort = new JTextField("");
		_ok.addActionListener(new StartServer(this));
		
		setLayout(new GridLayout(1, 3));

		add(_labelPort);
		add(_fieldPort);
		add(_ok);
	} // MainPanel()


	public JLabel get_labelPort() {
		return _labelPort;
	}


	public void set_labelPort(JLabel _labelPort) {
		this._labelPort = _labelPort;
	}


	public JTextField get_fieldPort() {
		return _fieldPort;
	}


	public void set_fieldPort(JTextField _fieldPort) {
		this._fieldPort = _fieldPort;
	}


	public JButton get_ok() {
		return _ok;
	}


	public void set_ok(JButton _ok) {
		this._ok = _ok;
	}
	
	
	
}
