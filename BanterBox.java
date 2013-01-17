import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import java.util.ArrayList;
import java.util.Random;

public class BanterBox extends ListenerAdapter {

	private ArrayList<String> triggerCommands;
	private ArrayList<String> outputSnark;
	private Random rand;

	public BanterBox() {
		
		triggerCommands = new ArrayList<String>();
		outputSnark = new ArrayList<String>();

		
		triggerCommands.add("!dicebag");
		triggerCommands.add("!dickbag");
		triggerCommands.add("!fortune");

		outputSnark.add("You think you're clever, don't you.");
		outputSnark.add("I had a feeling you'd say that at some point.  No.");
		outputSnark.add("Who do you think I am?");
		outputSnark.add("I'll never be out of his shadow, will I.");
		outputSnark.add("Does not compute.");
		outputSnark.add("Sod off.");
		outputSnark.add("It's unlikely that you actually want me to do that.");
		outputSnark.add("I don't feel like that's a productive use of my time.");
		outputSnark.add("Come on.  Really?");
		outputSnark.add("Is that why you think I'm here?");
		outputSnark.add("I AM NOT MARVINBOT YOU PIKEY TWAT");
		outputSnark.add("I should kick you for that.");
		outputSnark.add("Go away.");

		rand = new Random();
	}


	public void onMessage(MessageEvent event) {
		
		boolean returnFire = false;
		for (String triggerString : triggerCommands) {
			if (event.getMessage().startsWith(triggerString)) {
				returnFire = true;
			}
		}
		
		if (returnFire) {

			int size = outputSnark.size();
			int index = rand.nextInt(size);
			event.respond(outputSnark.get(index));
		}
	}
}
