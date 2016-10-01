package sqlsugg.template;
import sqlsugg.template.tgraph.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.backends.SQLBackend;
import sqlsugg.config.Config;
import sqlsugg.scoring.*;
import sqlsugg.util.*;


import java.util.*;


public class TemplateGenerator {
	
	int tptID = -1;
	
	void expand(Template template, Set<Relation> relations,
			TemplateIndex tptIndex, int depth, int maxDepth,
			List<Template> buffer, SchemaGraph sg) throws Exception {
		if (template == null) {
			return;
		}
		if (depth > maxDepth) {
			return;
		}
//		System.out.println(template);
		buffer.add(template);		
		if (template.isIntegral(sg)) {
			tptIndex.indexTemplate(template);
		}
		Set<RTNode> rnodes = template.getRTNodes();
		// We examine every rnode in the existing template, and expand it with 
		// every possible relation. 
		for (RTNode rnode : rnodes) {
			for (Relation relation : relations) {
				if (isJoinable(rnode.relation, relation, sg) && 
						isApplicable (template, rnode, relation, sg)) {
					Template newTpt = template.copy(-1);
					// We do not assign the new id unless there is no duplicate.
					RTNode newRNode = newTpt.find(rnode);
					RTNode newNode = newTpt.addRTNode(relation);
					newTpt.addTEdge(newRNode, newNode, new JTEdge());
					boolean isom = false;
					for (Template existTpt : buffer) {
						if (newTpt.isIsomorphism(existTpt)) {
							isom = true;
							break;
						}
					}
					if (!isom) {
						tptID++;
						newTpt.tptid = tptID;
						expand (newTpt, relations, tptIndex, depth + 1, maxDepth, 
								buffer, sg);
					}
				}
			}
		}
	}
	
	boolean isJoinable (Relation r1, Relation r2, SchemaGraph sg) {
		return sg.getJoinEdge(r1.getName(), r2.getName()) != null;
	}
	
	/**
	 * Examine whether the expansion follows the multiplicity of the schema. 
	 * @param template
	 * @param rnode
	 * @param relation
	 * @param sg
	 * @return
	 */
	boolean isApplicable (Template template, RTNode rnode, Relation relation, 
			SchemaGraph sg) {
		Relation nodeRelation = rnode.relation;
		Set<JoinEdge> edges = sg.getJoinEdges();
		for (JoinEdge edge : edges) {
			Multiplicity multi = edge.multiplicity;
			if (multi == Multiplicity.MN2N) {
				continue;
			} else if (multi == Multiplicity.M12N) {
			} else if (multi == Multiplicity.MN21) {
				List<Pair<Relation, Attribute>> refs = sg.getReference(nodeRelation);
				Set<Relation> refRelations = new HashSet<Relation> ();
				for (Pair<Relation, Attribute> ref : refs) {
					refRelations.add(ref.first);
				}
				if (refRelations.size() > 0) {
					List<RTNode> adjs = template.getAdjacentRTNodes(rnode);
					for (RTNode adj : adjs) {
						if (adj.relation.equals(relation) && 
								refRelations.contains(relation)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	public TemplateIndex generate (SchemaGraph sg, Scorer scorer, int maxDepth) throws Exception{
		TemplateIndex tptIndex = new TemplateIndex(scorer);
		Set<Relation> relations = sg.getRelations(); 
		// Use the relations to construct the templates iteratively. 
		List<Template> buffer = new LinkedList<Template> ();
		for (Relation relation : relations) {
			tptID ++;
			Template atom = new Template (tptID);
			atom.addRTNode(relation);
			expand (atom, relations, tptIndex, 1, maxDepth, buffer, sg);
		}
		return tptIndex;
	}
		
	
	public static void main (String args[]) {
		try {
			String dbname = "sqlsugg_dblp";
			SchemaGraph sg = new SchemaGraph();
			sg.buildFromFile("data/dblp/schema.dat");
			
			SQLBackend sql = new SQLBackend ();
			sql.connectMySQL(Config.dbHost, Config.dbUser, Config.dbPass, dbname);
			
			Scorer scorer = new Scorer (sg, sql, dbname);
			TemplateGenerator tptGen = new TemplateGenerator();
			
			TemplateIndex tptIndex = tptGen.generate(sg, scorer, 5);
			System.out.println (tptIndex);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}






