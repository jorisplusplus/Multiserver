package joris.multiserver.common;

import joris.multiserver.jexxus.common.Connection;
import net.minecraft.nbt.NBTTagCompound;

public class DummyPacket extends Packet {

	// Constructor used by the packet registry
	public DummyPacket(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public DummyPacket() {
		super(null); // Always do this it sets the sendername.
	}

	@Override
	public int getID() {
		return 1;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
	}

	@Override
	public void handle() {

	}
}