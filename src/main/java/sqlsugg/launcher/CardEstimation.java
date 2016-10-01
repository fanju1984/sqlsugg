package sqlsugg.launcher;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import sqlsugg.backends.SQLBackend;
import sqlsugg.config.Config;
import sqlsugg.display.Translator;
import sqlsugg.display.Visualizer;
import sqlsugg.mapping.MapSearcher;
import sqlsugg.mapping.NumK2VMapSearcher;
import sqlsugg.mapping.mapindex.DBMapSearcher;
import sqlsugg.scoring.Scorer;
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
import sqlsugg.selest.*;

public class CardEstimation {
	public static void main(String args[]) {
		try {
			int k = 10;
			Config config = new Config("dblp");
			int size = Integer.valueOf(3);
			String schemaFile = config.domain.schemaFile;
			String dbName = config.domain.dbName;
			String keywords = "data wang";

			TemplateGenerator tptGen = new TemplateGenerator();
			SQLBackend sql = new SQLBackend();
			sql.connectMySQL(Config.dbHost, Config.dbUser, 
					Config.dbPass, dbName);
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

			int hashNum = 1000;
			
			SelEster selEster = new SelEster(sql, hashNum, 1000, sg);
			//selEster.constructHistograms(sg);
			//System.out.println ("2D-Histogram constructed! ");

			InputStream is = System.in;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			System.out.println("Please input the query keywords: ");
			keywords = br.readLine();

			while (!keywords.equals("exit")) {

				long start = System.currentTimeMillis();
				List<String> tokens = tokenizer.tokenize(keywords);
				System.out.println(tokens);

				SortedList<String, Template> templates = tptSearcher
						.searchTemplates(tokens, k);

				System.out.println("Found templates:  " + templates);
				int count = 0;
				XMLWrapper wrapper = new XMLWrapper();
				templates.initIterator();
				while (templates.hasNext()) {
					Template template = templates.next().getItem();
					// System.out.println ("Template " + count + " : " +
					// template);
					SortedList<String, SQLStruct> results = ra.generate(
							template, tokens, k);
					wrapper.startGroup(template.desc);
					results.initIterator();
					while (results.hasNext()) {
						SQLStruct sqlStruct = results.next().getItem();
						String sqlStat = translator.translateSQL(sqlStruct);

						String sqlImp = translator.translateSQLImp(sqlStruct);

						System.out.println(sqlStat);
						System.out.println(sqlImp);
						int card = selEster.estimate(sqlStruct);

						System.out.println("estimated cardinality: " + card);

						String sqlGraphics = Visualizer
								.visualize(sqlStruct, sg);
						wrapper.addSQL(sqlStat, sqlImp, sqlGraphics);
						break;
					}
					results.destroyIterator();
					wrapper.endGroup();
					count++;
					break;
				}
				templates.destroyIterator();
				long end = System.currentTimeMillis();
				wrapper.setHeader(end - start, keywords);
				wrapper.finalize();
				System.out.println("Please input the query keywords: ");
				keywords = br.readLine();
			}
			// System.out.println(wrapper.getXML());
			sql.disconnectMySQL();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
