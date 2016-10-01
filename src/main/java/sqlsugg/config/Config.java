package sqlsugg.config;

public class Config {
	public Domain domain;
	public static String dbUser = "sqlsugg";
	public static String dbPass = "sqlsugg";
	public static String stopfile = "etc/stopwords.txt";
	public Config (String domainName) {
		if (domainName.equals("dblp")) {
		domain = new Domain ("dblp", 
				"data/dblp/schema.dat", 
				"sqlsugg_dblp");
		} else if (domainName.equals("scale")) {
			domain = new Domain ("scale", 
					"data/dblp/schema.dat", 
					"scale_dblp");
		}
		else if (domainName.equals("dblife")){
			domain = new Domain ("dblife", 
					"data/dblife/schema.dat", 
					"dblife_clean");
		} else if (domainName.equals("test")) {
			domain = new Domain ("test", 
					"data/dblp/schema.dat", 
					"sqlsugg_test");
		} else if (domainName.equals("dblp_server")) {
			domain = new Domain ("dblp_server", 
					"data/dblp_server/schema.dat", 
					"sqlsugg_dblp_server");
		}
	}
	
	public String getDomainName () {
		return domain.domainName;
	}
	
	public String getSchemaFile () {
		return domain.schemaFile;
	}
	
	public String getDBName () {
		return domain.dbName;
	}
}
