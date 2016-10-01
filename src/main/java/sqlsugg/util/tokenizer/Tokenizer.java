package sqlsugg.util.tokenizer;
import java.util.*;


public class Tokenizer {
	public StopWords sw;
	final String separator = "\\W+";
	public Tokenizer (String swfile) {
		sw = new StopWords (swfile);
	}
	
	
	public List<String> tokenize (String str) {
		String rawTokens[] = str.split(separator);
		List<String> tokens = new LinkedList<String> ();
		for (String t: rawTokens) {
			String t1s[] = t.split("_");
			for (String t1: t1s) {
				if (!sw.isStopWord(t1.trim())) {
					tokens.add(normalize(t1));
				}
			}
		}
		return tokens;
	}
	
	private String normalize (String str) {
		str = str.toLowerCase();
		return str;
	}
	
	public static void main (String args[]) {
		try {
			Tokenizer tokenizer = new Tokenizer ("etc/stopwords.txt");
			String str = "database_Ir";
			System.out.println(tokenizer.tokenize(str));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
