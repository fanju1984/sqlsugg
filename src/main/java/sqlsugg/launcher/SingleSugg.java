package sqlsugg.launcher;

import java.util.*;

import sqlsugg.backends.*;
import sqlsugg.config.Config;
import sqlsugg.scoring.Scorer;
import sqlsugg.servlet.*;
import sqlsugg.template.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.tokenizer.Tokenizer;
import sqlsugg.sqlgen.*;
import sqlsugg.sqlgen.genAlgos.*;
import sqlsugg.mapping.*;
import sqlsugg.mapping.mapindex.*;
import sqlsugg.util.basicstruct.*;
import sqlsugg.display.*;

public class SingleSugg {
	public static void main(String args[]) {
		try {
			if (args.length != 3) {
				System.out.println("args: domain size keywords");
				return;
			}
			int k = 10;
			Config config = new Config (args[0]);
			int size = Integer.valueOf(args[1]);
			String schemaFile = config.domain.schemaFile;
			String dbName = config.domain.dbName;
			String keywords = args[2];
			
			TemplateGenerator tptGen = new TemplateGenerator();
			SQLBackend sql = new SQLBackend();
			sql.connectMySQL(Config.dbHost, Config.dbUser, Config.dbPass, dbName);
			SchemaGraph sg = new SchemaGraph();
			sg.buildFromFile(schemaFile);
			Scorer scorer = new Scorer (sg, sql, dbName);
			sg.loadWeights(scorer);
			TemplateIndex tptIndex = tptGen.generate(sg, scorer, size);
			MapSearcher txtSearcher = new DBMapSearcher (sql);
			NumK2VMapSearcher numSearcher = new NumK2VMapSearcher (sg, sql);
			numSearcher.constructHistograms(10);
			
			TemplateSearcher tptSearcher = new TemplateSearcher (
					txtSearcher, tptIndex, scorer);
			
			SQLGenerator ra = new WSCAlgo(txtSearcher, numSearcher, scorer, sg);
			
			Tokenizer tokenizer = new Tokenizer("etc/stopwords.txt");

			Translator translator = new Translator (sg);
			
			long start = System.currentTimeMillis();
			List<String> tokens = tokenizer.tokenize(keywords);
			System.out.println(tokens);

			SortedList<String, Template> templates = tptSearcher.searchTemplates(tokens, k);
			int count = 0;
			XMLWrapper wrapper = new XMLWrapper();
			templates.initIterator();
			while (templates.hasNext()) {
				Template template = templates.next().getItem();
				System.out.println ("Template " + count + " : " + template);
				SortedList<String, SQLStruct> results = 
					ra.generate(template, tokens, k);
				wrapper.startGroup(template.desc);
				results.initIterator();
				while (results.hasNext()) {
					SQLStruct sqlStruct = results.next().getItem();
					String sqlStat = translator.translateSQL(sqlStruct);
					String sqlImp = translator.translateSQLImp(sqlStruct);
					String sqlGraphics = Visualizer.visualize(sqlStruct, sg);
					wrapper.addSQL(sqlStat, sqlImp, sqlGraphics);
				}
				results.destroyIterator();
				wrapper.endGroup();
				count++;
			}
			templates.destroyIterator();
			long end = System.currentTimeMillis();
			wrapper.setHeader(end - start, keywords);
			wrapper.finalize();
			System.out.println(wrapper.getXML());
			sql.disconnectMySQL();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
