package sqlsugg.parser;

import java.io.*;
import java.util.ArrayList;

import sqlsugg.parser.xml.*;

public class Main_Xml2Mem
{
  public static void main(String[] args) throws Exception
  {
    //String dir = (args.length == 1) ? args[0] : "E:\\Data\\dblp\\";
    String dir = "G:\\exp\\dblpparser-exp\\";
    //
    // Split the entire XML file into smaller ones
    //
    XmlSplitter splitter = new XmlSplitter();
    int n_segments = splitter.split(dir + "dblp.xml", dir, 100000);
    //
    // Parse XML files to populate record list
    //
    ArrayList<Record> records = new ArrayList<Record>();
    for (int i = 0; i < n_segments; i++) {
      String xmlFile = dir + i + ".xml";
      System.out.print("Parsing " + xmlFile + " ... ");
      XmlParser parser = new XmlParser();
      parser.parse(xmlFile, records);
      System.out.println("done.");
    }
    //
    //
    //
    System.out.println(records.size() + " record(s) loaded.");
  }
}
