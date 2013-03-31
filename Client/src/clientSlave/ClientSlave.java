package clientSlave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import clientSupreme.talking;

public class ClientSlave implements talking{

	/** Groups associated with their creator (ip) which a client has been invited */
	private HashMap<InetAddress, String> _listGroups;
	/** communication broadcast from client master */
	private DatagramSocket _sockBroadcast;
	/** Port of a client master (UDP) */
	private int _portBis;
	/** Port of a client */
	private int _portClient;
	/** communication between the client's neighboor and the client (ring) */
	private Socket _sockNeighboor;
	/** communication between the clients (slave) and the client master */
	private Socket _sockClientBis;
	
	/**
	 * Constructor
	 */
	public ClientSlave (){
		_portBis	= 9300; // We considerate port 9300 for the clients Bis
		_portClient = 9301;
		try {
			_sockBroadcast = new DatagramSocket(_portClient);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Allow a client to receive invitation then if the client wished this group, he sends an answer
	 * @throws IOException
	 */
	public void receiveInvitation () throws IOException{
		assert(_listGroups != null);
		byte[] receiveDtg = new byte[1024];
		DatagramPacket invitation = new DatagramPacket(receiveDtg, receiveDtg.length);
		_sockBroadcast.receive(invitation);
		System.out.println("coucou");
		String grpInvitation = invitation.getData().toString();
		_listGroups.put(invitation.getAddress(), grpInvitation); // add the sender's ip (client bis) and group name
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
		DatagramSocket tmp = new DatagramSocket();
		DatagramPacket confirm = new DatagramPacket(answer, answer.length, ipClientBis, _portBis);
		tmp.send(confirm);
	} // requestJoinGroup ()
	
	/**
	 * Each client must use this function to bind itself with its neighboor for the creation of the ring
	 * @param ipClientBis is the Address IP of the client master
	 * @throws IOException
	 */
	public void linkNeighboor (InetAddress ipClientBis) throws IOException {
		_sockClientBis = new Socket(ipClientBis, _portBis);
		BufferedReader bw = new BufferedReader(new InputStreamReader(_sockClientBis.getInputStream()));
		String ipNext = bw.readLine();
		bw.close();
		_sockClientBis.close();
		_sockNeighboor = new Socket(InetAddress.getByName(ipNext), _portClient);
	} // linkNeighboor ()

	// Get(s) :
	
	/**
	 * 
	 * @return the groups with their creators (IP)
	 */
	public HashMap<InetAddress, String> get_listGroups() {
		return _listGroups;
	}

	// functions high level :
	
	@Override
	public void sendMessagetoChat() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveMessageFromChat() {
		// TODO Auto-generated method stub
		
	}

}
