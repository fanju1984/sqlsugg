package sqlsugg.parser.xml;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import sqlsugg.parser.Record;

public class XmlParser2
{
  public XmlParser2()
  {
  }

  public void parse(String uri, String outputFile, String sep, String quote, boolean caption)
  {
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      SAXParser parser = parserFactory.newSAXParser();
      ConfigHandler handler = new ConfigHandler(outputFile, sep, quote, caption);
      parser.getXMLReader().setFeature("http://xml.org/sax/features/validation", true);
      parser.parse(new File(uri), handler);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }
}

class ConfigHandler extends DefaultHandler
{
  String          sep_;
  String          quote_;
  boolean         caption_;
  //
  int             n_;
  HashSet<String> validRecordTags_;
  BufferedWriter  output_;
  Record          record_;
  String          recordTag_;
  String          value_;
  //
  Locator         locator_;

  public ConfigHandler(String outputFile, String sep, String quote, boolean caption) throws IOException
  {
    sep_ = sep;
    quote_ = quote;
    caption_ = caption;
    //
    n_ = 0;
    validRecordTags_ = new HashSet<String>();
    validRecordTags_.add("article"); // journal papers
    validRecordTags_.add("inproceedings"); // conf papers
    //validRecordTags_.add("proceedings");
    //validRecordTags_.add("book");
    validRecordTags_.add("incollection"); // similar to conf papers
    //validRecordTags_.add("phdthesis");
    //validRecordTags_.add("mastersthesis");
    //validRecordTags_.add("www");
    //
    record_ = new Record();
    record_.authors.ensureCapacity(100);
    //
    output_ = new BufferedWriter(new FileWriter(outputFile, true));
  }

  public void setDocumentLocator(Locator locator)
  {
    this.locator_ = locator;
  }

  public void startDocument()
  {
    try {
      if (caption_) {
        output_.write(quote("key") + sep_);
        output_.write(quote("title") + sep_);
        output_.write(quote("authors") + sep_);
        output_.write(quote("booktitle") + sep_);
        output_.write(quote("year") + sep_);
        output_.write(quote("volumn") + sep_);
        output_.write(quote("number") + sep_);
        output_.write(quote("pages") + sep_);
        output_.write(quote("ee") + sep_);
        output_.write(quote("url"));
        output_.write("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void endDocument()
  {
    try {
      output_.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void startElement(String namespaceURI, String localName, String rawName, Attributes atts)
      throws SAXException
  {
    if (validRecordTags_.contains(rawName)) {
      record_.dblpid = atts.getValue("key");
      record_.authors.clear();
      recordTag_ = rawName;
    }
    value_ = "";
  }

  public void endElement(String namespaceURI, String localName, String rawName) throws SAXException
  {
    if (rawName.equals("title")) {
      record_.title = value_;
    } else if (rawName.equals("author")) {
      record_.authors.add(value_);
    } else if (rawName.equals("booktitle")) {
      record_.booktitle = value_;
    } else if (rawName.equals("journal")) {
      record_.booktitle = value_;
    } else if (rawName.equals("year")) {
      record_.year = value_;
    } else if (rawName.equals("volume")) {
      record_.volumn = value_;
    } else if (rawName.equals("number")) {
      record_.number = value_;
    } else if (rawName.equals("pages")) {
      record_.pages = value_;
    } else if (rawName.equals("ee")) {
      record_.ee = value_;
    } else if (rawName.equals("url")) {
      record_.url = value_;
    } else if (rawName.equals(recordTag_)) {
      try {
        handleNewRecord();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException
  {
    value_ += new String(ch, start, length);
  }

  public void warning(SAXParseException exception) throws SAXException
  {

    Message("**Parsing Warning**\n", exception);
    throw new SAXException("Warning encountered");
  }

  public void error(SAXParseException exception) throws SAXException
  {

    Message("**Parsing Error**\n", exception);
    throw new SAXException("Error encountered");
  }

  public void fatalError(SAXParseException exception) throws SAXException
  {

    Message("**Parsing Fatal Error**\n", exception);
    throw new SAXException("Fatal Error encountered");
  }

  //

  private void Message(String mode, SAXParseException exception)
  {
    System.out.println(mode + " Line: " + exception.getLineNumber() + " URI: " + exception.getSystemId() + "\n"
        + " Message: " + exception.getMessage());
  }

  private void handleNewRecord() throws IOException
  {
    //output_.write("\"" + String.valueOf(n_)) + sep_);
    output_.write(quote(encode(record_.dblpid)) + sep_);
    output_.write(quote(encode(record_.title)) + sep_);
    //
    String authors = "";
    for (int i = 0; i < record_.authors.size(); i++) {
      if (i > 0) {
        authors += "; ";
      }
      authors += record_.authors.get(i);
    }
    output_.write(quote(encode(authors)) + sep_);
    //
    output_.write(quote(encode(record_.booktitle)) + sep_);
    output_.write(quote(encode(record_.year)) + sep_);
    output_.write(quote(encode(record_.volumn)) + sep_);
    output_.write(quote(encode(record_.number)) + sep_);
    output_.write(quote(encode(record_.pages)) + sep_);
    output_.write(quote(encode(record_.ee)) + sep_);
    output_.write(quote(encode(record_.url)));
    output_.write("\n");
    output_.flush();
    //System.out.println("Doc " + n_ + " converted.");
    n_++;
  }

  private String quote(String str)
  {
    return quote_ + str + quote_;
  }
  
  private String encode(String str)
  {
    if (quote_.equals("")) {
      return str;
    }
    char q = quote_.charAt(0);
    String estr = "";
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c == '\\') {
        estr += "\\\\";
      } else if (c == q) {
        estr += "\\" + q;
      } else {
        estr += c;
      }
    }
    return estr;
  }
}

//