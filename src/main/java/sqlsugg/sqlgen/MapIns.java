package sqlsugg.sqlgen;

import sqlsugg.mapping.*;
import sqlsugg.template.tgraph.*;

/**
 * Different from the KeywordMap, this class is to associate a KeywordMap 
 * to a specific RTNode and ATNode.
 * 
 * @author Ju Fan
 *
 */
public class MapIns implements Comparable <Object>{
	public KeywordMap keywordMap;
	public RTNode rnode;
	public ATNode anode;
	
	public MapIns childIns = null;
	
	public MapIns (KeywordMap pKeywordMap, RTNode pRNode, ATNode pANode) {
		keywordMap = pKeywordMap;
		rnode = pRNode;
		anode = pANode;
	}
	
	public int compareTo(Object obj) {
		MapIns ins = (MapIns) obj;
		return keywordMap.compareTo(ins.keywordMap);
	}
	
	public boolean equals (Object obj) {
		MapIns ins = (MapIns) obj;
		return this.keywordMap.equals(ins.keywordMap);
	}
	
	public String toString () {
		return keywordMap.toString() + "  " + (rnode != null) + "," + (anode != null);
	}
}
