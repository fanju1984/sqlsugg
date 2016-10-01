package sqlsugg.parser;

import java.io.FileOutputStream;

import sqlsugg.parser.xml.XmlParser2;
import sqlsugg.parser.xml.XmlSplitter;

public class Main_Xml2Csv
{
  public static void main(String[] args) throws Exception
  {
    //String dir = (args.length == 1) ? args[0] : "E:\\Data\\dblp\\";
    //String dir = (args.length == 1) ? args[0] : "/Users/wuhao/Workspaces/exp/dblp/";
    String dir = (args.length == 1) ? args[0] : "";
    //
    // Split the entire XML file into smaller ones
    //
    XmlSplitter splitter = new XmlSplitter();
    int n_segments = splitter.split(dir + "dblp.xml", dir, 100000);
    //int n_segments = 29;
    //
    // Clear the CSV file
    //
    FileOutputStream fos = new FileOutputStream(dir + "dblp.csv");
    fos.close();
    //
    // Parse XML files and output to the CSV file
    //
    for (int i = 0; i < n_segments; i++) {
      String xmlFile = dir + i + ".xml";
      System.out.print("Parsing " + xmlFile + " ... ");
      XmlParser2 parser = new XmlParser2();
      parser.parse(xmlFile, dir + "dblp.csv", ",", "\"", (i == 0));
      System.out.println("done.");
    }
  }
}
