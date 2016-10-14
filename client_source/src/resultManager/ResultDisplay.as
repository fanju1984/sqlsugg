import src.resultManager.*;
import src.schemaGraph.*;
import flash.filters.DropShadowFilter;
import mx.managers.PopUpManager;
import mx.containers.Window;
import mx.transitions.Tween;
import mx.transitions.easing.*;

class src.resultManager.ResultDisplay
{
	var m_parent_mc:MovieClip;
	var m_scroll_mc:MovieClip;
	var m_detail_mc:MovieClip;
	
	var m_brief_panel_array:Array;
	var m_brief_separator_array:Array;
	var m_brief_group_array:Array;
	
	var m_listener :Object;
	
	var m_broadcaster:Object;
	var m_keywords:Array;
	
	var painter:SGPainter;
	
	static var displayer:ResultDisplay;
	
	public var cur_page:Number = 0;
	
	public static function get_instance ():ResultDisplay
	{
		if (displayer == undefined) {
			displayer = new ResultDisplay ();
		}
		return displayer;
	}
	
	function ResultDisplay () 
	{
		var thisObj = this;
		m_brief_panel_array = new Array ();
		m_brief_separator_array = new Array ();
		m_brief_group_array = new Array();
		thisObj . m_broadcaster = new Object ();
		AsBroadcaster.initialize(thisObj . m_broadcaster);
	}
	
	public function init (parent_mc:MovieClip, scroll_mc:MovieClip)
	{
		var thisObj = this;
		thisObj . m_parent_mc = parent_mc;
		thisObj . m_scroll_mc = scroll_mc;
	}
	
	private function remove_movie_clips () 
	{
		var thisObj = this;
		var i:Number = 0;
		for (i = 0; i < thisObj . m_brief_panel_array.length; i ++) {
			thisObj. m_brief_panel_array[i].removeMovieClip();
		}
		for (i = 0; i < thisObj . m_brief_separator_array.length; i ++) {
			thisObj . m_brief_separator_array[i].removeMovieClip ();
		}
		thisObj . m_detail_mc.removeMovieClip();
		for (i = 0; i < thisObj. m_brief_group_array.length; i ++) {
			thisObj . m_brief_group_array[i].removeMovieClip ();
		}
		
	}
	public function clean_previous_results ():Void
	{
		remove_movie_clips ();
	}
	
	private function select_preview_type (index:Number)
	{
		var thisObj = this;
		var content_y:Number = thisObj . m_parent_mc._y;
		var screen_height:Number = 450;
		var item_y:Number = thisObj . m_brief_panel_array[index-1]._y;
		var symbol_y:Number = 85;
		
		var yPos:Number = content_y + item_y + symbol_y;
		if (yPos < screen_height * 3/ 4) {
			thisObj . m_detail_mc.gotoAndStop (1);
		}
		else {
			thisObj . m_detail_mc.gotoAndStop(5);
		}
		
		
	}
	
	public function regist_listener (listener:Object)
	{
		var thisObj = this;
		this.m_listener = listener;
		thisObj . m_broadcaster.addListener (thisObj . m_listener);
	}
	
	public function set_keywords (keywords:Array) 
	{
		m_keywords = keywords;
	}
	
