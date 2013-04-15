package server.ihm;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import server.controller.Server;

@SuppressWarnings("serial")
public class Config extends JPanel {
	
	/** Listening port of the server */
	private JTextField _port;
    
    /**
     * Constructor
     */
    public Config() {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);
        
        JLabel lblListeningPort = new JLabel("Listening port:");
        lblListeningPort.setBounds(101, 103, 85, 14);
        add(lblListeningPort);
        
        _port = new JTextField();
        _port.setBounds(207, 100, 114, 20);
        add(_port);
        _port.setColumns(10);
        
        JButton btnLaunchServer = new JButton("Launch server");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		try {
        			Server s = new Server(Integer.parseInt(_port.getText()));
					s.service();
					s.disconnection();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
        	} // actionPerformed()
        });
        btnLaunchServer.setBounds(171, 153, 114, 39);
        add(btnLaunchServer); 
        
    } // Config()
    
} // Config