package sqlsugg.template;

import java.util.*;

import sqlsugg.mapping.*;
import sqlsugg.mapping.mapindex.DBMapSearcher;
import sqlsugg.util.basicalgo.*;
import sqlsugg.util.basicstruct.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.scoring.*;
import sqlsugg.backends.*;
import sqlsugg.config.Config;


public class TemplateSearcher {
	MapSearcher mapSearcher;
	TemplateIndex tptIndex;
	Scorer scorer;
	
	public TemplateSearcher (MapSearcher pMapSearcher, 
			TemplateIndex pTptIndex, Scorer pScorer) {
		mapSearcher = pMapSearcher;
		tptIndex = pTptIndex;
		scorer = pScorer;
	}
	
	public SortedList<String, Template> searchTemplates (List<String> keywords, 
			int k) throws Exception {
		// Step 1: Scan every keyword, and construct the lists and maps for TA
		List<SortedList<String, Template>> lists = 
			new LinkedList<SortedList<String, Template>> ();
		Map<String, Double> params = new HashMap<String, Double> ();
		List<Double> paramList = new LinkedList<Double> (); 
		Map<SortedList<String, Template>, RanAccIndex<Template,String>> map = 
			new HashMap <SortedList<String, Template>, RanAccIndex<Template,String>> ();
		
		// Step 2: Update the parameters of relations. 
		for (String keyword: keywords) {
			List<KeywordMap> keywordMaps = 
				mapSearcher.searchMaps(keyword, MapType.K2R);
			for (KeywordMap keywordMap : keywordMaps) {
				String rname = keywordMap.getRStr();
				Double param = params.get(rname);
				if (param == null) {
					param = 0.0;
				}
				param += keywordMap.score();
				params.put(rname, param);
			}
		}
		
		// Step 3: Construct the lists and maps. 
		RanAccIndex<Template, String> t2rIndex = tptIndex.getT2rIndex();
		for (String rname: params.keySet()) {
			SortedList<String, Template> r2tList = 
				tptIndex.getR2tList(rname);
				new RanAccIndex<Template, String> ("t2r: " + rname);
			lists.add(r2tList);
			map.put(r2tList, t2rIndex);
			paramList.add(params.get(rname));
		}
		//System.out.println("TMP: " + lists);
		// Step 4: Run the TA algorithm.
		ThresholdAlgo<String, Template> talgo = 
			new ThresholdAlgo<String, Template> (map);
		SortedList<String, Template> templates = talgo.run(lists, paramList, k, false);
		return templates;
		
	}
	
	public static void main (String args[]) {
		try {
			TemplateGenerator tptGen = new TemplateGenerator();
			SchemaGraph sg = new SchemaGraph();
			sg.buildFromFile("data/dblp/schema.dat");
			String dbname = "sqlsugg_dblp";
			SQLBackend sql = new SQLBackend ();
			sql.connectMySQL(Config.dbHost, Config.dbUser, Config.dbPass, dbname);
			Scorer scorer = new Scorer (sg, sql, dbname);
			
			TemplateIndex tptIndex = tptGen.generate(sg, scorer, 5);
			
			
			
			
			MapSearcher mapSearcher = new DBMapSearcher (sql);
			
			
			
			List<String> tokens = new LinkedList<String> ();
			tokens.add("database");
			tokens.add("author");
			
			TemplateSearcher tptSearcher = 
				new TemplateSearcher (mapSearcher, tptIndex, scorer);
			
			long start = System.currentTimeMillis();
			SortedList<String, Template> templates = 
				tptSearcher.searchTemplates(tokens, 10);
			long end = System.currentTimeMillis();
			System.out.println("Elapsed Time: " + (end - start) + " ms");
			System.out.println("Results: " + templates);
			sql.disconnectMySQL();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
