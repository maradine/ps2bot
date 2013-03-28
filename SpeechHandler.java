import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.User;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import java.util.Scanner;
import java.util.Collections;
import java.util.LinkedList;

public class SpeechHandler extends ListenerAdapter {


	private Scanner scanner;
	private PermissionsManager pm;
	private PircBotX bot;
	private String channel;
			
	public SpeechHandler(PircBotX bot, String channel) {
		super();
		this.bot = bot;
		this.channel = channel;
		this.pm = PermissionsManager.getInstance(); 
		System.out.println("PermissionsHandler initialized.");
	}
			
	public void onPrivateMessage(PrivateMessageEvent event) {
		
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!speak ")) {
			User user = event.getUser();
			Channel channelobject = bot.getChannel(channel);
			//check permissions
			if (!pm.isAllowed("!speak",event.getUser(),channelobject)) {
				event.respond("Sorry, you are not authorized to run this command.");
				return;
			}
			scanner = new Scanner(command);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next();
				String newmessage = rawcommand.substring(7);
				bot.sendMessage(channel, newmessage);
			}
		}
		if (command.startsWith("!kick ")) {
			User user = event.getUser();
			Channel channelobject = bot.getChannel(channel);
			//check permissions
			if (!pm.isAllowed("!kick",event.getUser(),channelobject)) {
				event.respond("Sorry, you are not authorized to run this command.");
				return;
			}
			scanner = new Scanner(command);
			String token = scanner.next(); //token is "!kick"
			
			if (scanner.hasNext()){
				String targetnick = scanner.next(); //token should be target nick
				int nicklength = targetnick.length();
				if (scanner.hasNext()) {
					// there was a reason tacked on, too
					String reason = rawcommand.substring(7+nicklength);
					bot.kick(bot.getChannel(channel),bot.getUser(targetnick),reason);
					return;
				} else {
					bot.kick(bot.getChannel(channel),bot.getUser(targetnick));
					return;
				}
			} 
		}

	}
}
