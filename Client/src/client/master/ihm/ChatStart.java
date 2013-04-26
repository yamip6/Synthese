package client.master.ihm;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
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
        
        _modele = new TableModelMaster(new ArrayList<String>());
        _table = new JTable(_modele);      
        _table.setBounds(85, 42, 316, 144);
        _scrollPane = new JScrollPane(_table);
        _scrollPane.setBounds(77, 11, 316, 144);
        add(_scrollPane, BorderLayout.CENTER);      
        
        JButton btnLaunchServer = new JButton("Start chat");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed (ActionEvent e) {
        		Thread t = new Thread(new Runnable() {
        		    @Override
        		    public void run () {
        				MasterClientGUI.get_master().set_loop(false);
        				try {
							MasterClientGUI.get_master().discussionGroupCreation();
						} catch (IOException e) {
							e.printStackTrace();
						}
        				
        	        } // run ()
        	    });
        		t.start();
        		
        	} // actionPerformed ()
        });
        btnLaunchServer.setBounds(165, 183, 118, 59);
        add(btnLaunchServer);             
            
    } // ChatStart ()
    
    /**
     * Call it to update the JTable
     */
    public void refresh () {
		_modele = new TableModelMaster(MasterClientGUI.get_master().get_acceptedClients());
        _table.setModel(_modele);
        _modele.fireTableDataChanged();
        
    } // refresh ()
    
} // ChatStart
