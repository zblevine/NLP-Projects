import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class verydumb {
	
	public static void EM (HashMap<List<String>, Double> tau, List<String[]> E_corpus, List<String[]> F_corpus) {
		Iterator<List<String>> pairs = tau.keySet().iterator();
		HashMap<List<String>, Double> N = new HashMap<List<String>, Double>();
		HashMap<String, Double> N_single = new HashMap<String, Double>();
		
		while(pairs.hasNext()) { //initializes alignment counts
			List<String> curr = pairs.next();
			N.put(curr, 0.0);
		}
		
		for(int i = 0; i < E_corpus.size(); i++) {
			String[] currF = F_corpus.get(i);
			String[] currE = E_corpus.get(i);
			for(int k = 0; k < currF.length; k++) {
				double p = 0;
				for(int j = 0; j < currE.length; j++) {
					List<String> currPair = new ArrayList<String>(2);
					currPair.add(currE[j]);
					currPair.add(currF[k]);
					p += tau.get(currPair);
				}
				for(int j = 0; j < currE.length; j++) {
					List<String> currNPair = new ArrayList<String>(2);
					currNPair.add(currE[j]);
					currNPair.add(currF[k]);
					double val = N.get(currNPair);
					val += tau.get(currNPair)/p;
					N.put(currNPair, val);
				}
			}
		}
		
		Iterator<List<String>> pairsE = N.keySet().iterator();
		while(pairsE.hasNext()) {
			List<String> cp = pairsE.next();
			double v = N.get(cp);
			String eng = cp.get(0);
			if(N_single.containsKey(eng)) {
				double s = N_single.get(eng);
				N_single.put(eng, v+s);
			}
			else {
				N_single.put(eng, v);
			}
		}
		
		Iterator<List<String>> pairsM = tau.keySet().iterator();
		while(pairsM.hasNext()) {
			List<String> cp = pairsM.next();
			double n_ef = N.get(cp);
			double n_eo = N_single.get(cp.get(0));
			tau.put(cp, n_ef/n_eo);
		}
	}
	
	public static List<String[]> splitSentences(List<String> lines) {
		int numLines = lines.size();
		List<String[]> sentences = new ArrayList<String[]>(numLines);
		for(int i = 0; i < numLines; i++) {
			sentences.add(lines.get(i).split(" "));
		}
		
		return sentences;
	}
	
	public static HashMap<String, List<String>> getAlign(List<String[]> from, List<String[]> to) {
		HashMap<String, List<String>> align = new HashMap<String, List<String>>();
		for(int i = 0; i < from.size(); i++) {
			String[] toSentence = to.get(i);
			String[] fromSentence = from.get(i);
			for(int j = 0; j < fromSentence.length; j++) {
				if(align.containsKey(fromSentence[j])) {
					List<String> fw = align.get(fromSentence[j]);
					for(int k = 0; k < toSentence.length; k++) {
						if(fw.contains(toSentence[k]) == false) {
							fw.add(toSentence[k]);
						}
					}
					align.put(fromSentence[j], fw);
				}
				else {
					List<String> nfw = new ArrayList<String>();
					for(int k = 0; k < toSentence.length; k++) {
						nfw.add(toSentence[k]);
					}
					align.put(fromSentence[j], nfw);
				}
			}
		}
		
		return align;
	}
	
	public static HashMap<List<String>, Double> getInitialTau(HashMap<String, List<String>> align) {
		HashMap<List<String>, Double> tau = new HashMap<List<String>, Double>();
		
		Iterator<String> fromList = align.keySet().iterator();
		while(fromList.hasNext()) {
			String curr = fromList.next();
			List<String> toList = align.get(curr);
			for(int i = 0; i < toList.size(); i++) {
				List<String> currPair = new ArrayList<String>(2);
				currPair.add(curr);
				currPair.add(toList.get(i));
				tau.put(currPair, 1.0);
			}
		}
		
		return tau;
	}
	
	public static List<String> decode(List<String> fromLang, HashMap<String, List<String>> align,
			HashMap<List<String>, Double> tau) {
		List<String[]> fromSentences = splitSentences(fromLang);
		List<String> toLang = new ArrayList<String>();
		int sentSize = fromSentences.size();
		for(int i = 0; i < sentSize; i++) {
			String[] currS = fromSentences.get(i);
			if(currS.length <= 10) {
				List<String> toS = new ArrayList<String>();
				for(int j = 0; j < currS.length; j++) {
					String word = currS[j];
					if(align.containsKey(word)) {
						List<String> alignword = align.get(word);
						double maxProb = 0;
						String bestMatch = "ERRNOWINNER";
						int alignSize = alignword.size();
						for(int k = 0; k < alignSize; k++) {
							String w2 = alignword.get(k);
							List<String> currPair = new ArrayList<String>(2);
							currPair.add(word);
							currPair.add(w2);
							double currTau = tau.get(currPair);
							if(currTau > maxProb) {
								maxProb = currTau;
								bestMatch = w2;
							}
						}
						toS.add(bestMatch);
					}
					else {
						toS.add(word);
					}
				}
				
				String sentence = toS.get(0);
				for(int n = 1; n < currS.length; n++) {
					sentence = sentence + " " + toS.get(n);
				}
				
				toLang.add(sentence);
			}
			
			else {
				toLang.add(fromLang.get(i));
			}
		}
		
		return toLang;
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
		long start = System.currentTimeMillis();
		Path fromTrain = Paths.get(args[0]);
		Path toTrain = Paths.get(args[1]);
		Path fromTest = Paths.get(args[2]);
		
		List<String[]> fromCorps = splitSentences(readFile(fromTrain));
		List<String[]> toCorps = splitSentences(readFile(toTrain));
		List<String> fromTestCorps = readFile(fromTest);
		
		HashMap<String, List<String>> alignment = getAlign(fromCorps, toCorps);
		long alignTime = (System.currentTimeMillis() - start)/1000;
		System.out.println("align time: " + alignTime);
		HashMap<List<String>, Double> tau = getInitialTau(alignment);
		long tauTime = (System.currentTimeMillis() - start)/1000;
		System.out.println("init tau time: " + tauTime);
		for(int i = 0; i < 10; i++) {
			EM(tau, fromCorps, toCorps);
			long iterTime = (System.currentTimeMillis() - start)/1000;
			int iplusone = i+1;
			System.out.println("time after " + iplusone + "th iteration: " + iterTime);
		}
		
		List<String> toTestCorps = decode(fromTestCorps, alignment, tau);
		long decodeTime = (System.currentTimeMillis() - start)/1000;
		System.out.println("time after decoding: " + decodeTime);
		Path out = Paths.get(args[3]);
		try {
			Files.write(out, toTestCorps, Charset.forName("ISO-8859-1"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
