package sqlsugg.launcher;

import java.sql.*;
import java.util.*;

import sqlsugg.backends.*;
import sqlsugg.config.Catelog;
import sqlsugg.selest.*;
import sqlsugg.util.HashFamily;

public class ConstructHists {
	public static void main (String args[]) throws Exception {
		SQLBackend sql = new SQLBackend ();
		sql.connectMySQL("166.111.68.40", "sqlsugg", "sqlsugg", "sqlsugg_dblp");
		int pHashNum = 100;
		int histWidth = 1000;
		HashFamily hashes = new HashFamily (pHashNum, 1000000);
		MHBucketGen gen = new MHBucketGen(sql, hashes, histWidth);
		constructInvHists (gen, histWidth, sql);
//		constructIdHists ("pa", "pid", gen, histWidth, sql);
//		constructIdHists ("paper", "id", gen, histWidth, sql);
//		constructIdHists ("author", "id", gen, histWidth, sql);
		sql.disconnectMySQL();
		sql = null;
	}
	
	public static void constructInvHists (MHBucketGen gen, int histWidth, SQLBackend sql) throws Exception {
		String tableName = "inv_index";
		String idName = "rcdid";
		String stat = "SELECT * FROM " + tableName ;
		ResultSet rs = sql.executeQuery(stat);
		System.out.println("Data Fetched!");
		Catelog catelog = Catelog.insCatelog(sql);
		Integer lb = catelog.get(tableName, idName, "MIN");
		Map<Integer, MHIdBucket> bucketPool = new TreeMap<Integer, MHIdBucket> ();
		String hkey = null;
		DBDump dump = new DBDump (sql);
		dump.initDump("INSERT INTO histograms VALUES", 100);
		int count = 0;
		int distinctCount = 0;
		while (rs.next()) {
			String keyword = rs.getString("word");
			int rcdid = rs.getInt(idName);
			if (hkey == null || !hkey.equals(keyword)) {
				if (hkey != null) {
					String histStr = "";
					for (int tmp: bucketPool.keySet()) {
						MHIdBucket bucket = bucketPool.get(tmp);
						bucket.finalize();
						histStr += bucket.toString() + "\t";
					}
					dump.addTuple("('" + hkey + "','" + histStr + "')");
					distinctCount ++;
				}
				// Process new histogram key
				bucketPool.clear();
				bucketPool = null;
				bucketPool = new TreeMap<Integer, MHIdBucket> ();
				hkey = keyword;
			}
			gen.assignEqWidthBucket(rcdid, lb, histWidth, bucketPool);
			count ++;
			if (count % 10000 == 0) {
				System.out.println ("Progress: " + count + " , " + distinctCount);
			}
		}
		rs.close();
		if (hkey != null) {
			String histStr = "";
			for (int tmp: bucketPool.keySet()) {
				MHIdBucket bucket = bucketPool.get(tmp);
				bucket.finalize();
				histStr += bucket.toString() + "\t";
			}
			dump.addTuple("('" + hkey + "','" + histStr + "')");
		}
		dump.finishDump();
	}
	
	public static void constructIdHists (String tableName, String idName, MHBucketGen gen, int histWidth, 
			SQLBackend sql) throws Exception {
		Catelog catelog = Catelog.insCatelog(sql);
		Integer lb = catelog.get(tableName, idName, "MIN");
		Map<Integer, MHIdBucket> bucketPool = new TreeMap<Integer, MHIdBucket> ();
		String stat = "SELECT " + idName + " FROM " + tableName;// + " LIMIT 10000";
		DBDump dump = new DBDump (sql);
		dump.initDump("INSERT INTO histograms VALUES", 100);
		ResultSet rs = sql.executeQuery(stat);
		System.out.println("Data Fetched: " + stat);
		int count = 0;
		while (rs.next()) {
			int id = rs.getInt(idName);
			gen.assignEqWidthBucket(id, lb, histWidth, bucketPool);
			count ++;
			if (count % 1000000 == 0) {
				System.out.println ("Progress: " + count );
			}
		}
		rs.close();
		String histStr = "";
		for (int tmp: bucketPool.keySet()) {
			MHIdBucket bucket = bucketPool.get(tmp);
			bucket.finalize();
			histStr += bucket.toString() + "\t";
		}
		String hkey = tableName + "." + idName;
		dump.addTuple("('" + hkey + "','" + histStr + "')");
		dump.finishDump();
	}
}
