package joris.multiserver.slave;

import joris.multiserver.slave.packet.PacketPlayerdata;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class BackupPlayerdata {

	private NBTTagCompound data = new NBTTagCompound();
	private String uuid;
	
	public BackupPlayerdata(EntityPlayer player) {
		this.uuid = player.getUniqueID().toString();
		NBTTagCompound playerData = new NBTTagCompound();
		player.writeToNBT(playerData);
		for (String key : MSS.Sync) {
			if (playerData.hasKey(key)) {
				this.data.setTag(key, playerData.getTag(key));
			} else {
				MSS.logger.log(Level.WARN, "Key not found: " + key);
			}
		}
	}
	
	public void sendData() {
		MSS.TCPClient.send(new PacketPlayerdata(this.data, this.uuid, "master"));
	}
	
}
