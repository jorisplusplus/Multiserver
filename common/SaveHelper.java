package joris.multiserver.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.server.FMLServerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.DimensionManager;

public class SaveHelper {

	public File	playersDirectory	= new File(DimensionManager.getCurrentSaveRootDirectory(), "playerdata");
	public File	dataDirectory		= new File(DimensionManager.getCurrentSaveRootDirectory(), "data");
	
	/**
	 * Reads the player data from disk returns the nbttagcompound.
	 */
	public NBTTagCompound readPlayerData(String UniqueID) {
		NBTTagCompound nbttagcompound = null;
		try {
			File file1 = new File(playersDirectory, UniqueID + ".dat");
			if (file1.exists() && file1.isFile()) {
				nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
			} else {
				return null;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return nbttagcompound;
	}

	/**
	 * Stores the nbttagcompound of a player.
	 *
	 * @param UniqueID
	 * @param nbttagcompound
	 */
	public void storePlayerData(String UniqueID, NBTTagCompound nbttagcompound) {
		try {

			File file1 = new File(playersDirectory, UniqueID + ".dat.tmp");
			File file2 = new File(playersDirectory, UniqueID + ".dat");
			CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file1));
			if (file2.exists()) {
				file2.delete();
			}
			file1.renameTo(file2);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public NBTTagCompound readWaypoints() {
		NBTTagCompound nbttagcompound = null;
		try {
			File file1 = new File(dataDirectory, "Waypoints.dat");
			if (file1.exists() && file1.isFile()) {
				nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
			} else {
				return new NBTTagCompound();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return nbttagcompound;
	}

	/**
	 * Stores the waypoint data
	 *
	 * @param nbttagcompound
	 */
	public void storeWaypoints(NBTTagCompound nbttagcompound) {
		try {
			File file1 = new File(dataDirectory, "Waypoints.dat.tmp");
			File file2 = new File(dataDirectory, "Waypoints.dat");
			CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file1));
			if (file2.exists()) {
				file2.delete();
			}
			file1.renameTo(file2);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
