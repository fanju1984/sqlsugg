package sqlsugg.config;

public class Domain {
	public String domainName;
	public String schemaFile;
	public String dbName;
	
	public Domain (String dn, String sf, String db) {
		domainName = dn;
		schemaFile = sf;
		dbName = db;
	}
}
