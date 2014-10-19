package joris.multiserver.common;

import joris.multiserver.jexxus.common.Connection;
import net.minecraft.nbt.NBTTagCompound;

public class Packet implements IPacket {

	protected Connection	sender;
	protected String		senderName;

	public Packet(Connection conn) {
		this.sender = conn;
		this.senderName = PacketRegistry.getName();
	}

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		this.senderName = tag.getString("senderName");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		tag.setInteger("ID", this.getID());
		tag.setString("senderName", this.senderName);
	}

	@Override
	public void handle() {

	}

	public void sendReply(Packet packet) {
		this.sender.send(packet);
	}

}
