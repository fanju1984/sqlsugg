package sqlsugg.launcher;
import sqlsugg.backends.*;
import java.sql.*;

public class Tmp {
	public static void main (String args[]) throws Exception {
		SQLBackend sql = new SQLBackend ();
		sql.connectMySQL("166.111.68.40", "sqlsugg", "sqlsugg", "sqlsugg_dblp_server");
		String stat = "SELECT * FROM paper LIMIT 10";
		ResultSet rs = sql.executeQuery(stat);
		while (rs.next()) {
			String hStr = rs.getString("title");
			System.out.println (hStr);
		}
		rs.close();
		sql.disconnectMySQL();
	}
}
