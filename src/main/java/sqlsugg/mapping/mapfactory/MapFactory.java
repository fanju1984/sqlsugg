package sqlsugg.mapping.mapfactory;

import sqlsugg.mapping.*;
import sqlsugg.util.schemaGraph.*;

import java.util.*;

/**
 * This class is to iteratively construct the various types of keyword mappings.
 * In particular, we consider the computation of a relation as a mile-stone.
 * @author Ju Fan
 *
 */
public abstract class MapFactory {
	public MapType mapType;
	public boolean flush; // Indicate whether to materialize the map scores.
	public String flushTable; // Point to the table to be flushed.
	protected SchemaGraph sg;
	
	protected Iterator<Relation> rIterator;
	protected Relation relation;
	
	
	
	public MapFactory (MapType pMapType, SchemaGraph pSg) {
		mapType = pMapType;
		flush = false;
		sg = pSg;
	}
	
	protected KeywordMap makeMap (String pKStr, String pRStr, String pAStr) {
		KeywordMap map = mapType.insMap(-1, pKStr, pRStr, pAStr);
		return map;
	}
	

	/**
	 * Initialize the relation being examined and the corresponding result set.
	 */
	public boolean initConstruction() throws Exception {
		Set<Relation> relations = sg.getRelations();
		rIterator = relations.iterator();
		return true;
	}
	
	public boolean finalizeConstruction() throws Exception{
		return true;
	}
	
	
	
	
	
	public List<KeywordMap> nextMapBatch () throws Exception{
		List<KeywordMap> mapBatch = null;
		if (!needSwitchRelation()) { //If we do NOT need to switch the relation.
			flush = false; 
			flushTable = null;
			mapBatch = generateMapBatch ();
		} else {
			if (needStoreRelation ()) {
				flush = true; 
				flushTable = relation.getName();
			} else {
				flush = false;
				flushTable = null;
			}
			mapBatch = switchRelation (); 
			
		}
		return mapBatch;
	}
	
	protected abstract boolean needSwitchRelation () throws Exception;
	protected abstract boolean needStoreRelation () throws Exception;
	
	protected abstract List<KeywordMap> generateMapBatch () throws Exception;
	protected abstract List<KeywordMap> switchRelation () throws Exception;
	
	
	protected abstract Relation updateRelation ();
}
