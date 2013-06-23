import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import twitter4j.FilterQuery;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;

public class TwitterListener extends ListenerAdapter<PircBotX> {
	private TwitterStream ts;
	private Set<String> usersToFollow;
	private PircBotX bot;
	private String channel;

	public TwitterListener(final PircBotX bot, final String channel,
			Properties props) {
		this.bot = bot;
		this.channel = channel;
		usersToFollow = new HashSet<String>();

		StatusListener listener = new StatusListener() {
			@Override
			public void onException(Exception arg0) {
				System.err.println(arg0.getMessage());
				arg0.printStackTrace();
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
			}

			@Override
			public void onStatus(Status status) {
				bot.sendMessage(channel, "@" + status.getUser().getScreenName()
						+ ": " + status.getText());
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
			}
		};

		ts = TwitterStreamFactory.getSingleton();
		ts.addListener(listener);

		// Check for twitter handles property
		String handles = props.getProperty("twitterhandles");
		if (handles != null) {
			for (String handle : handles.split(",")) {
				usersToFollow.add(handle.toLowerCase());
			}
		}

		setFilter();
	}

	public void onMessage(MessageEvent event) {

		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		Scanner scan;

		if (command.startsWith("!twitter")) {
			scan = new Scanner(command);
			String token = scan.next();

			if (scan.hasNext()) {
				token = scan.next().toLowerCase();
				switch (token) {
				case "add":
					if (scan.hasNext()) {
						String name = scan.next();
						usersToFollow.add(name.toLowerCase());
						setFilter();
						bot.sendMessage(channel, "Added " + name
								+ " to twitter feeds.");
					}
					break;
				case "remove":
					if (scan.hasNext()) {
						String name = scan.next();
						usersToFollow.add(name.toLowerCase());
						setFilter();
						bot.sendMessage(channel, "Removed " + name
								+ " from twitter feeds.");
					}
					break;
				default:
					event.respond("I'm not sure what you asked me.  Valid commands are \"add\" and \"remove\".");
					break;
				}
			}
		}
	}

	private void setFilter() {
		Twitter tw = TwitterFactory.getSingleton();

		ResponseList<User> userIdents;
		try {
			userIdents = tw.lookupUsers(usersToFollow.toArray(new String[0]));
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		/**
		 * lookup IDs for usernames
		 */
		long[] followArray = new long[usersToFollow.size()];
		int i = 0;
		for (User u : userIdents) {
			followArray[i] = u.getId();
			i++;
		}

		ts.filter(new FilterQuery(0, followArray));
	}

}
