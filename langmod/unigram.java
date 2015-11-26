import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class unigram {
	
	public static double logProbDocument(HashMap<String, Double> oldThetas, 
			HashMap<String, Integer> newCounts) {
		double logProb = 0;
		Iterator<String> words = newCounts.keySet().iterator();
		while(words.hasNext()) {
			String curr = words.next();
			int wc = newCounts.get(curr);
			if(oldThetas.containsKey(curr)) {
				logProb += Math.log(oldThetas.get(curr))*wc;
			}
			else {
				logProb += Math.log(oldThetas.get("*U*"))*wc;
			}
		}
		
		return logProb;
	}
	
	public static HashMap<String, Integer> getCounts(List<String> lines) {
		HashMap<String, Integer> count = new HashMap<String, Integer>();
		
		for(int i = 0; i < lines.size(); i++) {
			String[] words = lines.get(i).split(" ");
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
		
		return count;
	}
	
	public static double theta(int totalCounts, int wordCount, int numWords, 
			double alpha) {
		return (wordCount + alpha)/(totalCounts + (alpha*numWords));
	}
	
	public static HashMap<String, Double> getThetas(HashMap<String, Integer> counts,
							int totalCounts, int numWords, double alpha) {
		HashMap<String, Double> thetas = new HashMap<String, Double>();
		Iterator<String> words = counts.keySet().iterator();
		while(words.hasNext()) {
			String curr = words.next();
			thetas.put(curr, theta(totalCounts, counts.get(curr), numWords,
					alpha));
		}
		
		return thetas;
	}
	
	public static double getOptimalAlpha(HashMap<String, Integer> train,
			HashMap<String, Integer> heldout, int totalCounts, int numWords,
			double start, double mid, double end, double tau, double midProb) {
		double phi = (1 + Math.sqrt(5))/2;
		double resphi = 2 - phi;
		double x;
		
		if((end - mid) > (mid - start)) {
			x = mid + resphi*(end - mid);
		}
		else {
			x = mid - resphi*(mid - start);
		}
		
		if((end - start) < tau*(mid + x)) {
			return (start + end)/2;
		}
		
		if(midProb == -1) {
			midProb = logProbDocument(getThetas(train, totalCounts, numWords, mid), heldout);
		}
		double xProb = logProbDocument(getThetas(train, totalCounts, numWords, x), heldout);
		
		if(midProb < xProb) {
			if((end - mid) > (mid - start)) {
				return getOptimalAlpha(train, heldout, totalCounts, numWords, mid, x, end, tau, xProb);
			}
			else {
				return getOptimalAlpha(train, heldout, totalCounts, numWords, start, x, mid, tau, xProb);
			}
		}
		else {
			if((end - mid) > (mid - start)) {
				return getOptimalAlpha(train, heldout, totalCounts, numWords, start, mid, x, tau, midProb);
			}
			else {
				return getOptimalAlpha(train, heldout, totalCounts, numWords, x, mid, end, tau, midProb);
			}
		}
	}
	
	public static double guessSentences(HashMap<String, Double> thetas, List<String> lines) {
		int numCorrect = 0;
		for(int i = 0; i < lines.size(); i += 2) {
			HashMap<String, Integer> map1 = new HashMap<String, Integer>();
			HashMap<String, Integer> map2 = new HashMap<String, Integer>();
			String[] s1 = lines.get(i).split(" ");
			String[] s2 = lines.get(i+1).split(" ");
			for(int j = 0; j < s1.length; j++) {
				if(map1.containsKey(s1[j])) {
					int val = map1.get(s1[j]);
					map1.put(s1[j], val + 1);
				}
				else {
					map1.put(s1[j], 1);
				}
			}
			for(int j = 0; j < s2.length; j++) {
				if(map2.containsKey(s2[j])) {
					int val = map2.get(s2[j]);
					map2.put(s2[j], val + 1);
				}
				else {
					map2.put(s2[j], 1);
				}
			}
			if(logProbDocument(thetas, map1) > logProbDocument(thetas, map2)) {
				numCorrect++;
			}
		}
		
		return 2.0*numCorrect/lines.size();
	}
	
	public static int sum(Iterator<Integer> c) {
		int s = 0;
		while(c.hasNext()) {
			s += c.next();
		}
		return s;
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
		Path train = Paths.get(args[0]);
		Path heldout = Paths.get(args[1]);
		Path test = Paths.get(args[2]);
		Path goodbad = Paths.get(args[3]);
		
		HashMap<String, Integer> trainCounts = getCounts(readFile(train));
		HashMap<String, Integer> heldOutCounts = getCounts(readFile(heldout));
		HashMap<String, Integer> testCounts = getCounts(readFile(test));
		trainCounts.put("*U*", 0);
		
		int numTrainWords = trainCounts.size();
		int totTrainWords = sum(trainCounts.values().iterator());
		
		HashMap<String, Double> alphaOneThetas = getThetas(trainCounts, 
				totTrainWords, numTrainWords, 1);
		double alphaOneProb = logProbDocument(alphaOneThetas, testCounts);
		System.out.println(alphaOneProb);
		
		double optimalAlpha = getOptimalAlpha(trainCounts, heldOutCounts, totTrainWords,
				numTrainWords, 0, 2.5, 5, .001, -1);
		
		HashMap<String, Double> alphaOptimalThetas = getThetas(trainCounts, totTrainWords,
				numTrainWords, optimalAlpha);
		double alphaOptimalProb = logProbDocument(alphaOptimalThetas, testCounts);
		System.out.println(alphaOptimalProb);
		System.out.println(guessSentences(alphaOptimalThetas, readFile(goodbad)));
		System.out.println(optimalAlpha);
	}
}
