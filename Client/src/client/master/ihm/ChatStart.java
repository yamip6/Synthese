package client.master.ihm;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

@SuppressWarnings("serial")
public class ChatStart extends JPanel {

	/**
     * Constructor
     */
    public ChatStart() {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);
        
        JLabel lblNewLabel = new JLabel("Liste des clients accept\u00E9s pour le groupe actualis\u00E9e");
        lblNewLabel.setBounds(112, 38, 297, 114);
        add(lblNewLabel);
        
        JButton btnLaunchServer = new JButton("Start chat");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Thread t = new Thread(new Runnable() {
        		    @Override
        		    public void run() {
        				try {
        					// On considère un seul serveur !
        					MasterClientGUI.get_master().set_loop(false);
        					MasterClientGUI.get_master().discussionGroupCreation();
        				} catch (IOException e) {
        					e.printStackTrace();
        				}	
        	        } // run()
        	    });
        		t.start();
        	} // actionPerformed()
        });
        btnLaunchServer.setBounds(174, 207, 114, 39);
        add(btnLaunchServer);             
            
    } // ChatStart()
    
} // ChatStart
