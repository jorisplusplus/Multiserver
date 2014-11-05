package joris.multiserver.master;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import joris.multiserver.common.Packet;
import joris.multiserver.common.PacketRegistry;
import joris.multiserver.common.SaveHelper;
import joris.multiserver.common.network.CheckMod;
import joris.multiserver.common.network.SwitchMessage;
import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.jexxus.server.Server;
import joris.multiserver.master.commands.InstancesCommand;
import joris.multiserver.master.commands.JoinCommand;
import joris.multiserver.master.commands.WarpCommand;
import joris.multiserver.master.commands.WarptoCommand;
import joris.multiserver.master.packet.PacketConnected;
import joris.multiserver.master.packet.PacketLogin;
import joris.multiserver.master.packet.PacketPlayerdata;
import joris.multiserver.master.packet.PacketRemoveWaypoint;
import joris.multiserver.master.packet.PacketReqstats;
import joris.multiserver.master.packet.PacketSendplayer;
import joris.multiserver.master.packet.PacketStats;
import joris.multiserver.master.packet.PacketText;
import joris.multiserver.master.packet.PacketWaypoint;
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

@Mod(modid = MSM.MODID, name = MSM.MODID, version = MSM.VERSION, acceptableRemoteVersions = "*")
public class MSM {

	public static final String						MODID			= "MultiServer";
	public static final String						VERSION			= "1.0";
	public static String							ServerDetails;
	public static String							ServerIP;
	public static Logger							logger;
	public static Server							serverTcp;
	public static TCPListener						Listener;
	public static String[]							Sync;									// What nbt tags should be synced
	public static HashMap<String, InstanceServer>	Instances		= new HashMap();		// All instances (even not) connected
	public static HashMap<String, NBTTagCompound>	Injectionlist	= new HashMap();		// Data that should be injected on login player
	public static HashMap<String, Boolean>			Scheduled		= new HashMap();		// List of players that should be send when the server is ready
	public static SimpleNetworkWrapper				network;
	public static NBTTagCompound					waypoints		= new NBTTagCompound();
	public static int								PORT;
	public static int								TickDelay;
	public static SaveHelper						Saver;

	// The instance of that Forge uses.
	@Instance(value = MODID)
	public static MSM								instance;

	/**
	 * Load config file
	 *
	 * @param config
	 */
	private void loadConfig(Configuration config) {
		config.load();
		PORT = config.get(Configuration.CATEGORY_GENERAL, "Port", 25566).getInt();
		ServerIP = config.get(Configuration.CATEGORY_GENERAL, "Server_IP", "127.0.0.1").getString();
		TickDelay = config.getInt(Configuration.CATEGORY_GENERAL, "TicksBetweenUpdate", 1200, 20, 6000, "Update frequency of the instance stats");
		Sync = config.getStringList("Synclist", Configuration.CATEGORY_GENERAL, new String[] { "Inventory", "EnderItems" }, "What playerdata should be synced. NBT tag names");
		String[] configInstances = config.get(Configuration.CATEGORY_GENERAL, "Instances", new String[] { "name", "password" }, "Whitelist your instances here format: Name newline Password").getStringList();
		config.save();
		if ((configInstances.length % 2) == 0) {
			for (int i = 0; i < configInstances.length; i += 2) {
				Instances.put(configInstances[i], new InstanceServer(configInstances[i], configInstances[i + 1]));
			}
			logger.log(Level.INFO, "Loaded instances: " + Instances.size());
		} else {
			throw new RuntimeException("Uneven config instances. In short you fucked up.");
		}
	}

