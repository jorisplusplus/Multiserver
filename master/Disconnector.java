package joris.multiserver.master;

import java.util.Timer;
import java.util.TimerTask;

import joris.multiserver.jexxus.common.Connection;

import org.apache.logging.log4j.Level;

/**
 * Close tcp connection when the client doesn't login within 5 seconds
 */
public class Disconnector {

	private Connection	conn;
	private Timer		timer;

	class RemindTask extends TimerTask {

		@Override
		public void run() {
			if (!Disconnector.this.conn.verified) {
				MSM.logger.log(Level.WARN, "Slave didn't verify intime. Cya later");
				Disconnector.this.conn.close();
			}
		}
	}

	public Disconnector(Connection connection) {
		this.conn = connection;
		this.timer = new Timer();
		this.timer.schedule(new RemindTask(), 5000);
	}
}