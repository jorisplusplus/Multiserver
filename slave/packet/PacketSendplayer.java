package joris.multiserver.slave.packet;

import java.util.List;

import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.slave.MSS;
import joris.multiserver.common.Packet;
import joris.multiserver.common.network.SwitchMessage;
import joris.multiserver.slave.MSS;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class PacketSendplayer extends Packet {

	private String	uuid;
	private String  IP;

	public PacketSendplayer(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.IP = MSS.ServerDetails;
		this.loadFromNBT(tag);
	}

	public PacketSendplayer(String uuid) {
		super(null);
		this.uuid = uuid;
		this.IP = MSS.ServerDetails;
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
		if (MSS.shouldTransfer(this.uuid)) {
			List<EntityPlayer> playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			for (EntityPlayer player : playerList) {
				if (player.getUniqueID().toString().equals(this.uuid)) {
					MSS.network.sendTo(new SwitchMessage(this.IP), (EntityPlayerMP) player);
					return;
				}
			}
		}

	}

}
