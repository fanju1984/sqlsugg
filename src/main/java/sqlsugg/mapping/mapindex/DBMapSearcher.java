package sqlsugg.mapping.mapindex;

import java.util.*;
import java.sql.*;

import sqlsugg.mapping.*;
import sqlsugg.mapping.maps.*;
import sqlsugg.util.FuncType;
import sqlsugg.backends.*;

public class DBMapSearcher extends MapSearcher{

	SQLBackend sql;
	
	public DBMapSearcher (SQLBackend pSql) {
		sql = pSql;
	}
	public List <KeywordMap> searchMaps(String keyword, MapType mapType)
			throws Exception {
		List<KeywordMap> maps = new LinkedList<KeywordMap> ();
		String tableName = mapType.getDBTableName();
		String stat = "SELECT * FROM " + tableName + " WHERE keyword='" + keyword + "'"; 
		// Issue a query to retrieve mappings satisfying the keyword.
		ResultSet rs = sql.executeQuery(stat);
		while (rs.next()) {
			// We convert each tuple into a mapping record.
			int mid = rs.getInt("mid");
			String rname = mapType.getRelationStr();
			String pRStr = rs.getString(rname);
			String aname = mapType.getAttributeStr();
			String pAStr = null;
			if (aname != null) {
				pAStr = rs.getString(aname);
			}
			double score = rs.getDouble("score");
			KeywordMap map = mapType.insMap(mid, keyword, pRStr, pAStr);
			if (mapType == MapType.K2F) {
				K2FMap k2fMap = (K2FMap) map;
				k2fMap.funcType = FuncType.fuzzyParse(keyword, sql);
			}
			map.setScore(score);
			maps.add(map);
		}
		rs.close();
		return maps;
	}
	
}
