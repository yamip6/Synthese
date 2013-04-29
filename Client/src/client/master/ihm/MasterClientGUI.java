package client.master.ihm;

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

import client.master.MasterClient;

@SuppressWarnings("serial")
public class MasterClientGUI extends JFrame {
	
    /** Graphic panel of configuration clientBis/server */
    protected static Config _config;
    /** Graphic panel of starting discussion with other clients */
    protected static ChatStart _start;
    /** Graphic panel of discussion with other clients */
    protected static Chat _chat;
    
    /** The master client */
	private static MasterClient _master;

	/**
     * Constructor
     */
    public MasterClientGUI () {
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
        
        _start = new ChatStart();
        _start.setBounds(0, 0, 500, 350);
        _start.setVisible(false);
        add(_start);
        
        _chat = new Chat();
        _chat.setBounds(0, 0, 500, 350);
        _chat.setVisible(false);
        add(_chat);
        
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
        
    } // MasterClientGUI () 
    
    /**
     * 
     * @return
     */
    public static MasterClient get_master () {
		return _master;
		
	} // get_master ()

    /**
     * 
     * @param master
     */
	public static void set_master (MasterClient master) {
	    _master = master;
	    
	} // set_master ()
	
	/**
	 * 
	 * @return
	 */
    public static ChatStart get_start () {
		return _start;
		
	} // get_start ()

	public static Chat get_chat() {
		return _chat;
	}

	/**
     * Method which permits to launch the client bis application
     * @param args
     */
    public static void main (String[] args) {
    	MasterClientGUI fr = new MasterClientGUI();
        fr.setTitle("Secured exchange group");
        fr.setSize(500, 350);
        fr.setLocationRelativeTo(null);
        fr.setResizable(false);
        fr.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        fr.setVisible(true);
        
    } // main ()
    
} // MasterClientGUI
