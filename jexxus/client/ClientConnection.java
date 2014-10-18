package joris.multiserver.jexxus.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.jexxus.common.ConnectionListener;
import joris.multiserver.jexxus.common.Delivery;
import joris.multiserver.common.Packet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Used to establish a connection to a server.
 *
 * @author Jason
 *
 */
public class ClientConnection extends Connection {

	private Socket			tcpSocket;
	private DatagramSocket	udpSocket;
	protected final String	serverAddress;
	protected final int		tcpPort, udpPort;
	private DatagramPacket	packet;
	private boolean			connected	= false;
	private InputStream		tcpInput;
	private OutputStream	tcpOutput;
	private final boolean	useSSL;

	/**
	 * Creates a new connection to a server. The connection is not ready for use
	 * until <code>connect()</code> is called.
	 *
	 * @param listener
	 *            The responder to special events such as receiving data.
	 * @param serverAddress
	 *            The IP address of the server to connect to.
	 * @param tcpPort
	 *            The port to connect to the server on.
	 */
	public ClientConnection(ConnectionListener listener, String serverAddress, int tcpPort, boolean useSSL) {
		this(listener, serverAddress, tcpPort, -1, useSSL);
	}

	/**
	 * Creates a new connection to a server. The connection is not ready for use
	 * until <code>connect()</code> is called.
	 *
	 * @param listener
	 *            The responder to special events such as receiving data.
	 * @param serverAddress
	 *            The IP address of the server to connect to.
	 * @param tcpPort
	 *            The port to send data using the TCP protocol.
	 * @param udpPort
	 *            The port to send data using the UDP protocol.
	 */
	public ClientConnection(ConnectionListener listener, String serverAddress, int tcpPort, int udpPort, boolean useSSL) {
		super(listener);

		this.listener = listener;
		this.serverAddress = serverAddress;
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.useSSL = useSSL;

		if (udpPort != -1) {
			try {
				this.packet = new DatagramPacket(new byte[0], 0, new InetSocketAddress(serverAddress, udpPort));
				this.udpSocket = new DatagramSocket();
			} catch (IOException e) {
				System.err.println("Problem initializing UDP on port " + udpPort);
				System.err.println(e.toString());
			}
		}
	}

	@Override
	public void close() {
		if (!this.connected) {
			System.err.println("Cannot close the connection when it is not connected.");
		} else {
			try {
				this.tcpSocket.close();
				this.tcpInput.close();
				this.tcpOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.connected = false;
		}
	}

	public synchronized void connect() throws IOException {
		this.connect(0);
	}

	/**
	 * Tries to open a connection to the server.
	 *
	 * @return true if the connection was successful, false otherwise.
	 */
	public synchronized void connect(int timeout) throws IOException {
		if (this.connected) {
			throw new IllegalStateException("Tried to connect after already connected!");
		}

		SocketFactory socketFactory = this.useSSL ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
		this.tcpSocket = socketFactory.createSocket();

		if (this.useSSL) {
			final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
			((SSLSocket) this.tcpSocket).setEnabledCipherSuites(enabledCipherSuites);
		}

		this.tcpSocket.connect(new InetSocketAddress(this.serverAddress, this.tcpPort), timeout);
		this.tcpInput = new BufferedInputStream(this.tcpSocket.getInputStream());
		this.tcpOutput = new BufferedOutputStream(this.tcpSocket.getOutputStream());

		this.startTCPListener();
		this.connected = true;
		if (this.udpPort != -1) {
			this.startUDPListener();
			this.send(new byte[0], Delivery.UNRELIABLE);
		}

	}

	@Override
	protected InputStream getTCPInputStream() {
		return this.tcpInput;
	}

	@Override
	protected OutputStream getTCPOutputStream() {
		return this.tcpOutput;
	}

	@Override
	public boolean isConnected() {
		return this.connected;
	}

	@Override
	public synchronized void send(byte[] data, Delivery deliveryType) {
		if (this.connected == false) {
			System.err.println("Cannot send message when not connected!");
			return;
		}

		if (deliveryType == Delivery.RELIABLE) {
			// send with TCP
			try {
				super.sendTCP(data);
			} catch (IOException e) {
				System.err.println("Error writing TCP data.");
				System.err.println(e.toString());
			}
		} else if (deliveryType == Delivery.UNRELIABLE) {
			if (this.udpPort == -1) {
				System.err.println("Cannot send Unreliable data unless a UDP port is specified.");
				return;
			}
			this.packet.setData(data);
			try {
				this.udpSocket.send(this.packet);
			} catch (IOException e) {
				System.err.println("Error writing UDP data.");
				System.err.println(e.toString());
			}
		}
	}

	@Override
	public void send(NBTTagCompound tag) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			CompressedStreamTools.writeCompressed(tag, stream);
			this.send(stream.toByteArray(), Delivery.RELIABLE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void send(Packet packet) {
		NBTTagCompound tag = new NBTTagCompound();
		packet.safeToNBT(tag);
		this.send(tag);
	}

	private void startTCPListener() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					byte[] ret;
					try {
						ret = ClientConnection.this.readTCP();
					} catch (IOException e) {
						if (ClientConnection.this.connected) {
							ClientConnection.this.connected = false;
							ClientConnection.this.listener.connectionBroken(ClientConnection.this, false);
						} else {
							ClientConnection.this.listener.connectionBroken(ClientConnection.this, true);
						}
						if (ClientConnection.this.udpSocket != null) {
							ClientConnection.this.udpSocket.close();
						}
						break;
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
					if (ret == null) {
						// the stream has ended
						if (ClientConnection.this.connected) {
							ClientConnection.this.connected = false;
							ClientConnection.this.listener.connectionBroken(ClientConnection.this, false);
						} else {
							ClientConnection.this.listener.connectionBroken(ClientConnection.this, true);
						}
						break;
					}
					try {
						ClientConnection.this.listener.receive(ret, ClientConnection.this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.setName("Jexxus-TCPSocketListener");
		t.start();
	}

	private void startUDPListener() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				final int BUF_SIZE = 2048;
				final DatagramPacket inputPacket = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
				while (true) {
					try {
						ClientConnection.this.udpSocket.receive(inputPacket);
						byte[] ret = Arrays.copyOf(inputPacket.getData(), inputPacket.getLength());
						ClientConnection.this.listener.receive(ret, ClientConnection.this);
					} catch (IOException e) {
						if (ClientConnection.this.connected) {
							ClientConnection.this.connected = false;
						}
						break;
					}
				}
			}
		});
		t.start();
	}
}
