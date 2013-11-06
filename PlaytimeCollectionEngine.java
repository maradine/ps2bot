import java.util.LinkedList;
import java.util.HashMap;
import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

public class PlaytimeCollectionEngine implements Runnable {

	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private boolean isHot;
	private long interval;
	private long backoff;
	private int timeout;
	private String soeapikey;
	private Properties props;	
	private Boolean firstRun;
	private long rowsProcessed;
	private float cacheHeat;

	public PlaytimeCollectionEngine(PircBotX bot, Properties props) {
		this.bot = bot;
		this.props = props;
		this.channel = props.getProperty("irc_channel");
		this.soeapikey = props.getProperty("soeapikey");
		onSwitch = false;
		isHot = false;
		interval = 60000L;
		backoff = 0L;
		timeout = 15000;
		firstRun = true;
		rowsProcessed = 0L;
		cacheHeat = 0f;
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
	
	public long getRowsProcessed() {
		return rowsProcessed;
	}

	public boolean isOn() {
		return onSwitch;
	}
	
	public float getCacheHeat() {
		return cacheHeat;
	}
	
	public boolean isHot() {
		return isHot;
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
						HashMap<String, Integer> results = PlaytimeCollector.collectPlaytime(timeout, interval, props);
						int playtimeEvents = results.get("playtimeEvents");
						int newRows = results.get("newRows");
						int updateCounter = results.get("updateCounter");
						int dupeCounter = results.get("dupeCounter");
						int hitCounter = results.get("hitCounter");
						cacheHeat = (float)updateCounter / (float)newRows * 100;
						bot.sendMessage(props.getProperty("irc_channel"), "Processed "+playtimeEvents+" events.  "+hitCounter+" cache hits.  "+dupeCounter+" duplicates in pull.  "+updateCounter+" rows updated.  "+cacheHeat+"% cache heat.");
						if (!firstRun) {
							if (results.get("dupeCounter") > 3000) {
								interval=interval+5000;
								bot.sendMessage(props.getProperty("irc_channel"), "Playtime pull interval to "+interval/1000+" seconds.");
							} else if (results.get("dupeCounter") > 2000) {
								interval=interval+2000;
								bot.sendMessage(props.getProperty("irc_channel"), "Playtime pull interval to "+interval/1000+" seconds.");
							} else if (results.get("dupeCounter") > 1000) {
								interval=interval+1000;
								bot.sendMessage(props.getProperty("irc_channel"), "Playtime pull interval to "+interval/1000+" seconds.");
							} else if (results.get("dupeCounter") < 1) {
								interval=interval-10000;
								if (interval <1000) {
									interval = 1000;
									bot.sendMessage(props.getProperty("irc_channel"), "MARADINE: PLAYTIME PULL LOWER BOUND REACHED.  PATCH IN PROGRESS OR CRITICAL PERFORMANCE ISSUE.");
								} else {
									bot.sendMessage(props.getProperty("irc_channel"), "Playtime pull interval to "+interval/1000+" seconds.");
								}
							} else if (results.get("dupeCounter") < 500) {
								interval=interval-1000;
								if (interval <1000) {
									interval = 1000;
									bot.sendMessage(props.getProperty("irc_channel"), "MARADINE: PLAYTIME PULL LOWER BOUND REACHED.  PATCH IN PROGRESS OR CRITICAL PERFORMANCE ISSUE.");
								} else {
									bot.sendMessage(props.getProperty("irc_channel"), "Playtime pull interval to "+interval/1000+" seconds.");
								}
							}
						}
						rowsProcessed += (5000 - results.get("dupeCounter"));
						firstRun=false;
					} catch (IllegalArgumentException e) {
						bot.sendMessage(props.getProperty("irc_channel"),"Bad or missing memcached server proprty.");
						bot.sendMessage(props.getProperty("irc_channel"),e.getMessage());
						e.printStackTrace();
						onSwitch=false;
					} catch (SocketTimeoutException stex) {
						bot.sendMessage(props.getProperty("irc_channel"), "SOE timed out an API pull.  maradine alert.");
					} catch (IOException e) {
						bot.sendMessage(props.getProperty("irc_channel"),"Unhandled IO Exception.  Fix it, asshole:");
						bot.sendMessage(props.getProperty("irc_channel"),e.getMessage());
						e.printStackTrace();
					} catch (SQLException e) {
						bot.sendMessage(props.getProperty("irc_channel"),"Unhandled SQL Exception.  Fix it, asshole:");
						bot.sendMessage(props.getProperty("irc_channel"),e.getMessage());
						e.printStackTrace();
						onSwitch=false;
					
					} catch (TimeoutException e) {
						bot.sendMessage(props.getProperty("irc_channel"),"Memcached connection timed out during operation.  Fix it, asshole:");
						bot.sendMessage(props.getProperty("irc_channel"),e.getMessage());
						e.printStackTrace();
						onSwitch=false;
					
					} catch (InterruptedException e) {
						bot.sendMessage(props.getProperty("irc_channel"),"Unhandled Interrupted Exception.  Fix it, asshole:");
						bot.sendMessage(props.getProperty("irc_channel"), e.getMessage());
						e.printStackTrace();
						onSwitch=false;
					
					} catch (ExecutionException e) {
						bot.sendMessage(props.getProperty("irc_channel"),"Unhandled Execution Exception.  Fix it, asshole:");
						bot.sendMessage(props.getProperty("irc_channel"), e.getMessage());
						e.printStackTrace();
						onSwitch=false;
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
