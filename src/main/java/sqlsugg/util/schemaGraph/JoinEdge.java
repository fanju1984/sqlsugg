package sqlsugg.util.schemaGraph;

public class JoinEdge {
	public String foreign;
	public String primary;
	public String foreignAtt;
	public String primaryAtt;
	
	public Multiplicity multiplicity; // foreign --> primary
	
	public JoinEdge (String f, String p, String fa, String pa, 
			Multiplicity multi) {
		foreign = f;
		primary = p;
		foreignAtt = fa;
		primaryAtt = pa;
		multiplicity = multi;
	}
	public boolean equals (Object o) {
		if (o instanceof JoinEdge) {
			JoinEdge e = (JoinEdge) o;
			return (foreign.equals(e.foreign) &&
					primary.equals(e.primary) &&
					foreignAtt.equals(e.foreignAtt) && 
					primaryAtt.equals(e.primaryAtt));
		} else {
			return false;
		}
	}
	
	public int hashCode () {
		return (foreign+primary+ foreignAtt + primaryAtt).hashCode();
	}
	
	public JoinEdge copy () {
		JoinEdge ne = new JoinEdge(foreign, primary, foreignAtt, primaryAtt, multiplicity);
		return ne;
	}

	public String toString () {
		return foreignAtt + "=" + primaryAtt;
	}
	
}
