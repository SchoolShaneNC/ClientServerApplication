package prog1415;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.*;

public class TCPClient extends JFrame implements WindowListener
{
	private static final long serialVersionUID = 1L;

	//networking objects
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	private boolean go = true;
	private boolean connect = false;
	private String name;

	//header components
	private JLabel lblUsername = new JLabel("Username:");
	private JTextField txtUsername = new JTextField(12);

	private JButton login = new JButton("Connect");

	private JLabel lblPort = new JLabel("Port: 8000");
	private JLabel lblStatus = new JLabel("Status: Disconnected");

	//user list
	private DefaultListModel<String> userListModel = new DefaultListModel<>();
	private JList<String> lstUsers = new JList<>(userListModel);

	//chat components
	private JTextArea output = new JTextArea("Ready...\n");
	private JTextField input = new JTextField();

	public TCPClient()
	{
		//header
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		headerPanel.add(lblUsername);
		headerPanel.add(txtUsername);
		headerPanel.add(login);
		headerPanel.add(lblPort);
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

		//connect
		login.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(txtUsername.getText().trim().isEmpty())
					return;

				try
				{
					InetAddress address = InetAddress.getByName("127.0.0.1");
					socket = new Socket(address, 8000);
					
					out = new ObjectOutputStream(socket.getOutputStream());

					in = new ObjectInputStream(socket.getInputStream());

					connect = true;

					name = txtUsername.getText().trim();

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
		});
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
						output.append(obj.toString() + "\n");
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