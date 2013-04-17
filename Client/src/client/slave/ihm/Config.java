package client.slave.ihm;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

import client.slave.SlaveClient;

@SuppressWarnings("serial")
public class Config extends JPanel {
	/** User name of the client */
	private JTextField _username;
	
    /**
     * Constructor
     */
    public Config() {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);               
        
        JLabel lblUserName = new JLabel("User name:");
        lblUserName.setBounds(119, 62, 84, 14);
        add(lblUserName);
        
        _username = new JTextField();
        _username.setBounds(209, 59, 106, 20);
        add(_username);
        _username.setColumns(10);
        
        JButton btnLaunchServer = new JButton("Start");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		try {
        			SlaveClientGUI.set_slave(new SlaveClient(_username.getText()));
        		    Thread t = new Thread(new Runnable() {
        				@Override
        				public void run() {
        					try {
        						SlaveClientGUI.get_slave().receiveInvitation();
        						// MAJ du modèle de la JTable de JoinGroup et refresh
        					} catch (IOException e) {
        						e.printStackTrace();
        					}
        				} // run()
        			});
        		    t.start();
        		    
        		    // Mettre le code de JoinGroup dans une nouvelle JFRAME
        		    
        		    SlaveClientGUI._config.setVisible(false);
        		    SlaveClientGUI._jgroup.setVisible(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
        	} // actionPerformed()
        });
        btnLaunchServer.setBounds(162, 163, 114, 39);
        add(btnLaunchServer);                 
        
    } // Config()    
    
} // Config
