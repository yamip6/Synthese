package client.slave;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;

import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import utils.Crypto;
import utils.Tools;
import utils.Utils;

import client.Client;
import client.slave.ihm.SlaveClientGUI;

public class SlaveClient extends Client {

	/** Groups associated with their creator (ip) which a client has been invited */
	private HashMap<String, String> _listGroups;
	
	/** Disposable certificate of a client */
	private byte[] _dcertif;
	
	/**
	 * Constructor
	 */
	public SlaveClient (String username) {
		super(username);
		
		try {
			_dcertif = Tools.encryptWithPass(_username, new String(Utils.readPassword("Enter your password : ")), Crypto.loadPubKey(new File("server/public.key")).getEncoded()); // Pour l'instant un certificat=chiffré avec mon pass de pubKeyServer + on verra si autre chose
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
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws SignatureException 
	 */
	public void receiveInvitation () throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, SignatureException {
		while(_loop) {
			// Receiving a broadcast invitation
			byte[] receiveDtg = new byte[1024];
			DatagramPacket invitation = new DatagramPacket(receiveDtg, receiveDtg.length);
			_broadcastSocket.receive(invitation);
			byte[] tInvitation = invitation.getData();
			int size = Utils.byteArrayToInt(Arrays.copyOfRange(tInvitation, 0, 4));
			size += 4; // Not very beautiful
			byte[] grpInvitation = Arrays.copyOfRange(tInvitation, 4, size-128);
			byte[] certInvitation = Arrays.copyOfRange(tInvitation, size-128, size);
			byte[] serverPubKey = Crypto.loadPubKey(new File("server/public.key")).getEncoded();
			// Verifying the certificate
			boolean signOK = Tools.verifSign(grpInvitation, serverPubKey, certInvitation);
			System.out.println("Receiving invitation : " + invitation.getAddress()); // DEBUG
			// Verifying if this group has already been created
			if (signOK && !(_listGroups.containsKey(invitation.getAddress()) && _listGroups.containsValue(new String(grpInvitation)))) {
				_listGroups.put(invitation.getAddress().getHostAddress(), new String(grpInvitation));
				SlaveClientGUI.get_jgroup().refresh(Utils.byteArrayToHexString(certInvitation));
				System.out.println("New invitation request.");
			} else
				System.err.println("This group has already been created (or certificate error).");
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
		send(Utils.intToByteArray(_dcertif.length, 4));
		send(_dcertif);
		byte[] serverPubKey = Crypto.loadPubKey(new File("server/public.key")).getEncoded();
		byte[] imprint = Tools.hash(_dcertif);
		byte[] ciphered = Tools.encrypt(Utils.concatenateByteArray(_username.getBytes(), imprint), serverPubKey); // + auth
		System.out.println("Sending the encrypted."); // DEBUG
		send(Utils.intToByteArray(ciphered.length, 4));
		send(ciphered);
		
		// Receiving the signed certificate
		System.out.println("Receiving the signed certificate.\n");
		byte[] size = receive(4);
		_certificate = receive(Utils.byteArrayToInt(size));
		
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
		byte[] sizeHash = receive(4);
		byte[] hash = receive(Utils.byteArrayToInt(sizeHash));
		byte[] sizeList = receive(4);
		byte[] list = receive(Utils.byteArrayToInt(sizeList));
		closeConnectionServer();
		
		// Verifying the hash
		if(!Arrays.equals(Tools.hash(list), hash)) {
			System.err.println("The integrity of the list has not been verified."); // DEBUG
			return;
		}
		
		ArrayList<String> listIps = Utils.byteArrayToList(list);
		_nbAcceptedClients = listIps.size();
		
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
	
	public void doDiffieHellman () throws Exception {
		System.out.println("Beginning Diffie Hellman."); // DEBUG
	    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");

	    keyGen.initialize(PARAMETER_SPEC);

	    KeyAgreement bKeyAgree = KeyAgreement.getInstance("DH");
	    KeyPair bPair = keyGen.generateKeyPair();

	    bKeyAgree.init(bPair.getPrivate());
	    sendChat(Utils.intToByteArray(bPair.getPublic().getEncoded().length, 4));
		sendChat(bPair.getPublic().getEncoded());
		Key key = null;
	    for(int i = 0; i < _nbAcceptedClients-1; ++i) {
		    // Receiving the publicKey of a
		    System.out.println("Receiving a public key (a part of the secret key)."); // DEBUG
		    byte[] size = receiveChat(4);
			byte[] pubA = receiveChat(Utils.byteArrayToInt(size));
			KeyFactory factory = KeyFactory.getInstance("DH");
			PublicKey aPubKey = factory.generatePublic(new X509EncodedKeySpec(pubA));
			
		    key = bKeyAgree.doPhase(aPubKey, true);
		    if(i == _nbAcceptedClients-2)
		    	break;
		    sendChat(Utils.intToByteArray(key.getEncoded().length, 4));
			sendChat(key.getEncoded());
	    }
	    
	    byte[] sharedSecret = bKeyAgree.generateSecret();
        SecretKey sk = new SecretKeySpec(sharedSecret, "AES");
	    System.out.println("Secret: " + Utils.byteArrayToHexString(sharedSecret)); // DEBUG
	    
	} // doDiffieHellman ()
	
	public void transmitMessage () throws ClassNotFoundException, IOException {
		while(true) {
			byte[] size = receiveChat(4);
			System.out.println("dfhf"); // DEBUG
			byte[] message = receiveChat(Utils.byteArrayToInt(size));
			byte[] messageTmp = Arrays.copyOfRange(message, 0, message.length-2);
			
			int cpt = Utils.byteArrayToInt(Arrays.copyOfRange(message, message.length-2, message.length));
			System.out.println("Counter : " + cpt); // DEBUG
			if(cpt < _nbAcceptedClients-1) {
				SlaveClientGUI.get_chat().get_fieldChat().setText(SlaveClientGUI.get_chat().get_fieldChat().getText() + "\n" + new String(messageTmp));
				cpt += 1;
				message = Utils.concatenateByteArray(messageTmp, Utils.intToByteArray(cpt, 2));
				sendChat(size);
				sendChat(message);
			}
		}
		
	} // transmitMessage ()
	
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
