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



public class KillCollectionHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private KillCollectionEngine kce;
	private Thread kceThread;
	private String soeapikey;
	private Properties props;

	public KillCollectionHandler(KillCollectionEngine kce, Thread kceThread, Properties props) {
		super();
		this.kce = kce;
		this.kceThread = kceThread;
		this.pm = PermissionsManager.getInstance();
		this.soeapikey = props.getProperty("soeapikey");
		System.out.println("KillCollectionHandler Initialized.");
		this.props = props;
	}

	public void onMessage(MessageEvent event) {
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!kills ")) {
			User user = event.getUser();
			if (!pm.isAllowed("!stats",event.getUser(),event.getChannel())) {
				event.respond("Sorry, you are not in the access list for kill colleciton handling.");
				return;
			}
			scanner = new Scanner(command);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next().toLowerCase();
				switch (token) {
					//
					case "on": kce.turnOn();
					event.respond("Automatic stat puller turned ON.");
					break;
					//
					case "off": kce.turnOff();
					event.respond("Automatic stat puller turned OFF.");
					break;
					//
					case "playtime": try {
						event.respond("Running the thing.");
						HashMap<String, Integer> hm = PlaytimeCollector.collectPlaytime(kce.getTimeout(), kce.getInterval(), props);
						int playtimeEvents = hm.get("playtimeEvents");
						int newRows = hm.get("newRows");
						int updateCounter = hm.get("updateCounter");
						int dupeCounter = hm.get("dupeCounter");
						int hitCounter = hm.get("hitCounter");
						float percentWarm = (float)updateCounter / (float)newRows * 100;
						event.respond("Processed "+playtimeEvents+" events.  "+hitCounter+" cache hits.  "+dupeCounter+" duplicates in pull.  "+updateCounter+" rows updated.  "+percentWarm+" cache warmth.");
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
	
			boolean onSwitch = kce.isOn();
			long interval = kce.getInterval();
			int iSeconds = (int)interval / 1000;
			if (onSwitch) {
				event.respond("Kill Collection Engine is running. "+kce.getRowsProcessed()+" kills processed. Current interval at "+iSeconds+" seconds.  ~"+(850/(iSeconds+5))+"kps.");
			} else {
				event.respond("Kill Collection Engine is idle.");
			}
		}
	}
}


