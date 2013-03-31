package client.ihm;

import javax.swing.JFrame;

public class ClientSlaveGUI extends JFrame {

	private static final long serialVersionUID = -255541101523045086L;
	
	private MainPanelSlave _clientSlaveMainPanel;

	public ClientSlaveGUI () {
		
		_clientSlaveMainPanel = new MainPanelSlave();
		add(_clientSlaveMainPanel);
		setTitle("Participant discussion's group");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
		
	}
	
	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		ClientSlaveGUI g = new ClientSlaveGUI();

	}

}
