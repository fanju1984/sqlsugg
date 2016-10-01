package sqlsugg.parser;

import java.sql.ResultSet;
import java.util.*;

import sqlsugg.backends.*;

public class TableSplit {
	public static void main (String args[]) throws Exception {
		String dbName = "sqlsugg_dblp_server";
		String pubTable = "publication";
		String paperTable = "paper";
		String authorTable = "author";
		String paTable = "pa";
		
		int paperBase = 10000000;
		int authorBase = 20000000;
		int paBase = 30000000;
		

		
		SQLBackend sql = new SQLBackend ();
		sql.connectMySQL("166.111.68.40", "sqlsugg", "sqlsugg", dbName);
		
		
		sql.execute("DROP TABLE IF EXISTS " + paperTable);
		sql.execute("CREATE TABLE " + paperTable + "(" + 
				"id int, " + 
				"dblpid text, " + 
				"title text, " + 
				"booktitle text, " + 
				"year int, " + 
				"volumn varchar(50), " + 
				"number varchar(50), " + 
				"pages varchar(50), " + 
				"url text )");
		sql.execute("DROP TABLE IF EXISTS " + authorTable);
		sql.execute("CREATE TABLE " + authorTable + " (" + 
				"id int, " + 
				"name varchar(200) )");
		
		sql.execute("DROP TABLE IF EXISTS " + paTable);
		sql.execute("CREATE TABLE " + paTable + " (" + 
				"id int key auto_increment, " + 
				"pid int, " + 
				"aid int " + 
				")");
		
		
		DBDump paperDump = new DBDump (sql);
		DBDump authorDump = new DBDump (sql);
		DBDump paDump = new DBDump (sql);
		paperDump.initDump("INSERT INTO " + paperTable + " VALUES ", 100);
		authorDump.initDump("INSERT INTO " + authorTable + " VALUES ", 100);
		paDump.initDump("INSERT INTO " + paTable + " VALUES ", 100);
		
		
		ResultSet rs = sql.executeQuery("SELECT * FROM " + pubTable + " LIMIT 100");
		
		int paperInc = 0;
		int authorInc = 0;
		int paInc = 0;
		Map<String, Integer> authorMap = new HashMap<String, Integer> ();
		while (rs.next()) {
			String dblpid = rs.getString("dblpid");
			String title = rs.getString("title");
			String booktitle = rs.getString("booktitle");
			String authors = rs.getString("authors");
			int year = rs.getInt("year");
			String volumn = rs.getString("volumn");
			String number = rs.getString("number");
			String pages = rs.getString("pages");
			String url = rs.getString("url");
			
			int paperid = paperBase + paperInc;
			paperDump.addTuple("(" + paperid + 
					",\"" + dblpid + "\"" + 
					",\"" + title + "\"" +
					",\"" + booktitle + "\"" + 
					"," + year + 
					",\"" + volumn + "\"" +
					",\"" + number + "\"" +
					",\"" + pages + "\"" + 
					",\"" + url + "\"" +  
					")");
			paperInc ++;
			
			String[] authorList = authors.split(";");
			for (String author: authorList) {
				Integer authorid = authorMap.get(author);
				if (authorid == null) {
					authorid = authorBase + authorInc;
					authorInc ++;
					authorMap.put(author, authorid);
					authorDump.addTuple("(" + authorid + ",\""+ author + "\")" );
				}
				int paid = paBase + paInc;
				paInc ++;
				paDump.addTuple("(" + paid + "," + paperid + "," + authorid + ")");
			}
			
		}
		rs.close();
		paperDump.finishDump();
		authorDump.finishDump();
		paDump.finishDump();
		
		sql.disconnectMySQL();
	}
}
