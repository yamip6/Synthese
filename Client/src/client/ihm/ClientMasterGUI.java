package client.ihm;

import javax.swing.JFrame;

public class ClientMasterGUI extends JFrame {

	private static final long serialVersionUID = -231437988150241694L;
	
	private MainPanel _clientMasterMainPanel;
	
	public ClientMasterGUI () {	
		_clientMasterMainPanel = new MainPanel();
		add(_clientMasterMainPanel);
		setTitle("Creator discussion's group");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	} // ClientMasterGUI ()

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		ClientMasterGUI c = new ClientMasterGUI();
	} // main()

} // ClientMasterGUI
