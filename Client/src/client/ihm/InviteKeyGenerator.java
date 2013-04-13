package client.ihm;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import utils.KeyGeneratorN;

public class InviteKeyGenerator extends JFrame {

	private static final long serialVersionUID = 268874480756471155L;

	private JLabel 		_question;
	private JTextField 	_key;
	private JButton		_ok;
	
	private String		_privateKey;
	private KeyGeneratorN _inst;
	
	public InviteKeyGenerator () {
		
		setTitle("Invite Key Generator");
		_ok = new JButton("OK");
		_question = new JLabel("Enter a pass to protect your private key : ");
		_key = new JTextField("");
		_ok.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				_privateKey = _key.getText();
				_inst = new KeyGeneratorN(_privateKey);
				_inst.set_key(_privateKey);
			}
		});
		setLayout(new GridLayout(1, 3));
		add(_question);
		add(_key);
		add(_ok);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	} // InviteKeyGenerator ()

	public String get_privateKey() {
		return _privateKey;
	}

	public static void main(String[] args) {
		
	InviteKeyGenerator k = new InviteKeyGenerator();
	try {
		Thread.sleep(20000); // 20 sec pr une paraphrase
		System.out.println(k.get_privateKey());
	} catch (InterruptedException e) {
		e.printStackTrace();
	} // 10 sec
	

		/*while (m.get_key() == null);
		System.out.println(m.get_key());*/
	}
}
