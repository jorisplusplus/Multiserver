package joris.multiserver.slave.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import joris.multiserver.common.Waypoint;
import joris.multiserver.slave.MSS;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class WarptoCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "warpto";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/warpto <waypoint name>";
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] list) {
		return new ArrayList(MSS.waypoints.func_150296_c());
	}

	@Override
	public void processCommand(ICommandSender sender, String[] para) {
		if (sender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) sender;
			if (para.length > 0) {
				if (MSS.waypoints.getTag(para[0]) != null) {
					Waypoint waypoint = new Waypoint((NBTTagCompound) MSS.waypoints.getTag(para[0]));
					if (waypoint.instanceName.equals(MSS.Name)) {
						// wayppoint on this server
						if (player.dimension != waypoint.dimension) {
							MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, waypoint.dimension);
						}
						player.playerNetServerHandler.setPlayerLocation(waypoint.x + 0.5, waypoint.y, waypoint.z + 0.5, player.rotationYaw, player.rotationPitch);
					} else {
						try {
							MSS.sendPlayerData(player, waypoint.travelData(), waypoint.instanceName);
							MSS.scheduleTransfer(player.getUniqueID().toString());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					sender.addChatMessage(new ChatComponentText("Waypoint not found"));
				}
			} else {
				sender.addChatMessage(new ChatComponentText("No waypoint given"));
			}
		} else {
			sender.addChatMessage(new ChatComponentText("Player only command"));
		}

	}
}