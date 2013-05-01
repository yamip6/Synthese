package client;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.KeyPair;

import javax.crypto.spec.DHParameterSpec;

import utils.Tools;

public class Client {
	
	/** Client's username */
	protected String _username;
	
	/** Connection socket for client/server */
	protected Socket _clientSocket;
	/** Connection socket with neighboor (for the ring) */
	protected Socket _socketNeighboor;
	/** Listen socket of the server (receive from precedent node) */
	private ServerSocket _listenSocket;
	/** Connection socket with precedent client*/
	private Socket _precSocket;
	/** Connection port with client */
	protected final int _port = 9301;

	/** Socket output stream with server */
	protected OutputStream _outServer;
	/** Socket input stream with server */
	protected InputStream _inServer;	
	/** Socket output stream with next other client (ring) */
	protected OutputStream _outClient;
	/** Socket input stream with precedent other client (ring) */
	protected InputStream _inClient;
	
	/** Group ip to receive broadcast invitation/discussion */
	protected InetAddress _ipGroup; // trial, his value is written in the source code => the clientMaster and the clients may choose one ? We'll see.
	/** Broadcast socket for sending invitation to join group */
	protected MulticastSocket _broadcastSocket;

	/** User's key pair */
	protected KeyPair _keyPair = null;
	/** Certificate of a client */
	protected byte[] _certificate;
	protected int _nbAcceptedClients;
	
	protected static BigInteger g512 = new BigInteger("1234567890", 16);
    protected static BigInteger p512 = new BigInteger("1234567890", 16);

	/** Constant of validation during communication*/
	public final byte[] OK = new byte[]{0x4f, 0x11};
	/** Constant of error */
	public final byte[] NOK = new byte[]{0x4f, 0x00};
	/** Constant of group creation */
	public final byte[] CREATION = new byte[]{0x2f, 0x00};
	/** Constant of other client authentification */
	public final byte[] AUTH = new byte[]{0x0d, 0x11};
	
	/** */
    protected volatile boolean _loop = true;
    
    /**
     *    Static variables for 1024 bit Diffie-Hellman algorithm.
     *
     *    This is required to have matching moduli between client
     *    and server. The values are unimportant, they simply must match.
     *    Ideally, everyone would agree on standard moduli, like SKIP,
     *    the Simple Key management for Internet Protocols spec.
     */
    private static final byte SKIP_1024_MODULUS_BYTES[] = {
      (byte)0xF4, (byte)0x88, (byte)0xFD, (byte)0x58,
      (byte)0x4E, (byte)0x49, (byte)0xDB, (byte)0xCD,
      (byte)0x20, (byte)0xB4, (byte)0x9D, (byte)0xE4,
      (byte)0x91, (byte)0x07, (byte)0x36, (byte)0x6B,
      (byte)0x33, (byte)0x6C, (byte)0x38, (byte)0x0D,
      (byte)0x45, (byte)0x1D, (byte)0x0F, (byte)0x7C,
      (byte)0x88, (byte)0xB3, (byte)0x1C, (byte)0x7C,
      (byte)0x5B, (byte)0x2D, (byte)0x8E, (byte)0xF6,
      (byte)0xF3, (byte)0xC9, (byte)0x23, (byte)0xC0,
      (byte)0x43, (byte)0xF0, (byte)0xA5, (byte)0x5B,
      (byte)0x18, (byte)0x8D, (byte)0x8E, (byte)0xBB,
      (byte)0x55, (byte)0x8C, (byte)0xB8, (byte)0x5D,
      (byte)0x38, (byte)0xD3, (byte)0x34, (byte)0xFD,
      (byte)0x7C, (byte)0x17, (byte)0x57, (byte)0x43,
      (byte)0xA3, (byte)0x1D, (byte)0x18, (byte)0x6C,
      (byte)0xDE, (byte)0x33, (byte)0x21, (byte)0x2C,
      (byte)0xB5, (byte)0x2A, (byte)0xFF, (byte)0x3C,
      (byte)0xE1, (byte)0xB1, (byte)0x29, (byte)0x40,
      (byte)0x18, (byte)0x11, (byte)0x8D, (byte)0x7C,
      (byte)0x84, (byte)0xA7, (byte)0x0A, (byte)0x72,
      (byte)0xD6, (byte)0x86, (byte)0xC4, (byte)0x03,
      (byte)0x19, (byte)0xC8, (byte)0x07, (byte)0x29,
      (byte)0x7A, (byte)0xCA, (byte)0x95, (byte)0x0C,
      (byte)0xD9, (byte)0x96, (byte)0x9F, (byte)0xAB,
      (byte)0xD0, (byte)0x0A, (byte)0x50, (byte)0x9B,
      (byte)0x02, (byte)0x46, (byte)0xD3, (byte)0x08,
      (byte)0x3D, (byte)0x66, (byte)0xA4, (byte)0x5D,
      (byte)0x41, (byte)0x9F, (byte)0x9C, (byte)0x7C,
      (byte)0xBD, (byte)0x89, (byte)0x4B, (byte)0x22,
      (byte)0x19, (byte)0x26, (byte)0xBA, (byte)0xAB,
      (byte)0xA2, (byte)0x5E, (byte)0xC3, (byte)0x55,
      (byte)0xE9, (byte)0x2F, (byte)0x78, (byte)0xC7
    };
    /** Transform the representation above to a BigInteger. */
    private static final BigInteger MODULUS = new BigInteger(1, SKIP_1024_MODULUS_BYTES);
    /** The Base we're going to use is 2, as defined in SKIP. */
    private static final BigInteger BASE = BigInteger.valueOf(2);
    /** This wraps the parameters above into one object. */
    protected static final DHParameterSpec PARAMETER_SPEC = new DHParameterSpec(MODULUS,BASE);
    
