package client.slave.ihm;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTable;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class JoinGroup extends JPanel {
	
    /**
     * Constructor
     */
    public JoinGroup() {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);               
        
        JTable table = new JTable(new TableModel());
        table.setBounds(85, 42, 316, 144);
        add(table);
        
        JButton btnLaunchServer = new JButton("Join group");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		try {
        			// _slave.requestJoinGroup(grp, ipClientBis); // devrait rendre un booleen avec raison echec
        			if(false)SlaveClientGUI.get_slave().linkNeighboor("192.168.1.25"); //_slave.get_listGroups().get("ligne sélectionnée");
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
        	} // actionPerformed()
        });
        btnLaunchServer.setBounds(160, 208, 114, 39);
        add(btnLaunchServer);           
        
    } // JoinGroup()    
    
} // JoinGroup