	public function display_results (result_array:Array, imp_array:Array, node_array:Array, edge_array:Array,
									 suggest_time:Number, keywords:String) {
		var thisObj = this;
		thisObj.m_parent_mc._parent._parent._parent.result_panel.head_txt.htmlText = 
			"Suggest <b>" + result_array.length + "</b> SQL queries for '<font color='#ff0000'>" + keywords + "</font>' (" + suggest_time + " ms)";
		if (result_array.length == 0) {
			return;
		}
		thisObj . remove_movie_clips ();
		m_scroll_mc._visible = true;
		var i:Number = 0;
		var current_y:Number = 50;
		var current_group_y:Number = 0;
		var current_group_id:Number = -1;
		var current_group_mc:MovieClip;
		var gtFormat:TextFormat = new TextFormat ();
		gtFormat.bold = true;
		gtFormat.font = "Calibri";
		//gtFormat.size = 17;
		for (i = 0; i < result_array.length; i ++) {
			var group_id:Number = result_array[i].group_id;
			var group_desc:String = result_array[i].group_desc;
			if (current_group_id == -1) {
				current_group_id = group_id;
				current_group_mc = 
					thisObj . m_parent_mc.attachMovie("templateUnit", "template" + group_id + "_mc", 
												  thisObj . m_parent_mc.getNextHighestDepth());
				current_group_mc._x = 3;
				current_group_mc._y = current_group_y;
				
				current_group_mc.title_mc.title_txt.text = "Template #" + (group_id + 1) + ": " 
					+ group_desc;
				current_group_mc.title_mc.title_txt.setTextFormat(gtFormat);
				thisObj. m_brief_group_array.push(current_group_mc);
				current_y = 30;
				
			} else {
				if (current_group_id != group_id) {
					current_group_mc.back_mc._height = current_y - 5;
					current_group_y += current_y + 10;
					current_group_mc = 
						thisObj . m_parent_mc.attachMovie("templateUnit", "template" + group_id + "_mc", 
												  thisObj . m_parent_mc.getNextHighestDepth());
					current_group_mc._x = 3;
					current_group_mc._y = current_group_y;
					current_group_mc.title_mc.title_txt.text = "Template #" + (group_id + 1) + ": " 
						+ group_desc;
					current_group_mc.title_mc.title_txt.setTextFormat(gtFormat);
					thisObj. m_brief_group_array.push(current_group_mc);
					current_y = 30;
					current_group_id = group_id;
				} 
			}
			
			var item:ResultUnit = new ResultUnit (current_group_mc, i);
			item.regist_listener (thisObj . m_listener);
			var unit:MovieClip = item.get_concrete_mc ();
			
			unit["number_txt"].text = i + 1;

			item.set_sql(result_array[i].sql, 
							imp_array[i], keywords, node_array[i], edge_array[i]);
			
			unit["result_back"]._height = unit["dsql_txt"].textHeight + 15;
			if (unit["result_back"]._height < 120) {
				unit["result_back"]._height = 120;
			}
			if (i != result_array.length -1) {
				var separator:MovieClip = thisObj. m_parent_mc .attachMovie ("resultSeparator", "result" + "_" + i + "_s_mc", thisObj. m_parent_mc.getNextHighestDepth ());
				separator._x = 5;
				separator._y = current_y + unit["result_back"]._height;
				thisObj . m_brief_separator_array.push(separator);
			}
			unit._x = 5;
			unit._y = current_y;
			current_y += unit["result_back"]._height + separator._height;
			thisObj . m_brief_panel_array.push(unit);
		}
		current_group_mc.back_mc._height = current_y;
		
		thisObj . m_detail_mc = thisObj . m_parent_mc.attachMovie("detailPreview", "preview_mc", thisObj . m_parent_mc.getNextHighestDepth());
		thisObj . m_detail_mc.next_btn.onRelease = function () {
			thisObj.cur_page += 10;
			thisObj.m_broadcaster.broadcastMessage("changepage", thisObj.m_detail_mc.m_sql, thisObj.cur_page);
		}
		thisObj . m_detail_mc.prev_btn.onRelease = function () {
			if (thisObj.cur_page - 10 >= 0) {
				thisObj.cur_page -= 10;
				thisObj.m_broadcaster.broadcastMessage("changepage", thisObj.m_detail_mc.m_sql, thisObj.cur_page);
			}
		}
		var dropShadow:DropShadowFilter = new DropShadowFilter(3, 45, 0x000000, 0.4, 10, 10, 2, 3);
		thisObj . m_detail_mc .filters = [dropShadow];
		thisObj . m_detail_mc . _visible = false;
		thisObj . m_scroll_mc.refresh ();
	}
	
	public function display_detail (attribute_array:Array, result_array:Array, stat:String,ypos:Number):Void
	{
		var thisObj = this;
		thisObj.m_detail_mc["detail_dg"].removeAllColumns();
		thisObj. m_detail_mc["detail_dg"].columnNames = attribute_array;
		thisObj. m_detail_mc["detail_dg"].dataProvider  = result_array;
		
		//adjust the widths of columns
		var i:Number;
		
		var total:Number = 0;
		if (result_array.length > 0) {
			for (i = 0; i < attribute_array.length; i ++) {
				var tuple_value:String = result_array[0][attribute_array[i]];
				if (tuple_value.length > 50) {
					total += 50;
				} else {
					total += tuple_value.length;
				}
			}
			var base:Number = 620 / total;
			for (i = 0; i < attribute_array.length; i ++) {
				var tuple_value:String = result_array[0][attribute_array[i]];
				var tuple_index:Number = thisObj. m_detail_mc["detail_dg"].getColumnIndex(attribute_array[i]);
				var l:Number = tuple_value.length;
				if (l > 50) {
					l = 50;
				}
				thisObj. m_detail_mc["detail_dg"].getColumnAt(tuple_index).width = base * l;
			}
		}
		
		thisObj . m_detail_mc._visible = true;
		
		thisObj . m_detail_mc._x = 43;
		thisObj.m_detail_mc.sg_mc._xscale = 100;
		thisObj.m_detail_mc.sg_mc._yscale = 100;
		if (thisObj.m_detail_mc.sg_mc._width > 450) {
			thisObj.m_detail_mc.sg_mc._xscale = 550 * 100 / thisObj.m_detail_mc.sg_mc._width;
			thisObj.m_detail_mc.sg_mc._yscale = 550 * 100 / thisObj.m_detail_mc.sg_mc._width;
		}
		
		if (ypos != undefined) {
			thisObj . m_detail_mc._y = ypos + 100;
		}
		thisObj . m_detail_mc.m_sql = stat;
		
	}
	
}