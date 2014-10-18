package joris.multiserver;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class Handler {

    @SubscribeEvent
    public void handleTick( TickEvent.ClientTickEvent clientTickEvent ) {

        if( multiServerClient.schedule != null )
        {
        	//Disconnect from current server
            Minecraft minecraft = Minecraft.getMinecraft();
            minecraft.theWorld.sendQuittingDisconnectingPacket();
            minecraft.loadWorld((WorldClient)null);
            minecraft.displayGuiScreen(new GuiConnecting(multiServerClient.schedule));
            multiServerClient.schedule = null;
        }
    }
}