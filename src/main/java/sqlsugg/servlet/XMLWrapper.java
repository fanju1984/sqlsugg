package sqlsugg.servlet;

public class XMLWrapper {
	StringBuffer buffer = new StringBuffer();
	
	public XMLWrapper () {
	}
	
	public void setHeader (long elapsedTime, String keywords) {
		buffer = new StringBuffer ("<results time='" + elapsedTime + "' " + 
				"keywords='" + keywords + "'>\n" +
				 buffer.toString());
	}
	
	public void startGroup (String desc) {
		buffer.append("<group desc='" + desc + "'>\n");
	}
	
	public void addSQL (String stat, String imp, String graphics) {
		buffer.append(" <sql>\n");
		buffer.append("  <stat value='" + encode (stat) + "'/>\n");
		buffer.append("  <imp value='" + encode (imp) + "'/>\n");
		buffer.append(graphics);
		buffer.append(" </sql>\n");
	}
	
	public void endGroup () {
		buffer.append("</group>\n");
	}
	
	public void finalize () {
		buffer.append("</results>");
	}
	
	public String getXML () {
		return buffer.toString();
	}
	String encode (String str) {
		str = str.replaceAll("\\\"", "&quot;");
		str = str.replaceAll("\\'", "&apos;");
		str = str.replaceAll("\n", " ");
		return str;
	}
	
	
}
