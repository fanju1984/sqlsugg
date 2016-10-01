package sqlsugg.template.tgraph;

import sqlsugg.util.schemaGraph.*;

public class ATNode extends TNode{
	public Attribute attribute;
	
	public ATNode (int id, Attribute pAttribute) {
		super(id);
		attribute = pAttribute;
	}
	
	public ATNode (Attribute pAttribute) {
		super(-1);
		attribute = pAttribute;
	}
	
	public ATNode copy (int newID) {
		ATNode node = new ATNode (id, attribute);
		return node;
	}
	
	
	public boolean equals (Object o) {
		if (o instanceof ATNode) {
			ATNode node = (ATNode)o;
			return  
				id == node.id &&
				attribute.equals(node.attribute);
		} else {
			return false;
		}
	}
	
	public int hashCode () {
		return (id + attribute.toString()).hashCode();
	}
	
	public String toString () {
		return attribute.toString();
	}
}
