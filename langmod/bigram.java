import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class bigram {
	
	public static double logProbBigram(HashMap<List<String>, Integer> bicounts, 
			HashMap<List<String>, Integer> bitrain, HashMap<String, Integer> unitrain, 
			HashMap<String, Double> unithetas, double beta) {
		double logProb = 0;
		Iterator<List<String>> pairs = bicounts.keySet().iterator();
		while(pairs.hasNext()) {
			List<String> curr = pairs.next();
			int wc = bicounts.get(curr);
			if(unitrain.containsKey(curr.get(0))) {
				int c = unitrain.get(curr.get(0));
				if(curr.get(0) == "STOPSYMBOL") {
					c = c/2;
				}
				if(bitrain.containsKey(curr)) {
					double th = (bitrain.get(curr) + (unithetas.get(curr.get(1))*beta))/(c + beta);
					logProb += Math.log(th)*wc;
				}
				else {
					if(unithetas.containsKey(curr.get(1))) {
						double th = (unithetas.get(curr.get(1))*beta)/(c + beta);
						logProb += Math.log(th)*wc;
					}
					else {
						double th = (unithetas.get("*U*")*beta)/(c + beta);
						logProb += Math.log(th)*wc;
					}
				}
			}
			else {
				if(unithetas.containsKey(curr.get(1))) {
					logProb += Math.log(unithetas.get(curr.get(1)))*wc;
				}
				else {
					logProb += Math.log(unithetas.get("*U*"))*wc;
				}
			}
		}
		
		return logProb;
	}
	
	public static double getOptimalBeta(HashMap<List<String>, Integer> biheldout, 
			HashMap<List<String>, Integer> bitrain, HashMap<String, Integer> unitrain,
			HashMap<String, Double> thetas, double start, double mid, 
			double end, double tau, double midProb) {
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
		
		if(midProb == -1) { //starter/"uninitialized" value for this variable
			midProb = logProbBigram(biheldout, bitrain, unitrain, thetas, mid);
		}
		double xProb = logProbBigram(biheldout, bitrain, unitrain, thetas, x);
		
		if(midProb < xProb) {
			if((end - mid) > (mid - start)) {
				return getOptimalBeta(biheldout, bitrain, unitrain, thetas, mid, x, end, tau, xProb);
			}
			else {
				return getOptimalBeta(biheldout, bitrain, unitrain, thetas, start, x, mid, tau, xProb);
			}
		}
		else {
			if((end - mid) > (mid - start)) {
				return getOptimalBeta(biheldout, bitrain, unitrain, thetas, start, mid, x, tau, midProb);
			}
			else {
				return getOptimalBeta(biheldout, bitrain, unitrain, thetas, x, mid, end, tau, midProb);
			}
		}
	}
	
	public static HashMap<List<String>, Integer> getCountsBigram(List<String> lines) {
		HashMap<List<String>, Integer> count = new HashMap<List<String>, Integer>();
		
		for(int i = 0; i < lines.size(); i++) {
			String[] words = lines.get(i).split(" ");
			String[] stopwords = new String[words.length + 2];
			stopwords[0] = "STOPSYMBOL";
			stopwords[stopwords.length - 1] = "STOPSYMBOL";
			for(int k = 0; k < words.length; k++) {
				stopwords[k+1] = words[k];
			}
			
			for(int j = 0; j < stopwords.length-1; j++) {
				List<String> pair = new ArrayList<String>(2);
				pair.add(stopwords[j]);
				pair.add(stopwords[j+1]);
				if(count.containsKey(pair)) {
					int val = count.get(pair);
					count.put(pair, val + 1);
				}
				else {
					count.put(pair, 1);
				}
			}
		}
		
		return count;
	}
	
	public static double guessSentences(HashMap<List<String>, Integer> bitrain, 
			HashMap<String, Integer> unitrain, HashMap<String, Double> unithetas,
			double beta, List<String> lines) {
		int numCorrect = 0;
		for(int i = 0; i < lines.size(); i += 2) {
			HashMap<List<String>, Integer> map1 = new HashMap<List<String>, Integer>();
			HashMap<List<String>, Integer> map2 = new HashMap<List<String>, Integer>();
			String[] s1 = lines.get(i).split(" ");
			String[] s2 = lines.get(i+1).split(" ");
			String[] s1STOP = new String[s1.length + 2];
			String[] s2STOP = new String[s2.length + 2];
			
			s1STOP[0] = "STOPSYMBOL";
			s2STOP[0] = "STOPSYMBOL";
			s1STOP[s1STOP.length - 1] = "STOPSYMBOL";
			s2STOP[s2STOP.length - 1] = "STOPSYMBOL";
			
			for(int k = 0; k < s1.length; k++) {
				s1STOP[k+1] = s1[k];
			}
			for(int k = 0; k < s2.length; k++) {
				s2STOP[k+1] = s2[k];
			}
			for(int j = 0; j < s1STOP.length-1; j++) {
				List<String> pair = new ArrayList<String>(2);
				pair.add(s1STOP[j]);
				pair.add(s1STOP[j+1]);
				if(map1.containsKey(pair)) {
					int val = map1.get(pair);
					map1.put(pair, val + 1);
				}
				else {
					map1.put(pair, 1);
				}
			}
			for(int j = 0; j < s2STOP.length-1; j++) {
				List<String> pair = new ArrayList<String>(2);
				pair.add(s2STOP[j]);
				pair.add(s2STOP[j+1]);
				if(map2.containsKey(pair)) {
					int val = map2.get(pair);
					map2.put(pair, val + 1);
				}
				else {
					map2.put(pair, 1);
				}
			}
			if(logProbBigram(map1, bitrain, unitrain, unithetas, beta) 
					> logProbBigram(map2, bitrain, unitrain, unithetas, beta)) {
				numCorrect++;
			}
		}
		
		return 2.0*numCorrect/lines.size();
	}
	
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
		
		int sz = lines.size();
		for(int i = 0; i < sz; i++) {
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
		count.put("*U*", 0);
		count.put("STOPSYMBOL", 2*sz);
		
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
		
		if(midProb == -1) { //starter/"uninitialized" value for this variable
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
		List<String> goodbad = readFile(Paths.get(args[3]));
		
		HashMap<String, Integer> trainCounts = getCounts(readFile(train));
		HashMap<String, Integer> heldOutCounts = getCounts(readFile(heldout));
		
		HashMap<List<String>, Integer> trainCountsBigram = getCountsBigram(readFile(train));
		HashMap<List<String>, Integer> heldOutCountsBigram = getCountsBigram(readFile(heldout));
		HashMap<List<String>, Integer> testCountsBigram = getCountsBigram(readFile(test));
		
		int nw = trainCounts.size();
		int tw = sum(trainCounts.values().iterator());
		HashMap<String, Double> theta1 = getThetas(trainCounts, nw, tw, 1);
		double logProbA1B1 = logProbBigram(testCountsBigram, trainCountsBigram, 
				trainCounts, theta1, 1);
		System.out.println(logProbA1B1);
		
		double AOpt = getOptimalAlpha(trainCounts, heldOutCounts, tw, nw, 0, 5, 10, .001, -1);
		System.out.println(AOpt);
		HashMap<String, Double> thetaOptimal = getThetas(trainCounts, nw, tw, AOpt);
		
		double BOpt = getOptimalBeta(heldOutCountsBigram, trainCountsBigram, trainCounts,
				thetaOptimal, 0, 100, 200, .001, -1);
		System.out.println(BOpt);
		System.out.println(guessSentences(trainCountsBigram, trainCounts, thetaOptimal,
				1, goodbad));
		System.out.println(guessSentences(trainCountsBigram, trainCounts, thetaOptimal,
				BOpt, goodbad));
	}
}
