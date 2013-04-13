package server.ihm;

import javax.swing.JFrame;
import server.controller.Server;

public class ServerGUI extends JFrame{

	private static final long serialVersionUID = -9117629567222020899L;

	public ServerGUI () {
		setTitle("Launch Server");
		add(new MainPanel());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("SERVER : "); // DEBUG
		ServerGUI s = new ServerGUI();
		
	} // main ()
}
