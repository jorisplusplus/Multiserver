package joris.multiserver.master.packet;

import joris.multiserver.common.Packet;
import joris.multiserver.jexxus.common.Connection;
import net.minecraft.nbt.NBTTagCompound;

public class PacketReqstats extends Packet {

	public PacketReqstats(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketReqstats() {
		super(null);
	}

	@Override
	public int getID() {
		return 3;
	}
}