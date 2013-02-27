import org.pircbotx.User;
import org.pircbotx.Channel;
import java.util.Set;

public class PermissionsManager {

	private static PermissionsManager instance;
	private static Boolean locked;
	private static String owner;

	private PermissionsManager(String setowner) {
		locked = true;
		this.owner = setowner;
	}

	public static synchronized PermissionsManager initInstance(String owner) {
		instance = new PermissionsManager(owner);
		return instance;
	}

	public static synchronized PermissionsManager getInstance() {
		return instance;
	}

	public static void lock() {
		locked = true;
	}

	public static void unlock() {
		locked = false;
	}

	public static boolean isAllowed(String command, User user, Channel channel) {

		// not currently doing any command checking
		Boolean isallowed = false;
		if (user.getNick().equals(owner)) {
			isallowed = true;
		}
		if (!locked) {
			Set<Channel> channels = user.getChannelsOpIn();
			if (channels.contains(channel)) {
				isallowed = true;
			}
		}
		return isallowed;
	}
	
}
