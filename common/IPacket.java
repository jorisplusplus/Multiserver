package joris.multiserver.packet;

import net.minecraft.nbt.NBTTagCompound;

public interface IPacket {

	public int getID();

	public void loadFromNBT(NBTTagCompound tag);

	public void safeToNBT(NBTTagCompound tag);

	public void handle();
}