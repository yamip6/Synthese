package clientSlave;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import utils.Tools;
import utils.Utils;

import clientSupreme.Client;

public class ClientSlave extends Client {

	/** Groups associated with their creator (ip) which a client has been invited */
	private HashMap<InetAddress, String> _listGroups; // on mettra plustot <ipCreateur, PseudoCreateur, NomGroupe>
	
	private InetAddress _groupIpR;
	private MulticastSocket _broadcastSocketR;
	private Socket _sockClient;
	
	private volatile boolean _loop = true;
	
	/**
	 * Constructor
	 */
	public ClientSlave () {
		try {
			// Vérification de l'existence d'une paire de clef
			// Sauvegarde si necessaire (si un seul fichier est absent on régénère tout)
			if(!(new File("keys/private.key").exists() && new File("keys/private.salt.key").exists() && new File("keys/public.key").exists()))
				Tools.keyGenerator(); // Idem que Client Slave on devrait faire un constructeur commun
			
			_groupIp = InetAddress.getByName("239.255.80.84");
			_broadcastSocket = new MulticastSocket(_portClient);
			_broadcastSocket.joinGroup(_groupIp);
			_groupIpR = InetAddress.getByName("239.255.80.85");
			_broadcastSocketR = new MulticastSocket(9999);
			_listGroups = new HashMap<InetAddress,String>();
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
	} // ClientSlave ()

	/**
	 * Allow a client to receive invitation then if the client wished this group, he sends an answer
	 * @throws IOException
	 */
	public void receiveInvitation () throws IOException{
		assert(_listGroups != null);
		byte[] receiveDtg = new byte[1024];
		byte[] ack = new byte[1024];
		DatagramPacket invitation, confirm;
		while(_loop){
			invitation = new DatagramPacket(receiveDtg, receiveDtg.length);
			_broadcastSocket.receive(invitation);
			String grpInvitation = new String(invitation.getData(), 0, invitation.getLength());
			System.out.println(grpInvitation); // DEBUG
			System.out.println("ip : " + invitation.getAddress()); // DEBUG
			ack = _broadcastSocket.getLocalAddress().getAddress();
			confirm = new DatagramPacket(ack, ack.length, invitation.getAddress(), invitation.getPort());
			_broadcastSocket.send(confirm);
			if (!(_listGroups.containsKey(invitation.getAddress()) && _listGroups.containsValue(grpInvitation)))
				_listGroups.put(invitation.getAddress(), grpInvitation);
			if (grpInvitation.equals("stop")) // Donnée mebre byte array (on peut utiliser NOK ou une nouvelle
				_loop = false;
		}
		
		@SuppressWarnings("resource")
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File("log.txt")));
		os.writeObject(get_listGroups());
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
		@SuppressWarnings("resource")
		DatagramSocket tmp = new DatagramSocket();
		DatagramPacket confirm = new DatagramPacket(answer, answer.length, ipClientBis, _portServer);
		tmp.send(confirm);
		
	} // requestJoinGroup ()
	
	/**
	 * Each client must use this function to bind itself with its neighboor for the creation of the ring
	 * @param ipClientBis is the Address IP of the client master
	 * @throws Exception 
	 */
	public void linkNeighboor (String ipClientBis) throws Exception {
		byte[] receiveDtg = new byte[1024];
		DatagramPacket pck = new DatagramPacket(receiveDtg, receiveDtg.length);
		_broadcastSocketR.joinGroup(_groupIpR);
		_broadcastSocketR.receive(pck);
		ArrayList<String> listIps = Utils.arrayByteToList(receiveDtg);
		
		// The client searchs its ip to determinate the ip following its own ip :
		if(listIps.contains(InetAddress.getLocalHost())){
			int pos = listIps.indexOf(InetAddress.getLocalHost());
			connectionNeighboor(listIps.get(pos+1));
			System.out.println("Two clients linked !!!");  // DEBUG
		}
		else {
			throw new Exception("Ip doesn't exist in the list of accepted clients...");
		}
		
		startServerMode();
	} // linkNeighboor ()
	
	/**
	 * 
	 * @return the groups with their creators (IP)
	 */
	public HashMap<InetAddress, String> get_listGroups() {
		return _listGroups;
		
	} // get_listGroups ()

} // ClientSlave
