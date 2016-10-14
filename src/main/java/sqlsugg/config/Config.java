package sqlsugg.config;

import java.io.*;

import org.json.JSONArray;
import org.json.JSONObject;



public class Config {
	public Domain domain;
	public static String dbHost = "localhost";
	public static String dbUser = null;
	public static String dbPass = null;
	public static String stopfile = "etc/stopwords.txt";
	final String dsFilename = "etc/dataset_config.json";
	
	public Config (String domainName) {
		// Step 1: Register Datasets from File 
		File file = new File (dsFilename);
		StringBuffer buff = new StringBuffer();
		try {
			if (!file.exists()) 
				throw new Exception ("Dataset registry file is missing");	
			BufferedReader r = new BufferedReader (
					new FileReader(file.getAbsolutePath()));
			String line = r.readLine();
			while (line != null) {
				buff.append(line);
				buff.append("\n");
				line = r.readLine();
			}
			r.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
				
		String str = buff.toString();
		
		JSONArray jdss = new JSONArray (str);
		for (int i = 0; i < jdss.length(); i ++) { // start to parse command conf-file
			JSONObject jds = jdss.getJSONObject(i);
			// command name
			String dsName = jds.getString("name");
			if (!dsName.equals(domainName)) continue;
			// command class
			dbHost = jds.getString("dbHost");
			dbUser = jds.getString("dbUser");
			dbPass = jds.getString("dbPass");
			
			domain = new Domain (dsName, 
					"etc/" + jds.getString("schemaFile"), 
					dbHost, dbUser, dbPass, 
					jds.getString("dbName")
					);
			break;
		}
		
		/*
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
		}*/
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
