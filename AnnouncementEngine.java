import java.util.ArrayList;
import org.pircbotx.PircBotX;

public class AnnouncementEngine implements Runnable {

	private ArrayList<String> content;
	private PircBotX bot;
	private String channel;
	boolean onSwitch;
	int interval;

	public AnnouncementEngine(PircBotX bot, String channel) {
		content = new ArrayList<String>();
		this.bot = bot;
		this.channel = channel;
		onSwitch = false;
		interval = 3600000;
	}

	public void setInterval(int set) {
		interval = set;
	}

	public void purgeContent() {
		content = new ArrayList<String>();
	}

	public void addContent(String s) {
		content.add(s);
	}

	public ArrayList<String> getContent() {
		return content;
	}

	public void turnOn() {
		onSwitch = true;
	}

	public void turnOff() {
		onSwitch = false;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(interval);
				if (onSwitch) {
					for (String s : content) {
						bot.sendMessage(channel, s);
					}
				}
			} catch (Exception e) {
				bot.sendMessage(channel, "Interval timer interrupted - restarting clock.");
			}
		}
	}

	
	
}
