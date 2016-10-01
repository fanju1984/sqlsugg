package sqlsugg.backends;
import java.sql.*;

public class SQLBackend {
	Connection conn;
	String dbName;
	
	public SQLBackend() {
        conn = null;
	}
	
	String mServer;
	String mUserName;
	String mPassword;
	String mDBName;
	
	public SQLBackend (String server, String userName, String password, String dbName) {
		mServer = server;
		mUserName = userName;
		mPassword = password;
		mDBName = dbName;
	}
	
	public void connectMySQL (String userName, String password, String dbName) 
		throws InstantiationException, Exception, 
		ClassNotFoundException, IllegalAccessException{
        /*String url = "jdbc:mysql://localhost/";
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection(url, userName, password);
        System.out.println("Database connection established");*/
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://localhost/sqlsugg_dblp_server?useUnicode=true&characterEncoding=gbk&jdbcCompliantTruncation=false", 
				userName, password);
		this.dbName = dbName;
		this.useDB(dbName);
	}
	
	public void connectMySQL (String server, String userName, String password, String dbName) 
		throws InstantiationException, Exception, 
		ClassNotFoundException, IllegalAccessException{
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://" + server + 
				"/sqlsugg_dblp_server?useUnicode=true&characterEncoding=gbk&jdbcCompliantTruncation=false", 
				userName, password);
		this.dbName = dbName;
		this.useDB(dbName);
}
	
	public void disconnectMySQL () throws SQLException{
		//System.out.println("disconnect...");
		conn.close();
		conn = null;
        //System.out.println("Database connection terminated");
	}
	
	public void useDB (String dbName) throws Exception {
		String stat = "USE " + dbName;
		this.execute(stat);
	}

	public void execute (String statement) throws Exception{
		//DebugPrinter.println("Now Execute: \n" + statement);
		Statement stat = null;
		
		try {
			stat = conn.createStatement();
		} catch (Exception e) {
			this.connectMySQL(mServer, mUserName, mPassword, mDBName);
			stat = conn.createStatement();
		}
		
		stat.execute(statement);
	}
	
//	public int executeUpdate (String statement) throws SQLException {
//		Statement stat = this.conn.createStatement();
//		return stat.executeUpdate(statement);
//	}
	
	public ResultSet executeQuery (String statement) throws Exception {
//		System.out.println("Now Execute: \n" + statement);
		Statement stat = null;
		try {
			stat = conn.createStatement();
		} catch (Exception e) {
			this.connectMySQL(mServer, mUserName, mPassword, mDBName);
			stat = conn.createStatement();
		}
		ResultSet rs = stat.executeQuery(statement);
		return rs;
	}
}
