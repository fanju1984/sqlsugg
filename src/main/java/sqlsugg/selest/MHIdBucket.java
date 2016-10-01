package sqlsugg.selest;

import sqlsugg.selest.sigs.*;
import sqlsugg.util.*;

import java.util.*;

public class MHIdBucket implements Comparable<Object>{
	int lowerBound;
	int upperBound;
	
	double freq;
	TreeSet<Integer> div;
	double divNum;
	
	MHBucketSig sigGen;
	
	
	public MHIdBucket (int pL, int pU, HashFamily pHashes) {
		lowerBound = pL;
		upperBound = pU;
		freq = 0;
		div = new TreeSet<Integer>();
		sigGen = new MHBucketSig (pHashes);
	}
	
	public MHIdBucket (int pL, int pU, double pDiv, 
			double pF) {
		lowerBound = pL;
		upperBound = pU;
		freq = pF;
		divNum = pDiv;
		
	}
	
	public void setSignature (int sig[]) {
		sigGen = new MHBucketSig (sig);
	}
	
	public void addId (int id) {
		freq ++;
		div.add(id);
		
		sigGen.updateSignature(id);
	}
	
	public void finalize () {
		divNum = div.size();
		div.clear();
		div = null;
	}
	
	/*
	 * We do NOT consider the case that one bucket contains another one.
	 */
	public int compareTo(Object obj) {
		MHIdBucket ivb = (MHIdBucket) obj;
		if (lowerBound > ivb.lowerBound) {
			return 1;
		} else if (lowerBound < ivb.lowerBound) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public boolean equals (Object obj) {
		if (obj instanceof MHIdBucket) {
			MHIdBucket bucket = (MHIdBucket) obj;
			return lowerBound == bucket.lowerBound && 
				upperBound == bucket.upperBound;
		} else {
			return false;
		}
	}
	
	public int hashCode () {
		return new Integer (lowerBound).hashCode();
	}
	
	public boolean hasOverlap (Object obj) {
		MHIdBucket ivb = (MHIdBucket) obj;
		if (upperBound > ivb.lowerBound && 
				lowerBound < ivb.upperBound) {
			return true;
		} else {
			return false;
		}
	}
	
	public static List<MHIdBucket> matchFuzzyBuckets (MHIdBucket item, List<MHIdBucket> buckets) {
		if (item.compareTo(buckets.get(0)) == -1) {
			return null;
		} 
		if (item.compareTo(buckets.get(buckets.size() - 1)) == 1) {
			return null;
		}
		List<MHIdBucket> retBuckets = new LinkedList<MHIdBucket> ();
		for (MHIdBucket bucket : buckets) {
			if (bucket.hasOverlap(item)) {
				retBuckets.add(bucket);
			}
		}
		return retBuckets;
	}
	
	
	public static MHIdBucket matchExactBucket (MHIdBucket item, List<MHIdBucket> buckets) {
		if (item.compareTo(buckets.get(0)) == -1) {
			return null;
		} 
		if (item.compareTo(buckets.get(buckets.size() - 1)) == 1) {
			return null;
		}
		int start = 0;
		int end = buckets.size() - 1;
		while (start <= end) {
			int middle = (start + end) / 2;
			MHIdBucket mBucket = buckets.get(middle);
			if (item.compareTo(mBucket) == -1) {
				end = middle - 1;
			} else if (item.compareTo(mBucket) == 1) {
				start = middle + 1;
			} else {
				return mBucket;
			}
		}
		return null;
	}
	
	public static String metaDataDefine () {
		String stat = "(word varchar(100), bid int , lb int , ub int , " +
				"freq double , dn double, sig text)";
		return stat;
	}
	
	public static String insertStat (String tableName) {
		String stat = "INSERT INTO " + tableName + 
			"(word, lb, ub, freq, dn, sig) VALUES ";
		return stat;
	}
	
	public String toString () {
		return tupleStr (null);
	}
	
	public String tupleStr (String word) {
		String stat = "";
		if (word != null) {
			stat += "'" + word + "'" + ",";
		}
		stat += lowerBound + ",";
		stat += upperBound + ",";
		stat += freq + ",";
		stat += divNum + ",";
		stat += sigGen.getSigStr() ;
		stat += "";
		return stat;
		
	}
	
	public static MHIdBucket parse (String histStr) {
		String tmp[] = histStr.split(",");
		if (tmp.length < 4) {
			return null;
		}
		int l = Integer.parseInt(tmp[0]);
		int u = Integer.parseInt(tmp[1]);
		double freq = Double.parseDouble(tmp[2]);
		double div = Double.parseDouble(tmp[3]);
		MHIdBucket bucket = new MHIdBucket (l,u,div, freq);
		int sigs[] = new int[tmp.length - 4];
		for (int i = 4; i < tmp.length; i ++) {
			int ele = Integer.parseInt(tmp[i]);
			sigs[i - 4] = ele;
		}
		bucket.sigGen = new MHBucketSig(sigs);
		if (tmp.length != 104) {
			System.out.println ("histStr -- " + tmp.length +  ": " + histStr);
		}
		return bucket;
	}
	
	public static void main (String args[]) throws Exception {
		MHIdBucket bucket = 
			MHIdBucket.parse("20000000,20000999,10000.0,180.0,3853067,3468819,43158646,7705469,8735864,5432013,11611735,2205826,7811722,3058891,6347071,19465872,13107267,26144231,3557158,1772946,1926205,52419368,24832217,15322108,2577875,8181826,5223449,42061826,25452430,850558,6429167,12198516,35307417,9119084,10272269,11486931,4660814,2587637,310344,10126700,6055726,3852045,6857487,3096260,11888992,1558840,15762503,5142911,7543441,2728948,2538527,20595291,911458,15708557,327838,12854154,19244339,3869639,590828,10072987,1167807,7331204,1159469,12516501,8898394,7013429,7962812,7758622,1692162,17626748,4309139,2257676,899170,4297761,10087495,11367655,13441375,1129394,2754756,23631700,8449443,12264067,2496228,35891727,17409447,9233888,19062557,2933941,10980742,17400304,16470112,5811805,40174457,26309036,2257668,38550229,25008079,493753,3699930,36798158,14438659,2400226,2482592,4577282");
		System.out.println();
	}
	
//	public static List<MHIdBucket> matchFuzzyBuckets (MHIdBucket item, List<MHIdBucket> buckets) {
//		List<MHIdBucket> matched = new LinkedList<MHIdBucket> ();
//		for (MHIdBucket)
//	}
	
}
