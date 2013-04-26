package server.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Arrays;

import java.security.KeyPair;

import java.sql.ResultSet;

import utils.Crypto;
import utils.Tools;
import utils.Utils;

import db.ConnectDB;

public class Server implements Runnable {

	/** Listen socket of the server */
	private ServerSocket _listenSocket;
	/** Connection socket with bis client*/
	private Socket _clientSocket;
	/** Socket output stream */
	private OutputStream _out;
	/** Socket input stream */
	private InputStream _in;	
	
	/** List of created groups */
	private ArrayList<String> _groupList;
	/** Group creator public key */
    private byte[] _publicKey;
    /** Server keypair */
    private KeyPair _keyPair;
    /** Client's thread */
    private Thread _clientThread;
	
	/** Constant of validation during communication*/
	public final byte[] OK = new byte[]{0x4f, 0x11};
	/** Constant of error */
	public final byte[] NOK = new byte[]{0x4f, 0x00};
	/** Constant of creation group */
	public final byte[] CREATION = new byte[]{0x2f, 0x00};
	/** Constant of other client authentification */
	public final byte[] AUTH = new byte[]{0x0d, 0x11};
	
	public Server (int port) {
		try {
			_groupList = new ArrayList<String>();
			// Verifying the existence of a key pair
			// Backup if necessary (if only one file is missing regenerating all)
			if(!(new File("keys/private.key").exists() && new File("keys/private.salt.key").exists() && new File("keys/public.key").exists()))
				Tools.keyGenerator();
			startServer(port);
			_clientThread = new Thread(this);
			ConnectDB.connect();
			// Loading key pair
			_keyPair = Crypto.loadKeyPair(new File("keys/private.key"), new File("keys/private.salt.key"), new File("keys/public.key"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} // Server ()
	
	/**
	 * Thread for each client of the server
	 */
	@Override
	public void run () {
		try {
			while(true) {
				// Thread for a new client
				_clientSocket = _listenSocket.accept(); 
				_in = _clientSocket.getInputStream();
				_out = _clientSocket.getOutputStream();

				byte[] request = receive(2);				
				if(Arrays.equals(request, CREATION)) { // Création
					if(identityControl())
						groupCreation();
				}
				
				// A mettre dans une boucle liée à ReceiveClient de MasterClient // TODO
				request = receive(2);
				if(Arrays.equals(request, AUTH)) { // Authentification
					slaveAuthentification();
				}
	        }
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				disconnection();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	} // run ()
	
	/**
	 * Method which realize the identity control between server and client bis
	 * @throws Exception
	 */
	public boolean identityControl () throws Exception {
		// Receipt hash of identity control
		byte[] hashSize = receive(1);
		byte[] hash = receive(Utils.byteArrayToInt(hashSize));
		System.out.println("Receive hash : " + Utils.byteArrayToHexString(hash)); // DEBUG
		
		// Hash check
		if(Tools.isPubKeyStored(hash)) {
			// Sending OK
			_publicKey = Crypto.loadPubKey(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key")).getEncoded();
			System.out.println("Checking OK (send OK)."); // DEBUG
			send(OK);
			
			System.out.println("Role reversal."); //DEBUG
			return changeRole();
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
		    		System.out.println("Saving the public key."); // DEBUG
		    	    Utils.saveBuffer(_publicKey, new File("contacts/" + Utils.byteArrayToHexString(imprint) + ".key"));
		    	    
		    	    System.out.println("Role reversal."); // DEBUG
		    	    return changeRole();
		    	} else if(Arrays.equals(valid, NOK))
		    		System.err.println("The client does not validate the impression."); //DEBUG
		    } else 
		    	System.err.println("Verification failed."); // DEBUG
		}	
		return false;
		
	} // identityControl ()
	
	/**
	 * Method which permit to change role during the identity control
	 * @throws Exception
	 */
	public boolean changeRole () throws Exception {
		// MD5 hash of the public key
		byte[] hash = Tools.hashFile("keys/public.key");
		// Sending hash
		System.out.println("Sending the imprint of the public key : " + Utils.byteArrayToHexString(hash)); // DEBUG
		send(Utils.intToByteArray(hash.length, 1));
		send(hash);

		// Hash result
		byte[] verif = receive(2);
		if(Arrays.equals(verif, OK)) {
			System.out.println("Your key is already registered with the recipient (receiving OK).\n"); // DEBUG
			return true;
			// End of the exchange
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
				System.out.println("Imprints are valid.\n");
				send(OK);
				return true;
			} else {
				System.err.println("The received and real footprints are differents.");
				send(NOK);
			}
		}
		return false;
		
	} // changeRole ()
	
	public void groupCreation () throws Exception {
		// Receiving username, group and signature
		byte[] size = receive(4);
		byte[] username = receive(Utils.byteArrayToInt(size));
		size = receive(4);
		byte[] grpName = receive(Utils.byteArrayToInt(size));
		System.out.println("Received from client : " + new String(username) + " - " + new String(grpName)); // DEBUG
		
		// Verifying the signature
		size = receive(4);
		byte[] signature = receive(Utils.byteArrayToInt(size));
		boolean result = Tools.verifSign(Utils.concatenateByteArray(username, grpName), _publicKey, signature);								
		if(result && !_groupList.contains(new String(grpName))) {
			System.out.println("The signature is valid (send OK)."); // DEBUG
			send(OK);
			signature = Tools.sign(_keyPair.getPrivate(), OK);
            send(Utils.intToByteArray(signature.length, 4));
            send(signature);
			
			// Getting the password of this user
			ResultSet res = ConnectDB.dbSelect("SELECT password FROM members WHERE username = '" + new String(username) + "'");
			res.next();
			
			// Sending the challenge to authentificate the user
			System.out.println("Sending the challenge for authentification."); // DEBUG
			byte[] tmpChallenge = Tools.getChallenge();
			byte[] challenge = Tools.decryptWithPass(new String(username), res.getString(1), tmpChallenge);
			send(Utils.intToByteArray(challenge.length, 4));
			send(challenge);
			signature = Tools.sign(_keyPair.getPrivate(), challenge);
			send(Utils.intToByteArray(signature.length, 4));
            send(signature);
            
            // Verifying the authentification
			size = receive(4);
			byte[] tmpChallengeR = receive(Utils.byteArrayToInt(size));
			if(Arrays.equals(tmpChallenge, tmpChallengeR)) {						
				_groupList.add(new String(grpName));
				send(OK);
				signature = Tools.sign(_keyPair.getPrivate(), OK);
	            send(Utils.intToByteArray(signature.length, 4));
	            send(signature);
	            System.out.println("New group created.\n"); // DEBUG
			} else {
				System.err.println("This user didn't managed to authentificate."); // DEBUG
				send(NOK);
			}
		} else {
			System.err.println("This group already exist (or this is a wrong signature)."); // DEBUG
			send(NOK);
		}
		
	} // groupCreation ()
	
	public void slaveAuthentification () throws Exception {
		System.out.println("Receiving the certificate."); // DEBUG
		byte[] size = receive(4);
		byte[] certificate = receive(Utils.byteArrayToInt(size));
		System.out.println("Receiving the encrypted."); // DEBUG
		size = receive(4);
		byte[] ciphered = receive(Utils.byteArrayToInt(size));
		
		// Decrypting
		System.out.println("Decrypting."); // DEBUG
		byte[] plain = Tools.decrypt(ciphered, _keyPair.getPrivate());
		byte[] username = Arrays.copyOfRange(plain, 0, plain.length-16);					
		byte[] imprint = Arrays.copyOfRange(plain, plain.length-16, plain.length);
		System.out.println("Authentification of : " + new String(username)); // DEBUG
		// Verifying the imprint of the certificate
		byte[] certImprint = Tools.hash(certificate);
		if(Arrays.equals(imprint, certImprint)) {
			System.out.println("The integrity of the certificate is checked."); // DEBUG
			// Getting the password of this user
			ResultSet res = ConnectDB.dbSelect("SELECT password FROM members WHERE username = '" + new String(username) + "'");
			res.next();
			// Decrypting the certificate with the password of the user who want authentificate
			byte[] myPubKey = Tools.encyptWithPass(new String(username), res.getString(1), certificate);
			// Compare the certificates
			if(Arrays.equals(_keyPair.getPublic().getEncoded(), myPubKey)) {
				System.out.println("The authentication was successful.\n"); // DEBUG
				send(OK);
				byte[] signature = Tools.sign(_keyPair.getPrivate(), OK);
	            send(Utils.intToByteArray(signature.length, 4));
	            send(signature);
				// OK => send sign(certificate)
				signature = Tools.sign(_keyPair.getPrivate(), certificate);
				send(Utils.intToByteArray(signature.length, 4));
				send(signature);
			} else {
				System.err.println("The user could not be authenticated."); // DEBUG
				send(NOK);
			}
		} else {
			System.err.println("The thumbprint of the certificate does not match."); // DEBUG
			send(NOK);
		}
		
	} // slaveAuthentification ()
	
	/**
	 * Method which permit to start the server's listening
	 * @param port : listen port
	 * @return The connection socket of cleint/server
	 * @throws IOException
	 */
	public void startServer (int port) throws IOException {
		_listenSocket = new ServerSocket(port);
		
	} // startServer ()
	
	public void disconnection () throws IOException {
		// Closing the sockets
		_clientSocket.close();
		_listenSocket.close();
		
	} // disconnection ()
	
	/**
	 * Method which permits to send byte array
	 * @param message : Byte array to send
	 * @throws IOException
	 */
	public void send (byte[] message) throws IOException {
		_out.write(message);
		_out.flush();
		
	} // send ()

	/**
	 * Méthod which permits to receive byte array
	 * @param size : Size of byte array we want to receive
	 * @return The byte array
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public byte[] receive (int size) throws IOException, ClassNotFoundException {
		byte[] data = new byte[size];
		_in.read(data); // Reading the inputstream
		return data;
		
	} // receive ()

	/**
	 * Accessor
	 * @return The client's thread
	 */
	public Thread get_clientThread () {
		return _clientThread;
		
	} // get_clientThread ()

} // Server
