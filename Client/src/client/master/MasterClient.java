package client.master;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Arrays;

import utils.Crypto;
import utils.Tools;
import utils.Utils;

import client.Client;
import client.master.ihm.MasterClientGUI;

public class MasterClient extends Client {
	/** List of ip clients which accepted the invitation and are accepted to join the group */
	private ArrayList<String> _acceptedClients; 
	
	/** Server public key */
    private byte[] _publicKey;
    
    /** Temporary listen socket with clients */
    private ServerSocket _tmpListenSocket;
    /** Temporary socket with clients */
    private Socket _tmpSocket;
    /** Socket input stream with client */
    private InputStream _tmpIn;
    /** Socket output stream with client */
    private OutputStream _tmpOut;
    /** Temporary socket list of the group */
    private ArrayList<Socket> _socketList;
    
    /** */
    private volatile boolean _loop = true;

	/**
	 * Constructor
	 * @param adressServer is the address of the server          
	 * @param portServer is the port of the server         
	 * @param username is the user name of the client bis    
	 */
	public MasterClient (String adressServer, int port, String username) {
		super(username);
		
		try {
			connectionServer(adressServer, port);
			_acceptedClients = new ArrayList<String>();
			_socketList = new ArrayList<Socket>();
			_broadcastSocket = new MulticastSocket();
			_broadcastSocket.joinGroup(_ipGroup);
		} catch (Exception e) {
			e.printStackTrace();
		}

	} // MasterClient ()

	/**
	 * Used by a client to request a creation of a group to the server
	 * @param nameGroup is the name of the group the client wants
	 * @throws Exception 
	 */
	public void requestGroupCreation (String nameGroup) throws Exception {
		send(CREATION);
		// Loading key pair
		_keyPair = Crypto.loadKeyPair(new File("keys/private.key"), new File("keys/private.salt.key"), new File("keys/public.key"));	
		if(identityControl()) {
			// Sending username|group and signature
			send(Utils.intToByteArray(_username.getBytes().length, 4));
			send(_username.getBytes());
			send(Utils.intToByteArray(nameGroup.getBytes().length, 4));
			send(nameGroup.getBytes());
			byte[] signature = Tools.sign(_keyPair.getPrivate(), Utils.concatenateByteArray(_username.getBytes(), nameGroup.getBytes()));
			send(Utils.intToByteArray(signature.length, 4));
			send(signature);
		}
		
	} // requestGroupCreation ()

