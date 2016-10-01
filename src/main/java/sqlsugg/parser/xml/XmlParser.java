package sqlsugg.parser.xml;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import sqlsugg.parser.Record;

public class XmlParser
{
  public XmlParser()
  {
  }

  public void parse(String uri, ArrayList<Record> records)
  {
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      SAXParser parser = parserFactory.newSAXParser();
      RecordHandler handler = new RecordHandler(records);
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

class RecordHandler extends DefaultHandler
{
  ArrayList<Record> records_;
  //
  HashSet<String> validRecordTags_;
  //
  boolean         recording_;
  String          currentRecordTag_;
  Record          currentRecord_;
  String          currentValue_;
  //
  Locator         locator_;

  public RecordHandler(ArrayList<Record> records) throws IOException
  {
    records_ = records;
    //
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
    recording_ = false;
  }

  public void setDocumentLocator(Locator locator)
  {
    this.locator_ = locator;
  }

  public void startDocument()
  {
  }

  public void endDocument()
  {
  }

  public void startElement(String namespaceURI, String localName, String rawName, Attributes atts)
      throws SAXException
  {
    if (validRecordTags_.contains(rawName)) {
      recording_ = true;
      currentRecord_ = new Record();
      currentRecord_.dblpid = atts.getValue("key");
      currentRecord_.mdate = atts.getValue("mdate");
      currentRecord_.authors.clear();
      currentRecordTag_ = rawName;
    }
    currentValue_ = "";
  }

  public void endElement(String namespaceURI, String localName, String rawName) throws SAXException
  {
    if (!recording_) {
      return;
    }
    if (rawName.equals("title")) {
      currentRecord_.title = currentValue_;
    } else if (rawName.equals("author")) {
      currentRecord_.authors.add(currentValue_);
    } else if (rawName.equals("booktitle")) {
      currentRecord_.booktitle = currentValue_;
    } else if (rawName.equals("journal")) {
      currentRecord_.booktitle = currentValue_;
    } else if (rawName.equals("year")) {
      currentRecord_.year = currentValue_;
    } else if (rawName.equals("volume")) {
      currentRecord_.volumn = currentValue_;
    } else if (rawName.equals("number")) {
      currentRecord_.number = currentValue_;
    } else if (rawName.equals("pages")) {
      currentRecord_.pages = currentValue_;
    } else if (rawName.equals("ee")) {
      currentRecord_.ee = currentValue_;
    } else if (rawName.equals("url")) {
      currentRecord_.url = currentValue_;
    } else if (rawName.equals(currentRecordTag_)) {
      records_.add(currentRecord_);
      recording_ = false;
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException
  {
    currentValue_ += new String(ch, start, length);
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
}

//
