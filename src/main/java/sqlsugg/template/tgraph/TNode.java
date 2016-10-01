package sqlsugg.template.tgraph;

public abstract class TNode {
	public int id; // The id of the node. 
	// The id should be unique in a graph. 
	
	public TNode (int i) {
		id = i;
	}
	
	public abstract TNode copy (int newID);
}
