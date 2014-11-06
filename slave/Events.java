package joris.multiserver.slave;

import java.io.IOException;

import joris.multiserver.common.network.CheckMod;
import joris.multiserver.slave.packet.PacketReqData;
import joris.multiserver.slave.packet.PacketStats;
import joris.multiserver.slave.packet.PacketText;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.ServerChatEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class Events {
	Integer	ticks	= 0;

	@SubscribeEvent
	public void handleTick(TickEvent.ServerTickEvent clientTickEvent) {
		if (!MSS.TCPClient.isConnected()) {
			// TCP not connected
			this.ticks++;
			if (this.ticks == 1200) {
				this.ticks = 0;
				MSS.connect();
			} else {
				this.ticks = 0;
			}
		}
	}

	@SubscribeEvent
	public void ServerChatEvent(ServerChatEvent event) {
		MSS.TCPClient.send(new PacketText(event.component.getUnformattedText()));
	}

	@SubscribeEvent
	public void PlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
		MSS.network.sendTo(new CheckMod("Server"), (EntityPlayerMP) event.player);
		MSS.TCPClient.send(new PacketStats(MinecraftServer.getServer().getCurrentPlayerCount()));
		// Injection data part
		if (MSS.Injectionlist.containsKey(event.player.getUniqueID().toString())) {
			NBTTagCompound data = MSS.Injectionlist.get((event.player.getUniqueID().toString()));
			NBTTagCompound player = new NBTTagCompound();
			event.player.writeToNBT(player);
			for (Object key : data.func_150296_c()) {
				if (player.hasKey((String) key)) {
					player.removeTag((String) key);
				}
				player.setTag((String) key, data.getTag((String) key));
			}
			event.player.readFromNBT(player);
		} else {
			MSS.TCPClient.send(new PacketReqData(event.player.getUniqueID().toString()));
		}
	}

	@SubscribeEvent
	public void PlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
		MSS.TCPClient.send(new PacketStats(MinecraftServer.getServer().getCurrentPlayerCount()));
		try {
			MSS.sendPlayerData((EntityPlayerMP) event.player, null, "master");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