	/**
	 * Setup custom logger and pre init stuff
	 *
	 * @param event
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger("MultiServer");
		logger.log(Level.INFO, "Starting multiserver master...");
		network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
		network.registerMessage(SwitchMessage.Handler.class, SwitchMessage.class, 0, Side.SERVER);
		network.registerMessage(CheckMod.Handler.class, CheckMod.class, 1, Side.SERVER);
		this.loadConfig(new Configuration(event.getSuggestedConfigurationFile()));
		PacketRegistry.setName("master");
		PacketRegistry.register(PacketLogin.class, 0);
		PacketRegistry.register(PacketConnected.class, 1);
		PacketRegistry.register(PacketText.class, 2);
		PacketRegistry.register(PacketReqstats.class, 3);
		PacketRegistry.register(PacketPlayerdata.class, 4);
		PacketRegistry.register(PacketSendplayer.class, 5);
		PacketRegistry.register(PacketStats.class, 6);
		PacketRegistry.register(PacketWaypoint.class, 7);
		PacketRegistry.register(PacketRemoveWaypoint.class, 8);
	}

	/**
	 * Server is starting, should start tcp server and register
	 * commands/handlers
	 *
	 * @param event
	 */
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		MSM.Saver = new SaveHelper();
		waypoints = MSM.Saver.readWaypoints();
		logger.log(Level.INFO, "Starting tcp socket on " + PORT);
		Listener = new TCPListener();
		serverTcp = new Server(Listener, PORT, false);
		serverTcp.startServer();
		Events events = new Events();
		MinecraftForge.EVENT_BUS.register(events);
		FMLCommonHandler.instance().bus().register(events);
		event.registerServerCommand(new JoinCommand());
		event.registerServerCommand(new InstancesCommand());
		event.registerServerCommand(new WarptoCommand());
		event.registerServerCommand(new WarpCommand());
		ServerDetails = ServerIP + ":" + MinecraftServer.getServer().getPort();
	}

	/**
	 * Server is stopping. Disconnect all slave clients
	 *
	 * @param event
	 */
	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		MSM.Saver.storeWaypoints(waypoints);
		serverTcp.shutdown(true);
	}

	/**
	 * Find an instance based on its connection
	 *
	 * @param conn
	 * @return
	 */
	public static InstanceServer findInstance(Connection conn) {
		Iterator it = Instances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			if (((InstanceServer) entry.getValue()).connection == conn) {
				return (InstanceServer) entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Schedule player to transfer to a server name
	 *
	 * @param uniqueID
	 *            player UUID
	 * @param b
	 *            server name
	 */
	public static void scheduleTransfer(String uniqueID) {
		Scheduled.put(uniqueID, true);
	}

	public static void sendPlayerData(Connection conn, String name, EntityPlayerMP player, NBTTagCompound additional) {
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
		conn.send(new PacketPlayerdata(transfer, player.getUniqueID().toString(), name));
	}

	/**
	 * Copy all nbttags listed in the config file.
	 *
	 * @param server
	 *            Target server
	 * @param player
	 *            Target player
	 * @throws IOException
	 */
	public static void sendPlayerData(InstanceServer server, EntityPlayerMP player, NBTTagCompound additional) throws IOException {
		sendPlayerData(server.connection, server.name, player, additional);
	}

	/**
	 * Send player stats and marks this player for transfer to an instance.
	 *
	 * @param server
	 * @param player
	 * @throws IOException
	 */
	public static void sendPlayerDataAndReconnect(InstanceServer server, EntityPlayerMP player) throws IOException {
		sendPlayerData(server, player, null);
		scheduleTransfer(player.getUniqueID().toString());
	}

	/**
	 * Returns to what server the player should connect null if not and removes
	 * entry from list.
	 *
	 * @param uniqueID
	 * @return Returns to what server the player should connect null if not.
	 */
	public static Boolean shouldTransfer(String uniqueID) {
		Boolean target = Scheduled.get(uniqueID);
		if (target != null) {
			return target;
		}
		return false;
	}

	/**
	 * Broadcast a packet to all connected slaves.
	 *
	 * @param packet
	 */
	public static void Broadcast(Packet packet) {
		Iterator it = Instances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			InstanceServer server = (InstanceServer) entry.getValue();
			if (server.isConnected()) {
				server.connection.send(packet);
			}
		}
	}

	/**
	 * Broadcast a packet to all connected slaves with a exclusion.
	 *
	 * @param packet Broadcast this packet
	 * @param exlude dont send it to this connection
	 */
	public static void Broadcast(Packet packet, Connection exlude) {
		Iterator it = MSM.Instances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			InstanceServer server = (InstanceServer) entry.getValue();
			NBTTagCompound tag = new NBTTagCompound();
			packet.safeToNBT(tag);
			if (server.connection != exlude) {
				if (server.isConnected()) {
					server.connection.send(tag);
				}
			}
		}
	}
}