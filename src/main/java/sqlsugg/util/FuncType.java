package sqlsugg.util;

import java.util.*;
import java.sql.*;

import sqlsugg.backends.*;


public enum FuncType {
	MAX (0), 
	MIN (1), 
	AVG (2), 
	SUM (3), 
	COUNT(4);
	
	int func;
	
	FuncType (int f) {
		func = f;
	}
	
	
	public String toString () {
		switch (func) {
		case 0:
			return "MAX";
		case 1:
			return "MIN";
		case 2:
			return "AVG";
		case 3:
			return "SUM";
		case 4: 
			return "COUNT";
		}
		return "";
	}
	
	public static FuncType matchFuncType (int kid) {
		return null;
	}
	
	public static FuncType parse (String str) {
		if (str.equals("MAX")) {
			return FuncType.MAX;
		} else if (str.equals("MIN")) {
			return FuncType.MIN;
		} else if (str.equals("AVG")) {
			return FuncType.AVG;
		} else if (str.equals("SUM")) {
			return FuncType.SUM;
		} else if (str.equals("COUNT")) {
			return FuncType.COUNT;
		} 
		return null;
	}
	
	public static FuncType fuzzyParse (String str, SQLBackend sql) 
		throws Exception {
		String stat = "SELECT function FROM func_thesaurus WHERE keyword = '" + str + "'";
		ResultSet rs = sql.executeQuery(stat);
		String nstr = null;
		if (rs.next()) {
			nstr = rs.getString("function");
		}
		rs.close();
		if (nstr == null) {
			return null;
		} else {
			return parse (nstr);
		}
	}
	
	public static List<FuncType> getAllTypes () {
		List<FuncType> types = new LinkedList<FuncType> ();
		types.add(FuncType.MAX);
		types.add(FuncType.MIN);
		types.add(FuncType.AVG);
		types.add(FuncType.SUM);
		types.add(FuncType.COUNT);
		return types;
	}
	
}
