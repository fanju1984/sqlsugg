package sqlsugg.mapping;

import java.util.*;



public abstract class MapSearcher {
	public abstract List<KeywordMap> searchMaps (String keyword, MapType mapType) throws Exception;
	
	public List<KeywordMap> searchMaps (String keyword, List<MapType> mapTypes) throws Exception {
		List<KeywordMap> maps = new LinkedList<KeywordMap> ();
		for (MapType mapType: mapTypes) {
			List<KeywordMap> list = searchMaps(keyword, mapType);
			maps.addAll(list);
		}
		return maps;
	}
	
	
}
