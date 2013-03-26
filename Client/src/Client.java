/**
 * @Author : Yassine AZIMANI, Benjamin GASTALDI
 * @Brief  : Projet Synthèse, Groupe de discussion anonyme
 * 
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The client class represents a client which could participate
 * in a discussion group or even create it. We need four main sockets :
 * three sockets TCP for the chat (creation ring (links), communication with server) and the other for broadcast with UDP.
 * Besides, this class need to have two child in a long term to maintain easily the source code. ClientMaster and ClientSlave.
 * ClientMaster = ClientBis and ClientSlave = normal client
 */
public class Client {

	/** communication between the futur client master and the Server */
	private Socket _sockClient; 
	/** communication between the clients (slave) and the client master */
	private Socket _sockClientBis;
	/** communication between the client's neighboor and the client (ring) */
	private Socket _sockNeighboor; 
	/** List of clients (slave) for the client master (send ip to interconnect them) */
	private ArrayList<Socket> _sockOthers; 
	/** communication broadcast from client master */
	private DatagramSocket _sockBroadcast;
	/** port Server */
	private int _portServer;
	/** Address IP Server */
	private InetAddress _adressServer;
	/** Groups associated with their creator (ip) which a client has been invited */
	private HashMap<InetAddress, String> _listGroups;
	/** List of the clients which accepted the invitation */
	private ArrayList<InetAddress> _clientsAccepted;
	/** Stream out */
	private DataOutputStream _outStream;
	
	/** Determinate if the client is a client master (true) or a client slave (false) */
	private boolean			  _isBis;
	/** Address IP of a client master */
	private InetAddress		  _adressBis;
	/** Port of a client master (UDP) */
	private int				  _portBis;
	/** Port of a client */
	private int				  _portClient;
	/** Port of a client master (TCP) */
	private int 			  _portBisTCP;
	
