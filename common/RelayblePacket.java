package joris.multiserver.common;

import joris.multiserver.jexxus.common.Connection;
import net.minecraft.nbt.NBTTagCompound;

public class RelayblePacket extends Packet implements IRelayble {

	protected String	relay;

	public RelayblePacket(Connection conn, String relay) {
		super(conn);
		this.relay = relay;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.relay = tag.getString("relay");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setString("relay", this.relay);
	}

	@Override
	public boolean shouldRelay() {
		return !this.relay.equalsIgnoreCase(PacketRegistry.getName());
	}

	@Override
	public String getName() {
		return this.relay;
	}

}
