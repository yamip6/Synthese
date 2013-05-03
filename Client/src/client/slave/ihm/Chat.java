package client.slave.ihm;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class Chat extends JPanel {
	
	private JTextArea _fieldChat;
	// Je fais le choix de ne pas mettre de widget affichant la liste des participants pour le moment. Néanmoins je l'ai préparé :
	// private JTable   _participants;
	
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
						    _fieldChat.setText(InetAddress.getLocalHost().getHostAddress() + ": " + fieldForm.getText());
	        			else
						    _fieldChat.setText(_fieldChat.getText() + "\n" + InetAddress.getLocalHost().getHostAddress() + ": " + fieldForm.getText());
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
        
		// _participants = new JTable(lemodele);
		_fieldChat = new JTextArea(10,32);
		_fieldChat.setEditable(false);
		setLayout(new BorderLayout());
        add(new JScrollPane(_fieldChat), BorderLayout.CENTER);
        add(new South(), BorderLayout.SOUTH);
		// est placé à droite (EAST) la liste des participants     
        
	} // Chat ()
	
	public JTextArea get_fieldChat() {
		return _fieldChat;
	}
	
} // Chat
