package joris.multiserver.master.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import joris.multiserver.master.InstanceServer;
import joris.multiserver.master.MSM;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class InstancesCommand extends CommandBase {

	@Override
	public List getCommandAliases() {
		List list = new ArrayList();
		list.add("instance");
		return list;
	}

	@Override
	public String getCommandName() {
		return "instances";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/instances <Stats>";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] tag) {
		if (tag.length == 1) {
			if (tag[0].equals("stats") || tag[0].equals("stat")) {
				sender.addChatMessage(new ChatComponentText("List of instances: "));
				Iterator it = MSM.Instances.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, InstanceServer> entry = (Map.Entry) it.next();
					sender.addChatMessage(new ChatComponentText(entry.getValue().getString()));
				}
			}
		} else if (tag.length > 1) {

		}

	}

}
