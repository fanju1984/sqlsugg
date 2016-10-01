package sqlsugg.selest;

import sqlsugg.backends.SQLBackend;
import sqlsugg.mapping.MapType;
import sqlsugg.mapping.maps.K2VMap;
import sqlsugg.sqlgen.MapIns;
import sqlsugg.template.tgraph.*;
import sqlsugg.template.*;
import sqlsugg.util.Op;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.*;

import java.util.*;

public class OpTree {
	public class OpNode {
		public int type;
		public RTNode rnode; 
		// Only the leaf node links to an RTNode.
		
		public String linkRelation;
		public String linkIdName;
		
		public OpNode left = null;
		public OpNode right = null;
		
		
		public OpNode (int pT, RTNode pR, String pLi) {
			type = pT;
			rnode = pR;
			linkRelation = pR.relation.getName();
			linkIdName = pLi;
		}
		
		public OpNode (int pT, String pLR, String pLI) {
			type = pT;
			linkRelation = pLR;
			linkIdName = pLI;
		}
		
		public String toString () {
			return type +" "+ linkRelation + "(" + linkIdName + ")";
		}
	}
	
	OpNode root;
	
	
	final String INVTABLE = "inv_index";
	
	SQLBackend sql;
	
	HashFamily hashes;
	int width;
	
	MHBucketGen gen;
	
	
	public OpTree (Template template, RTNode rnode, SchemaGraph sg, 
			MHBucketGen pGen) {
		Set<RTNode> visited = new HashSet<RTNode> ();
		String linkIdName = rnode.relation.getKey();
		root = constructOpTree (template, rnode, visited, sg, linkIdName);
		gen = pGen;
	}
	
	public OpNode constructOpTree (Template template, 
			RTNode rnode, Set<RTNode> visited, SchemaGraph sg, String pLI) {
		if (visited.contains(rnode)) {
			return null;
		}
		visited.add(rnode);
		OpNode n1 = new OpNode (0, rnode, pLI);
		List<RTNode> adjs = template.getAdjacentRTNodes(rnode);
		if (adjs.size() == 0) {
			return n1;
		}
		OpNode root = n1;
		for (RTNode adj: adjs) {
			JoinEdge edge = sg.getJoinEdge(rnode.relation.getName(), adj.relation.getName());
			String rootLinkIdName = edge.primaryAtt;
			String adjLinkIdName = edge.foreignAtt;
			if (adj.relation.getName().equals(edge.primary)) {
				adjLinkIdName = edge.primaryAtt;
				rootLinkIdName = edge.foreignAtt;
			}
			
			OpNode n2 = constructOpTree (template, adj, visited, sg, adjLinkIdName);
			if (n2 != null) {
				OpNode joinNode = new OpNode (1, root.linkRelation, root.linkIdName);
				root.linkIdName = rootLinkIdName;
				joinNode.left = root;
				joinNode.right = n2;
				root = joinNode;
			}
		}
		return root;
	}
	
	public String toString () {
		StringBuffer buffer = new StringBuffer();
		doString (root, buffer, 0);
		return buffer.toString();
	}
	
	static void doString (OpNode root, StringBuffer buffer, int level) {
		if (root == null) {
			return;
		}
		for (int i = 0; i < level; i ++) {
			buffer.append("  ");
		}
		buffer.append(root);
		buffer.append("\n");
		doString (root.left, buffer, level + 1);
		doString (root.right, buffer, level + 1);
	}
	
	public int estimateCard (Collection<MapIns> matching, 
			IdConverter converter) throws Exception {
		List<MHIdBucket> buckets = doEstimateCard (root, matching, converter);
		return MHBucketOps.cardinality(buckets);
	}
	
	public List<MHIdBucket> doEstimateCard (OpNode root, 
			Collection<MapIns> matching, IdConverter converter) throws Exception {
		List<MHIdBucket> buckets = null;
		if (root.type == 0) { // Construct the buckets
			for (MapIns mapIns : matching) {
				if (mapIns.keywordMap.type != MapType.K2V) {
					continue;
				}
				if (mapIns.rnode == root.rnode) {
					List<MHIdBucket> newBuckets = map2MHIdBuckets (mapIns);
					if (buckets == null) {
						buckets = newBuckets;
					} else {
						System.out.println ("\tIntersect with " + mapIns);
						buckets = MHBucketOps.operation(buckets, newBuckets, MHBucketOps.INTERSECT);
					}
				}
			}
			if (buckets == null) {
				buckets = gen.genEqIdBuckets(root.linkRelation, 
						root.linkIdName, MHBucketGen.EQ_WIDTH);
			}
			System.out.println("\t" + root + ": #Card:  " + 
					MHBucketOps.cardinality(buckets) + " , #Buckets: " + buckets.size());
		} else { // Do the join operation, and transform the ids.
			List<MHIdBucket> lBuckets = doEstimateCard (root.left, matching, converter);
			List<MHIdBucket> rBuckets = doEstimateCard (root.right, matching, converter);
			buckets = MHBucketOps.operation(lBuckets, rBuckets, MHBucketOps.JOIN);
			System.out.println("\tJOIN [[[" + root.left.linkRelation + "." + root.left.linkIdName
					+ " , " + root.right.linkRelation + "." + root.right.linkIdName + 
					"]]]: #Card:  " + 
					MHBucketOps.cardinality(buckets) + " , #Buckets: " + buckets.size() + " , " + 
					"#Hitted Buckets: " + MHBucketOps.tmp);
			String sourceRelation = root.left.linkRelation;
			String sourceIdName = root.left.linkIdName;
			String targetRelation = root.linkRelation;
			String targetIdName = root.linkIdName;
			if (!sourceRelation.equals(targetRelation)) {
				sourceRelation = root.right.linkRelation;
				sourceIdName = root.right.linkIdName;
			}
			if (!sourceIdName.equals(targetIdName)) {
				int totalNum = MHBucketOps.cardinality(buckets);
				buckets = converter.propagate(targetRelation, targetIdName, totalNum);
				System.out.println ("\tPropagate: " + sourceRelation + "." + sourceIdName + " --> " + 
						targetRelation + "." + targetIdName);
			}
		}
		return buckets;
	}
	
	
	protected List<MHIdBucket> map2MHIdBuckets (MapIns mapIns) throws Exception {
		if (mapIns.keywordMap.type != MapType.K2V) {
			return null;
		}
		List<MHIdBucket> buckets = null; 
		K2VMap k2vMap = (K2VMap)mapIns.keywordMap;
		if (k2vMap.op == Op.CONTAINS) {
			String idName = "rcdid";
			String value = k2vMap.value;
			String aname = "word";
			String keyword = value + "_" + 
				mapIns.rnode.relation.getName() + "." + 
				mapIns.anode.attribute.name + ".value";
			buckets = gen.genEqIdBuckets(INVTABLE, aname, keyword, idName, MHBucketGen.EQ_WIDTH);
			return buckets;
		} else {
			//TODO: Fix here later. 
			return null;
		}
	}
}
