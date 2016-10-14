package sqlsugg.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class maintain a pool of domain configurations. 
 * This class is basically used by the web service
 * @author fanj
 *
 */
public class DomainPool {
	Map<String, Domain> domains; // name -> domain
	
	public DomainPool (String contextPath, String domainRegFile) {
		System.out.println ("[SQLSugg]: Load domain reg file from `" + contextPath + domainRegFile + "`");
		domains = new HashMap<String, Domain> ();
		File file = new File (contextPath + domainRegFile);
		StringBuffer buff = new StringBuffer();
		try {
			if (!file.exists()) 
				throw new Exception ("Domain registry file is missing");	
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
		for (int i = 0; i < jdss.length(); i ++) { 
			JSONObject jds = jdss.getJSONObject(i);
			// domain name
			String domainName = jds.getString("name");
			
			// domain details
			String dbHost = jds.getString("dbHost");
			String dbUser = jds.getString("dbUser");
			String dbPass = jds.getString("dbPass");
			
			Domain domain = new Domain (domainName, 
					contextPath + jds.getString("schemaFile"), 
					dbHost, dbUser, dbPass,
					jds.getString("dbName"));
			domains.put(domainName, domain);
		}
	}
	
	public Map<String, Domain> getDomains () {
		return domains;
	}
}
