import java.util.LinkedList;
import java.util.HashMap;
import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import java.io.IOException;

public class PresenceEngine implements Runnable {

	private HashMap<String,HashMap<String,Boolean>> allPresenceStates;
	private HashMap<String,Boolean> presenceState;
	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private long interval;
	private long backoff;
	private int timeout;
	private LinkedList<String> outfitAliasList;
	private String soeapikey;	
	private boolean squelch;
	
	public PresenceEngine(PircBotX bot, String channel, String soeapikey) {
		presenceState = new HashMap<String,Boolean>();
		this.bot = bot;
		this.channel = channel;
		this.soeapikey = soeapikey;
		onSwitch = false;
		squelch = true;
		interval = 60000L;
		backoff = 0L;
		timeout = 10000;
		outfitAliasList = new LinkedList<String>();
		outfitAliasList.add("FKPK");
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

	public void squelchOn() {
		squelch = true;
	}

	public void squelchOff() {
		squelch = false;
	}

	public LinkedList<String> getOutfits() {
		return outfitAliasList;
	}

	public void addOutfit(String set){
		this.outfitAliasList.add(set);
	}

	public void purgeOutfits() {
		this.outfitAliasList = new LinkedList<String>();
		this.allPresenceStates = new HashMap<String, HashMap<String, Boolean>>();
	}

	public void turnOff() {
		onSwitch = false;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(interval+backoff);
				if (onSwitch) {
				
					
					for (String outfit : outfitAliasList) {
					
						// go and get new data from source
						HashMap<String,Boolean> newPresenceState = PresenceChecker.getPresence(outfit, timeout, soeapikey);
						HashMap<String,Boolean> oldPresenceState = allPresenceStates.get(outfit);
						if (oldPresenceState == null) {
							oldPresenceState = new HashMap<String,Boolean>();
						}
						LinkedList<String> wentonline = new LinkedList<String>();
						LinkedList<String> wentoffline = new LinkedList<String>();
					
						// compare data to current list
						for (String name : newPresenceState.keySet()){
							//bot.sendMessage(channel, "Iterating over "+name);
							Boolean oldstatus = oldPresenceState.get(name);
							Boolean newstatus = newPresenceState.get(name);
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
					

						//squelch output if the change is unrealistically large (SOE patch time)
						int totalSize = newPresenceState.size();
						int onlineDiff = wentonline.size();
						int offlineDiff = wentoffline.size();
					
						float percentChangeOnline = (float)onlineDiff / (float)totalSize; 
						float percentChangeOffline = (float)offlineDiff / (float)totalSize; 
						
						boolean squelchTrigger = false;
	
						if (percentChangeOnline > .9 || percentChangeOffline > .9) {
							squelchTrigger = true;
						}

						if (!squelchTrigger) {
							for (String s : wentonline) {
								bot.sendMessage(channel, "["+outfit+"]"+s+" is now "+Colors.GREEN+"online.");
							}
							for (String s : wentoffline) {
								bot.sendMessage(channel, "["+outfit+"]"+s+" is now "+Colors.RED+"offline.");
							}
							allPresenceStates.put(outfit, newPresenceState);
						}
						backoff = 0L;
					}
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
