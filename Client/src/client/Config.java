package client;

import javax.swing.JPanel;

import java.awt.Color;
import javax.swing.JButton;

import client.master.ihm.MasterClientGUI;
import client.slave.ihm.SlaveClientGUI;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class Config extends JPanel {
	
    /**
     * Constructor
     */
    public Config () {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);
        
        JButton btnCreateDiscussionGroup = new JButton("Create discussion group");
        btnCreateDiscussionGroup.addActionListener(new ActionListener() {
        	public void actionPerformed (ActionEvent arg0) {
        		MasterClientGUI.main(null);
        		
        	} // actionPerformed ()
        });
        btnCreateDiscussionGroup.setBounds(152, 43, 168, 70);
        add(btnCreateDiscussionGroup);
        
        JButton btnJoinDiscussionGroup = new JButton("Join discussion group");
        btnJoinDiscussionGroup.addActionListener(new ActionListener() {
        	public void actionPerformed (ActionEvent e) {
        		SlaveClientGUI.main(null);
        		
        	} // actionPerformed ()
        });
        btnJoinDiscussionGroup.setBounds(152, 156, 168, 70);
        add(btnJoinDiscussionGroup);
        
    } // Config ()
    
} // Config
