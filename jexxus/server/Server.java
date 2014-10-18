package jexxus.server;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import jexxus.common.Connection;
import jexxus.common.ConnectionListener;

/**
 * Acts as a server for incoming client connections. The server can send and
 * receive data from all clients who connect to this server.
 *
 * @author Jason
 *
 */
public class Server {

	private final ConnectionListener				listener;
	private ServerSocket							tcpSocket;
	private DatagramSocket							udpSocket;
	private boolean									running			= false;
	protected final int								tcpPort, udpPort;
	private final ArrayList<ServerConnection>		clients			= new ArrayList<ServerConnection>();
	private final HashMap<String, ServerConnection>	udpClients		= new HashMap<String, ServerConnection>();

	private final DatagramPacket					outgoingPacket	= new DatagramPacket(new byte[0], 0);

	/**
	 * Creates a new server.
	 *
	 * @param listener
	 *            The responder to special events such as receiving data.
	 * @param port
	 *            The port to listen for client connections on. [TCP]
	 */
	public Server(ConnectionListener listener, int port, boolean useSSL) {
		this(listener, port, -1, useSSL);
	}

	/**
	 * Creates a new server.<br>
	 * <br>
	 * Note: The server will not begin listening for connections until
	 * <code>startServer()</code> is called.
	 *
	 * @param listener
	 *            The responder to special events such as receiving data.
	 * @param tcpPort
	 *            The port to listen for TCP client connections on.
	 * @param udpPort
	 *            The port to listen for UDP client connections on. Use -1 if
	 *            you don't want to use any UDP.
	 */
	public Server(ConnectionListener listener, int tcpPort, int udpPort, boolean useSSL) {
		this.listener = listener;

		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		try {
			ServerSocketFactory socketFactory = useSSL ? SSLServerSocketFactory.getDefault() : ServerSocketFactory.getDefault();
			this.tcpSocket = socketFactory.createServerSocket(tcpPort);

			if (useSSL) {
				final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
				((SSLServerSocket) this.tcpSocket).setEnabledCipherSuites(enabledCipherSuites);
			}
		} catch (BindException e) {
			System.err.println("There is already a server bound to port " + tcpPort + " on this computer.");
			throw new RuntimeException(e);
		} catch (IOException e) {
			if (e.toString().contains("JVM_Bind")) {
				System.err.println("There is already a server bound to port " + tcpPort + " on this computer.");
			}
			throw new RuntimeException(e);
		}
		if (udpPort != -1) {
			try {
				this.udpSocket = new DatagramSocket(udpPort);
			} catch (SocketException e) {
				System.err.println("There was a problem starting the server's UDP socket on port " + udpPort);
				System.err.println(e.toString());
			}
		}

	}

	void connectionDied(ServerConnection conn, boolean forced) {
		synchronized (this.clients) {
			this.clients.remove(conn);
		}
		synchronized (this.udpClients) {
			this.clients.remove(conn.getIP() + conn.getUDPPort());
		}
		this.listener.connectionBroken(conn, forced);

	}

	public void remove(Connection broken) {
		if (broken != null) {
			if (broken.isConnected()) {
				broken.close();
			}
			synchronized (this.clients) {
				this.clients.remove(broken);
			}
		}
	}

	void sendUDP(byte[] data, ServerConnection serverConnection) {
		synchronized (this.outgoingPacket) {
			this.outgoingPacket.setData(data);
			this.outgoingPacket.setAddress(serverConnection.getAddress());
			this.outgoingPacket.setPort(serverConnection.getUDPPort());
			try {
				this.udpSocket.send(this.outgoingPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * After the server has shut down, no new client connections can be
	 * established.
	 *
	 * @param closeAllConnections
	 *            If this is true, all previously opened client connections will
	 *            be closed.
	 */
	public void shutdown(boolean closeAllConnections) {
		this.running = false;
		try {
			this.tcpSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (this.udpSocket != null) {
			this.udpSocket.close();
		}
		synchronized (this.clients) {
			for (ServerConnection sc : this.clients) {
				sc.exit();
			}
		}
	}

	/**
	 * After the server has started, it is open for accepting new client
	 * connections.
	 */
	public synchronized void startServer() {
		if (this.running) {
			System.err.println("Cannot start server when already running!");
			return;
		}
		this.running = true;
		this.startTCPConnectionListener();
		if (this.udpPort != -1) {
			this.startUDPListener();
		}
	}

	private void startTCPConnectionListener() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (Server.this.running) {
					try {
						Socket sock = Server.this.tcpSocket.accept();
						ServerConnection sc = new ServerConnection(Server.this, Server.this.listener, sock);
						Server.this.clients.add(sc);
						Server.this.listener.clientConnected(sc);
					} catch (IOException e) {
						if (Server.this.running) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
		});
		t.setName("Jexxus-TCPConnectionListener");
		t.start();
	}

	private void startUDPListener() {
		// Don't need this, no need in fixing it to use an arraylist

		// Thread t = new Thread(new Runnable() {
		// public void run() {
		// final int BUF_SIZE = 2048;
		// final DatagramPacket inputPacket = new DatagramPacket(new
		// byte[BUF_SIZE], BUF_SIZE);
		// while (true) {
		// try {
		// udpSocket.receive(inputPacket);
		// byte[] ret = Arrays.copyOf(inputPacket.getData(),
		// inputPacket.getLength());
		// String senderIP = inputPacket.getAddress().getHostAddress();
		// ServerConnection conn = udpClients.get(senderIP +
		// inputPacket.getPort());
		// if (conn == null) {
		// conn = clients.get(senderIP);
		// }
		// if (conn == null) {
		// System.err.println("Received UDP Packet from unknown source: " +
		// senderIP);
		// } else {
		// if (ret.length == 0) {
		// System.out.println("Set UDP Port: " + inputPacket.getPort());
		// if (conn.getUDPPort() != -1) {
		// // see if there is another connection without a UDP port set
		// for (ServerConnection sc : clients.values()) {
		// if (sc.getUDPPort() == -1) {
		// conn = sc;
		// break;
		// }
		// }
		// }
		// conn.setUDPPort(inputPacket.getPort());
		// udpClients.put(senderIP + inputPacket.getPort(), conn);
		// } else {
		// listener.receive(ret, conn);
		// }
		// }
		// } catch (IOException e) {
		// if (running) {
		// System.err.println("UDP Socket failed!");
		// running = false;
		// }
		// break;
		// }
		// }
		// }
		// });
		// t.start();
	}

}
