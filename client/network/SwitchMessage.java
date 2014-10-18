package joris.multiserver.network;

import java.util.Timer;
import java.util.TimerTask;

import joris.multiserver.multiServerClient;

import com.google.common.net.HostAndPort;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreenServerList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class SwitchMessage implements IMessage{
	
	private String IP;
	
	public SwitchMessage() {}
	
	public SwitchMessage(String Address) {
		this.IP = Address;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		IP = ByteBufUtils.readUTF8String(buf);		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, IP);
		
	}
	
	public static class Handler implements IMessageHandler<SwitchMessage, IMessage> {		

		@Override
		public IMessage onMessage(SwitchMessage message, MessageContext ctx) {
			System.out.println("Received");
			if(ctx.side == Side.CLIENT) {
				multiServerClient.schedule = message.IP;
			}
			return null;
		}
		
	}

}
