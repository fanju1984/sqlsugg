package sqlsugg.mapping.maps;


import sqlsugg.mapping.*;
/**
 * This class is to define the keyword-to-relation mapping.
 * @author Administrator
 *
 */

public class K2RMap extends KeywordMap{
	public K2RMap (int pMid, String pKStr, String pRStr) {
		super (pMid, pKStr, pRStr, null);
		type = MapType.K2R;
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
