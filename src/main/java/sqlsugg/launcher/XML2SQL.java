//package sqlsugg.launcher;
//
//import org.jdom2.input.*;
//import org.jdom2.*;
//
//import java.util.*;
//import java.io.*;
//
//public class XML2SQL {
//	public static void main (String args[]) throws Exception {
//		String xmlpath = "data/dblp.xml";
//		SAXBuilder builder = new SAXBuilder (false);
//		Document doc = builder.build(xmlpath);
//		Element dblp = doc.getRootElement();
//		System.out.println(dblp.getName());
//		
//		String folder = "data/dblp_data/";
//		Map<String, BufferedWriter> typeWriters = 
//			new HashMap<String, BufferedWriter> ();
//		
//		List<Element> children = dblp.getChildren();
//		System.out.println ("# of Element: " + children.size());
//		int count = 0;
//		for (Element element: children) {
//			String docType = element.getName();
//			BufferedWriter dataWriter = null;
//			BufferedWriter metaWriter = null;
//			if (typeWriters.containsKey(docType)) {
//				dataWriter = typeWriters.get(docType);
//			} else {
//				dataWriter = new BufferedWriter (
//						new FileWriter(folder + docType + ".dat"));
//				typeWriters.put(docType, dataWriter);
//				metaWriter = new BufferedWriter (
//						new FileWriter (folder + docType + ".meta"));
//				
//			}
//			List<Element> nextChildren = element.getChildren();
//			String meta = "";
//			String data = "";
//			TreeMap<String, String> nameValues = 
//				new TreeMap<String, String> ();
//			for (Element nextChild: nextChildren) {
//				String name = nextChild.getName();
//				String value = nextChild.getValue();
//				String existValue 
//			}
//			
//			
//			data = data.trim();
//			data = data.replaceAll("\"", "\\\"");
//			meta = meta.trim();
//			dataWriter.write(data);
//			dataWriter.newLine();
//			if (metaWriter != null) {
//				metaWriter.write(meta);
//				metaWriter.close();
//			}
//			count ++;
//			if (count % 1000 == 0) {
//				System.out.println ("Progress: " + count + "/" + children.size());
//			}
//		}
//		for (String type: typeWriters.keySet()) {
//			typeWriters.get(type).close();
//		}
//	}
//}
