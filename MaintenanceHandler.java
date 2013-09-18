import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.BiMap;
import java.util.Scanner;
import org.pircbotx.Colors;
import java.util.Properties;
import java.sql.SQLException;
import java.io.IOException;
import java.util.StringTokenizer;

public class MaintenanceHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private MaintenanceEngine me;
	private Thread meThread;
	private String soeapikey;
	private Properties props;

	public MaintenanceHandler(MaintenanceEngine me, Thread meThread, Properties props) {
		super();
		this.me = me;
		this.meThread = meThread;
		this.pm = PermissionsManager.getInstance();
		this.soeapikey = props.getProperty("soeapikey");
		System.out.println("MaintenanceHandler Initialized.");
		this.props = props;
	}

	private boolean isValidTimeInput(String input) {
		StringTokenizer st = new StringTokenizer(input, ":");
		if (st.countTokens() != 2) {
			return false;
		} else {
			String hoursString = st.nextToken();
			String minutesString = st.nextToken();
			int hours = Integer.parseInt(hoursString);
			int minutes = Integer.parseInt(minutesString);
			if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
				return false;
			} else {
				return true;
			}
		}
	}

	private SimpleTime parseTimeInput(String input) {
		StringTokenizer st = new StringTokenizer(input, ":");
		if (st.countTokens() != 2) {
			return null;
		} else {
			String hoursString = st.nextToken();
			String minutesString = st.nextToken();
			int hours = Integer.parseInt(hoursString);
			int minutes = Integer.parseInt(minutesString);
			if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
				return null;
			} else {
				SimpleTime t = new SimpleTime(hours, minutes);
				return t;
			}
		}
	}

	
	public void onMessage(MessageEvent event) {
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!maint ")) {
			User user = event.getUser();
			if (!pm.isAllowed("!maint",event.getUser(),event.getChannel())) {
				event.respond("Sorry, you are not in the access list for maintenance functions.");
				return;
			}
			scanner = new Scanner(command);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next().toLowerCase();
				switch (token) {
					/*
					case "interval": if (!scanner.hasNextLong()){
						event.respond("Setting the interval requires a number, expressed in seconds.  Max 60.");
					} else {
						long rawlong = scanner.nextLong();
						if (rawlong > 60) {
							event.respond("Setting the interval requires a number, expressed in seconds.  Max 60.");
						} else {
							event.respond("Interval set to "+rawlong+" seconds.");
							kce.setInterval(rawlong*1000);
						}
					}
					break;
					*/
					
					case "add": if (scanner.hasNext()){
						String maintEvent = scanner.next();
						if (scanner.hasNext()) {
							String maintTime = scanner.next();
							if (isValidTimeInput(maintTime)) {
								SimpleTime st = parseTimeInput(maintTime);
								me.addSchedule(maintEvent, st);
								event.respond("Maintenance event added.");
							} else {
								event.respond("Invalid time format.  hours:minutes in 24 hour time.  See a clock.");
							}
						} else {
							event.respond("ADD syntax requires an event keyword and a colon separated hours:minutes. ex: \"!maint add cats 17:30\".");
						}					
					} else {
						event.respond("ADD syntax requires an event keyword and a colon separated hours:minutes. ex: \"!maint add cats 17:30\".");
					}
					break;
					//
					case "list": BiMap<String,SimpleTime> schedule = me.getSchedule();
					if (schedule.size() > 0) {
						event.respond("The following events are scheduled:");
						BiMap<SimpleTime, String> inverse = schedule.inverse();
						Set<SimpleTime> timeSet = inverse.keySet();
						ArrayList<SimpleTime> timeList = new ArrayList<SimpleTime>(timeSet);
						java.util.Collections.sort(timeList);
	
						for (SimpleTime time : timeList) {
							String scheduledEvent = inverse.get(time);
							event.respond("["+time.getHours()+":"+time.getMinutes()+"] - "+scheduledEvent);
						}
					} else {
						event.respond("No maintenance events are scheduled.");
					}
					break;
					//
					case "purge": me.purgeSchedule();
					event.respond("All scheduled events cleared.");
					break;
					//
					case "on": me.turnOn();
					event.respond("Maintenance engine turned ON.");
					break;
					//
					case "off": me.turnOff();
					event.respond("Maintenance engine turned OFF.");
					break;
					//
					default: event.respond("I'm not sure what you asked me.  Valid commands are BLAH BLHA BLAH");
					break;

				}
			}
		}
		
		if (command.equals("!maint")) {
	
			event.respond("Taint!");
		}
	}
}


