//package sqlsugg.selest;
//
//import java.util.*;
//
//import sqlsugg.backends.*;
//import sqlsugg.util.*;
//
//
//public class BiHistogram {
//	String tableName;
//	String aname1; 
//	String aname2; 
//	
//	Map<MHIdBucket, List<MHIdBucket>> twoDMap;
//	
//	SQLBackend sql;
//	MHBucketGen gen;
//	
//	public BiHistogram (String pAname1, String pAname2, String pTableName, MHBucketGen pGen) {
//		tableName = pTableName;
//		aname1 = pAname1;
//		aname2 = pAname2;
//		twoDMap = new HashMap<MHIdBucket, List<MHIdBucket>> ();
//	}
//	
//	public BiHistogram (String pAname1, String pAname2, String pTableName, SQLBackend pSql) {
//		tableName = pTableName;
//		aname1 = pAname1;
//		aname2 = pAname2;
//		twoDMap = new HashMap<MHIdBucket, List<MHIdBucket>> ();
//		sql = pSql;
//	}
//	
//	
//	public void eqConstruct (int width, HashFamily hashes, Map<String, MHBucketGen> table2Gen) throws Exception {
//		MHBucketGen gen1 = table2Gen.get(tableName);
//		if (gen1 == null) {
//			gen1 = new MHBucketGen (sql, tableName, hashes);
//			table2Gen.put(tableName, gen1);
//		}
//		List<MHIdBucket> bl1 = gen1.genEqIdBuckets(aname1, width, MHBucketGen.EQ_WIDTH);
//		MHBucketGen gen2 = new MHBucketGen (sql, tableName, hashes);
//		for (MHIdBucket bucket : bl1) {
//			String stat = "SELECT " + aname2 + " FROM " + tableName + 
//				" WHERE " + aname1 + ">=" + bucket.lowerBound + " AND " + 
//				aname1 + "<=" + bucket.upperBound;
//			List<MHIdBucket> bl2 = gen2.doGenEqWidthIdBuckets(stat, aname2, width);
//			twoDMap.put(bucket, bl2);
//		}
//	}
//	
////	public List<MHIdBucket> propagate (int card) {
////		
////	}
//	
//	public List<MHIdBucket> transform (List<MHIdBucket> buckets) {
//		List<MHIdBucket> ret = new LinkedList<MHIdBucket> ();
//		for (MHIdBucket bucket : buckets) {
//			List<MHIdBucket> found = twoDMap.get(bucket);
//			if (found != null) {
//				ret.addAll(found);
//			}
//		}
//		return ret;
//	}
//	
//	public static void main (String args[]) {
//		try {
////			SQLBackend sql = new SQLBackend ();
////			sql.connectMySQL(Config.dbUser, Config.dbPass, "sqlsugg_dblp");
////			BiHistogram hist = new BiHistogram ("pa", "pid", "aid", sql);
////			hist.eqConstruct(1000);
//			
//			
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}
