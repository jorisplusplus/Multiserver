package joris.multiserver.slave.packet;

import joris.multiserver.common.Packet;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.slave.MSS;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.logging.log4j.Level;

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
		if (this.connected) {
			MSS.logger.log(Level.INFO, "Connected to Master.");
			MSS.ServerPort = this.port;
			MSS.waypoints = this.waypoints;
		} else {
			MSS.logger.log(Level.INFO, "Auth failed.");
		}
	}

}
