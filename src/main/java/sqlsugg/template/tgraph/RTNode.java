package sqlsugg.template.tgraph;

import java.util.*;

import sqlsugg.template.TNIDAssigner;
import sqlsugg.util.schemaGraph.*;

public class RTNode extends TNode{
	public Relation relation; // The corresponding relation
	public int instance = -1; // The instance ID. 
	// In particular, this id should be unique in one template.
	public List<ATNode> anodes = new LinkedList<ATNode> ();
	
	public RTNode (int id, Relation pRelation) {
		super (id);
		relation = pRelation;
	}
	
	public RTNode (int id, Relation pRelation, int in) {
		super(id);
		relation = pRelation;
		instance = in;
	}
	
	public void insANodes (TNIDAssigner idAssigner) {
		List<Attribute> attributes = relation.getAttributes();
		for (Attribute attribute : attributes) {
			ATNode anode = new ATNode (idAssigner.getTNID(), attribute);
			anodes.add(anode);
		}
	}
	
	public ATNode getATNode (String aname) {
		for (ATNode anode : anodes) {
			if (anode.attribute.name.equals(aname)) {
				return anode;
			}
		}
		return null;
	}
	
	public TNode copy (int newID) {
		RTNode node = new RTNode (newID, relation, instance);
		return node;
	}
	
	public boolean equals (Object o) {
		if (o instanceof RTNode) {
			RTNode node = (RTNode) o;
			return (id == node.id) &&
				(relation.equals(node.relation) &&
					instance == node.instance);
		} else {
			return false;
		}
	}
	
	public boolean withSameRelation (RTNode node) {
		return relation.equals(node.relation);
	}
	
	public int hashCode () {
		return (relation.toString()+instance).hashCode();
	}
	
	public String toString () {
		return id + "(" + relation.getName() + ")";
	}
}
