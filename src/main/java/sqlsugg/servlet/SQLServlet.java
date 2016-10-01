package sqlsugg.servlet;


import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;

import sqlsugg.mapping.*;
import sqlsugg.mapping.mapindex.*;
import sqlsugg.scoring.Scorer;
import sqlsugg.sqlgen.*;
import sqlsugg.sqlgen.genAlgos.WSCAlgo;
import sqlsugg.template.*;
import sqlsugg.util.schemaGraph.*;
import sqlsugg.util.tokenizer.Tokenizer;
import sqlsugg.backends.*;
import sqlsugg.config.Config;
import sqlsugg.display.Translator;
import sqlsugg.display.Visualizer;
import sqlsugg.util.basicstruct.*;




public class SQLServlet extends HttpServlet{
	
	SchemaGraph dblpSg;
	SchemaGraph dblifeSg;
	
	SQLBackend dblpSQL = new SQLBackend("localhost", "sqlsugg", "sqlsugg", "sqlsugg_dblp_server");
	//SQLBackend dblifeSQL = new SQLBackend("localhost", "sqlsugg", "sqlsugg", "dblife_clean");
	
	
	TemplateSearcher dblpTptSearcher;
	TemplateSearcher dblifeTptSearcher;
	
	SQLGenerator raDBLP;
	SQLGenerator raDBLife;
	
	Translator dblpTranslator;
	Translator dblifeTranslator;
	
	Tokenizer tokenizer;
	
	int k = 3;

	private static final long serialVersionUID = 1L;

	public SQLServlet () {
		
	}
	
	public void init(ServletConfig config) throws ServletException {
		try {
			System.out.println("init");
			TemplateGenerator tptGen = new TemplateGenerator();
			String dblpDB = "sqlsugg_dblp_server";
			String dblifeDB = "dblife_clean";
			dblpSQL.connectMySQL("localhost", Config.dbUser, Config.dbPass, dblpDB);
			//dblifeSQL.connectMySQL("localhost",  Config.dbUser, Config.dbPass, dblifeDB);
			dblpSg = new SchemaGraph();
			
			File dblpFile = new File ("sqlsugg_config/data/dblp/schema.dat");
			File dblifeFile = new File ("sqlsugg_config/data/dblife/schema.dat");
			
			dblpSg.buildFromFile(dblpFile.getAbsolutePath());
			Scorer dblpScorer = new Scorer (dblpSg, dblpSQL, dblpDB);
			dblpSg.loadWeights(dblpScorer);
			dblifeSg = new SchemaGraph();
			dblifeSg.buildFromFile(dblifeFile.getAbsolutePath());
			//Scorer dblifeScorer = new Scorer (dblifeSg, dblifeSQL, dblifeDB);
			//dblifeSg.loadWeights(dblifeScorer);
			
			
			dblpTranslator = new Translator (dblpSg);
			dblifeTranslator = new Translator (dblifeSg);

			TemplateIndex dblpTptIndex = tptGen.generate(dblpSg, dblpScorer, 5);
			//TemplateIndex dblifeTptIndex = tptGen.generate(dblifeSg, dblifeScorer, 5);
			
			DBMapSearcher dblpTxtSearcher = new DBMapSearcher (dblpSQL);
			NumK2VMapSearcher dblpNumSearcher = new NumK2VMapSearcher (dblpSg, dblpSQL);
			
			//DBMapSearcher dblifeTxtSearcher = new DBMapSearcher (dblifeSQL);
			//NumK2VMapSearcher dblifeNumSearcher = new NumK2VMapSearcher (dblifeSg, dblifeSQL);
			
			//dblpNumSearcher.constructHistograms(100);
			//dblifeNumSearcher.constructHistograms(100);
			
			
			dblpTptSearcher = new TemplateSearcher (
					dblpTxtSearcher, dblpTptIndex, dblpScorer);
			
//			dblifeTptSearcher = new TemplateSearcher (
//					dblifeTxtSearcher, dblifeTptIndex, dblpScorer);

			raDBLP = new WSCAlgo(dblpTxtSearcher, dblpNumSearcher, 
					dblpScorer, dblpSg);
			
//			raDBLife = new WSCAlgo(dblifeTxtSearcher, dblifeNumSearcher, 
//					dblifeScorer, dblifeSg);


			File swFile = new File ("sqlsugg_config/etc/stopwords.txt"); 
			tokenizer = new Tokenizer(
					swFile.getAbsolutePath());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public void destroy() {
		try {
			System.out.println("destroy");
			dblpSQL.disconnectMySQL();
			//dblifeSQL.disconnectMySQL();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		doService (request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		doService (request, response);
	}

	private void doService(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String keywords = request.getParameter("keywords");
		String domain = request.getParameter("domain");
		if (keywords != null && keywords.length() > 0 && 
				domain != null && domain.length() > 0) {
			System.out.println("SQLSUGG Query: " + domain + "|||" + keywords);
			TemplateSearcher tptSearcher = null;
			SQLGenerator ra = null;
			Translator translator = null;
			SchemaGraph sg = null;
			if (domain.equals("dblp")) {
				tptSearcher = dblpTptSearcher;
				translator = dblpTranslator;
				ra = raDBLP;
				sg = dblpSg;
			} else {
//				tptSearcher = dblifeTptSearcher;
//				ra = raDBLife;
//				translator = dblifeTranslator;
			}
			
			try {
				long start = System.currentTimeMillis();
				List<String> tokens = tokenizer.tokenize(keywords);
				SortedList<String, Template> templates = tptSearcher.searchTemplates(tokens, 10);
				int count = 0;
				XMLWrapper wrapper = new XMLWrapper();
				templates.initIterator();
				while (templates.hasNext()) {
					Template template = templates.next().getItem();
					wrapper.startGroup(template.desc);
					SortedList<String, SQLStruct> results = ra.generate(template, tokens, k);
					results.initIterator();
					while (results.hasNext()) {
						SQLStruct sqlStruct = results.next().getItem();
						String sql = translator.translateSQL(sqlStruct);
						String sqlImp = translator.translateSQLImp(sqlStruct);
						String sqlGraphics = Visualizer.visualize(sqlStruct, sg);
						wrapper.addSQL(sql, sqlImp, sqlGraphics);
					}
					results.destroyIterator();
					wrapper.endGroup();
					count++;
				}
				templates.destroyIterator();
				long end = System.currentTimeMillis();
				wrapper.setHeader(end - start, keywords	);
				wrapper.finalize();
				//response.setContentType("text/html");
				PrintWriter out = new PrintWriter(response.getOutputStream());
				out.println(wrapper.getXML());
				out.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			response.setContentType("text/html");
			PrintWriter out = new PrintWriter(response.getOutputStream());
			out.println("<html>");
			out.println("</html>");
		}
	}
}
