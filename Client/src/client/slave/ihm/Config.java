package client.slave.ihm;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import client.slave.SlaveClient;

@SuppressWarnings("serial")
public class Config extends JPanel {
	/** User name of the client */
	private JTextField _username;
	/** Slave client */
	private SlaveClient _slave;
	
    /**
     * Constructor
     */
    public Config() {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);               
        
        JLabel lblUserName = new JLabel("User name:");
        lblUserName.setBounds(117, 14, 84, 14);
        add(lblUserName);
        
        _username = new JTextField();
        _username.setBounds(228, 11, 106, 20);
        add(_username);
        _username.setColumns(10);
        
        JLabel lblNewLabel = new JLabel("Liste des groupes recus (et de leur cr\u00E9ateur) dans une JTABLE");
        lblNewLabel.setBounds(85, 42, 316, 144);
        add(lblNewLabel);
        
        JButton btnLaunchServer = new JButton("Join group");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		try {
        			_slave = new SlaveClient(_username.getText());
        			_slave.receiveInvitation(); // requestjoingroup ????
        			_slave.linkNeighboor("192.168.0.15"); // L'ip ne doit pas etre entré en dur !!!!!!
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
        	} // actionPerformed()
        });
        btnLaunchServer.setBounds(160, 208, 114, 39);
        add(btnLaunchServer);           
        
    } // Config()    
    
} // Config
