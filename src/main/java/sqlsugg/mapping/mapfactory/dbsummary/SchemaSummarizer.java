package sqlsugg.mapping.mapfactory.dbsummary;

import java.util.*;


import sqlsugg.mapping.mapfactory.*;
import sqlsugg.mapping.*;
import sqlsugg.util.schemaGraph.*;


public class SchemaSummarizer extends MapFactory{
	boolean needSwitch;
	boolean firstRun;
	
	
	public SchemaSummarizer(MapType mapType, SchemaGraph pSg) {
		super(mapType, pSg);
		needSwitch = true;
		firstRun = true;
	}
	

	protected Relation updateRelation() {
		if (rIterator.hasNext()) {
			Relation relation = rIterator.next();
			System.out.println("Compute mappings with the type " + this.mapType + 
					" from the relation, " + relation.getName());
			return relation;
		} else {
			return null;
		}
	}


	protected List<KeywordMap> generateMapBatch() throws Exception {
		List<KeywordMap> mapBatch = new LinkedList<KeywordMap> ();
		List<Attribute> attributes = relation.getAttributes();
		for (Attribute attribute: attributes) {
			KeywordMap map1 = makeMap (attribute.name, relation.getName(), 
					attribute.name);
			map1.setScore(1.0);
			mapBatch.add(map1);
		}
		needSwitch = true;
		return mapBatch;
	}

	@Override
	protected boolean needStoreRelation() throws Exception {
		return !firstRun;
	}

	protected boolean needSwitchRelation() throws Exception {
		return needSwitch;
	}

	@Override
	protected List<KeywordMap> switchRelation() throws Exception {
		List<KeywordMap> mapBatch = new LinkedList<KeywordMap> ();
		relation = this.updateRelation();
		if (relation == null) {
			return null;
		}
		String relationName = relation.getName();
		KeywordMap map = makeMap (relationName, relationName, relation.getKey());
		map.setScore(1.0);
		mapBatch.add(map);
		needSwitch = false;
		firstRun = false;
		return mapBatch;
	}

}
