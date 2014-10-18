package joris.multiserver.jexxus.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.jexxus.common.ConnectionListener;
import joris.multiserver.jexxus.common.Delivery;
import joris.multiserver.common.Packet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Represents a server's connection to a client.
 */
public class ServerConnection extends Connection {

	private final Server		controller;
	private final Socket		socket;
	private final OutputStream	tcpOutput;
	private final InputStream	tcpInput;
	private boolean				connected	= true;
	private final String		ip;
	private int					udpPort		= -1;

	ServerConnection(Server controller, ConnectionListener listener, Socket socket) throws IOException {
		super(listener);

		this.controller = controller;
		this.socket = socket;
		this.ip = socket.getInetAddress().getHostAddress();
		this.tcpOutput = new BufferedOutputStream(socket.getOutputStream());
		this.tcpInput = new BufferedInputStream(socket.getInputStream());

		this.startTCPListener();
	}

	@Override
	public synchronized void close() {
		if (!this.connected) {
			throw new RuntimeException("Cannot close the connection when it is not connected.");
		} else {
			try {
				this.socket.close();
				this.tcpInput.close();
				this.tcpOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.connected = false;
		}
	}

	/**
	 * Closes this connection to the client.
	 */
	public void exit() {
		this.connected = false;
		try {
			this.tcpInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.tcpOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	InetAddress getAddress() {
		return this.socket.getInetAddress();
	}

	/**
	 * @return The IP of this client.
	 */
	public String getIP() {
		return this.ip;
	}

	@Override
	protected InputStream getTCPInputStream() {
		return this.tcpInput;
	}

	@Override
	protected OutputStream getTCPOutputStream() {
		return this.tcpOutput;
	}

	int getUDPPort() {
		return this.udpPort;
	}

	@Override
	public boolean isConnected() {
		return this.connected;
	}

	@Override
	public synchronized void send(byte[] data, Delivery deliveryType) {
		if (this.connected == false) {
			throw new RuntimeException("Cannot send message when not connected!");
		}
		if (deliveryType == Delivery.RELIABLE) {
			// send with TCP
			try {
				this.sendTCP(data);
			} catch (IOException e) {
				System.err.println("Error writing TCP data.");
				System.err.println(e.toString());
			}
		} else if (deliveryType == Delivery.UNRELIABLE) {
			this.controller.sendUDP(data, this);
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

	void setUDPPort(int port) {
		this.udpPort = port;
	}

	private void startTCPListener() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					byte[] ret;
					try {
						ret = ServerConnection.this.readTCP();
					} catch (SocketException e) {
						if (ServerConnection.this.connected) {
							ServerConnection.this.connected = false;
							ServerConnection.this.controller.connectionDied(ServerConnection.this, false);
							ServerConnection.this.listener.connectionBroken(ServerConnection.this, false);
						} else {
							ServerConnection.this.controller.connectionDied(ServerConnection.this, true);
							ServerConnection.this.listener.connectionBroken(ServerConnection.this, true);
						}
						break;
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
					if (ret == null) {
						// the stream has ended
						if (ServerConnection.this.connected) {
							ServerConnection.this.connected = false;
							ServerConnection.this.controller.connectionDied(ServerConnection.this, false);
							ServerConnection.this.listener.connectionBroken(ServerConnection.this, false);
						} else {
							ServerConnection.this.controller.connectionDied(ServerConnection.this, true);
							ServerConnection.this.listener.connectionBroken(ServerConnection.this, true);
						}
						break;
					}
					try {
						ServerConnection.this.listener.receive(ret, ServerConnection.this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.setName("Jexxus-TCPSocketListener");
		t.start();
	}

	@Override
	public String toString() {
		return this.ip;
	}
}
