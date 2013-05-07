package client.slave.ihm;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import utils.Utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Chat extends JPanel {
	
	private JTextArea _fieldChat;
	@SuppressWarnings("unused")
	private ArrayList<String> _listParticip;
    private JTable _participants;
    private ModelListAttendants _modele;
	
	private class East extends JPanel {
		
		public East (){
			_modele       = new ModelListAttendants();
			_participants = new JTable(_modele);
			JScrollPane scrollPane = new JScrollPane(_participants);
			scrollPane.setPreferredSize(new Dimension(80, 260));
			add(scrollPane);
		} // East ()
		
	} // East
	
	private class South extends JPanel {
		private JButton send;
		private JTextField fieldForm;
		
		
		public South () {
			send = new JButton("Send message");
			fieldForm = new JTextField(32);
			setLayout(new FlowLayout());
			add(fieldForm); 
			add(send);
			
			send.addActionListener(new ActionListener() {
	        	@Override
	        	public void actionPerformed (ActionEvent e) {
	        		try {
	        			SlaveClientGUI.get_slave().sendMessage(fieldForm.getText());
	        			if(_fieldChat.getText().contentEquals(""))
						    _fieldChat.setText(InetAddress.getLocalHost().getHostAddress() + " at " + Utils.getDate() + " : " + fieldForm.getText());
	        			else
						    _fieldChat.setText(_fieldChat.getText() + "\n" + SlaveClientGUI.get_slave().get_username() + " at " + Utils.getDate() + " : " + fieldForm.getText());
						fieldForm.setText("");
					} catch (Exception e1) {
						e1.printStackTrace();
					}
	        	}
			});
		} // South ()
		
	} // South
	
	public Chat () {
		setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);
        
		_fieldChat = new JTextArea(10,32);
		_fieldChat.setEditable(false);
		setLayout(new BorderLayout());
        add(new JScrollPane(_fieldChat), BorderLayout.CENTER);
        add(new South(), BorderLayout.SOUTH);
        add(new East(), BorderLayout.EAST); 
        
	} // Chat ()
	
	public JTextArea get_fieldChat() {
		return _fieldChat;
	}

	public void set_listParticip(ArrayList<String> _listParticip) {
		this._listParticip = _listParticip;
		_modele = new ModelListAttendants();
		_modele.set_members(_listParticip);
		_participants.setModel(_modele);
        _modele.fireTableDataChanged();
	}
	
} // Chat
