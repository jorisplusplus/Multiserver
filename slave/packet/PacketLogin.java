package joris.multiserver.slave.packet;

import joris.multiserver.common.Packet;
import joris.multiserver.jexxus.common.Connection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

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

	}
}
