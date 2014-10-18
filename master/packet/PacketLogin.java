package joris.multiserver.packet;

import java.util.HashMap;

import jexxus.common.Connection;
import joris.multiserver.InstanceServer;
import joris.multiserver.MSM;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.Level;

public class PacketLogin extends Packet {

	private String	password;
	private String	name;
	private int		port;
	private String	IP;

	public PacketLogin(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketLogin(String pass, String name, String ip) {
		super(null);
		this.password = pass;
		this.name = name;
		this.IP = ip;
		this.port = MinecraftServer.getServer().getPort();
	}

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public void loadFromNBT(NBTTagCompound tag) {
		super.loadFromNBT(tag);
		this.password = tag.getString("pass");
		this.name = tag.getString("name");
		this.IP = tag.getString("ip");
		this.port = tag.getInteger("port");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setString("name", this.name);
		tag.setString("pass", this.password);
		tag.setString("ip", this.IP);
		tag.setInteger("port", this.port);
	}

	@Override
	public void handle() {
		Boolean legit = false;
		// Basic password and name check. To prevent hijacking
		// instances.
		// THIS IS NOT SECURE, EVERYTHING IS SEND CLEAR TEXT
		InstanceServer instance = MSM.Instances.get(this.name);
		legit = instance.password.equalsIgnoreCase(this.password);
		HashMap Reply = new HashMap();
		Reply.put("Command", "Login");
		if (legit) {
			this.sendReply(new PacketConnected(true, MinecraftServer.getServer().getPort()));
			MSM.logger.log(Level.INFO, "Connection verified.");
			this.sender.verified = true;
			instance.connection = this.sender;
			instance.Port = this.port;
			instance.IP = this.IP;
		} else {
			Reply.put("Data", false);
			this.sendReply(new PacketConnected(false, 0));
			MSM.logger.log(Level.INFO, "Connection denied.");
			MSM.serverTcp.remove(this.sender);
		}
	}
}
