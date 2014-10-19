package joris.multiserver.slave.packet;

import joris.multiserver.common.Packet;
import joris.multiserver.jexxus.common.Connection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

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

	}
}
