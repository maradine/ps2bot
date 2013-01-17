import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.DateFormat;
import java.util.Date;

public class TZTest extends ListenerAdapter {

	public void onMessage(MessageEvent event) {
		if (event.getMessage().equals("!newdate-tz")) {
			Date date = new Date();
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);
			TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
			df.setTimeZone(tz);
			String out = df.format(date);	
			event.respond(out);
		}
		if (event.getMessage().equals("!newdate")) {
			Date date = new Date();
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);
			String out = df.format(date);	
			event.respond(out);
		}
	}
}
