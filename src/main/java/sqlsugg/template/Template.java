package sqlsugg.template;
import java.util.*;

import sqlsugg.template.tgraph.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * Template is the basic concept to catpure the query structure. Its underlying structure is a graph where
 * nodes represent either entities, attributes, values and etc. and edges represent various relationships between 
 * nodes. 
 * @author Ju Fan
 *
 */

public class Template {
	TNIDAssigner idAssigner;
	public UndirectedGraph<RTNode, JTEdge> graph;
	public int tptid;
	public String desc = "Template";
	
	public Template(int id) {
		graph = new Multigraph<RTNode, JTEdge> (JTEdge.class);
		idAssigner = new TNIDAssigner ();
	}
	
	/**
	 * Get the degree of edges with the type JOIN for a RTNode.
	 * @param node
	 * @return
	 */
	public int joinDegreeof (RTNode node) {
		Set<JTEdge> edges = graph.edgesOf(node);
		return edges.size();
	}
	
	/**
	 * Add and return a new rnode with the type "relation"
	 * @param relation
	 * @return
	 */
	public RTNode addRTNode (Relation relation) {
		RTNode node = new RTNode (idAssigner.getTNID(), relation);
		node.insANodes(idAssigner);
		graph.addVertex(node);
		return node;
	}
	
	/**
	 * Get all rnodes. 
	 * @return
	 */
	public Set<RTNode> getRTNodes () {
		return graph.vertexSet();
	}
	
	/**
	 * Get all JTEdges.
	 * @return
	 */
	public Set<JTEdge> getJTEdges () {
		return graph.edgeSet();
	}
	
	/**
	 * Get the rnodes corresponding to the "relation". 
	 * @param relation
	 * @return
	 */
	public Set<RTNode> getRTNodes (Relation relation) {
		Set<RTNode> rnodes = new HashSet<RTNode> ();
		Set<RTNode> allNodes = graph.vertexSet();
		for (RTNode node: allNodes) {
			if (node.relation.equals(relation)) {
				rnodes.add(node);
			}
		}
		return rnodes;
	}
	
	public Set<RTNode> getRTNodes (String rname) {
		Set<RTNode> rnodes = new HashSet<RTNode> ();
		Set<RTNode> allNodes = graph.vertexSet();
		for (RTNode node: allNodes) {
			if (node.relation.getName().equals(rname)) {
				rnodes.add(node);
			}
		}
		return rnodes;
	}
	
	/**
	 * Find a relation. 
	 * @param node
	 * @return
	 */
	public RTNode find (RTNode node) {
		Set<RTNode> rnodes = this.getRTNodes();
		for (RTNode rnode : rnodes) {
			if (rnode.equals(node)) {
				return rnode;
			}
		}
		return null;
	}
	
	/**
	 * Find the corresponding rnode of an anode. 
	 * @param anode
	 * @return
	 */
	public RTNode findRTNode (ATNode anode) {
		Set<RTNode> rnodes = this.getRTNodes();
		for (RTNode rnode : rnodes) {
			ATNode ranode = rnode.getATNode(anode.attribute.name);
			if (anode.id == ranode.id) {
				return rnode;
			}
		}
		return null;
	}
	
	/**
	 * The number of vertices. 
	 * @return
	 */
	public int size () {
		return graph.vertexSet().size();
	}
	
	/**
	 * Get the degrees of nodes group by the relations. 
	 * @return
	 */
	Map<Relation, Set<Integer>> getRelationDegrees () {
		Map<Relation, Set<Integer>> degrees = 
			new HashMap<Relation, Set<Integer>> ();
		Set<RTNode> rnodes = this.getRTNodes();
		for (RTNode rnode : rnodes) {
			Relation relation = rnode.relation;
			Set<Integer> degree = degrees.get(relation);
			if (degree == null) {
				degree = new HashSet<Integer> ();
			}
			int d = this.joinDegreeof(rnode);
			degree.add(d);
			degrees.put(relation, degree);
		}
		return degrees;
	}

	public Set<Relation> getRelations () {
		Set<Relation> relations = new HashSet<Relation> ();
		Set<RTNode> rnodes = graph.vertexSet();
		for (RTNode rnode : rnodes) {
			Relation relation = rnode.relation;
			relations.add(relation);
		}
		return relations;
	}
	
