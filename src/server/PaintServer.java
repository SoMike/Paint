package server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import network.NetworkServer;

/**
 * Project's server class that holds all the graphical components
 * @author Somlea Mihai
 * @since 06.01.2019
 * */
public class PaintServer extends JFrame implements WindowListener {
	
	private static final long serialVersionUID = 3401392058990169109L;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
	
	public static final String TITLE = "Paint Server";
	private static PaintServer instance;
	
	private static final String newline = "\n";
	
	//creating the server network
	private NetworkServer server;
	
	
	private JTextArea console;
	private JList<String> listUsers;
	
	/**
	 * PaintServer constructor that initializes the instance of the server 
	 * and starts it
	 * */
	public PaintServer(){
		//initialize the server instance to be this for later reference
		instance = this;
		
		createView();
		
		server = new NetworkServer(this, 1501);
		server.startServer();
		
		setTitle(TITLE);
		setSize(700, 500);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}//end PaintServer constructor
	
	
	/**
	 * Method that creates the GUI of the server
	 * */
	private void createView() {
		  JPanel panel = new JPanel(new BorderLayout());
		  getContentPane().add(panel);
		  
		  console = new JTextArea();
		  console.setEditable(false);
		  ((DefaultCaret) console.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		  JScrollPane consoleSP = new JScrollPane(console);
		  consoleSP.setBorder(BorderFactory.createTitledBorder("Console Output"));
		  panel.add(consoleSP, BorderLayout.CENTER);
		  
		  listUsers = new JList<String>();
		  JScrollPane listUsersSP = new JScrollPane(listUsers);
		  listUsersSP.setPreferredSize(new Dimension(200, 500));
		  listUsersSP.setBorder(BorderFactory.createTitledBorder("Connected Users"));
		  panel.add(listUsersSP, BorderLayout.EAST);
	}//end createView method
	
	
	/**
	 * Method that updates the view of the connect users list on the server
	 * */
	public void updateView() {
		DefaultListModel<String> model = new DefaultListModel<>();
		for(String nickname : server.getConnectedClientMap().keySet()) {
			model.addElement(nickname);
		}//end for
		listUsers.setModel(model);
	}//end updateView method
	
	
	/**
	 * Method that returns the instance of the PaintServer class
	 * */
	public static PaintServer getInstance(){
		return instance;
	}//end getInstance method
	
	
	/**
	 * Method that logs a message to the console
	 *@param message The message that will be logged in the console
	 * */
	public void log(String message) {
		console.append(DATE_FORMAT.format(new Date()) + " " +  message + newline);
	}//end log method
	
	
	/**
	 * Main entry in the program
	 * */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				//UIManager.put("swing.boldMetal", Boolean.FALSE);
				instance = new PaintServer();
				instance.setVisible(true);
			}//end run
		});
	}//end main method

	//Window listener methods
	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		server.stopServer();
		System.exit(0);
	}//end windowsClosing

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}
	
}//end PaintServer class