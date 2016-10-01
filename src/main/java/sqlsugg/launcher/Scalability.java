//package sqlsugg.launcher;
//
//
//import java.util.List;
//
//
//import sqlsugg.config.Config;
//import sqlsugg.keywordIndex.KeywordMatcher;
//import sqlsugg.ranking.*;
//import sqlsugg.backends.*;
//import sqlsugg.ranking.SQLIContain;
//import sqlsugg.template.Template;
//import sqlsugg.template.TemplateGenerator;
//import sqlsugg.template.TemplateIndex;
//import sqlsugg.template.TemplateMatcher;
//import sqlsugg.util.schemaGraph.SchemaGraph;
//import sqlsugg.util.tokenizer.Tokenizer;
//
//public class Scalability {
//	public static void main (String args[]) {
//		try {
//			int k = 5;
//			if (args.length != 2) {
//				System.out.println ("args: domain size");
//				return;
//			}
//			Config config = new Config (args[0]);
//			int size = Integer.valueOf(args[1]);
//			String schemaFile = config.domain.schemaFile;
//			String dbName = config.domain.dbName;
//			System.out.println ("Database: " + dbName);
//			TemplateGenerator tptGen = new TemplateGenerator();
//			SchemaGraph sg = new SchemaGraph();
//			sg.buildFromFile(schemaFile);
//			SQLBackend sql = new SQLBackend();
//			sql.connectMySQL(Config.dbUser, Config.dbPass, dbName);
//			sg.loadWeights(sql);
//			TemplateIndex tptIndex = tptGen.generate(sg, size);
//			KeywordMatcher kMatcher = new KeywordMatcher (sql);
//			TemplateMatcher tptMatcher = new TemplateMatcher();
//			AbstractRankAlgo ra = new WSCAlgo (dbName, sg,sql);
//			Tokenizer tokenizer = new Tokenizer ("etc/stopwords.txt");
//			System.out.println("Please input the query keywords: ");
//			
//			for (int num = 3; num <= 5; num ++) {
//				int sample = 10;
//				double time = 0;
//				for (int count = 0; count < sample; count ++) {
//					String st = "";
//					for (int i = 0; i < num; i ++) {
//						st += (String) keywords.getRandomKeyword() + " ";
//					}
//					long start = System.currentTimeMillis();
//					List<String> tokens = tokenizer.tokenize(st);
//					List<Template> templates = tptMatcher.matchTemplates(tokens, tptIndex, kMatcher);
//					StringBuffer buffer = new StringBuffer();
//					for (Template template: templates) {
//						buffer.append("Template #" + (count + 1 ) + ": " + template + "\n");
//						List<SQLIContain> results = ra.suggest(template, tokens, k);
//						for (int i = 0; i < results.size();i ++) {
//							buffer.append(results.get(i).sqlStr);
//							buffer.append("\n");
//						}
//						buffer.append("\n");
//					}
//					long end = System.currentTimeMillis();
//					time += (end - start);
//				}
//				time /= sample;
//				System.out.println (num + " : " + time);
//				sql.disconnectMySQL();
//			}
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}
