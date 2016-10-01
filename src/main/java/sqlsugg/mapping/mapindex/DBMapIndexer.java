package sqlsugg.mapping.mapindex;

import sqlsugg.backends.*;
import sqlsugg.mapping.mapfactory.*;
import sqlsugg.mapping.*;

import java.sql.ResultSet;
import java.util.*;

public class DBMapIndexer {
	SQLBackend sql;
	MapFactory mapFactory;
	MapType mapType;
	
	
	Map<String, Double> cache; 
	// the in-memory cache, which records the temporal scores
	
	public DBMapIndexer (SQLBackend pSql) {
		sql = pSql;
	}
	
	
	public DBMapIndexer (SQLBackend pSql, MapFactory pMapFactory) {
		sql = pSql;
		mapFactory = pMapFactory;
		mapType = pMapFactory.mapType;
		cache = new HashMap<String, Double> ();
	}
	
	
	private void createIndexTable (String tableName, boolean incre) throws Exception {
		if (!incre) {
			sql.execute("DROP TABLE IF EXISTS " + tableName);
		}
		sql.execute(mapType.getCreateTableStat());
	}
	
	private void updateScore (Map<String, Double> cache, String searchKey, 
			double score) throws Exception {
		Double freq = cache.get(searchKey);
		if (freq == null) {
			freq = 0.0;
		}
		freq += score;
		cache.put(searchKey, freq);
	}
	
	void updateScores (String tableName, String keyword, double sumScore) throws Exception {
		String stat = "UPDATE " + tableName + " SET score=score/" + sumScore 
			+ " WHERE keyword = '" + keyword + "'";
		sql.execute(stat);
	}
	
	private String getInsertValues (String searchKey, String tableName, double score) {
		String str = "(";
		String parts[] = searchKey.split("_");
		if (parts.length == 1) {
			str += "\"" + parts[0] +  "\",";
			str += "\"" + tableName + "\",";
		} else if (parts.length == 2){
			//System.out.println(searchKey + "!!!");
			str += "\"" + parts[0] +  "\",";
			str += "\"" + tableName + "\",";
			str += "\"" + parts[1] +  "\",";
		} else {
			//System.out.println(searchKey + "!!!");
			return null;
		}
		str += score;
		str += ")";
		return str;
	}
	
	public void executeInsert(SQLBackend sql, String prefix,
			List<String> valueBuffer) throws Exception {
		String stat = prefix;
		stat += " VALUES ";
		for (int i = 0; i < valueBuffer.size(); i++) {
			stat += valueBuffer.get(i);
			if (i < valueBuffer.size() - 1) {
				stat += ",";
			}
		}
		sql.execute(stat);
	}
	/**
	 * This class is to materalize the cache into the relational database.
	 * @param cache
	 * @param tableName
	 * @throws Exception
	 */
	private void insertToDB (Map<String, Double> cache, 
			String tableName, String relation) throws Exception {
		int batch = 100; // the batch of insertion is 100;
		String attributeList = mapType.getAttributeList();
		String prefix = "INSERT INTO " + tableName + " " + attributeList + " ";
		List<String> valueBuffer = new LinkedList<String>();
		for (String key: cache.keySet()) {
			double score = cache.get(key);
			String insertStat = this.getInsertValues(key, relation, score);
			if (insertStat != null) {
				valueBuffer.add(insertStat);
			}
			if (valueBuffer.size() >= batch) {
				executeInsert (sql, prefix, valueBuffer);
				valueBuffer.clear();
			}
		}
		if (valueBuffer.size() > 0) {
			executeInsert (sql, prefix, valueBuffer);
		}
	}
	
	public void createDBIndex (String tableName) throws Exception{
		System.out.println("Create Index for the Mapping Table");
		String indexName = tableName + "_index";
		String stat = "CREATE INDEX " + indexName + " ON " + tableName + "(keyword)";
		
		sql.execute(stat);
	}
	
	/**
	 * Normalize the scores of one keyword, in order to 
	 * let the sum of the score to be 1.
	 * @param tableName
	 * @throws Exception
	 */
	void normalizeScores (String tableName) throws Exception{
		System.out.println("Normailize Scores");
		String stat = "SELECT keyword, score FROM " + tableName + " ORDER BY keyword";
		ResultSet rs = sql.executeQuery(stat);
		String curKeyword = null;
		double sumScore = 0;
		while (rs.next()) {
			String keyword = rs.getString("keyword");
			double score = rs.getDouble("score");
			if (curKeyword == null || !keyword.equals(curKeyword)) {
				if (curKeyword != null) {
					updateScores (tableName, curKeyword, sumScore);
				}
				curKeyword = keyword;
				sumScore = 0;
			}
			sumScore += score;
		}
		rs.close();
		if (curKeyword != null) {
			updateScores (tableName, curKeyword, sumScore);
		}
	}
	
	void refine (String tableName) throws Exception {
		System.out.println ("Refine Scores");
		String stat = "DELETE FROM " + tableName + " WHERE attribute='NULL'";
		sql.execute(stat);
	}
	
	/**
	 * The framework of indexing mappings to a relational database.
	 * @throws Exception
	 */
	public void index (boolean inc) throws Exception {
		String tableName = mapType.getDBTableName();
		createIndexTable (tableName, inc);
		mapFactory.initConstruction();
		// construct the mappings iteratively.
		List<KeywordMap> mapBatch = mapFactory.nextMapBatch();
		while (mapBatch != null) {
			if (mapFactory.flush) {
				this.insertToDB(cache, tableName, mapFactory.flushTable);
				cache.clear();
			}
			for (KeywordMap keywordMap: mapBatch) {
				String searchKey = keywordMap.getSearchKey();
				double score = keywordMap.score();
				this.updateScore(cache, searchKey, score);
			}
			mapBatch = mapFactory.nextMapBatch();
		}
		if (cache.size() > 0) {
			this.insertToDB(cache, tableName, mapFactory.flushTable);
			cache.clear();
		}
		mapFactory.finalizeConstruction();
		if (!inc) {
			this.createDBIndex(tableName);
		}
		
		postProcess (tableName);
		
	}
	
	private void postProcess (String tableName) throws Exception {
		if (mapType == MapType.K2V || mapType == MapType.K2R) {
			this.normalizeScores(tableName);
		}
		if (mapType == MapType.K2V) {
			this.refine(tableName);
		}
	}
	
}
