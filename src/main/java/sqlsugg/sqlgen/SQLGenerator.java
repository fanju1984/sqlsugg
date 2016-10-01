package sqlsugg.sqlgen;

import java.util.*;

import sqlsugg.template.*;
import sqlsugg.template.tgraph.ATNode;
import sqlsugg.template.tgraph.RTNode;
import sqlsugg.mapping.*;
import sqlsugg.mapping.maps.*;
import sqlsugg.util.basicstruct.*;
import sqlsugg.scoring.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.*;

/**
 * This class is to generate a list of SQL queries from a template.
 * In particular, the obtained SQL queries are sorted by their scores, 
 * which are computed by a "Scorer".
 * 
 * In general, this class generates SQL queries as follows. 
 * 1) Obtain all possible mappings using a MapSearcher. 
 * 2) Compute the overall score for each obtained mapping. 
 * 3) Construct a MapIns of each mapping to associate the mapping to 
 *    the specific RTNode and ATNode. 
 * 3) Optionally, compute the relationships among the mappings.
 * 4) Employ an efficient algorithm to compute candidate matchings, 
 *    which consists of a set of mappings satisfying some constraints. 
 * 
 * @author Ju Fan
 *
 */
public abstract class SQLGenerator {
	MapSearcher txtSearcher;
	MapSearcher numSearcher;
	protected Scorer scorer;
	SchemaGraph sg;
	
	public SQLGenerator (MapSearcher pTxtSearcher, MapSearcher pNumSearcher, 
			Scorer pScorer, SchemaGraph pSg) {
		txtSearcher = pTxtSearcher;
		numSearcher = pNumSearcher;
		scorer = pScorer;
		sg = pSg;
	}
	
	ScoredItem<Pair<KeywordMap, KeywordMap>> pickBestK2MMap (Template template, 
			List<String> keywords, String fKeyword, 
			List<KeywordMap> k2fMaps, 
			List<KeywordMap> k2mMaps) throws Exception {
		TreeSet<ScoredItem<Pair<KeywordMap, KeywordMap>>> buffer = 
			new TreeSet<ScoredItem<Pair<KeywordMap, KeywordMap>>> ();
		for (KeywordMap k2fMap : k2fMaps) {
			for (KeywordMap k2mMap : k2mMaps) {
				double score = 
					scorer.getK2FScore(template, (K2FMap)k2fMap, (K2MMap)k2mMap, keywords);
				Pair<KeywordMap, KeywordMap> p = new Pair<KeywordMap, KeywordMap>(k2fMap, k2mMap);
				ScoredItem<Pair<KeywordMap, KeywordMap>> newItem = 
					new ScoredItem<Pair<KeywordMap, KeywordMap>> (p, score);
				buffer.add(newItem);
			}
		}
		if (buffer.size() > 0) {
			return buffer.first();
		} else {
			return null;
		}
	}
		
	
	void generateK2FMapInstances (SortedList<String, MapIns> mapInstances, 
			Template template, List<KeywordMap> k2mMaps, 
			Map<String, List<KeywordMap>> k2fMaps, 
			List<String> keywords) throws Exception {
		// TODO: Now we only consider one function keyword, and do NOT consider 
		// the nested issues. 
		for (String keyword : k2fMaps.keySet()) {
			if (k2mMaps.size() > 0) {
				ScoredItem<Pair<KeywordMap, KeywordMap>> item = pickBestK2MMap (template, 
						keywords, keyword, 
						k2fMaps.get(keyword), k2mMaps);
				if (item != null) {
					KeywordMap k2mMap = item.getItem().second;
					double score = item.getScore();
					k2mMaps.remove(k2mMap);
					Set<RTNode> rtNodes = template.getRTNodes(k2mMap.getRStr());
					if (rtNodes != null) {
						for (RTNode rtNode: rtNodes) {
							ATNode atNode = rtNode.getATNode(k2mMap.getAStr());
							KeywordMap k2fMap = item.getItem().first;
							k2fMap.addCoveredKeywords(k2mMap.getKStr());
							MapIns mapIns = new MapIns (k2fMap, rtNode, atNode);
							ScoredItem<MapIns> insItem = new ScoredItem<MapIns>(mapIns, 
									score);
							mapInstances.add(insItem);
						}
					}
				}
			}
		}
	}
	
