package sqlsugg.util.schemaGraph;

public enum DataType {
	TXT (1), 
	CAT (2), 
	NUM (3);
	
	int type;
	
	private DataType (int t) {
		type = t;
	}
	
	public static DataType parse (String str) {
		if (str.equals("string")) {
			return DataType.TXT;
		} else if (str.equals("int")) {
			return DataType.NUM;
		} else if (str.equals("cat")) {
			return DataType.CAT;
		}
		return null;
	}
}
