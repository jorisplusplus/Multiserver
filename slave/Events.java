package joris.multiserver;

import java.io.IOException;

import joris.multiserver.packet.PacketStats;
import joris.multiserver.packet.PacketText;
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
		if (!MultiServerSlave.TCPClient.isConnected()) {
			// TCP not connected
			this.ticks++;
			if (this.ticks == 1200) {
				this.ticks = 0;
				MultiServerSlave.connect();
			} else {
				this.ticks = 0;
			}
		}
	}

	@SubscribeEvent
	public void ServerChatEvent(ServerChatEvent event) {
		MultiServerSlave.TCPClient.send(new PacketText(event.component.getUnformattedText()));
	}

	@SubscribeEvent
	public void PlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
		MultiServerSlave.TCPClient.send(new PacketStats(MinecraftServer.getServer().getCurrentPlayerCount()));
		// Injection data part
		if (MultiServerSlave.Injectionlist.containsKey(event.player.getUniqueID().toString())) {
			NBTTagCompound data = MultiServerSlave.Injectionlist.get((event.player.getUniqueID().toString()));
			NBTTagCompound player = new NBTTagCompound();
			event.player.writeToNBT(player);
			for (Object key : data.func_150296_c()) {
				if (player.hasKey((String) key)) {
					player.removeTag((String) key);
				}
				player.setTag((String) key, data.getTag((String) key));
			}
			event.player.readFromNBT(player);
		}
	}

	@SubscribeEvent
	public void PlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
		MultiServerSlave.TCPClient.send(new PacketStats(MinecraftServer.getServer().getCurrentPlayerCount()));
		try {
			MultiServerSlave.sendPlayerData((EntityPlayerMP) event.player, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
