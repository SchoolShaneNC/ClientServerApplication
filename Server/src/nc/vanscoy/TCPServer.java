//PROG1415
//Vanscoy


package nc.vanscoy;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

//a java server program
public class TCPServer extends JFrame implements Runnable, WindowListener {

	//JFrame (class, only one allowed) - set up the interface
	//Runnable (interface - must implement certain methods) - forces the creation of a run method
	//WindowListener (interface - must implement certain methods) - listens for windows events
	private static final long serialVersionUID = 1L;
	/*
	 * This is used for serialization, which allows Java objects to be converted into a byte stream 
	 * and later reconstructed.
		serialVersionUID ensures that a deserialized object matches the version of the 
		class used to serialize it.
		It’s typically found in classes that implement Serializable.
		The serialVersionUID is a unique identifier that helps Java ensure that the class 
		used to deserialize an object is compatible with the class that was used to serialize it.
		you control version compatibility and avoid unexpected errors when deserializing objects.
		Even if not strictly required, it's good practice to define serialVersionUID in any class 
		that implements Serializable. It makes your code more robust and future-proof.
		any object you pass to writeObject(...) must be serializable, or Java will throw a 
		NotSerializableException.
	 */
	
	//Server
	private ServerSocket server;
	//This is the core of a TCP/IP server in Java—it listens for incoming client connections on a specified port.
	private boolean go = false;
	//These fields belong to the class itself, not to any individual instance.
	//any class in the same package (or with appropriate access) can reference them
	//easy access without needing getters/setters
	static JTextArea output = new JTextArea("Ready...\n");
	static List<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();
	static List<String> messages = new ArrayList<String>();
	//all instances of the server share these lists because they are listed as static
	
	public	TCPServer() {
		//build interface
		output.setEditable(false);
		this.getContentPane().add(new JScrollPane(output));
		this.setTitle("Server");
		this.setBounds(100,100,300,500);
		this.setVisible(true);
		//It tells the window (the JFrame) to use itself (this) as the listener for its own window events.
		this.addWindowListener(this);
		
		//start server main thread
		//incoming threads
		Thread acceptThread = new Thread(this);
		//this refers to the current object instance — in your case, the TCPServer object.
		//It’s used to pass the server itself to a thread (new Thread(this)), because TCPServer implements Runnable.
		//prioritize accepting connections over other work
		acceptThread.setPriority(Thread.MAX_PRIORITY);
		//calls the run() method
		acceptThread.start();
	}

	@Override
	public void run() {
		try {
			//construct the server
			server = new ServerSocket(8000);
			//start additional server threads
			go = true;
			new BroadCast().start();
		} catch (Exception e) {
			output.append("Server launch failed...\n");
			return;
		}
		
		//loop to receive client connections
		int count = 1;
		while(go) {
			try {
				output.append("Waiting...\n");
				Socket socket = server.accept();//waiting for incoming connections
				//start a new process for each client
				new ClientHandler(socket);
			} catch (IOException e) {
				output.append("Attempt to connect " + count + " failed...\n");
				if(count++ <= 3) 
					continue;
				else
					break;
			}
		}
		output.append("Server is no longer accepting clients...\n");
	}

	//a server thread to broadcast text messages to all connected clients
	//as a thread runs independently of the main server thread that is listening for connections
	//This is where serializable would come in if we changed this class
	//Right now broadcast works with simple strings which are already serializable
	//if we codify the output to something more complex we would need to change message to implement Serializable
	/*public class ChatMessage implements Serializable {
	    private static final long serialVersionUID = 1L;

	    private String sender;
	    private String content;
	    private long timestamp;

	    // Constructor, getters, setters, etc.
	}*/

	
	class BroadCast extends Thread {
		@Override
		public void run() {
			output.append("Broadcasting enabled...\n");
			while(go) {//as long as server is running will run
				//allow other threads to run
				//this thread will 'step aside' for others
				Thread.yield();
				//check for clients and messages
				if(messages.size() > 0 && clientHandlers.size() > 0) {
					String msg = messages.get(0);
					for(int x=0;x<clientHandlers.size();x++)
						try {
							clientHandlers.get(x).out.writeObject(msg);
						} catch (IOException e) {
							output.append("Error writing to client...\n");
							clientHandlers.remove(x);//remove non responsive clients
						}
					messages.remove(0);
				}
			}
		}
	}
	//Methods of Window Listener Interface, so must be implemented even if empty
	@Override
	public void windowClosing(WindowEvent arg0) {
		//what happens just before the window is closed
		this.go = false; //stops the loops
		//loop through the clients' list and stop their threads
		for(int x=0;x<clientHandlers.size();x++)
			clientHandlers.get(x).go = false;
		try {
			server.close();//closes the server socket
		} catch (IOException e) {}
		System.exit(0); //terminates application cleanly
		
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) {}
	//when window gets the focus (refresh/resume)
	@Override
	public void windowClosed(WindowEvent arg0) {}
	//after window closed, cleanup
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	//loses focus, pause or save
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	//restored from minimized
	@Override
	public void windowIconified(WindowEvent arg0) {}
	//minimized
	@Override
	public void windowOpened(WindowEvent arg0) {}
	//first opened
	public static void main(String[] args) {
		new TCPServer();
	}
}
