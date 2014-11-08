package joris.multiserver.slave;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import joris.multiserver.common.Packet;
import joris.multiserver.common.PacketRegistry;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.jexxus.common.ConnectionListener;
import joris.multiserver.jexxus.server.ServerConnection;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class TCPListener implements ConnectionListener {

	@Override
	public void connectionBroken(Connection broken, boolean forced) {
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText("[Server] Attention, master server went offline. Please stay connected until master server is back up."));
		new Reconnector();
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
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText("[Server]Master server connection established."));
	}
}