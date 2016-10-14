import src.resultManager.*;

class src.resultManager.ResultData 
{
	private var total_count:Number ;
	private var current_keywords:String;
	private var m_result_ids:Array;
	private static var manager:ResultData = undefined;
	
	// the construction 
	function ResultData (){}
	
	public static function get_instance ():ResultData
	{
		if (manager == undefined) {
			manager = new ResultData ();
		}
		return manager;
	}

	// the responsibility
	//1. query using the script
	public function fetch_results (result_xml:XML) :Object
	{
		var carrier:Object = parse_brief_xml (result_xml);
		this. total_count = carrier["total"];
		return  carrier;
	}

	
	
	private function parse_brief_xml (result_xml:XML):Object 
	{
		
		var carrier:Object = new Object ();
		var sql_array:Array = new Array();//the array of returned sql statements
		var imp_array:Array = new Array();
		
		var node_array:Array = new Array();
		var edge_array:Array = new Array();
		
		var result_ids:Array = new Array();//we assign a id for each returned sql statement
		var len = 0;
		var sql_results_xml:Object = result_xml.firstChild;
		var suggest_time:Number = sql_results_xml.attributes.time;
		var keywords:String = sql_results_xml.attributes.keywords;
		var group_xml_array:Array = sql_results_xml.childNodes;
		var i:Number = 0;
		for (i = 0; i < group_xml_array.length; i ++) {
			result_ids.push(i);
			var group_xml:Object = group_xml_array[i];
			var group_desc:String = group_xml.attributes.desc;
			var sql_xml_array:Array = group_xml.childNodes;
			var j:Number = 0;
			for (j = 0; j < sql_xml_array.length; j ++) {
				var sql_xml:Object = sql_xml_array[j];
				var sql_elements_xml:Array = sql_xml.childNodes;
				var sql_stat:String = sql_elements_xml[0].attributes.value;
				var sql_imp:String = sql_elements_xml[1].attributes.value;
				var sql_wrapper:Object = new Object ();
				sql_wrapper.sql = sql_stat;
				sql_wrapper.group_id = i;
				sql_wrapper.group_desc = group_desc;
				sql_array.push(sql_wrapper);
				imp_array.push(sql_imp);
				var nodes:Array = new Array();
				var edges:Array = new Array();
				var sql_graphic_xml:Object = sql_elements_xml[2];
				var nodes_xml:Array = sql_graphic_xml.childNodes[0].childNodes
				var edges_xml:Array = sql_graphic_xml.childNodes[1].childNodes
				var k:Number = 0;
				for (k = 0; k < nodes_xml.length; k ++) {
					var node:Object = new Object();
					node.name = nodes_xml[k].attributes.name;
					node.predicates = new Array();
					var predicate_xml:Array = nodes_xml[k].childNodes;
					var m:Number = 0;
					for (; m < predicate_xml.length; m ++) {
						node.predicates.push(predicate_xml[m].attributes.cdt);
					}
					nodes.push(node);
				}
				for (k = 0; k < edges_xml.length; k ++) {
					edges.push(edges_xml[k].attributes.name);
				}
				node_array.push(nodes);
				edge_array.push(edges);
			}
		}
		
		carrier["sql_array"] = sql_array;
		carrier["imp_array"] = imp_array;
		carrier["node_array"] = node_array;
		carrier["edge_array"] = edge_array;
		
		carrier["result_ids"] = result_ids;
		carrier["total"] = sql_array.length;
		carrier["suggest_time"] = suggest_time;
		carrier["keywords"] = keywords;
		return carrier;
	}
	
	private function parse_data_table (dt_xml:XML):Object {
		var carrier:Object = new Object;
		var attribute_array:Array = new Array();
		var result_array:Array = new Array();
		
		var attribute_xml = dt_xml.firstChild.childNodes[0];
		var result_xml = dt_xml.firstChild.childNodes[1];
		
		var i:Number;
		
		//step1
		var attribute_xml_array:Array = attribute_xml.childNodes;
		for (i = 0; i < attribute_xml_array.length; i ++) {
			var attribute_name:String = attribute_xml_array[i].attributes.name;
			attribute_array.push(attribute_name);
		}
		trace (attribute_array);
		//step2
		var result_xml_array:Array = result_xml.childNodes;
		for (i = 0; i < result_xml_array.length; i ++) {
			var obj:Object = new Object();
			var j:Number = 0;
			for (j = 0; j < attribute_array.length; j ++) {
				obj[attribute_array[j]] = result_xml_array[i].attributes[attribute_array[j]];
			}
			result_array.push(obj);
		}
		carrier["attribute_array"] = attribute_array;
		carrier["result_array"] = result_array;
		return carrier;
	}
	
	//4. priview the details
	public function preview_details (stat:String, db:Number, ypos:Number, sql_start:Number):Void
	{
		var thisObj = this;
		var preview_xml:XML = new XML ();
		System.useCodepage = true;
		preview_xml.ignoreWhite = true;
		preview_xml.onLoad = function (success:Boolean)
		{
			if (success) {
				var display = ResultDisplay.get_instance ();
				var carrier:Object = thisObj .  parse_data_table(preview_xml);
				display . display_detail (carrier["attribute_array"], carrier["result_array"], stat, ypos);
			}
			else {
				////////////////////////////////////
			}
		}
		
		if (sql_start >= 0) {
			var stat1:String = stat + " LIMIT " + sql_start + ",10";
			var c = new LoadVars ();
			c.sql = stat1;
			var database:String = "";
			if (db == 1) {
				database = "dblp";
			} else if (db == 2){
				database = "dblife";
			}
			c.domain = database;
			c.sendAndLoad("/recordsugg", preview_xml, "GET");
			//preview_xml.load("sample_table.xml");
		}
		
		
	}
	
	// 6. some set & get functions
	public function set_keywords (keywords:String)
	{
		this.current_keywords = keywords;
	}
	
	public function set_ids (result_ids:Array)
	{
		this.m_result_ids = result_ids;
	}
}