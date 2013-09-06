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
				props.setProperty("irc_server", "irc.slashnet.org");
				props.setProperty("irc_channel", "#planetside2");
				props.setProperty("botnick", "ps2bot");
				props.setProperty("nickpass", "");
				props.setProperty("ownernick", "");
				props.setProperty("channelpass", "");
				props.setProperty("soeapikey", "");
				props.setProperty("db_server", "");
				props.setProperty("db_port", "");
				props.setProperty("database", "");
				props.setProperty("db_username", "");
				props.setProperty("db_password", "");
		
				props.store(new FileOutputStream("ps2bot.properties"), null);
			} catch (IOException ioe2) {
				System.out.println("There was an error writing to the filesystem.");
			}
			System.exit(1);

		} 			
		props.load(fis);
		if (!props.containsKey("irc_server") || !props.containsKey("irc_channel") || !props.containsKey("botnick")) {
			System.out.println("Config file is incomplete.  Delete it to receive a working template.");
			System.exit(1);
		}
		String ircServer = props.getProperty("irc_server");
		String ircChannel = props.getProperty("irc_channel");
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
		bot.connect(ircServer);
		
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
			bot.joinChannel(ircChannel);
		} else {
			bot.joinChannel(ircChannel, channelpass);
		}
		
		//pause to let channel join complete.  If we failed, exit.	
		Thread.sleep(5000);
		if (!bot.channelExists(ircChannel)) {
			System.out.println("*** Bot failed to connect to channel \""+ircChannel+"\".  Either the key is wrong, or the server is experiencing unusual load.");
			bot.shutdown(true);
		}

		
		//set up announcement engine
		AnnouncementEngine ae = new AnnouncementEngine(bot, ircChannel);
		Thread at = new Thread(ae, "at");
		at.start();

		//link announcement handler
		bot.getListenerManager().addListener(new AnnouncementHandler(ae, at));

		//set up presence engine
		String soeapikey = props.getProperty("soeapikey");
		PresenceEngine pe = new PresenceEngine(bot, ircChannel, soeapikey);
		Thread pt = new Thread(pe, "pt");
		pt.start();

		//link presence handler
		bot.getListenerManager().addListener(new PresenceHandler(pe, pt, soeapikey));

		//set up stat engine
		//StatCollectionEngine se = new StatCollectionEngine(bot, props);
		//Thread st = new Thread(se, "st");
		//st.start();

		//link stat handler
		//bot.getListenerManager().addListener(new StatCollectionHandler(se, st, props));

		//link general command handler
		bot.getListenerManager().addListener(new GeneralHandler(bot));
		bot.getListenerManager().addListener(new SpeechHandler(bot,ircChannel));

		//DISABLED
		//set up twitter listener
		//bot.getListenerManager().addListener(new TwitterListener(bot, channel, props));
	}
}

