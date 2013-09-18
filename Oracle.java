import java.util.HashMap;
import java.util.LinkedList;
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

	
	
	public static HashMap<Integer, String> getTypes(Properties props) throws SQLException {

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


}


