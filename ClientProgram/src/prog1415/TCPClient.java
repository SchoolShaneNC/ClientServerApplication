package prog1415;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;



import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.*;

public class TCPClient extends JFrame implements WindowListener
{
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_PORT = 8000;

	//networking objects
	private Socket socket;
	private int portNum;
	private final File portFile = new File("port.txt");
	private boolean portFileExisted;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	private boolean go = true;
	private boolean connect = false;
	private String name;
	private boolean portConfirm = false;

	//header components
	private JLabel lblUsername = new JLabel("Username:");
	private JTextField txtUsername = new JTextField(12);

	private JButton login = new JButton("Connect");

	private JLabel lblPort = new JLabel("Port:");
	private JTextField txtPort = new JTextField(6);
	private JButton portBtn = new JButton("Enter");
	
	private JLabel lblStatus = new JLabel("Status: Disconnected");

	//user list
	private DefaultListModel<String> userListModel = new DefaultListModel<>();
	private JList<String> lstUsers = new JList<>(userListModel);

	//chat components
	private JTextArea output = new JTextArea("Ready...\n");
	private JTextField input = new JTextField();

	public TCPClient()
	{
		loadPort();

		//header
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		headerPanel.add(lblUsername);
		headerPanel.add(txtUsername);
		headerPanel.add(login);
		headerPanel.add(lblPort);
		headerPanel.add(txtPort);
		headerPanel.add(portBtn);
		headerPanel.add(lblStatus);

		//users panel
		JPanel usersPanel = new JPanel(new BorderLayout());
		usersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		usersPanel.add(new JLabel("Connected Users"), BorderLayout.NORTH);
		JScrollPane usersScroll = new JScrollPane(lstUsers);
		usersScroll.setPreferredSize(new Dimension(150, 0));
		usersPanel.add(usersScroll, BorderLayout.CENTER);

		//chat area
		output.setEditable(false);
		output.setLineWrap(true);
		output.setWrapStyleWord(true);

		JScrollPane chatScroll = new JScrollPane(output);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, usersPanel, chatScroll);

		splitPane.setDividerLocation(150);
		splitPane.setResizeWeight(0);

		//message area
		JPanel messagePanel = new JPanel(new BorderLayout(8, 0));

		messagePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		messagePanel.add(new JLabel("Message:"), BorderLayout.WEST);

		messagePanel.add(input, BorderLayout.CENTER);

		//JFrame setup
		add(headerPanel, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		add(messagePanel, BorderLayout.SOUTH);

		setTitle("TCP Client");
		setSize(800, 500);
		setMinimumSize(new Dimension(600, 400));
		setLocationRelativeTo(null);

		addWindowListener(this);

		setVisible(true);

//		if(portFileExisted)
//		{
//			confirmPort();
//		}

		//send message
		input.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!connect)
					return;

				if(input.getText().length() > 0)
				{
					try
					{
						out.writeObject(name + ": " + input.getText());
						out.flush();

						input.setText("");
						input.requestFocus();
					}
					catch(IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		
		portBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				confirmPort();
			}
		});
		
		//connect
		login.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(txtUsername.getText().trim().isEmpty())
					return;
				
				if (confirmPort() == false)
					return;
				else
					portConfirm = true;
				
				if (portConfirm == true) 
				{
					try
					{
						InetAddress address = InetAddress.getByName("127.0.0.1");
						socket = new Socket(address, portNum);
						
						out = new ObjectOutputStream(socket.getOutputStream());
						
						in = new ObjectInputStream(socket.getInputStream());

						name = txtUsername.getText().trim();
						out.writeObject(name);
						out.flush();

						connect = true;

						lblStatus.setText("Status: Connected");

						txtUsername.setEnabled(false);
						login.setEnabled(false);

						input.requestFocus();

						new ReadMessages().start();
					}
					catch(IOException ex)
					{
						ex.printStackTrace();

						lblStatus.setText("Status: Failed");
					}
				}
				else
					return;
			}
		});
	}

	private void loadPort()
	{
		portFileExisted = portFile.exists();
		portNum = DEFAULT_PORT;

		if(!portFileExisted)
		{
			savePort();
		}

		try(BufferedReader reader = new BufferedReader(new FileReader(portFile)))
		{
			String savedPort = reader.readLine();
			int loadedPort = Integer.parseInt(savedPort);
			if(loadedPort < 1 || loadedPort > 65535)
			{
				throw new NumberFormatException();
			}
			portNum = loadedPort;
		}
		catch(IOException | NumberFormatException e)
		{
			portNum = DEFAULT_PORT;
			savePort();
		}

		txtPort.setText(String.valueOf(portNum));
	}

	private void savePort()
	{
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(portFile)))
		{
			writer.write(String.valueOf(portNum));
		}
		catch(IOException e)
		{
			System.out.println("Unable to save port number.");
		}
	}

	private boolean confirmPort()
	{
		try
		{
			int enteredPort = Integer.parseInt(txtPort.getText().trim());
			if(enteredPort < 1 || enteredPort > 65535)
			{
				throw new NumberFormatException();
			}

			int portResponse = JOptionPane.showConfirmDialog(this,
					"Use port " + enteredPort + "?", "Confirm Port Number",
					JOptionPane.YES_NO_OPTION);
			if(portResponse == JOptionPane.YES_OPTION)
			{
				boolean portChanged = portNum != enteredPort;
				portNum = enteredPort;
				if(portChanged)
				{
					savePort();
				}
				System.out.println("this is true inside if block.");
				return true;
			}
			else
			{
				txtPort.setText(String.valueOf(portNum));
				System.out.println("this is false inside else block.");
				return false;
			}
		}
		catch(NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, "Enter a port number from 1 to 65535.",
					"Invalid Port", JOptionPane.ERROR_MESSAGE);
			txtPort.setText(String.valueOf(portNum));
		}
		System.out.println("this is false at bottom.");
		return false;
	}
	//read messages thread
	class ReadMessages extends Thread
	{
		@Override
		public void run()
		{
			while(go)
			{
				try
				{
					Object obj = in.readObject();

					if(obj instanceof String)
					{
						String message = obj.toString();
						if(message.startsWith("USERS:"))
						{
							userListModel.clear();
							String usernames = message.substring("USERS:".length());
							if(!usernames.isEmpty())
							{
								for(String username : usernames.split(","))
								{
									userListModel.addElement(username);
								}
							}
						}
						else
						{
							output.append(message + "\n");
						}
					}
				}
				catch(Exception e)
				{
				}
			}
		}
	}

	public static void main(String[] args)
	{
		new TCPClient();
	}

	@Override
	public void windowActivated(WindowEvent arg0)
	{
	}

	@Override
	public void windowClosed(WindowEvent arg0)
	{
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
	}

	@Override
	public void windowIconified(WindowEvent arg0)
	{
	}

	@Override
	public void windowOpened(WindowEvent arg0)
	{
	}
}
