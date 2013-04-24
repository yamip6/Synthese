package client.master.ihm;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import client.slave.ihm.SlaveClientGUI;
import client.slave.ihm.TableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

@SuppressWarnings("serial")
public class ChatStart extends JPanel {

	private TableModelMaster _modele;
	private JTable			 _table;
	private JScrollPane      _scrollPane;
	
	/**
     * Constructor
     */
    public ChatStart () {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);
        
       /* JLabel lblNewLabel = new JLabel("Liste des clients accept\u00E9s pour le groupe actualis\u00E9e");
        lblNewLabel.setBounds(112, 38, 297, 114);
        add(lblNewLabel);*/
        
        _modele = new TableModelMaster(MasterClientGUI.get_master().get_acceptedClients());
        _table = new JTable(_modele); // Pour Yassine
        
        _table.setBounds(85, 42, 316, 144);
        _table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _scrollPane = new JScrollPane(_table);
        _scrollPane.setBounds(85, 42, 316, 144);
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
        btnLaunchServer.setBounds(161, 171, 118, 59);
        add(btnLaunchServer);             
            
    } // ChatStart ()
    
    /**
     * Call it to update JTable
     */
    public void refresh () {
		_modele = new TableModelMaster(MasterClientGUI.get_master().get_acceptedClients());
        _table.setModel(_modele);
        _modele.fireTableDataChanged();
    } // refresh ()
    
} // ChatStart
