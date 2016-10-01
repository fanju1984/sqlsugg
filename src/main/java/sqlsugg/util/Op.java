package sqlsugg.util;

public enum Op {
	CONTAINS (0),
	EQUALS (1),
	NGT (2),
	NLT (3);
	
	
	int opType;
	
	private Op (int pOp) {
		opType = pOp;
	}
	
	public String toString () {
		switch (opType) {
		case 0:
			return "CONTAINS";
		case 1: 
			return "=";
		case 2: 
			return "<=";
		case 3:
			return ">=";
		}
		return null;
	}
	
	public String getQuota () {
		if (opType == 0) {
			return "\"";
		} else {
			return "";
		}
	}
}
