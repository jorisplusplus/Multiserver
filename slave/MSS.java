package joris.multiserver.slave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import joris.multiserver.jexxus.client.ClientConnection;
import joris.multiserver.slave.commands.CreateWarpCommand;
import joris.multiserver.slave.commands.JoinCommand;
import joris.multiserver.slave.commands.ReconnectCommand;
import joris.multiserver.slave.commands.WarptoCommand;
import joris.multiserver.common.network.SwitchMessage;
import joris.multiserver.slave.packet.PacketConnected;
import joris.multiserver.slave.packet.PacketLogin;
import joris.multiserver.slave.packet.PacketPlayerdata;
import joris.multiserver.common.PacketRegistry;
import joris.multiserver.common.SaveHelper;
import joris.multiserver.slave.packet.PacketReqstats;
import joris.multiserver.slave.packet.PacketSendplayer;
import joris.multiserver.slave.packet.PacketStats;
import joris.multiserver.slave.packet.PacketText;
import joris.multiserver.slave.packet.PacketWaypoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.server.FMLServerHandler;

@Mod(modid = MSS.MODID, name = MSS.MODID, version = MSS.VERSION, acceptableRemoteVersions = "*")
public class MSS {
	public static final String						MODID			= "MultiServer";
	public static final String						VERSION			= "1.0";
	public static Logger							logger;
	public static ClientConnection					TCPClient;
	public static TCPListener						Listener;
	public static SimpleNetworkWrapper				network;
	// MultiServer master port and ip
	public static Integer							PORT;
	public static String							IP;
	// Minecraft master port and ip
	public static String							ServerIP;
	public static String							ServerDetails;
	public static String							Name;
	public static String							Password;
	public static HashMap<String, NBTTagCompound>	Injectionlist	= new HashMap();
	public static HashMap<String, Boolean>			Scheduled		= new HashMap();
	public static ArrayList<String>					Sync			= new ArrayList();
	public static SaveHelper						Saver;	
	public static NBTTagCompound					waypoints;
	public static NBTTagCompound					instances;
	// The instance of your mod that Forge uses.
	@Instance(value = MODID)
	public static MSS					instance;
	

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger("MultiServer");
		logger.log(Level.INFO, "Starting multiserver master...");
		network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
		network.registerMessage(SwitchMessage.Handler.class, SwitchMessage.class, 0, Side.SERVER);
		this.loadConfig(new Configuration(event.getSuggestedConfigurationFile()));
		PacketRegistry.setName(Name);
		PacketRegistry.register(PacketLogin.class, 0);
		PacketRegistry.register(PacketConnected.class, 1);
		PacketRegistry.register(PacketText.class, 2);
		PacketRegistry.register(PacketReqstats.class, 3);
		PacketRegistry.register(PacketPlayerdata.class, 4);
		PacketRegistry.register(PacketSendplayer.class, 5);
		PacketRegistry.register(PacketStats.class, 6);
		PacketRegistry.register(PacketWaypoint.class, 7);
	}

	private void loadConfig(Configuration config) {
		config.load();
		PORT = config.get(Configuration.CATEGORY_GENERAL, "Port", 25566).getInt();
		IP = config.get(Configuration.CATEGORY_GENERAL, "Master_IP", "127.0.0.1").getString();
		ServerIP = config.get(Configuration.CATEGORY_GENERAL, "Server_IP", "127.0.0.1").getString();
		Name = config.get(Configuration.CATEGORY_GENERAL, "Name", "name").getString();
		Password = config.get(Configuration.CATEGORY_GENERAL, "Password", "password").getString();
		Sync = new ArrayList<String>(Arrays.asList(config.getStringList("Synclist", Configuration.CATEGORY_GENERAL, new String[] { "Inventory", "EnderItems" }, "What playerdata should be synced. NBT tag names")));
		config.save();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		this.Saver = new SaveHelper();
		logger = LogManager.getLogger("MultiServer");
		logger.log(Level.INFO, "Starting tcp on 25566");
		ServerDetails = ServerIP + ":" + MinecraftServer.getServer().getPort();
		Events events = new Events();
		MinecraftForge.EVENT_BUS.register(events);
		FMLCommonHandler.instance().bus().register(events);
		event.registerServerCommand(new JoinCommand());
		event.registerServerCommand(new ReconnectCommand());
		event.registerServerCommand(new WarptoCommand());
		event.registerServerCommand(new CreateWarpCommand());
		Listener = new TCPListener();
		TCPClient = new ClientConnection(Listener, IP, PORT, false);
		MSS.connect();
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		TCPClient.close();
	}

	public static void sendPlayerData(EntityPlayerMP player, NBTTagCompound additional, String instanceName) throws IOException {
		NBTTagCompound data = new NBTTagCompound();
		player.writeToNBT(data);
		NBTTagCompound transfer = new NBTTagCompound();
		for (String key : Sync) {
			if (data.hasKey(key)) {
				transfer.setTag(key, data.getTag(key));
			} else {
				logger.log(Level.WARN, "Key not found: " + key);
			}
		}
		if (additional != null) {
			for (Object key : additional.func_150296_c()) {
				if (transfer.hasKey((String) key)) {
					transfer.removeTag((String) key);
				}
				transfer.setTag((String) key, additional.getTag((String) key));
			}
		}
		TCPClient.send(new PacketPlayerdata(transfer, player.getUniqueID().toString(), instanceName));
	}

	public static void sendPlayerDataAndReconnect(EntityPlayerMP player, String instanceName) throws IOException {
		sendPlayerData(player, null, instanceName);
		scheduleTransfer(player.getUniqueID().toString());
	}

	public static void scheduleTransfer(String uniqueID) {
		Scheduled.put(uniqueID, true);
	}

	public static Boolean shouldTransfer(String uniqueID) {
		Boolean target = Scheduled.get(uniqueID);
		if(target != null) return target;
		return false;
	}

	public static void connect() {
		try {
			TCPClient.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (TCPClient.isConnected()) {
			logger.log(Level.INFO, "Logging in on master server.");
			TCPClient.send(new PacketLogin(Password, Name, ServerDetails));
		} else {
			logger.log(Level.INFO, "Can't connect to master server.");
		}
	}

}
