package sqlsugg.mapping.mapfactory.dbsummary;

import java.util.*;
import java.sql.*;


import sqlsugg.mapping.KeywordMap;
import sqlsugg.mapping.*;
import sqlsugg.mapping.maps.*;
import sqlsugg.util.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.backends.*;

/**
 * This class is to construct the mapping from a keyword to a specific type of function
 * The basic idea is firstly read all keywords related to a function type, 
 * and then summarize the relationship between the keywords and attributes.
 * @author Administrator
 *
 */

public class FuncSummarizer extends SchemaSummarizer {

	
	Map<FuncType, List<Pair<String, Double>>> funcKeywords;
	SQLBackend sql;
	
	final String DBTABLE = "func_thesaurus";
	
	public FuncSummarizer(MapType mapType, 
			SchemaGraph sg, SQLBackend pSql) throws Exception {
		super(mapType, sg);
		funcKeywords = 
			new HashMap<FuncType, List<Pair<String, Double>>> ();
		sql = pSql;
		// Read the mapping from keywords to function types from the databases.
		System.out.println("Loading function keywords...");
		String stat = "SELECT * FROM " + DBTABLE;
		ResultSet rs = sql.executeQuery(stat);
		while (rs.next()) {
			String keyword = rs.getString("keyword");
			String function = rs.getString("function");
			double sim = rs.getDouble("sim");
			FuncType funcType = FuncType.parse(function);
			List<Pair<String, Double>> flist = funcKeywords.get(funcType);
			if (flist == null) {
				flist = new LinkedList<Pair<String, Double>> ();
			}
			Pair<String, Double> p = new Pair<String, Double> (keyword, sim);
			flist.add(p);
			funcKeywords.put(funcType, flist);
		}
		rs.close();
	}

	protected List<KeywordMap> generateMapBatch() throws Exception {
		List<KeywordMap> mapBatch = new LinkedList<KeywordMap> ();
		for (FuncType type: funcKeywords.keySet()) { // Examine every attribute.
			// Case 1: Compute the scores of numerical attributes 
			// for the types, MIN, MAX, AVG, SUM. 
			// Case 2: Compute the scores of all attributes for the type, COUNT.
			double score = 1.0;
			List<Attribute> as;
			if (type != FuncType.COUNT) {
				as = relation.getAttributesByType(DataType.NUM);
			} else {
				as = relation.getAttributes();
			}
			for (Attribute a: as) {
				if (type != FuncType.SUM && type != FuncType.COUNT) {
					score = getNVariation (relation.getName(), a.name);
				}
				this.generateTypeMaps(mapBatch, type, 
						funcKeywords.get(type), relation.getName(), a.name, score);
			}
		}
		needSwitch = true;
		return mapBatch;
	}

	protected List<KeywordMap> switchRelation() throws Exception {
		List<KeywordMap> mapBatch = new LinkedList<KeywordMap> ();
		relation = this.updateRelation();
		if (relation == null) {
			return null;
		}
		needSwitch = false;
		firstRun = false;
		return mapBatch;
	}
	
	private void generateTypeMaps (List<KeywordMap> maps, FuncType funcType, 
			List<Pair<String, Double>> keywords, String rname, String aname, double score) {
		for (Pair<String, Double> p: keywords) {
			String keyword = p.first;
			Double sim = p.second;
			KeywordMap map = makeMap (keyword, rname, aname);
			((K2FMap)map).setFuncType(funcType);
			map.setScore(score * sim);
			maps.add(map);
		}
	}
	
	
	/**
	 * Compute the normalized variation for a numerical attribute.
	 * @param relationName
	 * @param attributeName
	 * @return
	 * @throws Exception
	 */
	private double getNVariation (String relationName, String attributeName) throws Exception {
		double avg = 0;
		double variation = 0;
		String stat = "SELECT AVG(" + attributeName + ") as average" + " FROM " + relationName;
		ResultSet rs = sql.executeQuery(stat);
		if (rs.next()) {
			avg = rs.getDouble("average");
			rs.close();
		} else {
			throw new Exception ("No Avg!");
		}
		stat = "SELECT " + attributeName + " FROM " + relationName;
		rs = sql.executeQuery(stat);
		int count = 0;
		while (rs.next()) {
			double value = rs.getDouble(attributeName);
			variation += (value - avg) * (value - avg);
			count ++;
			if (count % 10000 == 0) {
				System.out.println("Progress:  " + count);
			}
		}
		rs.close();
		variation /= count;
		variation = Math.sqrt(variation);
		variation = 1 / variation;
		variation = Math.pow(Math.E, -variation);//normalize it!

		return variation;
	}

}
