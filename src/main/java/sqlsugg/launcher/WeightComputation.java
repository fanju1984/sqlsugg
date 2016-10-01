package sqlsugg.launcher;

import java.util.*;

import sqlsugg.backends.*;
import sqlsugg.config.Config;
import sqlsugg.util.dataGraph.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.scoring.schemascoring.*;


public class WeightComputation {

	public static void main(String args[]) {
		try {
			Config config = new Config(args[0]);
			String schemaFile = config.domain.schemaFile;
			String dbName = config.domain.dbName;
			SQLBackend sql = new SQLBackend();
			sql.connectMySQL(Config.dbUser, Config.dbPass, dbName);
			SchemaGraph sg = new SchemaGraph();
			sg.buildFromFile(schemaFile);
			System.out.println("Calculate the prior weights of tables");
			System.out.println("\tLoad the data graph...");
			DataGraph dg = new DataGraph(sg, sql);
			HashMap<String, Integer> relationMap = new HashMap<String, Integer>();
			dg.load(relationMap);
			System.out.println("\tcalcuate the weights of tables");
			TableWeighter tWeighter = new TableWeighter(dbName, sql);
			tWeighter.computeWeights(relationMap, 0.15, dg);
			System.out.println("Calculate the prior weights of attributes");
			// step3: calculate the prior weights of attributes
			AttributeWeighter aWeighter = new AttributeWeighter(dbName, sql);
			aWeighter.compute(sg);
			
			sql.disconnectMySQL();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
