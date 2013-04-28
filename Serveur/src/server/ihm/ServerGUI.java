package server.ihm;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import server.controller.Server;

@SuppressWarnings("serial")
public class ServerGUI extends JFrame {
	
    /** Graphic panel to configure the server (port) */
    protected static Config _config;
    /** Server */
    private static Server _server;
    
    /**
     * Constructor
     */
    public ServerGUI () {
        try { // Forcing the use of the system's look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        // It forces all components to redraw the window with the system's look and feel
        SwingUtilities.updateComponentTreeUI(this);
        
        _config = new Config();
        _config.setBounds(0, 0, 500, 350);
        _config.setVisible(true);
        add(_config);
        
        // Shared graphical components (of the window)
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu Menu = new JMenu("Menu");
        menuBar.add(Menu);
        
        JMenuItem mntmQuitter = new JMenuItem("Exit");
        mntmQuitter.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed (ActionEvent e) {
            	int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Stopping the application", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(option == JOptionPane.OK_OPTION) System.exit(0);
                
            } // actionPerformed ()
        });
        Menu.add(mntmQuitter);
        
        addWindowListener(new WindowAdapter() {
        	@Override
            public void windowClosing (WindowEvent e) {
            	int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Stopping the application", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(option == JOptionPane.OK_OPTION) System.exit(0);
                
            } // windowClosing ()
        });
        
    } // ServerGUI ()     
    
    /**
     * Accessor
     * @return The current server
     */
    public static Server get_server () {
		return _server;
		
	} // get_server ()

    /**
     * Modifier
     * @param server is the new server
     */
	public static void set_server (Server server) {
		_server = server;
		
	} // set_server ()

	/**
     * Method which permits to launch the server application
     * @param args
     */
    public static void main (String[] args) {
    	ServerGUI fr = new ServerGUI();
        fr.setTitle("Secured exchange group");
        fr.setSize(500, 350);
        fr.setLocationRelativeTo(null);
        fr.setResizable(false);
        fr.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        fr.setVisible(true);
        
    } // main ()
    
} // ServerGUI
