import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class score {
	
	public static List<String[]> splitSentences(List<String> lines) {
		int numLines = lines.size();
		List<String[]> sentences = new ArrayList<String[]>(numLines);
		for(int i = 0; i < numLines; i++) {
			sentences.add(lines.get(i).split(" "));
		}
		
		return sentences;
	}
	
	public static List<String> readFile(Path file) {
		try {
			List<String> lines = Files.readAllLines(file, Charset.forName("ISO-8859-1"));
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		List<String[]> correctLines = splitSentences(readFile(Paths.get(args[0])));
		List<String[]> outputLines = splitSentences(readFile(Paths.get(args[1])));
		
		int correct = 0;
		int total = 0;
		
		int crsize = correctLines.size();
		for(int i = 0; i < crsize; i++) {
			String[] correctI = correctLines.get(i);
			String[] outputI = outputLines.get(i);
			
			for(int j = 1; j < correctI.length; j += 2) {
				total += 1;
				if(correctI[j].equals(outputI[j])) {
					correct += 1;
				}
			}
		}
		
		System.out.println("The proportion of correct tags: " + 1.0*correct/total);
	}
}
