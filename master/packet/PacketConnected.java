package joris.multiserver.packet;

import jexxus.common.Connection;
import net.minecraft.nbt.NBTTagCompound;

public class PacketConnected extends Packet {

	private boolean	connected;
	private Integer	port;

	public PacketConnected(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketConnected(Boolean connected, Integer port) {
		super(null);
		this.connected = connected;
		this.port = port;
	}

	@Override
	public int getID() {
		return 1;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.connected = tag.getBoolean("connected");
		this.port = tag.getInteger("port");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setBoolean("connected", this.connected);
		tag.setInteger("port", this.port);
	}

	@Override
	public void handle() {

	}

}
