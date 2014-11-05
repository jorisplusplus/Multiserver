package joris.multiserver.master;

import joris.multiserver.common.network.CheckMod;
import joris.multiserver.master.packet.PacketReqstats;
import joris.multiserver.master.packet.PacketText;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class Events {

	private int	Ticks	= 0;

	/**
	 * Periodically request stats from every server. Frequency defined in config
	 *
	 * @param event
	 */
	@SubscribeEvent
	public void handleTick(ServerTickEvent event) {
		this.Ticks++;
		if (this.Ticks == MSM.TickDelay) {
			MSM.Broadcast(new PacketReqstats());
		}
	}

	/**
	 * On chat event broadcast the chat to every connected slave server.
	 *
	 * @param event
	 */
	@SubscribeEvent
	public void ServerChatEvent(ServerChatEvent event) {
		MSM.Broadcast(new PacketText(event.component.getUnformattedText()));
	}

	/**
	 * Player logged in check if we should inject playerdata
	 *
	 * @param event
	 */
	@SubscribeEvent
	public void PlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
		MSM.network.sendTo(new CheckMod("Server"), (EntityPlayerMP) event.player);
		if (MSM.Injectionlist.containsKey(event.player.getUniqueID().toString())) {
			NBTTagCompound data = MSM.Injectionlist.get((event.player.getUniqueID().toString()));
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
}
