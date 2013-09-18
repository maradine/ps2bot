import java.util.LinkedList;
import java.util.HashMap;
import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.HashMap;

public class KillCollectionEngine implements Runnable {

	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private long interval;
	private long backoff;
	private int timeout;
	private String soeapikey;
	private Properties props;	
	private Boolean firstRun;

	public KillCollectionEngine(PircBotX bot, Properties props) {
		this.bot = bot;
		this.props = props;
		this.channel = props.getProperty("irc_channel");
		this.soeapikey = props.getProperty("soeapikey");
		onSwitch = false;
		interval = 15000L;
		backoff = 0L;
		timeout = 10000;
		firstRun = true;
	}

	public void setInterval(long set) {
		interval = set;
	}

	public void setTimeout(int set) {
		timeout = set;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public long getInterval() {
		return interval;
	}

	public boolean isOn() {
		return onSwitch;
	}
	
	public void turnOn() {
		onSwitch = true;
	}

	public void turnOff() {
		onSwitch = false;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(interval+backoff);
				if (onSwitch) {
				
					try {
						HashMap<String, Integer> results = KillCollectorEnhanced.collectKills(timeout, interval, props);
						//bot.sendMessage(props.getProperty("irc_channel"), "Stat run complete.  Duration: "+results.get("duration")+" millis.  Duplicates: "+results.get("skipCount")+" rows.");
						if (!firstRun) {
							if (results.get("skipCount") > 400) {
								interval=interval+2000;
								bot.sendMessage(props.getProperty("irc_channel"), "Lots of dupes - increasing pull interval.  Next pull starts in "+interval/1000+" seconds.");
							} else if (results.get("skipCount") > 250) {
								interval=interval+1000;
								bot.sendMessage(props.getProperty("irc_channel"), "Lots of dupes - increasing pull interval.  Next pull starts in "+interval/1000+" seconds.");
							} else if (results.get("skipCount") < 100) {
								interval=interval-1000;
								if (interval <0L) {interval = 0L;}
								bot.sendMessage(props.getProperty("irc_channel"), "Dupe buffer too small - decreasing pull interval.  Next pull starts in "+interval/1000+" seconds.");
							}
						}
						firstRun=false;
					} catch (SocketTimeoutException stex) {
						bot.sendMessage(props.getProperty("irc_channel"), "SOE timed out an API pull.  Call your congressman.");
					} catch (SQLException sex) {
						bot.sendMessage(props.getProperty("irc_channel"), sex.getMessage());
					} catch (IOException ioex) {
						bot.sendMessage(props.getProperty("irc_channel"), "Unhandled IO Exception.  Someone get maradine.");
						bot.sendMessage(props.getProperty("irc_channel"), ioex.getMessage());
					}
						
					
				}

			} catch (InterruptedException e) {
				bot.sendMessage(channel, "Interval timer interrupted - resetting backoff and restarting clock.");
			} /*catch (IOException e) {
				if (backoff==0L) {
					backoff = 300000L;
					bot.sendMessage(channel, "SOE just choked over an update check - sorry!  Backing off a bit.");
					System.out.println("API Failure - backoff is now "+backoff);
				} else if (backoff > 3600000L) {
					bot.sendMessage(channel, "Enough API calls have failed that I'm shutting down the presence engine.  Please contact my owner.");
					System.out.println("API Failure");
					System.out.println("Shutting down presence and resetting timeouts.");
					this.turnOff();
					backoff=0L;
				} else {
					backoff = backoff*2;
					bot.sendMessage(channel, "SOE choked again.  Backing off further.");
					System.out.println("API Failure - backoff is now "+backoff);
				}
			}*/
		}
	}

	
	
}
