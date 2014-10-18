package joris.multiserver.packet;

import jexxus.common.Connection;
import joris.multiserver.MultiServerSlave;
import joris.multiserver.SaveHelper;
import net.minecraft.nbt.NBTTagCompound;

public class PacketPlayerdata extends Packet {

	private NBTTagCompound	player;
	private String			uuid;

	public PacketPlayerdata(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketPlayerdata(NBTTagCompound player, String uuid) {
		super(null);
		this.uuid = uuid;
		this.player = player;
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
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setString("uuid", this.uuid);
		tag.setTag("player", this.player);
	}

	@Override
	public void handle() {
		NBTTagCompound save = SaveHelper.readPlayerData(this.uuid);
		if (save != null) {
			for (Object key : this.player.func_150296_c()) {
				if (save.hasKey((String) key)) {
					save.removeTag((String) key);
				}
				save.setTag((String) key, this.player.getTag((String) key));
			}
			SaveHelper.storePlayerData(this.uuid, save);
		} else {
			MultiServerSlave.Injectionlist.put(this.uuid, this.player);
		}
		this.sendReply(new PacketSendplayer(this.uuid));
	}
}