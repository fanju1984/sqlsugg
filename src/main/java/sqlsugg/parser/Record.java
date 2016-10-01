package sqlsugg.parser;

import java.util.*;

public class Record
{
  public String               dblpid;
  public String                mdate;
  public String                title;
  public ArrayList<String>   authors;
  public String            booktitle;
  public String                 year;
  public String               volumn;
  public String               number;
  public String                pages;
  public String                   ee;
  public String                  url;
  //
  public Record()
  {
    dblpid = "";
    mdate = "";
    title = "";
    authors = new ArrayList<String>();
    booktitle = "";
    year = "";
    volumn = "";
    number = "";
    pages = "";
    ee = "";
    url = "";
  }
}
