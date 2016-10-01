package sqlsugg.template.tgraph;

public abstract class TEdge implements Comparable<Object>{
	double score;
	
	public TEdge (double s) {
		score = s;
	}
	public abstract TEdge copy ();
	
	public int compareTo (Object o) {
		TEdge edge = (TEdge)o;
		if (score > edge.score) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public String toString () {
		return "(" + score + ")";
	}
}
