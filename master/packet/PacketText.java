package joris.multiserver.master.packet;

import java.util.Iterator;
import java.util.Map;
import joris.multiserver.common.Packet;

import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.InstanceServer;
import joris.multiserver.master.MSM;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class PacketText extends Packet {

	private String	text;

	public PacketText(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketText(String text) {
		super(null);
		this.text = text;
	}

	@Override
	public int getID() {
		return 2;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.text = tag.getString("text");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setString("text", this.text);
	}

	@Override
	public void handle() {
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(this.text));
		Iterator it = MSM.Instances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			InstanceServer server = (InstanceServer) entry.getValue();
			NBTTagCompound tag = new NBTTagCompound();
			this.safeToNBT(tag);
			if (server.connection != this.sender) {
				if (server.isConnected()) {
					server.connection.send(tag);
				}
			}
		}
	}
}
