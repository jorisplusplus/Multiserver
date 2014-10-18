package joris.multiserver.packet;

import java.util.List;

import jexxus.common.Connection;
import joris.multiserver.MultiServerSlave;
import joris.multiserver.network.SwitchMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class PacketSendplayer extends Packet {

	private String	uuid;

	public PacketSendplayer(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketSendplayer(String uuid) {
		super(null);
		this.uuid = uuid;
	}

	@Override
	public int getID() {
		return 5;
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
		String target = MultiServerSlave.shouldTransfer(this.uuid);
		if (target != null) {
			List<EntityPlayer> playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			for (EntityPlayer player : playerList) {
				if (player.getUniqueID().toString().equals(this.uuid)) {
					MultiServerSlave.network.sendTo(new SwitchMessage(target), (EntityPlayerMP) player);
				}
			}
		}

	}

}
