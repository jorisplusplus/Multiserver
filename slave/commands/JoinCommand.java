package joris.multiserver.slave.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import joris.multiserver.slave.MSS;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class JoinCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "join";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "join [instance] - Jump to a instance or go to the master server.";
	}

	@Override
	public List getCommandAliases() {
		List list = new ArrayList();
		list.add("return");
		return list;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] para) {
		if (MSS.TCPClient.isConnected()) {
			if (sender instanceof EntityPlayerMP) {
				try {
					if(para.length > 0) {
						MSS.sendPlayerDataAndReconnect((EntityPlayerMP) sender, para[0]);
					} else {
						MSS.sendPlayerDataAndReconnect((EntityPlayerMP) sender, "master");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				sender.addChatMessage(new ChatComponentText("Player only"));
			}
		} else {
			sender.addChatMessage(new ChatComponentText("No connection to the master server"));
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] list) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}

}
