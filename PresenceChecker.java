import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.parser.Parser;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.IOException;

public class PresenceChecker {

	public static HashMap<String,Boolean> getPresence(String outfitalias, int timeout) throws IOException {
		
		Document doc = null;
		Connection con = null;
		try {
			con = Jsoup.connect("http://census.soe.com/xml/get/ps2-beta/outfit/?alias="+outfitalias+"&c:resolve=member_character(character_id)");
			con.timeout(timeout);
			con.parser(Parser.xmlParser());
			doc = con.get();
		} catch (IOException ioe) {
			System.out.println("***connection error to SOE***");
			System.out.println(ioe);
			throw ioe;
		}
		Elements idelements = doc.select("outfit_list > outfit > members_list > members[id]"); 			

		LinkedList<String> idlist = new LinkedList<String>();
		LinkedList<String> namelist = new LinkedList<String>();
		LinkedList<Boolean> statuslist = new LinkedList<Boolean>();

		for (Element el : idelements) {
			String id = el.attr("id");
			idlist.add(id);
		}

		for (String id : idlist) {

			System.out.println("iterating over "+id);
			try {
				con = Jsoup.connect("http://census.soe.com/xml/get/ps2-beta/character/?id="+id+"&c:resolve=online_status&c:show=name");
				con.timeout(timeout);
				con.parser(Parser.xmlParser());
				doc = con.get();
			} catch (IOException ioe) {
				System.out.println("***connection error to SOE***");
				System.out.println(ioe);
				throw ioe;
			}
			Element el = doc.select("character_list > character > name").first();
			String name = el.attr("first_lower");
			namelist.add(name);
			el = doc.select("character_list > character").first();
			String status = el.attr("online_status");
			if (status.equals("2")) {
				statuslist.add(true);
			} else {
				statuslist.add(false);
			}
		
		}

		HashMap<String,Boolean> hm = new HashMap<String,Boolean>();
		for (int i = 0; i < idlist.size(); i++) {
			hm.put(namelist.get(i), statuslist.get(i));
		}

		return hm;
		
	}

}


