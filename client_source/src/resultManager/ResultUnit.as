import src.frontManager.*;
import src.schemaGraph.*;

class src.resultManager.ResultUnit 
{
	var m_parent_mc:MovieClip;
	var m_concrete_mc:MovieClip;
	var broadcaster:Object;
	
	var m_dsql:String;
	var m_sql:String;
	
	var nodes:Array;
	var edges:Array;
	
	var m_id:Number;
	
	var m_sgpainter:SGPainter;
	
	function ResultUnit (parent_mc:MovieClip, id:Number)
	{
		var thisObj = this;
		thisObj . m_parent_mc = parent_mc;
		thisObj . m_concrete_mc = parent_mc.attachMovie("resultUnit", "unit" + id, parent_mc.getNextHighestDepth());
		thisObj . m_id = id;
		thisObj . broadcaster = new Object ();
		AsBroadcaster.initialize(broadcaster);
		thisObj . add_action_listener ();
		thisObj . m_sgpainter = new SGPainter (thisObj.m_concrete_mc.sg_mc, 800, 120);
	}
	
	private function add_action_listener ()
	{
		var thisObj = this;
		thisObj . m_concrete_mc["detail_infector"].onRollOver = function ()
		{
			thisObj . broadcaster.broadcastMessage ("preview", thisObj . m_id,  thisObj.m_sql, thisObj.m_dsql, 
													thisObj.m_concrete_mc._y);
		}
		
		thisObj  .m_concrete_mc["detail_infector"].onRelease = function ()
		{
			thisObj . broadcaster.broadcastMessage ("details", thisObj . m_id);
		}
	}
	
	public function set_sql (sql:String, imp:String, keywords:String, ns:Array, es:Array) {
		var keyword_array:Array = keywords.split(" ");
		
		var thisObj = this;
		
		thisObj.nodes = ns;
		thisObj.edges = es;
		
		m_sql = imp;
		var tmp:String = sql;
		tmp = tmp.split(">").join("&gt;");
		tmp = tmp.split("<").join("&lt;");
		trace (tmp);
		tmp = tmp.split("like").join("<b><font color-'#0000FF'>CONTAIN</b>");
		tmp = tmp.split("CONTAIN").join("<i>CONTAIN</i>");
		tmp = tmp.split("%").join("");
		tmp = tmp.split("SELECT").join("<b><font color='#0000FF'>SELECT</font></b>");
		tmp = tmp.split("FROM").join("<br><b><font color='#0000FF'>FROM</font></b>");
		tmp = tmp.split("WHERE").join("<br><b><font color='#0000FF'>WHERE</font></b>");
		tmp = tmp.split("GROUP BY").join("<br><b><font color='#0000FF'>GROUP BY</font></b>");
		
		
		var i:Number = 0;
		for (i = 0; i < keyword_array.length; i ++) {
			tmp = tmp.split("'" + keyword_array[i] + "'").join("'<b><font color='#FF0000'>" + keyword_array[i] + "</font></b>'");
			tmp = tmp.split(" " + keyword_array[i] + " ").join(" <b><font color='#FF0000'>" + keyword_array[i] + "</font></b> ");
			//tmp = tmp.split(" " + keyword_array[i].toUpperCase() + "(").join(" <b><font color='#FF0000'>" + keyword_array[i].toUpperCase()
																														//+ "</font></b>(");
																														

			tmp = tmp.split("," + keyword_array[i].toUpperCase() + "(").join(",<b><font color='#FF0000'>" + keyword_array[i].toUpperCase()
																														+ "</font></b>(");
		}
		m_dsql = tmp;
		thisObj.m_concrete_mc["dsql_txt"].htmlText = tmp;
		
		thisObj.m_sgpainter . paint (ns, es);
		if (thisObj.m_concrete_mc.sg_mc._width > 700) {
			thisObj.m_concrete_mc.sg_mc._xscale = 700 / thisObj.m_concrete_mc.sg_mc._width * 100;
			thisObj.m_concrete_mc.sg_mc._yscale = thisObj.m_concrete_mc.sg_mc._xscale;
		}
	}
	
	private function moveTo (xPos:Number, yPos:Number)
	{
		var thisObj = this;
		thisObj . m_concrete_mc._x = xPos;
		thisObj . m_concrete_mc._y = yPos;
	}
	
	private function set_height (h:Number):Void
	{
		var thisObj = this;
		thisObj . m_concrete_mc["result_back"]._height = h;
	}
	
	public function get_concrete_mc ():MovieClip
	{
		return this.m_concrete_mc;
	}
	
	public function regist_listener (listener:Object)
	{
		var thisObj = this;
		thisObj . broadcaster.addListener (listener );
	}
	
}