package joris.multiserver.master.packet;

import java.util.List;

import joris.multiserver.common.RelayblePacket;
import joris.multiserver.common.network.SwitchMessage;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.MSM;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class PacketSendplayer extends RelayblePacket {

	private String	uuid;
	private String	IP;

	public PacketSendplayer(Connection conn, NBTTagCompound tag) {
		super(conn, null);
		this.IP = MSM.ServerDetails;
		this.loadFromNBT(tag);
	}

	public PacketSendplayer(String uuid, String relay) {
		super(null, relay);
		this.uuid = uuid;
		this.IP = MSM.ServerDetails;
	}

	@Override
	public int getID() {
		return 5;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.uuid = tag.getString("uuid");
		this.IP = tag.getString("IP");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setString("uuid", this.uuid);
		tag.setString("IP", this.IP);
	}

	@Override
	public void handle() {
		if (MSM.shouldTransfer(this.uuid)) {
			List<EntityPlayer> playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			for (EntityPlayer player : playerList) {
				if (player.getUniqueID().toString().equals(this.uuid)) {
					MSM.network.sendTo(new SwitchMessage(this.IP), (EntityPlayerMP) player);
					return;
				}
			}
		}

	}
}
