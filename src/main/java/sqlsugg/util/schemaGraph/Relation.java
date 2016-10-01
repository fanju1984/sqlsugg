package sqlsugg.util.schemaGraph;
import java.util.*;

public class Relation {
	String name;
	List<Attribute> attributes = new LinkedList<Attribute>();
	List<Attribute> keys = new LinkedList<Attribute>();
	public double weight;
	
	public Relation (String n) {
		name = n;
	}
	
	public boolean isKey (Attribute a) {
		return keys.contains(a);
	}
	
	public boolean isPrimaryKey (Attribute a) {
		if (keys.size() == 0) {
			return false;
		}
		return a == keys.get(0);
	}
	
	public void addAttribute (Attribute a, boolean isKey) {
		attributes.add(a);
		if (isKey) {
			keys.add(a);
		}
	}
	
	public Attribute getAttribute (String attributeName) {
		for (Attribute attribute: attributes) {
			if (attribute.name.equals(attributeName)) {
				return attribute;
			}
		}
		return null;
	}
	
	
	public String getKey () {
		return keys.get(0).name;
	}
	
	
	public Relation copy () {
		Relation r = new Relation(this.name);
		for (Attribute a: attributes) {
			boolean isKey = false;
			if (keys.contains(a)) {
				isKey = true;
			}
			r.addAttribute(a, isKey);
		}
		return r;
	}
	
	public String getName () {
		return name;
	}
	
	public List<Attribute> getAttributes () {
		return attributes;
	}
	
	public List<Attribute> getAttributesByType (DataType dataType) {
		List<Attribute> tas = new LinkedList<Attribute> ();
		for (Attribute a: attributes) {
			if (this.isKey(a)) {
				continue;
			}
			if (a.type == dataType) {
				tas.add(a);
			}
		}
		return tas;
	}
	
	public boolean equals (Object o) {
		if (o instanceof Relation) {
			Relation r = (Relation)o;
			
			return this.name.equals(r.name);
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return name.hashCode();
	}
	
	public String toString () {
		String ret = new String();
		ret = name;
		ret += "(";
		for (int i = 0; i < attributes.size(); i ++) {
			ret += attributes.get(i);
			if (i < attributes.size() - 1) {
				ret += ",";
			}
		}
		ret += ")";
		return ret;
	}
}
