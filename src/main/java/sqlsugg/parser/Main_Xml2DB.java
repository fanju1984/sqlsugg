package sqlsugg.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import sqlsugg.backends.DBDump;
import sqlsugg.backends.SQLBackend;
import sqlsugg.config.Config;
import sqlsugg.parser.xml.XmlParser;
import sqlsugg.parser.xml.XmlSplitter;

public class Main_Xml2DB {
	public static void main(String[] args) throws Exception {
		String dir = (args.length == 1) ? args[0] : "";
		// String dir = "G:\\exp\\dblpparser-exp\\";
		//
		// Split the entire XML file into smaller ones
		//
		//XmlSplitter splitter = new XmlSplitter();
//		int n_segments = 0;
//		try {
//			n_segments = splitter.split(dir + "dblp.xml", dir, 100000);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		//
		// setup database connection
		// and create a table
		//
		SQLBackend sql = new SQLBackend();
		sql.connectMySQL(Config.dbHost, "sqlsugg", 
				"sqlsugg", "sqlsugg_dblp_server");

		sql.execute("DROP TABLE IF EXISTS paper");
		sql.execute("DROP TABLE IF EXISTS author");
		sql.execute("DROP TABLE IF EXISTS pa");

		sql.execute("CREATE TABLE paper ( " + "id INT NOT NULL PRIMARY KEY, "
				+ "dblpid TEXT, " + "title TEXT, " + "booktitle TEXT, "
				+ "year INT, " + "volumn VARCHAR(50), "
				+ "number VARCHAR(50), " + "pages VARCHAR(50), " + "url TEXT "
				+ ")");

		sql.execute("CREATE TABLE author (" + "id int NOT NULL PRIMARY KEY, "
				+ "name TEXT )");

		sql.execute("CREATE TABLE pa (" + "id int NOT NULL PRIMARY KEY, "
				+ "pid INT, " + "aid INT )");

		Map<String, Integer> authorMap = new HashMap<String, Integer>();
		int papermaxid = 10000000;
		int authormaxid = 20000000;
		int pamaxid = 30000000;

		DBDump paperDump = new DBDump(sql);
		DBDump authorDump = new DBDump(sql);
		DBDump paDump = new DBDump(sql);
		paperDump.initDump("INSERT INTO paper VALUES ", 500);
		authorDump.initDump("INSERT INTO author VALUES ", 500);
		paDump.initDump("INSERT INTO pa VALUES ", 500);

		int n_segments = 32;
		for (int i = 0; i < n_segments; i++) {
			String xmlFile = dir + i + ".xml";
			System.out.print("Parsing " + xmlFile + " ... ");
			XmlParser parser = new XmlParser();
			ArrayList<Record> records = new ArrayList<Record>();
			parser.parse(xmlFile, records);
			System.out.print("done.");
			//
			// batch insert
			//
			System.out.print(" Updating database ... ");
			int n = 0;

			for (int j = 0; j < records.size(); j++) {
				Record r = records.get(j);
				int paperid = papermaxid;
				papermaxid++;
				paperDump
						.addTuple("("
								+ paperid
								+ ",\""
								+ encode(r.dblpid)
								+ "\""
								+ ",\""
								+ encode(r.title)
								+ "\""
								+ ",\""
								+ encode(r.booktitle)
								+ "\""
								+ ","
								+ (r.year.equals("") ? "NULL" : Integer
										.valueOf(r.year)) + ",\""
								+ encode(r.volumn) + "\"" + ",\""
								+ encode(r.number) + "\"" + ",\""
								+ encode(r.pages) + "\"" + ",\""
								+ encode(r.url) + "\"" + ")");
				for (int k = 0; k < r.authors.size(); k++) {
					String author = r.authors.get(k);
					Integer authorid = authorMap.get(author);
					if (authorid == null) {
						authorid = authormaxid;
						authorMap.put(author, authorid);
						authormaxid++;
						authorDump.addTuple("(" + authorid + ",\"" + encode(author)
								+ "\")");
					}
					int paid = pamaxid;
					pamaxid ++;
					paDump.addTuple("(" + paid + "," + paperid + "," + authorid + ")");
				}
			}

			System.out.println("done. " + n + " tuples inserted.");
		}
		paperDump.finishDump();
		authorDump.finishDump();
		paDump.finishDump();
		sql.disconnectMySQL();
		System.out.println("Done.");
	}

	private static String encode(String str) {
		String estr = "";
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			estr += (c == '\"' || c == '\\') ? ("\\" + c) : c;
		}
		return estr;
	}
}