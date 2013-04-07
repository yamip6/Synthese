import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import utils.Utils;

public class Server {

	/** Listen socket of the server */
	private ServerSocket _listenSocket;
	/** Connection socket with bis client*/
	protected Socket _clientSocket;
	/** Socket output stream */
	protected OutputStream _out;
	/** Socket input stream */
	protected InputStream _in;	
	
	/** Constant of validation during communication*/
	public final byte[] OK = new byte[]{0x4f, 0x11};
	/** Constant of error */
	public final byte[] NOK = new byte[]{0x4f, 0x00};
	/** Constant of creation group */
	public final byte[] CREATION = new byte[]{0x2f, 0x00};
	
	public Server (){
		try {
			startServer(50000); // Port à paramétrer dans le GUI
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // Server ()
	
	/**
	 * Method which permit to start the server's listening
	 * @param port : listen port
	 * @return The connection socket of cleint/server
	 * @throws IOException
	 */
	public Socket startServer(int port) throws IOException {
		_listenSocket = new ServerSocket(port);
		_clientSocket = _listenSocket.accept();
		_in = _clientSocket.getInputStream(); 
		_out = _clientSocket.getOutputStream();  
		return _clientSocket;
		
	} // startServer()
	
	
	public void service () throws IOException, ClassNotFoundException {
		while(true)
        {
			byte[] request = receive(2);
			if(Arrays.equals(request, CREATION)) {
			    byte[] size = receive(4);
			    byte[] grpName = receive(Utils.byteArrayToInt(size));
	            System.out.println("Received from client : " + new String(grpName)); // DEBUG
                // Si l'authentification a réussie && grpName n'est pas dans listGroup
	            send(OK);
	            send(size);
	            send(grpName);
	            // Sinon
	            /*send(NOK);
	            send(size);
	            send(grpName); // + Raison Echec*/
			}
        }
		
	} // service ()
	
	public void disconnection () throws IOException {
		// Closing the sockets
		_clientSocket.close();
		_listenSocket.close();
		
	} // disconnection ()
	
	/**
	 * Method which permits to send byte array
	 * @param message : Byte array to send
	 * @throws IOException
	 */
	public void send(byte[] message) throws IOException {
		_out.write(message);
		_out.flush();
		
	} // send()

	/**
	 * Méthod which permits to receive byte array
	 * @param size : Size of byte array we want to receive
	 * @return The byte array
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public byte[] receive(int size) throws IOException, ClassNotFoundException {
		byte[] data = new byte[size];
		_in.read(data); // Reading the inputstream
		return data;
		
	} // receive()
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("SERVER : "); // DEBUG
		Server s = new Server();
		try {
			s.service();
			s.disconnection();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	} // main ()

} // Server
