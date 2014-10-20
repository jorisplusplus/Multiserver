package joris.multiserver.master.packet;

import net.minecraft.nbt.NBTTagCompound;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.MSM;
import joris.multiserver.common.Packet;

public class PacketWaypoint extends Packet {
	
	private NBTTagCompound waypoint;
	private String name;

	public PacketWaypoint(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketWaypoint(String name, NBTTagCompound waypoint) {
		super(null);
		this.name = name;
		this.waypoint = waypoint;
	}

	@Override
	public int getID() {
		return 7;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.name = tag.getString("name");
		this.waypoint = tag.getCompoundTag("waypoint");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setString("name", this.name);
		tag.setTag("waypoint", this.waypoint);
	}

	@Override
	public void handle() {
		System.out.println("test");
		MSM.waypoints.setTag(this.name, this.waypoint);
		MSM.Broadcast(this, this.sender);
		MSM.Saver.storeWaypoints(MSM.waypoints);
	}
}