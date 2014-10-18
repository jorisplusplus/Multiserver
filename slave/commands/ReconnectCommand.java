package joris.multiserver.commands;

import joris.multiserver.MultiServerSlave;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class ReconnectCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "reconnect";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		// TODO Auto-generated method stub
		return "reconnect - reconnect to the masterserver";
	}

	@Override
	public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
		if (MultiServerSlave.TCPClient.isConnected()) {
			MultiServerSlave.TCPClient.close();
		}
		MultiServerSlave.connect();
	}

	/**
	 * Return the required permission level for this command.
	 */
	@Override
	public int getRequiredPermissionLevel() {
		return 3;
	}

}
