package joris.multiserver.master.commands;

import joris.multiserver.common.Waypoint;
import joris.multiserver.master.MSM;
import joris.multiserver.master.packet.PacketWaypoint;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class CreateWarpCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "createwarp";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/createwarp <warpname>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] para) {
		if (sender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) sender;
			if (para.length == 1) { // <name>
				Waypoint waypoint = new Waypoint(player, "master");
				MSM.waypoints.setTag(para[0], waypoint.storeToNBT());
				MSM.Saver.storeWaypoints(MSM.waypoints);
				MSM.Broadcast(new PacketWaypoint(para[0], waypoint.storeToNBT()));
				sender.addChatMessage(new ChatComponentText("Warp created."));
			}
		}
	}

}
