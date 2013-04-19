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

import utils.Crypto;
import utils.Tools;
import utils.Utils;

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
	
	public Server (int port) {
		try {
			_groupList = new ArrayList<String>();
			// Verifying the existence of a key pair
			// Backup if necessary (if only one file is missing regenerating all)
				if(!(new File("keys/private.key").exists() && new File("keys/private.salt.key").exists() && new File("keys/public.key").exists()))
					Tools.keyGenerator();
			startServer(port);
			_clientThread = new Thread(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} // Server ()
	
	/**
	 * Method which permit to start the server's listening
	 * @param port : listen port
	 * @return The connection socket of cleint/server
	 * @throws IOException
	 */
	public void startServer (int port) throws IOException {
		_listenSocket = new ServerSocket(port);
		
	} // startServer ()
	
	@Override
	public void run () {
		try {
			while(true) {
				_clientSocket = _listenSocket.accept();
				_in = _clientSocket.getInputStream(); 
				_out = _clientSocket.getOutputStream(); 
				
				byte[] request = receive(2);
				
				if(Arrays.equals(request, CREATION)) { // Création
					identityControl();
					
					byte[] size = receive(4);
					byte[] username = receive(Utils.byteArrayToInt(size));
					size = receive(4);
					byte[] grpName = receive(Utils.byteArrayToInt(size));
					System.out.println("Received from client : " + new String(grpName)); // DEBUG
					size = receive(4);
					byte[] signature = receive(Utils.byteArrayToInt(size));
					boolean result = Tools.verifSign(Utils.concatenateByteArray(username, grpName), _publicKey, signature);
					if(result && !_groupList.contains(new String(grpName))) { // Et test authentification
						_groupList.add(new String(grpName));
						send(OK); // On chiffre la réponse avec le mot de passe de la bdd, si lz client arrive a déchiffrer c'est qu'il s'est authentifier
			            send(Utils.intToByteArray(grpName.length, 4));
			            send(grpName); // Pas de controle d'intégrité
					} else {
						send(NOK); // + Raison Echec - Pas de controle d'intégrité
					}
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
	public void identityControl () throws Exception {
		// Receipt hash of identity control
		byte[] tailleHash = receive(1);
		byte[] hash = receive(Utils.byteArrayToInt(tailleHash));
		System.out.println("Réception du hash : " + Utils.byteArrayToHexString(hash)); // DEBUG
		
		// Hash check
		if(Tools.isPubKeyStored(hash)) {
			// Sending OK
			_publicKey = Crypto.loadPubKey(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key")).getEncoded();
			System.out.println("Vérification OK (envoie OK)."); // DEBUG
			send(OK);
			
			System.out.println("Inversion des rôles."); //DEBUG
			changeRole();
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
		    	System.out.println("Envoie du hash."); // DEBUG
		    	byte[] empreinte = Tools.hash(_publicKey);
		    	send(Utils.intToByteArray(empreinte.length, 1));
		    	send(empreinte);
		    	
		    	// Imprint validation
		    	byte[] valide = receive(2);
		    	if(Arrays.equals(valide, OK)) {
		    		System.out.println("Le client a validé l'empreinte."); // DEBUG
		    		// Saving the public key
		    		System.out.println("Sauvegarde de la clé publique."); // DEBUG
		    	    Utils.saveBuffer(_publicKey, new File("contacts/" + Utils.byteArrayToHexString(empreinte) + ".key"));
		    	    
		    	    System.out.println("Inversion des rôles."); // DEBUG
		    	    changeRole();
		    	} else if(Arrays.equals(valide, NOK))
		    		System.out.println("Le client n'a pas validé l'empreinte."); //DEBUG
		    } else 
		    	System.out.println("La vérification a échouée."); // DEBUG
		}	
		
	} // identityControl ()
	
	/**
	 * Method which permit to change role during the identity control
	 * @throws Exception
	 */
	public void changeRole () throws Exception {
		// MD5 hash of the public key
		byte[] hash = Tools.hashFile("keys/public.key");
		// Sending hash
		System.out.println("Envoie de l'empreinte de la clef publique : " + Utils.byteArrayToHexString(hash)); // DEBUG
		send(Utils.intToByteArray(hash.length, 1));
		send(hash);

		// Hash result
		byte[] verif = receive(2);
		if(Arrays.equals(verif, OK)) {
			System.out.println("Votre clef est déjà enregistrée auprès du destinataire (réceprion OK)."); // DEBUG
			// End of the exchange
		} else if(Arrays.equals(verif, NOK)) {
			// Receiving the challenge
			byte[] challengeR = receive(16);
			System.out.println("Réception du challenge (réception NOK)."); // DEBUG

			// Loading your public key
			System.out.println("Charmement de votre paire de clefs."); // DEBUG
			_keyPair = Crypto.loadKeyPair(new File("keys/private.key"), new File("keys/private.salt.key"), new File("keys/public.key"));

			// Sending the public key and the signature public key/challenge
			System.out.println("Envoie de la clef publique/signature."); // DEBUG
			byte[] pubKey = _keyPair.getPublic().getEncoded();
			send(Utils.intToByteArray(pubKey.length, 4));
			send(pubKey);
						
			byte[] signature = Tools.sign(_keyPair.getPrivate(), Utils.concatenateByteArray(_keyPair.getPublic().getEncoded(), challengeR));
			send(Utils.intToByteArray(signature.length, 4));
			send(signature);

			// Receiving the imprint
			byte[] tailleHash = receive(1);
			byte[] empreinte = receive(Utils.byteArrayToInt(tailleHash));
			System.out.println("Enpreinte reçue : " + Utils.byteArrayToHexString(empreinte));

			// Comparison of imprints
			System.out.println("Mon empreinte : " + Utils.byteArrayToHexString(hash));
			// Footprints validation
			if(Arrays.equals(empreinte, hash)) {
				System.out.println("Les empreintes sont bien valides.");
				send(OK);
			} else {
				System.out.println("Les empreintes reçue et réelle sont différentes.");
				send(NOK);
			}
		}
	} // changeRole ()
	
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
	 * 
	 * @return
	 */
	public Thread get_clientThread () {
		return _clientThread;
		
	} // get_clientThread ()

} // Server
