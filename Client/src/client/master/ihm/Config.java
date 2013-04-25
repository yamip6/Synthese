package client.master.ihm;

import javax.crypto.NoSuchPaddingException;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

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
        lblUserName.setBounds(103, 43, 114, 14);
        add(lblUserName);
        
        JLabel lblGroupName = new JLabel("Group name:");
        lblGroupName.setBounds(103, 83, 114, 14);
        add(lblGroupName);
        
        JLabel lblUriServer = new JLabel("IP server:");
        lblUriServer.setBounds(103, 122, 114, 14);
        add(lblUriServer);
        
        JLabel lblPort = new JLabel("Port:");
        lblPort.setBounds(103, 159, 114, 14);
        add(lblPort);
        
        _username = new JTextField();
        _username.setBounds(214, 40, 106, 20);
        add(_username);
        _username.setColumns(10);
        
        _groupName = new JTextField();
        _groupName.setBounds(214, 80, 106, 20);
        add(_groupName);
        _groupName.setColumns(10);
        
        _ipServer = new JTextField();
        _ipServer.setBounds(214, 119, 106, 20);
        add(_ipServer);
        _ipServer.setColumns(10);
        
        _port = new JTextField();
        _port.setBounds(214, 156, 106, 20);
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
							} catch (IOException | InterruptedException | InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException | NoSuchPaddingException e) {
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
        btnLaunchServer.setBounds(160, 208, 114, 46);
        add(btnLaunchServer);   
        
    } // Config ()
    
} // Config
