import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.Scanner;
import org.pircbotx.Colors;
import java.util.Properties;
import java.sql.SQLException;
import java.io.IOException;
public class StatCollectionHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private StatCollectionEngine se;
	private Thread seThread;
	private String soeapikey;
	private Properties props;

	public StatCollectionHandler(StatCollectionEngine se, Thread seThread, Properties props) {
		super();
		this.se = se;
		this.seThread = seThread;
		this.pm = PermissionsManager.getInstance();
		this.soeapikey = props.getProperty("soeapikey");
		System.out.println("StatHandler Initialized.");
		this.props = props;
	}

	public void onMessage(MessageEvent event) {
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!stats ")) {
			User user = event.getUser();
			if (!pm.isAllowed("!stats",event.getUser(),event.getChannel())) {
				event.respond("Sorry, you are not in the access list for presence checking.");
				return;
			}
			scanner = new Scanner(command);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next().toLowerCase();
				switch (token) {
					//
					case "interval": if (!scanner.hasNextLong()){
						event.respond("Setting the interval requires a number, expressed in seconds.  Max 60.");
					} else {
						long rawlong = scanner.nextLong();
						if (rawlong > 60) {
							event.respond("Setting the interval requires a number, expressed in seconds.  Max 60.");
						} else {
							event.respond("Interval set to "+rawlong+" seconds.");
							se.setInterval(rawlong*1000);
						}
					}
					break;
					//
					//
					case "timeout": if (!scanner.hasNextInt()){
						event.respond("Setting the timeout requires a number, expressed in seconds.  Max 20.");
					} else {
						int rawint = scanner.nextInt();
						if (rawint > 20) {
							event.respond("Setting the timeout requires a number, expressed in seconds.  Max 20.");
						} else {
							event.respond("Timeout set to "+rawint+" seconds.");
							se.setTimeout(rawint*1000);
						}
					}
					break;
					//
					case "manual": try {
						event.respond("Initiating manual pull from API.");
						HashMap<String, Integer> results = KillCollector.collectKills(se.getTimeout(), props);
						event.respond("Stat run complete.  Duration: "+results.get("duration")+" millis.  Duplicates: "+results.get("skipCount")+" rows.");
					} catch (SQLException sex) {
						event.respond(sex.getMessage());
					} catch (IOException ioex) {
						event.respond(ioex.getMessage());
					}
					break;
					//
					//
					case "on": se.turnOn();
					event.respond("Automatic stat puller turned ON.");
					break;
					//
					case "off": se.turnOff();
					event.respond("Automatic stat puller turned OFF.");
					break;
					//
					default: event.respond("I'm not sure what you asked me.  Valid commands are BLAH BLHA BLAH");
					break;

				}
			}
		}
		
		if (command.equals("!stats")) {
	
			event.respond("Cats!");
		}
	}
}


