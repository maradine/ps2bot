import org.pircbotx.hooks.ListenerAdapter;

import org.pircbotx.hooks.events.MessageEvent;
import java.util.ArrayList;
import java.util.Random;

public class OracleBanterBox extends ListenerAdapter {

	private ArrayList<String> triggerCommands;
	private ArrayList<String> outputSnark;
	private Random rand;

	public OracleBanterBox() {
		
		triggerCommands = new ArrayList<String>();
		outputSnark = new ArrayList<String>();

		
		triggerCommands.add("!prescience");
		triggerCommands.add("!future");
		triggerCommands.add("!fortune");

		outputSnark.add("You're gonna die.");
		outputSnark.add("Cubs take the pennant in 2016");
		outputSnark.add("Cancer.  Sorry.");
		outputSnark.add("You don't want to know.");
		outputSnark.add("Have you seen The Terminator?");
		outputSnark.add("Flying cars.");
		outputSnark.add("Steve Jobs returns from the dead, gets rehired.  Jesus returns as VP PR.");
		outputSnark.add("If I told you, would you be any happier?");
		outputSnark.add("The Striker gets nerfed.  Just kidding.");
		outputSnark.add("I see a Vanu dance party.  I see spandex.  I see you at the center of a large circle.  I see where this is going.");

		rand = new Random();
	}


	public void onMessage(MessageEvent event) {
		
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!presents ") || command.equals("!presents")) {
			event.respond("(ノಠ益ಠ)ノ彡┻━┻");

		} else {
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
}
