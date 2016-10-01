package sqlsugg.launcher;

import sqlsugg.config.Config;
import sqlsugg.mapping.*;
import sqlsugg.mapping.mapfactory.*;
import sqlsugg.mapping.mapindex.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.backends.*;

public class KeywordIndexer {
	public static void main (String args []) {
		try {
			if (args.length < 2) {
				System.out.println("Usage: KeywordIndexer domainName -Param1 -Param2 mapType1 mapType2 ...");
				System.out.println("\tParams: -i: incremental");
				return;
			}
			Config config = new Config(args[0]);
			String schemaFile = config.domain.schemaFile;
			String dbName = config.domain.dbName;
			SchemaGraph sg = new SchemaGraph();
			sg.buildFromFile(schemaFile);
			boolean inc = false;
			int offset = 1;
			for (int i = offset; i < args.length; i ++) {
				String str = args[i];
				if (str.contains("-i")) {
					inc = true;
					offset ++;
				} else {
					break;
				}
			}
			for (int i = offset; i < args.length; i ++) {
				MapType mapType = MapType.parse(args[i]);
				if (mapType != null) {
					indexKeywordMap (mapType, dbName, sg, inc);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static SQLBackend initSQL(String dbName) throws Exception{
		SQLBackend sql = new SQLBackend ();
		sql.connectMySQL(Config.dbUser, Config.dbPass, dbName);
		sql.useDB(dbName);
		return sql;
	}
	
	public static void indexKeywordMap (MapType mapType, 
			String dbName, SchemaGraph sg, boolean inc) throws Exception {
		System.out.println("\n\nNow Indexing the mappings with the type " + 
				mapType.toString() + " INC(" + inc + ")"+ "\n\n" ) ;
		SQLBackend sql1 = initSQL (dbName);
		SQLBackend sql2 = initSQL (dbName);
		SQLBackend sql3 = initSQL (dbName);
		
		MapFactory mapFactory = mapType.getMapFactory(sg, sql1, sql3);
		DBMapIndexer indexer = new DBMapIndexer (sql2, mapFactory);
		
		indexer.index(inc);
		
		sql1.disconnectMySQL();
		sql2.disconnectMySQL();
		sql3.disconnectMySQL();
		
		System.out.println("Finish indexing!");
	}
}
