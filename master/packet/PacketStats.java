package joris.multiserver.master.packet;

import joris.multiserver.common.Packet;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.InstanceServer;
import joris.multiserver.master.MSM;
import net.minecraft.nbt.NBTTagCompound;

public class PacketStats extends Packet {

	public Integer	players;

	public PacketStats(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketStats(Integer players) {
		super(null);
		this.players = players;
	}

	@Override
	public int getID() {
		return 6;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.players = tag.getInteger("players");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setInteger("players", this.players);
	}

	@Override
	public void handle() {
		InstanceServer instance = MSM.findInstance(this.sender);
		if (instance != null) {
			instance.numberPlayers = this.players;
		}
	}
}