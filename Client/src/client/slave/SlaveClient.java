package client.slave;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.util.ArrayList;
import java.util.HashMap;

import java.io.IOException;

import utils.Utils;

import client.Client;

public class SlaveClient extends Client {

	/** Groups associated with their creator (ip) which a client has been invited */
	private HashMap<String, String> _listGroups;
	
	/**
	 * Constructor
	 */
	public SlaveClient (String username) {
		super(username);
		
		try {
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
	public void receiveInvitation () throws IOException{
		assert(_listGroups != null);
		
		byte[] receiveDtg = new byte[1024];
		DatagramPacket invitation;
		invitation = new DatagramPacket(receiveDtg, receiveDtg.length);
		_broadcastSocket.receive(invitation);
		byte[] grpInvitation = invitation.getData();
		System.out.println(invitation.getAddress()); // DEBUG
		if (!(_listGroups.containsKey(invitation.getAddress()) && _listGroups.containsValue(new String(grpInvitation))))
			_listGroups.put(invitation.getAddress().getHostAddress(), new String(grpInvitation));
		
		assert(_listGroups.size() > 0);
		
	} // receiveInvitation ()
	
	/**
	 * Following process invitation. The client choose among groups in listGroups he's interested, the group to join
	 * @param grp the name of the group to join
	 * @param ipClientBis the Address IP of the client Master
	 * @throws IOException
	 */
	public void requestJoinGroup (String grp, String ipClientBis) throws IOException{
		System.out.println("Request join group");
		connectionServer(ipClientBis, 10000);
		send(OK);
		
	} // requestJoinGroup ()
	
	/**
	 * Each client must use this function to bind itself with its neighboor for the creation of the ring
	 * @param ipClientBis is the Address IP of the client master
	 * @throws Exception 
	 */
	public void linkNeighboor (String ipClientBis) throws Exception {
		_inServer = _clientSocket.getInputStream();
		byte[] data = receive(1024);
		System.out.println("Liaison au voisin...");
		ArrayList<String> listIps = Utils.byteArrayToList(data); // DERNIERE ERREUR DU PROGRAMME !!!!
		int pos;
		// The client searchs its ip to determinate the ip following its own ip :
		if((pos = (listIps.indexOf(InetAddress.getLocalHost().getHostAddress()))) > -1){
			connectionNeighboor(listIps.get(pos+1), _port);
			System.out.println("Two clients linked !!!"); // DEBUG
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

} // SlaveClient
