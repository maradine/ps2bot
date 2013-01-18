import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class PS2Bot extends ListenerAdapter {
	
	public static void main(String[] args) throws Exception {
		
		//instantiate underlying bot
		PircBotX bot = new PircBotX();

		//load properties from disk
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("ps2bot.properties");
		} catch (IOException ioe) {
			System.out.println("Can't find ps2bot.proprties in local directory.");
			System.out.println("Wrting out example file and terminating.");
			System.out.println("Modify this file and re-run.");
			
			try {
				props.setProperty("server", "irc.slashnet.org");
				props.setProperty("channel", "#planetside2");
				props.setProperty("botnick", "ps2bot");
				props.setProperty("nickpass", "");

				props.store(new FileOutputStream("ps2bot.properties"), null);
			} catch (IOException ioe2) {
				System.out.println("There was an error writing to the filesystem.");
			}
			System.exit(1);

		} 			
		props.load(fis);
		if (!props.containsKey("server") || !props.containsKey("channel") || !props.containsKey("botnick")) {
			System.out.println("Config file is incomplete.  Delete it to receive a working template.");
			System.exit(1);
		}
		String server = props.getProperty("server");
		String channel = props.getProperty("channel");
		String botnick = props.getProperty("botnick");
		
		//add listeners
		//bot.getListenerManager().addListener(new JoinDetector());
		bot.getListenerManager().addListener(new BanterBox());
		bot.getListenerManager().addListener(new PlayTracker());
		bot.getListenerManager().addListener(new TZTest());
		


		//execute
		bot.setVerbose(true);
		bot.setName(botnick);
		bot.connect(server);
		bot.joinChannel(channel);

		//set up announcement engine
		AnnouncementEngine ae = new AnnouncementEngine(bot, channel);
		Thread at = new Thread(ae, "at");
		at.start();

		//link announcement handler
		bot.getListenerManager().addListener(new AnnouncementHandler(ae, at));



	}
}

