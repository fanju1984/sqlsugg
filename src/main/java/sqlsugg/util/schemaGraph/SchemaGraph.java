package sqlsugg.util.schemaGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.*;
import java.io.*;
import java.util.regex.*;
import java.util.*;
import sqlsugg.scoring.Scorer;
import sqlsugg.util.*;

public class SchemaGraph {
	protected UndirectedGraph<Relation, JoinEdge> graph;
	HashMap<String, Relation> relationMap = new HashMap<String, Relation>();
	
	public SchemaGraph () {
		graph = new SimpleGraph<Relation, JoinEdge> (JoinEdge.class);
	}
	
	public void addRelation (Relation r) {
		relationMap.put(r.name, r);
		graph.addVertex(r);
	}
	
	public Relation getRelation (String name) {
		return relationMap.get(name);
	}
	
	
	public Set<JoinEdge> getJoinEdges () {
		return graph.edgeSet();
	}
	
	public JoinEdge getJoinEdge (String rn1, String rn2) {
		Relation r1 = this.getRelation(rn1);
		Relation r2 = this.getRelation(rn2);
		if (r1 != null && r2 != null) {
			return graph.getEdge(r1, r2);
		} else {
			return null;
		}
	}
	
		
	public void addJoinEdge (Relation r1, Relation r2, JoinEdge e) {
		graph.addEdge(r1, r2, e);
	}
	
	public void buildFromFile (String filename) throws Exception {
		BufferedReader r = new BufferedReader (new FileReader(filename));
		String line = r.readLine();
		Pattern rExp = Pattern.compile("R\\:(.+)\\((.+)\\)\t(.+)");
		Pattern jExp = Pattern.compile("J\\:(.+)\t(.+)\t(.+)\t(.+)\t(.+)");
		HashMap<String, Relation> map = new HashMap<String, Relation>();
		while (line != null) {
			Matcher m = rExp.matcher(line);
			if (m.matches()) {
				String name = m.group(1);
				String attributes = m.group(2);
				Relation relation = new Relation (name);
				String aNames[] = attributes.split(",");
				for (String aName: aNames) {
					aName = aName.trim();
					String aParts[] = aName.split(" ");
					boolean isKey = false;
					if (aParts.length == 3) {
						isKey = true;
					}
					Attribute attribute = new Attribute (aParts[0], aParts[1]);
					relation.addAttribute(attribute, isKey);
				}
				this.addRelation(relation);
				map.put(name, relation);
			} else {
				m = jExp.matcher(line);
				if (m.matches()) {
					String rName1 = m.group(1);
					Relation r1 = map.get(rName1);
					String rName2 = m.group(2);
					Relation r2 = map.get(rName2);
					String rKey1 = m.group(3);
					String rKey2 = m.group(4);
					String type = m.group(5);
					Multiplicity multi = 
						Multiplicity.parse(type);
					JoinEdge e = new JoinEdge (r1.name, r2.name, 
							rKey1, rKey2, multi);
					this.addJoinEdge(r1, r2, e);
				}
			}
			line = r.readLine();
		}
		r.close();
	}
	
	public void loadWeights (Scorer scorer) throws Exception {
		Set<Relation> relations = this.getRelations();
		for (Relation relation: relations) {
			relation.weight = scorer.tweighter.getWeight(relation.name);
			List<Attribute> attributes = relation.getAttributes();
			for (Attribute attribute: attributes) {
				attribute.weight = scorer.aweighter.getWeight(relation.name,
						attribute.name);
			}
		}
	}
	
	public Set<Relation> getRelations () {
		return graph.vertexSet();
	}
	
	public List<Pair<Relation,Attribute>> getForeign (Relation relation) {
		List<Pair<Relation,Attribute>> foreign = 
			new LinkedList<Pair<Relation,Attribute>> ();
		Set<JoinEdge> edges = graph.edgesOf(relation);
		for (JoinEdge edge: edges) {
			if (edge.primary.equals(relation.getName())) {
				Relation foreignRelation = this.getRelation(edge.foreign);
				Attribute foreignAttribute = 
					foreignRelation.getAttribute(edge.foreignAtt);
				Pair<Relation,Attribute> pair = 
					new Pair<Relation, Attribute> (foreignRelation, foreignAttribute);
				foreign.add(pair);
			}
		}
		return foreign;
	}
	
	public List<Pair<Relation, Attribute>> getReference (Relation relation ) {
		List<Pair<Relation,Attribute>> reference = 
			new LinkedList<Pair<Relation,Attribute>> ();
		Set<JoinEdge> edges = graph.edgesOf(relation);
		for (JoinEdge edge: edges) {
			if (edge.foreign.equals(relation.getName())) {
				Relation refRelation = this.getRelation(edge.primary);
				Attribute refAttribute = 
					refRelation.getAttribute(edge.primaryAtt);
				Pair<Relation,Attribute> pair = 
					new Pair<Relation, Attribute> (refRelation, refAttribute);
				reference.add(pair);
			}
		}
		return reference;
	}
	
	public String toString () {
		return graph.toString();
	}
	
	public DataType getAttributeType (String relationName, String attributeName) {
		Relation relation = this.getRelation(relationName);
		return relation.getAttribute(attributeName).type;
	}
	
	
	public static void main (String args[]) {
		try {
			SchemaGraph sg = new SchemaGraph ();
			sg.buildFromFile("data/dblp/schema.dat");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
