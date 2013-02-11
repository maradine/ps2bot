import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.Scanner;

public class PresenceHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private PresenceEngine pe;
	private Thread peThread;


	public PresenceHandler(PresenceEngine pe, Thread peThread) {
		super();
		this.pe = pe;
		this.peThread = peThread;
		this.pm = PermissionsManager.getInstance();
	}




	public void onMessage(MessageEvent event) {
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!presence ")) {
			User user = event.getUser();
			//check permissions
			//if (!pm.isAllowed("!announcements",user.getNick())) {
			if (!pm.isAllowed("!presence",event.getUser(),event.getChannel())) {
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
						event.respond("Setting the interval requires a number, expressed in minutes.  Max 180.");
					} else {
						long rawlong = scanner.nextLong();
						if (rawlong > 6000) {
							event.respond("Setting the interval requires a number, expressed in minutes.  Max 180.");
						} else {
							event.respond("Interval set to "+rawlong+" minutes.");
							pe.setInterval(rawlong*1000*60);
							peThread.interrupt();
						}
					}
					break;
					//
					//
					case "on": pe.turnOn();
					event.respond("Auto-announcements turned ON.");
					break;
					//
					case "off": pe.turnOff();
					event.respond("Auto-announcements turned OFF.");
					break;
					//
					default: event.respond("I'm not sure what you asked me.  Valid commands are \"on\", \"off\", \"add\", \"remove\", \"purge\", and \"interval\".");
					break;

				}
			
			
			}
		}
			
		
		
		if (command.equals("!presence")) {
			
			User user = event.getUser();
			//check permissions
			//if (!pm.isAllowed("!announcements",user.getNick())) {
			if (!pm.isAllowed("!presence",event.getUser(),event.getChannel())) {
				event.respond("QUIT SPAMMING MY SHIT.");
				return;
			}
			
			HashMap<String,Boolean> hm = null;
			try {
				hm = GhettoPresenceChecker.getPresence();
			} catch (Exception e) {
				event.respond("Something real bad wrong happened when I checked in with PSU.  So, no.");
				return;
			}
			for (String key : hm.keySet()) {
				String s = "Offline";
				if (hm.get(key)) {
					s = "Online";
				}
				event.respond(key+": "+s);
			}
		}
	}
}


