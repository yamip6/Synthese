package clientMaster;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;

import clientSupreme.talking;

public class ClientMaster implements talking {

	/** communication between the futur client master and the Server */
	private Socket _sockClient;
	/** List of clients (slave) for the client master (send ip to interconnect them) */
	private ArrayList<Socket> _sockOthers;
	/** communication broadcast from client master */
	private DatagramSocket _sockBroadcast;

	/** port Server */
	private int _portServer;
	/** Address IP Server */
	private InetAddress _adressServer;
	/** Address IP of a client master */
	private InetAddress		  _adressMaster;
	/** List of the clients which accepted the invitation */
	private ArrayList<InetAddress> _clientsAccepted;
	/** Port of a client */
	private int _portClient;
	private DataOutputStream _outStream;
	private volatile boolean _start = false;
	private volatile boolean _loop = true;
	
	private InetAddress _ipNext = null;

	private InetAddress _ipGroup; // trial, his value is written in the source code => 
	// the clientMaster and the clients may choose one ? We'll see.
	private MulticastSocket _socketEmission;

	/**
	 * Constructor
	 * 
	 * @param adressServer is the address of the server          
	 * @param portServer is the port of the server         
	 */
	public ClientMaster(String adressServer, int portServer) {

		_sockOthers = new ArrayList<Socket>();
		_portServer = portServer;

		try {
			_adressServer = InetAddress.getByName(adressServer);
			_sockClient = new Socket(_adressServer, _portServer);
			_outStream = new DataOutputStream(_sockClient.getOutputStream());
			_sockOthers      = new ArrayList<Socket>();
			_sockBroadcast = null;
			_portServer = portServer;
			_portClient = 9301;
			_ipGroup = InetAddress.getByName("239.255.80.84");
			_socketEmission = new MulticastSocket();
			_socketEmission.joinGroup(_ipGroup);
			_clientsAccepted = new ArrayList<InetAddress>();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	} // ClientMaster

	// Functions low level :

	/**
	 * Close the socket which permits communication between server and client(s)
	 * 
	 * @throws IOException
	 */
	public void closeConnexionServer() throws IOException {
		_sockClient.close();
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
			_adressMaster = _sockClient.getLocalAddress(); // my IP for the others clients
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
	 * @throws InterruptedException 
	 */
	public void Invitation(String nameGroup, int portClient) throws IOException, InterruptedException {
		
		byte[] invitation = nameGroup.getBytes(); // The nameGroup is considered as an invitation	
		byte[] receiveDtg = new byte[1024]; // answers from interested clients

		DatagramPacket reception;
		DatagramPacket toSend = new DatagramPacket(invitation, invitation.length, _ipGroup, _portClient);
		// The client (bis) will stop the loop when he wants, so the discussion could begin
		while (_loop) {
			_socketEmission.send(toSend);
			reception = new DatagramPacket(receiveDtg,receiveDtg.length);
			if(_start){
				byte[] stop = new String("stop").getBytes();
				toSend = new DatagramPacket(stop, stop.length, _ipGroup, _portClient);
				_socketEmission.send(toSend);
				break;
			}
			_socketEmission.receive(reception);
			System.out.println(reception.getAddress());
			if (!_clientsAccepted.contains(reception.getAddress()))
				_clientsAccepted.add(reception.getAddress()); // IPAdress of a enjoyed client is added in the ArrayList to create the ring 		
				System.out.println("Client added");
		}
		System.out.println("exit loop");
		
	} // Invitation ()
	
	/**
	 * Distribution of the adresses ips to the clients slave from the client master. Moreover,
	 * this function creates the first link between the client master and the first client slave for the ring.
	 * @throws IOException
	 */
	public void creationGroupDiscussion () throws IOException {
		DataOutputStream os = null; int i = 1; System.out.println(_clientsAccepted);
		_sockOthers.add(new Socket(_clientsAccepted.get(0), _portClient)); // Problème sur cette ligne, n'arrive pas à construire la socket alors que _clientsAccepted est rempli
		os = new DataOutputStream(_sockOthers.get(0).getOutputStream()); // clientMaster bound to first client
		os.writeBytes(_clientsAccepted.get(i).getHostAddress() + '\n'); // next
		os.writeBytes(_adressMaster.getHostAddress() + '\n'); // prev
		os.close();
		if (_clientsAccepted.size() > 1){
			for (; i < _clientsAccepted.size() - 1; ++i){
				_sockOthers.add(new Socket(_clientsAccepted.get(i), _portClient));
				Socket tmp = _sockOthers.get(i);
				os = new DataOutputStream(tmp.getOutputStream());
				os.writeBytes(_clientsAccepted.get(i+1).getHostAddress() + '\n'); // next
				os.writeBytes(_clientsAccepted.get(i-1).getHostAddress() + '\n'); // prev
				os.close();
			}
		}
		os = new DataOutputStream(_sockOthers.get(i).getOutputStream()); // last client bound to clientMaster
		os.writeBytes(_adressMaster.getHostAddress() + '\n');
		os.close();
		_ipNext = _sockOthers.get(0).getInetAddress();
		for (int j = 1; j < _clientsAccepted.size(); ++j){
			_sockOthers.get(j).close(); // except the first client for the beginning of the ring
		}
	} // creationGroupDiscussion()
	
	

	// functions high level :
	
	public boolean is_start() {
		return _start;
	}

	public void set_start(boolean _start) {
		this._start = _start;
	}

	@Override
	public void sendMessagetoChat(String text) throws IOException {
		assert(_sockOthers != null);
		DataOutputStream outToClient = new DataOutputStream(_sockOthers.get(0).getOutputStream());
		System.out.println("Preparation envoi");
		outToClient.writeBytes(text + '\n');
		System.out.println(text + " envoyé");
	}

	@Override
	public String receiveMessageFromChat() throws IOException {
		assert(_sockOthers != null);
		BufferedReader bw = new BufferedReader(new InputStreamReader(_sockOthers.get(_sockOthers.size()-1).getInputStream()));
		String rec = bw.readLine();
		System.out.println("J'ai recu : " + rec);
		return rec;
	}

	
}
