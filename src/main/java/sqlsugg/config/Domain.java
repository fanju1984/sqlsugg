package sqlsugg.config;

public class Domain {
	public String domainName;
	public String schemaFile;
	
	public String dbHost = "localhost"; // by default
	public String dbUser;
	public String dbPass;
	public String dbName;
	
	
	public Domain (String dn, String sf, 
			String dbH, String dbU, String dbP, String dbN) {
		domainName = dn;
		schemaFile = sf;
		dbHost = dbH;
		dbUser = dbU;
		dbPass = dbP;
		dbName = dbN;
		
	}
}
