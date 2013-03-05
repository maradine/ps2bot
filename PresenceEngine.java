import java.util.LinkedList;
import java.util.HashMap;
import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import java.io.IOException;

public class PresenceEngine implements Runnable {

	private HashMap<String,Boolean> presence;
	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private long interval;
	private long backoff;
	private int timeout;
	private String outfitalias;
	private String soeapikey;	

	public PresenceEngine(PircBotX bot, String channel, String soeapikey) {
		presence = new HashMap<String,Boolean>();
		this.bot = bot;
		this.channel = channel;
		this.soeapikey = soeapikey;
		onSwitch = false;
		interval = 300000L;
		backoff = 0L;
		timeout = 10000;
		outfitalias = "fkpk";
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

	public void turnOn() {
		onSwitch = true;
	}

	public String getOutfit() {
		return outfitalias;
	}

	public void setOutfit(String set){
		this.outfitalias=set;
	}

	public void turnOff() {
		onSwitch = false;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(interval+backoff);
				if (onSwitch) {
				
					// go and get new data from source
					HashMap<String,Boolean> newpresence = PresenceChecker.getPresence(outfitalias, timeout, soeapikey);
					LinkedList<String> wentonline = new LinkedList<String>();
					LinkedList<String> wentoffline = new LinkedList<String>();
					
					// compare data to current list
					for (String name : newpresence.keySet()){
						//bot.sendMessage(channel, "Iterating over "+name);
						Boolean oldstatus = presence.get(name);
						Boolean newstatus = newpresence.get(name);
						if (oldstatus == null) {
							if (newstatus == true) {
								wentonline.add(name);
							}
						} else if (oldstatus == true) {
							if (newstatus == false) {
								wentoffline.add(name);
							}
						} else if (oldstatus == false) {
							if (newstatus == true) {
								wentonline.add(name);
							}
						}
					}
					for (String s : wentonline) {
						bot.sendMessage(channel, s+" is now "+Colors.GREEN+"online.");
					}
					for (String s : wentoffline) {
						bot.sendMessage(channel, s+" is now "+Colors.RED+"offline.");
					}
					presence = newpresence;
					backoff = 0L;
				}

			} catch (InterruptedException e) {
				bot.sendMessage(channel, "Interval timer interrupted - resetting backoff and restarting clock.");
			} catch (IOException e) {
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
			}
		}
	}

	
	
}
