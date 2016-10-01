package sqlsugg.scoring.schemascoring;
import sqlsugg.util.schemaGraph.*;

import java.sql.*;

import sqlsugg.backends.*;
import java.util.*;

public class AttributeWeighter {
	SQLBackend sql;
	static final String tableName = "attribute_weight";
	String dbName;
	
	Map<String, Double> weightCache = new HashMap<String, Double> ();
	
	public AttributeWeighter (String pDbName, SQLBackend sqlBack) {
		sql = sqlBack;
		dbName = pDbName;
	}
	
	public void loadWeights () throws Exception {
		String stat = "SELECT * FROM " + tableName;
		ResultSet rs = sql.executeQuery(stat);
		while (rs.next()) {
			String relation = rs.getString("relation");
			String attribute = rs.getString("attribute");
			double weight = rs.getDouble("weight");
			String key = relation + "_" + attribute;
			weightCache.put(key, weight);
		}
		rs.close();
	}
	
	public double getWeight (String relation, String attribute) throws Exception{
		String key = relation + "_" + attribute;
		return weightCache.get(key);
	}
	
	public void compute (SchemaGraph sg) throws Exception{
		String stat = "DROP TABLE IF EXISTS " + tableName;
		sql.execute(stat);
		stat = "CREATE TABLE " + tableName + "(relation varchar(100), attribute varchar(100), weight double)";
		sql.execute(stat);
		
		Set<Relation> relations = sg.getRelations();
		for (Relation relation: relations) {
			List<Attribute> attributes = relation.getAttributes();
			List<Double> scores = new LinkedList<Double> ();
			double sum = 0;
			for (Attribute attribute: attributes) {
				double score = computeAttribute(attribute.name, relation.getName());
				scores.add(score);
				sum += score;
			}
			for (int i = 0; i < scores.size(); i ++) {
				stat = "INSERT INTO " + tableName + " VALUES ('" + relation.getName() + "', " 
					+ "'" + attributes.get(i).name + "', " 
					+ scores.get(i) /sum + ")";
				sql.execute(stat);
			}
			
		}
	}
	
	public double computeAttribute (String attribute, String relation) throws Exception {
		double score = 0.0;
		String stat = "SELECT count(*) as num FROM " + relation;
		ResultSet rs = sql.executeQuery(stat);
		int num = 0;
		if (rs.next()) {
			num = rs.getInt("num");
		}
		rs.close();
		stat = "SELECT " + attribute + ", count(*) as num FROM " + 
			relation + " GROUP BY " + attribute;
		rs = sql.executeQuery(stat);
		while (rs.next()) {
			double count = (double) rs.getInt("num");
			double freq = count / num;
			double lscore = freq * Math.log(freq);
			score += lscore;
		}
		return -score;
	}
}
