package network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import packet.Packet;
import server.PaintServer;

/**
 * Class that handles the networking of the server
 * */
public class NetworkServer{

	private ServerSocket socket;
	private int port;
	private boolean running = false;
	
	private PaintServer paintServer;
	
	/**
	 * Map with all the user names of the clients connected to the server
	 * */
	private Map<String, Socket> connectedClientMap = new HashMap<>();
	/**
	 * Map with the canvas of all the clients connected to the server
	 * */
	private Map<int[][], Socket> clientImagesMap = new HashMap<>();
	
	/**
	 * Constructor for the NetworkServer class that sets the port and the 
	 * instance of the paint server
	 * */
	public NetworkServer(PaintServer paintServer, int port) {
		this.port = port;
		this.paintServer = paintServer;
	}//end NetworkServer constructor
	
	/**
	 * Method that starts the server by implementing the server socket and opens
	 * the connection 
	 * */
	public void startServer() {
		try {
			socket = new ServerSocket(port);
			//log to the console what port is the socket initialized on
			PaintServer.getInstance().log("Server socket initialized on port " + port);
			running = true;
			//new thread that starts the server
			Thread serverStart = new Thread(new Runnable() {
				@Override
				public void run() {
					PaintServer.getInstance().log("Listening for clients!!");
					
					while(running) {
						try {
							Socket s = socket.accept();
							ClientListener cl = new ClientListener(s);
							
							Thread t = new Thread(cl);
							t.start();
						}catch(IOException ex) {
							ex.printStackTrace();
						}//end try catch statement
					}//end while
				}//end run
			});
			serverStart.start();
		}catch(IOException e) {
			e.printStackTrace();
		}//end try catch
	}//end startServer method
	
	/**
	 * Method that stops the server and closes the socket
	 * */
	public void stopServer() {
		try {
			socket.close();
			running = false;
		}catch(IOException e) {
			e.printStackTrace();
		}//end try catch
	}//end stopServer method
	
	/**
	 * Method that enters the userName of the client inside the map
	 * @param packet The packet that was sent by the client
	 * @param client The socket of the client
	 * */
	private synchronized void connectClient(Packet packet, Socket client) {
		if(connectedClientMap.get(packet.getUserName()) != null) {
			System.out.println("There already is a user with that name!!");
		}else {
			connectedClientMap.put(packet.getUserName(), client);
			paintServer.updateView();
		}//end if else
	}//end connectClient method
	
	/**
	 * Method that removes the userName of the client from the map
	 * @param client The socket of the client
	 * */
	private synchronized void removeClient(Socket client) {
		for(String nickname : connectedClientMap.keySet()) {
			Socket socket = connectedClientMap.get(nickname);
			if(socket.equals(client)) {
				connectedClientMap.remove(nickname);
			}//end if
		}//end for
		paintServer.updateView();
	}//end removeClient method
	
	/**
	 * Method that stores the canvas of the client inside a map
	 * @param packet The packet that was send from the client
	 * @param client The socket of the client
	 * */
	private synchronized void putClientImage(Packet packet, Socket client) {
		if(clientImagesMap.containsValue(client)) {
			clientImagesMap.replace(packet.getImage(), client);
		}else {
			clientImagesMap.put(packet.getImage(), client);
		}//end if else
	}//end putClientImage method
	
	/**
	 * Method that processes the received packet from the client
	 * @param packet The packet that was received from the client
	 * @param client the socket of the client
	 * */
	private synchronized void processPacket(Packet packet, Socket client, ObjectOutputStream out) {
		if(!packet.getImageState()) {
			connectClient(packet, client);
		}else {
			putClientImage(packet, client);
			for(String nickname : connectedClientMap.keySet()) {
				Socket socket = connectedClientMap.get(nickname);
				if(socket.equals(client)) {
					PaintServer.getInstance().log("Client " + nickname + " shared his screen!!");
				}else {
					try {
						out.writeObject(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}//end if
			}//end for
			
		}//end if else
	}//end processPacket
	
	/**
	 * Method that returns the map with all the connected clients
	 * @return connectedClientMap
	 * */
	public Map<String, Socket> getConnectedClientMap(){
		return connectedClientMap;
	}// end getConnectedClientMap method
	
	
	/**
	 * Method that returns the map with all the canvases of the clients
	 * @return clientImagesMap
	 * */
	public Map<int[][], Socket> getClientImagesMap(){
		return clientImagesMap;
	}//end getClientImagesMap method
	
	
	
	/**
	 * Inner class that implements the runnable interface and 
	 * handles the listening of clients
	 * */
	private class ClientListener implements Runnable{
		
		Socket client;
		private ObjectOutputStream out;
		private ObjectInputStream input = null;
		public ClientListener(Socket client) {
			this.client = client;
			
		}//end ClientListener constructor
		
		@Override
		public void run() {
			PaintServer.getInstance().log("Client has connected" + client.getRemoteSocketAddress());
			boolean error = false;
			
			try {
				
				out = new ObjectOutputStream(client.getOutputStream());
				input = new ObjectInputStream(client.getInputStream());
				
			}catch (IOException e) {
				System.out.println("Couldn't initialize the input and output streams!");
			}
			System.out.println("Streams have been initialized successfully!");
			while(!error && client.isConnected()) {
				try {
					//input = new ObjectInputStream(client.getInputStream());
					Packet packet = (Packet) input.readObject();
					processPacket(packet, client, out);
				} catch (ClassNotFoundException cnfe) {
					error = true;
					cnfe.printStackTrace();
				} catch (EOFException eofe) {
					error = true;
					PaintServer.getInstance().log("Client " + client.getRemoteSocketAddress() + " has disconnected! eof");
//					eofe.printStackTrace();
				} catch (IOException e) {
					error = true;
					PaintServer.getInstance().log("Client " + client.getRemoteSocketAddress() + " has disconnected!");
//					e.printStackTrace();
				}//end try catch statement
			}//end while
			
			
			try {
				input.close();
				out.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			removeClient(client);
			
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//end try catch statement
		}//end run method
	
}//end ClientListener inner class
	
}//end NetworkServer class
