package joris.multiserver.common.network;

//import net.minecraft.client.gui.GuiMainMenu;
//import net.minecraft.client.multiplayer.ServerData;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class SwitchMessage implements IMessage {

	public static class Handler implements IMessageHandler<SwitchMessage, IMessage> {

		@Override
		public IMessage onMessage(SwitchMessage message, MessageContext ctx) {
			return null;
		}

	}

	private String	IP;

	public SwitchMessage() {
	}

	public SwitchMessage(String Address) {
		this.IP = Address;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.IP = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.IP);

	}

}
