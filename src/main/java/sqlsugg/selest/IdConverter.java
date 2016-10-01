package sqlsugg.selest;

import java.util.*;

import sqlsugg.util.*;
import sqlsugg.util.schemaGraph.*;

public class IdConverter {
	MHBucketGen gen = null;
	
	public IdConverter (MHBucketGen pGen) {
		gen = pGen;
	}
	
	public List<MHIdBucket> propagate (String targetRelation, 
			String targetIdName, int totalNum) throws Exception {
		List<MHIdBucket> buckets = 
			gen.genEqIdBuckets(targetRelation, targetIdName, MHBucketGen.EQ_WIDTH);
		int card = MHBucketOps.cardinality(buckets);
		double ratio = (double) totalNum / (double) card;
		for (MHIdBucket bucket: buckets) {
			bucket.freq *= ratio;
		}
		return buckets;
	}
	
	
//	public void constructHistograms (SchemaGraph sg) throws Exception {
//		for (Relation r: sg.getRelations()) {
//			List<Pair<Relation, Attribute>> rs = sg.getReference(r);
//			if (rs.size() == 2) {
//				JoinEdge edge1 = sg.getJoinEdge(r.getName(), rs.get(0).first.getName());
//				JoinEdge edge2 = sg.getJoinEdge(r.getName(), rs.get(1).first.getName());
//				BiHistogram bhist1 = new BiHistogram (edge1.foreignAtt, edge2.foreignAtt, r.getName(), 
//						sql);
//				bhist1.eqConstruct(width, hashes, table2Gen);
//				BiHistogram bhist2 = new BiHistogram (edge2.foreignAtt, edge1.foreignAtt, r.getName(), 
//						sql);
//				bhist2.eqConstruct(width, hashes, table2Gen);
//				bihistograms.put (edge1.foreignAtt + "_" + edge2.foreignAtt, bhist1);
//				bihistograms.put (edge2.foreignAtt + "_" + edge1.foreignAtt, bhist2);
//			}
//		}
//	}
}
