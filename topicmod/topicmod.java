import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class topicmod {
	public static void gibbsSample(HashMap<List<String>, Integer> documentTopic,
			HashMap<List<String>, Integer> topicWord, HashMap<List<String>, String> topicAssign,
			HashMap<String, Integer> wholeDocument, HashMap<String, Integer> wholeTopic, 
			List<List<String>> topics, List<List<String>> articles, int V, double alpha, Random rand) {
		int as = articles.size();
		int ts = topics.size();
		for(int i = 0; i < as; i++) {
			String docNumber = Integer.toString(i);
			List<String> art_i = articles.get(i);
			for(int w = 0; w < art_i.size(); w++) {
				//Step 1: "removing" the word from document and decrementing counts
				String word = art_i.get(w);
				String wordNumber = Integer.toString(w);
				
				List<String> d_w = new ArrayList<String>(2);
				d_w.add(docNumber);
				d_w.add(wordNumber);
				String topicNumber = topicAssign.get(d_w);
				//System.out.println(topicNumber);
				List<String> docTop = new ArrayList<String>(2);
				docTop.add(docNumber);
				docTop.add(topicNumber);
				List<String> topWord = new ArrayList<String>(2);
				topWord.add(topicNumber);
				topWord.add(word);
				
				documentTopic.put(docTop, documentTopic.get(docTop)-1);
				topicWord.put(topWord, topicWord.get(topWord)-1);
				wholeDocument.put(docNumber, wholeDocument.get(docNumber)-1);
				wholeTopic.put(topicNumber, wholeTopic.get(topicNumber)-1);
				
				List<String> t_n = topics.get(Integer.parseInt(topicNumber));
				t_n.remove(word);
				topics.set(Integer.parseInt(topicNumber), t_n);
				
				//Step 2: measuring probability of each topic
				double p = 0;
				double[] q = new double[ts];
				double[] dt = new double[ts];
				double[] X = new double[ts];
				for(int a = 0; a < ts; a++) {
					String str_a = Integer.toString(a);
					List<String> doc_a = new ArrayList<String>(2);
					doc_a.add(docNumber);
					doc_a.add(str_a);
					List<String> a_word = new ArrayList<String>(2);
					a_word.add(str_a);
					a_word.add(word);
					
					double dtTop = -1;
					if(documentTopic.containsKey(doc_a)) {
						if(topicWord.containsKey(a_word)) {
							dtTop = (alpha + documentTopic.get(doc_a))*(alpha + topicWord.get(a_word));
						}
						else {
							dtTop = (alpha + documentTopic.get(doc_a))*alpha;
						}
					}
					else {
						if(topicWord.containsKey(a_word)) {
							dtTop = alpha*(alpha + topicWord.get(a_word));
						}
						else {
							dtTop = alpha*alpha;
						}
					}
					double dtBottom = (ts*alpha + wholeDocument.get(docNumber))*(V*alpha + wholeTopic.get(str_a));
					dt[a] = dtTop/dtBottom;
					p += dt[a];
				}
				double xx = 0;
				for(int b = 0; b < ts; b++) {
					q[b] = dt[b]/p;
					X[b] = xx + q[b];
					xx += q[b];
				}
				
				//Step 3: randomly assigning a new topic based on distribution
				double ra = rand.nextDouble();
				int new_t = -1;
				for(int c = 0; c < ts; c++) {
					if(c == 0) {
						if(ra <= X[0]) {
							new_t = 0;
						}
					}
					else {
						if(ra <= X[c] && ra > X[c-1]) {
							new_t = c;
						}
					}
				}
				List<String> top_t = topics.get(new_t);
				top_t.add(word);
				topics.set(new_t, top_t);
				
				//Step 4: re-incrementing counts
				String str_new_t = Integer.toString(new_t);
				List<String> docTopT = new ArrayList<String>(2);
				docTopT.add(docNumber);
				docTopT.add(str_new_t);
				List<String> topWordT = new ArrayList<String>(2);
				topWordT.add(str_new_t);
				topWordT.add(word);
				
				topicAssign.put(d_w, str_new_t);
				if(documentTopic.containsKey(docTopT)) {
					documentTopic.put(docTopT, documentTopic.get(docTopT)+1);
				}
				else {
					documentTopic.put(docTopT, 1);
				}
				if(topicWord.containsKey(topWordT)) {
					topicWord.put(topWordT, topicWord.get(topWordT)+1);
				}
				else {
					topicWord.put(topWordT, 1);
				}
				
				wholeDocument.put(docNumber, wholeDocument.get(docNumber)+1);
				wholeTopic.put(str_new_t, wholeDocument.get(str_new_t)+1);
			}
		}
	}
	
	public static void getOriginalSample(HashMap<List<String>, Integer> documentTopic,
			HashMap<List<String>, Integer> topicWord, HashMap<List<String>, String> topicAssign,
			HashMap<String, Integer> wholeDocument, HashMap<String, Integer> wholeTopic,
			HashMap<String, Integer> wordCount, List<List<String>> topics, 
			List<List<String>> articles, Random ra) {
		int as = articles.size();
		for(int k = 0; k < 50; k++) {
			topics.add(new ArrayList<String>());
		}
		for(int i = 0; i < as; i++) {
			String docNumber = Integer.toString(i);
			List<String> art_i = articles.get(i);
			for(int w = 0; w < art_i.size(); w++) {
				String word = art_i.get(w);
				String wordNumber = Integer.toString(w);
				int n = ra.nextInt(50);
				String topicNumber = Integer.toString(n);
				
				List<String> d_w = new ArrayList<String>(2);
				d_w.add(docNumber);
				d_w.add(wordNumber);
				List<String> docTop = new ArrayList<String>(2);
				docTop.add(docNumber);
				docTop.add(topicNumber);
				List<String> topWord = new ArrayList<String>(2);
				topWord.add(topicNumber);
				topWord.add(word);
				
				List<String> top_n = topics.get(n);
				top_n.add(word);
				topics.set(n, top_n);
				topicAssign.put(d_w, topicNumber);
				
				if(documentTopic.containsKey(docTop)) {
					documentTopic.put(docTop, documentTopic.get(docTop)+1);
				}
				else {
					documentTopic.put(docTop, 1);
				}
				
				if(topicWord.containsKey(topWord)) {
					topicWord.put(topWord, topicWord.get(topWord)+1);
				}
				else {
					topicWord.put(topWord, 1);
				}
				
				if(wholeDocument.containsKey(docNumber)) {
					wholeDocument.put(docNumber, wholeDocument.get(docNumber)+1);
				}
				else {
					wholeDocument.put(docNumber, 1);
				}
				
				if(wholeTopic.containsKey(topicNumber)) {
					wholeTopic.put(topicNumber, wholeTopic.get(topicNumber)+1);
				}
				else {
					wholeTopic.put(topicNumber, 1);
				}
				
				if(wordCount.containsKey(word)) {
					wordCount.put(word, wordCount.get(word)+1);
				}
				else {
					wordCount.put(word, 1);
				}
			}
		}
	}
	
	public static int getV(List<List<String>> articles) {
		Set<String> vocab = new HashSet<String>();
		
		Iterator<List<String>> ar = articles.iterator();
		while(ar.hasNext()) {
			Iterator<String> a_i = ar.next().iterator();
			while(a_i.hasNext()) {
				vocab.add(a_i.next());
			}
		}
		
		return vocab.size();
	}
	
	public static List<List<String>> getArticles(List<String[]> sentences) {
		List<List<String>> articles = new ArrayList<List<String>>(1000);
		int s = sentences.size();
		for(int i = 2; i < s; i++) {
			ArrayList<String> art = new ArrayList<String>();
			while(sentences.get(i).length > 1) {
				for(int j = 1; j < sentences.get(i).length; j++) {
					art.add(sentences.get(i)[j]);
				}
				if(i == s-1){
					break;
				}
				i++;
				
			}
			
			articles.add(art);
		}
		
		return articles;
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
	
	public static void printLogLikelihood(HashMap<List<String>, Integer> documentTopic,
			HashMap<List<String>, Integer> topicWord, 
			HashMap<String, Integer> wholeDocument, HashMap<String, Integer> wholeTopic, 
			List<List<String>> topics, List<List<String>> articles, int V, double alpha,
			List<String> output) {
		double logProb = 0;
		int as = articles.size();
		for(int i = 0; i < as; i++) {
			List<String> art_i = articles.get(i);
			int ais = art_i.size();
			for(int w = 0; w < ais; w++) {
				double sum = 0;
				int ts = topics.size();
				for(int t = 0; t < ts; t++) {
					List<String> d_t = new ArrayList<String>(2);
					d_t.add(Integer.toString(i));
					d_t.add(Integer.toString(t));
					List<String> t_w = new ArrayList<String>(2);
					t_w.add(Integer.toString(t));
					t_w.add(art_i.get(w));
					double dtTop = -1;
					if(documentTopic.containsKey(d_t)) {
						if(topicWord.containsKey(t_w)) {
							dtTop = (alpha + documentTopic.get(d_t))*(alpha + topicWord.get(t_w));
						}
						else {
							dtTop = (alpha + documentTopic.get(d_t))*alpha;
						}
					}
					else {
						if(topicWord.containsKey(t_w)) {
							dtTop = alpha*(alpha + topicWord.get(t_w));
						}
						else {
							dtTop = alpha*alpha;
						}
					}
					sum += dtTop/((ts*alpha + wholeDocument.get(Integer.toString(i)))*(V*alpha + wholeTopic.get(Integer.toString(t))));
				}
				logProb += Math.log(sum);
			}
		}
		output.add("Log probability: " + logProb);
	}
	
	public static void printArticle17TopicProbs(List<String> article17,
			HashMap<List<String>, String> topicAssign, List<String> output) {
		int[] topicNumbers = new int[50];
		for(int i = 0; i < article17.size(); i++) {
			List<String> ta = new ArrayList<String>(2);
			ta.add(Integer.toString(16));
			ta.add(Integer.toString(i));
			int t = Integer.parseInt(topicAssign.get(ta));
			topicNumbers[t]++;
		}
		
		for(int j = 0; j < 50; j++) {
			output.add("The probability of topic " + j + " is:" + 1.0*topicNumbers[j]/article17.size());
		}
		
	}
	
	public static void printBest15(List<List<String>> topics, HashMap<List<String>, Integer> topicWords,
			HashMap<String, Integer> wordCount, double theta, List<String> output) {
		int N = topics.size();
		for(int i = 0; i < N; i++) {
			output.add("The 15 best words for topic " + i + ":");
			HashMap<String, Double> wordProbs = new HashMap<String, Double>();
			for(int j = 0; j < topics.get(i).size(); j++) {
				String w = topics.get(i).get(j);
				List<String> tw = new ArrayList<String>(2);
				tw.add(Integer.toString(i));
				tw.add(w);
				double p = (topicWords.get(tw) + theta)/(wordCount.get(w) + theta*topics.size());
				wordProbs.put(w, p);
			}
			List<String> doNotSearch = new ArrayList<String>(15);
			int b = wordProbs.size();
			if(b > 15) {
				b = 15;
			}
			for(int k = 0; k < b; k++) {
				Iterator<String> wp = wordProbs.keySet().iterator();
				double maxProb = -1;
				String bestWord = null;
				
				while(wp.hasNext()) {
					String w = wp.next();
					if(doNotSearch.contains(w) == false) {
						double prob = wordProbs.get(w);
						if(prob > maxProb) {
							maxProb = prob;
							bestWord = w;
						}
					}
				}

				doNotSearch.add(bestWord);
				output.add(bestWord);
			}
			output.add("");
		}
	}
	
	public static void main(String[] args) {
		List<List<String>> articles = getArticles(splitSentences(readFile(Paths.get(args[0]))));
		int V = getV(articles);
		double alpha = 0.5;
		Random rand = new Random();
		
		HashMap<List<String>, Integer> documentTopic = new HashMap<List<String>, Integer>();
		HashMap<List<String>, Integer> topicWord = new HashMap<List<String>, Integer>();
		HashMap<List<String>, String> topicAssign = new HashMap<List<String>, String>();
		HashMap<String, Integer> wholeDocument = new HashMap<String, Integer>();
		HashMap<String, Integer> wholeTopic = new HashMap<String, Integer>(); 
		HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
		List<List<String>> topics = new ArrayList<List<String>>();
		
		getOriginalSample(documentTopic, topicWord, topicAssign, wholeDocument, wholeTopic,
				wordCount, topics, articles, rand);
		
		for(int i = 0; i < 10; i++) {
			gibbsSample(documentTopic, topicWord, topicAssign, wholeDocument, wholeTopic,
					topics, articles, V, alpha, rand);
		}
		
		List<String> out = new ArrayList<String>();
		
		printLogLikelihood(documentTopic, topicWord, wholeDocument, wholeTopic, topics, articles,
				V, alpha, out);
		printArticle17TopicProbs(articles.get(16), topicAssign, out);
		printBest15(topics, topicWord, wordCount, 5.0, out);
		
		Path op = Paths.get(args[1]);
		try {
			Files.write(op, out, Charset.forName("ISO-8859-1"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
