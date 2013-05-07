package client.master.ihm;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import client.slave.ihm.SlaveClientGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ChatStart extends JPanel {

	private TableModelMaster _modele;
	private JTable _table;
	private JScrollPane _scrollPane;
	
	/**
     * Constructor
     */
    public ChatStart () {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);
        
        _modele = new TableModelMaster(new ArrayList<String>(), "");
        _table = new JTable(_modele);      
        _table.setBounds(85, 42, 316, 144);
        _scrollPane = new JScrollPane(_table);
        _scrollPane.setBounds(43, 11, 374, 144);
        add(_scrollPane, BorderLayout.CENTER);      
        
        JButton btnLaunchServer = new JButton("Start chat");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed (ActionEvent e) {
        		Thread t = new Thread(new Runnable() {
        		    @Override
        		    public void run () {
        				try {
        					MasterClientGUI.get_master().set_loop(false);
							MasterClientGUI.get_master().discussionGroupCreation();
							MasterClientGUI.get_chat().set_listParticip(MasterClientGUI.get_master().get_acceptedClients());
							MasterClientGUI.get_master().doDiffieHellman();
							MasterClientGUI.get_master().transmitMessage();
						} catch (Exception e) {
							e.printStackTrace();
						}
        				
        	        } // run ()
        	    });
        		t.start();
        		
        		MasterClientGUI._start.setVisible(false);
				MasterClientGUI._chat.setVisible(true);
				
        	} // actionPerformed ()
        });
        btnLaunchServer.setBounds(165, 183, 118, 59);
        add(btnLaunchServer);             
            
    } // ChatStart ()
    
    /**
     * Call it to update the JTable
     */
    public void refresh (String certificate) {
		_modele = new TableModelMaster(MasterClientGUI.get_master().get_acceptedClients(), certificate);
        _table.setModel(_modele);
        _modele.fireTableDataChanged();
        
    } // refresh ()
    
} // ChatStart
