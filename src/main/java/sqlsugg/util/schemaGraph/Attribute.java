package sqlsugg.util.schemaGraph;

public class Attribute {
	public String name;
	public DataType type;
	public double weight;
	
	
	public Attribute (String n, String t) {
		name = n;
		type = DataType.parse(t);
	}
	
	public Attribute () {}
	
	public Attribute copy () {
		Attribute a = new Attribute();
		a.name = name;
		a.type = type;
		a.weight = weight;
		return a;
	}
	
	public boolean equals (Object obj) {
		if (obj instanceof Attribute) {
			Attribute att = (Attribute) obj;
			return name.equals(att.name) && 
				type == att.type;			
		} else {
			return false;
		}
	}
	
	public String toString () {
		return name;
	}
}
