package clientSupreme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import java.security.KeyPair;

public class Client {
	
	/** Connection socket for client/server */
	protected Socket _clientSocket;
	/** Connection socket for the ring */
	protected Socket _socketNeighboor;
	/** Listen socket of the server (receive from precedent node) */
	private ServerSocket _listenSocket;
	/** Connection port with server */
	protected final int _portServer = 9300;
	/** Connection port with client */
	protected final int _portClient = 9301;

	/** Socket output stream with server */
	protected OutputStream _outServer;
	/** Socket input stream with server */
	protected InputStream _inServer;	
	/** Socket output stream with next other client (ring) */
	protected OutputStream _outClient;
	/** Socket input stream with next other client (ring) */
	protected InputStream _inClient;
	
	/** Group ip to receive broadcast invitation */
	protected InetAddress _groupIp; // trial, his value is written in the source code => the clientMaster and the clients may choose one ? We'll see.
	/** Broadcast socket for sending invitation to join group */
	protected MulticastSocket _broadcastSocket;
	
	/** Current receiver's public key */
	protected byte[] _pubKeyReceiver;
	/** User's key pair */
	protected KeyPair _keyPair = null;

	/** Constant of validation during communication*/
	public final byte[] OK = new byte[]{0x4f, 0x11};
	/** Constant of error */
	public final byte[] NOK = new byte[]{0x4f, 0x00};
	/** Constant of creation group */
	public final byte[] CREATION = new byte[]{0x2f, 0x00};

	/**
	 * Method which permits to connect client/server via socket
	 * @param host : Connection server URI or IP
	 * @param port : Connection port
	 * @return Connection socket client/server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Socket connectionServer (String host, int port) throws UnknownHostException, IOException {
		_clientSocket = new Socket(host, port);
		_outServer = _clientSocket.getOutputStream();
		_inServer = _clientSocket.getInputStream();
		return _clientSocket;
		
	} // connectionServer()
	
	public Socket connectionNeighboor (String ipNext, int port) throws UnknownHostException, IOException { // Tu pourras mettre le port _portClient
		_socketNeighboor = new Socket(ipNext, port);
		_outClient = _socketNeighboor.getOutputStream();
		_inClient = _socketNeighboor.getInputStream();
		return _socketNeighboor;
		
	} // connectionServer()
	
	/** You have to call it when you build the ring */
	public void startServerMode () throws IOException {
		_listenSocket = new ServerSocket(_portClient); // parameter it and change it
		_listenSocket.accept();
		
	} // startServer()
	
	/**
	 * Close the socket which permits communication between server and client(s)
	 * @throws IOException
	 */
	public void closeConnectionServer () throws IOException {
		_clientSocket.close();
		
	} // closeConnectionServer ()
	
	/**
	 * Method which permits to send byte array
	 * @param message : Byte array to send
	 * @throws IOException
	 */
	public void send(byte[] message) throws IOException {
		_outServer.write(message);
		_outServer.flush();
		
	} // send ()

	/**
	 * M�thod which permits to receive byte array
	 * @param size : Size of byte array we want to receive
	 * @return The byte array
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public byte[] receive(int size) throws IOException, ClassNotFoundException {
		byte[] data = new byte[size];
		_inServer.read(data); // Reading the inputstream
		return data;
		
	} // receive ()
	
} // Client
