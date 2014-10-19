package joris.multiserver.master.packet;

import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.MSM;
import joris.multiserver.common.Packet;
import net.minecraft.nbt.NBTTagCompound;

public class PacketConnected extends Packet {

	private boolean	connected;
	private Integer	port;
	private NBTTagCompound waypoints;

	public PacketConnected(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketConnected(Boolean connected, Integer port) {
		super(null);
		this.connected = connected;
		this.port = port;
		this.waypoints = MSM.waypoints;
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
		this.waypoints = tag.getCompoundTag("waypoints");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setBoolean("connected", this.connected);
		tag.setInteger("port", this.port);
		tag.setTag("waypoints", this.waypoints);
	}

	@Override
	public void handle() {

	}

}
