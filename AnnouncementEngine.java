import java.util.LinkedList;
import org.pircbotx.PircBotX;

public class AnnouncementEngine implements Runnable {

	private LinkedList<String> content;
	private PircBotX bot;
	private String channel;
	private boolean onSwitch;
	private long interval;

	public AnnouncementEngine(PircBotX bot, String channel) {
		content = new LinkedList<String>();
		this.bot = bot;
		this.channel = channel;
		onSwitch = false;
		interval = 3600000L;
	}

	public void setInterval(long set) {
		interval = set;
	}

	public void purgeContent() {
		content = new LinkedList<String>();
	}

	public void addContent(String s) {
		content.add(s);
	}

	public LinkedList<String> getContent() {
		return content;
	}

	public void turnOn() {
		onSwitch = true;
	}

	public void turnOff() {
		onSwitch = false;
	}

	public String removeContent(int i) {
		if (i <= content.size() || i < 0){
			return content.remove(i);
		} else {
			return null;
		}
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
