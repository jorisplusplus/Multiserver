package joris.multiserver.master;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import joris.multiserver.common.IRelayble;
import joris.multiserver.common.Packet;
import joris.multiserver.common.PacketRegistry;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.jexxus.common.ConnectionListener;
import joris.multiserver.jexxus.server.ServerConnection;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.logging.log4j.Level;

public class TCPListener implements ConnectionListener {

	/**
	 * Client is connecting, log it and start a disconnector to automatically
	 * disconnect it if it doens't login
	 */
	@Override
	public void clientConnected(ServerConnection conn) {
		MSM.logger.log(Level.INFO, "Slave connection started..., " + conn.getIP());
		new Disconnector(conn);
	}

	/**
	 * Client disconnected or connection was lost
	 */
	@Override
	public void connectionBroken(Connection broken, boolean forced) {
		MSM.logger.log(Level.WARN, "Slave connection lost.");
	}

	/**
	 * Received byte array convert it back and handle the packet
	 */
	@Override
	public void receive(byte[] data, Connection from) {
		ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
		try {
			NBTTagCompound tag = CompressedStreamTools.readCompressed(byteIn);
			Packet packet = PacketRegistry.createPacket(from, tag);
			if (packet != null) {
				if (packet.getID() == 0) { // Always handle login packet
					packet.handle();
				} else if (from.verified) { // Only handle non login packets
					// when connection is verified.
					if (packet instanceof IRelayble) {
						// System.out.println(((IRelayble) packet).getName());
						if (((IRelayble) packet).shouldRelay()) { // Should
							// relay the
							// message
							// not
							// handle it
							InstanceServer server = MSM.Instances.get(((IRelayble) packet).getName());
							if (server != null) {
								server.connection.send(packet);
							}
							return;
						}
					}
					packet.handle();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}