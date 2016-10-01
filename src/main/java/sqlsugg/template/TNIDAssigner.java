package sqlsugg.template;

public class TNIDAssigner {
	int tnID = -1;
	
	public int getTNID () {
		tnID ++;
		return tnID;
	}
	
	TNIDAssigner copy () {
		TNIDAssigner assigner = new TNIDAssigner ();
		assigner.tnID = tnID;
		return assigner;
	}
}
