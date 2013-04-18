package client.master;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import utils.Crypto;
import utils.Tools;
import utils.Utils;

import client.Client;

public class MasterClient extends Client {
	/** List of ip clients which accepted the invitation and are accepted to join the group */
	private ArrayList<String> _acceptedClients; 
	
	/** Server public key */
    private byte[] _publicKey;
    
    private ServerSocket _tmpListenSocket;
    private Socket _tmpSocket;
    private InputStream _tmpIn;
    private OutputStream _tmpOut;

	/**
	 * Constructor
	 * @param adressServer is the address of the server          
	 * @param portServer is the port of the server         
	 * @param username is the user name of the client bis    
	 */
	public MasterClient(String adressServer, int port, String username) {
		super(username);
		try {
			connectionServer(adressServer, port);
			_acceptedClients = new ArrayList<String>();
			_broadcastSocket = new MulticastSocket();
			_broadcastSocket.joinGroup(_ipGroup);
		} catch (Exception e) {
			e.printStackTrace();
		}

	} // MasterClient()

	/**
	 * Used by a client to request a creation of a group to the server
	 * @param nameGroup is the name of the group the client wants
	 * @throws Exception 
	 */
	public void requestGroupCreation(String nameGroup) throws Exception {
		send(CREATION);
		
		_keyPair = Crypto.loadKeyPair(new File("keys/private.key"), new File("keys/private.salt.key"), new File("keys/public.key"));
		
		identityControl();
		
		send(Utils.intToByteArray(_username.getBytes().length, 4));
		send(_username.getBytes());
		send(Utils.intToByteArray(nameGroup.getBytes().length, 4));
		send(nameGroup.getBytes());
		byte[] signature = Tools.sign(_keyPair.getPrivate(), Utils.concatenateByteArray(_username.getBytes(), nameGroup.getBytes()));
		send(Utils.intToByteArray(signature.length, 4));
		send(signature);
		
	} // requestGroupCreation()

