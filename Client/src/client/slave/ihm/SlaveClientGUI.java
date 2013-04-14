package client.slave.ihm;

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

@SuppressWarnings("serial")
public class SlaveClientGUI extends JFrame {
    /** Graphic pannel of client configuration (and join group) */
    protected static Config _config;
    
    /**
     * Constructor
     */
    public SlaveClientGUI() {
        try { // Forcing the use of the system's look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.out.println("Erreur durant le chargement du look and feel.");
        }
        // It forces all components to redraw the window with the system's look and feel
        SwingUtilities.updateComponentTreeUI(this);
        
        _config = new Config();
        _config.setBounds(0, 0, 500, 350);
        _config.setVisible(true);
        getContentPane().add(_config);
        
        // Composants graphique communs (de la fenêtre)
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu Menu = new JMenu("Menu");
        menuBar.add(Menu);
        
        JMenuItem mntmQuitter = new JMenuItem("Exit");
        mntmQuitter.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) {
            	int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Stopping the application", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(option == JOptionPane.OK_OPTION) System.exit(0);
            } // actionPerformed()
        });
        Menu.add(mntmQuitter);
        
        addWindowListener(new WindowAdapter() {
        	@Override
            public void windowClosing(WindowEvent e) {
            	int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Stopping the application", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(option == JOptionPane.OK_OPTION) System.exit(0);
            } // windowClosing()
        });
        
    } // SlaveClientGUI() 
    
    /**
     * Method which permits to launch the client slave application
     * @param args
     */
    public static void main(String[] args) {
    	SlaveClientGUI fr = new SlaveClientGUI();
        fr.setTitle("Secured exchange group");
        fr.setSize(500, 350);
        fr.setLocationRelativeTo(null);
        fr.setResizable(false);
        fr.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        fr.setVisible(true);
        
    } // main()
    
} // SlaveClientGUI
