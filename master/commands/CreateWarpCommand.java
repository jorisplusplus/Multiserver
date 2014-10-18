package joris.multiserver.commands;

import joris.multiserver.MSM;
import joris.multiserver.SaveHelper;
import joris.multiserver.Waypoint;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

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
			} else if (para.length == 5) {

			} else {

			}
		}
	}

}