	/**
	 * Used by the client to know the response of the group's creation from the server
	 * @param nameGroupWished is the name of the group the client wants
	 * @return boolean
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public Boolean responseGroupCreation()throws IOException, ClassNotFoundException {
		byte[] response = receive(2);
		if(Arrays.equals(response, OK)) {
		    byte[] size = receive(4);
		    byte[] grpWished = receive(Utils.byteArrayToInt(size));
		    System.out.println("Response OK : " + new String(grpWished)); // DEBUG
		    return true;
		} else if(Arrays.equals(response, NOK)) {
			byte[] size = receive(4);
		    @SuppressWarnings("unused")
			byte[] grpWished = receive(Utils.byteArrayToInt(size)); // + Raison Echec
		    return false;
		}
		return false;
		
	} // responseGroupCreation()
	
	/**
	 * Method which realize the identity control between clientBis and server
	 * @throws Exception
	 */
	public void identityControl() throws Exception {
		// MD5 hash of the public key
		byte[] hash = Tools.hashFile("keys/public.key");
		// Sending hash
		System.out.println("Envoi de l'empreinte de la clef publique : " + Utils.byteArrayToHexString(hash)); // DEBUG
		send(Utils.intToByteArray(hash.length, 1));
		send(hash);

		// Hash result
		byte[] verif = receive(2);
		if(Arrays.equals(verif, OK)) {
			System.out.println("Votre clef est déjà enregistrée auprès du destinataire (réceprion OK)."); // DEBUG
			
			System.out.println("Inversion des rôles."); // DEBUG
			changeRole();
		} else if(Arrays.equals(verif, NOK)) {
			// Receiving the challenge
			byte[] challengeR = receive(16);
			System.out.println("Réception du challenge (réception NOK)."); // DEBUG

			// Sending the public key and the signature public key/challenge
			System.out.println("Envoi de la clef publique/signature."); // DEBUG
			byte[] pubKey = _keyPair.getPublic().getEncoded();
			send(Utils.intToByteArray(pubKey.length, 4));
			send(pubKey);
						
			byte[] signature = Tools.sign(_keyPair.getPrivate(), Utils.concatenateByteArray(_keyPair.getPublic().getEncoded(), challengeR));
			send(Utils.intToByteArray(signature.length, 4));
			send(signature);

			// Receiving the imprint
			byte[] tailleHash = receive(1);
			byte[] empreinte = receive(Utils.byteArrayToInt(tailleHash));
			System.out.println("Enpreinte reçue : " + Utils.byteArrayToHexString(empreinte)); // DEBUG

			// Comparison of imprints
			System.out.println("Mon empreinte : " + Utils.byteArrayToHexString(hash)); // DEBUG
			// Footprints validation
			if(Arrays.equals(empreinte, hash)) {
				System.out.println("Les empreintes sont bien valides."); // DEBUG
				send(OK);

				System.out.println("Inversion des rôles."); // DEBUG
				changeRole();
			} else {
				System.out.println("Les empreintes reçue et réelle sont différentes."); // DEBUG
				send(NOK);
			}
		}
	} // identityControl()
	/**
	 * Method which permit to change role during the identity control
	 * @throws Exception
	 */
	public void changeRole() throws Exception {
        // Receipt of hash of identity control
		byte[] tailleHash = receive(1);
		byte[] hash = receive(Utils.byteArrayToInt(tailleHash));
		System.out.println("Réception du hash : " + Utils.byteArrayToHexString(hash)); // DEBUG
		
		// Hash check
		if(Tools.isPubKeyStored(hash)) {
			// Sending OK
			_publicKey = Crypto.loadPubKey(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key")).getEncoded();
			System.out.println("Vérification OK (envoie OK)."); // DEBUG
			send(OK);
			// End of the exchange
		} else {
			// Sending challenge
			System.out.println("Envoie du challenge (envoie NOK)."); // DEBUG
			byte[] challenge = Tools.getChallenge();
			send(NOK);
			send(challenge);
			
			// Receiving the public key and signature
			System.out.println("Réception de la clef publique et de la signature."); // DEBUG
			byte[] taillePubKey = receive(4);
			_publicKey = receive(Utils.byteArrayToInt(taillePubKey));
			byte[] tailleSign = receive(4);
		    byte[] signature = receive(Utils.byteArrayToInt(tailleSign));

		    // Signature check
		    byte[] data = Utils.concatenateByteArray(_publicKey, challenge);
		    boolean verif = Tools.verifSign(data, _publicKey, signature);
		    if(verif) {
		    	System.out.println("La vérification a réussie."); //DEBUG
		    	
		    	// Sending hash
		    	System.out.println("Envoi du hash."); // DEBUG
		    	byte[] empreinte = Tools.hash(_publicKey);
		    	send(Utils.intToByteArray(empreinte.length, 1));
		    	send(empreinte);
		    	
		    	// Imprint validation
		    	byte[] valide = receive(2);
		    	if(Arrays.equals(valide, OK)) {
		    		System.out.println("Le serveur a validé l'empreinte."); // DEBUG
		    	    // Saving the public key
		    		System.out.println("Sauvegarde de la clé publique."); // DEBUG
		    	    Utils.saveBuffer(_publicKey, new File("contacts/" + Utils.byteArrayToHexString(empreinte) + ".key"));
		    	    // End of the exchange
		    	} else if(Arrays.equals(valide, NOK))
		    		System.out.println("Le serveur n'a pas validé l'empreinte."); //DEBUG
		    } else 
		    	System.out.println("La vérification a échouée."); // DEBUG
		}	
		
	} // changeRole()

	/**
	 * Used by the client master to invite clients to join its group
	 * @param nameGroup the client Master's group
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void invitation(String nameGroup) throws IOException, InterruptedException {
		byte[] invitation = nameGroup.getBytes(); // The nameGroup is considered as an invitation we can use a key word as invitation !

		DatagramPacket toSend = new DatagramPacket(invitation, invitation.length, _ipGroup, 9999);

	    _broadcastSocket.send(toSend);
		
	} // Invitation()
	
	public void receiveClient() throws IOException {
		System.out.println("aaa");
		byte[] data = new byte[2];
		
		_tmpListenSocket = new ServerSocket(10000);
		_tmpSocket = _tmpListenSocket.accept();
		_tmpIn = _tmpSocket.getInputStream();
		_tmpIn.read(data);
		
		System.out.println(_tmpSocket.getInetAddress()); // DEBUG
		System.out.println(Arrays.equals(data, OK));	// DEBUG	
		if(Arrays.equals(data, OK) && !_acceptedClients.contains(_tmpSocket.getInetAddress().getHostAddress())) {
			_acceptedClients.add(_tmpSocket.getInetAddress().getHostAddress()); // IPAdress of a enjoyed client is added in the ArrayList to create the ring
			System.out.println("Client added");	// DEBUG
		} 
		// listenSocket.close()
	}

	/**
	 * Distribution of the adresses ips to the clients slave from the client master. Moreover,
	 * this function creates the first link between the client master and the first client slave for the ring.
	 * @throws IOException
	 */
	public void discussionGroupCreation() throws IOException {
		_acceptedClients.add(InetAddress.getLocalHost().getHostAddress());
		byte[] toSend = Utils.arrayListToByteArray(_acceptedClients);
		_tmpOut = _tmpSocket.getOutputStream();
		_tmpOut.write(toSend);
		_tmpOut.flush();
		String ipNeighboor = _acceptedClients.get(0);
		System.out.println(ipNeighboor);
		startServerMode(_port);
		connectionNeighboor(ipNeighboor, _port);
		
		_tmpSocket.close();
		_tmpListenSocket.close();
	} // discussionGroupCreation()
    
} // ClientMaster
