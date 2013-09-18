import java.util.HashMap;
import java.util.LinkedList;
import java.io.IOException;
import java.util.Properties;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.Types;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;



public class DailyAggregator {

	public static HashMap<String, Integer> aggregateKills(Properties props) throws SQLException {
		
		HashMap<String, Integer> returner = new HashMap<String,Integer>();
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			System.out.println("SOMETHING BLEW THE FUCK UP WHILE LOADING THE DRIVER");
                }

		java.sql.Connection conn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;
		String callString = "{call kills_nightly(?,?)}";

		String dbServer = props.getProperty("db_server");
		String database = props.getProperty("database");
		String dbUsername = props.getProperty("db_username");
		String dbPassword = props.getProperty("db_password");
		System.out.println("Entering DB connection block.");
		try {
                        conn = DriverManager.getConnection("jdbc:mysql://"+dbServer+"/"+database+"?user="+dbUsername+"&password="+dbPassword);
			stmt = conn.prepareCall(callString);
			stmt.registerOutParameter(1,Types.INTEGER);
			stmt.registerOutParameter(2,Types.INTEGER);
			System.out.println("Executing procedure.");
			boolean hadResults = stmt.execute();
			System.out.println("FOr the sake of argument - "+stmt.getInt(1)+" "+stmt.getInt(2));
			returner.put("deletedRows", stmt.getInt(1));
			returner.put("elapsedSeconds", stmt.getInt(2));
			System.out.println("I am totally returning now.");
			return returner;

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


