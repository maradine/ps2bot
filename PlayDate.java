import java.util.Date;
import java.util.LinkedList;
import org.pircbotx.User;
import java.util.TimeZone;

public class PlayDate implements Comparable<PlayDate> {

	Date date;
	LinkedList<User> players;
	
	public PlayDate() {
		this.date = new Date();
		this.players = new LinkedList<User>();
	}

	public PlayDate(User p, Date d) {
		this();
		this.date = d;
		this.players.add(p);
	}

	public LinkedList<User> getPlayers() {
		return this.players;
	}

	public void addPlayer(User p) {
		this.players.add(p);
	}

	public Date getDate() {
		return this.date;
	}

	public int compareTo(PlayDate o) {
		Date odate = o.getDate();
		return this.date.compareTo(odate);
	}

	
}
