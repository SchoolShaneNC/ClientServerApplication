package prog1415;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

//A java client program
public class TCPClient extends JFrame implements WindowListener {

	private static final long serialVersionUID = 1L;

	//Networking Objects
	private Socket socket; //connect to the server
	private ObjectInputStream in; // receives
	private ObjectOutputStream out; //sends
	
	private boolean go = true;
	private boolean connect = false;
	private String name;
	
	private JTextArea output = new JTextArea("Ready...\n");
	private JTextField input = new JTextField();

	
	private JButton login = new JButton("Connect");
	private JPanel north = new JPanel(new FlowLayout(FlowLayout.CENTER));
	
	//constructor
	public TCPClient() {
		//build interface
		output.setEditable(false); //keeps the output field from being edited
		
		input.setPreferredSize(new Dimension(100,30));

		north.add(input);
		north.add(login);
		
		this.getContentPane().add(north,BorderLayout.NORTH);
		this.getContentPane().add(new JScrollPane(output),BorderLayout.CENTER);
		this.setTitle("Client");
		this.setBounds(100,100,300,500);
		this.setVisible(true);
		this.addWindowListener(this);
		

		//event to send user's input to the server
		input.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//check for the connection, exits if not
				if(!connect) return;
				//make sure the user supplied their name
				if(input.getText().length() > 0) {
					try {
						//creates and sends message to server
						out.writeObject(name + ": " + input.getText());
						//forces immediate write, message sent without delay
						out.flush();
						input.setText("");
						input.requestFocus();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(input.getText().length() == 0) return; //exits if empty
				//Connect to the server
				try {
					//Resolves the localhost IP address
					InetAddress address = InetAddress.getByName("127.0.0.1");
					//create a TCP socket connection to the server
					socket = new Socket(address,8000);
					//output stream
					out = new ObjectOutputStream(socket.getOutputStream());
					//input stream
					in = new ObjectInputStream(socket.getInputStream());
					//set connection
					connect = true;
					//grabs the name from the textbox
					name = input.getText().toString();
					// resets the text box
					input.setText("");
					input.requestFocus();
					login.setVisible(false);
				} catch (IOException ex) {
					ex.printStackTrace();
				}				
			}
		});
			
		new ReadMessages().start(); // starts the read messages thread
	}
	
	//thread to read messages coming from the server
	class ReadMessages extends Thread {
		@Override
		public void run() {
			while(go) {
				try {
					// reads from the input stream, can read any serialized object
					Object obj = in.readObject();
					//check for a string so we don't have to deal with serialization
					if(obj instanceof String) 
						output.append(obj.toString() + "\n");
				} catch (Exception e) { }
			}
		}
		
	}
	
	public static void main(String[] args) {
		new TCPClient();
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}

}
