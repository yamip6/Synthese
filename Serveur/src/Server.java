import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

	private ServerSocket _sockServ;
	
	public Server (){
		try {
			_sockServ = new ServerSocket(50000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void foo () throws IOException {
		while(true)
        {
           Socket connectionSocket = _sockServ.accept();
           BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
           DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
           String request = inFromClient.readLine();
           String[] expr = request.split(" ");
           System.out.println("Received from client : " + expr);
           System.out.println("To send : " + expr[1]);
           outToClient.writeBytes("ok " + expr[1] + '\n');
        }
	}
	
	public void deco () throws IOException {
		_sockServ.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("SERVER : ");
		Server s = new Server();
		try {
			s.foo();
			s.deco();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