	/**
	 * Used by the client to know the response of the group's creation from the server
	 * @param nameGroupWished is the name of the group the client wants
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean responseGroupCreation () throws Exception {
		byte[] response = receive(2);
		if(Arrays.equals(response, OK)) {
			byte[] size = receive(4);
	        byte[] signature = receive(Utils.byteArrayToInt(size));
	        if(!Tools.verifSign(OK, _publicKey, signature)) {
	        	System.out.println("Erreur dans la signature.");
	        	return false;
	        }
		    size = receive(4);
		    byte[] challenge = receive(Utils.byteArrayToInt(size));
		    size = receive(4);
	        signature = receive(Utils.byteArrayToInt(size));
	        if(!Tools.verifSign(challenge, _publicKey, signature)) {
	        	System.out.println("Erreur dans la signature.");
	        	return false;
	        }
		    byte[] answer = Tools.tryChallenge(_username, new String(Utils.readPassword("Enter your password : ")), challenge);
		    
		    send(Utils.intToByteArray(answer.length, 4));
		    send(answer);
		    
		    response = receive(2);
			if(Arrays.equals(response, OK)) {
		        System.out.println("Response OK."); // DEBUG
		        size = receive(4);
		        signature = receive(Utils.byteArrayToInt(size));
		        if(Tools.verifSign(OK, _publicKey, signature))
		            return true;
		        else {
		        	System.out.println("Erreur dans la signature.");
		        }
			} else if(Arrays.equals(response, NOK)) {
				System.out.println("L'authentification a échoué.");
			    return false;
			}
		} else if(Arrays.equals(response, NOK)) {
			System.out.println("Le groupe souhaité existe déjà (ou erreur dans la signature)");
		    return false;
		}
		return false;
		
	} // responseGroupCreation ()
	
	/**
	 * Method which realize the identity control between clientBis and server
	 * @throws Exception
	 */
	public boolean identityControl () throws Exception {
		// MD5 hash of the public key
		byte[] hash = Tools.hashFile("keys/public.key");
		// Sending hash
		System.out.println("Sending the imprint of the public key : " + Utils.byteArrayToHexString(hash)); // DEBUG
		send(Utils.intToByteArray(hash.length, 1));
		send(hash);

		// Hash result
		byte[] verif = receive(2);
		if(Arrays.equals(verif, OK)) {
			System.out.println("Your key is already registered with the recipient (receiving OK)."); // DEBUG
			
			System.out.println("Role reversal.."); // DEBUG
			return changeRole();
		} else if(Arrays.equals(verif, NOK)) {
			// Receiving the challenge
			byte[] challengeR = receive(16);
			System.out.println("Receiving the challenge (receive NOK)."); // DEBUG

			// Sending the public key and the signature public key/challenge
			System.out.println("Sending the public key and the signature."); // DEBUG
			byte[] pubKey = _keyPair.getPublic().getEncoded();
			send(Utils.intToByteArray(pubKey.length, 4));
			send(pubKey);
									
			byte[] signature = Tools.sign(_keyPair.getPrivate(), Utils.concatenateByteArray(_keyPair.getPublic().getEncoded(), challengeR));
			send(Utils.intToByteArray(signature.length, 4));
			send(signature);

			// Receiving the imprint
			byte[] hashSize = receive(1);
			byte[] imprint = receive(Utils.byteArrayToInt(hashSize));
			System.out.println("Imprint received : " + Utils.byteArrayToHexString(imprint));

			// Comparison of imprints
			System.out.println("My imprint : " + Utils.byteArrayToHexString(hash));
			// Footprints validation
			if(Arrays.equals(imprint, hash)) {
				System.out.println("Imprints are valid.");
				send(OK);

				System.out.println("Role reversal."); // DEBUG
				return changeRole();
			} else {
				System.err.println("The received and real footprints are differents.");
				send(NOK);
			}
		}
		return false;
		
	} // identityControl ()
	/**
	 * Method which permit to change role during the identity control
	 * @throws Exception
	 */
	public boolean changeRole () throws Exception {
		// Receipt hash of identity control
		byte[] hashSize = receive(1);
		byte[] hash = receive(Utils.byteArrayToInt(hashSize));
		System.out.println("Receive hash : " + Utils.byteArrayToHexString(hash)); // DEBUG
				
		// Hash check
		if(Tools.isPubKeyStored(hash)) {
			// Sending OK
			_publicKey = Crypto.loadPubKey(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key")).getEncoded();
			System.out.println("Checking OK (send OK).\n"); // DEBUG
			send(OK);				
			return true;
			// End of the exchange
		} else {
			// Sending challenge
			System.out.println("Sending challenge (send NOK)."); // DEBUG
			byte[] challenge = Tools.getChallenge();
			send(NOK);
			send(challenge);
					
			// Receiving the public key and signature
			System.out.println("Receiving the public key and signature."); // DEBUG
			byte[] pubKeySize = receive(4);
			_publicKey = receive(Utils.byteArrayToInt(pubKeySize));
			byte[] signSize = receive(4);
			byte[] signature = receive(Utils.byteArrayToInt(signSize));

			// Signature check
			byte[] data = Utils.concatenateByteArray(_publicKey, challenge);
			boolean check = Tools.verifSign(data, _publicKey, signature);
			if(check) {
				System.out.println("The verification successful."); //DEBUG
				    	
				// Sending hash
				System.out.println("Sending hash."); // DEBUG
				byte[] imprint = Tools.hash(_publicKey);
				send(Utils.intToByteArray(imprint.length, 1));
				send(imprint);
				    	
				// Imprint validation
				byte[] valid = receive(2);
				if(Arrays.equals(valid, OK)) {
				    System.out.println("The client has validated the footprint."); // DEBUG
				    // Saving the public key
				    System.out.println("Saving the public key.\n"); // DEBUG
				    Utils.saveBuffer(_publicKey, new File("contacts/" + Utils.byteArrayToHexString(imprint) + ".key"));		    	    
				    return true;
				} else if(Arrays.equals(valid, NOK))
				    System.err.println("The client does not validate the impression."); //DEBUG
			} else 
				System.err.println("Verification failed."); // DEBUG
		}	
		return false;
		
	} // changeRole ()

	/**
	 * Used by the client master to invite clients to join its group
	 * @param nameGroup the client Master's group
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void invitation (String nameGroup) throws IOException, InterruptedException {
		byte[] invitation = nameGroup.getBytes(); // The nameGroup is considered as an invitation we can use a key word as invitation !

		DatagramPacket toSend = new DatagramPacket(invitation, invitation.length, _ipGroup, 9999);
	    _broadcastSocket.send(toSend);
		
	} // Invitation ()
	
	/**
	 * 
	 * @throws Exception
	 */
	public void receiveClient () throws Exception {	
		_tmpListenSocket = new ServerSocket(10000);
		
		while(_loop) {
			_tmpSocket = _tmpListenSocket.accept();
			_tmpIn = _tmpSocket.getInputStream();
			System.out.println("Réception du certificat."); // DEBUG
			byte[] data = new byte[4];
			_tmpIn.read(data);
			byte[] certificate = new byte[Utils.byteArrayToInt(data)];
			_tmpIn.read(certificate);
			System.out.println("Réception du chiffré."); // DEBUG
			_tmpIn.read(data);
			byte[] ciphered = new byte[Utils.byteArrayToInt(data)];
			_tmpIn.read(ciphered);
			
			System.out.println("Demande d'autorisation au serveur."); // DEBUG
			send(AUTH);
			System.out.println("Transmission du certificat.");
			send(Utils.intToByteArray(certificate.length, 4));
			send(certificate);
			System.out.println("Transmission du chiffré.");
			send(Utils.intToByteArray(ciphered.length, 4));
			send(ciphered);
			byte[] result = receive(2);
			if(Arrays.equals(OK, result)) {
				byte[] size = receive(4);
				byte[] signature = receive(Utils.byteArrayToInt(size));
				if(!Tools.verifSign(OK, _publicKey, signature))
					return; // + message
				System.out.println("Réception de la signature.");
				size = receive(4);
				byte[] cerificateSigned = receive(Utils.byteArrayToInt(size));
				boolean ok = Tools.verifSign(certificate, _publicKey, cerificateSigned);
				
				System.out.println(_tmpSocket.getInetAddress()); // DEBUG
				if(ok && !_acceptedClients.contains(_tmpSocket.getInetAddress().getHostAddress())) {
					_acceptedClients.add(_tmpSocket.getInetAddress().getHostAddress()); // IPAdress of a enjoyed client is added in the ArrayList to create the ring
					_socketList.add(_tmpSocket); // Record the client
					MasterClientGUI.get_chat().refresh();
					System.out.println("Client added");	// DEBUG + else message
				}
			} else if(Arrays.equals(NOK, result))
				System.err.println("The user could not be authenticated (or The thumbprint of the certificate does not match)."); // DEBUG
		}
		
	} // receiveClient ()

	/**
	 * Distribution of the adresses ips to the clients slave from the client master. Moreover,
	 * this function creates the first link between the client master and the first client slave for the ring.
	 * @throws IOException
	 */
	public void discussionGroupCreation () throws IOException {
		System.out.println("Création du groupe de discussion");
		_acceptedClients.add(InetAddress.getLocalHost().getHostAddress());
		
		for(Socket tmpSocket : _socketList) {
			byte[] toSend = Utils.arrayListToByteArray(_acceptedClients);
			_tmpOut = tmpSocket.getOutputStream();
			_tmpOut.write(toSend);
			_tmpOut.flush();
		}

		String ipNeighboor = _acceptedClients.get(0);
		System.out.println(ipNeighboor);
		startServerMode(_port);
		connectionNeighboor(ipNeighboor, _port);
		
		_tmpSocket.close();
		_tmpListenSocket.close();
		
	} // discussionGroupCreation ()

	/**
	 * 
	 * @param loop
	 */
	public void set_loop (boolean loop) {
		_loop = loop;
		
	} // set_loop ()

	/**
	 * 
	 * @return
	 */
	public ArrayList<String> get_acceptedClients() {
		return _acceptedClients;
	}
	
} // ClientMaster
