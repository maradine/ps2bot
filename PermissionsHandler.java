import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.Scanner;
import java.util.Collections;
import java.util.LinkedList;

public class PermissionsHandler extends ListenerAdapter {


	private Scanner scanner;
	private PermissionsManager pm;
		
	public PermissionsHandler() {
		super();
		this.pm = PermissionsManager.getInstance(); 
		System.out.println("PermissionsHandler initialized.");
	}
			
	public void onMessage(MessageEvent event) {

		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		
		if (command.equals("!lock")){  
			if (!pm.isAllowed(command,event.getUser(),event.getChannel())) {
				event.respond("Sorry, you do not have permission to execute this command.");
				return;
			}
			pm.lock();
			event.respond("Bot is now locked.  Only the owner at instantiation can run restricted commands.");
			return;
		}


		if (command.equals("!unlock")){  
			if (!pm.isAllowed(command,event.getUser(),event.getChannel())) {
				event.respond("Sorry, you do not have permission to execute this command.");
				return;
			}
			pm.unlock();
			event.respond("Bot is now unlocked.  Any Op can run restricted commands.");
			return;
		}

		
	}
}
