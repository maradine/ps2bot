import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.parser.Parser;
import java.util.HashMap;
import java.util.ArrayList;
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
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.AddrUtil;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;



/*
do a big weapon_play_time pull
for each entry:
	create key from character_id+item_id
	get key from cache
	if key exists and last_save < this last_save
		compute diff of value field
		insert time record into db
		write new key to cache
	else if key exists and last_save = this last_save
		increment dupe count
	else
		query SOE for prior entry
		compute diff of value field
		insert time record into db
		write new key to cache

*/



public class PlaytimeCollector {

	public static HashMap<String, Integer> collectPlaytime(int timeout, long interval, Properties props) throws TimeoutException, IOException, SQLException, InterruptedException, ExecutionException {
		
		Document doc = null;
		org.jsoup.Connection jcon = null;
		String soeapikey = props.getProperty("soeapikey");
		String querystring;
	
	
		MemcachedClient mcc = null;	
		String memcachedServer = props.getProperty("memcached_server", "");
		mcc = new MemcachedClient(new BinaryConnectionFactory(),AddrUtil.getAddresses(memcachedServer));

		if (soeapikey == null || soeapikey.equals("")) {
			querystring = "http://census.soe.com/xml/get/ps2:v2/characters_weapon_stat?stat_name=weapon_play_time&c:sort=last_save_date:-1&c:limit=5000&c:show=character_id,item_id,last_save,value,vehicle_id";
		} else {
			querystring = "http://census.soe.com/s:"+soeapikey+"/xml/get/ps2:v2/characters_weapon_stat?stat_name=weapon_play_time&c:sort=last_save_date:-1&c:limit=5000&c:show=character_id,item_id,last_save,value,vehicle_id";
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

		Elements playtimeEvents = doc.select("characters_weapon_stat_list > characters_weapon_stat");

		int hitCounter = 0;
		int dupeCounter = 0;
		int updateCounter = 0;

		ArrayList<PlaytimeRow> alPtr = new ArrayList<PlaytimeRow>();
		int pullTimestamp = 2000000000;

		for (Element el : playtimeEvents) {
			String characterId = el.attr("character_id");
			String itemId = el.attr("item_id");
			String lastSave = el.attr("last_save");
			String value = el.attr("value");
			String vehicleId = el.attr("vehicle_id");
			//System.out.println("ELEMENT: "+characterId+" "+itemId+" "+lastSave+" "+value+" "+vehicleId);
			
			if (Integer.parseInt(lastSave) < pullTimestamp) {
				pullTimestamp = Integer.parseInt(lastSave);
			}

			PlaytimeRow ptr = new PlaytimeRow(Long.parseLong(characterId), Integer.parseInt(itemId), Integer.parseInt(lastSave), Integer.parseInt(value), Integer.parseInt(vehicleId));
			String cacheKey = characterId+"+"+itemId+"+"+vehicleId;
			Future<Object> future = mcc.asyncGet(cacheKey);
			Object cachedObject = null;
			try {
				cachedObject = future.get(5, TimeUnit.SECONDS); //make this variable
			} catch (TimeoutException e) {
				future.cancel(false);
				System.out.println("Timeout to memcached exceeded.");
				throw e;
			} catch (Exception e) {
				future.cancel(false);
				System.out.println("UNHANDLED CONCURRENCY EXCPETION");
				throw e;
			}
		
			if (cachedObject == null) {
			//	System.out.println("KEY "+cacheKey+" IS NOT IN CACHE");
				Future<Boolean> setter = mcc.set(cacheKey, 0, ptr);
				try {
					Boolean success = setter.get(5, TimeUnit.SECONDS);
					if (success) {
			//			System.out.println("WROTE NEW RECORD TO CACHE");
					}
				} catch (TimeoutException e) {
					setter.cancel(false);
					System.out.println("Timeout to memcached exceeded.");
					throw e;
				} catch (Exception e) {
					setter.cancel(false);
					System.out.println("UNHANDLED CONCURRENCY EXCPETION");
					throw e;
				}

			} else { 
			//	System.out.println("KEY "+cacheKey+" CACHE HIT");
				hitCounter++;
				PlaytimeRow cachedPtr = (PlaytimeRow)cachedObject;
				if (ptr.getLastSave() > cachedPtr.getLastSave()) {
			//		System.out.println("PULLED RECORD IS NEWER THAN CACHE.");
			
					Future<Boolean> setter = mcc.set(cacheKey, 0, ptr);
					try {
						Boolean success = setter.get(5, TimeUnit.SECONDS);
						if (success) {
				//			System.out.println("WROTE UPDATED RECORD TO CACHE");
							updateCounter++;
							int diff = ptr.getValue() - cachedPtr.getValue();
							ptr.setValue(diff);
							alPtr.add(ptr);
						}
					} catch (TimeoutException e) {
						setter.cancel(false);
						System.out.println("Timeout to memcached exceeded.");
						throw e;
					} catch (Exception e) {
						setter.cancel(false);
						System.out.println("UNHANDLED CONCURRENCY EXCPETION");
						throw e;
					}
			
				} else {
			//		System.out.println("DUPLICATE RECORD.");
					dupeCounter++;
				}
			}

		}

		System.out.println(playtimeEvents.size() + " playtime events processed.");
		System.out.println(hitCounter + " cache hits.");
		System.out.println(dupeCounter + " duplicate events.");
		System.out.println(updateCounter + " events updated.");
		int newRows = playtimeEvents.size() - dupeCounter;
		float percentWarm = (float)updateCounter / (float)newRows * 100;
				
		System.out.println("Cache is "+percentWarm+"% warm.");
		
		HashMap<String, Integer> results = new HashMap<String,Integer>();

		results.put("playtimeEvents", playtimeEvents.size());
		results.put("newRows", newRows);
		results.put("updateCounter", updateCounter);
		results.put("dupeCounter", dupeCounter);
		results.put("hitCounter", hitCounter);



		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			System.out.println("SOMETHING BLEW THE FUCK UP WHILE LOADING THE DRIVER");
                }

		java.sql.Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String insertString = "insert into v2_playtimes (character_id, item_id, last_save, vehicle_id, diff) values (?,?,?,?,?)";
		String logString = "insert into v2_api_pulls (timestamp, dupes, first_timestamp_pulled, last_timestamp_pulled, write_time, `interval`, stat, cache_heat, updated_rows) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String dbServer = props.getProperty("db_server");
		String database = props.getProperty("database");
		String dbUsername = props.getProperty("db_username");
		String dbPassword = props.getProperty("db_password");
		

                int minTimestamp=2000000000;
                int maxTimestamp=0;
                long startTimeMillis = System.currentTimeMillis();
		int duration = 0;
		int[] updateCounts=new int[0];

		try {
                        conn = DriverManager.getConnection("jdbc:mysql://"+dbServer+"/"+database+"?user="+dbUsername+"&password="+dbPassword);
                        conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(insertString);
                        for (PlaytimeRow ptr : alPtr) {
                        
                                pstmt.setLong(1,ptr.getCharacterId());
                                pstmt.setInt(2,ptr.getItemId());
                                pstmt.setLong(3,ptr.getLastSave());
                                pstmt.setInt(4,ptr.getVehicleId());
                                pstmt.setInt(5,ptr.getValue());
				
				pstmt.addBatch();
                                //System.out.println("for shits, last save is "+ptr.getLastSave());
				if (ptr.getLastSave() < minTimestamp) {
                                        minTimestamp = ptr.getLastSave();
                                }
                                if (ptr.getLastSave() > maxTimestamp) {
                                        maxTimestamp = ptr.getLastSave();
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
		
                long endTimeMillis = System.currentTimeMillis();
                duration = (int)(endTimeMillis-startTimeMillis);
                try {
			pstmt = conn.prepareStatement(logString);


                        pstmt.setInt(1, pullTimestamp);
                        pstmt.setInt(2, dupeCounter);
                        pstmt.setInt(3, minTimestamp);
                        pstmt.setInt(4, maxTimestamp);
                        pstmt.setInt(5, duration);
                        pstmt.setLong(6, interval);
                        pstmt.setString(7, "playtimes");
			pstmt.setFloat(8, percentWarm);
                        pstmt.setInt(9, updateCounter);
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
                

		return results;

		
	}

}


