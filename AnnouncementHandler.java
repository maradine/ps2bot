import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.Scanner;
import java.util.Collections;
import java.util.ArrayList;

public class AnnouncementHandler extends ListenerAdapter {


	private Scanner scanner;
	private AnnouncementEngine ae;
	private Thread aeThread;
	private PermissionsManager pm;
		
	public AnnouncementHandler(AnnouncementEngine ae, Thread aeThread) {
		super();
		this.ae = ae;
		this.aeThread = aeThread;
		this.pm = PermissionsManager.getInstance(); 
	}
			
	public void onMessage(MessageEvent event) {

		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		if (command.startsWith("!announcements ")) {
			User user = event.getUser();
			//check permissions
			//if (!pm.isAllowed("!announcements",user.getNick())) {
			if (!pm.isAllowed("!announcements",event.getUser(),event.getChannel())) {
				event.respond("Sorry, you are not in the access list for announcment modification.");
				return;
			}
			scanner = new Scanner(command);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next().toLowerCase();
				switch (token) {
					//
					case "add": if (!scanner.hasNext()) {
						event.respond("I can't add an empty announcement.");
					} else {
						ae.addContent(rawcommand.substring(19));
						event.respond("Your announcement has been added.");
					}
					break;
					//
					case "purge": ae.purgeContent();
					event.respond("All announcement content purged.");
					break;
					//
					case "interval": if (!scanner.hasNextInt()){
						event.respond("Setting the interval requires a number, expressed in minutes.  Max 180.");
					} else {
						int rawint = scanner.nextInt();
						if (rawint > 180) {
							event.respond("Setting the interval requires a number, expressed in minutes.  Max 180.");
						} else {
							event.respond("Interval set to "+rawint+" minutes.");
							ae.setInterval(rawint*1000*60);
							aeThread.interrupt();
						}
					}
					break;
					//
					case "on": ae.turnOn();
					event.respond("Auto-announcements turned ON.");
					break;
					//
					case "off": ae.turnOff();
					event.respond("Auto-announcements turned OFF.");
					break;
					//
					default: event.respond("I'm not sure what you asked me.  Valid commands are \"on\", \"off\", \"add\", \"purge\", and \"interval\".");
					break;

				}
			
			
			}
		
		}else if (command.equals("!announcements")){  //dump announcements
			ArrayList<String> content = ae.getContent();
			event.respond("Current announcements in memory:");
			for (String s : content) {
				event.respond(s);
			}
			return;
		}

		
	}
}
