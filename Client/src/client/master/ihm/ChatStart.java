package client.master.ihm;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
        	public void actionPerformed(ActionEvent e) {

        	}
        });
        btnLaunchServer.setBounds(174, 207, 114, 39);
        add(btnLaunchServer);             
            
    } // Config()
    
} // Config
