package sqlsugg.display;
import sqlsugg.template.*;
import sqlsugg.template.tgraph.*;
import sqlsugg.sqlgen.*;

import java.util.*;

import sqlsugg.util.Op;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.mapping.*;
import sqlsugg.mapping.maps.*;

public class Translator {
	SchemaGraph sg;
	
	public Translator (SchemaGraph pSg) {
		sg = pSg;
	}
	
	class RNodeNamespace {
		Map<Relation, Integer> maxInses = new HashMap<Relation, Integer> ();
		Map<RTNode, Integer> nodeInses = new HashMap<RTNode, Integer> ();
		
		public String getName (RTNode rnode) {
			Integer ins = nodeInses.get(rnode);
			if (ins == null) {
				Integer maxIns = maxInses.get(rnode.relation);
				if (maxIns == null) {
					maxIns = 0;
				}
				maxIns ++;
				maxInses.put(rnode.relation, maxIns);
				ins = maxIns;
				nodeInses.put(rnode, ins);
			}
			String name = rnode.relation.getName().toLowerCase()
				+ ins;
			return name;
		}
	}
	
	String generateGroupStat (RNodeNamespace rns, 
			Collection<MapIns> matching) {
		String groupStat = "";
		for (MapIns mapIns : matching) {
			KeywordMap keywordMap = mapIns.keywordMap;
			if (keywordMap instanceof K2FMap &&
					mapIns.childIns != null) {
				RTNode rnode = mapIns.childIns.rnode;
				ATNode anode = mapIns.childIns.anode;
				groupStat = rns.getName(rnode) + "." + anode.attribute.name;
				return groupStat;
			}
		}
		return groupStat;
	}
	
	String generateSelectStat (Template template, RNodeNamespace rns, 
			Collection<MapIns> matching) {
		String selectStat = "";
		boolean hasK2M = false;
		for (MapIns mapIns : matching) {
			KeywordMap keywordMap = mapIns.keywordMap;
			ATNode anode = mapIns.anode;
			RTNode rnode = mapIns.rnode;
			String raname = rns.getName(rnode) + "." + anode.attribute.name;
			if (keywordMap instanceof K2MMap) {
				selectStat += raname;
				selectStat += ",";
				hasK2M = true;
			} else if (keywordMap instanceof K2FMap) {
				K2FMap k2fMap = (K2FMap) keywordMap;
				selectStat += k2fMap.funcType.toString() + "(" + raname + ")";
				selectStat += ",";
				if (mapIns.childIns != null) {
					MapIns childIns = mapIns.childIns;
					selectStat += rns.getName(childIns.rnode) + "." + childIns.anode.attribute.name;
					selectStat += ",";
				}
				hasK2M = true;
			}
		}
		if (!hasK2M) {
			selectStat += " * ";
		}
		if (selectStat.length() > 0) {
			selectStat = selectStat.substring(0, selectStat.length() - 1);
		}
		return selectStat;
	}
	
	String generateFromStat (Set<RTNode> rnodes, RNodeNamespace rns) {
		String fromStat = "";
		int count = 0;
		for (RTNode rnode : rnodes) {
			fromStat += rnode.relation.getName() 
				+ " " + rns.getName(rnode);
			if (count < rnodes.size() - 1) {
				fromStat += ", ";
			}
			count ++;
		}
		return fromStat;
	}
	
	String generateJoins (Template template, Set<JTEdge> jedges, 
			RNodeNamespace rns) {
		String joinStat = "";
		int count = 0; 
		for (JTEdge jedge : jedges) {
			RTNode snode = template.graph.getEdgeSource(jedge);
			RTNode tnode = template.graph.getEdgeTarget(jedge);
			String sname = rns.getName(snode);
			String tname = rns.getName(tnode);
			JoinEdge edge = 
				sg.getJoinEdge(snode.relation.getName(), tnode.relation.getName());
			if (snode.relation.getName().equals(edge.primary)) {
				joinStat += sname + "." + edge.primaryAtt;
				joinStat += " = ";
				joinStat += tname + "." + edge.foreignAtt;
			} else {
				joinStat += sname + "." + edge.foreignAtt;
				joinStat += " = ";
				joinStat += tname + "." + edge.primaryAtt;
			}
			if (count < jedges.size() - 1) {
				joinStat += " AND ";
			}
			count ++;
		}
		return joinStat;
	}
	
