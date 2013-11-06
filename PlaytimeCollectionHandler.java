import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.Scanner;
import org.pircbotx.Colors;
import java.util.Properties;
import java.sql.SQLException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;



public class PlaytimeCollectionHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private PlaytimeCollectionEngine pce;
	private Thread pceThread;
	private String soeapikey;
	private Properties props;

	public PlaytimeCollectionHandler(PlaytimeCollectionEngine pce, Thread pceThread, Properties props) {
		super();
		this.pce = pce;
		this.pceThread = pceThread;
		this.pm = PermissionsManager.getInstance();
		this.soeapikey = props.getProperty("soeapikey");
		System.out.println("PlaytimeCollectionHandler Initialized.");
		this.props = props;
	}

	public void onMessage(MessageEvent event) {
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!playtime ")) {
			User user = event.getUser();
			if (!pm.isAllowed("!playtime",event.getUser(),event.getChannel())) {
				event.respond("Sorry, you are not in the access list for playtime colleciton handling.");
				return;
			}
			scanner = new Scanner(command);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next().toLowerCase();
				switch (token) {
					//
					case "on": pce.turnOn();
					event.respond("Automatic stat puller turned ON.");
					break;
					//
					case "off": pce.turnOff();
					event.respond("Automatic stat puller turned OFF.");
					break;
					//
					case "playtime": try {
						event.respond("Running the thing.");
						HashMap<String, Integer> hm = PlaytimeCollector.collectPlaytime(pce.getTimeout(), pce.getInterval(), props);
						int playtimeEvents = hm.get("playtimeEvents");
						int newRows = hm.get("newRows");
						int updateCounter = hm.get("updateCounter");
						int dupeCounter = hm.get("dupeCounter");
						int hitCounter = hm.get("hitCounter");
						float percentWarm = (float)updateCounter / (float)newRows * 100;
						event.respond("Processed "+playtimeEvents+" events.  "+hitCounter+" cache hits.  "+dupeCounter+" duplicates in pull.  "+updateCounter+" rows updated.  "+percentWarm+"% cache heat.");
					} catch (IllegalArgumentException e) {
						event.respond("Bad or missing memcached server proprty.");
						event.respond(e.getMessage());
						e.printStackTrace();
					
					} catch (IOException e) {
						event.respond("Unhandled IO Exception.  Fix it, asshole:");
						event.respond(e.getMessage());
						e.printStackTrace();
					
					} catch (SQLException e) {
						event.respond("Unhandled SQL Exception.  Fix it, asshole:");
						event.respond(e.getMessage());
						e.printStackTrace();
					
					} catch (TimeoutException e) {
						event.respond("Memcached connection timed out during operation.  Fix it, asshole:");
						event.respond(e.getMessage());
						e.printStackTrace();
					
					} catch (InterruptedException e) {
						event.respond("Unhandled Interrupted Exception.  Fix it, asshole:");
						event.respond(e.getMessage());
						e.printStackTrace();
					
					} catch (ExecutionException e) {
						event.respond("Unhandled Execution Exception.  Fix it, asshole:");
						event.respond(e.getMessage());
						e.printStackTrace();
					}
					
					
					
					//}catch (Exception e) {
					//	event.respond("Unhandled Exception.  Fix it, asshole:");
					//	event.respond(e.getMessage());
					//	e.printStackTrace();
					//}
					//
					break;
					default: event.respond("I'm not sure what you asked me.  Valid commands are BLAH BLHA BLAH");
					break;

				}
			}
		}

		if (command.equals("!status")) {
	
			boolean onSwitch = pce.isOn();
			long interval = pce.getInterval();
			if (onSwitch) {
				event.respond("Playtime Collection Engine is running. "+pce.getRowsProcessed()+" rows processed. Current interval at "+pce.getInterval()+" seconds.  Cache heat at "+pce.getCacheHeat()+"%");
			} else {
				event.respond("Playtime Collection Engine is idle.");
			}
		}
		
	}
}


