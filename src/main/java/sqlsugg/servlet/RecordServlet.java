package sqlsugg.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import sqlsugg.backends.*;
import sqlsugg.config.Config;
import java.util.*;


public class RecordServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	SQLBackend dblpSQL = new SQLBackend ("localhost", "sqlsugg", "sqlsugg", "sqlsugg_dblp_server");
	//SQLBackend dblifeSQL = new SQLBackend ("localhost", "sqlsugg", "sqlsugg", "dblife_clean");
	
	public RecordServlet () {}
	
	public void init(ServletConfig config) throws ServletException {
		try {
			System.out.println("record service init");
			String dblpDBName = "sqlsugg_dblp_server";
			String dblifeDBName = "dblife_clean";
			dblpSQL.connectMySQL("localhost", Config.dbUser, Config.dbPass, dblpDBName);
			//dblifeSQL.connectMySQL("localhost", Config.dbUser, Config.dbPass, dblifeDBName);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void destroy() {
		try {
			System.out.println("record service destroy");
			dblpSQL.disconnectMySQL();
			//dblifeSQL.disconnectMySQL();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doService(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doService(request, response);
	}

	private void doService(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String sqlImp = request.getParameter("sql");
		String domain = request.getParameter("domain");
		if (sqlImp != null && sqlImp.length() > 0 && 
				domain != null && domain.length() > 0) {
			System.out.println("RecordRetrieval: " + domain + "|||" + sqlImp);
			SQLBackend sql = null;
			try {
				if (domain.equals("dblp")) {
					sql = dblpSQL;
				} else {
					//sql = dblifeSQL;
				}
				ResultSet rs = sql.executeQuery(sqlImp);
				String resultXML = xmlWrap (rs);
				rs.close();
				PrintWriter out = new PrintWriter(response.getOutputStream());
				out.println(resultXML);
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
	
	String xmlWrap (ResultSet rs) throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<datatable>\n");
		buffer.append(" <attributes>\n");
		ResultSetMetaData rsmd = rs.getMetaData();
		int columNum = rsmd.getColumnCount();
		HashMap<String, Integer> relationOcc = new HashMap<String, Integer> ();
		HashMap<String, Integer> a2iMap = new HashMap<String, Integer> ();
		for (int i = 0; i < columNum; i ++) {
			String relation = rsmd.getTableName(i + 1);
			String attribute = rsmd.getColumnName(i + 1);
			if (attribute.contains("(") ||
					(!attribute.contains("id") && !attribute.contains("word"))) {
				Integer occ = relationOcc.get(relation + "." + attribute);
				if (occ == null) {
					occ = -1;
				}
				occ ++;
				relationOcc.put(relation + "." + attribute, occ);
				a2iMap.put(relation + occ + "." + attribute, i + 1);
				buffer.append("  <attribute name='" + relation  + occ + "." + attribute + "'/>");
			}
		}
		buffer.append(" </attributes>\n");
		buffer.append(" <results>\n");
		while (rs.next()) {
			buffer.append("   <result ");
			for (String attribute: a2iMap.keySet()) {
				int index = a2iMap.get(attribute);
				buffer.append(attribute + "='" + rs.getString(index) + "' ");
			}
			buffer.append("/>\n");
		}
		buffer.append(" </results>\n");
		
		buffer.append("</datatable>\n");
		return buffer.toString();
	}
}
