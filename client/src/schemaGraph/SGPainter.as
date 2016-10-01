
class src.schemaGraph.SGPainter {
	private var movieclips:Array;
	
	private var m_this_mc:MovieClip;
	private var m_width:Number;
	private var m_length:Number;
	
	function SGPainter (tobj:MovieClip, width:Number, length:Number){
		m_this_mc = tobj;
		movieclips = new Array();
		m_width = width;
		m_length = length;
	}
	
	function clear_movieclips ():Void {
		var thisObj = this;
		var i:Number = 0;
		for (; i < thisObj.movieclips.length; i ++) {
			thisObj.movieclips[i].removeMovieClip();
		}
		thisObj.movieclips = new Array();
	}
	
	function paint (nodes:Array, edges:Array):Void {
		var thisObj = this;
		thisObj.clear_movieclips();
		var step:Number = 180;
		var ystart = 30;
		var xstart:Number = -50;
		var i:Number = 0;
		var predicate_width:Number = 120;
		var predicate_step:Number = 0;
		var node_margin = 50;
		var node_vmargin = 12;
		
		var right_pre = 0;
		
		var last_x:Number = -1;
		var last_leftmost = -1;
		for (; i < nodes.length; i ++) {
			var local_clips:Array = new Array ();//the array storing local movie clips
			var leftmost = 0;//the position of leftmost boundary;
			
			var node:MovieClip = thisObj.m_this_mc.attachMovie ("sg_node", "node" + "_" + i + "_mc", thisObj. m_this_mc.getNextHighestDepth ());
			node.ntxt.text = nodes[i].name;
			node._x = 0;
			node._y = ystart;
			leftmost = - node_margin;
			thisObj.movieclips.push(node);
			local_clips.push(node);
			
			var predicate_start:Number = 0;
			if (nodes[i].predicates.length % 2 == 0) {
				predicate_start = node._x - (predicate_width + predicate_step) * (nodes[i].predicates.length / 2 - 0.5);
			} else {
				predicate_start = node._x - (predicate_width + predicate_step) * (nodes[i].predicates.length -1) / 2;
				
			}
			var j:Number = 0;
			for (; j < nodes[i].predicates.length; j ++) {
				var predicate:MovieClip = thisObj.m_this_mc.attachMovie("sg_predicate", "predicate" + "_" + i + "_" + j + "_mc", 
																		thisObj. m_this_mc.getNextHighestDepth ());
				var predicate_line:MovieClip = 
					thisObj.m_this_mc.attachMovie("sg_line1", "plin_" + i + "_" + j + "_mc", thisObj.m_this_mc.getNextHighestDepth());
				
				var ptext:String = nodes[i].predicates[j];
				ptext = ptext.split(" like ").join(":");
				ptext = ptext.split("&pct;").join("");
				var dotindex:Number = ptext.indexOf(".", 0);
				ptext = ptext.substr(dotindex + 1, ptext.length - dotindex);
				if (ptext.length > 14) {
					ptext = ptext.substr(0, 14) + "...";
				}
				predicate.ptxt.text = ptext;
				predicate._y = thisObj.m_length - ystart;
				predicate._x = predicate_start + (predicate_width + predicate_step) * j;
				var predicate_width1:Number = predicate.ptxt.textWidth;
				if (predicate._x - predicate_width1 / 2 < leftmost) {
					leftmost = predicate._x - predicate_width1 / 2;
					//leftmost = 0;
				}
				
				var degree:Number = Math.atan((predicate._x - node._x) / (predicate._y - node._y)) * 180 / Math.PI;
				degree = 90 - degree;
				
				predicate_line._x = node._x ;
				predicate_line._y = node._y + node_vmargin;
				predicate_line._width = (predicate._y - node._y - 2 * node_vmargin) / Math.sin(degree * Math.PI / 180);
				predicate_line._rotation = degree;
				
				thisObj.movieclips.push(predicate);
				thisObj.movieclips.push(predicate_line);
				local_clips.push(predicate);
				local_clips.push(predicate_line);
			}
			if (last_leftmost != -1) {
				var margin:Number = - (last_leftmost * 2);
				if (step + leftmost > - last_leftmost) {
					margin = step + leftmost - last_leftmost;
				}
				xstart += margin;
			}
			var li:Number = 0;
			for (; li < local_clips.length; li ++) {
				local_clips[li]._x += xstart - leftmost;
			}
			
			if (last_x != -1) {
				var line:MovieClip = thisObj.m_this_mc.attachMovie("sg_line", "line_" + i + "_mc", thisObj.m_this_mc.getNextHighestDepth());
				var line_text:MovieClip = thisObj.m_this_mc.attachMovie("sg_relation", "linetext" + i + "_mc", thisObj.m_this_mc.getNextHighestDepth());
				line_text.rtxt.text = edges[i - 1];
				line_text._x = last_x + (node._x - last_x) / 2;
				line_text._y = node._y;
				line._x = last_x + node_margin;
				line._width = node._x - last_x - (node_margin) * 2;
				line._y = node._y;
				thisObj.movieclips.push(line);
				thisObj.movieclips.push(line_text);
			}
			last_x = xstart - leftmost;
			last_leftmost = leftmost;
		}
		if (leftmost < 0) {
			var i:Number = 0;
			for (; i < movieclips.length; i ++) {
				movieclips[i]._x -= leftmost;
			}
		}
	}
}