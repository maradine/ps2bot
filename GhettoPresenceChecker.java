import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.IOException;

public class GhettoPresenceChecker {
//This is only useful until SOE's API is up.  Then, this is dead code.

	public static HashMap<String,Boolean> getPresence(int timeout) throws IOException {
		
		Document doc = null;
		Connection con = null;
		try {
			con = Jsoup.connect("http://www.planetside-universe.com/character.php?stats=maradine&tab=Friends&nojs=1");
			con.timeout(timeout);
			doc = con.get();
		} catch (IOException ioe) {
			System.out.println("***connection error to PSU***");
			throw ioe;
		}
			
		Elements players = doc.select("[id=tab_content-Friends] > div > table > tbody > tr > td:eq(0) > a");
		Elements statuses = doc.select("[id=tab_content-Friends] > div > table > tbody >tr:gt(0) > td:eq(3)");
			
		LinkedList<String> plist = new LinkedList<String>();
		LinkedList<Boolean> slist = new LinkedList<Boolean>();
		
		for (Element el : players) {
			plist.add(el.text());
		}
		for (Element el : statuses) {
			//System.out.println("check: "+el.text());
			if (el.text().equals("Online")) {
				//System.out.println("Match - true.");
				slist.add(true);
			} else {
				//System.out.println("No match - false");
				slist.add(false);
			}
		}

		HashMap<String,Boolean> hm = new HashMap<String,Boolean>();
		for (int i = 0; i < players.size(); i++) {
			hm.put(plist.get(i), slist.get(i));
			//System.out.println("item "+ plist.get(i) + " " + slist.get(i));
		}


		return hm;
		
	}

}


