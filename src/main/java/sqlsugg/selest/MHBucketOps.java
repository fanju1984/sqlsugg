package sqlsugg.selest;

import java.util.*;
import sqlsugg.selest.sigs.MHBucketSig;

public class MHBucketOps {
	
	public static final int INTERSECT = 0;
	public static final int JOIN = 1;
	public static final int UNION = 2;
	
	private static void mergeSigs (MHIdBucket bucket, int sig1[], int sig2[]) {
		int sig[] = new int[sig1.length];
		for (int i = 0; i < sig.length; i ++) {
			if (sig1[i] != -1 && 
					sig2[i] != -1 && 
					sig1[i] == sig2[i]) {
				sig[i] = sig2[i];
			} else {
				sig[i] = -1;//Math.min(sig1[i], sig2[i]);
			}
		}
		bucket.setSignature(sig);
	}
	
	static int tmp = 0;
	
	private static MHIdBucket genBucket (MHIdBucket bucket1, MHIdBucket bucket2, 
			int op) {
		Double jaccard = MHBucketSig.computeJaccard(bucket1.sigGen.sig
				, bucket2.sigGen.sig);
		boolean set = false;
		double div = 0.0;
		double freq = 0.0;
		
		double density1 = bucket1.freq /bucket1.divNum;
		double density2 = bucket2.freq / bucket2.divNum;
		
		int lb = Math.max(bucket1.lowerBound, bucket2.lowerBound);
		int ub = Math.min(bucket1.upperBound, bucket2.upperBound);
		
		if (jaccard == null) {
			double div1 = bucket1.divNum * (double)(ub - lb) / (double)(bucket1.upperBound - bucket1.lowerBound);
			double div2 = bucket2.divNum * (double) (ub - lb) / (double) (bucket2.upperBound - bucket2.lowerBound);
			div = Math.min(div1, div2);
			if (op == INTERSECT) {
				freq = div;
			} else if (op == JOIN) {
				freq = (density1 * density2) * div;
			}
			set = true;
		} else if (jaccard > 0.0) { // It indicates that there is no min-hash
			div = (bucket1.divNum + bucket2.divNum) * jaccard / (jaccard + 1);
			if (op == INTERSECT) {
				freq = div; 
			} else if (op == JOIN) {
				freq = (bucket1.freq /bucket1.divNum * bucket2.freq / bucket2.divNum) * div;
			}
			set = true;
		}
		if (set) {
			MHIdBucket newBucket = new MHIdBucket (lb, ub, div, freq);
			mergeSigs(newBucket, bucket1.sigGen.sig, bucket2.sigGen.sig);
			return newBucket;
		}
		
		return null;
	}
	
	public static int cardinality (List<MHIdBucket> buckets) {
		double card = 0.0;
		for (MHIdBucket bucket : buckets) {
			card += bucket.freq;
		}
		return (int) card;
	} 
	
	public static List<MHIdBucket> operation (List<MHIdBucket> b1, 
			List<MHIdBucket> b2, int op) {
		List<MHIdBucket> blist1, blist2;
		if (b1.size() < b2.size()) {
			blist1 = b1;
			blist2 = b2;
		} else {
			blist1 = b2;
			blist2 = b1;
		}
		tmp = 0;
		List<MHIdBucket> joined = new LinkedList<MHIdBucket> ();
		for (MHIdBucket bucket : blist1) {
			List<MHIdBucket> sbuckets = MHIdBucket.matchFuzzyBuckets(bucket, blist2);
			if (sbuckets != null) {
				for (MHIdBucket sbucket : sbuckets) {
					tmp ++;
					MHIdBucket newBucket = genBucket (bucket, sbucket, op);
					if (newBucket != null) {
						joined.add(newBucket);
					}
				}
			}
		}
		return joined;
	}
	
	public static void main (String args[]) {
		try {
//			SQLBackend sql = new SQLBackend ();
//			sql.connectMySQL(Config.dbUser, Config.dbPass, "sqlsugg_dblp");
//			MHBucketGen gen1 = new MHBucketGen (sql, "sample_inv", 100);
//			MHBucketGen gen2 = new MHBucketGen (sql, "pa", 100);
//			
//			List<MHIdBucket> bl1 = gen1.genEqIdBuckets("word", "graph_paper.title.value", "rcdid", 1000);
//			System.out.println("# of Buckets of the first predicate: " + bl1.size());
//			
//			List<MHIdBucket> bl2 = gen2.genEqIdBuckets("pid", 1000);
//			System.out.println("# of Buckets of the second predicate: " + bl2.size());
//			
//			
//			List<MHIdBucket> joined = MHBucketOps.eqJoin(bl1, bl2);
//			
//			int card = MHBucketOps.cardinality(joined);
//			
//			System.out.println("Estimate of the joined size: " + card);
//			
//			sql.disconnectMySQL();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
