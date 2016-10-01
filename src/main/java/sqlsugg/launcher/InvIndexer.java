package sqlsugg.launcher;

import sqlsugg.backends.SQLBackend;
import sqlsugg.config.Config;
import sqlsugg.mapping.mapinv.K2VMapInv;
import sqlsugg.util.schemaGraph.SchemaGraph;


public class InvIndexer {
	public static void main (String args[]) throws Exception {
		Config config = new Config(args[0]);
		String schemaFile = config.domain.schemaFile;
		String dbName = config.domain.dbName;
		SchemaGraph sg = new SchemaGraph();
		sg.buildFromFile(schemaFile);
		SQLBackend sql = new SQLBackend ();
		sql.connectMySQL(Config.dbHost, Config.dbUser, 
				Config.dbPass, dbName);
		K2VMapInv inv = new K2VMapInv (sg, sql);
		inv.index();
		sql.disconnectMySQL();
		System.out.println("Done.");
	}
}
