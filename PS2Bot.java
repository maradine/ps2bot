import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class PS2Bot extends ListenerAdapter {
	
	public static void main(String[] args) throws Exception {
		
		//create
		PircBotX bot = new PircBotX();
		
		String activeChannel = "#planetside2";
		
		
		//add listeners
		//bot.getListenerManager().addListener(new JoinDetector());
		bot.getListenerManager().addListener(new BanterBox());
		bot.getListenerManager().addListener(new PlayTracker());
		bot.getListenerManager().addListener(new TZTest());
		


		//execute
		bot.setVerbose(true);
		bot.setName("SWAGMOWER");
		bot.connect("irc.slashnet.org");
		bot.joinChannel(activeChannel);

		//set up announcement engine
		AnnouncementEngine ae = new AnnouncementEngine(bot, activeChannel);
		Thread at = new Thread(ae, "at");
		at.start();

		//link announcement handler
		bot.getListenerManager().addListener(new AnnouncementHandler(ae, at));



	}
}

