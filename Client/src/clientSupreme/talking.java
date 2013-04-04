package clientSupreme;

import java.io.IOException;

public interface talking {

	// Functions high level :
	
	public void sendMessagetoChat (String text) throws IOException; 
	public String receiveMessageFromChat() throws IOException;
	
}
