package sqlsugg.mapping.maps;

import sqlsugg.mapping.*;

/**
 * This class is to define the keyword-to-metadata mapping.
 * @author Ju Fan
 *
 */

public class K2MMap extends KeywordMap{

	public K2MMap (int pMid, String pKStr, String pRStr, String pAStr) {
		super (pMid, pKStr, pRStr, pAStr);
		type = MapType.K2M;
	}

	@Override
	public boolean hasConflicts(KeywordMap keywordMap) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasRedundancies(KeywordMap keywordMap) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
