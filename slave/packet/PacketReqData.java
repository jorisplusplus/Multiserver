package joris.multiserver.slave.packet;

import net.minecraft.nbt.NBTTagCompound;
import joris.multiserver.common.Packet;
import joris.multiserver.jexxus.common.Connection;

public class PacketReqData extends Packet{

		private String	uuid;

		// Constructor used by the packet registry
		public PacketReqData(Connection conn, NBTTagCompound tag) {
			super(conn);
			this.loadFromNBT(tag);
		}

		public PacketReqData(String uuid) {
			super(null);
			this.uuid = uuid;
		}

		@Override
		public int getID() {
			return 9;
		}

		@Override
		public void loadFromNBT(NBTTagCompound tag) {
			super.loadFromNBT(tag);
			this.uuid = tag.getString("uuid");
		}

		@Override
		public void safeToNBT(NBTTagCompound tag) {
			super.safeToNBT(tag);
			tag.setString("uuid", this.uuid);
		}
		
		@Override
		public void handle() {
			
		}

	}