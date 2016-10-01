import src.resultManager.*;

class src.query.QueryFacade
{
	var listener:Object;
	function QueryFacade ()
	{
		var thisObj = this;
		listener = new Object ();
		listener.run = function (keywords:String)
		{
			thisObj . query (keywords, 1);
		}
		
		
	}
	
	public function get_listener ():Object
	{
		return this.listener;
	}
	
	public function query (keywords:String, db:Number)
	{
		var query_xml:XML = new XML ();
		query_xml.ignoreWhite = true;
		System.useCodepage = true;
		query_xml.onLoad = function (success:Boolean)
		{
			
			var result_data:ResultData = ResultData.get_instance();
			if (success) {
				
				var carrier:Object = result_data. fetch_results (query_xml);
				if (carrier["sql_array"].length > 0) {
					var display = ResultDisplay.get_instance ();
					display.set_keywords (keywords);
					display.clean_previous_results ();
					display.display_results (carrier["sql_array"], carrier["imp_array"], 
																	   carrier["node_array"], carrier["edge_array"], 
																	   carrier["suggest_time"], carrier["keywords"]);
					result_data.set_ids (carrier["result_ids"]);
				} else {
					trace ("empty result!");
				}
				
				
				/*if (carrier["message"] == "successful") {
					display.set_concept_name (carrier["concept"]);
					display.set_statistics (carrier["begin"], carrier["end"], 
								carrier["total"], carrier["queryTime"]);
					display.set_categories (carrier["category_array"]);
					display.set_red_label (red_label);
					display. display_results(carrier["results_array"]);
					var result_ids:Array = carrier["result_ids"];
					result_data.set_ids (result_ids);
					message_poster.cancel_message ();
					display.hide_text();
					display.show_buttons ();
					tab_manager . select_tab (Configuration.RESULTTAB);
				}
				else if (carrier["message"] == "Result Not Found") {
					display.set_concept_name (carrier["concept"]);
					display.hide_statistics ();
					display.hide_categories ();
					display.hide_scroll_mc ();
					display.hide_buttons ();
					message_poster.cancel_message ();
					display.show_text ("No Result statisfies your query." + newline + newline+
									   "Suggestions：" + newline + 
									   "·Please Check your input" + newline +
									   "·Please Read the semantic meanning carefully" + newline +
									   "·Please Reduce the number of query conditions" + newline);
					tab_manager . select_tab (Configuration.RESULTTAB);
				}
				else {
					message_poster.post_message(carrier["message"]);
				}*/
				
			}
			else {
				//result_data.set_script ("");
				trace ("Connection Error!");
			}
		}
		
		if (keywords.length > 0) {
			var c = new LoadVars ();
			c.keywords = keywords;
			//c.cmd = "suggest";
			var database:String = "";
			if (db == 1) {
				database = "dblp";
			} else if (db == 2){
				database = "dblife";
			}
			c.domain = database;
			c.sendAndLoad("/sqlsugg", query_xml, "GET");
			//c.sendAndLoad("http://166.111.68.42:8000/servlet/sqlsugg", query_xml, "GET");
			/*if (db == 1) {
				c.sendAndLoad("/ssk/dblp", query_xml, "GET");
			} else if (db == 2) {
				c.sendAndLoad ("/ssk/imdb", query_xml, "GET");
			}*/
			//query_xml.load("sample.xml");
			
			var result_data = ResultData . get_instance ();
			
		}
	}
}