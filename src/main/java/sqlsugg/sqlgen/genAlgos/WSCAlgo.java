package sqlsugg.sqlgen.genAlgos;


import java.util.*;


import sqlsugg.sqlgen.SQLGenerator;
import sqlsugg.sqlgen.SQLStruct;
import sqlsugg.template.*;
import sqlsugg.util.basicstruct.*;
import sqlsugg.mapping.*;
import sqlsugg.template.tgraph.*;
import sqlsugg.sqlgen.*;
import sqlsugg.scoring.*;
import sqlsugg.util.schemaGraph.*;

public class WSCAlgo extends SQLGenerator {
	
	public WSCAlgo (MapSearcher pTxtSearcher, MapSearcher pNumSearcher,
			Scorer pScorer, SchemaGraph sg) throws Exception {
		super(pTxtSearcher, pNumSearcher, pScorer, sg);
	}
	
	public SortedList<String, SQLStruct> rank (Template t, List<String> keywords,
			SortedList<String, MapIns> mappings, int k) 
			throws Exception {
		
		//System.out.println("Mappings: " + mappings);
		SortedList<String, SQLStruct> topk = new SortedList<String, SQLStruct> ("top-k");
		Template template = t;
		//step 2: run a greedy approximation algorithm
		Stack<MapIns> matching = new Stack<MapIns> ();
		MapIns lastMap = null;
		while (true) {
			boolean isQualified = examineQualified (matching, keywords, template);
			if (isQualified) {
				Collection<MapIns> cmatching = new HashSet<MapIns> ();
				cmatching.addAll(matching);
				this.postProcessing(cmatching);
				SQLStruct sqls = new SQLStruct (template, cmatching);
				double score = 0.0;
				for (MapIns mapIns : cmatching) {
					score += mapIns.keywordMap.score();
				}
				ScoredItem<SQLStruct> item = 
					new ScoredItem<SQLStruct> (sqls, score);
				topk.add(item);
				if (topk.size() == k) {
					break;
				}
				lastMap = matching.pop();//backtrack
			} else {
				MapIns mapping = getMapping (matching, mappings, lastMap);
				if (mapping == null) {
					if (matching.isEmpty()) {
						break;
					} else {
						lastMap = matching.pop();//backtrack
					}
				} else {
					matching.push(mapping);
					lastMap = mapping;
				}
			}
		}
		return topk;
	}
	
	/**
	 * A set of mappings is a matching if and only if:
	 * 1) the set of mappings has covered all query keywords, AND
	 * @param matching
	 * @param tokens
	 * @return
	 */
	private boolean examineQualified (Stack<MapIns> matching, List<String> tokens, 
			Template template) {
		// Criterion 1: Whether the set of mappings has covered all query keywords
		Set<String> coveredKeywords = getCoveredKeywords(matching);
		boolean isCovered = coveredKeywords.containsAll(tokens);
		if (!isCovered) {
			return false;
		}
		//Criterion 2: Whether the leaf rtnodes in the template are all covered
		Set<RTNode> leafRTNodes = template.getLeafNodes();
		Set<RTNode> coveredRTNodes = this.getCoveredRTNodes(matching);
		if (!coveredRTNodes.containsAll(leafRTNodes)) {
			return false;
		}
		
		return true;
	}
	
	Set<String> getCoveredKeywords (Stack<MapIns> matching) {
		Set<String> keywords = new HashSet<String> ();
		for (MapIns mapping: matching) {
			keywords.addAll(mapping.keywordMap.coveredKeywords);
		}
		return keywords;
	}
	
	Set<RTNode> getCoveredRTNodes (Stack<MapIns> matching) {
		Set<RTNode> rtNodes = new HashSet<RTNode> ();
		for (MapIns mapping: matching) {
			rtNodes.add(mapping.rnode);
		}
		return rtNodes;
	}
	
	private MapIns getMapping (Stack<MapIns> matching, SortedList<String, MapIns> mappings, 
			MapIns lastMapping) {
		boolean flag = false;
		Set<String> coveredKeywords = getCoveredKeywords (matching);
		if (lastMapping == null) {
			flag = true;
		}
		mappings.initIterator();
		while (mappings.hasNext()) {
			MapIns mapping = mappings.next().getItem();
			if (!flag) {
				if (mapping == lastMapping) {
					flag = true;
				}
			} else {
				boolean canRet = true;
				// Rule 1: Examine the covered keywords.
				for (String keyword : mapping.keywordMap.coveredKeywords) {
					if (coveredKeywords.contains(keyword)) {
						canRet = false;
						break;
					}
				}
				// Rule 2: Examine the conflicts or redundancies of mappings
				for (MapIns extMap : matching) {
					if (extMap.keywordMap.hasConflicts(mapping.keywordMap) || 
							extMap.keywordMap.hasRedundancies(mapping.keywordMap)) {
						canRet = false;
						break;
					}
				}
				
				
				if (canRet) {
					mappings.destroyIterator();
					return mapping;
				}
			}
		}
		mappings.destroyIterator();
		return null;
	}
}
