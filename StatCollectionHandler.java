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
	private KillCollectionEngine kce;
	private Thread kceThread;
	private String soeapikey;
	private Properties props;

	public StatCollectionHandler(KillCollectionEngine kce, Thread kceThread, Properties props) {
		super();
		this.kce = kce;
		this.kceThread = kceThread;
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
					case "on": kce.turnOn();
					event.respond("Automatic stat puller turned ON.");
					break;
					//
					case "off": kce.turnOff();
					event.respond("Automatic stat puller turned OFF.");
					break;
					//
					case "status": boolean onSwitch = kce.isOn();
					long interval = kce.getInterval();
					int iSeconds = (int)interval / 1000;
					if (onSwitch) {
						event.respond("Kill Collection Engine is running. "+kce.getRowsProcessed()+" kills processed. Current interval at "+iSeconds+" seconds.  ~"+(850/(iSeconds+5))+"kps.");
					} else {
						event.respond("Kill Collection Engine is idle.");
					}
					break;
					//
					default: event.respond("I'm not sure what you asked me.  Valid commands are BLAH BLHA BLAH");
					break;

				}
			}
		}
		
		if (command.equals("!stats")) {
	
			event.respond("http://bit.ly/1eVKYmh");
		}
	}
}


