package joris.multiserver.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import joris.multiserver.MultiServerSlave;
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
		return "join - Go back to the master server";
	}

	@Override
	public List getCommandAliases() {
		List list = new ArrayList();
		list.add("return");
		return list;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] parameters) {
		if (MultiServerSlave.TCPClient.isConnected()) {
			if (sender instanceof EntityPlayerMP) {
				try {
					MultiServerSlave.sendPlayerDataAndReconnect((EntityPlayerMP) sender);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				sender.addChatMessage(new ChatComponentText("Player only"));
			}
		} else {
			sender.addChatMessage(new ChatComponentText("Master not live"));

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
