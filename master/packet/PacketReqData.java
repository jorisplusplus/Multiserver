package joris.multiserver.master.packet;

import org.apache.logging.log4j.Level;

import net.minecraft.nbt.NBTTagCompound;
import joris.multiserver.common.Packet;
import joris.multiserver.common.SaveHelper;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.MSM;

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
			NBTTagCompound data = MSM.Saver.readPlayerData(this.uuid);
			if(data != null) {
				NBTTagCompound transfer = new NBTTagCompound();
				for (String key : MSM.Sync) {
					if (data.hasKey(key)) {
						transfer.setTag(key, data.getTag(key));
					} else {
						MSM.logger.log(Level.WARN, "Key not found: " + key);
					}
				}
				this.sendReply(new PacketPlayerdata(transfer, this.uuid, this.senderName));
			}
		}

	}