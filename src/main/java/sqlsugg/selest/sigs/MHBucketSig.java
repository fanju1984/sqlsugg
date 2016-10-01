package sqlsugg.selest.sigs;

import sqlsugg.util.*;

public class MHBucketSig {
	public int sig [] = null;
	HashFamily hashes = null;

	public MHBucketSig (HashFamily pHashes) {
		hashes = pHashes;
		int pNh = hashes.numHashes;
		sig = new int [pNh];
		for (int i = 0; i < pNh; i ++) {
			sig[i] = Integer.MAX_VALUE;
		}
	}
	
	public MHBucketSig (int pSig[]) {
		sig = pSig;
	}
	
	public String getSigStr () {
		String str = "";
		for (int i = 0; i < sig.length; i ++) {
			str += sig[i];
			if (i < sig.length - 1) {
				str += ",";
			}
		}
		return str;
	}

	/**
	 * Given a list of IDs in a bucket, 
	 * Return the MIN-Hash Signatures. 
	 * @param ids
	 * @return
	 */
	
	public void updateSignature (int id) {
		for (int i = 0; i < hashes.numHashes; i ++) {
				int rowIndex = id ;
				int hash = hashes.getHash(rowIndex, i);
				if (hash < sig[i]) {
					sig[i] = hash;
				}
		}
	}
	
	public static Double computeJaccard (int sig1[], int sig2[]) {
		//Step 1: Check the correctness
		double similarMinHashes = 0.0;
		int numHashes = sig1.length;
		if (numHashes == 0) {
			return null;
		}
		for(int i = 0; i < numHashes; i++){
			if(sig1[i] != -1 && 
					sig2[i] != -1 && 
					sig1[i] == sig2[i]){
				similarMinHashes++;
			}
		}
		return ( similarMinHashes ) / numHashes;
	}
	
	public static void main (String args[]) {
//		try {
//			List<Integer> l1 = new LinkedList<Integer> ();
//			List<Integer> l2 = new LinkedList<Integer> ();
//			
//			l1.add(10008654);
//			
//			l2.add(10008654);
//			l2.add(10008668);
//			l2.add(10008690);
//			l2.add(10008698);
//			
//			MHBucketSig bsig = new MHBucketSig(10008650, 10008700, 5);
//			
//			int sig1[] = bsig.getSignature(l1);
//			int sig2[] = bsig.getSignature(l2);
//			
//			for (int i = 0; i < sig1.length; i ++) {
//				System.out.println (sig1[i] + " , " + sig2[i]);
//			}
//			
//			System.out.println("FINISH!");
//			
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