	/**
	 * Constructor
	 * @param username : User name of the client
	 * @throws Exception
	 */
	public Client (String username) {
		try {
			// Verifying the existence of a key pair
			// Backup if necessary (if only one file is missing regenerating all)
			if(!(new File("keys/private.key").exists() && new File("keys/private.salt.key").exists() && new File("keys/public.key").exists()))
				Tools.keyGenerator();
									
		    _username = username;
			_ipGroup = InetAddress.getByName("239.255.80.84"); // A voir
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	} // Client ()

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
		
	} // connectionServer ()
	
	/**
	 * Method which permits to connect client with his neighboor via socket
	 * @param host : Connection neighboor IP
	 * @param port : Connection port
	 * @return Connection socket client/neighboor
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Socket connectionNeighboor (String ipNext, int port) throws UnknownHostException, IOException { // Tu pourras mettre le port _portClient
		_socketNeighboor = new Socket(ipNext, port);
		_outClient = _socketNeighboor.getOutputStream();
		return _socketNeighboor;
		
	} // connectionNeighboor ()
	
	/**
	 * Method which permits to receive message from precedent client in the ring
	 * @param port : Listening port
	 * @return The connection socket
	 * @throws IOException
	 */
	public Socket startServerMode (int port) throws IOException {
		_listenSocket = new ServerSocket(port);
		_precSocket = _listenSocket.accept();
		_inClient = _precSocket.getInputStream();
		return _precSocket;
		
	} // startServerMode ()
	
	/**
	 * Close the socket which permits communication between server and client
	 * @throws IOException
	 */
	public void closeConnectionServer () throws IOException {
		_clientSocket.close();
		
	} // closeConnectionServer ()
	
	/**
	 * Close the socket which permits communication between client and neighboor
	 * @throws IOException
	 */
	public void closeConnectionNeighboor () throws IOException {
		_socketNeighboor.close();
		
	} // closeConnectionNeighboor ()
	
	/**
	 * Method which permits to send byte array
	 * @param message : Byte array to send
	 * @throws IOException
	 */
	public void send (byte[] message) throws IOException {
		_outServer.write(message);
		_outServer.flush();
		
	} // send ()

	/**
	 * Méthod which permits to receive byte array
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
	
	/**
	 * Method which permits to send byte array
	 * @param message : Byte array to send
	 * @throws IOException
	 */
	public void sendChat (byte[] message) throws IOException {
		_outClient.write(message);
		_outClient.flush();
		
	} // send ()

	/**
	 * Méthod which permits to receive byte array
	 * @param size : Size of byte array we want to receive
	 * @return The byte array
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public byte[] receiveChat (int size) throws IOException, ClassNotFoundException {
		byte[] data = new byte[size];
		_inClient.read(data); // Reading the input stream
		return data;
		
	} // receive ()
	
} // Client
