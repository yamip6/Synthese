/*
 * @Author : Yassine AZIMANI, Benjamin GASTALDI
 * @Brief  : Projet Synthèse, Groupe de discussion anonyme
 */


import java.io.*;
import java.net.*;
import java.util.ArrayList;

/*
 * The client class represents a client which could participate
 * in a discussion group or even create it. We need two sockets :
 * one socket TCP for the chat and the other for broadcast with UDP
 */
public class Client {

	private Socket 				_sockClient;
	private DatagramSocket 		_sockBroadcast;
	private int					_portServer;
	private InetAddress     	_adressServer;
	private DataOutputStream 	_outStream;
	
	public Client (String adressServer, int portServer) {
		try {
			_adressServer = InetAddress.getByName(adressServer); } catch (UnknownHostException e) { e.printStackTrace(); }
			_portServer   = portServer;
		try {
			_sockClient = new Socket(_adressServer, _portServer); } catch (IOException e) { e.printStackTrace(); }
			_sockBroadcast = null;
		try {
			_outStream = new DataOutputStream(_sockClient.getOutputStream()); } catch (IOException e) { e.printStackTrace(); }
			
	} // Client
	
	public void requestCreationGroup (String nameGroup) throws IOException{
		_outStream.writeBytes("demande " + nameGroup);
	} // requestCreationGroup ()
	
	public Boolean responseCreationGroup (String nameGroupWished) throws IOException{
		BufferedReader bf = new BufferedReader(new InputStreamReader(_sockClient.getInputStream()));
		String response = bf.readLine();
		String correctAnswer = "ok " + nameGroupWished;
		if (correctAnswer.equals(response)){
			return true;
		}
		else {
			return false;
		}
	} // responseCreationGroup ()
	
	/*
	 * 
	 */
	public void Invitation (String nameGroup, int portClient, ArrayList<InetAddress> clientsAccept) throws IOException{
		
		byte[] invitation = nameGroup.getBytes(); // The nameGroup is considered as an invitation
		byte[]  receiveDtg = new byte [1024];
		
		_sockBroadcast = new DatagramSocket(); 
		_sockBroadcast.setBroadcast(true);
		_sockBroadcast.setSoTimeout(15000); // 26s to wait answers from clients
		_sockBroadcast.send(new DatagramPacket(invitation, invitation.length,InetAddress.getByName("255.255.255.255"), portClient));
		
		// Wait response until timeout (15 sec) :   Code à vérifier, modifier attentivement
		boolean keepgoing = true;
		while(keepgoing){ 
			try{
				DatagramPacket reception = new DatagramPacket(receiveDtg, receiveDtg.length);
				_sockBroadcast.receive(reception);
				clientsAccept.add(reception.getAddress()); // IPAdress of a enjoyed client is added in the ArrayList to create the ring
			}
			catch (SocketTimeoutException e){
				keepgoing = false;
			}

		}

	} // Invitation ()
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello");

	}

}
