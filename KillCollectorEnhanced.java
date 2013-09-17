import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.parser.Parser;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.IOException;
import java.util.Properties;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.DriverManager;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;



public class KillCollectorEnhanced {

	public static HashMap<String, Integer> collectKills(int timeout, long interval, Properties props) throws IOException, SQLException {
		
		Document doc = null;
		org.jsoup.Connection jcon = null;
		String soeapikey = props.getProperty("soeapikey");
		String querystring;

		Long startingTimestampNumber = ((System.currentTimeMillis()/1000L)-90L);
		String startingTimestamp = startingTimestampNumber.toString();


		if (soeapikey == null || soeapikey.equals("")) {
			querystring = "http://census.soe.com/xml/get/ps2:v2/event?type=KILL&after="+startingTimestamp+"&c:limit=1000&c:join=type:character^on:attacker_character_id^to:character_id^show:faction_id'battle_rank.value";
		} else {
			querystring = "http://census.soe.com/s:"+soeapikey+"/xml/get/ps2:v2/event?type=KILL&after="+startingTimestamp+"&c:limit=1000&c:join=type:character^on:attacker_character_id^to:character_id^show:faction_id'battle_rank.value";
		}
		System.out.println("running "+ querystring);
		try {
			jcon = Jsoup.connect(querystring);
			jcon.timeout(timeout);
			jcon.parser(Parser.xmlParser());
			doc = jcon.get();
		} catch (IOException ioe) {
			System.out.println("***connection error to SOE***");
			System.out.println(ioe);
			throw ioe;
		}

		Elements killEvents = doc.select("event_list > event");
		int killEventsSize = killEvents.size();


		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			System.out.println("SOMETHING BLEW THE FUCK UP WHILE LOADING THE DRIVER");
                }

		java.sql.Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String insertString = "insert into v2_kills (attacker_character_id, attacker_vehicle_id, attacker_weapon_id, character_id, is_critical, is_headshot, "+
			"timestamp, world_id, zone_id, faction_id, br_value) values (?,?,?,?,?,?,?,?,?,?,?)";
		String logString = "insert into v2_api_pulls (timestamp, dupes, first_timestamp_pulled, last_timestamp_pulled, write_time, `interval`) values (?, ?, ?, ?, ?, ?)";

		String dbServer = props.getProperty("db_server");
		String database = props.getProperty("database");
		String dbUsername = props.getProperty("db_username");
		String dbPassword = props.getProperty("db_password");
		

                int skipCount = 0;
                int minTimestamp=2000000000;
                int maxTimestamp=0;
                long startTimeMillis = System.currentTimeMillis();
		int duration = 0;
		int[] updateCounts=new int[0];

		try {
                        conn = DriverManager.getConnection("jdbc:mysql://"+dbServer+"/"+database+"?user="+dbUsername+"&password="+dbPassword);
                        conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(insertString);
                        for (int i=0;i<killEventsSize;i++) {
                        
                        	long attacker_character_id = Long.parseLong(killEvents.get(i).attr("attacker_character_id"));
                                pstmt.setLong(1,attacker_character_id);
                                int attacker_vehicle_id = Integer.parseInt(killEvents.get(i).attr("attacker_vehicle_id"));
                                pstmt.setInt(2,attacker_vehicle_id);
                                int attacker_weapon_id = Integer.parseInt(killEvents.get(i).attr("attacker_weapon_id"));
                                pstmt.setInt(3,attacker_weapon_id);
                                long character_id = Long.parseLong(killEvents.get(i).attr("character_id"));
                                pstmt.setLong(4,character_id);
                                int is_critical = Integer.parseInt(killEvents.get(i).attr("is_critical"));
                                pstmt.setInt(5,is_critical);
                                int is_headshot = Integer.parseInt(killEvents.get(i).attr("is_headshot"));
                                pstmt.setInt(6,is_headshot);
                                int timestamp = Integer.parseInt(killEvents.get(i).attr("timestamp"));
                                pstmt.setInt(7,timestamp);
                                int world_id = Integer.parseInt(killEvents.get(i).attr("world_id"));
                                pstmt.setInt(8,world_id);
                                int zone_id = Integer.parseInt(killEvents.get(i).attr("zone_id"));
                                pstmt.setInt(9,zone_id);
                                
				//values that might not exist
				int faction_id=-1;
				if (killEvents.get(i).childNodeSize()>0) {
					faction_id = Integer.parseInt(killEvents.get(i).childNode(0).attr("faction_id"));
				}
                                pstmt.setInt(10,faction_id);
                                
				int br_value=-1;
				if (killEvents.get(i).childNodeSize()>0) {
					if (killEvents.get(i).childNode(0).childNodeSize()>0) {
						br_value = Integer.parseInt(killEvents.get(i).childNode(0).childNode(0).attr("value"));
					}
				}
                                pstmt.setInt(11,br_value);
                                //
				pstmt.addBatch();
                                if (timestamp < minTimestamp) {
                                        minTimestamp = timestamp;
                                }
                                if (timestamp > maxTimestamp) {
                                        maxTimestamp = timestamp;
                                }
                        }
			updateCounts = pstmt.executeBatch();
			
                } catch (BatchUpdateException ex) {

                        System.out.println("Batch Update Exception - dupes in load stream.");
			updateCounts = ex.getUpdateCounts();
		} catch (SQLException ex) {

                        System.out.println("SQLException: " + ex.getMessage());
                        System.out.println("SQLState: " + ex.getSQLState());
                        System.out.println("VendorError: " + ex.getErrorCode());
			throw ex;
		}
		
			
		conn.commit();
		
		for (int i=0; i<updateCounts.length; i++) {
			if (updateCounts[i] == Statement.EXECUTE_FAILED) {
				skipCount++;
			}
		}
                long endTimeMillis = System.currentTimeMillis();
                duration = (int)(endTimeMillis-startTimeMillis);
                try {
			pstmt = conn.prepareStatement(logString);


                        pstmt.setInt(1, Integer.parseInt(startingTimestamp));
                        pstmt.setInt(2, skipCount);
                        pstmt.setInt(3, minTimestamp);
                        pstmt.setInt(4, maxTimestamp);
                        pstmt.setInt(5, duration);
                        pstmt.setLong(6, interval);
                        pstmt.executeUpdate();
			conn.commit();



                } catch (BatchUpdateException ex) {

                        System.out.println("Batch Update Exception: " + ex.getMessage());
                        System.out.println("SQLState: " + ex.getSQLState());
                        System.out.println("VendorError: " + ex.getErrorCode());
			throw ex;
		} catch (SQLException ex) {

                        System.out.println("SQLException: " + ex.getMessage());
                        System.out.println("SQLState: " + ex.getSQLState());
                        System.out.println("VendorError: " + ex.getErrorCode());
			throw ex;

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
                
		HashMap<String, Integer> returner = new HashMap<String, Integer>();
		returner.put("startingTimestamp", Integer.parseInt(startingTimestamp));
		returner.put("skipCount", skipCount);
		returner.put("minTimestamp", minTimestamp);
		returner.put("maxTimestamp", maxTimestamp);
		returner.put("duration", duration);



		return returner;

		
	}

}


