package sqlsugg.util;

public class Pair<S,T> {
	public S first;
	public T second;
	
	public Pair (S f, T s) {
		first = f;
		second = s;
	}
	
	public Pair () {
		
	}
	
	public String toString () {
		return "<" + first + "," + second + ">";
	}

}
