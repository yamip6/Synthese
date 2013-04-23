package client.slave.ihm;


import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("serial")
public class JoinGroup extends JPanel {
	
	private JTable _table;
	private TableModel _modele;
	private HashMap<String, String> _listGroups;
	
    /**
     * Constructor
     */
    public JoinGroup () {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);               
        
        _listGroups = SlaveClientGUI.get_slave().get_listGroups();
        _modele = new TableModel(_listGroups);
        _table = new JTable(_modele); // Pour Yassine
        
        _table.setBounds(85, 42, 316, 144);
        _table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(new JScrollPane(_table), BorderLayout.CENTER);
        
        
        JButton btnLaunchServer = new JButton("Join group");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed (ActionEvent e) {
        		try {
        			// MAJ de la JTable dans config
        			int rowSelectionned = _table.getSelectedRow();
        			String ip = (String) _table.getValueAt(rowSelectionned, 0);
        			String grp = (String) _table.getValueAt(rowSelectionned, 1);
        			// _slave.requestJoinGroup(/*récupérer le 1er element de la ligne sélectionnée (grp)*/, /*récupérer le 2e element de la ligne sélectionnée (ip)*/); // devrait rendre un booleen avec raison echec
        			SlaveClientGUI.get_slave().requestJoinGroup(grp, "192.168.56.1"); // Pas sûr que le groupe serve a quelque chose ici, je laisse l'ip pr les tests
        			SlaveClientGUI.get_slave().linkNeighboor("192.168.56.1"); //_slave.get_listGroups().get("ligne sélectionnée");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
        		
        	} // actionPerformed ()
        });
        btnLaunchServer.setBounds(160, 208, 114, 39);
        add(btnLaunchServer);           
        
    } // JoinGroup ()    
    
    /**
     * Call it to update JTable
     */
    public void refresh() {
		_modele = new TableModel(SlaveClientGUI.get_slave().get_listGroups());
        _table.setModel(_modele);
        _modele.fireTableDataChanged();
    } // refresh()
    
} // JoinGroup
