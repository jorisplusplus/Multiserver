package joris.multiserver;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;

public class Waypoint {
	public int		dimension;
	public int		x;
	public int		y;
	public int		z;
	public String	instanceName;

	public Waypoint(int xCoord, int yCoord, int zCoord, int dim, String Instance) {
		this.x = xCoord;
		this.y = yCoord;
		this.z = zCoord;
		this.dimension = dim;
		this.instanceName = Instance;
	}

	public Waypoint(EntityPlayerMP player, String instance) {
		this.x = (int) Math.floor(player.posX);
		this.y = (int) Math.floor(player.posY);
		this.z = (int) Math.floor(player.posZ);
		this.instanceName = instance;
		this.dimension = player.dimension;
	}

	public Waypoint(NBTTagCompound tag) {
		this.loadFromNBT(tag);
	}

	public void loadFromNBT(NBTTagCompound tag) {
		this.x = tag.getInteger("x");
		this.y = tag.getInteger("y");
		this.z = tag.getInteger("z");
		this.dimension = tag.getInteger("dimension");
		this.instanceName = tag.getString("instance");
	}

	public NBTTagCompound storeToNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("x", this.x);
		tag.setInteger("y", this.y);
		tag.setInteger("z", this.z);
		tag.setInteger("dimension", this.dimension);
		tag.setString("instance", this.instanceName);
		return tag;
	}

	public NBTTagCompound travelData() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("Dimension", this.dimension);
		NBTTagList pos = new NBTTagList();
		NBTTagDouble posx = new NBTTagDouble(this.x + 0.5);
		NBTTagDouble posy = new NBTTagDouble(this.y + 0.5);
		NBTTagDouble posz = new NBTTagDouble(this.z + 0.5);
		pos.appendTag(posx);
		pos.appendTag(posy);
		pos.appendTag(posz);
		tag.setTag("Pos", pos);
		return tag;
	}
}