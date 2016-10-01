package sqlsugg.config;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import sqlsugg.backends.*;

public class Catelog {
	
	SQLBackend sql;
	Map<String, Integer> catelog = new HashMap<String, Integer> ();
	
	private static Catelog ins = null;
	
	public static Catelog insCatelog (SQLBackend pSql) throws Exception {
		if (ins == null) {
			ins = new Catelog (pSql);
		}
		return ins;
	}
	
	private Catelog (SQLBackend pSql) throws Exception {
		sql = pSql;
		constructCatelog();
	}
	
	public Integer get (String tableName, 
			String aName, String statType) throws Exception {
		String key = tableName + "_" + aName + "_" + statType;
		Integer value = catelog.get(key);
		if (value == null) {
			value = getIdStatistic (tableName, aName, statType);
			updateCatelog (tableName, aName, statType, value);
		}
		return value;
	}
	
	private void constructCatelog () throws Exception {
		String stat = "CREATE TABLE IF NOT EXISTS catelog (" +
			"tablename varchar(100), aname varchar(100), " +
			"keyname varchar(100), value double)";
		sql.execute(stat);
		stat = "SELECT * FROM catelog";
		ResultSet rs = sql.executeQuery(stat);
		while (rs.next()) {
			String tableName = rs.getString("tablename");
			String keyName = rs.getString("keyname");
			String aName = rs.getString("aname");
			int value = rs.getInt("value");
			catelog.put(tableName + "_" + aName + "_" + keyName,  value);
		}
		rs.close();
	}
	
	private void updateCatelog (String tableName, String aName, 
			String statType, int value) throws Exception {
		catelog.put(tableName + "_" + aName + "+" + statType, value);
		String stat1 = "INSERT INTO catelog (tablename, aname, keyname, value) VALUES (" +
				"'" + tableName + "','" + aName + "', '" + 
				statType + "', " + value + ")";
		sql.execute(stat1);
	}
	
	private int getIdStatistic (String tableName, String idName, String statType) throws Exception {
		String stat = "SELECT " + statType + "(" + idName + ") as stat FROM " + tableName;
		ResultSet rs = sql.executeQuery(stat);
		int statValue = -1;
		if (rs.next()) {
			statValue = rs.getInt("stat");
		} 
		rs.close();
		return statValue;
	}
}
