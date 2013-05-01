package client.slave.ihm;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.HashMap;

@SuppressWarnings("serial")
public class JoinGroup extends JPanel {
	
	/** */
	private JTable _table;
	/** */
	private TableModel _modele;
	/** */
	private HashMap<String, String> _listGroups;
	/** */
	private JScrollPane scrollPane;
	
    /**
     * Constructor
     */
    public JoinGroup () {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);               
        
        _listGroups = new HashMap<String,String>();
        
        _modele = new TableModel(_listGroups, "");
        _table = new JTable(_modele);
        _table.setBounds(85, 42, 316, 144);
        scrollPane = new JScrollPane(_table);
        scrollPane.setBounds(29, 42, 411, 144);
        add(scrollPane, BorderLayout.CENTER);      
        
        JButton btnLaunchServer = new JButton("Join group");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed (ActionEvent e) {
        		try {
        			// Getting the selected row
        			int rowSelectionned = _table.getSelectedRow();
        			String ip = (String) _table.getValueAt(rowSelectionned, 0);
        			String grp = (String) _table.getValueAt(rowSelectionned, 1);
   
        			SlaveClientGUI.get_slave().set_loop(false);
        			SlaveClientGUI.get_slave().requestJoinGroup(grp, ip); // devrait rendre un booleen avec raison echec !!!
        			
        			SlaveClientGUI.get_slave().linkNeighboor();
        			
        			SlaveClientGUI._jgroup.setVisible(false);
        			SlaveClientGUI._chat.setVisible(true);
        			Thread t = new Thread(new Runnable() {
        			    @Override
        			    public void run () {
        					try {
        						SlaveClientGUI.get_slave().doDiffieHellman();
        						SlaveClientGUI.get_slave().transmitMessage();
        					} catch (Exception e) {
        						e.printStackTrace();
        					}
        					
        		        } // run ()
        		    });
        			t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
        		
        	} // actionPerformed ()
        });
        btnLaunchServer.setBounds(170, 205, 125, 56);
        add(btnLaunchServer);           
        
    } // JoinGroup ()    
    
    /**
     * Call it to update JTable
     */
    public void refresh (String certificate) {
		_modele = new TableModel(SlaveClientGUI.get_slave().get_listGroups(), certificate);
        _table.setModel(_modele);
        _modele.fireTableDataChanged();
        
    } // refresh ()
    
} // JoinGroup
