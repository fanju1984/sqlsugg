package sqlsugg.scoring.schemascoring;
import sqlsugg.util.dataGraph.*;
import sqlsugg.util.dataGraph.DataGraph.Instance;
import sqlsugg.util.dataGraph.DataGraph.DGEdge;
import java.sql.*;

import java.util.*;

import edu.uci.ics.jung.algorithms.scoring.*;
import sqlsugg.backends.*;

public class TableWeighter {
	String dbname;
	SQLBackend sql;
	static final String tablename = "relation_weight";
	Map<String, Double> weightCache = new HashMap<String, Double> ();
	
	public  TableWeighter (String pDbname, SQLBackend pSql) {
		dbname = pDbname;
		sql = pSql;
	}
	
	public void computeWeights (HashMap<String, Integer> relationMap, 
			double damp, DataGraph dg) throws Exception {
		HashMap<String, Double> weights = this.runPageRank(dg, relationMap, damp);
		String stat = "DROP TABLE IF EXISTS " + tablename;
		sql.execute(stat);
		stat = "CREATE TABLE " + tablename + "(relation varchar(100), weight double)";
		sql.execute(stat);
		Set<String> relations = weights.keySet();
		for (String relation: relations) {
			stat = "INSERT INTO " + tablename + " VALUES ('" + relation + "', " + 
				weights.get(relation) + ")";
			sql.execute(stat);
		}
	}
	
	public void loadWeights () throws Exception {
		String stat = "SELECT * FROM " + tablename;
		ResultSet rs = sql.executeQuery(stat);
		while (rs.next()) {
			String relation = rs.getString("relation");
			double weight = rs.getDouble("weight");
			weightCache.put(relation, weight);
		}
		rs.close();
	}
	
	public double getWeight (String relation) throws Exception{
		return weightCache.get(relation);
	}
	
	HashMap<String, Double> runPageRank (DataGraph dg, HashMap<String, Integer> relationMap, 
			double damp) {
		HashMap<Integer, List<Double>> tableWeight =
			new HashMap<Integer, List<Double>> ();
		PageRank<Instance, DGEdge> pr = new PageRank<Instance, DGEdge> (dg.graph1, damp);
		pr.initialize();
		int maxIt = pr.getMaxIterations();
		System.out.println("\t\tmaximal iteration: " + maxIt);
		for (int i = 0; i < maxIt; i ++) {
			pr.step();
			if (i % 10 == 0) {
				System.out.println ("\t\t#iteration: " + i);
			}
		}
		HashMap<Integer, String> invRelationMap = new HashMap<Integer, String> ();
		Set<String> r = relationMap.keySet();
		for (String relation: r) {
			invRelationMap.put(relationMap.get(relation), relation);
		}
		
		
		for (Instance ins: dg.graph1.getVertices()) {
			List<Double> scores = tableWeight.get(ins.relationID);
			if (scores == null) {
				scores = new LinkedList<Double>();
				scores.add(0.0);
				scores.add(0.0);
			}
			double score = scores.get(0);
			double count = scores.get(1);
			score += pr.getVertexScore(ins);
			count += 1.0;
			scores.set(0, score);
			scores.set(1, count);
			
			tableWeight.put(ins.relationID, scores);
		}

		Set<Integer> relationIds = tableWeight.keySet();
		double sum = 0;
		for (int relationID: relationIds) {
			List<Double> scores = tableWeight.get(relationID) ;
			double score = scores.get(0) / scores.get(1);
			sum += score;
		}
		HashMap<String, Double> weight = new HashMap<String, Double> ();
		for (int relationID: relationIds) {
			List<Double> scores = tableWeight.get(relationID) ;
			double score = scores.get(0) / scores.get(1) / sum;
			weight.put(invRelationMap.get(relationID), score);
		}
		return weight;
	}
	
	public static void main (String args[]) {
		try {
//			SchemaGraph sg = new SchemaGraph();
//			sg.buildFromFile("data/dblp/schema.dat");
//			DataGraph dg = new DataGraph (sg, "sqlsugg_dblp");
//			dg.load();
//			TableWeighter weighter = new TableWeighter (0.15, dg, "sqlsugg_dblp");
//			weighter.computeWeights();

		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}










