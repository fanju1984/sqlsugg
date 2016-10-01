package sqlsugg.mapping;

import java.util.*;
import java.util.regex.*;
import java.sql.*;

import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.*;
import sqlsugg.backends.*;
import sqlsugg.config.Config;
import sqlsugg.mapping.maps.*;

public class NumK2VMapSearcher extends MapSearcher {

	class NumAsSummary {
		protected Relation relation;
		protected Attribute attribute;
		protected Histogram histogram;
		
	}
	
	List<NumAsSummary> naHistograms;
	SchemaGraph sg;
	SQLBackend sql;
	
	public NumK2VMapSearcher (SchemaGraph pSg, SQLBackend pSql) {
		sg = pSg;
		naHistograms = new LinkedList<NumAsSummary> ();
		sql = pSql;
	}
	
	/**
	 * Get some statistics of the underlying attribute. 
	 * @param relation
	 * @param attribute
	 * @param function
	 * @return
	 * @throws Exception
	 */
	private Double getStatistics (Relation relation, 
			Attribute attribute, String function) throws Exception {
		String stat = "SELECT " + function + "(" + 
			attribute.name + ") as st FROM " + relation.getName();
		ResultSet rs = sql.executeQuery(stat);
		Double st = null;
		if (rs.next()) {
			st = rs.getDouble("st");
			
		} 
		rs.close();
		return st;
	}
	
	public void constructHistograms (int num) throws Exception {
		Set<Relation> relations = sg.getRelations();
		for (Relation relation : relations) {
			List<Attribute> numAttributes = 
				relation.getAttributesByType(DataType.NUM);
			for (Attribute numAttribute : numAttributes) {
				double ub = this.getStatistics(relation, numAttribute, "MAX");
				double lb = this.getStatistics(relation, numAttribute, "MIN");
				Histogram hist = new Histogram (lb, ub, num, false);
				String stat = "SELECT " + numAttribute.name + " FROM " + relation.getName();
				ResultSet rs = sql.executeQuery(stat);
				while (rs.next()) {
					double value = rs.getDouble(numAttribute.name);
					hist.addValue(value);
				}
				rs.close();
				NumAsSummary naSummary = new NumAsSummary ();
				naSummary.relation = relation;
				naSummary.attribute = numAttribute;
				naSummary.histogram = hist;
				naSummary.histogram.normalize();
				this.naHistograms.add(naSummary);
			}
		}
	}
	
	double similarity (double value, Histogram histogram, Op op) {
		double sim = 0.0;
		sim = histogram.getCumProb(value, op);
		return sim;
	}
	
	public List<KeywordMap> searchMaps(String keyword, MapType mapType)
			throws Exception {
		List<KeywordMap> keywordMaps = new LinkedList<KeywordMap> ();
		if (mapType != MapType.K2V && mapType != MapType.K2R) {
			return keywordMaps;
		}
		Pattern regex = Pattern.compile("\\d+\\.*\\d*");
		Matcher m = regex.matcher(keyword);
		if (!m.matches()) {
			return keywordMaps;
		}
		double value = Double.parseDouble(keyword);
		Op ops [] = {Op.EQUALS, Op.NGT, Op.NLT};
		for (NumAsSummary naSummary : this.naHistograms) {
			for (Op op : ops) {
				double sim = similarity (value, naSummary.histogram, op);
				if (sim > 0) {
					KeywordMap keywordMap = mapType.insMap(-1, keyword, 
							naSummary.relation.getName(), naSummary.attribute.name);
					if (mapType == MapType.K2V) {
						K2VMap k2vMap = (K2VMap) keywordMap;
						k2vMap.setScore(sim);
						k2vMap.op = op;
					}
					keywordMaps.add(keywordMap);
				}
			}
		}
		return keywordMaps;
	}
	
	public static void main (String args[]) {
		try {
			SchemaGraph sg = new SchemaGraph();
			sg.buildFromFile("data/dblp/schema.dat");
			String dbname = "sqlsugg_dblp";
			SQLBackend sql = new SQLBackend ();
			sql.connectMySQL(Config.dbUser, Config.dbPass, dbname);
			NumK2VMapSearcher nsearcher = new NumK2VMapSearcher (sg, sql);
			nsearcher.constructHistograms(10);
			List<KeywordMap> keywordMaps = nsearcher.searchMaps("2000", MapType.K2V);
			System.out.println(keywordMaps);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
