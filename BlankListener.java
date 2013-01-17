import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class BlankListener extends ListenerAdapter {

	public void onMessage(MessageEvent event) {
		event.respond("BORK");
	}
}
