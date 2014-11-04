package joris.multiserver.slave.commands;

import joris.multiserver.common.Waypoint;
import joris.multiserver.slave.MSS;
import joris.multiserver.slave.packet.PacketRemoveWaypoint;
import joris.multiserver.slave.packet.PacketWaypoint;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class WarpCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "warp";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/warp <create/remove> <warpname>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] para) {
		if (sender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) sender;
			if (para.length == 2) { // <name>
				if(para[0].equalsIgnoreCase("create")) {
					Waypoint waypoint = new Waypoint(player, "master");
					MSS.waypoints.setTag(para[1], waypoint.storeToNBT());
					MSS.TCPClient.send(new PacketWaypoint(para[1], waypoint.storeToNBT()));
					sender.addChatMessage(new ChatComponentText("Warp created."));
				} else if(para[0].equalsIgnoreCase("remove")) {
					if(MSS.waypoints.hasKey(para[1])) {
						MSS.waypoints.removeTag(para[1]);
						sender.addChatMessage(new ChatComponentText("Warp removed."));
						MSS.TCPClient.send(new PacketRemoveWaypoint(para[1]));
					} else {
						sender.addChatMessage(new ChatComponentText("Warp does not exist."));
					}
				}
			}
		}
	}
}