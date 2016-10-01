package sqlsugg.sqlgen;

import java.util.*;

import sqlsugg.template.*;

public class SQLStruct{

	public Template template;
	public Collection <MapIns> matching;
	
	
	public SQLStruct (Template pTemplate, 
			Collection<MapIns> pMatching) {
		template = pTemplate;
		matching = pMatching;
	}
	
	public String toString () {
		return matching.toString();
	}
}
