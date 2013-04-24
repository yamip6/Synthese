package chat.ihm;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Chat extends JFrame {

	public Chat () {
		add(new Pannel());
		setTitle("Discussion");
		pack();
	}
	
	public static void main(String[] args) {
		Chat c = new Chat();
		c.setVisible(true);
	}

}
