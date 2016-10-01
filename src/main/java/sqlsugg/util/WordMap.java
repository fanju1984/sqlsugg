package sqlsugg.util;

import java.sql.*;

import sqlsugg.backends.*;

public class WordMap {
	String dbName;
	
	SQLBackend sql;
	
	int maxID;
	
	public WordMap (String dn, SQLBackend s) throws Exception{
		dbName = dn;
		maxID = -1;
		sql = s;
		this.createDBTable();
	}
	
	void createDBTable () throws Exception {
		String stat = "DROP TABLE IF EXISTS wm";
		sql.execute(stat);
		stat = "CREATE TABLE wm (id integer, word varchar(200))";
		sql.execute(stat);
		stat = "CREATE INDEX wm1Index on wm (id)";
		sql.execute(stat);
		stat = "CREATE INDEX wm2Index on wm (word)";
		sql.execute(stat);
	}
	
	public Integer getID (String word) throws Exception {
		String stat = "SELECT id FROM wm WHERE word='" + word + "'";
		ResultSet rs = sql.executeQuery(stat);
		if (rs.next()) {
			int wordID = rs.getInt("id");
			rs.close();
			return wordID;
		} else {
			return null;
		}
	}
	
	public String getWord (int id) throws Exception {
		String stat = "SELECT word FROM wm WHERE id = " + id;
		ResultSet rs = sql.executeQuery(stat);
		if (rs.next()) {
			String word = rs.getString("word");
			rs.close();
			return word;
		} else {
			return null;
		}
	}
	
	public int assignID (String word) throws Exception {
		maxID ++;
		int id = maxID;
		String stat = "INSERT INTO wm (id, word) VALUES (" + 
			id + ",\'" + word + "')";
		sql.execute(stat);
		return id;
	}
	
	public void destroy () throws Exception {
		String stat = "DROP TABLE wm";
		sql.execute(stat);
	}
	
	
}
