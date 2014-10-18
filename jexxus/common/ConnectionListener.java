package joris.multiserver.jexxus.common;

import joris.multiserver.jexxus.server.ServerConnection;

public interface ConnectionListener {

	public void clientConnected(ServerConnection conn);

	public void connectionBroken(Connection broken, boolean forced);

	public void receive(byte[] data, Connection from);

}
