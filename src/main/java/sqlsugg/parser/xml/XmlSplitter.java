package sqlsugg.parser.xml;

import java.io.*;
import java.util.*;

public class XmlSplitter
{
  public XmlSplitter()
  {
    validRecordEndings_ = new HashSet<String>();
    validRecordEndings_.add("</article>");
    validRecordEndings_.add("</inproceedings>");
    validRecordEndings_.add("</proceedings>");
    validRecordEndings_.add("</book>");
    validRecordEndings_.add("</incollection>");
    validRecordEndings_.add("</phdthesis>");
    validRecordEndings_.add("</mastersthesis>");
    validRecordEndings_.add("</www>");
  }
  
  public int split(String xmlFile, String targetFolder, int limit) throws FileNotFoundException, IOException
  {
    BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
    String head = "";
    head += reader.readLine() + "\n";
    head += reader.readLine() + "\n";
    //
    int segmentId = 0;
    while (true) {
      boolean eof = false;
      int n_record = 0;
      BufferedWriter bw = new BufferedWriter(new FileWriter(targetFolder + segmentId + ".xml"));
      //
      System.out.print("Writing to " + segmentId + ".xml" + " ... ");
      //
      bw.write(head);
      bw.write("<dblp>\n");
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          eof = true;
          break;
        }
        if (line.equals("") || line.equals("<dblp>") || line.equals("</dblp>")) {
          continue;
        }
        bw.write(line + "\n");
        if (validRecordEndings_.contains(line)) {
          n_record++;
        }
        if (n_record >= limit) {
          break;
        }
      }
      bw.write("</dblp>\n");
      bw.close();
      //
      System.out.println("done.");
      //
      if (eof) {
        break;
      }
      segmentId++;
    }
    //
    return segmentId + 1;
  }

  //
  
  protected HashSet<String> validRecordEndings_; 
}