	/**
	 * Note that it is only a heuristic, which is not accurate. 
	 * @param tpt
	 * @return
	 */
	public boolean isIsomorphism (Template tpt) {
		if (this.size() != tpt.size()) {
			return false;
		}
		Map<Relation, Set<Integer>> rd1 = this.getRelationDegrees();
		Map<Relation, Set<Integer>> rd2 = tpt.getRelationDegrees();
		if (rd1.size() != rd2.size()) {
			return false;
		}
		for (Relation r : rd1.keySet()) {
			Set<Integer> d1 = rd1.get(r);
			Set<Integer> d2 = rd2.get(r);
			if (d2 == null) {
				return false;
			}
			if (d1.size() != d2.size()) {
				return false;
			}
			if (d1.containsAll(d2) && d2.containsAll(d1)) {
			} else {
				return false;
			}
		}
		return true;
	}
	
	public void addTEdge(RTNode n1, RTNode n2, JTEdge e) {
		graph.addEdge(n1, n2, e);
	}
	
	/**
	 * Examine whether there is a relation that does not follow 
	 * Referential integrity. 
	 * @return
	 */
	public boolean isIntegral (SchemaGraph sg) {
		Set<RTNode> rnodes = graph.vertexSet();
		for (RTNode rnode : rnodes) {
			Relation relation = rnode.relation;
			List<Pair<Relation, Attribute>> refs = sg.getReference(relation);
			Set<JTEdge> edges = graph.edgesOf(rnode);
			boolean integral = true;
			for (Pair<Relation, Attribute> ref : refs) {
				boolean found = false;
				for (JTEdge edge : edges) {
					RTNode tnode = graph.getEdgeTarget(edge);
					if (rnode == tnode) {
						tnode = graph.getEdgeSource(edge);
					}
					if (tnode.relation.equals(ref.first)) {
						found = true;
						break;
					}
				}
				integral = integral && found;
				if (!integral) {
					break;
				}
			}
			if (!integral) {
				return false;
			}
		}
		return true;
	}
	
	public List<RTNode> getAdjacentRTNodes(RTNode node) {
		List<RTNode> adjs = new LinkedList<RTNode>();
		Set<JTEdge> edges = graph.edgesOf(node);
		for (JTEdge edge : edges) {
				RTNode n = graph.getEdgeSource(edge);
				if (n == node) {
					n = graph.getEdgeTarget(edge);
				}
				adjs.add(n);
		}
		return adjs;
	}
	
	public Set<RTNode> getLeafNodes() {
		Set<RTNode> leafNodes = new HashSet<RTNode>();
		for (RTNode rnode : graph.vertexSet()) {
			if (this.getAdjacentRTNodes(rnode).size() <= 1) {
				leafNodes.add(rnode);
			}
		}
		return leafNodes;
	}
	
	public Template copy (int newID) {
		Template clone = new Template(newID);
		//step1
		HashMap<RTNode, RTNode> map = 
			new HashMap<RTNode, RTNode> ();
		Set<RTNode> sources = this.graph.vertexSet();
		for (RTNode r:sources) {
			RTNode nr = (RTNode) r.copy(r.id);
			nr.insANodes(idAssigner);
			clone.graph.addVertex(nr);
			map.put(r, nr);
		}
		Set<JTEdge> edges = this.graph.edgeSet();
		for (JTEdge je: edges) {
			JTEdge nje = (JTEdge)je.copy();
			RTNode r1 = this.graph.getEdgeSource(je);
			RTNode r2 = this.graph.getEdgeTarget(je);
			RTNode nr1 = map.get(r1);
			RTNode nr2 = map.get(r2);
			clone.graph.addEdge(nr1, nr2, nje);
		}
		clone.idAssigner = idAssigner.copy();
		return clone;
	}
	
	
	public String toString() {
		Set<RTNode> nodes = graph.vertexSet();
		String ret = new String();
		ret += tptid + ": ";
		ret += "[";
		for (RTNode node : nodes) {
			ret += node.relation.getName() + ",";
		}
		ret += "]";
		return ret;
	}
	
}


