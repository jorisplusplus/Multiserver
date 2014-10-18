package jexxus.common;

import jexxus.server.ServerConnection;

public interface ConnectionListener {

	public void clientConnected(ServerConnection conn);

	public void connectionBroken(Connection broken, boolean forced);

	public void receive(byte[] data, Connection from);

}
