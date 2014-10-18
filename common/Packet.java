package joris.multiserver.common;

import joris.multiserver.jexxus.common.Connection;
import net.minecraft.nbt.NBTTagCompound;

public class Packet implements IPacket {

	protected Connection	sender;

	public Packet(Connection conn) {
		this.sender = conn;
	}

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {

	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		tag.setInteger("ID", this.getID());
	}

	@Override
	public void handle() {

	}

	public void sendReply(Packet packet) {
		this.sender.send(packet);
	}

}
