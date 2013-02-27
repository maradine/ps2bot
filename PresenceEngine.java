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
	private int timeout;
	private String outfitalias;	

	public PresenceEngine(PircBotX bot, String channel) {
		presence = new HashMap<String,Boolean>();
		this.bot = bot;
		this.channel = channel;
		onSwitch = false;
		interval = 360000L;
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
				Thread.sleep(interval);
				if (onSwitch) {
				
					// go and get new data from source
					HashMap<String,Boolean> newpresence = PresenceChecker.getPresence(outfitalias, timeout);
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
				}

			} catch (InterruptedException e) {
				bot.sendMessage(channel, "Interval timer interrupted - restarting clock.");
			} catch (IOException e) {
				bot.sendMessage(channel, "PSU just choked over an update check - sorry!");
			}
		}
	}

	
	
}
