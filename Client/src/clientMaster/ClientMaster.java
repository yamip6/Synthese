package clientMaster;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;
import java.io.IOException;

import clientSupreme.Client;

import utils.Crypto;
import utils.Tools;
import utils.Utils;

public class ClientMaster extends Client {
	/** List of ip clients which accepted the invitation and are accepted to join the group */
	private ArrayList<String> _acceptedClients; 
	private MulticastSocket _broadcastSocketRing;
	private InetAddress _groupIpRing;
	
	/** Server public key */
    private byte[] _publicKey;
	
	private volatile boolean _start = false;
	private volatile boolean _loop = true;

	/**
	 * Constructor
	 * @param adressServer is the address of the server          
	 * @param portServer is the port of the server         
	 */
	public ClientMaster(String adressServer, int port, String username) {
		try {
			// Vérification de l'existence d'une paire de clef
			// Sauvegarde si necessaire (si un seul fichier est absent on régénère tout)
			if(!(new File("keys/private.key").exists() && new File("keys/private.salt.key").exists() && new File("keys/public.key").exists()))
				Tools.keyGenerator(); // Idem que Client Slave on devrait faire un constructeur commun
						
            _username = username;
			connectionServer(adressServer, port);
			_groupIp = InetAddress.getByName("239.255.80.84"); // A voir
			_groupIpRing = InetAddress.getByName("239.255.80.85");
			_broadcastSocket = new MulticastSocket();
			_broadcastSocket.joinGroup(_groupIp);
			_broadcastSocketRing = new MulticastSocket();
			_acceptedClients = new ArrayList<String>();
		} catch (Exception e) {
			e.printStackTrace();
		}

	} // ClientMaster ()

	/**
	 * Used by a client to request a creation of a group to the server
	 * @param nameGroup is the name of the group the client wants
	 * @throws Exception 
	 */
	public void requestCreationGroup(String nameGroup) throws Exception {
		send(CREATION);
		
		_keyPair = Crypto.loadKeyPair(new File("keys/private.key"), new File("keys/private.salt.key"), new File("keys/public.key"));
		
		identityControl();
		
		send(Utils.intToByteArray(_username.getBytes().length, 4));
		send(_username.getBytes());
		send(Utils.intToByteArray(nameGroup.getBytes().length, 4));
		send(nameGroup.getBytes());
		byte[] signature = Tools.sign(_keyPair.getPrivate(), Tools.concatenateByteArray(_username.getBytes(), nameGroup.getBytes()));
		send(Utils.intToByteArray(signature.length, 4));
		send(signature);
	} // requestCreationGroup ()

