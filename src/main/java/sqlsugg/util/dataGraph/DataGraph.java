package sqlsugg.util.dataGraph;

import sqlsugg.util.schemaGraph.*;
import edu.uci.ics.jung.graph.*;

import java.sql.*;
import sqlsugg.backends.*;

import java.util.*;


public class DataGraph {
	public class Instance {
		public int relationID;
		int id;
		public double weight;
		
		public Instance(int rID, int id, double w)  {
			this.relationID = rID;
			this.id = id;
			weight = w;
		}
		
		public boolean equals (Object o) {
			if (o instanceof Instance) {
				Instance ins = (Instance) o;
				return relationID == ins.relationID &&
					id == ins.id ;
			} else {
				return false;
			}
		}
		
		public int hashCode () {
			return (relationID + "_" + id).hashCode();
		}
		
		public String toString () {
			return relationID + "_" + id + "(" + weight + ")";
		}
	}
	
	public class DGEdge {
		public String toString () {
			return "dgEdge";
		}
	}
	
	public DirectedGraph<Instance, DGEdge> graph1 = 
		new DirectedSparseGraph<Instance, DGEdge> ();
	

	SchemaGraph sg;
	String dbName;
	SQLBackend sql = new SQLBackend ();
	
	public DataGraph (SchemaGraph sg, SQLBackend sqlBack) throws Exception{
		this.sg = sg;
		sql = sqlBack;
	}
	
	public void load (HashMap<String, Integer> relationMap) throws Exception {
		Set<Relation> relations = sg.getRelations();
		int count = 0;
		for (Relation relation: relations) {
			System.out.println ("\t\tLoad relation " + relation);
			loadVertex (count, relation.getName(), relation.getKey());
			relationMap.put(relation.getName(), count);
			count ++;
		}
		loadEdges (relationMap);
	}
	public void loadEdges (HashMap<String, Integer> relationMap) throws Exception{
		Set<JoinEdge> joinEdges = sg.getJoinEdges();
		for (JoinEdge joinEdge: joinEdges) {
			System.out.println ("\t\tLoad edge: " + joinEdge);
			String foreign = joinEdge.foreign;
			String primary = joinEdge.primary;
			String fID = joinEdge.foreignAtt;
			
			String stat = "SELECT " + sg.getRelation(foreign).getKey() + "," + 
				fID + " FROM " + foreign + " LIMIT 1000000";
			ResultSet rs = sql.executeQuery(stat);
			while (rs.next()) {
				int id = rs.getInt(sg.getRelation(foreign).getKey());
				int fid = rs.getInt(fID);
				Instance ins1 = new Instance(relationMap.get(foreign), id, 0);
				Instance ins2 = new Instance (relationMap.get(primary), fid, 0);
				if (ins1 != null && ins2 != null) {
					graph1.addEdge(new DGEdge(), ins1, ins2);
				}
			}
			rs.close();
		}
	}
	
	
	
	public void loadVertex (int relationID, String relation, String key) throws Exception {
		String stat = "SELECT " + key + " FROM " + relation + " LIMIT 1000000";
		ResultSet rs = sql.executeQuery(stat);
		while (rs.next()) {
			int tid = rs.getInt(key);
			Instance ins = new Instance (relationID, tid, 0);
			//graph.addVertex(ins);
			graph1.addVertex(ins);
		}
		rs.close();
	}
	

	public String toString () {
		return graph1.toString();
	}
	
//	public void setScores (double score) {
//		Set<Instance> instances= graph.vertexSet();
//		Set<Instance> visited = 
//			new HashSet<Instance> ();
//
//		for (Instance ins: instances) {
//			dfs (ins, visited, score, graph);
//		}
//	}
//	
//	public static void dfs (Instance instance, Set<Instance> visited, double score,
//			DefaultDirectedGraph<Instance, DGEdge> graph) {
//		if (visited.contains(instance)) {
//			return;
//		}
//		visited.add(instance);
//		Set<DGEdge> outEdges = graph.outgoingEdgesOf(instance);
//		for (DGEdge outEdge: outEdges) {
//			dfs (graph.getEdgeTarget(outEdge), visited, score, 
//					graph);
//		}
//		instance.weight = score;
//	}
//	
//	public void runPageRank (double damp) {
//		Set<Instance> instances= graph.vertexSet();
//		Set<Instance> visited = 
//			new HashSet<Instance> ();
//
//		int insNum = instances.size();
//		for (Instance ins: instances) {
//			dfs1 (ins, visited, damp, insNum, graph);
//		}
//	}
//	
//	public static void dfs1 (Instance instance, Set<Instance> visited, 
//			double damp, int insNum,
//			DefaultDirectedGraph<Instance, DGEdge> graph) {
//		if (visited.contains(instance)) {
//			return;
//		}
//		visited.add(instance);
//		double score = (1-damp) / (double) insNum;
//		Set<DGEdge> edges = graph.incomingEdgesOf(instance);
//		int inNum = edges.size();
//		for (DGEdge edge: edges) {
//			Instance link = graph.getEdgeSource(edge);
//			
//			System.out.println("Link: " + link + "," + 
//					graph.getEdgeTarget(edge));
//			score += damp * (link.weight / (double) inNum);
//		}
//		instance.weight = score;
//	}
	
	public static void main (String args[]) {
		try {
//			SchemaGraph sg = new SchemaGraph();
//			sg.buildFromFile("data/dblp/schema.dat");
//			DataGraph dg = new DataGraph (sg, "sqlsugg_dblp");
//			dg.load();
//			System.out.println (dg);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}












