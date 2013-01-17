import org.pircbotx.User;
import org.pircbotx.Channel;
import java.util.Set;

public class PermissionsManager {

	private static PermissionsManager instance;

	private PermissionsManager() {
	}

	public static synchronized PermissionsManager getInstance() {
		if (instance==null) {
			instance = new PermissionsManager();
		}
		return instance;
	}

	public static boolean isAllowed(String command, String nick) {
		if (nick.equals("maradine")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isAllowed(String command, User user, Channel channel) {
		Set<Channel> channels = user.getChannelsOpIn();
		if (channels.contains(channel)) {
			return true;
		} else {
			return false;
		}
	}
	
}
