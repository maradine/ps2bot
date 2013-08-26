import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import java.util.Properties;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.parser.Parser;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.IOException;

class PopulateV2Weapons {


	public static void main(String args[]) {


	//load properties from disk
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("utility.properties");
		} catch (IOException ioe) {
			System.out.println("Can't find utility.proprties in local directory.");
			System.out.println("Wrting out example file and terminating.");
			System.out.println("Modify this file and re-run.");

			try {
				props.setProperty("server", "fkpk1.c77v5riasird.us-west-2.rds.amazonaws.com");
				props.setProperty("port", "3306");
				props.setProperty("database", "fkpk");
				props.setProperty("db_username", "blah");
				props.setProperty("db_password", "blah");
				props.setProperty("soeapikey", "1234");

				props.store(new FileOutputStream("utility.properties"), null);
			} catch (IOException ioe2) {
				System.out.println("There was an error writing to the filesystem.");
			}
			System.exit(1);

		} 			
		try {
			props.load(fis);
		} catch (IOException ioe) {
			System.out.println("We've failed to load the properties file, despite it being, you know, there.");
			System.out.println("Not paid for this.  Aborting.");
			System.exit(1);
		}
		if (!props.containsKey("server") || !props.containsKey("database") || !props.containsKey("db_username") || !props.containsKey("db_password")) {
			System.out.println("Config file is incomplete.  Delete it to receive a working template.");
			System.exit(1);
		}
		String server = props.getProperty("server");
		String port = props.getProperty("port");
		String database = props.getProperty("database");
		String db_username = props.getProperty("db_username");
		String db_password = props.getProperty("db_password");
		String soeapikey = props.getProperty("soeapikey");



		Document doc = null;
		org.jsoup.Connection jcon = null;
		String querystring;

		Long startingTimestampNumber = ((System.currentTimeMillis()/1000L)-90L);
		String startingTimestamp = startingTimestampNumber.toString();

		if (soeapikey == null || soeapikey.equals("")) {
			querystring = "http://census.soe.com/xml/get/ps2:v2/item?item_type_id=26&c:limit=1000&c:has=name.en,description.en";
		} else {
			querystring = "http://census.soe.com/s:"+soeapikey+"/xml/get/ps2:v2/item?item_type_id=26&c:limit=1000&c:has=name.en,description.en";
		}
		System.out.println("running "+ querystring);
		try {
			jcon = Jsoup.connect(querystring);
			jcon.timeout(10000);
			jcon.parser(Parser.xmlParser());
			doc = jcon.get();
		} catch (IOException ioe) {
			System.out.println("***connection error to SOE***");
			System.out.println(ioe);
		}

		Elements weapons = doc.select("item_list > item"); 			

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			System.out.println("SOMETHING BLEW THE FUCK UP WHILE LOADING THE DRIVER");
		}	
	
		java.sql.Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String insertString = "replace into fkpk.v2_weapons (id, faction_id, image_id, image_path, image_set_id, is_vehicle_weapon, "+
					"item_category_id, item_type_id, max_stack_size, skill_set_id, name, description) "+
					"values (?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			conn = DriverManager.getConnection("jdbc:mysql://"+server+"/"+database+"?"+"user="+db_username+"&password="+db_password);
			pstmt = conn.prepareStatement(insertString);
			for (Element w : weapons) {
				int id = Integer.parseInt(w.attr("item_id"));
				int factionId;
				if (w.attr("faction_id").equals("")) {
					factionId = 0;
				} else {
					factionId = Integer.parseInt(w.attr("faction_id"));
				}				
				int imageId = Integer.parseInt(w.attr("image_id"));
				String imagePath = w.attr("image_path");
				int imageSetId = Integer.parseInt(w.attr("image_set_id"));
				int isVehicleWeapon = Integer.parseInt(w.attr("is_vehicle_weapon"));
				int itemCategoryId;
				if (w.attr("item_category_id").equals("")) {
					itemCategoryId=0;
				} else {
					itemCategoryId = Integer.parseInt(w.attr("item_category_id"));
				}
				int itemTypeId = Integer.parseInt(w.attr("item_type_id"));
				int maxStackSize = Integer.parseInt(w.attr("max_stack_size"));
				int skillSetId;
				if (w.attr("skill_set_id").equals("")) {
					skillSetId=0;
				} else {
					skillSetId = Integer.parseInt(w.attr("skill_set_id"));
				}
				String name = w.childNode(0).attr("en");
				String description = w.childNode(1).attr("en");
				
				pstmt.setInt(1,id);
				pstmt.setInt(2,factionId);
				pstmt.setInt(3,imageId);
				pstmt.setString(4,imagePath);
				pstmt.setInt(5,imageSetId);
				pstmt.setInt(6,isVehicleWeapon);
				pstmt.setInt(7,itemCategoryId);
				pstmt.setInt(8,itemTypeId);
				pstmt.setInt(9,maxStackSize);
				pstmt.setInt(10,skillSetId);
				pstmt.setString(11,name);
				pstmt.setString(12,description);
				
				pstmt.executeUpdate();
			}
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			System.exit(1);

		} finally {
			if (rs != null) {
       				try {
           					rs.close();
     					} catch (SQLException sqlEx) { } // ignore
				rs = null;
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException sqlEx) { } // ignore
				pstmt = null;

			}
		}
	}
}

