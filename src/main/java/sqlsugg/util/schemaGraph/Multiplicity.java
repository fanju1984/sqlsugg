package sqlsugg.util.schemaGraph;

public enum Multiplicity {
	M12N (0), 
	MN21 (1),
	MN2N (2);
	
	
	int multi;
	
	private Multiplicity (int m) {
		multi = m;
	}
	
	static Multiplicity parse (String str) {
		if (str.equals("n1")) {
			return Multiplicity.MN21;
		} else if (str.equals("1n")) {
			return Multiplicity.M12N;
		} else if (str.equals("nn")) {
			return Multiplicity.MN2N;
		}
		return null;
	}
}
