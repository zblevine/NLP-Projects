import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.io.IOException;

public class App {
	
	public static List<String> readFile(Path file) {
		try {
			List<String> lines = Files.readAllLines(file, Charset.forName("ISO-8859-1"));
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main (String[] args) {
		Path p = Paths.get(args[0]);
		List<String> sentences = readFile(p);
		Hashtable<String, Integer> count = new Hashtable<String, Integer>();
		
		for(int i = 0; i < sentences.size(); i++) {
			String[] words = sentences.get(i).split(" ");
			for(int j = 0; j < words.length; j++) {
				if(count.containsKey(words[j])) {
					int val = count.get(words[j]);
					count.put(words[j], val + 1);
				}
				else {
					count.put(words[j], 1);
				}
			}
		}
		
		Vector<String> linesToWrite = new Vector<String>();
		for(Enumeration<String> wordlist = count.keys(); wordlist.hasMoreElements();) {
			String line = wordlist.nextElement();
			String value = Integer.toString(count.get(line));
			linesToWrite.add(line + " " + value);
		}
		
		Path out = Paths.get(args[1]);
		try {
			Files.write(out, linesToWrite, Charset.forName("ISO-8859-1"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
