package nc.vanscoy;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
	//this is not a client app
	//Represents a server-side handler for a connected client
	//this is the client handler, the management system for the client apps that will connect
	//this is what's added to the client list
	//one for each client that connects over port 8000
	//streams to read and write with client instances
	/* In general:
	 * This code will manage the communications
	 * Add itself to the list of active handlers
	 * Run in its own thread
	 */
	ObjectInputStream in = null;
	ObjectOutputStream out = null;
	boolean go = false;

	public ClientHandler(Socket socket) {
		try {
			//create IO streams
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.in = new ObjectInputStream(socket.getInputStream());
			//add client to the server's client list
			TCPServer.clientHandlers.add(this);
			TCPServer.output.append("Client " + TCPServer.clientHandlers.size() + " connected...\n");
			//start a thread to read incoming client messages
			this.go = true;
			Thread thread = new Thread(this);
			thread.start();
		} catch (IOException e) {}
	}
	
	@Override
	public void run() {
		while(go) {
			try {
				//wait until a message comes from the client
				Object obj = in.readObject();
				//add valid message to the queue
				if(obj instanceof String) {
					TCPServer.messages.add(obj.toString());
				}
				
			} catch (Exception e) {
				//remove from the client list if streams are broken
				TCPServer.clientHandlers.remove(this);
			} 
		}
	}
}
