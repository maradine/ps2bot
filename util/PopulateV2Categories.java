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

class PopulateV2Categories {


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

				props.store(new FileOutputStream("utility .properties"), null);
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
			querystring = "http://census.soe.com/xml/get/ps2:v2/item_category?c:limit=1000";
		} else {
			querystring = "http://census.soe.com/s:"+soeapikey+"/xml/get/ps2:v2/item_category?c:limit=1000";
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

		Elements categories = doc.select("item_category_list > item_category"); 			

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			System.out.println("SOMETHING BLEW THE FUCK UP WHILE LOADING THE DRIVER");
		}	
	
		java.sql.Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String insertString = "insert into fkpk.v2_item_categories (id, name) values (?,?)";

		try {
			conn = DriverManager.getConnection("jdbc:mysql://"+server+"/"+database+"?"+"user="+db_username+"&password="+db_password);
			pstmt = conn.prepareStatement(insertString);
			for (Element cat : categories) {
				int catId = Integer.parseInt(cat.attr("item_category_id"));
				String name = cat.childNode(0).attr("en");
				pstmt.setInt(1,catId);
				pstmt.setString(2,name);
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