	String generateImpPredicates (Template template, 
			Collection <MapIns> matching, RNodeNamespace rns, 
			Set<String> invs) {
		String predicateStat = "";
		int invMax = 0;
		for (MapIns mapIns : matching) {
			KeywordMap keywordMap = mapIns.keywordMap;
			if (keywordMap.type == MapType.K2V ) {
				K2VMap k2vMap = (K2VMap) keywordMap;
				ATNode anode = mapIns.anode;
				RTNode rnode = mapIns.rnode;
				String rname = rns.getName(rnode);
				Op op = k2vMap.op;
				if (op == Op.CONTAINS) {
					String invTable = "i" + invMax;
					invMax ++;
					invs.add(invTable);
					predicateStat += invTable + ".word = \"" 
						+ k2vMap.value + "_" + rnode.relation.getName() + 
						"." + anode.attribute.name + ".value"+ "\"";
					predicateStat += " AND ";
					predicateStat += invTable + ".rcdid = " + 
						rname + ".id";
				} else {
					predicateStat += rname + "." + anode.attribute.name;
					predicateStat += " " + op.toString() + " ";
					predicateStat += op.getQuota() + k2vMap.value + op.getQuota();
				}
				
				predicateStat += " AND ";
			}
		}
		if (predicateStat.length() > 0) {
			predicateStat = predicateStat.substring(0, predicateStat.length() - 5);
		}
		return predicateStat;
	}

	String generatePredicates (Template template, 
			Collection <MapIns> matching, RNodeNamespace rns) {
		String predicateStat = "";
		for (MapIns mapIns : matching) {
			KeywordMap keywordMap = mapIns.keywordMap;
			if (keywordMap instanceof K2VMap) {
				K2VMap k2vMap = (K2VMap) keywordMap;
				ATNode anode = mapIns.anode;
				RTNode rnode = mapIns.rnode;
				String rname = rns.getName(rnode);
				predicateStat += rname + "." + anode.attribute.name;
				Op op = k2vMap.op;
				predicateStat += " " + op.toString() + " ";
				predicateStat += op.getQuota() + k2vMap.value + op.getQuota();
				predicateStat += " AND ";
			}
		}
		if (predicateStat.length() > 0) {
			predicateStat = predicateStat.substring(0, predicateStat.length() - 5);
		}
		return predicateStat;
	}
	
	public String translateSQL (SQLStruct sqlStruct) {
		Template template = sqlStruct.template;
		Collection <MapIns> matching = sqlStruct.matching;
		RNodeNamespace rns  = new RNodeNamespace();
		Set<RTNode> rnodes = template.getRTNodes();
		Set<JTEdge> jedges = template.getJTEdges();	
		String selectStat = this.generateSelectStat(template, rns, matching);
		String fromStat = this.generateFromStat(rnodes, rns);
		String joinStat = this.generateJoins(template, jedges, rns);
		String predicateStat = this.generatePredicates(template, matching, rns);
		String sqlStat = "SELECT " + selectStat + "\n";
		sqlStat += "FROM " + fromStat + "\n";
		if (predicateStat.length() > 0 || joinStat.length() > 0) {
			sqlStat += "WHERE ";
			sqlStat += predicateStat;
			if (predicateStat.length() > 0 && 
					joinStat.length() > 0) {
				sqlStat += " AND \n";
			}
			sqlStat += joinStat + "\n";
		}
		String groupStat = generateGroupStat(rns, matching);
		if (groupStat.length() > 0) {
			sqlStat += "GROUP BY " + groupStat + "\n";
		}
		return sqlStat;
	}
	
	public String translateSQLImp (SQLStruct sqlStruct) {
		Template template = sqlStruct.template;
		Collection <MapIns> matching = sqlStruct.matching;
		RNodeNamespace rns  = new RNodeNamespace();
		Set<RTNode> rnodes = template.getRTNodes();
		Set<JTEdge> jedges = template.getJTEdges();	
		Set<String> invs = new HashSet<String> ();
		String predicateStat = this.generateImpPredicates(template, matching, rns, invs);
		String selectStat = this.generateSelectStat(template, rns, matching);
		String fromStat = this.generateFromStat(rnodes, rns);
		String joinStat = this.generateJoins(template, jedges, rns);
		for (String inv : invs) {
			fromStat += ", " + "inv_index " + inv;
		}
		
		String sqlStat = "SELECT " + selectStat + "\n";
		sqlStat += "FROM " + fromStat + "\n";
		if (predicateStat.length() > 0 || joinStat.length() > 0) {
			sqlStat += "WHERE ";
			sqlStat += predicateStat;
			if (predicateStat.length() > 0 && 
					joinStat.length() > 0) {
				sqlStat += " AND \n";
			}
			sqlStat += joinStat;
		}
		String groupStat = generateGroupStat(rns, matching);
		if (groupStat.length() > 0) {
			sqlStat += groupStat;
		}
		return sqlStat;
	}
}
