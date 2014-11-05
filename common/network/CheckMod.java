package joris.multiserver.common.network;

import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class CheckMod implements IMessage {
	public static HashMap <String, Boolean>PlayerList = new HashMap();
	
	public static Boolean PlayerHasMod(String playername) {
		if(PlayerList.containsKey(playername)) {
			return PlayerList.get(playername);
		}
		return false;
	}
	
	public static class Handler implements IMessageHandler<CheckMod, IMessage> {

		@Override
		public IMessage onMessage(CheckMod message, MessageContext ctx) {
			PlayerList.put(message.name, true);
			return null;
		}

	}

	private String	name;

	public CheckMod() {
	}

	public CheckMod(String name) {
		this.name = name;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.name);
	}

}