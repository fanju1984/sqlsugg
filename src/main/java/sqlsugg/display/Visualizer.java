package sqlsugg.display;

import java.util.*;

import sqlsugg.sqlgen.*;
import sqlsugg.mapping.*;
import sqlsugg.mapping.maps.K2FMap;
import sqlsugg.template.*;
import sqlsugg.template.tgraph.RTNode;
import sqlsugg.util.schemaGraph.SchemaGraph;


public class Visualizer {
	
	public static String visualize (SQLStruct sqlStruct, SchemaGraph sg) {
		Template template = sqlStruct.template;
		Collection<MapIns> matching = sqlStruct.matching;
		Collection<MapIns> mappings = new HashSet<MapIns> ();
		for (MapIns mapIns : matching) {
			mappings.add(mapIns);
			if (mapIns.childIns != null) {
				mappings.add(mapIns.childIns);
			}
		}
		Set<RTNode> leafNodes = template.getLeafNodes();
		if (leafNodes.size() == 0) {
			return "";
		} 
		Iterator<RTNode> it = leafNodes.iterator();
		RTNode start = it.next();
		Set<RTNode> visited = new HashSet<RTNode> ();
		List<String> nodes = new LinkedList<String> ();
		List<String> edges = new LinkedList<String> ();
		traverseTemplate (nodes, edges, start, template, mappings,
				visited, 0);
		StringBuffer graphicsBuffer = new StringBuffer ();
		graphicsBuffer.append("  <graphics>\n");
		graphicsBuffer.append("   <nodes>\n");
		for (String node: nodes) {
			graphicsBuffer.append(node);
		}
		graphicsBuffer.append("   </nodes>\n");
		graphicsBuffer.append("   <edge>\n");
		for (String edge: edges) {
			graphicsBuffer.append(edge);
		}
		graphicsBuffer.append("   </edge>\n");
		graphicsBuffer.append("  </graphics>\n");
		return graphicsBuffer.toString();
	}
	
	static void traverseTemplate (List<String> nodes,List<String> edges,
			RTNode node, Template template, Collection<MapIns> matching, 
			Set<RTNode> visited, int count) {
		if (visited.contains(node)) {
			return;
		}
		visited.add(node);
		List<RTNode> adjs = template.getAdjacentRTNodes(node);
		if (count % 2 == 0) {
			String nodeStr = new String ();
			nodeStr += "    <node id='" + count / 2 + "' name='" +
			node.relation.getName() + "'>\n";
			for (MapIns mapIns : matching) {
				if (mapIns.rnode.equals(node)) {
					String cdt = "";
					if (mapIns.keywordMap.type == MapType.K2V) {
							cdt += mapIns.anode.attribute.name 
								+ ":" + mapIns.keywordMap.getKStr() ;
					} else if (mapIns.keywordMap.type == MapType.K2F) {
						K2FMap k2fMap = (K2FMap) mapIns.keywordMap;
						cdt += k2fMap.funcType + "(" + k2fMap.getAStr() + ")";
					} else if (mapIns.keywordMap.type == MapType.K2M) {
						cdt += "GROUP BY " + mapIns.keywordMap.getAStr();
					}
					if (cdt.length() > 0) {
						nodeStr += "     <predicate cdt='" + cdt + "'/>\n";
					}
				}
			}
			nodeStr += "    </node>\n";
			nodes.add(nodeStr);
		} else {
			String edgeStr = new String ();
			edgeStr += "    <edge name='" + node.relation.getName() + "'/>\n";
			edges.add(edgeStr);
		}
		
		for (RTNode adj: adjs) {
			traverseTemplate (nodes, edges, adj, template, matching,
					visited, count + 1);
		}
		
	}
}
