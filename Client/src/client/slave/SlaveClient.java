package client.slave;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.util.ArrayList;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;

import utils.Crypto;
import utils.Tools;
import utils.Utils;

import client.Client;
import client.slave.ihm.SlaveClientGUI;

public class SlaveClient extends Client {

	/** Groups associated with their creator (ip) which a client has been invited */
	private HashMap<String, String> _listGroups;
	/** Disposable certificate of a client */
	private byte[] _certificate;
	
	/** */
    private volatile boolean _loop = true;
	
	/**
	 * Constructor
	 */
	public SlaveClient (String username) {
		super(username);
		
		try {
			_certificate = Tools.encryptWithPass(_username, new String(Utils.readPassword("Enter your password : ")), Crypto.loadPubKey(new File("server/public.key")).getEncoded()); // Pour l'instant un certificat=chiffré avec mon pass de pubKeyServer + on verra si autre chose
			_listGroups = new HashMap<String,String>();
			_broadcastSocket = new MulticastSocket(9999);
			_broadcastSocket.joinGroup(_ipGroup);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} // SlaveClient ()

	/**
	 * Allow a client to receive invitation then if the client wished this group, he sends an answer
	 * @throws IOException
	 */
	public void receiveInvitation () throws IOException {
		while(_loop) {
			// Receiving a broadcast invitation
			byte[] receiveDtg = new byte[1024];
			DatagramPacket invitation;
			invitation = new DatagramPacket(receiveDtg, receiveDtg.length);
			_broadcastSocket.receive(invitation);
			byte[] grpInvitation = invitation.getData();
			System.out.println("Receiving invitation : " + invitation.getAddress()); // DEBUG
			// Verifying if this group has already been created
			if (!(_listGroups.containsKey(invitation.getAddress()) && _listGroups.containsValue(new String(grpInvitation)))) {
				_listGroups.put(invitation.getAddress().getHostAddress(), new String(grpInvitation));
				SlaveClientGUI.get_jgroup().refresh();
			}
		}
		
	} // receiveInvitation ()
	
	/**
	 * Following process invitation. The client choose among groups in listGroups he's interested, the group to join
	 * @param grp the name of the group to join
	 * @param ipClientBis the Address IP of the client Master
	 * @throws Exception
	 */
	public void requestJoinGroup (String grp, String ipClientBis) throws Exception {
		System.out.println("Request join group"); // DEBUG
		connectionServer(ipClientBis, 10000);
		
		// Here we have to send : certificate + encrypt(identity + auth + certificate's footprint) with pubKeyServer
		System.out.println("Sending the certificate."); // DEBUG
		send(Utils.intToByteArray(_certificate.length, 4));
		send(_certificate);
		byte[] serverPubKey = Crypto.loadPubKey(new File("server/public.key")).getEncoded();
		byte[] imprint = Tools.hash(_certificate);
		byte[] ciphered = Tools.encrypt(Utils.concatenateByteArray(_username.getBytes(), imprint), serverPubKey); // + auth
		System.out.println("Sending the encrypted.\n"); // DEBUG
		send(Utils.intToByteArray(ciphered.length, 4));
		send(ciphered);
		
	} // requestJoinGroup ()
	
	/**
	 * Each client must use this function to bind itself with its neighboor for the creation of the ring
	 * @param ipClientBis is the Address IP of the client master
	 * @throws Exception 
	 */
	public void linkNeighboor () throws Exception {
		System.out.println("Linking neighboor."); // DEBUG
		// Receiving the ip list of the ring (for this group)
		_inServer = _clientSocket.getInputStream();
		byte[] data = receive(1024);
		ArrayList<String> listIps = Utils.byteArrayToList(data);
		
		// The client searchs its ip to determinate the ip following its own ip :
		int pos;
		if((pos = (listIps.indexOf(InetAddress.getLocalHost().getHostAddress()))) > -1){
			connectionNeighboor(listIps.get(pos+1), _port);
			System.out.println("I'm linked with my neighboor."); // DEBUG
		}
		else
			throw new Exception("Ip doesn't exist in the list of accepted clients...");
		
		startServerMode(_port);
		closeConnectionServer();
		
	} // linkNeighboor ()
	
	/**
	 * Accessor
	 * @return the groups with their creators (IP)
	 */
	public HashMap<String, String> get_listGroups () {
		return _listGroups;
		
	} // get_listGroups ()

	/**
	 * 
	 * @param loop
	 */
	public void set_loop (boolean loop) {
		_loop = loop;
		
	} // set_loop ()

} // SlaveClient
