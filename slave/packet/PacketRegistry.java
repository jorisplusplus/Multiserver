package joris.multiserver.packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import jexxus.common.Connection;
import net.minecraft.nbt.NBTTagCompound;

public class PacketRegistry {

	public static HashMap<Integer, Class<? extends Packet>>	classes	= new HashMap<Integer, Class<? extends Packet>>();

	public static void register(Class<? extends Packet> classname, int id) {
		if (classes.get(id) != null) {
			throw new RuntimeException("Packet id already exists.");
		} else {
			classes.put(id, classname);
		}
	}

	public static Packet createPacket(Connection conn, NBTTagCompound tag) {
		Class classname = classes.get(tag.getInteger("ID"));
		if (classname != null) {
			try {
				Constructor constructor = classname.getConstructor(Connection.class, NBTTagCompound.class);
				return (Packet) constructor.newInstance(conn, tag);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
