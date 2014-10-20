package joris.multiserver.master.packet;

import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.InstanceServer;
import joris.multiserver.master.MSM;
import joris.multiserver.common.IRelayble;
import joris.multiserver.common.PacketRegistry;
import joris.multiserver.common.RelayblePacket;
import joris.multiserver.common.SaveHelper;
import joris.multiserver.common.Packet;
import net.minecraft.nbt.NBTTagCompound;

public class PacketPlayerdata extends RelayblePacket {

	private NBTTagCompound	player;
	private String			uuid;

	public PacketPlayerdata(Connection conn, NBTTagCompound tag) {
		super(conn, null);
		this.loadFromNBT(tag);
	}

	public PacketPlayerdata(NBTTagCompound player, String uuid, String relay) {
		super(null, relay);
		this.uuid = uuid;
		this.player = player;
		this.relay = relay;
	}

	@Override
	public int getID() {
		return 4;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.uuid = tag.getString("uuid");
		this.player = tag.getCompoundTag("player");
		this.relay = tag.getString("relay");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setString("uuid", this.uuid);
		tag.setTag("player", this.player);
		tag.setString("relay", this.relay);
	}

	@Override
	public void handle() {
		NBTTagCompound save = MSM.Saver.readPlayerData(this.uuid);
		if (save != null) {
			for (Object key : this.player.func_150296_c()) {
				if (save.hasKey((String) key)) {
					save.removeTag((String) key);
				}
				save.setTag((String) key, this.player.getTag((String) key));
			}
			MSM.Saver.storePlayerData(this.uuid, save);
		} else {
			MSM.Injectionlist.put(this.uuid, this.player);
		}
		this.sendReply(new PacketSendplayer(this.uuid, this.senderName));
	}
}