package client.slave.ihm;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.BorderLayout;
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
        
        JTable table = new JTable(new TableModel()); // Pour Yassine
        table.setBounds(85, 42, 316, 144);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton btnLaunchServer = new JButton("Join group");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		System.out.println("PASCAL");
        		try {
        			// MAJ de la JTable dans config
        			// _slave.requestJoinGroup(/*récupérer le 1er element de la ligne sélectionnée (grp)*/, /*récupérer le 2e element de la ligne sélectionnée (ip)*/); // devrait rendre un booleen avec raison echec
        			SlaveClientGUI.get_slave().requestJoinGroup("toto", "192.168.1.110"); // Pas sûr que le groupe serve a quelque chose ici
        			if(false)SlaveClientGUI.get_slave().linkNeighboor("192.168.1.110"); //_slave.get_listGroups().get("ligne sélectionnée");
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
        	} // actionPerformed()
        });
        btnLaunchServer.setBounds(160, 208, 114, 39);
        add(btnLaunchServer);           
        
    } // JoinGroup()    
    
} // JoinGroup
