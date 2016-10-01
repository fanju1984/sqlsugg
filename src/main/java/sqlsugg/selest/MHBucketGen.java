package sqlsugg.selest;

import sqlsugg.backends.*;
import sqlsugg.config.*;
import sqlsugg.util.*;

import java.util.*;
import java.sql.*;

public class MHBucketGen {
	SQLBackend sql;
	HashFamily hashes;
	int width;
	
	
	public static final int EQ_WIDTH = 0;
	public static final int EQ_DEPTH = 1;
	
	
	public MHBucketGen (SQLBackend pSql, HashFamily pHashes, int pWidth) throws Exception {
		sql = pSql;
		hashes = pHashes;
		width = pWidth;
	}
	
	public void assignEqWidthBucket (int id, int lb, int width, 
			Map<Integer, MHIdBucket> bucketPool) {
		int bid = (id - lb) / width;
		MHIdBucket bucket = bucketPool.get(bid);
		if (bucket == null) {
			int blb = lb + bid * width;
			int bub = blb + width - 1;
			bucket = new MHIdBucket (blb, bub, hashes);
		}
		bucket.addId(id);
		bucketPool.put(bid, bucket);
	}
	
	void assignEqDepthBucket (int id, int width, 
			List<MHIdBucket> buckets) {
		MHIdBucket bucket = null;
		if (buckets.size() == 0) {
			bucket = new MHIdBucket (id, id + 10000, hashes);
			buckets.add(bucket);
		} else {
			bucket = buckets.get(buckets.size() - 1);
			if (bucket.div.size() == width) {
				bucket.upperBound = bucket.div.last();
				bucket.finalize();
				bucket = new MHIdBucket (id, id + 10000, hashes);
				buckets.add(bucket);
			}
		}
		bucket.addId(id);
	}
	
	
	
	public List<MHIdBucket> genEqIdBuckets (String tableName, String aname, String keyword, 
			String idName, int mode) throws Exception {
		String stat = "SELECT " + idName + " FROM " + tableName + 
			" WHERE " + aname + "= '" + keyword  + "'";
		//String stat = "SELECT hstr FROM histograms WHERE hkey = '" + keyword + "'";
		List<MHIdBucket> buckets = null;
		if (mode == EQ_WIDTH) {
			buckets = doGenEqWidthIdBuckets1 (tableName, stat, idName, width);
		} else if (mode == EQ_DEPTH) {
			buckets = doGenEqDepthIdBuckets (stat, idName, width);
		}
		
		return buckets;
	}
	
	
	
	List<MHIdBucket> genEqIdBuckets (String tableName, String idName, int mode) throws Exception {
		List<MHIdBucket> buckets = null;
		//String stat = "SELECT " + idName + " FROM " + tableName;
		String stat = "SELECT hstr FROM histograms WHERE hkey = '" + tableName + "." + idName + "'";
		if (mode == EQ_WIDTH) {
			buckets = doGenEqWidthIdBuckets (stat, idName, width);
		} else if (mode == EQ_DEPTH) {
			buckets = doGenEqDepthIdBuckets (stat, idName, width);
		}
		return buckets;
	}
	
	// Method 1: Compute histogram in offline component, and read it online.
	public List<MHIdBucket> doGenEqWidthIdBuckets (String stat, 
			String idName, int width) throws Exception {
		List<MHIdBucket> buckets = new LinkedList<MHIdBucket> ();
		ResultSet rs = sql.executeQuery(stat);
		if (rs.next()) {
			String hStr = rs.getString("hstr");
			String tmps[] = hStr.split("\t");
			for (String tmp : tmps) {
				MHIdBucket bucket = MHIdBucket.parse(tmp);
				buckets.add(bucket);
			}
		}
		rs.close();
		return buckets;
	}

	// Compute histogram online.
	public List<MHIdBucket> doGenEqWidthIdBuckets1 (String tableName, String stat, 
			String idName, int width) throws Exception {
		//System.out.println("\t\tFetch Equi-Width buckets " + " ...");
		List<MHIdBucket> buckets = new LinkedList<MHIdBucket> ();
		Map<Integer, MHIdBucket> bucketPool = new TreeMap<Integer, MHIdBucket> ();
		Catelog catelog = Catelog.insCatelog(sql);
		//System.out.println("\t\tRead the catelog...");
		Integer lb = catelog.get(tableName, idName, "MIN");		
		if (lb != -1) {
			//System.out.println("\t\tRead the databases --> " + stat);
			ResultSet rs = sql.executeQuery(stat);
			while (rs.next()) {
				int id = rs.getInt (idName);
				assignEqWidthBucket (id, lb, width, bucketPool);
			}
			rs.close();
			//System.out.println("\t\tLoad the buckets");
			for (Integer bid: bucketPool.keySet()) {
				MHIdBucket bucket = bucketPool.get(bid);
				bucket.finalize();
				buckets.add(bucket);
			}
			//System.out.println ("Fetch Buckets for " + stat + " (" + buckets.size() + ") --> " + (s2 - s1) + " ms");
			//System.out.println("\t\tDone!");
			return buckets;
		} else {
			throw new Exception("Error: Cannot obtain the lower bound of '" + idName + "'.");
		}
	}
	
	
	
	public List<MHIdBucket> doGenEqDepthIdBuckets (String stat, 
			String idName, int width) throws Exception {
		System.out.println("\t\tFetch Equi-Depth buckets " + " ...");
		ResultSet rs = sql.executeQuery(stat);
		List<MHIdBucket> buckets = new LinkedList<MHIdBucket> ();
		while (rs.next()) {
			int id = rs.getInt (idName);
			assignEqDepthBucket (id, width, buckets);
		}
		rs.close();
		System.out.println ("\t\tDone!");
		return buckets;
	}
	
	
	public static void main (String args[]) {
		try {
//			SQLBackend sql = new SQLBackend ();
//			sql.connectMySQL(Config.dbUser, Config.dbPass, "sqlsugg_dblp");
//			SQLBackend msql = new SQLBackend ();
//			msql.connectMySQL(Config.dbUser, Config.dbPass, "sqlsugg_dblp");
//			MHBucketGen gen = new MHBucketGen (sql, "inv_index", 100);
//			gen.materailzeEqBuckets("k2bIndex", 1000, msql);
//			sql.disconnectMySQL(); 
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
