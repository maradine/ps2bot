import java.util.LinkedList;
import java.util.HashMap;
import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.HashMap;
import java.util.Set;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.BiMap;
import java.util.Calendar;

public class MaintenanceEngine implements Runnable {

	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private int timeout;
	private String soeapikey;
	private Properties props;
	private HashBiMap<String, SimpleTime> schedule;	

	public MaintenanceEngine(PircBotX bot, Properties props) {
		this.bot = bot;
		this.props = props;
		this.channel = props.getProperty("irc_channel");
		this.soeapikey = props.getProperty("soeapikey");
		onSwitch = false;
		timeout = 10000;
		schedule = HashBiMap.create();
	}


	public void setTimeout(int set) {
		timeout = set;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void purgeSchedule() {
		schedule.clear();
	}
	
	public HashBiMap<String, SimpleTime> getSchedule() {
		return schedule;
	}

	public void addSchedule(String event, SimpleTime time) {
		schedule.put(event, time);
	}

	public void addSchedule(String event, int hours, int minutes) {
		SimpleTime time = new SimpleTime(hours, minutes);
		schedule.forcePut(event, time);
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
				//bot.sendMessage(channel, "Entering run() block.");
				Calendar now;
				
				//are we even turned on?
				if (onSwitch) {
					//what time is it?
					now = Calendar.getInstance();
					int nowHours = now.get(Calendar.HOUR_OF_DAY);
					int nowMinutes = now.get(Calendar.MINUTE);
					//bot.sendMessage(channel, "Checking events for "+nowHours+":"+nowMinutes);
					
					//is there an event for this minute?
					SimpleTime st = new SimpleTime(nowHours,nowMinutes);
					//System.out.println("Constructed a SimpleTime to use as a key with elements "+st.getHours()+":"+st.getMinutes());
					BiMap<SimpleTime, String> inverted = schedule.inverse();
					//System.out.println("Inverted map has "+ inverted.size() +" elements");
					Set<SimpleTime> keySet = inverted.keySet();
					for (SimpleTime s : keySet) {
						//System.out.println("key "+s.getHours()+":"+s.getMinutes());
					}
					String eventString = inverted.get(st);
					//System.out.println("Just inverted and got the event string: "+eventString);
					if (eventString != null) {
						if (eventString.equals("cat")) {
							System.out.println("if cats");
							bot.sendMessage(channel, "This is the cat event!");
						}
						if (eventString.equals("florida")) {
							System.out.println("if florida");
							bot.sendMessage(channel, "This is the florida event!");
						}
						if (eventString.equals("nightlykills")) {
							bot.sendMessage(channel, "Running nightly kill aggregation routines.");
							HashMap<String, Integer> results = DailyAggregator.aggregateKills(props);
							int rowsDeleted = results.get("deletedRows");
							int secondsElapsed = results.get("elapsedSeconds");
							if (rowsDeleted >=0 && secondsElapsed >=0) {
								bot.sendMessage(channel,"Daily kill aggregator execution complete.  Aggregation job took "+secondsElapsed+" seconds.  "+rowsDeleted+" rows removed from raw table.");
							} else {
								bot.sendMessage(channel, "Received no result set from the aggregation job.  Bro, do you even collect?");
							}
						}
					}
				} else {
					//bot.sendMessage(channel, "Turned off, doing nothing.");
				}					
				
				//caluclate time to next minute
				now = Calendar.getInstance();
				int millis = 1000 - now.get(Calendar.MILLISECOND);
				int seconds = 59 - now.get(Calendar.SECOND);
				int sleepMillis = millis + (1000 * seconds);

				//sleep
				//bot.sendMessage(channel, "Sleeping for "+sleepMillis+" millis.");
				Thread.sleep(sleepMillis);


			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				bot.sendMessage(channel, "Totally unhandled exception - check the live feed, sparky.");
			}
		}
	}

	
	
}
