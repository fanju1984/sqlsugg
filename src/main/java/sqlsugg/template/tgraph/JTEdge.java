package sqlsugg.template.tgraph;


public class JTEdge extends TEdge{
	public String condition;
	
	public JTEdge (String con) {
		super(0);
		condition = con;
	}
	
	public JTEdge () {
		super(0);
		condition = "";
	}
	
	public JTEdge (String con, double s) {
		super(s);
		condition = con;
	}
	public TEdge copy () {
		TEdge e = new JTEdge(condition);
		return e;
	}
	
	public String toString () {
		return "join";
	}
}
