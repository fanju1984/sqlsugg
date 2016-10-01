package sqlsugg.util.basicalgo;

import java.util.*;

import sqlsugg.util.basicstruct.*;

/**
 * Using binary-search for random access.
 * @author jfan
 *
 * @param <S>
 * @param <T>
 */
public class RanAccIndex<S,T> {
	String name;
	Map<S,Map<T, Double>> map = new HashMap<S, Map<T, Double>> ();
	
	public RanAccIndex (String n) {
		name = n;
	}
	
	//static void addList<ScoredItem>
	
	public void put (S key, ScoredItem<T> item) {
		Map<T, Double> localMap = map.get(key) ;
		if (localMap == null) {
			localMap = new HashMap<T, Double> ();
		}
		localMap.put(item.getItem(), item.getScore());
		map.put(key, localMap);
	}
	
	public ScoredItem<T> get (S key, T item) {
		Map<T, Double> localMap = map.get(key) ;
		if (localMap == null) {
			return null;
		}
		Double score = localMap.get(item);
		if (score == null) {
			return null;
		}
		ScoredItem<T> ret = new ScoredItem<T> (item, score);
		return ret;
	}
	
	public Set<T> get (S key) {
		Map<T, Double> localMap = map.get(key) ;
		if (localMap == null) {
			return null;
		}
		return localMap.keySet();
	}
	
	public String toString () {
		return name + ":" + map;
	}
	
	public void clear () {
		this.map.clear();
	}
}
