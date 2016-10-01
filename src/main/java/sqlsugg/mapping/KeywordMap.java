package sqlsugg.mapping;

import java.util.*;

/**
 * This class is to define a keyword to a abstract element, 
 * e.g., value, attribute, etc. 
 * @author Ju Fan
 *
 */
public abstract class KeywordMap{
	protected int mid; // The map ID
	protected String kStr; // The keyword ID
	protected String rStr; // The relation ID
	protected String aStr; // The attribute ID
	public MapType type;
	protected double score; // The score of the map
	
	public List<String> coveredKeywords;
	
	public KeywordMap (int pMid, String pKStr, String pRStr, String pAStr) {
		//super(null, 0);
		mid = pMid;
		kStr = pKStr;
		rStr = pRStr;
		aStr = pAStr;
		coveredKeywords = new LinkedList<String> ();
		coveredKeywords.add(kStr);
	}
	
	/**
	 * Return the score of the map
	 * @return score
	 */
	public double score () {
		return score;
	}
	
	/**
	 * Set the score of the map
	 * @param pScore: the score of the map
	 */
	public void setScore (double pScore) {
		score = pScore;
	}
	
	public String getRStr () {
		return rStr;
	}
	
	public String getAStr () {
		return aStr;
	}
	
	public String getKStr () {
		return kStr;
	}
	
	public String getSearchKey () {
		String str = null;
		if (type == MapType.K2R) {
			str = this.kStr;
			if (str.contains("_")) {
				str = str.replaceAll("_", "");
				System.out.println("NOP: " + this.kStr + "," + str);
			}
		} else {
			if (this.aStr != null) {
				str = kStr + "_" + aStr;
			} else {
				str = kStr + "_NULL";
			}
		}
		return str;
	}
	
	public String toString () {
		String str = type + "(" + score + ") : " 
			+ kStr + " --> " + rStr + "." + aStr;
		return str;
	}
	
	public boolean equals (Object obj) {
		if (obj instanceof KeywordMap) {
			KeywordMap kmap = (KeywordMap) obj;
			return type == kmap.type && mid == kmap.mid;
		} else {
			return false;
		}
	}
	
	public int compareTo(Object arg0) {
		KeywordMap sItem = (KeywordMap) arg0;
		if (sItem.score > score) {
			return 1;
		} else if (sItem.score < score){
			return -1;
		} else {
			//return ((Comparable)obj).compareTo(sItem.obj);
			return -1;
		}
	}
	
	
	public void addCoveredKeywords (String keyword) {
		if (keyword != null) {
			coveredKeywords.add(keyword);
		}
	}
	
	public abstract boolean hasConflicts (KeywordMap keywordMap);
	public abstract boolean hasRedundancies (KeywordMap keywordMap);
}
