package sqlsugg.mapping.maps;

import sqlsugg.mapping.*;
import sqlsugg.util.*;

/**
 * This class is to define the keyword-to-function mapping.
 * @author Ju Fan
 *
 */

public class K2FMap extends KeywordMap {
	/**
	 * A embedded class, which define various types of functions
	 * @author Ju Fan
	 *
	 */
	
	public FuncType funcType;
	
	public K2FMap (int pMid, String pKStr, String pRStr, String pAStr) {
		super (pMid, pKStr, pRStr, pAStr);
		type = MapType.K2F;
	}
	
	public void setFuncType (FuncType ft) {
		funcType = ft;
	}
	
	public String toString () {
		String str = type + "(" + score + ") : " 
		+ kStr + " --> " 
		+ funcType.toString() + "("+ rStr + "." + aStr + ")";
		return str;
	}

	@Override
	public boolean hasConflicts(KeywordMap keywordMap) {
		return false;
	}

	@Override
	public boolean hasRedundancies(KeywordMap keywordMap) {
		return false;
	}
}
