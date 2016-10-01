package sqlsugg.util.tokenizer;
import java.util.*;
import java.io.*;

public class StopWords {
	public Set<String> stopwords = new HashSet<String>();
	
	public StopWords (String filename) {
		try {
			BufferedReader r = new BufferedReader (new FileReader(filename));
			String line = r.readLine();
			while (line != null) {
				String words[] = line.split("\t");
				for (String word: words) {
					word = word.trim();
					if (word.length() > 0) {
						stopwords.add(word);
					}
				}
				line = r.readLine();
			}
			r.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isStopWord (String word) {
		return stopwords.contains(word);
	}
	
	public static void main (String args[]) {
		StopWords sw = new StopWords("etc/stopwords.txt");
		System.out.println(sw.isStopWord("thats"));
	}
}
