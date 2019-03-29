package network;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import client.PaintClient;
import packet.Packet;


/**
 * Class that handles the networking of the client
 * */
public class NetworkClient {

	private Socket socket;
	private String ipAdress;
	private int serverPort;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String nickname;
	private Packet packet;
	private PaintClient instance;
	
	/**
	 * NetworkClient class constructor that initializes the ipAdress and the 
	 * server port 
	 * @param ipAdress The IpAdress of the client
	 * @param serverPort The port for the server
	 * @param paintClient The PaintClient instance of that client that will be modified
	 * */
	public NetworkClient(String ipAdress, int serverPort, PaintClient paintClient) {
		this.ipAdress = ipAdress;
		this.serverPort = serverPort;
		this.instance = paintClient;
	}//end NetworkClient class constructor
	
	
	/**
	 * Method that realizes the connection to the server
	 * */
	public void connectToServer() {
		try {
			System.out.println("Attempting to connect to the server!!");
			socket = new Socket(ipAdress, serverPort);
			System.out.println("Client " + socket.getRemoteSocketAddress() + " connected to the server!");
			input = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
		}catch(UnknownHostException he) {
			he.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
			return;
		}//end try catch statement
		
		nickname = JOptionPane.showInputDialog("Enter your nickname:");
		System.out.println("User " + nickname + " connected!");
		
		try {
			sendPacket(new Packet(nickname));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//end try catch statement
		
		Thread packagesHandlerT = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					processConnection();
				}//end while
			}//end run method
		});
		packagesHandlerT.start();
	}//end connectToServer method
	
	
	/**
	 * Method that closes the connection with the server by closing
	 * the socket and the in/out streams
	 * */
	public void closeConnection() {
		try {
			input.close();
			output.close();
			socket.close();
		}catch(IOException e) {
			e.printStackTrace();
		}//end try catch 
	}//end closeConnection method
	
	/**
	 * Method that initializes the input/output streams of the client
	 * */
	private void getStreams() throws IOException {
		output = new ObjectOutputStream(socket.getOutputStream());
		output.flush();
		input = new ObjectInputStream(socket.getInputStream());
	}//end getStreams method
	
	/**
	 * Method that extracts the information from the packet received and puts it on the canvas
	 * */
	private void processConnection() {
		try {
			packet = (Packet) input.readObject();
			int width = packet.getImageWidth();
			int height = packet.getImageHeight();
			int[][] pixels = packet.getImage();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			for(int x = 0; x < width; x++) {
				for(int y = 0; y < height; y++) {
					image.setRGB(x, y, pixels[x][y]);
				}//end for
			}//end for
			ImageIO.write(image, "jpg", new File("test.jpg"));
			Graphics g = instance.drawingP.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 400, 300);
			g.dispose();
//			instance.drawingP.setBackBuffer(image);
//			instance.drawingP.repaint();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}//end try catch statement
	}//end processConnection method
	
	/**
	 * Method that sends the packet from the client to the server
	 * @param packet The packet object that will be sent to the server
	 * @throws IOException 
	 * */
	public void sendPacket(Packet packet) throws IOException {
		//ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		output.writeObject(packet);
	}//end sendPacket method
	
}//end NetworkClient class
