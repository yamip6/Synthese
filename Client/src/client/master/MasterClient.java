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

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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

	/**
	 * Constructor
	 * @param adressServer is the address of the server          
	 * @param portServer is the port of the server         
	 * @param username is the user name of the client bis    
	 */
	public MasterClient (String adressServer, int port, String username, String pseudo) {
		super(username, pseudo);
		
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
	 * Used by the client to know the response of the group's creation from the server
	 * @param nameGroupWished is the name of the group the client wants
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean responseGroupCreation () throws Exception {
		// Identity control response
		byte[] response = receive(2);
		if(Arrays.equals(response, OK)) {
			System.out.println("Authentication."); // DEBUG
			// Checking signature
			byte[] size = receive(4);
	        byte[] signature = receive(Utils.byteArrayToInt(size));
	        if(!Tools.verifSign(OK, _publicKey, signature)) {
	        	System.err.println("Signature error.");
	        	return false;
	        }
	        // Receiving challenge of Authentication and check signature
	        System.out.println("Receiving the challenge."); // DEBUG
		    size = receive(4);
		    byte[] challenge = receive(Utils.byteArrayToInt(size));
		    size = receive(4);
	        signature = receive(Utils.byteArrayToInt(size));
	        if(!Tools.verifSign(challenge, _publicKey, signature)) {
	        	System.err.println("Signature error.");
	        	return false;
	        }
	        // Trying authentificate
		    byte[] answer = Tools.decryptWithPass(_username, new String(Utils.readPassword("Enter your password : ")), challenge);
		    send(Utils.intToByteArray(answer.length, 4));
		    send(answer);
		    
		    // Authentication result
		    response = receive(2);
			if(Arrays.equals(response, OK)) {
		        size = receive(4);
		        signature = receive(Utils.byteArrayToInt(size));
		        if(Tools.verifSign(OK, _publicKey, signature)) {
		        	System.out.println("The Authentication was successful.\n"); // DEBUG
		        	size = receive(4);
		        	_certificate = receive(Utils.byteArrayToInt(size));
		            return true;
		        } else
		        	System.err.println("Signature error."); // DEBUG
			} else if(Arrays.equals(response, NOK)) 
				System.err.println("Authentication failed."); // DEBUG
		} else if(Arrays.equals(response, NOK))
			System.err.println("This group already exist (or this is a wrong signature)."); // DEBUG
		return false;
		
	} // responseGroupCreation ()

	/**
	 * Used by the client master to invite clients to join its group
	 * @param nameGroup the client Master's group
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void invitation (String nameGroup) throws IOException, InterruptedException {
		// The invitation need a certificate
		byte[] content = Utils.concatenateByteArray(nameGroup.getBytes(), _certificate);
        byte[] invitation = Utils.concatenateByteArray(Utils.intToByteArray(content.length, 4), content);
		
		DatagramPacket toSend = new DatagramPacket(invitation, invitation.length, _ipGroup, 9999);
	    _broadcastSocket.send(toSend);
		
	} // invitation ()
	
	/**
	 * Method which permits to receive new clients in our group.
	 * @throws Exception
	 */
	public void receiveClient () throws Exception {	
		_tmpListenSocket = new ServerSocket(10000);
		
		while(_loop) {
			// Receipt of requests (certificate|encrypted)
			_tmpSocket = _tmpListenSocket.accept();
			_tmpIn = _tmpSocket.getInputStream();
			_tmpOut = _tmpSocket.getOutputStream();
			byte[] cerificateSigned = null;
			System.out.println("Receiving the certificate."); // DEBUG
			byte[] data = new byte[4];
			_tmpIn.read(data);
			byte[] certificate = new byte[Utils.byteArrayToInt(data)];
			_tmpIn.read(certificate);
			System.out.println("Receiving the encrypted."); // DEBUG
			_tmpIn.read(data);
			byte[] ciphered = new byte[Utils.byteArrayToInt(data)];
			_tmpIn.read(ciphered);
			System.out.println("Receiving the pseudo."); // DEBUG
			_tmpIn.read(data);
			byte[] pseudo = new byte[Utils.byteArrayToInt(data)];
			_tmpIn.read(pseudo);
			
			// Transferring data to the server
			System.out.println("Authorization request to server."); // DEBUG
			send(AUTH);
			System.out.println("Transfering the certificate."); // DEBUG
			send(Utils.intToByteArray(certificate.length, 4));
			send(certificate);
			System.out.println("Transferring the encrypted."); // DEBUG
			send(Utils.intToByteArray(ciphered.length, 4));
			send(ciphered);
			// Authentication result
			byte[] result = receive(2);
			if(Arrays.equals(OK, result)) {
				byte[] size = receive(4);
				byte[] signature = receive(Utils.byteArrayToInt(size));
				if(!Tools.verifSign(OK, _publicKey, signature)) {
					System.err.println("Signature error."); // DEBUG
					return;
				}
				System.out.println("The authentication was successful : " + _tmpSocket.getInetAddress()); // DEBUG
				size = receive(4);
				cerificateSigned = receive(Utils.byteArrayToInt(size));
				boolean ok = Tools.verifSign(certificate, _publicKey, cerificateSigned);
				if(ok && !_acceptedClients.contains(_tmpSocket.getInetAddress().getHostAddress())) {
					_acceptedClients.add(_tmpSocket.getInetAddress().getHostAddress()); // IPAdress of an enjoyed client is added in the ArrayList to create the ring
					_socketList.add(_tmpSocket); // Record the client
					_groupMembers.add(new String(pseudo));
					MasterClientGUI.get_start().refresh(Utils.byteArrayToHexString(cerificateSigned));
					System.out.println("A new client is added."); // DEBUG
				} else
					System.err.println("This client has already been added (or signature error)."); // DEBUG
			} else if(Arrays.equals(NOK, result))
				System.err.println("The user could not be authenticated (or The thumbprint of the certificate does not match)."); // DEBUG
			
			_tmpOut.write(Utils.intToByteArray(cerificateSigned.length, 4));
			_tmpOut.write(cerificateSigned.length);
		}
		
	} // receiveClient ()

	/**
	 * Distribution of the adresses ips to the clients slave from the client master. Moreover,
	 * this function creates the first link between the client master and the first client slave for the ring.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 */
	public void discussionGroupCreation () throws IOException, NoSuchAlgorithmException {
		System.out.println("Création du groupe de discussion");
		_acceptedClients.add(InetAddress.getLocalHost().getHostAddress());
		_groupMembers.add(_pseudo);
		
		for(int i = 0; i < _socketList.size(); ++i) {
			byte[] toSend = Utils.arrayListToByteArray(_groupMembers);
			byte[] ipNext = _acceptedClients.get(i+1).getBytes();
			_tmpOut = _socketList.get(i).getOutputStream();
			byte[] hash = Tools.hash(ipNext); // Hash to verifiy the integrity of the list
			_tmpOut.write(Utils.intToByteArray(toSend.length, 4));
			_tmpOut.write(toSend);
			_tmpOut.write(Utils.intToByteArray(ipNext.length, 4));
			_tmpOut.write(ipNext);
			_tmpOut.write(Utils.intToByteArray(hash.length, 4));
			_tmpOut.write(hash);
			_tmpOut.flush();
			_socketList.get(i).close();
		}

		String ipNeighboor = _acceptedClients.get(0);
		System.out.println("Linked to : " + ipNeighboor); // DEBUG
		startServerMode(_port);
		connectionNeighboor(ipNeighboor, _port);
		
	} // discussionGroupCreation ()
	
	/**
	 * 
	 * @throws Exception
	 */
	public void doDiffieHellman () throws Exception {
		System.out.println("Beginning Diffie Hellman."); // DEBUG
	    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");

	    keyGen.initialize(PARAMETER_SPEC);

	    KeyAgreement aKeyAgree = KeyAgreement.getInstance("DH");
	    KeyPair aPair = keyGen.generateKeyPair();

	    aKeyAgree.init(aPair.getPrivate());

	    Key key = null;
	    for(int i = 0; i < _acceptedClients.size()-1; ++i) {
		    // Receiving the publicKey of b
		    System.out.println("Receiving a public key (a part of the secret key)."); // DEBUG
		    byte[] size = receiveChat(4);
			byte[] pubB = receiveChat(Utils.byteArrayToInt(size));
			KeyFactory factory = KeyFactory.getInstance("DH");
			PublicKey bPubKey = factory.generatePublic(new X509EncodedKeySpec(pubB));
		    
			if(i == 0) {
				sendChat(Utils.intToByteArray(aPair.getPublic().getEncoded().length, 4));
				sendChat(aPair.getPublic().getEncoded());
			} else if (i > 0) {
		        sendChat(Utils.intToByteArray(key.getEncoded().length, 4));
			    sendChat(key.getEncoded());
			}
			if(i == _acceptedClients.size()-2)
			    key = aKeyAgree.doPhase(bPubKey, true);
			else
				key = aKeyAgree.doPhase(bPubKey, false);
	    }
	    
	    byte[] sharedSecret = aKeyAgree.generateSecret();
	    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WITHHMACSHA1");
		byte[] salt = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0, 0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b, 0x30, 0x30, (byte)0x9d };
		PBEKeySpec keySpec = new PBEKeySpec(new String(sharedSecret).toCharArray(), salt, 1000, 128);
		_sk = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
		    
		System.out.println("Secret: " + Utils.byteArrayToHexString(_sk.getEncoded()) + "\nSize : " + _sk.getEncoded().length + "\n"); // DEBUG
	    
	} // doDiffieHellman ()
	
	public void sendMessage (String text) throws Exception {
		System.out.println("Send a message (master)."); // DEBUG
		int cpt = 0;
		byte[] count = Utils.intToByteArray(cpt, 2);
		byte[] messageTmp = text.getBytes();
		byte[] cipher = Tools.encryptSym(messageTmp, _sk);
		System.out.println("Send cipher: " + Utils.byteArrayToHexString(cipher)); // DEBUG
		byte[] message = Utils.concatenateByteArray(cipher, count);
		
		sendChat(Utils.intToByteArray(message.length, 4));
		sendChat(message);
		
	} // sendMessage ()
	
	public void transmitMessage () throws Exception {
		while(true) {
			byte[] size = receiveChat(4);
			byte[] message = receiveChat(Utils.byteArrayToInt(size));
			byte[] messageTmp = Arrays.copyOfRange(message, 0, message.length-2);
			System.out.println("Receive cipher: " + Utils.byteArrayToHexString(messageTmp)); // DEBUG
			
			int cpt = Utils.byteArrayToInt(Arrays.copyOfRange(message, message.length-2, message.length));
			System.out.println("Counter : " + cpt); // DEBUG
			if(cpt < _acceptedClients.size()-1) {
				// Decrypting the message
				byte[] plain = Tools.decryptSym(messageTmp, _sk);
				int pos = _acceptedClients.size()-1-(cpt+1);
				String emetteur = _groupMembers.get(pos);
				if(MasterClientGUI.get_chat().get_fieldChat().getText().contentEquals(""))
				    MasterClientGUI.get_chat().get_fieldChat().setText(emetteur + " at " + Utils.getDate() + " : " + new String(plain));
				else
					MasterClientGUI.get_chat().get_fieldChat().setText(MasterClientGUI.get_chat().get_fieldChat().getText() + "\n" + " at " + Utils.getDate() + " : " + new String(plain));
				cpt += 1;
				message = Utils.concatenateByteArray(messageTmp, Utils.intToByteArray(cpt, 2));
				sendChat(size);
				sendChat(message);
			}
		}
	} // transmitMessage ()

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
