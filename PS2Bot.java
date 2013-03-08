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
				props.setProperty("ownernick", "");
				props.setProperty("channelpass", "");
				props.setProperty("soeapikey", "");

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
		String ownernick = props.getProperty("ownernick");
		
		//seed the permissions manager
		PermissionsManager pm = PermissionsManager.initInstance(ownernick);
		
		//add misc listeners
		bot.getListenerManager().addListener(new BanterBox());
		bot.getListenerManager().addListener(new PermissionsHandler());

		//connect
		bot.setVerbose(true);
		bot.setName(botnick);
		bot.connect(server);
		
		//identify with nickserv if so enabled
		String nickpass = props.getProperty("nickpass");
		if (nickpass != null) {
			if (!nickpass.isEmpty()) {
				bot.identify(nickpass);
			}
		}

		//join channel, passing key if needed
		String channelpass = props.getProperty("channelpass");
		if (channelpass==null || channelpass.equals("")) {
			bot.joinChannel(channel);
		} else {
			bot.joinChannel(channel, channelpass);
		}
		
		//pause to let channel join complete.  If we failed, exit.	
		Thread.sleep(5000);
		if (!bot.channelExists(channel)) {
			System.out.println("*** Bot failed to connect to channel \""+channel+"\".  Either the key is wrong, or the server is experiencing unusual load.");
			bot.shutdown(true);
		}

		
		//set up announcement engine
		AnnouncementEngine ae = new AnnouncementEngine(bot, channel);
		Thread at = new Thread(ae, "at");
		at.start();

		//link announcement handler
		bot.getListenerManager().addListener(new AnnouncementHandler(ae, at));

		//set up presence engine
		String soeapikey = props.getProperty("soeapikey");
		PresenceEngine pe = new PresenceEngine(bot, channel, soeapikey);
		Thread pt = new Thread(pe, "pt");
		pt.start();

		//link presence handler
		bot.getListenerManager().addListener(new PresenceHandler(pe, pt, soeapikey));


		//link general command handler
		bot.getListenerManager().addListener(new GeneralHandler(bot));
		bot.getListenerManager().addListener(new SpeechHandler(bot,channel));
	}
}

