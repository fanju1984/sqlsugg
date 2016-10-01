package sqlsugg.mapping.mapinv;

import java.sql.*;
import java.util.*;

import sqlsugg.backends.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.tokenizer.*;
import sqlsugg.config.*;

/**
 * This class is to construct a "mapping" from a mapping to a list of tuple id.
 * 
 * @author Administrator
 * 
 */
public class K2VMapInv {
	SQLBackend sql;
	SchemaGraph sg;
	
	String tableName;
	
	Map<String, List<Integer>> cache;

	public K2VMapInv(SchemaGraph pSg, SQLBackend pSql) {
		tableName = "inv_index";
		sg = pSg;
		sql = pSql;
	}

	private String getQuery(Relation relation) {
		String ret = "SELECT * ";
		ret += " FROM ";
		ret += relation.getName();
		return ret;
	}

	void addID(Map<String, List<Integer>> cache, String word, int id) {
		List<Integer> idList = cache.get(word);
		if (idList == null) {
			idList = new LinkedList<Integer>();
		}
		idList.add(id);
		cache.put(word, idList);
	}

	public void index() throws Exception {
		sql.execute("DROP TABLE IF EXISTS " + tableName);
		Tokenizer tokenizer = new Tokenizer(Config.stopfile);
		Set<Relation> relations = sg.getRelations();
		for (Relation relation : relations) {
			System.out.println("Processs Relation " + relation.getName());

			List<Attribute> ts = relation.getAttributesByType(DataType.TXT);
			if (ts.size() == 0) {
				continue;
			}
			Map<String, List<Integer>> cache = new HashMap<String, List<Integer>>();
			String stat = getQuery(relation);
			System.out.println ("SQL: " + stat);
			ResultSet rs = sql.executeQuery(stat);
			int progress = 0;
			while (rs.next()) {
				String primaryKey = relation.getKey();
				int primaryValue = rs.getInt(primaryKey);
				for (Attribute a : ts) {
					String value = rs.getString(a.name);
					if (value == null) {
						continue;
					}
					List<String> tokens = tokenizer.tokenize(value);
					Set<String> distinctTokens = new HashSet<String>();
					for (String token : tokens) {
						distinctTokens.add(token);
					}
					for (String token : distinctTokens) {
						String searchKey = token + "_" + relation.getName()
								+ "." + a.name + ".value";
						addID(cache, searchKey, primaryValue);
					}
				}
				progress++;
				if (progress % 10000 == 0) {
					System.out.println("\t Tuple Progress: " + progress);
				}
			}
			rs.close();
			this.insertToDB(cache);
			cache.clear();
		}
		this.createIndex();
	}

	public void createIndex() throws Exception {
		String indexName = tableName + "_index";
		String stat = "CREATE INDEX " + indexName + " ON " + tableName
				+ "(word)";
		sql.execute(stat);
		stat = "CREATE INDEX " + indexName + "1 ON " + tableName + "(rcdid)";
		sql.execute(stat);
	}

	public void insertToDB(Map<String, List<Integer>> cache) throws Exception {
		int batch = 100;
		String stat = "CREATE TABLE IF NOT EXISTS " + tableName
				+ " (word varchar(100)," + "rcdid int)";
		sql.execute(stat);
		String prefix = "INSERT INTO " + tableName;
		List<String> valueBuffer = new LinkedList<String>();
		Set<String> keys = cache.keySet();
		for (String key : keys) {
			List<Integer> idList = cache.get(key);
			for (Integer id : idList) {
				valueBuffer.add("('" + key + "'," + id + ")");
				if (valueBuffer.size() >= batch) {
					executeInsert(sql, prefix, valueBuffer);
					valueBuffer.clear();
				}
			}
		}
		if (valueBuffer.size() > 0) {
			executeInsert(sql, prefix, valueBuffer);
		}
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
	
	public static void main (String args[]) throws Exception {
		Config config = new Config("dblp_server");
		String schemaFile = config.domain.schemaFile;
		String dbName = config.domain.dbName;
		SchemaGraph sg = new SchemaGraph();
		sg.buildFromFile(schemaFile);
		SQLBackend sql = new SQLBackend ();
		sql.connectMySQL(Config.dbHost, "sqlsugg", "sqlsugg", dbName);
		K2VMapInv inv = new K2VMapInv (sg, sql);
		inv.index();
		sql.disconnectMySQL();
		System.out.println("Done.");
	}
}
