package joris.multiserver.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import joris.multiserver.InstanceServer;
import joris.multiserver.MSM;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class JoinCommand extends CommandBase {

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] list) {
		List listing = new ArrayList();
		Iterator it = MSM.Instances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			InstanceServer server = (InstanceServer) pairs.getValue();
			if (list.length > 0) {
				if (server.name.contains(list[0])) {
					listing.add(server.name);
				}
			} else {
				listing.add(server.name);
			}
			// it.remove(); // avoids a ConcurrentModificationException
		}

		return listing;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public String getCommandName() {
		return "join";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/join <instance name> [player]";
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] parameters) {
		EntityPlayerMP player = null;
		if (parameters.length == 1) {
			if (sender instanceof EntityPlayerMP) {
				player = (EntityPlayerMP) sender;
			}
		} else if (parameters.length == 2) {
			player = this.getPlayer(parameters[1]);
		} else {
			sender.addChatMessage(new ChatComponentText("Wrong usage"));
			return;
		}
		if (this.InstanceCheck(sender, parameters[0]) && (player != null)) {
			try {
				player.addChatMessage(new ChatComponentText("Joining instance..."));
				MSM.sendPlayerDataAndReconnect(MSM.Instances.get(parameters[0]), player);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public boolean InstanceCheck(ICommandSender sender, String name) {
		InstanceServer server = MSM.Instances.get(name);
		if (server == null) {
			sender.addChatMessage(new ChatComponentText("Instance not found"));
			return false;
		} else if (!server.isConnected()) {
			sender.addChatMessage(new ChatComponentText("Instance not live"));
			return false;
		}
		return true;
	}

	public EntityPlayerMP getPlayer(String name) {
		for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			EntityPlayerMP playermp = (EntityPlayerMP) player;
			if (playermp.getCommandSenderName().equalsIgnoreCase(name)) {
				return playermp;
			}
		}
		return null;
	}

}
