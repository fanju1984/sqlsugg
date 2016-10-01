package sqlsugg.mapping.maps;

import sqlsugg.mapping.*;
import sqlsugg.util.Op;
/**
 * This class is to define the keyword-to-value mapping.
 * @author Administrator
 *
 */
public class K2VMap extends KeywordMap {
	public Op op = Op.CONTAINS;
	public String value;
	public K2VMap (int pMid, String pKStr, String pRStr, String pAStr) {
		super (pMid, pKStr, pRStr, pAStr);
		type = MapType.K2V;
		value = pKStr;
	}
	
	public String toString () {
		String str = type + "(" + score + ") : " 
			+ rStr + "." + aStr + " "  + op + " " + kStr; 
		return str;
	}
	
	boolean checkConflicts (Op op1, Op op2, double v1, double v2) {
		if (op1 == Op.EQUALS && op2 == Op.EQUALS) {
			return v1 != v2;
		}
		if (op1 == Op.EQUALS && op2 == Op.NGT) {
			return v1 > v2;
		}
		if (op1 == Op.EQUALS && op2 == Op.NLT) {
			return v1 < v2;
		}
		if (op1 == Op.NGT && op2 == Op.NGT) {
			return v1 == v2;
		}
		if (op1 == Op.NGT && op2 == Op.NLT) {
			return v1 < v2;
		}
		if (op1 == Op.NLT && op2 == Op.NLT) {
			return v1 == v2;
		}
		return false;
	}
	
	boolean checkRedundancies (Op op1, Op op2, double v1, double v2) {
		if (op1 == Op.EQUALS) {
			if (op2 == Op.EQUALS) {
				return v1 == v2;
			} else {
				return true;
			}
		} 
		if (op1 == Op.NGT && op2 == Op.NGT) {
			return true;
		}
		if (op1 == Op.NGT && op2 == Op.NLT) {
			return false;
		}
		if (op1 == Op.NLT && op2 == Op.NLT) {
			return true;
		}
		return false;
	}

	public boolean hasConflicts(KeywordMap keywordMap) {
		if (keywordMap instanceof K2VMap) {
			K2VMap k2vMap = (K2VMap) keywordMap;
			if (op == Op.CONTAINS || k2vMap.op == Op.CONTAINS) {
				return false;
			}
			Op op1 = op;
			Op op2 = k2vMap.op;
			double v1 = Double.parseDouble(value);
			double v2 = Double.parseDouble(k2vMap.value);
			boolean conflict1 = checkConflicts (op1, op2, v1, v2);
			boolean conflict2 = checkConflicts (op2, op1, v2, v1);
			return conflict1 || conflict2;
		} else {
			return false;
		}
	}

	public boolean hasRedundancies(KeywordMap keywordMap) {
		if (keywordMap instanceof K2VMap) {
			K2VMap k2vMap = (K2VMap) keywordMap;
			if (op == Op.CONTAINS || k2vMap.op == Op.CONTAINS) {
				return false;
			}
			Op op1 = op;
			Op op2 = k2vMap.op;
			double v1 = Double.parseDouble(value);
			double v2 = Double.parseDouble(k2vMap.value);
			boolean conflict1 = checkRedundancies (op1, op2, v1, v2);
			boolean conflict2 = checkRedundancies (op2, op1, v2, v1);
			return conflict1 || conflict2;
		} else {
			return false;
		}
	}
}
