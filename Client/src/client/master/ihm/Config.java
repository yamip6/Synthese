package client.master.ihm;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

import client.master.MasterClient;

@SuppressWarnings("serial")
public class Config extends JPanel {
	/** User name of the client bis */
	private JTextField _username;
	/** The name of the created group */
	private JTextField _groupName;
	/** IP server */
	private JTextField _ipServer;
	/** Connection port of the server */
	private JTextField _port;
	
    /**
     * Constructor
     */
    public Config () {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);               
        
        JLabel lblUserName = new JLabel("User name:");
        lblUserName.setBounds(109, 42, 114, 14);
        add(lblUserName);
        
        JLabel lblGroupName = new JLabel("Group name:");
        lblGroupName.setBounds(109, 82, 114, 14);
        add(lblGroupName);
        
        JLabel lblUriServer = new JLabel("IP server:");
        lblUriServer.setBounds(109, 121, 114, 14);
        add(lblUriServer);
        
        JLabel lblPort = new JLabel("Port:");
        lblPort.setBounds(109, 158, 114, 14);
        add(lblPort);
        
        _username = new JTextField();
        _username.setBounds(220, 39, 106, 20);
        add(_username);
        _username.setColumns(10);
        
        _groupName = new JTextField();
        _groupName.setBounds(220, 79, 106, 20);
        add(_groupName);
        _groupName.setColumns(10);
        
        _ipServer = new JTextField();
        _ipServer.setBounds(220, 118, 106, 20);
        add(_ipServer);
        _ipServer.setColumns(10);
        
        _port = new JTextField();
        _port.setText("50000");
        _port.setBounds(220, 155, 106, 20);
        add(_port);
        _port.setColumns(10);
        
        JButton btnLaunchServer = new JButton("Create group");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed (ActionEvent e) {
        		try {
        			MasterClientGUI.set_master(new MasterClient(_ipServer.getText(), Integer.parseInt(_port.getText()), _username.getText()));
        			MasterClientGUI.get_master().requestGroupCreation(_groupName.getText());
        			MasterClientGUI.get_master().responseGroupCreation();
					Thread t = new Thread(new Runnable() {
						@Override
						public void run () {
							try {
								MasterClientGUI.get_master().invitation(_groupName.getText());
								MasterClientGUI.get_master().receiveClient();
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						} // run ()
					});
					t.start();
					
					MasterClientGUI._config.setVisible(false);
					MasterClientGUI._chat.setVisible(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
        		
        	} // actionPerformed ()
        });
        btnLaunchServer.setBounds(166, 207, 114, 46);
        add(btnLaunchServer);   
        
    } // Config ()
    
} // Config
