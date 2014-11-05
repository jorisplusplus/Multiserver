package joris.multiserver.client.network;

import org.apache.logging.log4j.core.helpers.Loader;

import net.minecraft.client.Minecraft;
import io.netty.buffer.ByteBuf;
import joris.multiserver.client.multiServerClient;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class CheckMod implements IMessage{
	
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

	
	public static class Handler implements IMessageHandler<CheckMod, IMessage> {		

		@Override
		public IMessage onMessage(CheckMod message, MessageContext ctx) {
			multiServerClient.network.sendToServer(new CheckMod(Minecraft.getMinecraft().getSession().getPlayerID()));
			return null;
		}
		
	}
}