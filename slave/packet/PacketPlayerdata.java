package joris.multiserver.slave.packet;

import java.util.List;

import joris.multiserver.common.RelayblePacket;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.slave.MSS;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

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
		EntityPlayer livePlayer = playerOnline(this.uuid);
		if(livePlayer != null) {
			NBTTagCompound source = new NBTTagCompound();
			livePlayer.writeToNBT(source);
			for (Object key : this.player.func_150296_c()) {
				if (source.hasKey((String) key)) {
					source.removeTag((String) key);
				}
				source.setTag((String) key, this.player.getTag((String) key));
			}
			livePlayer.readFromNBT(source);
		} else {
			MSS.Injectionlist.put(this.uuid, this.player);
			this.sendReply(new PacketSendplayer(this.uuid, this.senderName));
		}
	}
	
	private EntityPlayer playerOnline(String uuid) {
		List<EntityPlayer> playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayer player : playerList) {
			if (player.getUniqueID().toString().equals(this.uuid)) {
				return player;
			}
		}
		return null;
	}
	
}