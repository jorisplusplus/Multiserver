package joris.multiserver.common;

public interface IRelayble {

	/**
	 * Should the handler relay this message
	 *
	 * @return
	 */
	public boolean shouldRelay();

	/**
	 * Return the name of the target server
	 *
	 * @return
	 */
	public String getName();

}
