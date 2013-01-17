import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import java.util.LinkedList;
import org.pircbotx.User;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Scanner;
import java.util.GregorianCalendar;
import java.util.Collections;

public class PlayTracker extends ListenerAdapter {

	private LinkedList<PlayDate> playdates;
	private static DateFormat df;
	private static TimeZone tz;
	private Scanner scanner;
	private final static String isValidTime = "(\\d{1,2}h)|(\\d{1,3}m)"; 

	public PlayTracker() {
		playdates = new LinkedList<PlayDate>();
		df = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);
		tz = TimeZone.getTimeZone("America/Los_Angeles");
		df.setTimeZone(tz);
	}
	
	public void onMessage(MessageEvent event) {
		String command = event.getMessage().toLowerCase();
		if (command.startsWith("!playdate ")) {
			User user = event.getUser();
			scanner = new Scanner(command);
			String token = scanner.next();
			// deal with first token
			if (!token.equals("!playdate")) {
				event.respond("This line of code should literally never be called.  It's like an assertion, but without the balls to self-terminate.  Have a nice day!");
				return;
			}
			//deal with second token
			if (scanner.hasNext(isValidTime)){
				//it's a valid token, convert to a time and make a playdate
				token = scanner.next();
				int length = token.length();
				//event.respond("I read the token to be "+token);
				//event.respond("I read the number component to be "+token.substring(0, length-1));
				//event.respond("I read the magnitude component to be "+token.substring(length-1, length));
				Integer num = new Integer(token.substring(0, length-1));
				String hm = token.substring(length-1, length);
				GregorianCalendar cal = new GregorianCalendar();
				if (hm.equals("h")) {
					cal.add(GregorianCalendar.HOUR, num);
				} else if (hm.equals("m")) {
					cal.add(GregorianCalendar.MINUTE, num);
				} else {
					event.respond("This line of code should literally never be called.  It's like an assertion, but without the balls to self-terminate.  Have a nice day!");
					return;
				}
				Date newdate = cal.getTime();
				PlayDate pd = new PlayDate(user, newdate);
				event.respond("You have created a new playdate for "+df.format(newdate));
				playdates.add(pd);
				Collections.sort(playdates);

			}else{  //yell loudly at the sender
				event.respond("Playdate valid time formats are 1-99 hours or 1-999 minutes, eg. \"3h\" or \"90m\".");
				return;
			}


			
			//PlayDate pd = new PlayDate();
			//pd.addPlayer(user);
			//playdates.add(pd);
			//event.respond("New playdate created NOW.");


		}

		if (command.equals("!playdate")) {
			if (playdates.size()==0) {
				event.respond("There are no playdates on the books.  Use '!playdate <timedelta>' to set one up, or '!warhorn <duration>' to let everyone know you dove into the fray.");
			} else {
				event.respond("Pulling the next playdate, one moment.");
				String pname = playdates.get(0).getPlayers().get(0).getNick();
				Date date = playdates.get(0).getDate();
				String datestring = df.format(date);	
				event.respond(pname+" has scheduled a playdate at "+datestring);

			}
		}

	}
}
