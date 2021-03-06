package joris.multiserver.slave.packet;

import java.util.ArrayList;

import joris.multiserver.common.Packet;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.slave.BackupPlayerdata;
import joris.multiserver.slave.Events;
import joris.multiserver.slave.MSS;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import org.apache.logging.log4j.Level;

public class PacketConnected extends Packet {

	private boolean			connected;
	private NBTTagCompound	waypoints;
	private NBTTagCompound	instances;

	public PacketConnected(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketConnected(Boolean connected) {
		super(null);
		this.connected = connected;
	}

	@Override
	public int getID() {
		return 1;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.connected = tag.getBoolean("connected");
		this.waypoints = tag.getCompoundTag("waypoints");
		this.instances = tag.getCompoundTag("instances");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setBoolean("connected", this.connected);
		tag.setTag("waypoints", this.waypoints);
		tag.setTag("instances", this.instances);
	}

	@Override
	public void handle() {
		if (this.connected) {
			MSS.logger.log(Level.INFO, "Connected to Master.");
			MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText("[Server] Master server connection established."));
			MSS.waypoints = this.waypoints;
			MSS.instances = this.instances;
			if(Events.disconnectedPLayers.size() > 0){
				MSS.logger.log(Level.INFO, "Sending logged out playerdata to master server");
				for(BackupPlayerdata playerData : Events.disconnectedPLayers) {
					playerData.sendData();					
				}
				Events.disconnectedPLayers.clear(); //All data send, clear list
			}
		} else {
			MSS.logger.log(Level.INFO, "Auth failed.");
		}
	}

}
