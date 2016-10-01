package sqlsugg.util.basicstruct;

import java.util.*;

public class SortedList<S, T> {
	S name;
	TreeSet<ScoredItem<T>> list;
	Iterator<ScoredItem<T>> iterator = null;

	public SortedList(S n, TreeSet<ScoredItem<T>> l) {
		name = n;
		list = l;
	}

	public SortedList(S n) {
		name = n;
		list = new TreeSet<ScoredItem<T>>();
	}

	public SortedList() {
		name = null;
		list = new TreeSet<ScoredItem<T>>();
	}

	// public int binarySearch ()

	public void add(ScoredItem<T> item) {
		list.add(item);
	}

	public void initIterator() {
		iterator = list.iterator();
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public ScoredItem<T> next() {
		return iterator.next();
	}

	public void destroyIterator() {
		iterator = null;
	}

	public S getS() {
		return name;
	}

	public int size() {
		return list.size();
	}

	public ScoredItem<T> getFloor(ScoredItem<T> item) {
		return list.floor(item);
	}

	public ScoredItem<T> getCeiling(ScoredItem<T> item) {
		return list.ceiling(item);
	}

	public ScoredItem<T> get(int index) {
		int count = 0;
		for (ScoredItem<T> item : list) {
			if (count == index) {
				return item;
			}
			count++;
		}
		return null;
	}

	public String toString() {
		return name.toString() + ":" + list.toString();
	}
}