	void generateMapByType (SortedList<String, MapIns> mapInstances, 
			Template template, List<KeywordMap> maps) throws Exception {
		for (KeywordMap map: maps) {
			String aStr = map.getAStr();
			String rStr = map.getRStr();
			Set<RTNode> rtNodes = template.getRTNodes(rStr);
			if (rtNodes != null) {
				for (RTNode rtNode: rtNodes) {
					scorer.computeOverallMapScore(template, map);
					ATNode atNode = rtNode.getATNode(aStr);
					MapIns mapIns = new MapIns (map, rtNode, atNode);
					ScoredItem<MapIns> item = new ScoredItem<MapIns>(mapIns, 
							mapIns.keywordMap.score());
					mapInstances.add(item);
				}
			}
			
		}
	}
	
	public SortedList<String, SQLStruct> generate (Template template, 
			List<String> keywords, int k) throws Exception {
		// Step 1: Obtain the mappings for the keywords.
		List<KeywordMap> k2vMaps = new LinkedList<KeywordMap> ();
		List<KeywordMap> k2mMaps = new LinkedList<KeywordMap> ();
		
		Map<String, List<KeywordMap>> k2fMaps = 
			new HashMap<String, List<KeywordMap>> ();
		// The K2F maps are grouped by their corresponding keywords. 
		
		for (String keyword : keywords) {
			k2vMaps.addAll(txtSearcher.searchMaps(keyword, MapType.K2V));
			k2vMaps.addAll(numSearcher.searchMaps(keyword, MapType.K2V));
			k2mMaps.addAll(txtSearcher.searchMaps(keyword, MapType.K2M));
			k2fMaps.put(keyword, txtSearcher.searchMaps(keyword, MapType.K2F));
		}
		
		SortedList<String, MapIns> mapInstances = new SortedList<String, MapIns> ("maps");
		if (k2fMaps.size() > 0) {
			if (k2mMaps.size() > 0) {
				generateK2FMapInstances (mapInstances, 
						template, k2mMaps, k2fMaps, keywords);
			} else {
				List<KeywordMap> newK2mMaps = new LinkedList<KeywordMap> ();
				for (KeywordMap k2vMap : k2vMaps) {
					String rStr = k2vMap.getRStr();
					String aStr = sg.getRelation(rStr).getKey();
					KeywordMap k2mMap = new K2MMap (-1, null, rStr, aStr);
					newK2mMaps.add(k2mMap);
				}
				generateK2FMapInstances (mapInstances, 
						template, newK2mMaps, k2fMaps, keywords);
			}
		}
		
		this.generateMapByType(mapInstances, template, k2vMaps);
		this.generateMapByType(mapInstances, template, k2mMaps);
		
		
		// Step 2: Employ a ranking algorithm to obtain a sorted list of SQL Structs. 
		SortedList<String, SQLStruct> genSQLs = rank (template, keywords, mapInstances, k);
		return genSQLs;
	}
	
	public void postProcessing (Collection<MapIns> matching) {
		MapIns k2fIns = null;
		MapIns k2mIns = null;
		for (MapIns ins : matching) {
			if (ins.keywordMap instanceof K2FMap) {
				k2fIns = ins;
			} else if (ins.keywordMap instanceof K2MMap) {
				k2mIns = ins;
			}
		}
		if (k2fIns != null && k2mIns != null) {
			k2fIns.childIns = k2mIns;
			matching.remove(k2mIns);
		}
	}
	
	public abstract SortedList<String, SQLStruct> rank (Template template, List<String> keywords,
			SortedList<String, MapIns> mappings, int k) throws Exception ;
}
