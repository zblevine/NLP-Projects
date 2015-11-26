import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class tag {
	public static HashMap<List<String>, Double> getSigma(List<String[]> input) {
		HashMap<String, Integer> singleCounts = new HashMap<String, Integer>();
		HashMap<List<String>, Integer> doubleCounts = new HashMap<List<String>, Integer>();
		for(int i = 0; i < input.size(); i++) {
			String[] sentence = input.get(i);
			if(singleCounts.containsKey("STOPSYMBOL")) {
				singleCounts.put("STOPSYMBOL", singleCounts.get("STOPSYMBOL") + 1);
			}
			else {
				singleCounts.put("STOPSYMBOL", 1);
			}
			
			List<String> firstPair = new ArrayList<String>(2);
			firstPair.add("STOPSYMBOL");
			firstPair.add(sentence[1]);
			if(doubleCounts.containsKey(firstPair)) {
				doubleCounts.put(firstPair, doubleCounts.get(firstPair) + 1);
			}
			else {
				doubleCounts.put(firstPair, 1);
			}
			
			for(int j = 1; j < sentence.length - 2; j += 2) {
				if(singleCounts.containsKey(sentence[j])) {
					singleCounts.put(sentence[j], singleCounts.get(sentence[j]) + 1);
				}
				else {
					singleCounts.put(sentence[j], 1);
				}
				
				List<String> currPair = new ArrayList<String>(2);
				currPair.add(sentence[j]);
				currPair.add(sentence[j+2]);
				if(doubleCounts.containsKey(currPair)) {
					doubleCounts.put(currPair, doubleCounts.get(currPair) + 1);
				}
				else {
					doubleCounts.put(currPair, 1);
				}
			}
			
			String last = sentence[sentence.length - 1];
			if(singleCounts.containsKey(last)) {
				singleCounts.put(last, singleCounts.get(last) + 1);
			}
			else {
				singleCounts.put(last, 1);
			}
			
			List<String> lastPair = new ArrayList<String>(2);
			lastPair.add(last);
			lastPair.add("STOPSYMBOL");
			if(doubleCounts.containsKey(lastPair)) {
				doubleCounts.put(lastPair, doubleCounts.get(lastPair) + 1);
			}
			else {
				doubleCounts.put(lastPair, 1);
			}
		}
		
		HashMap<List<String>, Double> sigma = new HashMap<List<String>, Double>();
		Iterator<List<String>> pairList = doubleCounts.keySet().iterator();
		while(pairList.hasNext()) {
			List<String> cp = pairList.next();
			String cw = cp.get(0);
			sigma.put(cp, 1.0*doubleCounts.get(cp)/singleCounts.get(cw));
		}
		
		return sigma;
	}
	
	public static HashMap<List<String>, Double> getTau(List<String[]> input) {
		HashMap<String, Integer> singleCounts = new HashMap<String, Integer>();
		HashMap<List<String>, Integer> doubleCounts = new HashMap<List<String>, Integer>();
		
		for(int i = 0; i < input.size(); i++) {
			String[] sentence = input.get(i);
			for(int j = 1; j < sentence.length; j += 2) {
				if(singleCounts.containsKey(sentence[j])) {
					singleCounts.put(sentence[j], singleCounts.get(sentence[j]) + 1);
				}
				else {
					singleCounts.put(sentence[j], 1);
				}
				
				List<String> currPair = new ArrayList<String>(2);
				currPair.add(sentence[j]);
				currPair.add(sentence[j-1]);
				if(doubleCounts.containsKey(currPair)) {
					doubleCounts.put(currPair, doubleCounts.get(currPair) + 1);
				}
				else {
					doubleCounts.put(currPair, 1);
				}
			}
		}
		
		HashMap<List<String>, Double> tau = new HashMap<List<String>, Double>();
		Iterator<List<String>> pairList = doubleCounts.keySet().iterator();
		while(pairList.hasNext()) {
			List<String> cp = pairList.next();
			String cw = cp.get(0);
			tau.put(cp, 1.0*doubleCounts.get(cp)/(singleCounts.get(cw)));
		}
		
		Iterator<String> singleList = singleCounts.keySet().iterator();
		while(singleList.hasNext()) {
			String curr = singleList.next();
			List<String> unkPair = new ArrayList<String>(2);
			unkPair.add(curr);
			unkPair.add("*UNK*");
			tau.put(unkPair, 1.0);
		}
		
		return tau;
	}
	
	public static List<String> getTags(List<String[]> input) {
		List<String> tags = new ArrayList<String>();
		for(int i = 0; i < input.size(); i++) {
			String[] currSent = input.get(i);
			for(int j = 1; j < currSent.length; j += 2) {
				if(tags.contains(currSent[j]) == false) {
					tags.add(currSent[j]);
				}
			}
		}
		
		return tags;
	}
	
	public static double viterbi(String[] input, HashMap<List<String>, Double> sigma,
			HashMap<List<String>, Double> tau, int i, String tag, 
			List<String> tagSet, List<HashMap<String, Double>> viterbiValues,
			List<HashMap<String, String>> prevValues) {
		List<String> tauPair = new ArrayList<String>(2);
		
		if(i == 1) {
			List<String> sigmaPair = new ArrayList<String>(2);
			sigmaPair.add("STOPSYMBOL");
			sigmaPair.add(tag);
			tauPair.add(tag);
			tauPair.add(input[0]);
			
			double sigmaVal = 0;
			if(sigma.containsKey(sigmaPair)) {
				sigmaVal = sigma.get(sigmaPair);
			}
			
			double tauVal = 0;
			if(tau.containsKey(tauPair)) {
				tauVal = tau.get(tauPair);
			}
			
			if(viterbiValues.get(i-1).containsKey(tag) == false) {
				viterbiValues.get(i-1).put(tag, sigmaVal*tauVal);
			}
			
			return sigmaVal*tauVal;
		}
		
		else if (i == ((input.length/2) + 1)) { //figuring out the end of the sentence
			double maxProb = 0.0;
			String bestTag = "ERRNOWINNER";
			Iterator<String> tagIt = tagSet.iterator();
			while(tagIt.hasNext()) {
				String currTag = tagIt.next();
				List<String> sigmaPair = new ArrayList<String>(2);
				sigmaPair.add(currTag);
				sigmaPair.add(tag);
				if(sigma.containsKey(sigmaPair)) {
					double prevProb = 0;
					if(viterbiValues.get(i-2).containsKey(currTag)) {
						prevProb = viterbiValues.get(i-2).get(currTag);
					}
					else {
						prevProb = viterbi(input, sigma, tau, i-1, currTag, tagSet, 
								viterbiValues, prevValues);
					}
					double currProb = prevProb*sigma.get(sigmaPair);
					if(currProb > maxProb) {
						maxProb = currProb;
						bestTag = currTag;
					}
				}
				
			}
			
			prevValues.get(i-2).put(tag, bestTag);
			return maxProb;
		}
		
		else {
			tauPair.add(tag);
			tauPair.add(input[2*(i-1)]);
			double tauVal = 0;
			if(tau.containsKey(tauPair)) {
				tauVal = tau.get(tauPair);
			}
			
			double maxProb = 0;
			String bestTag = "ERRNOWINNER";
			Iterator<String> tagIt = tagSet.iterator();
			while(tagIt.hasNext()) {
				String currTag = tagIt.next();
				List<String> sigmaPair = new ArrayList<String>(2);
				sigmaPair.add(currTag);
				sigmaPair.add(tag);
				if(sigma.containsKey(sigmaPair) && tau.containsKey(tauPair)) {
					double prevProb = 0;
					if(viterbiValues.get(i-2).containsKey(currTag)) {
						prevProb = viterbiValues.get(i-2).get(currTag);
					}
					else {
						prevProb = viterbi(input, sigma, tau, i-1, currTag, tagSet, 
								viterbiValues, prevValues);
					}
					double currProb = prevProb*sigma.get(sigmaPair)*tauVal;
					if(currProb > maxProb) {
						maxProb = currProb;
						bestTag = currTag;
					}
				}
			}
			
			if(viterbiValues.get(i-1).containsKey(tag) == false) {
				viterbiValues.get(i-1).put(tag, maxProb);
				prevValues.get(i-2).put(tag, bestTag);
			}
			return maxProb;
		}
	}
	
	public static List<String> getTagSeq(List<HashMap<String, String>> prev, String lastTag) {
		List<String> tags = new ArrayList<String>();
		for(int i = 0; i < prev.size(); i++) {
			tags.add("");
		}
		
		String currTag = lastTag;
		for(int c = prev.size()-1; c >= 0; c--) {
			String prevTag = prev.get(c).get(currTag);
			tags.set(c, prevTag);
			currTag = prevTag;
		}
		
		return tags;
	}
	
	public static String tagSentence(String[] input, List<String> tags) {
		for(int i = 1; i < input.length; i += 2) {
			input[i] = tags.get((i-1)/2);
		}
		
		String sentence = input[0];
		for(int j = 1; j < input.length; j++) {
			sentence = sentence + " " + input[j];
		}
		
		return sentence;
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
	
	public static void main(String[] args) {
		//long start = System.currentTimeMillis();
		List<String[]> trainwsj = splitSentences(readFile(Paths.get(args[0])));
		List<String[]> testwsj = splitSentences(readFile(Paths.get(args[1])));
		
		HashMap<List<String>, Double> sigma = getSigma(trainwsj);
		HashMap<List<String>, Double> tau = getTau(trainwsj);
		List<String> tags = getTags(trainwsj);
		
		int sz = testwsj.size();
		List<String> taggedTest = new ArrayList<String>();
		
		for(int i = 0; i < sz; i++) {
			String[] currSent = testwsj.get(i);
			
			List<HashMap<String, Double>> viterbiValues = new ArrayList<HashMap<String, Double>>();
			List<HashMap<String, String>> prevValues = new ArrayList<HashMap<String, String>>();
			for(int k = 0; k < currSent.length/2; k++) {
				HashMap<String, Double> hm = new HashMap<String, Double>();
				viterbiValues.add(hm);
				HashMap<String, String> hms = new HashMap<String, String>();
				prevValues.add(hms);
			}
			
			viterbi(currSent, sigma, tau, (currSent.length/2) + 1, "STOPSYMBOL", tags,
					viterbiValues, prevValues);
			
			List<String> tagSeq = getTagSeq(prevValues, "STOPSYMBOL");
			taggedTest.add(tagSentence(currSent, tagSeq));
		}
		
		Path out = Paths.get(args[2]);
		try {
			Files.write(out, taggedTest, Charset.forName("ISO-8859-1"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
