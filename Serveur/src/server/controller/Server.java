package server.controller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;

import utils.Crypto;
import utils.Tools;
import utils.Utils;

public class Server {

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
	
	/** Constant of validation during communication*/
	public final byte[] OK = new byte[]{0x4f, 0x11};
	/** Constant of error */
	public final byte[] NOK = new byte[]{0x4f, 0x00};
	/** Constant of creation group */
	public final byte[] CREATION = new byte[]{0x2f, 0x00};
	
	public Server (int port){
		try {
			_groupList = new ArrayList<String>();
			// V�rification de l'existence d'une paire de clef
			// Sauvegarde si necessaire (si un seul fichier est absent on r�g�n�re tout)
				if(!(new File("keys/private.key").exists() && new File("keys/private.salt.key").exists() && new File("keys/public.key").exists()))
					Tools.keyGenerator(); // Idem que Client Slave on devrait faire un constructeur commun
			startServer(port); // Port � param�trer dans le GUI
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
	public Socket startServer(int port) throws IOException {
		_listenSocket = new ServerSocket(port);
		_clientSocket = _listenSocket.accept();
		_in = _clientSocket.getInputStream(); 
		_out = _clientSocket.getOutputStream();  
		return _clientSocket;
		
	} // startServer()
	
	
	public void service () throws Exception {
		while(true)
        {
			byte[] request = receive(2);
			if(Arrays.equals(request, CREATION)) {
				identityControl();
				
				byte[] size = receive(4);
				byte[] username = receive(Utils.byteArrayToInt(size));
				size = receive(4);
				byte[] grpName = receive(Utils.byteArrayToInt(size));
				System.out.println("Received from client : " + new String(grpName)); // DEBUG
				size = receive(4);
				byte[] signature = receive(Utils.byteArrayToInt(size));
				boolean result = Tools.verifSign(Tools.concatenateByteArray(username, grpName), _publicKey, signature);
				if(result && !_groupList.contains(new String(grpName))) { // Et test authentification
					_groupList.add(new String(grpName));
					send(OK); // On chiffre la r�ponse avec le mot de passe de la bdd, si lz client arrive a d�chiffrer c'est qu'il s'est authentifier
		            send(Utils.intToByteArray(grpName.length, 4));
		            send(grpName); // Pas de controle d'int�grit�
				} else {
					send(NOK); // + Raison Echec - Pas de controle d'int�grit�
				}
			}
        }
		
	} // service ()
	
	/**
	 * M�thode r�alisant le contr�le d'identit�
	 * @throws Exception
	 */
	public void identityControl() throws Exception {
        // R�ception du hash de controle d'identit�
		byte[] tailleHash = receive(1);
		byte[] hash = receive(Utils.byteArrayToInt(tailleHash));
		System.out.println("R�ception du hash : " + Utils.byteArrayToHexString(hash)); // DEBUG
		
		// V�rification du hash
		if(Tools.isPubKeyStored(hash)) {
			// Envoie OK
			_publicKey = Crypto.loadPubKey(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key")).getEncoded();
			System.out.println("V�rification OK (envoie OK)."); // DEBUG
			send(OK);
			
			System.out.println("Inversion des r�les."); //DEBUG
			changeRole();
		} else {
			// Envoie du challenge
			System.out.println("Envoie du challenge (envoie NOK)."); // DEBUG
			byte[] challenge = Tools.getChallenge();
			send(NOK);
			send(challenge);
			
			// On recoit la cl� publique et la signature
			System.out.println("R�ception de la clef publique et de la signature."); // DEBUG
			byte[] taillePubKey = receive(4);
			_publicKey = receive(Utils.byteArrayToInt(taillePubKey));
			byte[] tailleSign = receive(4);
		    byte[] signature = receive(Utils.byteArrayToInt(tailleSign));

		    // V�rification de la signature
		    byte[] data = Tools.concatenateByteArray(_publicKey, challenge);
		    boolean verif = Tools.verifSign(data, _publicKey, signature);
		    if(verif) {
		    	System.out.println("La v�rification a r�ussie."); //DEBUG
		    	
		    	// Envoie du hash
		    	System.out.println("Envoie du hash."); // DEBUG
		    	byte[] empreinte = Tools.hash(_publicKey);
		    	send(Utils.intToByteArray(empreinte.length, 1));
		    	send(empreinte);
		    	
		    	// Validation de l'empreinte
		    	byte[] valide = receive(2);
		    	if(Arrays.equals(valide, OK)) {
		    		System.out.println("Le client a valid� l'empreinte."); // DEBUG
		    	    // Sauvegarde de la cl� publique
		    		System.out.println("Sauvegarde de la cl� publique."); // DEBUG
		    	    Utils.saveBuffer(_publicKey, new File("contacts/" + Utils.byteArrayToHexString(empreinte) + ".key"));
		    	    
		    	    System.out.println("Inversion des r�les."); // DEBUG
		    	    changeRole();
		    	} else if(Arrays.equals(valide, NOK))
		    		System.out.println("Le client n'a pas valid� l'empreinte."); //DEBUG
		    } else 
		    	System.out.println("La v�rification a �chou�e."); // DEBUG
		}	
		
	} // identityControl()
	
	/**
	 * M�thode permettant d'�changer les r�les au cours du contr�le d'identit�
	 * @throws Exception : Tout exception (non g�r�)
	 */
	public void changeRole() throws Exception {
		// Hashage MD5 de le clef publique
		byte[] hash = Tools.hashFile("keys/public.key");
		// Envoie du hash
		System.out.println("Envoie de l'empreinte de la clef publique : " + Utils.byteArrayToHexString(hash)); // DEBUG
		send(Utils.intToByteArray(hash.length, 1));
		send(hash);

		// R�sultat du hash
		byte[] verif = receive(2);
		if(Arrays.equals(verif, OK)) {
			System.out.println("Votre clef est d�j� enregistr�e aupr�s du destinataire (r�ceprion OK)."); // DEBUG
			// Fin de l'�change
		} else if(Arrays.equals(verif, NOK)) {
			// R�ception du challenge
			byte[] challengeR = receive(16);
			System.out.println("R�ception du challenge (r�ception NOK)."); // DEBUG

			// Chargement de votre clef publique
			System.out.println("Charmement de votre paire de clefs."); // DEBUG
			_keyPair = Crypto.loadKeyPair(new File("keys/private.key"), new File("keys/private.salt.key"), new File("keys/public.key"));

			// Envoie de la clef publique et de la signature clef publique/challenge
			System.out.println("Envoie de la clef publique/signature."); // DEBUG
			byte[] pubKey = _keyPair.getPublic().getEncoded();
			send(Utils.intToByteArray(pubKey.length, 4));
			send(pubKey);
						
			byte[] signature = Tools.sign(_keyPair.getPrivate(), Tools.concatenateByteArray(_keyPair.getPublic().getEncoded(), challengeR));
			send(Utils.intToByteArray(signature.length, 4));
			send(signature);

			// R�ception de l'empreinte
			byte[] tailleHash = receive(1);
			byte[] empreinte = receive(Utils.byteArrayToInt(tailleHash));
			System.out.println("Enpreinte re�ue : " + Utils.byteArrayToHexString(empreinte));

			// Comparaison des empreintes
			System.out.println("Mon empreinte : " + Utils.byteArrayToHexString(hash));
			// Validation des empreintes
			if(Arrays.equals(empreinte, hash)) {
				System.out.println("Les empreintes sont bien valides.");
				send(OK);
			} else {
				System.out.println("Les empreintes re�ue et r�elle sont diff�rentes.");
				send(NOK);
			}
		}
	} // changeRole()
	
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
	public void send(byte[] message) throws IOException {
		_out.write(message);
		_out.flush();
		
	} // send()

	/**
	 * M�thod which permits to receive byte array
	 * @param size : Size of byte array we want to receive
	 * @return The byte array
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public byte[] receive(int size) throws IOException, ClassNotFoundException {
		byte[] data = new byte[size];
		_in.read(data); // Reading the inputstream
		return data;
		
	} // receive()
	

} // Server
