package sqlsugg.template;

import java.util.*;

import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.basicstruct.*;
import sqlsugg.util.basicalgo.*;
import sqlsugg.scoring.*;

public class TemplateIndex {
	List<SortedList<String, Template>> r2tLists;
	RanAccIndex<Template, String> t2rIndex;
	Scorer scorer;
	
	
	public TemplateIndex (Scorer pScorer) {
		r2tLists = new LinkedList<SortedList<String, Template>> ();
		t2rIndex = new RanAccIndex<Template, String> ("t2r Index");
		scorer = pScorer;
	}
	
	public SortedList<String, Template> getR2tList (String rname) {
		for (SortedList<String, Template> r2tList : r2tLists) {
			if (r2tList.getS().equals(rname)) {
				return r2tList;
			}
		}
		return null;
	}
	
	public RanAccIndex<Template, String> getT2rIndex () {
		return t2rIndex;
	}
	
	public void indexTemplate (Template template) throws Exception {
		Set<Relation> tptRelations = template.getRelations();
		for (Relation relation : tptRelations) {
			double score = 
				scorer.getRelationWeight(template, relation.getName());
			this.addRelationTemplate(relation.getName(), template, score);
			
		}
	}
	
	private void addRelationTemplate (String rname, 
			Template template, double score) {
		boolean newadded = false;
		SortedList<String, Template> r2tList = this.getR2tList(rname);
		if (r2tList == null) {
			r2tList = new SortedList<String, Template> (rname);
			newadded = true;
		}
		r2tList.add(new ScoredItem<Template> (template, score));
		if (newadded) {
			r2tLists.add(r2tList);
		}
		t2rIndex.put(template, new ScoredItem<String> (rname, score));
	}
	
	public String toString () {
		String str = "";
		System.out.println ("Size of TptIndex: " + r2tLists.size());
		for (SortedList<String, Template> r2tList : r2tLists) {
			str += r2tList.toString();
			str += "\n";
		}
		return str;
	}
	
//	DefaultDirectedGraph<Template, TIndEdge> graph =
//		new DefaultDirectedGraph<Template, TIndEdge>(TIndEdge.class);
//	HashMap<String, List<Template>> atoms = 
//		new HashMap<String, List<Template>>();
//	
//	public void addTemplate (Template t) {
//		graph.addVertex(t);
//	}
//	
//	public void setAtoms (String relationName, Template t) {
//		List<Template> templates = atoms.get(relationName);
//		if (templates == null) {
//			templates = new LinkedList<Template> ();
//		}
//		templates.add(t);
//		atoms.put(relationName, templates);
//	}
//	
//	public int inDegreeOf (Template t) {
//		return graph.inDegreeOf(t);
//	}
//	
//	public int outDegreeOf (Template t) {
//		return graph.outDegreeOf(t);
//	}
//	
////	public void translateTemplateDesc () {
////		Set<Template> templates = graph.vertexSet();
////		for (Template template: templates) {
////			template.translateDesc();
////		}
////	}
//	
//	public List<Template> outNeighbors (Template t) {
//		List<Template> neighbors = new LinkedList<Template> ();
//		Set<TIndEdge> edges = graph.outgoingEdgesOf(t);
//		for (TIndEdge edge: edges) {
//			neighbors.add(graph.getEdgeTarget(edge));
//		}
//		return neighbors;
//	}
//	
//	public List<Template> getAtoms (String relationName) {
//		return atoms.get(relationName);
//	}
//	
//	public int atomNum (String relationName) {
//		if (getAtoms(relationName) != null) {
//			return getAtoms (relationName).size();
//		} else {
//			return 0;
//		}
//	}
//	
//	public void addEdge (Template t1, Template t2, TIndEdge e) {
//		graph.addEdge(t1, t2, e);
//	}
//	
//	public void refine (SchemaGraph sg) {
//		Set<Template> templates = graph.vertexSet();
//		List<Template> removal = new LinkedList<Template> ();
//		for (Template template: templates) {
//			if (!template.saturates(sg)) {
//				removal.add(template);
//			}
//		}
//		for (Template template: removal) {
//			Set<TIndEdge> outEdges = graph.outgoingEdgesOf(template);
//			List<Template> outTpts = new LinkedList<Template> ();
//			for (TIndEdge outEdge: outEdges) {
//				Template outTpt = graph.getEdgeTarget(outEdge);
//				outTpts.add(outTpt);
//			}
//			
//			Set<TIndEdge> inEdges = graph.incomingEdgesOf(template);
//			List<Template> inTpts = new LinkedList<Template> ();
//			for (TIndEdge inEdge: inEdges) {
//				Template inTpt = graph.getEdgeSource(inEdge);
//				inTpts.add(inTpt);
//			}
//			for (Template inTpt: inTpts) {
//				for (Template outTpt: outTpts) {
//					graph.addEdge(inTpt, outTpt, new TIndEdge());
//				}
//			}
//			graph.removeVertex(template);
//		}
//	}
//	
//	public String toString () {
//		StringBuffer buffer = new StringBuffer();
//		Set<Template> templates = graph.vertexSet();
//		buffer.append("Templates:\n");
//		for (Template template: templates) {
//			buffer.append (template.toString());
//			buffer.append("\n");
//		}
//		buffer.append("Edges: \n");
//		Set<TIndEdge> edges = graph.edgeSet();
//		for (TIndEdge edge: edges) {
//			buffer.append(edge + ": " + graph.getEdgeSource(edge) + "\t" + 
//					graph.getEdgeTarget(edge));
//			buffer.append("\n");
//		}
//		return buffer.toString();
//	}
	
}
