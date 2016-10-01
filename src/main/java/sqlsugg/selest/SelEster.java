package sqlsugg.selest;

import java.util.*;

import sqlsugg.backends.*;
import sqlsugg.sqlgen.*;
import sqlsugg.template.*;
import sqlsugg.template.tgraph.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.*;

public class SelEster {
	
	protected final String INVTABLE = "inv_index";
	
	MHBucketGen gen = null;
	IdConverter converter = null;
	
	SchemaGraph sg;
	SQLBackend sql;
	
	
	public SelEster (SQLBackend pSql, int pHashNum, int pWidth, SchemaGraph pSg) throws Exception {
		sql = pSql;
		HashFamily hashes = new HashFamily (pHashNum, 1000000);
		gen = new MHBucketGen (sql, hashes, pWidth);
		converter = new IdConverter (gen);
		sg = pSg;
	}
	
	public int estimate (SQLStruct sqlStruct) throws Exception {
		Template template = sqlStruct.template;
		Collection<MapIns> matching = sqlStruct.matching;
		Set<RTNode> leafNodes = template.getLeafNodes();
		int card = -1;
		for (RTNode leafNode: leafNodes) {
			OpTree opTree = new OpTree (template, leafNode, sg, gen);
			card = opTree.estimateCard(matching, converter);
			break;
		}
		return card;
	}
}
