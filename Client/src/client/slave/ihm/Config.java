package client.slave.ihm;

import javax.crypto.NoSuchPaddingException;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import client.slave.SlaveClient;

@SuppressWarnings("serial")
public class Config extends JPanel {
	/** User name of the client */
	private JTextField _username;
	private JTextField _pseudo;
	
    /**
     * Constructor
     */
    public Config () {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);               
        
        JLabel lblUserName = new JLabel("User name:");
        lblUserName.setBounds(127, 61, 84, 14);
        add(lblUserName);
        
        JLabel lblPseudo = new JLabel("Pseudo:");
        lblPseudo.setBounds(127, 93, 84, 14);
        add(lblPseudo);
        
        _username = new JTextField();
        _username.setBounds(217, 58, 106, 20);
        add(_username);
        _username.setColumns(10);
        
        _pseudo = new JTextField();
        _pseudo.setBounds(217, 90, 106, 20);
        add(_pseudo);
        _pseudo.setColumns(10);
        
        JButton btnLaunchServer = new JButton("Start");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed (ActionEvent e) {
        		try {
        			SlaveClientGUI.set_slave(new SlaveClient(_username.getText(), _pseudo.getText()));
        		    Thread t = new Thread(new Runnable() {
        				@Override
        				public void run () {
        					try {
        						SlaveClientGUI.get_slave().receiveInvitation();
        					} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | SignatureException e) {
        						e.printStackTrace();
        					}
        					
        				} // run ()
        			});
        		    t.start();
        		    
        		    SlaveClientGUI._config.setVisible(false);
        		    SlaveClientGUI._jgroup.setVisible(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
        		
        	} // actionPerformed()
        });
        btnLaunchServer.setBounds(171, 131, 122, 71);
        add(btnLaunchServer);                 
        
    } // Config ()    
    
} // Config
