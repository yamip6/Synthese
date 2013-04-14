package client.master.ihm;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

import java.io.IOException;

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
	
	/** The master client */
	private MasterClient _master;
	
    /**
     * Constructor
     */
    public Config() {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);               
        
        JLabel lblUserName = new JLabel("User name:");
        lblUserName.setBounds(96, 43, 84, 14);
        add(lblUserName);
        
        JLabel lblGroupName = new JLabel("Group name:");
        lblGroupName.setBounds(96, 83, 84, 14);
        add(lblGroupName);
        
        JLabel lblUriServer = new JLabel("IP server:");
        lblUriServer.setBounds(96, 122, 84, 14);
        add(lblUriServer);
        
        JLabel lblPort = new JLabel("Port:");
        lblPort.setBounds(96, 159, 84, 14);
        add(lblPort);
        
        _username = new JTextField();
        _username.setBounds(207, 40, 106, 20);
        add(_username);
        _username.setColumns(10);
        
        _groupName = new JTextField();
        _groupName.setBounds(207, 80, 106, 20);
        add(_groupName);
        _groupName.setColumns(10);
        
        _ipServer = new JTextField();
        _ipServer.setBounds(207, 119, 106, 20);
        add(_ipServer);
        _ipServer.setColumns(10);
        
        _port = new JTextField();
        _port.setBounds(207, 156, 106, 20);
        add(_port);
        _port.setColumns(10);
        
        JButton btnLaunchServer = new JButton("Create group");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		try {
        			_master = new MasterClient(_ipServer.getText(), Integer.parseInt(_port.getText()), _username.getText());
					_master.requestGroupCreation(_groupName.getText());
					_master.responseGroupCreation();
					invitation();
					
					ClientMasterGUI._config.setVisible(false);
					ClientMasterGUI._chat.setVisible(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
        	} // actionPerformed()
        });
        btnLaunchServer.setBounds(160, 208, 114, 39);
        add(btnLaunchServer);   
        
    } // Config()
    
    private void invitation() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					_master.Invitation(_groupName.getText());
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			} // run()
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				JFrame f = new JFrame(); JButton start = new JButton("Start discussion");
				start.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						_master.set_start(true);
						try {
							// On considère un seul serveur !
							_master.discussionGroupCreation();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				f.add(start); f.pack(); f.setVisible(true);			
			} // run()
		});
		t.start();
		t2.start();
		
	} // invitation ()
    
} // Config
