package joris.multiserver.master.packet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.InstanceServer;
import joris.multiserver.master.MSM;
import joris.multiserver.common.Packet;
import net.minecraft.nbt.NBTTagCompound;

public class PacketConnected extends Packet {

	private boolean	connected;
	private NBTTagCompound waypoints;
	private NBTTagCompound instances = new NBTTagCompound();

	public PacketConnected(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketConnected(Boolean connected) {
		super(null);
		this.connected = connected;
		this.waypoints = MSM.waypoints;
		   Iterator it = MSM.Instances.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        System.out.println(pairs.getKey() + " = " + pairs.getValue());
		        this.instances.setString((String) pairs.getKey(), ((InstanceServer) pairs.getValue()).Details);
		    }
	}

	@Override
	public int getID() {
		return 1;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.connected = tag.getBoolean("connected");
		this.waypoints = tag.getCompoundTag("waypoints");
		this.instances = tag.getCompoundTag("instances");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setBoolean("connected", this.connected);
		tag.setTag("waypoints", this.waypoints);
		tag.setTag("instances", this.instances);
	}

	@Override
	public void handle() {

	}

}
