package joris.multiserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.server.ServerConnection;
import joris.multiserver.packet.Packet;
import joris.multiserver.packet.PacketRegistry;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class TCPListener implements ConnectionListener {

	@Override
	public void connectionBroken(Connection broken, boolean forced) {

	}

	/**
	 * Received byte array convert it back to an HashMap and handle the contents
	 */
	@Override
	public void receive(byte[] data, Connection from) {
		// Parse byte array to Map
		ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
		try {
			NBTTagCompound tag = CompressedStreamTools.readCompressed(byteIn);
			Packet packet = PacketRegistry.createPacket(from, tag);
			if (packet != null) {
				packet.handle();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void clientConnected(ServerConnection conn) {

	}
}