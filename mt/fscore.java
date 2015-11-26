import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;



public class fscore {
	
	
	public static int getCorrect(List<String[]> output, List<String[]> correct) {
		int outSize = output.size();
		int count = 0;
		for(int i = 0; i < outSize; i++) {
			String[] outI = output.get(i);
			String[] correctI = correct.get(i);
			if(outI.length <= 10) {
				for(int j = 0; j < outI.length; j++) {
					if(Arrays.asList(correctI).contains(outI[j])) {
						count++;
					}
				}
			}
		}
		
		return count;
	}
	
	public static int getWordCount(List<String[]> sentences) {
		int sentSize = sentences.size();
		int sum = 0;
		for(int i = 0; i < sentSize; i++) {
			int len = sentences.get(i).length;
			if(len <= 10) {
				sum += len;
			}
		}
		
		return sum;
	}
	
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
	
	public static void main(String args[]) {
		List<String[]> progout = splitSentences(readFile(Paths.get(args[0])));
		List<String[]> correct = splitSentences(readFile(Paths.get(args[1])));
		
		int numCorrect = getCorrect(progout, correct);
		int numReturned = getWordCount(progout);
		int shouldHave = getWordCount(correct);
		
		double precision = numCorrect*1.0/numReturned;
		double recall = numCorrect*1.0/shouldHave;
		
		double fScore = 2*precision*recall/(precision + recall);
		System.out.println("The f-score of " + args[0] + " was: " + fScore);
	}
}