	/**
	 * Used by the client to know the response of the group's creation from the server
	 * @param nameGroupWished is the name of the group the client wants
	 * @return boolean
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public Boolean responseCreationGroup()throws IOException, ClassNotFoundException {
		byte[] response = receive(2);
		if(Arrays.equals(response, OK)) {
		    byte[] size = receive(4);
		    byte[] grpWished = receive(Utils.byteArrayToInt(size));
		    System.out.println("Response OK : " + new String(grpWished)); // DEBUG
		    return true;
		} else if(Arrays.equals(response, NOK)) {
			byte[] size = receive(4);
		    byte[] grpWished = receive(Utils.byteArrayToInt(size)); // + Raison Echec
		    return false;
		}
		return false;
		
	} // responseCreationGroup ()
	
	/**
	 * Méthode réalisant le contrôle d'identité
	 * @throws Exception
	 */
	public void identityControl() throws Exception {
		// Hashage MD5 de le clef publique
		byte[] hash = Tools.hashFile("keys/public.key");
		// Envoie du hash
		System.out.println("Envoie de l'empreinte de la clef publique : " + Utils.byteArrayToHexString(hash)); // DEBUG
		send(Utils.intToByteArray(hash.length, 1));
		send(hash);

		// Résultat du hash
		byte[] verif = receive(2);
		if(Arrays.equals(verif, OK)) {
			System.out.println("Votre clef est déjà enregistrée auprès du destinataire (réceprion OK)."); // DEBUG
			
			System.out.println("Inversion des rôles."); // DEBUG
			changeRole();
		} else if(Arrays.equals(verif, NOK)) {
			// Réception du challenge
			byte[] challengeR = receive(16);
			System.out.println("Réception du challenge (réception NOK)."); // DEBUG

			// Envoie de la clef publique et de la signature clef publique/challenge
			System.out.println("Envoie de la clef publique/signature."); // DEBUG
			byte[] pubKey = _keyPair.getPublic().getEncoded();
			send(Utils.intToByteArray(pubKey.length, 4));
			send(pubKey);
						
			byte[] signature = Tools.sign(_keyPair.getPrivate(), Tools.concatenateByteArray(_keyPair.getPublic().getEncoded(), challengeR));
			send(Utils.intToByteArray(signature.length, 4));
			send(signature);

			// Réception de l'empreinte
			byte[] tailleHash = receive(1);
			byte[] empreinte = receive(Utils.byteArrayToInt(tailleHash));
			System.out.println("Enpreinte reçue : " + Utils.byteArrayToHexString(empreinte)); // DEBUG

			// Comparaison des empreintes
			System.out.println("Mon empreinte : " + Utils.byteArrayToHexString(hash)); // DEBUG
			// Validation des empreintes
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
	 * Méthode permettant d'échanger les rôles au cours du contrôle d'identité
	 * @throws Exception : Tout exception (non géré)
	 */
	public void changeRole() throws Exception {
        // Réception du hash de controle d'identité
		byte[] tailleHash = receive(1);
		byte[] hash = receive(Utils.byteArrayToInt(tailleHash));
		System.out.println("Réception du hash : " + Utils.byteArrayToHexString(hash)); // DEBUG
		
		// Vérification du hash
		if(Tools.isPubKeyStored(hash)) {
			// Envoie OK
			_publicKey = Crypto.loadPubKey(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key")).getEncoded();
			System.out.println("Vérification OK (envoie OK)."); // DEBUG
			send(OK);
			// Fin de l'échange
		} else {
			// Envoie du challenge
			System.out.println("Envoie du challenge (envoie NOK)."); // DEBUG
			byte[] challenge = Tools.getChallenge();
			send(NOK);
			send(challenge);
			
			// On recoit la clé publique et la signature
			System.out.println("Réception de la clef publique et de la signature."); // DEBUG
			byte[] taillePubKey = receive(4);
			_publicKey = receive(Utils.byteArrayToInt(taillePubKey));
			byte[] tailleSign = receive(4);
		    byte[] signature = receive(Utils.byteArrayToInt(tailleSign));

		    // Vérification de la signature
		    byte[] data = Tools.concatenateByteArray(_publicKey, challenge);
		    boolean verif = Tools.verifSign(data, _publicKey, signature);
		    if(verif) {
		    	System.out.println("La vérification a réussie."); //DEBUG
		    	
		    	// Envoie du hash
		    	System.out.println("Envoie du hash."); // DEBUG
		    	byte[] empreinte = Tools.hash(_publicKey);
		    	send(Utils.intToByteArray(empreinte.length, 1));
		    	send(empreinte);
		    	
		    	// Validation de l'empreinte
		    	byte[] valide = receive(2);
		    	if(Arrays.equals(valide, OK)) {
		    		System.out.println("Le serveur a validé l'empreinte."); // DEBUG
		    	    // Sauvegarde de la clé publique
		    		System.out.println("Sauvegarde de la clé publique."); // DEBUG
		    	    Utils.saveBuffer(_publicKey, new File("contacts/" + Utils.byteArrayToHexString(empreinte) + ".key"));
		    	    // Fin de l'échange
		    	} else if(Arrays.equals(valide, NOK))
		    		System.out.println("Le serveur n'a pas validé l'empreinte."); //DEBUG
		    } else 
		    	System.out.println("La vérification a échouée."); // DEBUG
		}	
		
	} // changeRole()

	/**
	 * Used by the client master to invite clients to join its group
	 * @param nameGroup the client Master's group
	 * @param portClient the port of the clients
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void Invitation(String nameGroup, int portClient) throws IOException, InterruptedException {
		byte[] invitation = nameGroup.getBytes(); // The nameGroup is considered as an invitation we can use a key word as invitation !
		byte[] receiveDtg = new byte[1024]; // answers from interested clients

		DatagramPacket reception;
		DatagramPacket toSend = new DatagramPacket(invitation, invitation.length, _groupIp, _portClient);
		// The client (bis) will stop the loop when he wants, so the discussion could begin
		while (_loop) {
			_broadcastSocket.send(toSend);
			reception = new DatagramPacket(receiveDtg, receiveDtg.length);
			if(_start) { // Use byte array constant for stop
				byte[] stop = new String("stop").getBytes();
				toSend = new DatagramPacket(stop, stop.length, _groupIp, _portClient);
				_broadcastSocket.send(toSend);
				break;
			}
			_broadcastSocket.receive(reception);
			System.out.println(reception.getAddress()); // DEBUG
			// Les faire s'authentifier ICI avant d'accepter !!!
			if (!_acceptedClients.contains(reception.getAddress().getHostAddress()))   
				_acceptedClients.add(reception.getAddress().getHostAddress()); // IPAdress of a enjoyed client is added in the ArrayList to create the ring 		
			System.out.println("Client added"); // DEBUG
		}
		
	} // Invitation ()
	
	/**
	 * Distribution of the adresses ips to the clients slave from the client master. Moreover,
	 * this function creates the first link between the client master and the first client slave for the ring.
	 * @throws IOException
	 */
	public void creationGroupDiscussion () throws IOException {
		// envoyer en multicast la liste des ips _acceptedClients
	
		// Le dernier client accepté n'est autre que le client master lui-même :
		_acceptedClients.add(InetAddress.getLocalHost().getHostAddress());
		
		byte[] toSend = Utils.arrayListToByte(_acceptedClients);
		
		String ipNeighboor = _acceptedClients.get(0);
		connectionNeighboor(ipNeighboor);

		_broadcastSocketRing.joinGroup(_groupIpRing);
		DatagramPacket pck = new DatagramPacket(toSend, toSend.length, _groupIpRing, 9999); // portClient à changer ? 9999 ?
		_broadcastSocketRing.send(pck);
		
		startServerMode();
		
	} // creationGroupDiscussion()
	
	public boolean is_start() {
		return _start;
		
	} // is_start ()

	public void set_start(boolean start) {
		_start = start;
		
	} // set_start ()

} // ClientMaster
