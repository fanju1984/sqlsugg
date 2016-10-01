package sqlsugg.launcher;

import java.sql.ResultSet;
import java.util.List;
import java.io.*;

import sqlsugg.backends.SQLBackend;
import sqlsugg.config.Config;
import sqlsugg.display.Translator;
import sqlsugg.display.Visualizer;
import sqlsugg.mapping.MapSearcher;
import sqlsugg.mapping.NumK2VMapSearcher;
import sqlsugg.mapping.mapindex.DBMapSearcher;
import sqlsugg.scoring.Scorer;
import sqlsugg.selest.SelEster;
import sqlsugg.servlet.XMLWrapper;
import sqlsugg.sqlgen.SQLGenerator;
import sqlsugg.sqlgen.SQLStruct;
import sqlsugg.sqlgen.genAlgos.WSCAlgo;
import sqlsugg.template.Template;
import sqlsugg.template.TemplateGenerator;
import sqlsugg.template.TemplateIndex;
import sqlsugg.template.TemplateSearcher;
import sqlsugg.util.basicstruct.SortedList;
import sqlsugg.util.schemaGraph.SchemaGraph;
import sqlsugg.util.tokenizer.Tokenizer;

public class MultiSugg {

	public static void main(String args[]) {
		try {
			if (args.length != 2) {
				System.out.println("args: domain size");
				return;
			}
			int k = 10;
			Config config = new Config(args[0]);
			int size = Integer.valueOf(args[1]);
			String schemaFile = config.domain.schemaFile;
			String dbName = config.domain.dbName;

			TemplateGenerator tptGen = new TemplateGenerator();
			SQLBackend sql = new SQLBackend();
			sql.connectMySQL("localhost", Config.dbUser, Config.dbPass, dbName);
			SchemaGraph sg = new SchemaGraph();
			sg.buildFromFile(schemaFile);
			Scorer scorer = new Scorer(sg, sql, dbName);
			sg.loadWeights(scorer);
			TemplateIndex tptIndex = tptGen.generate(sg, scorer, size);
			MapSearcher txtSearcher = new DBMapSearcher(sql);
			NumK2VMapSearcher numSearcher = new NumK2VMapSearcher(sg, sql);
			numSearcher.constructHistograms(10);

			TemplateSearcher tptSearcher = new TemplateSearcher(txtSearcher,
					tptIndex, scorer);

			SQLGenerator ra = new WSCAlgo(txtSearcher, numSearcher, scorer, sg);

			Tokenizer tokenizer = new Tokenizer("etc/stopwords.txt");

			Translator translator = new Translator(sg);
			
			int hashNum = 100;
			int histWidth = 1000;
			
			SelEster selEster = new SelEster(sql, hashNum, histWidth, sg);

			InputStream is = System.in;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			System.out.println("Please input the query keywords: ");
			String keywords = br.readLine();

			while (!keywords.equals("exit")) {

				long start = System.currentTimeMillis();
				List<String> tokens = tokenizer.tokenize(keywords);
				System.out.println(tokens);

				SortedList<String, Template> templates = tptSearcher
						.searchTemplates(tokens, k);
				int count = 0;
				templates.initIterator();
				while (templates.hasNext()) {
					Template template = templates.next().getItem();
					SortedList<String, SQLStruct> results = ra.generate(
							template, tokens, k);
					if (results.size() == 0) {
						continue;
					}
					System.out.println("########## Template " + (count + 1) + ": " + template + 
							" ##########");
					results.initIterator();
					while (results.hasNext()) {
						int card = -1;
						SQLStruct sqlStruct = results.next().getItem();
						String sqlStat = translator.translateSQL(sqlStruct);
						System.out.println ( sqlStat);
						break;
						//card = selEster.estimate(sqlStruct);
//						String sqlImp = translator.translateSQLImp(sqlStruct);
//						
//						sqlImp = sqlImp.replaceAll("SELECT [^\n]+\n", "SELECT COUNT(\\*) as cnt \n");
//						int realCard = -1;
//						ResultSet rs = sql.executeQuery(sqlImp);
//						if (rs.next()) {
//							realCard = rs.getInt("cnt");
//						}
//						rs.close();
						//System.out.println (realCard + " vs " + card + " --> " + sqlStat);
					}
					results.destroyIterator();
					count++;
					System.out.println("##################");
				}
				templates.destroyIterator();
				long end = System.currentTimeMillis();
				System.out.println("Time: " + (end - start));
				System.out.println("Please input the query keywords: ");
				keywords = br.readLine();
			}
			sql.disconnectMySQL();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
