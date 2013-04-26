package chat.ihm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Pannel extends JPanel {
	private JTextArea   _fieldChat;
	// Je fais le choix de ne pas mettre de widget affichant la liste des participants pour le moment.
	// Néanmoins je l'ai préparé :
	// private JTable   _participants;
	
	private class South extends JPanel {
		private JButton 	 _send;
		private JTextField   _fieldForm;
		
		public South (){
			_send = new JButton("Send message");
			_fieldForm = new JTextField(32);
			setLayout(new FlowLayout());
			add(_fieldForm); add(_send);
		}
	}
	
	public Pannel () {
		// _participants = new JTable(lemodele);
		_fieldChat = new JTextArea(10,32);
		_fieldChat.setEditable(false);
		setLayout(new BorderLayout());
        add(new JScrollPane(_fieldChat), BorderLayout.CENTER);
        add(new South(), BorderLayout.SOUTH);
		// est placé à droite (EAST) la liste des participants
	}
}