	/**
	 * Constructor
	 * @param adressServer is the address of the server
	 * @param portServer is the port of the server
	 */
	public Client(String adressServer, int portServer) {
		_listGroups = new HashMap<InetAddress, String>();
		_clientsAccepted = new ArrayList<InetAddress>();
		_sockOthers      = new ArrayList<Socket>();
		_portServer = portServer;
		_portBis	= 9300; // We considerate port 9300 for the clients Bis
		_portBisTCP = 9302;
		_portClient = 9301;
		try {
			_adressServer = InetAddress.getByName(adressServer);
			_sockClient = new Socket(_adressServer, _portServer);
			_outStream = new DataOutputStream(_sockClient.getOutputStream());
			_sockBroadcast = null;
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	} // Client
	
	// Functions low level :
	
	/**
	 * Close the socket which permits communication between server and client(s)
	 * @throws IOException
	 */
	public void closeConnexionServer () throws IOException {
		_sockClient.close();
	}

	/**
	 * Close the socket which permits communication between client master and client(s)
	 * @throws IOException
	 */
	public void closeConnexionClient () throws IOException {
		_sockClientBis.close();
	}
	
	/**
	 * Used by a client to request a creation of a group to the server
	 * @param nameGroup is the name of the group the client wants
	 * @throws IOException
	 */
	public void requestCreationGroup(String nameGroup) throws IOException {
		_outStream.writeBytes("demande " + nameGroup + '\n');
	} // requestCreationGroup ()

	/**
	 * Used by the client to know the response of the group's creation from the server
	 * @param nameGroupWished is the name of the group the client wants
	 * @return boolean
	 * @throws IOException
	 */
	public Boolean responseCreationGroup(String nameGroupWished)throws IOException {
		BufferedReader bf = new BufferedReader(new InputStreamReader(_sockClient.getInputStream()));
		String response = bf.readLine();
		String correctAnswer = "ok " + nameGroupWished;
		System.out.println("rep : " + response);
		if (correctAnswer.equals(response)) {
			_isBis = true; // i become client Bis
			_adressBis = _sockClient.getLocalAddress(); // my IP for the others clients
			return true;
		} else {
			return false;
		}
	} // responseCreationGroup ()

	/**
	 * Used by the client master to invite clients to join its group
	 * @param nameGroup the client Master's group
	 * @param portClient the port of the clients
	 * @throws IOException
	 */
	public void Invitation(String nameGroup, int portClient) throws IOException {

		byte[] invitation = nameGroup.getBytes(); // The nameGroup is considered as an invitation	
		byte[] receiveDtg = new byte[1024]; // answers from interested clients
	
		_sockBroadcast = new DatagramSocket();
		_sockBroadcast.setBroadcast(true);
		_sockBroadcast.send(new DatagramPacket(invitation, invitation.length,
				InetAddress.getByName("255.255.255.255"), portClient));

		// The client (bis) will stop the loop when he wants, so the discussion could begin
		while (true) {
			//if(...)break;
			DatagramPacket reception = new DatagramPacket(receiveDtg,receiveDtg.length);
			_sockBroadcast.receive(reception);
			_clientsAccepted.add(reception.getAddress()); // IPAdress of a enjoyed client is added in the ArrayList to create the ring 										
		}

	} // Invitation ()
	
	/**
	 * Allow a client to receive invitation then if the client wished this group, he sends an answer
	 * @throws IOException
	 */
	public void receiveInvitation () throws IOException{
		assert(_listGroups != null);
		byte[] receiveDtg = new byte[1024];
		DatagramPacket invitation = new DatagramPacket(receiveDtg, receiveDtg.length);
		_sockBroadcast.receive(invitation);
		String grpInvitation = invitation.getData().toString();
		_listGroups.put(invitation.getAddress(), grpInvitation); // add the sender's ip (client bis) and group name
		assert(_listGroups.size() > 0);
	} // receiveInvitation ()
	
	/**
	 * Following process invitation. The client choose among groups in listGroups he's interested, the group to join
	 * @param grp the name of the group to join
	 * @param ipClientBis the Address IP of the client Master
	 * @throws IOException
	 */
	public void requestJoinGroup (String grp, InetAddress ipClientBis) throws IOException{
		byte[] answer = new String("invitation true " + grp).getBytes();
		DatagramSocket tmp = new DatagramSocket();
		DatagramPacket confirm = new DatagramPacket(answer, answer.length, ipClientBis, _portBis);
		tmp.send(confirm);
	} // requestJoinGroup ()
	
	/**
	 * Distribution of the adresses ips to the clients slave from the client master. Moreover,
	 * this function creates the first link between the client master and the first client slave for the ring.
	 * @throws IOException
	 */
	public void creationGroupDiscussion () throws IOException {
		for (int i = 0; i < _clientsAccepted.size(); ++i){
			_sockOthers.add(new Socket(_clientsAccepted.get(i), _portClient));
			Socket tmp = _sockOthers.get(i);
			DataOutputStream os = new DataOutputStream(tmp.getOutputStream());
			os.writeBytes(_clientsAccepted.get(i+1).getHostName() + '\n'); // Line
			os.close();
		}
		for (int i = 1; i < _clientsAccepted.size(); ++i){
			_sockOthers.get(i).close(); // except the first client for the beginning of the ring
		}
	} // creationGroupDiscussion()
	
	/**
	 * Each client must use this function to bind itself with its neighboor for the creation of the ring
	 * @param ipClientBis is the Address IP of the client master
	 * @throws IOException
	 */
	public void linkNeighboor (InetAddress ipClientBis) throws IOException {
		_sockClientBis = new Socket(ipClientBis, _portBis);
		BufferedReader bw = new BufferedReader(new InputStreamReader(_sockClientBis.getInputStream()));
		String ipNext = bw.readLine();
		bw.close();
		_sockClientBis.close();
		_sockNeighboor = new Socket(InetAddress.getByName(ipNext), _portClient);
	} // linkNeighboor ()

	// Get(s) :
	
	/**
	 * 
	 * @return the groups with their creators (IP)
	 */
	public HashMap<InetAddress, String> get_listGroups() {
		return _listGroups;
	}
	
	// Functions high level :
	
	public void sendMessagetoChat () {} // sendMessagetoChat ()
	public void receiveMessageFromChat () {} // receiveMessageFromChat ()
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello");
		Client c = new Client("127.0.0.1", 50000);
		try {
			c.requestCreationGroup("toto");
			System.out.println("rep serv : " + c.responseCreationGroup("toto"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



}
