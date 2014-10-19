package joris.multiserver.slave.packet;

import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.slave.MSS;
import joris.multiserver.common.Packet;
import joris.multiserver.common.SaveHelper;
import joris.multiserver.slave.MSS;
import net.minecraft.nbt.NBTTagCompound;

public class PacketPlayerdata extends Packet {

	private NBTTagCompound	player;
	private String			uuid;
	private String			relay;

	public PacketPlayerdata(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketPlayerdata(NBTTagCompound player, String uuid, String relay) {
		super(null);
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
		this.player = (NBTTagCompound) tag.getTag("player");
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
		NBTTagCompound save = MSS.Saver.readPlayerData(this.uuid);
		if (save != null) {
			for (Object key : this.player.func_150296_c()) {
				if (save.hasKey((String) key)) {
					save.removeTag((String) key);
				}
				save.setTag((String) key, this.player.getTag((String) key));
			}
			MSS.Saver.storePlayerData(this.uuid, save);
		} else {
			MSS.Injectionlist.put(this.uuid, this.player);
		}
		this.sendReply(new PacketSendplayer(this.uuid));
	}
}