package sqlsugg.util.basicalgo;

import java.util.*;

import sqlsugg.util.basicstruct.*;

public class ThresholdAlgo<S,T> {
	Map<SortedList<S, T>, RanAccIndex<T,S>> map;
	RanAccIndex<T,S> fwdIndex;
	
	public ThresholdAlgo (Map<SortedList<S, T>, RanAccIndex<T,S>> m) {
		map = m;
	}
	
	public ThresholdAlgo (RanAccIndex<T,S> f) {
		fwdIndex = f;
	}
	
	/**
	 * run the TA algorithm with SUM-manner inputs.
	 * @param lists: the multiple lists
	 * @param k: the number of results to be computed
	 * @param andLogic: each result item MUST have partial scores in all lists.
	 * @return
	 */
	public SortedList<String, T> run (List<SortedList<S, T>> lists, List<Double> params, int k, 
			boolean andLogic) {
		//step 1: prepare the iteration.
		for (SortedList<S, T> list: lists) {
			list.initIterator();
		}
		//step2: run a TA-based algorithm
		SortedList<String, T> topk =
			new SortedList<String, T> ("top-" + k);
		Set<T> computed = new HashSet<T> (); 
		//caching items which have been already computed to avoid duplicated computation.
		while (true) {
			double threshold = 0;
			boolean hasUnseen = false;
			boolean promising = true;
			
			for (int i = 0; i < lists.size(); i ++) {
				SortedList<S, T> list = lists.get(i);
				double param = params.get(i);
				if (!list.hasNext()) {
					continue;
				}
				ScoredItem<T> item = list.next();	
				//update the threshold.
				threshold += item.getScore() * param;
				hasUnseen = true;
				if (computed.contains(item.getItem())) {//we have already compute the score.
					continue;
				}
				//Now we compute the score of sItem.getItem()
				double score = item.getScore() * param;
				for (int j = 0; j < lists.size(); j ++) {
					if (i != j) {
						SortedList<S,T> list1 = lists.get (j);
						S name = list1.getS();
						RanAccIndex<T,S> fwd =  findFwdIndex(list1);
						ScoredItem<S> item1 = fwd.get(item.getItem(), name);
						if (item1 != null) {
							score += item1.getScore() * params.get(j);
						} else {
							if (andLogic) {
								promising = false;
								break;
							}
						}
					}
				}
				computed.add(item.getItem());
				if (promising) {
					ScoredItem<T> cache = new ScoredItem <T> (item.getItem(), score);
					topk.add(cache);
				}
			}
			if (!hasUnseen) {
				break;
			}
			if (topk.size() >= k) {
				ScoredItem<T> kItem = (ScoredItem<T>) topk.get(k - 1);
				if (kItem.getScore() >= threshold) {
					break;
				}
			}
		}
		return topk;
	}
	
	public RanAccIndex<T,S> findFwdIndex (SortedList<S,T> list) {
		if (fwdIndex != null) {
			return fwdIndex;
		} else {
			return map.get(list);
		}
	}
}
