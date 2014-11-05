package joris.multiserver.client;

import java.util.ArrayList;

import joris.multiserver.client.network.CheckMod;
import joris.multiserver.client.network.SwitchMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = multiServerClient.MODID, name = multiServerClient.MODID, version = multiServerClient.VERSION)
public class multiServerClient
{
    public static final String MODID = "MultiServer";
    public static final String VERSION = "1.0";
    public static String schedule = null;
    public static Logger multiServerLogger;
    public static SimpleNetworkWrapper network;
    
    
    // The instance of your mod that Forge uses.
    @Instance(value = MODID)
    public static multiServerClient instance;

    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	multiServerLogger = LogManager.getLogger("MultiServer");
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    	network.registerMessage(SwitchMessage.Handler.class, SwitchMessage.class, 0, Side.CLIENT);
        network.registerMessage(CheckMod.Handler.class, CheckMod.class, 1, Side.CLIENT);
    	Handler handlers = new Handler();
        MinecraftForge.EVENT_BUS.register( handlers );
        FMLCommonHandler.instance().bus().register( handlers );
        multiServerLogger.log(Level.INFO, Minecraft.getMinecraft().getSession().getPlayerID());
    }
}
