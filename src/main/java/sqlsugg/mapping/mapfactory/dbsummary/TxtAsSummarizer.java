package sqlsugg.mapping.mapfactory.dbsummary;

import java.util.*;
import java.sql.*;

import sqlsugg.mapping.*;
import sqlsugg.mapping.mapfactory.*;
import sqlsugg.util.Pair;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.tokenizer.Tokenizer;
import sqlsugg.backends.*;
import sqlsugg.config.Config;

public class TxtAsSummarizer extends MapFactory {
	SQLBackend sql;
	SQLBackend sql1;
	
	ResultSet rs = null;
	List<Attribute> tas = null;
	Tokenizer tokenizer;
	int count = 0;
	
	public TxtAsSummarizer (MapType pMapType, SchemaGraph pSg, SQLBackend pSql, 
			SQLBackend pSql1) {
		super (pMapType, pSg);
		tokenizer = new Tokenizer (Config.stopfile);
		sql = pSql;
		sql1 = pSql1;
		sg = pSg;
	}
	

	protected Relation updateRelation () {
		count = 0;
		if (rIterator.hasNext()) {
			Relation relation = rIterator.next();
			tas = relation.getAttributesByType(DataType.TXT); 
			if (tas.size() == 0) {
				relation = updateRelation();
				if (relation == null) {
					return null;
				}
			}
			System.out.println("Compute mappings with the type " + this.mapType + 
					" from the relation, " + relation.getName());
			return relation;
		} else {
			return null;
		}
	}
	
	
	private void updateResultSet (Relation relation) throws Exception {
		String stat = "SELECT * FROM " + relation.getName();
		rs = sql.executeQuery(stat);
	}
	
	
	protected boolean needSwitchRelation () throws Exception{
		return rs == null || !rs.next();
	}
	
	protected boolean needStoreRelation () throws Exception {
		return rs != null;
	}
	
	protected List<KeywordMap> generateMapBatch () throws Exception {
		List<KeywordMap> mapBatch = new LinkedList<KeywordMap> ();
		String primaryKey = relation.getKey();
		// If the current result set has not be all set, examine the next tuple.
		for (Attribute ta: tas) { // Examine every textual attribute.
			KeywordMap map = this.makeMap(ta.name, relation.getName(), null);
			map.setScore(1.0);
			mapBatch.add(map);
			String value = rs.getString(ta.name);
			if (value == null) {
				continue;
			}
			List<String> tokens = tokenizer.tokenize(value);
			for (String token: tokens) {
				
				String primaryValue = rs.getString(primaryKey);
				//int pop = computeTuplePopularity(relation, primaryValue);
				int pop = 1;
				map = makeMap(token, relation.getName(), ta.name);
				map.setScore(pop);
				mapBatch.add(map);
			}
		}
		count ++;
		if (count % 10000 == 0) {
			System.out.println("Tuple Progress: " + count);
		}
		return mapBatch;
	}
	
	protected List<KeywordMap> switchRelation () throws Exception {
		if (rs != null) {
			rs.close(); // close the result set of the previous relation
		}
		List<KeywordMap> mapBatch = new LinkedList<KeywordMap> ();
		relation = this.updateRelation();
		if (relation == null) {
			return null;
		}
		this.updateResultSet(relation);
		if (rs == null) {
			return null;
		}
		KeywordMap map = 
			makeMap (relation.getName(), relation.getName(), null);
		map.setScore(1.0);
		mapBatch.add(map);
		return mapBatch;
	}

	int computeTuplePopularity(Relation relation, String primaryValue)
			throws Exception {
		SQLBackend lsql = new SQLBackend ();
		lsql.connectMySQL(Config.dbHost, Config.dbUser, Config.dbPass, "sqlsugg_dblp");
		int pop = 0;
		List<Pair<Relation, Attribute>> foreigns = sg.getForeign(relation);
		for (Pair<Relation, Attribute> foreign : foreigns) {
			String fRelation = foreign.first.getName();
			String fAtt = foreign.second.name;
			String stat = "SELECT count(*) as cnt FROM " + fRelation
					+ " WHERE " + fAtt + " = " + primaryValue;
			ResultSet rs = lsql.executeQuery(stat);
			int partial = 0;
			if (rs.next()) {
				partial = rs.getInt("cnt");
			}
			rs.close();
			pop += partial;
		}
		lsql.disconnectMySQL();
		return pop;
	}


	
}
