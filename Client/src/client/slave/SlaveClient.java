package client.slave;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import utils.Utils;

import client.Client;

public class SlaveClient extends Client {

	/** Groups associated with their creator (ip) which a client has been invited */
	private HashMap<String, String> _listGroups;
	
	
	/** */
	private volatile boolean _loop = true;
	
	/**
	 * Constructor
	 */
	public SlaveClient(String username) {
		super(username);
		try {
			_listGroups = new HashMap<String,String>();
			_broadcastSocket = new MulticastSocket(9999);
			_broadcastSocket.joinGroup(_ipGroup);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} // SlaveClient()

	/**
	 * Allow a client to receive invitation then if the client wished this group, he sends an answer
	 * @throws IOException
	 */
	public void receiveInvitation() throws IOException{
		assert(_listGroups != null);
		byte[] receiveDtg = new byte[1024];
		byte[] ack = new byte[1024];
		DatagramPacket invitation, confirm;
		while(_loop) {
			invitation = new DatagramPacket(receiveDtg, receiveDtg.length);
			_broadcastSocket.receive(invitation);
			System.out.println("ss");
			byte[] grpInvitation = invitation.getData();
			System.out.println("ss");
			System.out.println(new String(grpInvitation)); // DEBUG
			System.out.println("ip : " + invitation.getAddress()); // DEBUG
			ack = _broadcastSocket.getLocalAddress().getAddress();
			confirm = new DatagramPacket(ack, ack.length, invitation.getAddress(), invitation.getPort());
			_broadcastSocket.send(confirm);
			if (!(_listGroups.containsKey(invitation.getAddress()) && _listGroups.containsValue(new String(grpInvitation))))
				_listGroups.put(invitation.getAddress().getHostAddress(), new String(grpInvitation));
			if (Arrays.equals(grpInvitation, NOK))
				_loop = false;
		}
		
		@SuppressWarnings("resource") // DEBUG
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File("log.txt"))); // DEBUG
		os.writeObject(get_listGroups()); // DEBUG
		assert(_listGroups.size() > 0);
		
	} // receiveInvitation()
	
	/**
	 * Following process invitation. The client choose among groups in listGroups he's interested, the group to join
	 * @param grp the name of the group to join
	 * @param ipClientBis the Address IP of the client Master
	 * @throws IOException
	 */
	public void requestJoinGroup(String grp, InetAddress ipClientBis) throws IOException{
		byte[] answer = new String("invitation true " + grp).getBytes(); // A normaliser avec byte array ok
		@SuppressWarnings("resource")
		DatagramSocket tmp = new DatagramSocket();
		DatagramPacket confirm = new DatagramPacket(answer, answer.length, ipClientBis, 9999);
		tmp.send(confirm);
		
	} // requestJoinGroup()
	
	/**
	 * Each client must use this function to bind itself with its neighboor for the creation of the ring
	 * @param ipClientBis is the Address IP of the client master
	 * @throws Exception 
	 */
	public void linkNeighboor(String ipClientBis) throws Exception {
		byte[] receiveDtg = new byte[1024];
		DatagramPacket pck = new DatagramPacket(receiveDtg, receiveDtg.length);
		_ipGroup = InetAddress.getByName("239.255.80.85");
		_broadcastSocket = new MulticastSocket(9999);
		_broadcastSocket.joinGroup(_ipGroup);
		_broadcastSocket.receive(pck);
		ArrayList<String> listIps = Utils.byteArrayToList(receiveDtg);
		int pos;
		pos = (listIps.indexOf(InetAddress.getLocalHost()));
		// The client searchs its ip to determinate the ip following its own ip :
		if((pos = (listIps.indexOf(InetAddress.getLocalHost()))) > -1){
			connectionNeighboor(listIps.get(pos+1), _port);
			System.out.println("Two clients linked !!!");  // DEBUG
		}
		else
			throw new Exception("Ip doesn't exist in the list of accepted clients...");
		
		startServerMode(_port);
		
	} // linkNeighboor()
	
	/**
	 * Accessor
	 * @return the groups with their creators (IP)
	 */
	public HashMap<String, String> get_listGroups() {
		return _listGroups;
		
	} // get_listGroups()

} // SlaveClient
