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

	public static HashMap<String,Boolean> getPresence(String outfitalias, int timeout, String soeapikey) throws IOException {
		
		Document doc = null;
		Connection con = null;
		HashMap<String,Boolean> hm = new HashMap<String,Boolean>();
		String querystring;

		if (soeapikey == null || soeapikey.equals("")) {
			querystring = "http://census.soe.com/xml/get/ps2:v2/outfit/?alias="+outfitalias+"&c:resolve=member_character(id,name),member_online_status";
		} else {
			querystring = "http://census.soe.com/s:"+soeapikey+"/xml/get/ps2:v2/outfit/?alias="+outfitalias+"&c:resolve=member_character(id,name),member_online_status";
		}
		System.out.println("running "+ querystring);
		try {
			con = Jsoup.connect(querystring);
			con.timeout(timeout);
			con.parser(Parser.xmlParser());
			doc = con.get();
		} catch (IOException ioe) {
			System.out.println("***connection error to SOE***");
			System.out.println(ioe);
			throw ioe;
		}

		Elements nameelements = doc.select("outfit_list > outfit > members_list > members[character_id] > name"); 			
		Elements statuselements = doc.select("outfit_list > outfit > members_list > members[character_id]");
		int size = nameelements.size();

		for (int i=0;i<size;i++) {
			String name = nameelements.get(i).attr("first_lower");
			String statusstring = statuselements.get(i).attr("online_status");
			Boolean status = false;
			if (!statusstring.equals("0")) {
				status = true;
			}
			hm.put(name,status);
		}

		return hm;
		
	}

}


