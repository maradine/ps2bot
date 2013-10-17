import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Properties;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.Types;
import java.sql.Connection;


public class Oracle {

	private static Connection getConnection(Properties props) throws SQLException {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			System.out.println("Exception instantiating driver.");
			System.out.println(ex);
			throw new SQLException("Failed to load up the drivers.");
		}
		String dbServer = props.getProperty("db_server");
		String database = props.getProperty("database");
		String dbUsername = props.getProperty("db_username");
		String dbPassword = props.getProperty("db_password");
                Connection conn = DriverManager.getConnection("jdbc:mysql://"+dbServer+"/"+database+"?user="+dbUsername+"&password="+dbPassword);
		return conn;
	}

	
	public static ArrayList<TimePeriod> getPeriods(Properties props) throws SQLException {
		//Returns a hashmap id/name pairs for all weapon types (categories in SOE API parlance; this is where we swap their nomenclature)
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try {
			conn = getConnection(props);
			String sqlString = "SELECT * FROM fkpk.v2_time_periods ORDER BY end_time DESC;";
			stmt = conn.prepareStatement(sqlString);
			rs = stmt.executeQuery();
			ArrayList<TimePeriod> al = new ArrayList<TimePeriod>();
			while (rs.next()) {
				int id = rs.getInt(1);
				int start = rs.getInt(2);
				int end = rs.getInt(3);
				boolean isDaily = rs.getBoolean(4);
				TimePeriod tp = new TimePeriod(id, start, end, isDaily);
				al.add(tp);
			}
			return al;

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

                        if (stmt != null) {
                                try {
                                        stmt.close();
                                } catch (SQLException sqlEx) { } // ignore
                                stmt = null;

                        }
                        if (conn != null) {
                                try {
                                        conn.close();
                                } catch (SQLException sqlEx) { } // ignore
                                conn = null;

                        }
		}
	}
	
	public static HashMap<Integer, String> getTypes(Properties props) throws SQLException {
		//Returns a hashmap id/name pairs for all weapon types (categories in SOE API parlance; this is where we swap their nomenclature)
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try {
			conn = getConnection(props);
			String sqlString = "SELECT * FROM fkpk.v2_weapon_categories;";
			stmt = conn.prepareStatement(sqlString);
			rs = stmt.executeQuery();
			HashMap<Integer, String> hm = new HashMap<Integer, String>();
			while (rs.next()) {
				hm.put(rs.getInt(1), rs.getString(2));
			}
			return hm;

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

                        if (stmt != null) {
                                try {
                                        stmt.close();
                                } catch (SQLException sqlEx) { } // ignore
                                stmt = null;

                        }
                        if (conn != null) {
                                try {
                                        conn.close();
                                } catch (SQLException sqlEx) { } // ignore
                                conn = null;

                        }
		}
	}

	public static HashMap<Integer, String> getWeapons(Properties props, int type) throws SQLException {
		//Returns a hashmap of id/name pairs of weapons for the provided category.
		//If the provided type id doesn't map to anything, the hashmap will be empty.  Handled upstream.
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try {
			conn = getConnection(props);
			String sqlString = "SELECT id, name FROM fkpk.v2_weapons WHERE item_category_id = ?;";
			stmt = conn.prepareStatement(sqlString);
			stmt.setInt(1, type);
			rs = stmt.executeQuery();
			
			HashMap<Integer, String> hm = new HashMap<Integer, String>();
			while (rs.next()) {
				hm.put(rs.getInt(1), rs.getString(2));
			}
			return hm;

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

                        if (stmt != null) {
                                try {
                                        stmt.close();
                                } catch (SQLException sqlEx) { } // ignore
                                stmt = null;

                        }
                        if (conn != null) {
                                try {
                                        conn.close();
                                } catch (SQLException sqlEx) { } // ignore
                                conn = null;

                        }
		}
	}
	
	public static HashMap<String, String> getKillAggregate(Properties props, int id) throws SQLException {
		//figure out most revent daily and then run more granular call;

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try {
			conn = getConnection(props);
			String sqlString = "SELECT id "+
						"FROM fkpk.v2_time_periods "+
						"WHERE is_daily = 1 "+
						"ORDER BY end_time DESC "+
						"LIMIT 1;";
			stmt = conn.prepareStatement(sqlString);
			rs = stmt.executeQuery();
			rs.next();
			int period = rs.getInt(1);
			return getKillAggregate(props, id, period);
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

                        if (stmt != null) {
                                try {
                                        stmt.close();
                                } catch (SQLException sqlEx) { } // ignore
                                stmt = null;

                        }
                        if (conn != null) {
                                try {
                                        conn.close();
                                } catch (SQLException sqlEx) { } // ignore
                                conn = null;

                        }
		}
	}
	
	public static HashMap<String, String> getKillAggregate(Properties props, int id, int period) throws SQLException {

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try {
			conn = getConnection(props);
			String sqlString = "SELECT a.kills, a.uniques, round(a.kpu,1), round(a.avgbr,1), "+
						"round(a.q1kpu,1), round(a.q2kpu,1), round(a.q3kpu,1), round(a.q4kpu,1), b.name "+
						"FROM fkpk.v2_kill_aggregates as a, fkpk.v2_weapons as b "+
						"WHERE item_id = ? "+
						"AND period = ? "+
						"AND a.item_id = b.id;";
			stmt = conn.prepareStatement(sqlString);
			stmt.setInt(1, id);
			stmt.setInt(2, period);
			rs = stmt.executeQuery();
			
			HashMap<String, String> hm = new HashMap<String, String>();
			while (rs.next()) {
				hm.put("id", String.valueOf(id));
				hm.put("kills", String.valueOf(rs.getInt(1)));
				hm.put("uniques", String.valueOf(rs.getInt(2)));
				hm.put("kpu", String.valueOf(rs.getFloat(3)));
				hm.put("avgbr", String.valueOf(rs.getFloat(4)));
				hm.put("q1kpu", String.valueOf(rs.getFloat(5)));
				hm.put("q2kpu", String.valueOf(rs.getFloat(6)));
				hm.put("q3kpu", String.valueOf(rs.getFloat(7)));
				hm.put("q4kpu", String.valueOf(rs.getFloat(8)));
				hm.put("name", String.valueOf(rs.getString(9)));
			}
			return hm;
			

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

                        if (stmt != null) {
                                try {
                                        stmt.close();
                                } catch (SQLException sqlEx) { } // ignore
                                stmt = null;

                        }
                        if (conn != null) {
                                try {
                                        conn.close();
                                } catch (SQLException sqlEx) { } // ignore
                                conn = null;

                        }
		}
	}

	public static ArrayList<KillAggregateRow> getAllKillAggregates(Properties props, int id) throws SQLException {
		//get every period for a weapon

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		ArrayList<KillAggregateRow> al = new ArrayList<KillAggregateRow>();
		
		try {
			conn = getConnection(props);
			String sqlString = "SELECT a.kills, a.uniques, round(a.kpu,1), round(a.avgbr,1), "+
						"round(a.q1kpu,1), round(a.q2kpu,1), round(a.q3kpu,1), round(a.q4kpu,1), b.name, a.period "+
						"FROM fkpk.v2_kill_aggregates as a, fkpk.v2_weapons as b "+
						"WHERE item_id = ? "+
						"AND a.item_id = b.id "+
						"ORDER BY a.period ASC;";
			stmt = conn.prepareStatement(sqlString);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				KillAggregateRow kar = new KillAggregateRow(id, rs.getInt(1), rs.getInt(2), rs.getFloat(3), rs.getFloat(4), 
										rs.getFloat(5), rs.getFloat(6), rs.getFloat(7), rs.getFloat(8), 
										rs.getString(9), rs.getInt(10));
				al.add(kar);
			}
			
			return al;
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

                        if (stmt != null) {
                                try {
                                        stmt.close();
                                } catch (SQLException sqlEx) { } // ignore
                                stmt = null;

                        }
                        if (conn != null) {
                                try {
                                        conn.close();
                                } catch (SQLException sqlEx) { } // ignore
                                conn = null;

                        }
		}
	}

}


