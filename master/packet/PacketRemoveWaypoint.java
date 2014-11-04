package joris.multiserver.master.packet;

import joris.multiserver.common.Packet;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.MSM;
import net.minecraft.nbt.NBTTagCompound;

public class PacketRemoveWaypoint extends Packet {

	private String	name;

	// Constructor used by the packet registry
	public PacketRemoveWaypoint(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketRemoveWaypoint(String name) {
		super(null);
		this.name = name;
	}

	@Override
	public int getID() {
		return 8;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.name = tag.getString("name");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setString("name", this.name);
	}

	@Override
	public void handle() {
		if (MSM.waypoints.hasKey(this.name)) {
			MSM.waypoints.removeTag(this.name);
		}
		MSM.Broadcast(this, this.sender);
	}
}
