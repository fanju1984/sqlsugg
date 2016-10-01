package sqlsugg.mapping;
/**
 * This class records the various types of the interesting mappings
 * @author Ju Fan
 *
 */

import sqlsugg.mapping.maps.*;
import sqlsugg.mapping.mapfactory.*;
import sqlsugg.mapping.mapfactory.dbsummary.*;
import sqlsugg.backends.*;
import sqlsugg.util.schemaGraph.*;

public enum MapType {
	K2V (1),
	K2M  (2),
	K2F  (3),
	K2R  (4);
	
	private int type;
	
	MapType (int t) {
		type = t;
	}
	
	public int getType () {
		return type;
	}
	
	public static MapType parse (String str) {
		if (str.equals("K2V")) {
			return MapType.K2V;
		} else if (str.equals("K2R")) {
			return MapType.K2R;
		} else if (str.equals("K2F")) {
			return MapType.K2F;
		} else if (str.equals("K2M")) {
			return MapType.K2M;
		} else {
			return null;
		}
	}
	
	public String getRelationStr () {
		return "relation";
	}
	
	public String getAttributeStr () {
		String str = null;
		if (this == MapType.K2R) {
			str = null;
		} else {
			str = "attribute";
		}
		return str;
	}
	
	public KeywordMap insMap (int pMid, String pKStr, String pRStr, String pAStr) {
		KeywordMap map = null;
		switch (type) {
		case 1: 
			map = new K2VMap (pMid, pKStr, pRStr, pAStr);
			break;
		case 2: 
			map = new K2MMap (pMid, pKStr, pRStr, pAStr);
			break;
		case 3: 
			map = new K2FMap (pMid, pKStr, pRStr, pAStr);
			break;
		case 4:
			map = new K2RMap (pMid, pKStr, pRStr);
			break;
		}
		return map;
	}
	
	public String getDBTableName () {
		String str;
		switch (type) {
		case 1:
			str = "vk_index";
			break;
		case 2:
			str = "mk_index";
			break;
		case 3:
			str = "fk_index";
			break;
		case 4: 
			str = "rk_index";
			break;
		default:
			str = null;
		}
		return str;
	}
	
	public String getCreateTableStat () {
		String str = null;
		String tableName = this.getDBTableName();
		if (type <= 3) {
			str = "CREATE TABLE IF NOT EXISTS " + tableName + " (mid int key auto_increment, keyword varchar(200)," +
			"relation varchar(100), attribute varchar(100), score double)";
		} else if (type == 4) {
			str = "CREATE TABLE IF NOT EXISTS " + tableName + " (mid int key auto_increment, keyword varchar(200)," +
			"relation varchar(100), score double)";
		}
		return str;
	}
	
	public String getAttributeList () {
		String str = null;
		if (type <= 3) {
			str = "(keyword, relation, attribute, score)";
		} else if (type == 4) {
			str = "(keyword, relation, score)";
		}
		return str;
	}
	
	public MapFactory getMapFactory (SchemaGraph sg, SQLBackend sql, SQLBackend sql1) throws Exception {
		MapFactory mapFactory = null;
		if (this == MapType.K2V || this == MapType.K2R) {
			mapFactory = new TxtAsSummarizer (this, sg, sql,sql1);
		} else if (this == MapType.K2M) {
			mapFactory = new SchemaSummarizer(this, sg); 
		} else if (this == MapType.K2F) {
			mapFactory = new FuncSummarizer (this, sg, sql);
		}
		return mapFactory;
		
	}
	
	public String toString () {
		String str = new String ();
		switch (type) {
		case 1:
			str = "K2V";
			break;
		case 2:
			str = "K2M";
			break;
		case 3:
			str = "K2F";
			break;
		case 4: 
			str = "K2R";
			break;
		case 5:
			str = "K2G";
			break;
		default:
			str = "";
		}
		return str;
	}
	
	
}
