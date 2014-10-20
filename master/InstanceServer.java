package joris.multiserver.master;

import joris.multiserver.jexxus.common.Connection;

public class InstanceServer {

	public String		name;
	public String		password;
	public Connection	connection;
	public Integer		numberPlayers;

	/**
	 * Create new instance server without connection and 0 players
	 *
	 * @param Name
	 * @param Password
	 */
	public InstanceServer(String Name, String Password) {
		this.name = Name;
		this.password = Password;
		this.numberPlayers = 0;
	}

	/**
	 * Create new instance server with connection and 0 players
	 *
	 * @param Name
	 * @param Password
	 */
	public InstanceServer(String Name, String Password, Connection conn) {
		this.name = Name;
		this.password = Password;
		this.connection = conn;
		this.numberPlayers = 0;
	}

	/**
	 *
	 * @return String representation of all the data stored
	 */
	public String getString() {
		return "Name: " + this.name + " Players: " + this.numberPlayers + " Status: " + this.statusString();
	}

	/**
	 * @return if this instance is connected to a slave server
	 */
	public boolean isConnected() {
		if (this.connection != null) {
			return this.connection.isConnected();
		}
		return false;
	}

	/**
	 *
	 * @return 'Live' if connected else returns 'Not live'
	 */
	public String statusString() {
		if (this.isConnected()) {
			return "Live";
		}
		return "Not live";
	}
}
