package joris.multiserver.master.packet;

import java.util.HashMap;

import joris.multiserver.jexxus.common.Connection;
import joris.multiserver.master.InstanceServer;
import joris.multiserver.master.MSM;
import joris.multiserver.common.Packet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.Level;

public class PacketLogin extends Packet {

	private String	password;
	private String	name;
	private String	Details;

	public PacketLogin(Connection conn, NBTTagCompound tag) {
		super(conn);
		this.loadFromNBT(tag);
	}

	public PacketLogin(String pass, String name, String Details) {
		super(null);
		this.password = pass;
		this.name = name;
		this.Details = Details;
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
		this.Details = tag.getString("Details");
	}

	@Override
	public void safeToNBT(NBTTagCompound tag) {
		super.safeToNBT(tag);
		tag.setString("name", this.name);
		tag.setString("pass", this.password);
		tag.setString("Details", this.Details);;
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
			this.sendReply(new PacketConnected(true));
			MSM.logger.log(Level.INFO, "Connection verified.");
			this.sender.verified = true;
			instance.connection = this.sender;
			instance.Details = this.Details;
		} else {
			Reply.put("Data", false);
			this.sendReply(new PacketConnected(false));
			MSM.logger.log(Level.INFO, "Connection denied.");
			MSM.serverTcp.remove(this.sender);
		}
	}
}
