package clientMaster;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;
import java.io.IOException;

import clientSupreme.Client;

import utils.Tools;
import utils.Utils;

public class ClientMaster extends Client {

	/** List of ip clients which accepted the invitation and are accepted to join the group */
	private ArrayList<InetAddress> _acceptedClients;
	
	private volatile boolean _start = false;
	private volatile boolean _loop = true;

	/**
	 * Constructor
	 * @param adressServer is the address of the server          
	 * @param portServer is the port of the server         
	 */
	public ClientMaster(String adressServer, int port) {
		try {
			// Vérification de l'existence d'une paire de clef
			// Sauvegarde si necessaire (si un seul fichier est absent on régénère tout)
			if(!(new File("keys/private.key").exists() && new File("keys/private.salt.key").exists() && new File("keys/public.key").exists()))
				Tools.keyGenerator(); // Idem que Client Slave on devrait faire un constructeur commun
						
			connectionServer(adressServer, port);
			_groupIp = InetAddress.getByName("239.255.80.84"); // A voir
			_broadcastSocket = new MulticastSocket();
			_broadcastSocket.joinGroup(_groupIp);
			_acceptedClients = new ArrayList<InetAddress>();
		} catch (Exception e) {
			e.printStackTrace();
		}

	} // ClientMaster ()

	/**
	 * Used by a client to request a creation of a group to the server
	 * @param nameGroup is the name of the group the client wants
	 * @throws IOException
	 */
	public void requestCreationGroup(String nameGroup) throws IOException {
		send(CREATION);
		
		byte[] toSend = nameGroup.getBytes();
		send(Utils.intToByteArray(toSend.length, 4));
		send(toSend);
		
	} // requestCreationGroup ()

	/**
	 * Used by the client to know the response of the group's creation from the server
	 * @param nameGroupWished is the name of the group the client wants
	 * @return boolean
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public Boolean responseCreationGroup()throws IOException, ClassNotFoundException {
		byte[] response = receive(2);
		if(Arrays.equals(response, OK)) {
		    byte[] size = receive(4);
		    byte[] grpWished = receive(Utils.byteArrayToInt(size));
		    System.out.println("Response OK : " + new String(grpWished)); // DEBUG
		    return true;
		} else if(Arrays.equals(response, NOK)) {
			byte[] size = receive(4);
		    byte[] grpWished = receive(Utils.byteArrayToInt(size)); // + Raison Echec
		    return false;
		}
		return false;
		
	} // responseCreationGroup ()

	/**
	 * Used by the client master to invite clients to join its group
	 * @param nameGroup the client Master's group
	 * @param portClient the port of the clients
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void Invitation(String nameGroup, int portClient) throws IOException, InterruptedException {
		byte[] invitation = nameGroup.getBytes(); // The nameGroup is considered as an invitation we can use a key word as invitation !
		byte[] receiveDtg = new byte[1024]; // answers from interested clients

		DatagramPacket reception;
		DatagramPacket toSend = new DatagramPacket(invitation, invitation.length, _groupIp, _portClient);
		// The client (bis) will stop the loop when he wants, so the discussion could begin
		while (_loop) {
			_broadcastSocket.send(toSend);
			reception = new DatagramPacket(receiveDtg, receiveDtg.length);
			if(_start) { // Use byte array constant for stop
				byte[] stop = new String("stop").getBytes();
				toSend = new DatagramPacket(stop, stop.length, _groupIp, _portClient);
				_broadcastSocket.send(toSend);
				break;
			}
			_broadcastSocket.receive(reception);
			System.out.println(reception.getAddress()); // DEBUG
			// Les faire s'authentifier ICI avant d'accepter !!!
			if (!_acceptedClients.contains(reception.getAddress()))
				_acceptedClients.add(reception.getAddress()); // IPAdress of a enjoyed client is added in the ArrayList to create the ring 		
			System.out.println("Client added"); // DEBUG
		}
		
	} // Invitation ()
	
	/**
	 * Distribution of the adresses ips to the clients slave from the client master. Moreover,
	 * this function creates the first link between the client master and the first client slave for the ring.
	 * @throws IOException
	 */
	public void creationGroupDiscussion () throws IOException {

		
	} // creationGroupDiscussion()
	
	public boolean is_start() {
		return _start;
		
	} // is_start ()

	public void set_start(boolean start) {
		_start = start;
		
	} // set_start ()

} // ClientMaster
