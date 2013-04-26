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
    public Config () {
        setFocusable(true);
        requestFocusInWindow(true);
        setBackground(Color.WHITE);
        setLayout(null);
        
        JLabel lblListeningPort = new JLabel("Listening port:");
        lblListeningPort.setBounds(109, 53, 107, 14);
        add(lblListeningPort);
        
        _port = new JTextField();
        _port.setText("50000");
        _port.setBounds(215, 50, 114, 20);
        add(_port);
        
        JButton btnLaunchServer = new JButton("Launch server");
        btnLaunchServer.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed (ActionEvent e) {
        		try {
        			ServerGUI.set_server(new Server(Integer.parseInt(_port.getText())));
        			ServerGUI.get_server().services();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
        		
        	} // actionPerformed ()
        });
        btnLaunchServer.setBounds(167, 138, 134, 78);
        add(btnLaunchServer); 
        
    } // Config ()
    
} // Config
