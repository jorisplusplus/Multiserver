package jexxus.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import joris.multiserver.packet.Packet;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Represents a connection between two computers.
 *
 * @author Jason
 *
 */
public abstract class Connection {

	private static final int		MAGIC_NUMBER	= 1304231989;
	public Boolean					verified		= false;
	private long					bytesSent		= 0;

	protected ConnectionListener	listener;

	private final byte[]			headerInput		= new byte[8];

	private final byte[]			headerOutput	= new byte[8];

	public Connection(ConnectionListener listener) {
		if (listener == null) {
			throw new RuntimeException("You must supply a connection listener.");
		}
		this.listener = listener;
	}

	/**
	 * Closes the connection. Further data may not be transfered across this
	 * link.
	 */
	public abstract void close();

	protected byte[] compress(byte[] data) {
		Deflater compressor = new Deflater();
		compressor.setInput(data);
		compressor.finish();

		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		// Compress the data
		byte[] buf = new byte[1024];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}

		return bos.toByteArray();
	}

	protected byte[] decompress(byte[] data) {
		Inflater decompressor = new Inflater();
		decompressor.setInput(data);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

		byte[] buf = new byte[1024];
		while (!decompressor.finished()) {
			try {
				int count = decompressor.inflate(buf);
				bos.write(buf, 0, count);
			} catch (DataFormatException e) {
				throw new RuntimeException(e);
			}
		}

		return bos.toByteArray();
	}

	public long getBytesSent() {
		return this.bytesSent;
	}

	protected abstract InputStream getTCPInputStream();

	protected abstract OutputStream getTCPOutputStream();

	/**
	 * Checks to see whether the current connection is open.
	 *
	 * @return True if the connection is established.
	 */
	public abstract boolean isConnected();

	protected byte[] readTCP() throws IOException {
		InputStream tcpInput = this.getTCPInputStream();

		if (tcpInput.read(this.headerInput) == -1) {
			return null;
		}
		int magicNumber = ByteBuffer.wrap(this.headerInput).getInt();
		if (magicNumber != MAGIC_NUMBER) {
			throw new InvalidProtocolException("Bad magic number: " + magicNumber);
		}
		int len = ByteBuffer.wrap(this.headerInput).getInt(4);
		byte[] data = new byte[len];
		int count = 0;
		while (count < len) {
			count += tcpInput.read(data, count, len - count);
		}

		data = this.decompress(data);

		return data;
	}

	/**
	 * Sends the given data over this connection.
	 *
	 * @param data
	 *            The data to send to the other computer.
	 * @param deliveryType
	 *            The requirements for the delivery of this data.
	 */
	public abstract void send(byte[] data, Delivery deliveryType);

	public abstract void send(NBTTagCompound tag);

	public abstract void send(Packet packet);

	protected void sendTCP(byte[] data) throws IOException {
		OutputStream tcpOutput = this.getTCPOutputStream();

		data = this.compress(data);
		ByteBuffer.wrap(this.headerOutput).putInt(MAGIC_NUMBER);
		ByteBuffer.wrap(this.headerOutput).putInt(4, data.length);
		tcpOutput.write(this.headerOutput);
		tcpOutput.write(data);
		tcpOutput.flush();

		this.bytesSent += data.length;
	}

	public void setConnectionListener(ConnectionListener listener) {
		this.listener = listener;
	}

}
